import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentLessonsComponent } from './student-lessons.component';

describe('StudentLessonsComponent', () => {
  let component: StudentLessonsComponent;
  let fixture: ComponentFixture<StudentLessonsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StudentLessonsComponent]
    });
    fixture = TestBed.createComponent(StudentLessonsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
