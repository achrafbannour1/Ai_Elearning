import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AiChatBubbleComponent } from './ai-chat-bubble.component';

describe('AiChatBubbleComponent', () => {
  let component: AiChatBubbleComponent;
  let fixture: ComponentFixture<AiChatBubbleComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AiChatBubbleComponent]
    });
    fixture = TestBed.createComponent(AiChatBubbleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
