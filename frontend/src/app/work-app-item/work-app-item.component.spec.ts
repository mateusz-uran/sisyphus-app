import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkAppItemComponent } from './work-app-item.component';

describe('WorkAppItemComponent', () => {
  let component: WorkAppItemComponent;
  let fixture: ComponentFixture<WorkAppItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkAppItemComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WorkAppItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
