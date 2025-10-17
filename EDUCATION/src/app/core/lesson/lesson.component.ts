import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LessonService, Lesson } from 'src/services/lesson.service';
import { CourseService, Course } from 'src/services/course.service';

@Component({
  selector: 'app-lesson',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './lesson.component.html',
  styleUrls: ['./lesson.component.css']
})
export class LessonComponent implements OnInit {

  courseId!: number;
  course: Course | null = null;
  lessons: Lesson[] = [];
  loading = false;

  search = '';
  modalOpen = false;
  confirmOpen = false;
  toDeleteId: number | null = null;
  toast: { type: 'success' | 'error', text: string } | null = null;

  form!: FormGroup;

  // 🔹 IA Résumé
  aiSummary: string | null = null;
  loadingSummary = false;
  summaryVisible = false; // affichage temporaire

  // 🔹 Lire plus / Lire moins
  expanded: Record<number, boolean> = {}; // map id -> état

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private lessonSvc: LessonService,
    private courseSvc: CourseService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      id: [null],
      title: ['', [Validators.required]],
      content: ['', [Validators.required]]
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    this.courseId = Number(idParam);
    this.loadCourse();
    this.fetch();
  }

  onSearch(ev: Event): void {
    const input = ev.target as HTMLInputElement | null;
    this.search = input?.value ?? '';
  }

  loadCourse(): void {
    this.courseSvc.getCourseById(this.courseId).subscribe({
      next: c => this.course = c,
      error: _ => this.error('Échec du chargement du cours')
    });
  }

  fetch(): void {
    this.loading = true;
    this.lessonSvc.getLessonsByCourse(this.courseId).subscribe({
      next: list => { this.lessons = list; this.loading = false; },
      error: _ => { this.loading = false; this.error('Échec du chargement des leçons'); }
    });
  }

  // 🔹 Génération du résumé IA (auto hide après 10s)
  generateSummary(lessonId: number): void {
    this.loadingSummary = true;
    this.aiSummary = null;
    this.summaryVisible = true;

    this.lessonSvc.generateSummary(lessonId).subscribe({
      next: (res: any) => {
        this.aiSummary = res.ai_summary;
        this.loadingSummary = false;
        this.success('Résumé IA généré avec succès');
        setTimeout(() => this.summaryVisible = false, 10000); // auto-disparition
      },
      error: _ => {
        this.loadingSummary = false;
        this.error('Erreur lors de la génération du résumé IA');
      }
    });
  }

  // 🔹 Lire plus / Lire moins
  toggleExpand(l: Lesson): void {
    if (!l.id) return;
    this.expanded[l.id] = !this.expanded[l.id];
  }

  // CRUD / UI existants
  openCreate(): void {
    this.form.reset({ id: null, title: '', content: '' });
    this.modalOpen = true;
  }

  openEdit(l: Lesson): void {
    this.form.reset({ id: l.id ?? null, title: l.title, content: l.content });
    this.modalOpen = true;
  }

  closeModal(): void { this.modalOpen = false; }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.value as Lesson;
    const payload: Lesson = {
      id: value.id ?? undefined,
      title: value.title ?? '',
      content: value.content ?? ''
    };

    if (payload.id) {
      this.lessonSvc.updateLesson(payload as any).subscribe({
        next: _ => { this.success('Leçon mise à jour'); this.closeModal(); this.fetch(); },
        error: _ => this.error('Échec de la mise à jour')
      });
    } else {
      this.lessonSvc.addLesson(this.courseId, payload).subscribe({
        next: _ => { this.success('Leçon créée'); this.closeModal(); this.fetch(); },
        error: _ => this.error('Échec de la création')
      });
    }
  }

  askDelete(l: Lesson): void { this.toDeleteId = l.id!; this.confirmOpen = true; }
  cancelDelete(): void { this.toDeleteId = null; this.confirmOpen = false; }

  confirmDelete(): void {
    if (!this.toDeleteId) return;
    this.lessonSvc.deleteLesson(this.toDeleteId).subscribe({
      next: _ => { this.success('Leçon supprimée'); this.cancelDelete(); this.fetch(); },
      error: _ => this.error('Échec de la suppression')
    });
  }

  downloadLesson(l: Lesson): void {
    const url = (l as any).attachmentUrl as string | undefined;
    if (url) window.open(url, '_blank');
    else this.error('Aucun fichier attaché à cette leçon');
  }

  filtered(): Lesson[] {
    const q = this.search.trim().toLowerCase();
    if (!q) return this.lessons;
    return this.lessons.filter(l =>
      (l.title ?? '').toLowerCase().includes(q) ||
      (l.content ?? '').toLowerCase().includes(q)
    );
  }

  trackById(_i: number, item: Lesson) { return item.id ?? _i; }

  success(text: string) { this.toast = { type: 'success', text }; setTimeout(() => this.toast = null, 1800); }
  error(text: string) { this.toast = { type: 'error', text }; setTimeout(() => this.toast = null, 2500); }
}
