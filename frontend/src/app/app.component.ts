import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { WorkGroupComponent } from './work-group/work-group.component';
import { UploadFileComponent } from './upload-file/upload-file.component';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    WorkGroupComponent,
    MatButtonModule,
    UploadFileComponent,
    MatDividerModule,
  ],
  template: `
    <header>
      <div class="h-content">
        <p>Syzyf</p>
        <img src="./sisyphus.svg" alt="" />
      </div>
    </header>
    <mat-divider></mat-divider>
    <div class="upload-wrapper" *ngIf="showUploadFileForm">
      <app-upload-file></app-upload-file>
    </div>
    <router-outlet></router-outlet>
  `,
  styleUrl: './app.component.scss',
})
export class AppComponent {
  showUploadFileForm: boolean = false;
  toggled: boolean = true;
  constructor(private router: Router) {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.showUploadFileForm = this.router.url === '/';
      }
    });
  }
}
