import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AiAssistantService {
  private apiUrl = 'http://localhost:8083/api/ollama';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  query(userQuery: string): Observable<any> {
    const token = this.authService.getToken();
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    const body = { query: userQuery };

    return this.http.post(`${this.apiUrl}/query`, body, { headers });
  }
}
