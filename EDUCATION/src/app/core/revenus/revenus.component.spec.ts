import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RevenusComponent } from './revenus.component';

describe('RevenusComponent', () => {
  let component: RevenusComponent;
  let fixture: ComponentFixture<RevenusComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RevenusComponent]
    });
    fixture = TestBed.createComponent(RevenusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
