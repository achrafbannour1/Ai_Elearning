import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
// ✅ Chemin vers ton service (corrige-le si besoin selon ton arborescence)
import { Course, CourseService } from 'src/services/course.service';

@Component({
  standalone: true,
  selector: 'app-student-courses',
  imports: [CommonModule, RouterModule],
  templateUrl: './student-courses.component.html',
  styleUrls: ['./student-courses.component.css']
})
export class StudentCoursesComponent {
  courses: Course[] = [];
  loading = true;
  error: string | null = null;

  // Images dans assets (ajoute/retire selon tes fichiers disponibles)
  private readonly imagePool: string[] = [
    'assets/courses/course-1.png',
    'assets/courses/course-2.png',
    'assets/courses/course-3.png',
    
  ];
  defaultCover = 'assets/courses/default.jpg';
  private imageCache = new Map<number | string, string>();

  constructor(private courseSrv: CourseService) {}

  ngOnInit(): void {
    this.courseSrv.getCourses().subscribe({
      next: (data: Course[]) => {
        this.courses = data ?? [];
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error(err);
        this.error = 'Impossible de charger les cours.';
        this.loading = false;
      }
    });
  }

  trackById = (_: number, c: Course) => c.id ?? _;

  // --- Helpers image: choix "aléatoire stable" par id/titre ---
  private hash(input: string): number {
    // FNV-1a (32-bit) simple
    let h = 0x811c9dc5;
    for (let i = 0; i < input.length; i++) {
      h ^= input.charCodeAt(i);
      h = Math.imul(h, 0x01000193);
    }
    return Math.abs(h);
  }

  getCourseImage(c: Course): string {
    const key = (c.id != null ? `id:${c.id}` : `t:${c.title || ''}`);
    const cached = this.imageCache.get(key);
    if (cached) return cached;

    const seed = this.hash(key);
    const idx = this.imagePool.length ? seed % this.imagePool.length : 0;
    const picked = this.imagePool[idx] || this.defaultCover;

    this.imageCache.set(key, picked);
    return picked;
  }

  onImgError(ev: Event): void {
    (ev.target as HTMLImageElement).src = this.defaultCover;
  }
}
