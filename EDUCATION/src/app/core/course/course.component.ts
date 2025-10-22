// src/app/core/course/course.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CourseService, Course } from 'src/services/course.service';

// IA Syllabus (standalone)
import { SyllabusResponse } from 'src/app/ai/ai-api.service';
import { SyllabusGeneratorComponent } from 'src/app/ai/syllabus-generator/syllabus-generator.component';

// IA Quiz (standalone) ✅ AJOUT
import { QuizGeneratorComponent } from 'src/app/ai-free/quiz-generator/quiz-generator.component';

@Component({
  selector: 'app-course',
  standalone: true,
  // ⬇️ On ajoute QuizGeneratorComponent ici
  imports: [CommonModule, ReactiveFormsModule, RouterLink, SyllabusGeneratorComponent, QuizGeneratorComponent],
  templateUrl: './course.component.html',
  styleUrls: ['./course.component.css']
})
export class CourseComponent implements OnInit {

  // Data
  courses: Course[] = [];
  loading = false;

  // UI state
  search = '';
  modalOpen = false;
  confirmOpen = false;
  toDeleteId: number | null = null;
  toast: { type: 'success' | 'error', text: string } | null = null;

  // panneau IA dans la modale
  aiOpen = false;

  // Edition
  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private courseSvc: CourseService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      id: [null],
      title: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(2)]],
    });
    this.fetch();
  }

  // Handlers
  onSearch(ev: Event): void {
    const input = ev.target as HTMLInputElement | null;
    this.search = input?.value ?? '';
  }

  // CRUD
  fetch(): void {
    this.loading = true;
    this.courseSvc.getCourses().subscribe({
      next: data => { this.courses = data; this.loading = false; },
      error: _ => { this.loading = false; this.error('Échec du chargement des cours'); }
    });
  }

  openCreate(): void {
    this.form.reset({ id: null, title: '', description: '' });
    this.aiOpen = false;
    this.modalOpen = true;
  }

  openEdit(c: Course): void {
    this.form.reset({ id: c.id ?? null, title: c.title, description: c.description });
    this.aiOpen = false;
    this.modalOpen = true;
  }

  closeModal(): void { this.modalOpen = false; }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const value = this.form.value as Course;
    const payload: Course = {
      id: value.id ?? undefined,
      title: value.title ?? '',
      description: value.description ?? ''
    };

    if (payload.id) {
      this.courseSvc.updateCourse(payload as any).subscribe({
        next: _ => { this.success('Cours mis à jour'); this.closeModal(); this.fetch(); },
        error: _ => this.error('Échec de la mise à jour')
      });
    } else {
      this.courseSvc.addCourse(payload).subscribe({
        next: _ => { this.success('Cours créé'); this.closeModal(); this.fetch(); },
        error: _ => this.error('Échec de la création')
      });
    }
  }

  askDelete(c: Course): void { this.toDeleteId = c.id!; this.confirmOpen = true; }
  cancelDelete(): void { this.toDeleteId = null; this.confirmOpen = false; }
  confirmDelete(): void {
    if (!this.toDeleteId) return;
    this.courseSvc.deleteCourse(this.toDeleteId).subscribe({
      next: _ => { this.success('Cours supprimé'); this.cancelDelete(); this.fetch(); },
      error: _ => this.error('Échec de la suppression')
    });
  }

  downloadCourse(c: Course): void {
    const url = (c as any).attachmentUrl as string | undefined;
    if (url) window.open(url, '_blank');
    else this.error('Aucun fichier attaché à ce cours');
  }

  filtered(): Course[] {
    const q = this.search.trim().toLowerCase();
    if (!q) return this.courses;
    return this.courses.filter(c =>
      (c.title ?? '').toLowerCase().includes(q) ||
      (c.description ?? '').toLowerCase().includes(q)
    );
  }
  trackById(_i: number, item: Course) { return item.id ?? _i; }

  success(text: string) { this.toast = { type: 'success', text }; setTimeout(() => this.toast = null, 1800); }
  error(text: string) { this.toast = { type: 'error', text }; setTimeout(() => this.toast = null, 2500); }

  // ====== IA : appliquer plan syllabus sur le form ======
  applySyllabus(plan: SyllabusResponse) {
    const title = plan.title || this.form.value.title || '';
    const desc = this.buildDescription(plan);

    this.form.patchValue({
      title,
      description: desc
    });

    this.success('Plan IA appliqué au formulaire');
    this.aiOpen = false;
  }

  private buildDescription(plan: SyllabusResponse): string {
    const level = plan.level || '—';
    const duration = plan.duration ?? 0;
    const modules = (plan.modules || []).map(m =>
      (m.title || '').replace(/^Module\s+\d+\s+—\s*/i, '')
    );
    const modulesLine = modules.length ? modules.join(' • ') : '—';

    const lines = [
      `Objectif: ${plan.title} (niveau ${level}) sur ${duration} semaines.`,
      `Modules: ${modulesLine}.`,
      `Chaque module inclut exercices et validation (quiz/mini-TP).`
    ];
    const text = lines.join('\n');
    return text.length > 900 ? text.slice(0, 900) + '…' : text;
  }
}
