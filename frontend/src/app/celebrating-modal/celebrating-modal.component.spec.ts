import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CelebratingModalComponent } from './celebrating-modal.component';

describe('CelebratingModalComponent', () => {
  let component: CelebratingModalComponent;
  let fixture: ComponentFixture<CelebratingModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CelebratingModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CelebratingModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
