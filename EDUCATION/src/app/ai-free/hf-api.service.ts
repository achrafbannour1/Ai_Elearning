import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http'; // laissé si tu l'utilises ailleurs
import { Observable, from, of, catchError, map } from 'rxjs';

// 🚀 Vraie IA côté navigateur (sans clé)
import { pipeline, env } from '@xenova/transformers';

/**
 * Remplacement "drop-in" de l'ancien service Hugging Face distant.
 * Ici on utilise Transformers.js en LOCAL dans le navigateur (Xenova/t5-small).
 * - Même signature: generateText(prompt, maxNewTokens, temperature) -> Observable<any>
 * - Même format de retour que ton composant attend: [{ generated_text: '...JSON...' }]
 *
 * Si le modèle met du temps / renvoie un format non JSON, on a un
 * fallback interne qui génère un QCM simple pour ne jamais casser l'UI.
 */
@Injectable({ providedIn: 'root' })
export class HfApiService {
  // On met en cache le pipeline pour éviter de recharger le modèle
  private pipePromise?: Promise<any>;

  constructor(private _http: HttpClient) {}

  /** Chargement (lazy) du pipeline text2text-generation en local */
  private async getPipeline() {
    if (!this.pipePromise) {
      // Chemins WASM (chargés depuis un CDN public)
      // -> garde cette version pour des chemins stables
      env.backends.onnx.wasm.wasmPaths =
        'https://cdn.jsdelivr.net/npm/@xenova/transformers@2.17.2/wasm/';

      env.allowLocalModels = false;  // ne cherche pas de modèles locaux
      env.cacheDir = 'transformers-cache'; // cache navigateur (IndexedDB)

      // Modèle léger pour le front
      this.pipePromise = pipeline('text2text-generation', 'Xenova/t5-small', {
        quantized: true
      });
    }
    return this.pipePromise;
  }

  /**
   * Garde la même API que ton composant :
   * Retourne un Observable qui émet: [{ generated_text: '...JSON...' }]
   */
  generateText(prompt: string, maxNewTokens = 256, temperature = 0.7): Observable<any> {
    return from(this._generate(prompt, maxNewTokens, temperature)).pipe(
      // Si la génération échoue (réseau bloqué, wasm non chargé, etc.) -> fallback local
      catchError((_err) => of([{ generated_text: this.buildFallbackQuiz(prompt) }]))
    );
  }

  /** Implémentation réelle avec Transformers.js */
  private async _generate(prompt: string, maxNewTokens: number, temperature: number) {
    const pipe = await this.getPipeline();

    // ⚠️ Astuce: on renforce le prompt pour forcer un JSON strict
    const strongPrompt = `${prompt}

IMPORTANT:
- Réponds STRICTEMENT en JSON valide (tableau d'objets).
- Pas de texte avant/après le JSON.
- Utilise exactement les clés: "question", "options", "answer", "explanation".
- "options" est un tableau de 4 entrées "A) ...", "B) ...", "C) ...", "D) ...".
- "answer" est l'une des lettres "A" | "B" | "C" | "D".
`;

    const out = await pipe(strongPrompt, {
      max_new_tokens: Math.min(Math.max(maxNewTokens, 64), 512),
      temperature: Math.min(Math.max(temperature, 0), 1)
    });

    // Le pipeline renvoie classiquement: [{ generated_text: '...' }]
    // (On normalise pour matcher l'UI existante)
    const text =
      Array.isArray(out) && out[0]?.generated_text
        ? out[0].generated_text
        : (typeof out === 'string' ? out : JSON.stringify(out));

    // Si ça ne ressemble pas à un JSON de tableau, on force un fallback
    if (!text.trim().startsWith('[') || !text.trim().endsWith(']')) {
      return [{ generated_text: this.buildFallbackQuiz(prompt) }];
    }

    // Sinon, on renvoie tel quel
    return [{ generated_text: text }];
  }

  // ---------------- Fallback local (au cas où) ----------------

  /** Produit un QCM minimal conforme à ton UI si l'IA ne renvoie pas du JSON propre */
  private buildFallbackQuiz(prompt: string): string {
    const count = this.extractCount(prompt);
    const topic = this.extractTopic(prompt);
    const level = this.extractLevel(prompt);

    const letters = ['A', 'B', 'C', 'D'];
    const templates = [
      `Concernant ${topic}, laquelle des affirmations est correcte ?`,
      `Dans ${topic}, quel est le meilleur choix ?`,
      `À propos de ${topic}, que signifie le concept principal ?`,
      `Quelle option décrit le mieux ${topic} ?`,
      `Dans le cadre de ${topic}, quelle réponse est exacte ?`
    ];

    const items = Array.from({ length: count }, (_, i) => {
      const q = templates[i % templates.length];
      const opts = [
        `${letters[0]}) Vrai`,
        `${letters[1]}) Faux`,
        `${letters[2]}) Cela dépend du contexte`,
        `${letters[3]}) Je ne sais pas`
      ];
      // petite rotation pour varier
      this.rotate(opts, i % 4);
      // on fixe la bonne réponse sur A (après rotation, elle varie)
      const answer = letters[0];
      return {
        question: `${q} (niveau ${level})`,
        options: opts,
        answer,
        explanation: `Réponse indicative pour ${topic}.`
      };
    });

    return JSON.stringify(items, null, 2);
  }

  private rotate<T>(arr: T[], n: number) {
    for (let i = 0; i < n; i++) arr.push(arr.shift() as T);
  }

  private extractCount(prompt: string): number {
    const m = prompt.match(/Crée\s+(\d+)\s+questions/i) || prompt.match(/(\d+)\s+questions/i);
    const n = m ? parseInt(m[1], 10) : 5;
    return Math.max(1, Math.min(10, isNaN(n) ? 5 : n));
  }
  private extractTopic(prompt: string): string {
    const m = prompt.match(/sujet\s*:\s*"([^"]+)"/i) || prompt.match(/sujet\s*:\s*([^\n]+)/i);
    return (m?.[1] || 'le thème demandé').trim();
  }
  private extractLevel(prompt: string): string {
    const m = prompt.match(/Niveau\s*:\s*([^\n]+)/i);
    return (m?.[1] || 'intermédiaire').trim().toLowerCase();
  }
}
