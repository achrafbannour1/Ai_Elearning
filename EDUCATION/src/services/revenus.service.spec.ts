import { TestBed } from '@angular/core/testing';

import { RevenusService } from './revenus.service';

describe('RevenusService', () => {
  let service: RevenusService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RevenusService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
