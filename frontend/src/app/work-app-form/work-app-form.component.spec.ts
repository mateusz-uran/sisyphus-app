import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkAppFormComponent } from './work-app-form.component';

describe('WorkAppFormComponent', () => {
  let component: WorkAppFormComponent;
  let fixture: ComponentFixture<WorkAppFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkAppFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WorkAppFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
