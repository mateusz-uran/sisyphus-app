import {
  Component,
  HostListener,
  afterNextRender,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { map, switchMap } from 'rxjs';
import { WorkApplicationsService } from '../services/work-applications.service';
import { WorkApplication } from '../interfaces/work-application';
import { WorkAppItemComponent } from '../work-app-item/work-app-item.component';
import { WorkAppFormComponent } from '../work-app-form/work-app-form.component';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { CelebratingModalComponent } from '../celebrating-modal/celebrating-modal.component';
import { ConfettiService } from '../services/confetti.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { openSnackbar } from '../utilities/snackbarFunction';

@Component({
  selector: 'app-group-spec',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    WorkAppFormComponent,
    WorkAppItemComponent,
    MatChipsModule,
    MatDividerModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    FormsModule,
    MatInputModule,
  ],
  template: ` <app-work-app-form
      (updateWorkAppList)="handleUpdateWorkAppList($event)"
    ></app-work-app-form>
    <div class="search-wrapper">
      <mat-form-field>
        <mat-label>Szukaj po nazwie firmy</mat-label>
        <input
          matInput
          [(ngModel)]="searchValue"
          (ngModelChange)="searchFilter($event)"
        />
      </mat-form-field>
    </div>
    <ng-container *ngFor="let app of filteredWorkApplications">
      <app-work-app-item
        [app]="app"
        [workStatus]="workStatus"
        [isScreenSmall]="isScreenSmall"
        (updateStatus)="updateWorkStatus($event.id, $event.status)"
        (delete)="deleteWork($event)"
      >
      </app-work-app-item>
    </ng-container>`,
  styleUrl: './group-spec.component.scss',
})
export class GroupSpecComponent {
  workApplicationService: WorkApplicationsService = inject(
    WorkApplicationsService
  );
  dialog = inject(MatDialog);
  private activatedRoute = inject(ActivatedRoute);
  private confettiService = inject(ConfettiService);

  workGroupId$ = this.activatedRoute.params.pipe(map((p) => p['workGroupId']));

  workApplications: WorkApplication[] = [];
  filteredWorkApplications: WorkApplication[] = [];

  workStatus: string[] = ['IN_PROGRESS', 'SENT', 'REJECTED', 'HIRED'];
  isScreenSmall: boolean = false;

  searchValue: string = '';

  constructor(private _snackBar: MatSnackBar) {
    afterNextRender(() => {
      this.checkScreenWidth();
    });
  }

  ngOnInit(): void {
    this.activatedRoute.params
      .pipe(
        map((params) => params['id']),
        switchMap((id) => this.workApplicationService.getWorkApplications(id))
      )
      .subscribe((appList: WorkApplication[]) => {
        this.workApplications = appList;
        this.filteredWorkApplications = appList;
      });
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(CelebratingModalComponent);

    dialogRef.afterClosed().subscribe((result) => {
      this.confettiService.stopCelebration();
    });
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: Event | undefined) {
    this.checkScreenWidth();
  }

  checkScreenWidth() {
    this.isScreenSmall = window.innerWidth < 820;
  }

  updateWorkStatus(appId: string, status: string) {
    this.workApplicationService
      .updateWorkApplicationStatus(appId, status)
      .subscribe((newWorkApp: WorkApplication) => {
        const index = this.workApplications.findIndex(
          (app) => app.id === newWorkApp.id
        );

        if (index !== -1) {
          this.workApplications[index] = newWorkApp;
        } else {
          this.workApplications.push(newWorkApp);
        }

        if (newWorkApp.status === 'HIRED') {
          this.openDialog();
          this.confettiService.celebrate();
        }
      });
  }

  deleteWork(appId: string) {
    this.workApplicationService.deleteWorkApplication(appId).subscribe({
      next: () => {
        this.filteredWorkApplications = this.filteredWorkApplications.filter(
          (app) => app.id !== appId
        );
      },
      error: (error) => {
        console.error('Failed to delete application:', error);
        openSnackbar(this._snackBar, 'Błąd usuwania aplikacji!', 'Ok');
      },
    });
  }

  handleUpdateWorkAppList(event: WorkApplication[]): void {
    this.filteredWorkApplications = [
      ...this.filteredWorkApplications,
      ...event.filter(
        (app) =>
          !this.filteredWorkApplications.some(
            (existingApp) => existingApp.id === app.id
          )
      ),
    ];
  }

  searchFilter(event: string) {
    this.filteredWorkApplications = this.workApplications.filter((work) =>
      work.specification.companyName
        ?.toLowerCase()
        .includes(event.toLowerCase())
    );
  }
}
