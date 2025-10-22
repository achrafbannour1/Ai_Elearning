import { TestBed } from '@angular/core/testing';

import { AiApiService } from './ai-api.service';

describe('AiApiService', () => {
  let service: AiApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AiApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
