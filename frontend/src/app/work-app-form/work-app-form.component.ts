import { CommonModule } from '@angular/common';
import { Component, inject, output, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormControl,
  ValidatorFn,
  AbstractControl,
} from '@angular/forms';
import { catchError, map, of, switchMap } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { WorkApplicationsService } from '../services/work-applications.service';
import {
  WorkApplication,
  WorkApplicationDTO,
  WorkSpecificationDTO,
} from '../interfaces/work-application';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { LoadingApplicationService } from '../services/loading-application.service';

@Component({
  selector: 'app-work-app-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
  ],

  template: `
    <div class="form-wrapper">
      <form [formGroup]="formGroup" (ngSubmit)="onSubmit()" class="urls-form">
        <mat-form-field class="field-wrapper">
          <mat-label>Linki</mat-label>
          <mat-chip-grid
            #chipGrid
            aria-label="Wpisz linki"
            [formControl]="formControl"
          >
            @for (url of workUrls(); track url) {
            <mat-chip-row (removed)="removeSingleUrl(url)" class="url">
              {{ url }}
              <button matChipRemove aria-label="'remove ' + keyword">
                <mat-icon>cancel</mat-icon>
              </button>
            </mat-chip-row>
            }
          </mat-chip-grid>
          <input
            placeholder="Nowy link..."
            [matChipInputFor]="chipGrid"
            (matChipInputTokenEnd)="addSingleUrl($event)"
          />
        </mat-form-field>

        <div *ngIf="errorMessage() !== null" class="error-message">
          {{ errorMessage() }}
        </div>

        <div class="btn-wrapper">
          <button
            type="submit"
            mat-raised-button
            [disabled]="formGroup.invalid"
          >
            Zapisz
          </button>
        </div>
      </form>
    </div>
  `,

  styleUrl: './work-app-form.component.scss',
})
export class WorkAppFormComponent {
  updateWorkAppList = output<WorkApplication[]>();

  workApplicationService: WorkApplicationsService = inject(
    WorkApplicationsService
  );

  workUrls = signal<string[]>([]);
  errorMessage = signal<string | null>(null);
  formControl = new FormControl([], [this.nonEmptyArrayValidator()]);

  formGroup: FormGroup;

  constructor(
    private fb: FormBuilder,
    private activatedRoute: ActivatedRoute,
    private loadingService: LoadingApplicationService
  ) {
    this.formGroup = this.fb.group({
      workUrls: this.formControl,
    });
  }

  private isSpecificationEmpty(specification: WorkSpecificationDTO): boolean {
    return (
      !specification.companyName &&
      (!specification.requirements ||
        specification.requirements.length === 0) &&
      (!specification.technologies || specification.technologies.length === 0)
    );
  }

  removeSingleUrl(url: string) {
    this.workUrls.update((workUrls) => {
      const index = workUrls.indexOf(url);
      if (index < 0) {
        return workUrls;
      }

      workUrls.splice(index, 1);
      return [...workUrls];
    });
  }

  addSingleUrl(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    const currentUrls = this.workUrls();

    if (value && !currentUrls.includes(value)) {
      this.workUrls.set([...currentUrls, value]);
      this.errorMessage.set(null);
    } else {
      this.errorMessage.set('Ten link już został dodany');
    }

    event.chipInput.clear();
  }

  onSubmit() {
    const dto: WorkApplicationDTO[] = this.formGroup.value.workUrls.map(
      (url: string) => ({
        workUrl: url,
      })
    );

    this.activatedRoute.params
      .pipe(
        map((params) => params['id']),
        switchMap((workGroupId: string) =>
          this.workApplicationService
            .saveNewWorkApplication(dto, workGroupId)
            .pipe(
              switchMap(() =>
                this.workApplicationService.getWorkApplications(workGroupId)
              )
            )
        )
      )
      .subscribe((appList: WorkApplication[]) => {
        const updatedAppList = appList.map((app) => {
          if (
            !app.specification ||
            this.isSpecificationEmpty(app.specification)
          ) {
            this.loadingService.setLoading(true, app.id);
            this.workApplicationService
              .scrapWorkApplicationSpecification(app.workUrl, app.id)
              .subscribe({
                next: (response) => {
                  if (response) {
                    app.specification = response;
                    this.updateWorkAppList.emit(appList);
                  }
                },
                error: (error) => {
                  console.log('Scraper error: ', error);
                  this.loadingService.setLoading(false, app.id);
                },
                complete: () => {
                  this.loadingService.setLoading(false, app.id);
                },
              });
          }
          return app;
        });
        this.updateWorkAppList.emit(updatedAppList);
        this.workUrls.set([]);
      });
  }

  nonEmptyArrayValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const isEmpty = control.value.length === 0;
      return isEmpty ? { emptyArray: { value: control.value } } : null;
    };
  }
}
