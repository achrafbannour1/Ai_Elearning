import { Component, OnInit } from '@angular/core';
import { EventService } from '../../../services/event.service';

// Define the User interface for the nested users array
export interface User {
  id: number;
  username: string | null;
  email: string;
  password: string; // Optional, might want to exclude this from UI
  role: string;
}

// Update the Event interface to include users
export interface Event {
  id: number;
  title: string;
  date: string;
  seatsLeft: number;
  isFull: boolean;
  image: string;
  description: string;
  users: User[]; // Added users array
}

@Component({
  selector: 'app-event',
  templateUrl: './event.component.html',
  styleUrls: ['./event.component.css']
})
export class EventComponent implements OnInit {
  events: Event[] = [];
  selectedEvent: Event | null = null;

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.eventService.getEvents().subscribe((events: Event[]) => {
      this.events = events;
    });
  }

  selectEvent(event: Event): void {
    this.selectedEvent = event;
  }
}
