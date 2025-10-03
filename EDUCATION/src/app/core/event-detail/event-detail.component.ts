import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { AuthService } from '../../../services/auth.service';
import { Event, User } from '../event/event.component';

@Component({
  selector: 'app-event-detail',
  templateUrl: './event-detail.component.html',
  styleUrls: ['./event-detail.component.css']
})
export class EventDetailComponent implements OnInit {
  event: Event | undefined;
  errorMessage: string | null = null;
  isAlreadyJoined: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    console.log('Loading event with ID:', id); // Debug log
    this.loadEvent(id);
  }

  loadEvent(id: number): void {
    this.eventService.getEventById(id).subscribe({
      next: (event: Event) => {
        this.event = event;
        this.checkIfJoined();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load event. Check console for details.';
        console.error('Load event error:', err);
      }
    });
  }

  checkIfJoined(): void {
    const userInfo = this.authService.getUserInfo();
    if (userInfo && this.event && this.event.users) {
      this.isAlreadyJoined = this.event.users.some((u: User) => u.id === userInfo.id);
    }
  }

  joinEvent(): void {
    if (!this.authService.isLoggedIn()) {
      alert('Please log in to join the event.');
      this.router.navigate(['/login']);
      return;
    }

    if (this.event && !this.isAlreadyJoined && this.event.seatsLeft > 0 && !this.event.isFull) {
      this.eventService.joinEvent(this.event.id).subscribe({
        next: () => {
          alert('You have joined the event!');
          this.loadEvent(this.event!.id);
        },
        error: (err) => {
          if (err.status === 403) {
            this.errorMessage = 'Access denied. Please ensure you are logged in with a valid account.';
          } else if (err.status === 404) {
            this.errorMessage = err.error?.message || 'Event not found. Please try another event.';
          } else if (err.status === 400) {
            this.errorMessage = err.error?.message || 'Cannot join event. It may be full or you are already registered.';
          } else {
            this.errorMessage = err.error?.message || 'Failed to join event. Check console for details.';
          }
          console.error('Join event error:', err);
        }
      });
    } else if (this.isAlreadyJoined) {
      alert('You are already registered for this event!');
    } else {
      alert('Event is full or no seats available!');
    }
  }
}
