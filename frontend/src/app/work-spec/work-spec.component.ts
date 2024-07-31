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
      <h5>Nazwa firmy: {{ specifications()!.companyName }}</h5>
      <div
        class="technologies"
        *ngIf="
          specifications()!.technologies &&
            specifications()!.technologies.length > 0;
          else noTechnologies
        "
      >
        <p *ngFor="let tech of specifications()!.technologies">{{ tech }}</p>
      </div>
      <ng-template #noTechnologies>
        <p>-</p>
      </ng-template>
      <mat-expansion-panel
        (opened)="panelOpenState.set(true)"
        (closed)="panelOpenState.set(false)"
      >
        <mat-expansion-panel-header>
          <mat-panel-description>Wymagania: &#8628; </mat-panel-description>
        </mat-expansion-panel-header>
        <div
          *ngIf="
            specifications()!.requirements &&
              specifications()!.requirements.length > 0;
            else noRequirements
          "
        >
          <ul class="req" *ngFor="let req of specifications()!.requirements">
            <li>{{ req }}</li>
          </ul>
        </div>
        <ng-template #noRequirements>
          <p>Nie było wymagań.</p>
        </ng-template>
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
