import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Lesson, LessonService } from 'src/services/lesson.service';
import { Course, CourseService } from 'src/services/course.service';

@Component({
  standalone: true,
  selector: 'app-student-lessons',
  imports: [CommonModule, RouterModule],
  templateUrl: './student-lessons.component.html',
  styleUrls: ['./student-lessons.component.css']
})
export class StudentLessonsComponent {
  courseId!: number;
  course: Course | null = null;

  lessons: Lesson[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private lessonSrv: LessonService,
    private courseSrv: CourseService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.courseId = idParam ? Number(idParam) : NaN;

    if (Number.isNaN(this.courseId)) {
      this.error = 'Identifiant de cours invalide.';
      this.loading = false;
      return;
    }

    this.courseSrv.getCourseById(this.courseId).subscribe({
      next: (c) => (this.course = c),
      error: () => {}
    });

    this.lessonSrv.getLessonsByCourse(this.courseId).subscribe({
      next: (data) => {
        this.lessons = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Impossible de charger les leçons.';
        this.loading = false;
      }
    });
  }

  trackById = (_: number, l: Lesson) => l.id ?? _;
}
