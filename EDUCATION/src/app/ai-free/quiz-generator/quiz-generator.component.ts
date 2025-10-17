import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HfApiService } from '../hf-api.service';

type QuizItem = {
  question: string;
  options: string[]; // ["A) ...","B) ...","C) ...","D) ..."]
  answer: string;    // "A" | "B" | "C" | "D"
  explanation?: string;
};

@Component({
  selector: 'app-quiz-generator',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './quiz-generator.component.html',
  styleUrls: ['./quiz-generator.component.css']
})
export class QuizGeneratorComponent {

  form = this.fb.group({
    topic: ['', [Validators.required, Validators.minLength(2)]],
    level: ['intermédiaire', Validators.required], // débutant | intermédiaire | avancé
    count: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
    context: [''] // ⭐ Contexte (RAG light)
  });

  loading = signal(false);
  error = signal<string | null>(null);
  rawText = signal<string>('');
  quiz = signal<QuizItem[]>([]);

  // ⭐ Réponses étudiant + correction
  selected = signal<Record<number, string>>({});
  graded = signal(false);
  score = signal<number>(0);

  // ⭐ Explications IA par question
  aiExplain = signal<Record<number, string>>({});

  // ✅ Tableau de lettres pour le template (évite ('ABCD'[j]))
  letters: Array<'A' | 'B' | 'C' | 'D'> = ['A', 'B', 'C', 'D'];

  constructor(private fb: FormBuilder, private hf: HfApiService) {}

  // --------------------------------------------------
  // 1) Génération du QCM (avec CONTEXTE + JSON strict)
  // --------------------------------------------------
  generate() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set(null);
    this.quiz.set([]);
    this.rawText.set('');
    this.selected.set({});
    this.graded.set(false);
    this.score.set(0);
    this.aiExplain.set({});

    const { topic, level, count, context } = this.form.getRawValue()!;
    const safeCount = Number(count) || 5;
    const ctx = (context ?? '').trim();

    const prompt =
`Tu es un générateur de QCM pour enseignants. Respecte strictement le format demandé.

CONTEXTE (peut être vide): """${ctx}"""
SUJET: "${topic}"
NIVEAU: ${level}
NOMBRE: ${safeCount}

RÈGLES:
- Crée ${safeCount} questions factuelles et vérifiables. Si CONTEXTE n'est pas vide, NE POSE que des questions couvertes par ce contexte.
- 4 options EXACTEMENT par question, au format "A) ...","B) ...","C) ...","D) ...".
- 1 seule bonne réponse ("answer" ∈ {"A","B","C","D"}).
- "explanation" courte (1-2 phrases), basée sur le CONTEXTE si fourni.
- Retourne UNIQUEMENT un JSON minifié valide (tableau d'objets).

EXEMPLE (style attendu) — NE PAS COPIER LE CONTENU:
[
  {"question":"...","options":["A) ...","B) ...","C) ...","D) ..."],"answer":"B","explanation":"..."}
]

SORTIE ATTENDUE (ton JSON final, minifié):`;

    this.hf.generateText(prompt, 512, 0.2).subscribe({
      next: (res) => {
        const text = Array.isArray(res) && res[0]?.generated_text
          ? res[0].generated_text
          : (typeof res === 'string' ? res : JSON.stringify(res));
        this.rawText.set(text);

        const parsed = this.tryParseQuizJson(text);
        const valid = parsed ? this.validateQuiz(parsed, safeCount) : null;

        if (!valid) {
          this.error.set('Réponse inattendue du modèle. Réessaie ou fournis un CONTEXTE.');
        } else {
          this.quiz.set(valid);
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error(err);
        if (err?.status === 401 || err?.status === 403) {
          this.error.set('Clé Hugging Face invalide ou permissions insuffisantes (401/403).');
        } else if (err?.status === 404) {
          this.error.set('Modèle introuvable (404).');
        } else if (err?.status === 429) {
          this.error.set('Quota/Rate limit atteint (429).');
        } else if (err?.status === 503) {
          this.error.set('Le modèle démarre (503).');
        } else {
          this.error.set('Erreur IA locale / pipeline.');
        }
        this.loading.set(false);
      }
    });
  }

