import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkSpecComponent } from './work-spec.component';

describe('WorkSpecComponent', () => {
  let component: WorkSpecComponent;
  let fixture: ComponentFixture<WorkSpecComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkSpecComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WorkSpecComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
