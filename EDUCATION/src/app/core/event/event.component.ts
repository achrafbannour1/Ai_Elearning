import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';

export interface User {
  id: number;
  username: string | null;
  email: string;
  password: string;
  role: string;
  events?: Event[];
}

export interface Event {
  id: number;
  title: string;
  date: string;
  seatsLeft: number;
  isFull: boolean;
  image: string;
  description: string;
  users?: User[];
  aiPrompt?: string;
}

@Component({
  selector: 'app-event',
  templateUrl: './event.component.html',
  styleUrls: ['./event.component.css']
})
export class EventComponent implements OnInit {
  events: Event[] = [];
  selectedEvent: Event | null = null;
  private apiBaseUrl = 'http://localhost:8083';

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.eventService.getEvents().subscribe((events: Event[]) => {
      this.events = events.map(event => ({
        ...event,
        image: event.image ? (event.image.startsWith('http://') || event.image.startsWith('https://') ? event.image : `${this.apiBaseUrl}${event.image}`) : ''
      }));
    });
  }

  selectEvent(event: Event): void {
    this.selectedEvent = {
      ...event,
      image: event.image ? (event.image.startsWith('http://') || event.image.startsWith('https://') ? event.image : `${this.apiBaseUrl}${event.image}`) : ''
    };
  }
}
