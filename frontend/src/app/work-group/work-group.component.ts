import { Component, Input, inject } from '@angular/core';
import { TransformedWorkGroup } from '../interfaces/work-group';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { WorkGroupService } from '../services/work-group.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-work-group',
  standalone: true,
  imports: [CommonModule, RouterModule, MatButtonModule, MatIconModule],
  template: `<section>
    <div
      [ngClass]="[
        'content-wrapper',
        'content',
        workGroup.isHired ? 'hired' : ''
      ]"
    >
      <div class="link-wrapper">
        <img src="./vector_cv_resized.png" alt="cv_template" />
        <button mat-flat-button (click)="openPdfInNewTab(workGroup.cvData)">
          Otwórz CV
        </button>
      </div>
      <article>
        <div class="group-content">
          <p>Data utworzenia</p>
          <p>{{ workGroup.creationTime }}</p>
        </div>
        <div class="group-content">
          <p>Aplikacji</p>
          <p>{{ workGroup.applied }}</p>
        </div>
        <div class="group-content">
          <p>Odrzucone</p>
          <p>{{ workGroup.denied }}</p>
        </div>
        <div class="group-content">
          <p>W trakcie</p>
          <p>{{ workGroup.inProgress }}</p>
        </div>
      </article>
      <div class="btn-wrapper">
        <a mat-raised-button [routerLink]="['/group', workGroup.id]"
          >Przeglądaj</a
        >
        <button mat-mini-fab (click)="deleteWorkGroup(workGroup.id)">
          <mat-icon>delete</mat-icon>
        </button>
      </div>
      <div *ngIf="workGroup.isHired" class="celeb">
        <img src="/cup-6614_256.gif" alt="hired gif" />
      </div>
    </div>
  </section>`,
  styleUrl: './work-group.component.scss',
})
export class WorkGroupComponent {
  @Input() workGroup!: TransformedWorkGroup;
  workGroupService: WorkGroupService = inject(WorkGroupService);

  constructor() {}

  ngOnInit(): void {}

  openPdfInNewTab(cvBlob: Blob): void {
    const url = URL.createObjectURL(cvBlob);
    window.open(url, '_blank');
    URL.revokeObjectURL(url);
  }

  deleteWorkGroup(workGroupId: string): void {
    this.workGroupService.deleteWorkGroup(workGroupId).subscribe({
      next: () => {
        console.log('Delete successful');
        // TODO: handle information
      },
      error: (err: any) => {
        console.error('Error deleting work group', err);
        // TODO: handle error information
      },
    });
  }
}
