import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AiApiService, SyllabusRequest, SyllabusResponse, ModuleDto, LessonDto } from '../ai-api.service';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-syllabus-generator',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DragDropModule],
  templateUrl: './syllabus-generator.component.html',
  styleUrls: ['./syllabus-generator.component.css']
})
export class SyllabusGeneratorComponent {

  @Input() defaultTitle = 'Nouveau cours';
  @Output() syllabusSelected = new EventEmitter<SyllabusResponse>(); // renvoie le plan au parent (CourseForm)

  loading = signal(false);
  error = signal<string | null>(null);
  preview = signal<SyllabusResponse | null>(null);

  form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    audience: [''],
    level: ['debutant', Validators.required],
    duration: [6, [Validators.required, Validators.min(1), Validators.max(24)]],
  });

  constructor(private fb: FormBuilder, private api: AiApiService) {}

  ngOnInit() {
    this.form.patchValue({ title: this.defaultTitle });
  }

  generate() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const req: SyllabusRequest = this.form.getRawValue() as SyllabusRequest;
    this.loading.set(true);
    this.error.set(null);

    this.api.generateSyllabus(req).subscribe({
      next: (res) => {
        this.preview.set(structuredClone(res));
        this.loading.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('Échec de génération.');
        this.loading.set(false);
      }
    });
  }

  // Drag & drop modules
  dropModule(event: CdkDragDrop<ModuleDto[]>) {
    const p = this.preview();
    if (!p) return;
    moveItemInArray(p.modules, event.previousIndex, event.currentIndex);
    this.preview.set(p);
  }

  // Drag & drop lessons à l’intérieur d’un module
  dropLesson(event: CdkDragDrop<LessonDto[]>, moduleIndex: number) {
    const p = this.preview();
    if (!p) return;
    const lessons = p.modules[moduleIndex].lessons;
    if (event.previousContainer === event.container) {
      moveItemInArray(lessons, event.previousIndex, event.currentIndex);
    } else {
      const prevMIndex = Number((event.previousContainer.id.split('__')[1]));
      const prevLessons = p.modules[prevMIndex].lessons;
      transferArrayItem(prevLessons, lessons, event.previousIndex, event.currentIndex);
    }
    this.preview.set(p);
  }

  addLesson(moduleIndex: number) {
    const p = this.preview();
    if (!p) return;
    p.modules[moduleIndex].lessons.push({ title: 'Nouvelle leçon', outcomes: ['Définir', 'Appliquer', 'Évaluer'] });
    this.preview.set(p);
  }

  removeLesson(moduleIndex: number, lessonIndex: number) {
    const p = this.preview();
    if (!p) return;
    p.modules[moduleIndex].lessons.splice(lessonIndex, 1);
    this.preview.set(p);
  }

  renameModule(moduleIndex: number, event: Event) {
    const p = this.preview();
    if (!p) return;
    const input = event.target as HTMLInputElement;
    p.modules[moduleIndex].title = input.value;
    this.preview.set(p);
  }

  renameLesson(moduleIndex: number, lessonIndex: number, event: Event) {
    const p = this.preview();
    if (!p) return;
    const input = event.target as HTMLInputElement;
    p.modules[moduleIndex].lessons[lessonIndex].title = input.value;
    this.preview.set(p);
  }

  useThisPlan() {
    const p = this.preview();
    if (!p) return;
    // émettre au parent (CourseForm) pour préremplir
    this.syllabusSelected.emit(p);
  }
}
