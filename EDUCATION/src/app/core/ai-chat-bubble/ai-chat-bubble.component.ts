import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { Router } from '@angular/router';
import { AiAssistantService } from '../../../services/AiAssistantService';

interface Message {
  text: string;
  isUser: boolean;
  timestamp: Date;
}

@Component({
  selector: 'app-ai-chat-bubble',
  templateUrl: './ai-chat-bubble.component.html',
  styleUrls: ['./ai-chat-bubble.component.css']
})
export class AiChatBubbleComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  isOpen = false;
  isListening = false;
  messages: Message[] = [];
  userInput = '';
  isLoading = false;

  recognition: any = null;
  synthesis = window.speechSynthesis;
  private shouldScrollToBottom = false;

  constructor(
    private aiService: AiAssistantService,
    private router: Router
  ) {
    // Initialize speech recognition
    if ('webkitSpeechRecognition' in window) {
      this.recognition = new (window as any).webkitSpeechRecognition();
      this.recognition.continuous = false;
      this.recognition.interimResults = false;
      this.recognition.lang = 'en-US';
      this.recognition.onresult = this.onSpeechResult.bind(this);
      this.recognition.onerror = this.onSpeechError.bind(this);
      this.recognition.onend = this.onSpeechEnd.bind(this);
    }
  }

  ngOnInit(): void {
    // Welcome message
    this.messages.push({
      text: 'Hi! I\'m your AI assistant. Ask me about events, navigation, or anything else!',
      isUser: false,
      timestamp: new Date()
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    this.stopListening();
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.shouldScrollToBottom = true;
    }
  }

  sendMessage(): void {
    if (!this.userInput.trim() || this.isLoading) return;

    const userMessage = this.userInput.trim();
    this.messages.push({
      text: userMessage,
      isUser: true,
      timestamp: new Date()
    });

    this.userInput = '';
    this.isLoading = true;
    this.shouldScrollToBottom = true;

    this.aiService.query(userMessage).subscribe({
      next: (response) => {
        this.handleAiResponse(response);
        this.isLoading = false;
        this.shouldScrollToBottom = true;
      },
      error: (err) => {
        console.error('AI query error:', err);
        this.messages.push({
          text: 'Sorry, I encountered an error. Please try again.',
          isUser: false,
          timestamp: new Date()
        });
        this.isLoading = false;
        this.shouldScrollToBottom = true;
      }
    });
  }

  startVoiceInput(): void {
    if (!this.recognition) {
      alert('Voice recognition is not supported in your browser. Please use Chrome or Edge.');
      return;
    }

    this.isListening = true;
    this.speak('I\'m listening...');
    this.recognition.start();
  }

  stopVoiceInput(): void {
    this.stopListening();
  }

  private onSpeechResult(event: any): void {
    const transcript = event.results[0][0].transcript;
    console.log('User said:', transcript);

    this.userInput = transcript;
    this.sendMessage();
  }

  private onSpeechError(event: any): void {
    console.error('Speech recognition error:', event.error);
    this.isListening = false;

    if (event.error === 'no-speech') {
      this.speak('I didn\'t hear anything. Please try again.');
    } else {
      this.speak('Sorry, I didn\'t catch that.');
    }
  }

  private onSpeechEnd(): void {
    this.isListening = false;
  }

  private stopListening(): void {
    if (this.recognition) {
      this.recognition.stop();
    }
    this.isListening = false;
  }

  private speak(text: string): void {
    if (this.synthesis) {
      // Cancel any ongoing speech
      this.synthesis.cancel();

      const utterance = new SpeechSynthesisUtterance(text);
      utterance.rate = 1.0;
      utterance.pitch = 1.0;
      utterance.volume = 1.0;
      this.synthesis.speak(utterance);
    }
  }

  private handleAiResponse(response: any): void {
    console.log('AI Response:', response);

    // Add AI message
    const aiMessage = response.message || 'I processed your request.';
    this.messages.push({
      text: aiMessage,
      isUser: false,
      timestamp: new Date()
    });

    // Speak the response
    this.speak(aiMessage);

    // Handle specific intents
    switch (response.intent) {
      case 'navigate':
        if (response.page) {
          this.messages.push({
            text: `Taking you to ${response.page} page in 2 seconds...`,
            isUser: false,
            timestamp: new Date()
          });
          setTimeout(() => {
            this.router.navigate([`/${response.page}`]);
            this.isOpen = false;
          }, 2000);
        }
        break;

      case 'join_event':
        if (response.success && response.event_id) {
          this.messages.push({
            text: `Opening event ${response.event_id} details...`,
            isUser: false,
            timestamp: new Date()
          });
          setTimeout(() => {
            this.router.navigate([`/event/${response.event_id}`]);
          }, 2000);
        }
        break;

      case 'list_events':
        if (response.events && response.events.length > 0) {
          // Show events summary
          const eventList = response.events.slice(0, 3).map((e: any) =>
            `• ${e.title} (${e.seatsLeft} seats)`
          ).join('\n');

          this.messages.push({
            text: `Here are some upcoming events:\n${eventList}`,
            isUser: false,
            timestamp: new Date()
          });
        }
        break;
    }
  }

  clearChat(): void {
    this.messages = [{
      text: 'Chat cleared. How can I help you?',
      isUser: false,
      timestamp: new Date()
    }];
    this.shouldScrollToBottom = true;
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesContainer) {
        this.messagesContainer.nativeElement.scrollTop =
          this.messagesContainer.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.error('Scroll error:', err);
    }
  }
}
