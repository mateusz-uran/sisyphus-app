import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupSpecComponent } from './group-spec.component';

describe('GroupSpecComponent', () => {
  let component: GroupSpecComponent;
  let fixture: ComponentFixture<GroupSpecComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupSpecComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupSpecComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
