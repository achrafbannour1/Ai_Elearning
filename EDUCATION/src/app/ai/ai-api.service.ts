import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SyllabusRequest {
  title: string;
  level: 'debutant' | 'intermediaire' | 'avance' | string;
  duration: number; // semaines
  audience?: string;
}

export interface LessonDto {
  title: string;
  outcomes: string[];
}

export interface ModuleDto {
  title: string;
  objectives: string[];
  lessons: LessonDto[];
  exercises: string[];
}

export interface SyllabusResponse {
  title: string;
  level: string;
  audience?: string;
  duration: number;
  modules: ModuleDto[];
}

@Injectable({ providedIn: 'root' })
export class AiApiService {
  // adapte la base URL (proxy Angular recommandé en dev)
    private base = 'http://localhost:8083/ai/courses';


  constructor(private http: HttpClient) {}

  generateSyllabus(req: SyllabusRequest): Observable<SyllabusResponse> {
    return this.http.post<SyllabusResponse>(`${this.base}/syllabus`, req);
  }
}
