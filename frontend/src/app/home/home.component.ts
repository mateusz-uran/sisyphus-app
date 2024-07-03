import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UploadFileComponent } from '../upload-file/upload-file.component';
import { WorkGroupComponent } from '../work-group/work-group.component';
import { TransformedWorkGroup } from '../interfaces/work-group';
import { WorkGroupService } from '../services/work-group.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, UploadFileComponent, WorkGroupComponent],
  template: `
    <ng-container *ngFor="let workGroup of transformedGroupList">
      <app-work-group [workGroup]="workGroup"></app-work-group>
    </ng-container>
  `,
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  transformedGroupList: TransformedWorkGroup[] = [];
  workGroupService: WorkGroupService = inject(WorkGroupService);

  ngOnInit(): void {
    this.workGroupService.workGroups$.subscribe(
      (groupList: TransformedWorkGroup[]) => {
        this.transformedGroupList = groupList;
      }
    );

    this.workGroupService.loadInitialWorkGroups();
  }
}
