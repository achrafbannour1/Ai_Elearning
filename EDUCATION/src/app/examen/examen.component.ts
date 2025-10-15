import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Examen, ExamenService } from '../services/examen.service';

@Component({
  selector: 'app-examen',
  templateUrl: './examen.component.html',
  styleUrls: ['./examen.component.css']
})
export class ExamenComponent {

  examenForm!: FormGroup;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  questionPdfUrl: string | null = null;
  answerPdfUrl: string | null = null;
  currentExam: string | null = null;
  examGenerated = false;


  constructor(private fb: FormBuilder,
    private examenService: ExamenService
  ) {}

  ngOnInit(): void {
    this.examenForm = this.fb.group({
      topic: ['', Validators.required],
      numMCQ: [0, [Validators.required, Validators.min(1)]],
      numTrueFalse: [0, [Validators.required, Validators.min(0)]],
      numShortAnswer: [0, [Validators.required, Validators.min(0)]],
      difficulty: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.examenForm.valid) {
      const examen: Examen = this.examenForm.value;
      this.loading = true;
      // Call the service
      this.examenService.generateExamen(examen).subscribe({
        next: (response) => {
          console.log('✅ Exam generated:', response);
          this.successMessage = 'Examen généré avec succès 🎉';
          this.errorMessage = null;
          this.questionPdfUrl = response.questionPdf;
          this.answerPdfUrl = response.answerPdf;
          this.currentExam = response.examJson;
          alert('Examen généré avec succès 🎉');
          this.examGenerated = true;
          this.loading = false;
        },
        error: (err) => {
          console.error('❌ Error generating exam:', err);
          this.errorMessage = "Erreur lors de la génération de l'examen.";
          this.successMessage = null;
        }
      });
    } else {
      this.errorMessage = 'Veuillez remplir tous les champs requis.';
      this.successMessage = null;
    }
  }


  aiPrompt: string = '';
loading = false;

regenerateExam(): void {
  // Validate prompt
  if (!this.aiPrompt || !this.aiPrompt.trim()) {
    this.errorMessage = "Veuillez entrer une instruction pour l'IA.";
    this.successMessage = null;
    return;
  }

  // Ensure there is a current exam to modify
  if (!this.currentExam) {
    this.errorMessage = "Aucun examen existant à modifier. Veuillez générer un examen d'abord.";
    this.successMessage = null;
    return;
  }

  this.loading = true;
  this.errorMessage = null;
  this.successMessage = null;

  // Prepare request payload
  const requestBody = {
    currentExam: this.currentExam,
    instruction: this.aiPrompt.trim(),
    topic: this.examenForm.value.topic || "modified_exam" // optional, for PDF naming
  };

  // Call the service
  this.examenService.modifyExam(requestBody).subscribe({
    next: (response) => {
      console.log('✅ Exam modified/generated:', response);

      // Update PDFs
      this.questionPdfUrl = response.questionPdf || null;
      this.answerPdfUrl = response.answerPdf || null;

      // Update the current exam JSON for further modifications
      this.currentExam = response.questions || this.currentExam;

      this.successMessage = 'Examen modifié avec succès 🎉';
      alert('Examen modifié avec succès 🎉');
      this.loading = false;
      this.aiPrompt = '';
    },
    error: (err) => {
      console.error('❌ Error regenerating exam:', err);
      this.errorMessage = "Erreur lors de la régénération de l'examen.";
      this.loading = false;
    }
  });
}


  prompt = '';
  generatedImageUrl: string | null = null;
  generate() {
    this.loading = true;
    this.examenService.generateImage(this.prompt).subscribe({
      next: (res) => {
        this.generatedImageUrl = res.data[0].url;
        this.loading = false;
        alert('✅ Image générée avec succès !');
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        alert('❌ Erreur lors de la génération.');
      },
    });
  }

}
