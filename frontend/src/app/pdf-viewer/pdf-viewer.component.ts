import { CommonModule } from '@angular/common';
import { Component, Input, input } from '@angular/core';
import { PdfViewerModule } from 'ng2-pdf-viewer';

@Component({
  selector: 'app-pdf-viewer',
  standalone: true,
  imports: [CommonModule, PdfViewerModule],
  template: `
    <div *ngIf="!errorOccurred; else errorTemplate">
      <pdf-viewer
        [src]="cvArr"
        [original-size]="false"
        [show-borders]="true"
        (error)="onError($event)"
        class="pdf-viewer"
      ></pdf-viewer>
    </div>
    <ng-template #errorTemplate>
      <p>Error</p>
    </ng-template>
  `,
  styleUrl: './pdf-viewer.component.scss',
})
export class PdfViewerComponent {
  @Input() cv: any;
  cvArr: Uint8Array | undefined = new Uint8Array();
  errorOccurred: boolean = false;

  ngOnInit() {
    this.convertStringToPDF();
  }

  onError(error: any) {
    if (error.message && error.message.includes('Worker was destroyed')) {
      console.warn(error);
      return;
    }
    console.log(error);
    this.errorOccurred = true;
  }

  async convertStringToPDF() {
    try {
      this.cvArr = new Uint8Array(await this.cv.arrayBuffer());
    } catch (error) {
      this.onError(error);
    }
  }
}