  // -------------------------------------------
  // 2) Sélection et notation côté étudiant
  // -------------------------------------------
  selectOption(qIndex: number, letter: 'A'|'B'|'C'|'D') {
    if (this.graded()) return;
    const curr = { ...this.selected() };
    curr[qIndex] = letter;
    this.selected.set(curr);
  }

  grade() {
    const items = this.quiz();
    if (!items.length) return;
    let good = 0;
    const picks = this.selected();

    items.forEach((q, i) => {
      if (picks[i] && picks[i] === q.answer) good++;
    });

    this.score.set(good);
    this.graded.set(true);
  }

  // -------------------------------------------
  // 3) Explication IA (vérification/justification)
  // -------------------------------------------
  explainWithAI(i: number) {
    const q = this.quiz()[i];
    if (!q) return;

    const picked = this.selected()[i];
    const prompt =
`Explique brièvement si la réponse choisie par l'étudiant est correcte.

Question: ${q.question}
Options: ${q.options.join(' | ')}
Bonne réponse attendue: ${q.answer}
Réponse choisie par l'étudiant: ${picked || '(non répondue)'}

Consignes:
- Réponds en 1-2 phrases en FRANÇAIS.
- Sois factuel. Si l'étudiant n'a pas répondu, dis-le.
- Ne retourne PAS de JSON, juste du texte concis.`;

    this.hf.generateText(prompt, 96, 0.2).subscribe({
      next: (res) => {
        const text = Array.isArray(res) && res[0]?.generated_text
          ? String(res[0].generated_text).trim()
          : (typeof res === 'string' ? res : JSON.stringify(res));
        const curr = { ...this.aiExplain() };
        curr[i] = text;
        this.aiExplain.set(curr);
      },
      error: () => {
        const curr = { ...this.aiExplain() };
        curr[i] = 'Impossible de générer une explication IA pour le moment.';
        this.aiExplain.set(curr);
      }
    });
  }

  // -------------------------------------------
  // Utils: parsing + validation stricte
  // -------------------------------------------
  private tryParseQuizJson(text: string): QuizItem[] | null {
    try {
      const start = text.indexOf('[');
      const end = text.lastIndexOf(']');
      if (start === -1 || end === -1 || end <= start) return null;
      const jsonStr = text.slice(start, end + 1);
      const arr = JSON.parse(jsonStr);
      if (!Array.isArray(arr)) return null;

      return arr.map((q: any) => ({
        question: String(q.question ?? '').trim(),
        options: Array.isArray(q.options) ? q.options.map((o: any) => String(o)) : [],
        answer: String(q.answer ?? '').trim().toUpperCase(),
        explanation: q.explanation ? String(q.explanation).trim() : undefined
      })) as QuizItem[];
    } catch {
      return null;
    }
  }

  private validateQuiz(items: QuizItem[], expectedCount: number): QuizItem[] | null {
    const letters = ['A','B','C','D'];
    const cleaned: QuizItem[] = [];

    for (const it of items) {
      if (!it.question || it.question.length < 8) return null;
      if (!Array.isArray(it.options) || it.options.length !== 4) return null;

      // Force le préfixe "A) ...", etc., s'il manque
      it.options = it.options.map((o, i) => {
        const t = String(o).trim();
        const want = `${letters[i]}) `;
        return t.startsWith(`${letters[i]})`) ? t : want + t.replace(/^[A-D]\)\s*/, '');
      });

      if (!letters.includes(it.answer)) return null;

      const idx = letters.indexOf(it.answer);
      if (idx < 0 || !it.options[idx]) return null;

      cleaned.push(it);
      if (cleaned.length === expectedCount) break;
    }

    return cleaned.length ? cleaned : null;
  }

  copyJson() {
    const data = JSON.stringify(this.quiz(), null, 2);
    navigator.clipboard.writeText(data);
  }
}
