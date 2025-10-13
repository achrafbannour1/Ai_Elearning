// src/app/services/admin.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { Event, User } from '../app/core/event/event.component';

export interface Statistics {
  totalEvents: number;
  availableEvents: number;
  fullEvents: number;
  totalUsers: number;
  adminUsers: number;
  studentUsers: number;
  totalRegistrations: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8083/api/admin';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getAllEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/events`, { headers: this.getHeaders() });
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/events/${id}`, { headers: this.getHeaders() });
  }

  createEvent(event: Event): Observable<Event> {
    return this.http.post<Event>(`${this.apiUrl}/events`, event, { headers: this.getHeaders() });
  }

  updateEvent(id: number, event: Event): Observable<Event> {
    return this.http.put<Event>(`${this.apiUrl}/events/${id}`, event, { headers: this.getHeaders() });
  }

  deleteEvent(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/events/${id}`, { headers: this.getHeaders() });
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`, { headers: this.getHeaders() });
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${id}`, { headers: this.getHeaders() });
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/${id}`, { headers: this.getHeaders() });
  }

  updateUserRole(id: number, role: string): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/${id}/role`, { role }, { headers: this.getHeaders() });
  }

  getStatistics(): Observable<Statistics> {
    return this.http.get<Statistics>(`${this.apiUrl}/statistics`, { headers: this.getHeaders() });
  }

  getEventRegistrations(eventId: number): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/events/${eventId}/registrations`, { headers: this.getHeaders() });
  }

  removeUserFromEvent(eventId: number, userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/events/${eventId}/registrations/${userId}`, { headers: this.getHeaders() });
  }

  generateImage(prompt: string): Observable<{ imageUrl: string; message: string }> {
    return this.http.post<{ imageUrl: string; message: string }>(`${this.apiUrl}/generate-image`, { prompt }, { headers: this.getHeaders() });
  }
}
