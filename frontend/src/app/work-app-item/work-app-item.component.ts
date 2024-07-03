import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WorkApplication } from '../interfaces/work-application';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-work-app-item',
  standalone: true,
  imports: [
    CommonModule,
    MatChipsModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  template: `
    <section *ngIf="app() as app">
      <div class="app content">
        <div class="link-wrapper">
          <p
            class="h-font-small"
            [matTooltip]="app.workUrl"
            matTooltipPosition="above"
          >
            {{ app.workUrl }}
          </p>
          <a mat-stroked-button [href]="app.workUrl">Otwórz</a>
        </div>
        <div class="application-date">
          <p class="h-font-small">Data aplikowania</p>
          <p>{{ app.appliedDate }}</p>
        </div>
        <div class="status-wrapper">
          <p class="h-font-small">Status</p>
          <mat-chip-listbox
            [ngClass]="
              isScreenSmall() ? 'mat-mdc-chip-set-stacked' : 'mat-mdc-chip-set'
            "
          >
            <mat-chip-option
              *ngFor="let status of workStatus()"
              [selected]="status === app.status"
              (click)="changeStatusValue(status, app.status, app.id)"
            >
              {{ status }}
            </mat-chip-option>
          </mat-chip-listbox>
        </div>
        <div class="btns-wrapper">
          <button
            mat-flat-button
            [disabled]="selectedAppId !== app.id || !allowSaveButton"
            (click)="updateWorkStatus(app.id, selectedNewStatus)"
          >
            Zapisz
          </button>
          <button mat-mini-fab (click)="deleteWork(app.id)">
            <mat-icon>delete</mat-icon>
          </button>
        </div>
      </div>
    </section>
  `,
  styleUrl: './work-app-item.component.scss',
})
export class WorkAppItemComponent {
  app = input<WorkApplication>();
  workStatus = input<string[]>();
  isScreenSmall = input<boolean>();

  updateStatus = output<{ id: string; status: string }>();
  delete = output<string>();

  selectedAppId: string = '';
  allowSaveButton: boolean = false;
  selectedNewStatus: string = '';

  changeStatusValue(newStatus: string, oldStatus: string, appId: string) {
    if (newStatus !== oldStatus) {
      this.allowSaveButton = true;
      this.selectedNewStatus = newStatus;
    } else {
      this.allowSaveButton = false;
    }
    this.selectedAppId = appId;
  }

  updateWorkStatus(appId: string, status: string) {
    this.updateStatus.emit({ id: appId, status });
  }

  deleteWork(appId: string) {
    this.delete.emit(appId);
  }
}
