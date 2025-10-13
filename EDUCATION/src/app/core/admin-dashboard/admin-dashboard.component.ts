import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminService, Statistics } from '../../../services/admin.service';
import { AuthService } from '../../../services/auth.service';
import { Event, User } from '../event/event.component';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  activeTab: 'dashboard' | 'events' | 'users' = 'dashboard';
  statistics: Statistics | null = null;
  events: Event[] = [];
  selectedEvent: Event | null = null;
  showEventModal = false;
  isEditMode = false;
  users: User[] = [];
  selectedUser: User | null = null;
  eventForm: Partial<Event> = {
    title: '',
    description: '',
    date: '',
    seatsLeft: 0,
    image: '',
    isFull: false,
    aiPrompt: ''
  };
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  generatedImageUrl: string | null = null;
  isGeneratingImage = false;
  private apiBaseUrl = 'http://localhost:8083';

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkAdminAccess();
    this.loadStatistics();
    this.loadEvents();
    this.loadUsers();
  }

  checkAdminAccess(): void {
    const userInfo = this.authService.getUserInfo();
    if (!userInfo || userInfo.role !== 'ROLE_ADMIN') {
      alert('Access denied. Admin only.');
      this.router.navigate(['/']);
    }
  }

  switchTab(tab: 'dashboard' | 'events' | 'users'): void {
    this.activeTab = tab;
    this.clearMessages();
  }

  loadStatistics(): void {
    this.adminService.getStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (err) => {
        console.error('Error loading statistics:', err);
        this.showError('Failed to load statistics');
      }
    });
  }

  loadEvents(): void {
    this.isLoading = true;
    this.adminService.getAllEvents().subscribe({
      next: (events) => {
        this.events = events;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading events:', err);
        this.showError('Failed to load events');
        this.isLoading = false;
      }
    });
  }

  openCreateEventModal(): void {
    this.isEditMode = false;
    this.eventForm = {
      title: '',
      description: '',
      date: '',
      seatsLeft: 0,
      image: '',
      isFull: false,
      aiPrompt: ''
    };
    this.generatedImageUrl = null;
    this.showEventModal = true;
  }

  openEditEventModal(event: Event): void {
    this.isEditMode = true;
    this.selectedEvent = event;
    this.eventForm = {
      title: event.title,
      description: event.description,
      date: event.date,
      seatsLeft: event.seatsLeft,
      image: event.image,
      isFull: event.isFull,
      aiPrompt: ''
    };
    this.generatedImageUrl = event.image ? `${this.apiBaseUrl}${event.image}` : null;
    this.showEventModal = true;
  }

  closeEventModal(): void {
    this.showEventModal = false;
    this.selectedEvent = null;
    this.eventForm = {};
    this.generatedImageUrl = null;
  }
  saveEvent(): void {
    if (!this.validateEventForm()) {
      this.showError('Please fill all required fields');
      return;
    }

    // ✅ CRITICAL FIX: Explicitly construct the event object
    const eventData: any = {
      title: this.eventForm.title,
      description: this.eventForm.description,
      date: this.eventForm.date,
      seatsLeft: this.eventForm.seatsLeft,
      isFull: this.eventForm.isFull || false,
      image: this.eventForm.image || '' // ✅ Explicitly include image!
    };

    console.log('💾 Saving event with data:', eventData); // Debug log

    this.isLoading = true;

    if (this.isEditMode && this.selectedEvent) {
      // For edit, add ID
      eventData.id = this.selectedEvent.id;

      this.adminService.updateEvent(this.selectedEvent.id, eventData).subscribe({
        next: (response) => {
          console.log('✅ Event updated:', response);
          this.showSuccess('Event updated successfully');
          this.closeEventModal();
          this.loadEvents();
          this.loadStatistics();
        },
        error: (err) => {
          console.error('❌ Error updating event:', err);
          this.showError('Failed to update event');
          this.isLoading = false;
        }
      });
    } else {
      this.adminService.createEvent(eventData).subscribe({
        next: (response) => {
          console.log('✅ Event created:', response);
          this.showSuccess('Event created successfully');
          this.closeEventModal();
          this.loadEvents();
          this.loadStatistics();
        },
        error: (err) => {
          console.error('❌ Error creating event:', err);
          this.showError('Failed to create event');
          this.isLoading = false;
        }
      });
    }
  }

  generateAiImage(): void {
    if (!this.eventForm.title) {
      this.showError('Enter an event title to generate a relevant image');
      return;
    }

    const prompt = this.eventForm.aiPrompt ||
      `Create an engaging promotional image for an e-learning event titled "${this.eventForm.title}". Style: vibrant, educational, modern.`;

    console.log('🎨 Generating image with prompt:', prompt);

    this.isGeneratingImage = true;
    this.showInfo('Generating image... This may take up to 60 seconds.');

    this.adminService.generateImage(prompt).subscribe({
      next: (response) => {
        console.log('✅ Image generated:', response);

        // Set BOTH the preview URL and the form image
        this.generatedImageUrl = `${this.apiBaseUrl}${response.imageUrl}`;
        this.eventForm.image = response.imageUrl; // This is the DB value

        console.log('📝 Set eventForm.image to:', this.eventForm.image);
        console.log('📝 Current eventForm:', JSON.stringify(this.eventForm, null, 2));

        this.showSuccess(`AI image generated! URL: ${response.imageUrl}`);
        this.isGeneratingImage = false;
      },
      error: (err) => {
        console.error('❌ Error generating image:', err);
        let message = 'Failed to generate image.';
        if (err.status === 404) {
          message = 'AI model not found.';
        } else if (err.status === 503) {
          message = 'AI model is warming up. Try again in 1-2 minutes.';
        } else if (err.status === 429) {
          message = 'Rate limit reached. Wait 1 minute and retry.';
        }
        this.showError(message);
        this.isGeneratingImage = false;
      }
    });
  }

  deleteEvent(event: Event): void {
    if (!confirm(`Are you sure you want to delete "${event.title}"?`)) {
      return;
    }
    this.adminService.deleteEvent(event.id).subscribe({
      next: () => {
        this.showSuccess('Event deleted successfully');
        this.loadEvents();
        this.loadStatistics();
      },
      error: (err) => {
        console.error('Error deleting event:', err);
        this.showError('Failed to delete event');
      }
    });
  }

  validateEventForm(): boolean {
    return !!(
      this.eventForm.title &&
      this.eventForm.description &&
      this.eventForm.date &&
      this.eventForm.seatsLeft !== undefined &&
      this.eventForm.seatsLeft >= 0
    );
  }

  loadUsers(): void {
    this.isLoading = true;
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.showError('Failed to load users');
        this.isLoading = false;
      }
    });
  }

  viewUserDetails(user: User): void {
    this.selectedUser = user;
  }

  closeUserDetails(): void {
    this.selectedUser = null;
  }

  changeUserRole(user: User, newRole: string): void {
    if (!confirm(`Change ${user.email}'s role to ${newRole}?`)) {
      return;
    }
    this.adminService.updateUserRole(user.id, newRole).subscribe({
      next: () => {
        this.showSuccess('User role updated successfully');
        this.loadUsers();
        this.loadStatistics();
      },
      error: (err) => {
        console.error('Error updating user role:', err);
        this.showError('Failed to update user role');
      }
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Are you sure you want to delete user "${user.email}"?`)) {
      return;
    }
    this.adminService.deleteUser(user.id).subscribe({
      next: () => {
        this.showSuccess('User deleted successfully');
        this.loadUsers();
        this.loadStatistics();
        if (this.selectedUser?.id === user.id) {
          this.closeUserDetails();
        }
      },
      error: (err) => {
        console.error('Error deleting user:', err);
        this.showError('Failed to delete user');
      }
    });
  }



  showInfo(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => {
      this.successMessage = '';
    }, 5000);
  }

  showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => {
      this.successMessage = '';
    }, 3000);
  }

  showError(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    setTimeout(() => {
      this.errorMessage = '';
    }, 5000);
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
