import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VoiceCoachComponent } from './voice-coach.component';

describe('VoiceCoachComponent', () => {
  let component: VoiceCoachComponent;
  let fixture: ComponentFixture<VoiceCoachComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [VoiceCoachComponent]
    });
    fixture = TestBed.createComponent(VoiceCoachComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
