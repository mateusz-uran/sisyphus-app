import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';

import { MatButtonModule } from '@angular/material/button';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
} from '@angular/material/dialog';

@Component({
  selector: 'app-celebrating-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
  ],
  template: `
    <mat-dialog-content>
      <h3>Gratulacje!</h3>
      <p>Twoja syzyfowa praca dobiegła końca.</p>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button (click)="onNoClick()" mat-button cdkFocusInitial>EZ</button>
    </mat-dialog-actions>
  `,
  styleUrl: './celebrating-modal.component.scss',
})
export class CelebratingModalComponent {
  dialogRef = inject(MatDialogRef<CelebratingModalComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }
}
