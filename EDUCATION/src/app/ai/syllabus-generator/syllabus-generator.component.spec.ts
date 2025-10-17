import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SyllabusGeneratorComponent } from './syllabus-generator.component';

describe('SyllabusGeneratorComponent', () => {
  let component: SyllabusGeneratorComponent;
  let fixture: ComponentFixture<SyllabusGeneratorComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SyllabusGeneratorComponent]
    });
    fixture = TestBed.createComponent(SyllabusGeneratorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
