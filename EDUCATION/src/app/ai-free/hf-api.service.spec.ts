import { TestBed } from '@angular/core/testing';

import { HfApiService } from './hf-api.service';

describe('HfApiService', () => {
  let service: HfApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HfApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
