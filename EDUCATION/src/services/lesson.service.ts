import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Lesson {
  id?: number;
  title: string;
  content: string;
  course?: { id: number } | null;
}

@Injectable({
  providedIn: 'root'
})
export class LessonService {
  // ⚠️ Mets le port de TON backend (8083 d’après tes logs)
  private apiUrl = 'http://localhost:8083/lesson';

  constructor(private http: HttpClient) {}

  // GET /lesson/retrieve-all-lessons
  getLessons(): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.apiUrl}/retrieve-all-lessons`);
  }

  // GET /lesson/retrieve-lesson/{id}
  getLessonById(id: number): Observable<Lesson> {
    return this.http.get<Lesson>(`${this.apiUrl}/retrieve-lesson/${id}`);
  }

  // GET /lesson/by-course/{courseId}
  getLessonsByCourse(courseId: number): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.apiUrl}/by-course/${courseId}`);
  }

  // POST /lesson/add-lesson/{courseId}
  addLesson(courseId: number, lesson: Partial<Lesson>): Observable<Lesson> {
    return this.http.post<Lesson>(`${this.apiUrl}/add-lesson/${courseId}`, lesson);
  }

  // PUT /lesson/modify-lesson
  updateLesson(lesson: Required<Pick<Lesson, 'id'>> & Partial<Lesson>): Observable<Lesson> {
    return this.http.put<Lesson>(`${this.apiUrl}/modify-lesson`, lesson);
  }

  // DELETE /lesson/remove-lesson/{id}
  deleteLesson(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/remove-lesson/${id}`);
  }

  // 🔹 IA Résumé automatique
  generateSummary(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/autosummary/${id}`, {}, { responseType: 'json' });
  }
}
