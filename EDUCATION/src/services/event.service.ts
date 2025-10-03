import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, User } from '../app/core/event/event.component';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = 'http://localhost:8083/event';

  constructor(private http: HttpClient) {}

  getEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(`${this.apiUrl}/retrieve-all-events`);
  }

  getEventById(id: number): Observable<Event> {
    return this.http.get<Event>(`${this.apiUrl}/retrieve-event/${id}`);
  }

  joinEvent(eventId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/register/${eventId}`, {});
  }
}
