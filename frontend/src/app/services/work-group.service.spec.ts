import { TestBed } from '@angular/core/testing';

import { WorkGroupService } from './work-group.service';

describe('WorkGroupService', () => {
  let service: WorkGroupService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WorkGroupService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
