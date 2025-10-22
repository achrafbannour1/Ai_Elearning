// src/app/services/course.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Course {
  id?: number;
  title: string;
  description: string;

  // Permet au template d'accéder à c.lessons?.length sans erreur TS
  lessons?: Array<{ id?: number; title?: string; content?: string }>;
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private apiUrl = 'http://localhost:8083/course';

  constructor(private http: HttpClient) {}

  // GET /course/retrieve-all-courses
  getCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.apiUrl}/retrieve-all-courses`);
  }

  // GET /course/retrieve-course/{id}
  getCourseById(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.apiUrl}/retrieve-course/${id}`);
  }

  // POST /course/add-course
  addCourse(course: Partial<Course>): Observable<Course> {
    return this.http.post<Course>(`${this.apiUrl}/add-course`, course);
  }

  // PUT /course/modify-course
  updateCourse(course: Required<Pick<Course, 'id'>> & Partial<Course>): Observable<Course> {
    return this.http.put<Course>(`${this.apiUrl}/modify-course`, course);
  }

  // DELETE /course/remove-course/{id}
  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/remove-course/${id}`);
  }
}
