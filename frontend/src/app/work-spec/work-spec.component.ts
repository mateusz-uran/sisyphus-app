import { Component, input, signal } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { WorkSpecificationDTO } from '../interfaces/work-application';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-work-spec',
  standalone: true,
  imports: [CommonModule, MatExpansionModule],
  template: `
    <section
      *ngIf="specifications() && !isSpecificationEmpty(specifications()!)"
    >
      <h5>{{ specifications()!.companyName }}</h5>
      <mat-expansion-panel
        hideToggle
        (opened)="panelOpenState.set(true)"
        (closed)="panelOpenState.set(false)"
      >
        <mat-expansion-panel-header>
          <mat-panel-description>
            <ul
              class="tech"
              *ngFor="let tech of specifications()!.technologies"
            >
              <li>{{ tech }}</li>
            </ul></mat-panel-description
          >
        </mat-expansion-panel-header>

        <div>
          <h4>Wymagania</h4>
          <ul class="req" *ngFor="let req of specifications()!.requirements">
            <li>{{ req }}</li>
          </ul>
        </div>
      </mat-expansion-panel>
    </section>
    <section
      *ngIf="!specifications() || isSpecificationEmpty(specifications()!)"
    >
      <p class="empty">Brak danych o tej robocie</p>
    </section>
  `,
  styleUrl: './work-spec.component.scss',
})
export class WorkSpecComponent {
  readonly panelOpenState = signal(false);
  specifications = input<WorkSpecificationDTO>();

  isSpecificationEmpty(
    specification: WorkSpecificationDTO | undefined
  ): boolean {
    if (!specification) {
      return true;
    }

    return (
      !specification.companyName &&
      (!specification.requirements ||
        specification.requirements.length === 0) &&
      (!specification.technologies || specification.technologies.length === 0)
    );
  }
}
