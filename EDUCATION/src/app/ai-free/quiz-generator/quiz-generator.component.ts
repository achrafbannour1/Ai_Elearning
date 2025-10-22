import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HfApiService } from '../hf-api.service';

type QuizItem = {
  question: string;
  options: string[];
  answer: string;
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
    count: [5, [Validators.required, Validators.min(1), Validators.max(10)]]
  });

  loading = signal(false);
  error = signal<string | null>(null);
  rawText = signal<string>('');
  quiz = signal<QuizItem[]>([]);

  constructor(private fb: FormBuilder, private hf: HfApiService) {}

  generate() {
  if (this.form.invalid) { this.form.markAllAsTouched(); return; }
  this.loading.set(true);
  this.error.set(null);
  this.quiz.set([]);
  this.rawText.set('');

  const { topic, level, count } = this.form.getRawValue()!;
  const safeCount = Number(count) || 5;

  const prompt =
`Tu es un générateur de QCM pour enseignants.

Tâche: Crée ${safeCount} questions QCM en FRANÇAIS sur le sujet: "${topic}".
Niveau: ${level}.
Format SORTIE STRICTEMENT en JSON (rien d'autre que du JSON !), avec ce schéma:
[
  {
    "question": "…",
    "options": ["A) …","B) …","C) …","D) …"],
    "answer": "A",
    "explanation": "…"
  }
]

Contraintes:
- 1 seule bonne réponse par question (A, B, C ou D).
- Pas de texte avant ou après le JSON.
- Explications concises (1-2 phrases).
`;

  this.hf.generateText(prompt, 512, 0.7).subscribe({
    next: (res) => {
      const text = Array.isArray(res) && res[0]?.generated_text
        ? res[0].generated_text
        : (typeof res === 'string' ? res : JSON.stringify(res));
      this.rawText.set(text);

      const parsed = this.tryParseQuizJson(text);
      if (!parsed) {
        this.error.set('Réponse inattendue du modèle. Réessaie (ou diminue le nombre de questions).');
      } else {
        this.quiz.set(parsed);
      }
      this.loading.set(false);
    },
    error: (err) => {
      console.error(err);
      if (err?.status === 401 || err?.status === 403) {
        this.error.set('Clé Hugging Face invalide ou permissions insuffisantes (401/403).');
      } else if (err?.status === 404) {
        this.error.set('Modèle introuvable (404). Vérifie le nom/URL — un fallback a déjà été tenté.');
      } else if (err?.status === 429) {
        this.error.set('Quota/Rate limit atteint (429). Réessaie plus tard.');
      } else if (err?.status === 503) {
        this.error.set('Le modèle démarre (503). Réessaie dans quelques secondes.');
      } else {
        this.error.set('Erreur API Hugging Face ou réseau.');
      }
      this.loading.set(false);
    }
  });
}


  private tryParseQuizJson(text: string): QuizItem[] | null {
    try {
      // Cherche le premier '[' et le dernier ']' pour isoler un tableau JSON
      const start = text.indexOf('[');
      const end = text.lastIndexOf(']');
      if (start === -1 || end === -1 || end <= start) return null;
      const jsonStr = text.slice(start, end + 1);
      const arr = JSON.parse(jsonStr);
      if (!Array.isArray(arr)) return null;

      // Normalisation minimale
      return arr.map((q: any) => ({
        question: String(q.question ?? ''),
        options: Array.isArray(q.options) ? q.options.map((o: any) => String(o)) : [],
        answer: String(q.answer ?? ''),
        explanation: q.explanation ? String(q.explanation) : undefined
      })) as QuizItem[];
    } catch {
      return null;
    }
  }

  copyJson() {
    const data = JSON.stringify(this.quiz(), null, 2);
    navigator.clipboard.writeText(data);
  }
}
