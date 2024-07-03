import { TestBed } from '@angular/core/testing';

import { WorkApplicationsService } from './work-applications.service';

describe('WorkApplicationsService', () => {
  let service: WorkApplicationsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WorkApplicationsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
