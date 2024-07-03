import {
  Directive,
  EventEmitter,
  HostBinding,
  HostListener,
  Output,
} from '@angular/core';

@Directive({
  selector: '[appDragAndDrop]',
  standalone: true,
})
export class DragAndDropDirective {
  @HostBinding('class.fileOver') fileOver: boolean = false;

  @Output() fileDropped = new EventEmitter<any>();

  constructor() {}

  @HostListener('dragover', ['$event']) onDragOver(evt: DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();

    console.log('Drag over');
    this.fileOver = true;
  }

  @HostListener('dragleave', ['$event']) public onDragLeave(evt: DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();

    console.log('Drag leave');
    this.fileOver = false;
  }

  @HostListener('drop', ['$event']) public onDrop(evt: DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();

    this.fileOver = false;
    const files = evt.dataTransfer?.files;
    if (files && files.length > 0) {
      const file = files[0]; // Get the first file
      this.fileDropped.emit(file); // Emit only the first file
    }
  }
}
