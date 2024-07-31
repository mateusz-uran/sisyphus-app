import { TestBed } from '@angular/core/testing';

import { LoadingApplicationService } from './loading-application.service';

describe('LoadingApplicationService', () => {
  let service: LoadingApplicationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoadingApplicationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
