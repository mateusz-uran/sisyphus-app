import {
  Component,
  ElementRef,
  ViewChild,
  ViewEncapsulation,
  inject,
} from '@angular/core';
import { WorkGroupService } from '../services/work-group.service';
import { HttpResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { DragAndDropDirective } from '../directives/drag-and-drop.directive';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-upload-file',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    DragAndDropDirective,
    MatExpansionModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  encapsulation: ViewEncapsulation.None,
  template: `<div
    class="container"
    appDragAndDrop
    (fileDropped)="onFileDropped($event)"
  >
    <div class="input-body">
      <input
        type="file"
        #fileDropRef
        id="fileDropRef"
        (change)="fileBrowseHandler($event)"
        style="display: none;"
        #input
      />
      <p>Utwórz grupę dodając plik PDF.</p>
      <button mat-raised-button (click)="openFileSelector()">Wybierz</button>
      <p class="small">Wybierz lub przeciągnij</p>
    </div>

    <mat-expansion-panel hideToggle disabled expanded="{{ !!currentFile }}">
      <div class="panel-body">
        <p>{{ currentFile?.name }}</p>
        <button
          mat-icon-button
          [disabled]="!currentFile"
          (click)="upload(input)"
        >
          <mat-icon>upload</mat-icon>
        </button>
      </div>
      <mat-progress-bar
        mode="indeterminate"
        *ngIf="isUploading"
      ></mat-progress-bar>
    </mat-expansion-panel>
  </div>`,
  styleUrl: './upload-file.component.scss',
})
export class UploadFileComponent {
  @ViewChild('fileDropRef') fileDropRef!: ElementRef<HTMLInputElement>;

  workGroupService: WorkGroupService = inject(WorkGroupService);
  currentFile?: File | null;
  MIME_type_accepted = 'application/pdf';
  isUploading: boolean = false;

  constructor(private _snackBar: MatSnackBar) {}

  checkFileMimeType(file: File): boolean {
    if (this.MIME_type_accepted !== file.type) {
      this.handleSnackBar('Wrong file type', 'Ok');
      return false;
    }
    return true;
  }

  onFileDropped(file: File) {
    if (this.checkFileMimeType(file)) {
      this.currentFile = file;
    }
  }

  fileBrowseHandler(event: Event) {
    const inputElement = event.target as HTMLInputElement;

    if (inputElement.files && inputElement.files.length > 0) {
      const file = inputElement.files[0];
      if (this.checkFileMimeType(file)) {
        this.currentFile = file;
      }
    }
  }

  openFileSelector() {
    this.fileDropRef.nativeElement.click();
  }

  upload(input: any): void {
    this.isUploading = true;
    if (this.currentFile) {
      this.workGroupService.addWorkGroup(this.currentFile).subscribe({
        next: (event: any) => {
          if (event instanceof HttpResponse) {
            this.isUploading = false;
            this.handleSnackBar('File uploaded successfuly!', 'Ok');
          }
        },
        error: (err: any) => {
          console.log('Error: ', err);
          this.currentFile = undefined;
          this.isUploading = false;
          this.handleSnackBar('Could not upload the file!', 'Ok');
        },
        complete: () => {
          this.currentFile = undefined;
          input.value = null;
          this.isUploading = false;
        },
      });
    }
  }

  handleSnackBar(message: string, action: string) {
    this._snackBar.open(message, action);
  }
}
