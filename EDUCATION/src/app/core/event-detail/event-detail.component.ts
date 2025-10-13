import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { AuthService } from '../../../services/auth.service';
import { Event, User } from '../event/event.component';

interface SpeechRecognitionEvent {
  results: SpeechRecognitionResultList;
  resultIndex: number;
}

@Component({
  selector: 'app-event-detail',
  templateUrl: './event-detail.component.html',
  styleUrls: ['./event-detail.component.css']
})
export class EventDetailComponent implements OnInit, OnDestroy {
  event: Event | undefined;
  errorMessage: string | null = null;
  isAlreadyJoined: boolean = false;
  isListening: boolean = false;
  recognition: any | null = null;
  synthesis = window.speechSynthesis;
  private apiBaseUrl = 'http://localhost:8083';

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private authService: AuthService,
    private router: Router
  ) {
    if ('webkitSpeechRecognition' in window) {
      this.recognition = new (window as any).webkitSpeechRecognition();
      this.recognition.continuous = false;
      this.recognition.interimResults = false;
      this.recognition.lang = 'en-US';
      this.recognition.onresult = this.onSpeechResult.bind(this);
      this.recognition.onend = this.onSpeechEnd.bind(this);
    }
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadEvent(id);
  }

  ngOnDestroy(): void {
    this.stopListening();
  }

  loadEvent(id: number): void {
    this.eventService.getEventById(id).subscribe((event: Event) => {
      this.event = {
        ...event,
        image: event.image ? (event.image.startsWith('http://') || event.image.startsWith('https://') ? event.image : `${this.apiBaseUrl}${event.image}`) : ''
      };
      this.checkIfJoined();
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
      this.speak('Please log in to join the event.');
      this.router.navigate(['/login']);
      return;
    }

    if (this.event && !this.isAlreadyJoined && this.event.seatsLeft > 0 && !this.event.isFull) {
      this.eventService.joinEvent(this.event.id).subscribe(() => {
        this.speak('You have successfully joined the event!');
        this.loadEvent(this.event!.id);
      });
    } else if (this.isAlreadyJoined) {
      this.speak('You are already registered for this event!');
    } else {
      this.speak('Event is full or no seats available!');
    }
  }

  startVoiceAssistant(): void {
    if (!this.recognition) {
      this.speak('Voice recognition is not supported in this browser.');
      return;
    }

    this.isListening = true;
    this.speak('Hello! How can I help? Say "join event" to register.');
    this.recognition.start();
  }

  stopVoiceAssistant(): void {
    this.stopListening();
    this.speak('Voice assistant stopped.');
  }

  private onSpeechResult(event: SpeechRecognitionEvent): void {
    const transcript = event.results[0][0].transcript.toLowerCase().trim();
    if (transcript.includes('join') || transcript.includes('register') || transcript.includes('sign up')) {
      this.joinEvent();
    } else {
      this.speak('I heard you say: ' + transcript + '. To join the event, say "join event".');
    }
  }

  private onSpeechEnd(): void {
    this.isListening = false;
  }

  private stopListening(): void {
    if (this.recognition) {
      this.recognition.stop();
    }
  }

  private speak(text: string): void {
    if (this.synthesis) {
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.rate = 1.2;
      utterance.pitch = 1.1;
      this.synthesis.speak(utterance);
    }
  }
}
