import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.css'],
  standalone: true,
  imports:[CommonModule]
})
export class AppFileUploadComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  isAdvancedUpload: boolean = false;
  droppedFiles: File[] | null = null;
  uploadStatus: 'initial' | 'uploading' | 'success' | 'error' = 'initial';
  errorMessage: string = '';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.checkAdvancedUpload();
  }

  checkAdvancedUpload() {
    const div = document.createElement('div');
    this.isAdvancedUpload = (
      ('draggable' in div) || 
      ('ondragstart' in div && 'ondrop' in div)
    ) && 'FormData' in window && 'FileReader' in window;
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.processFiles(Array.from(input.files));
    }
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    
    this.removedragOverClass();
    
    if (event.dataTransfer?.files) {
      this.processFiles(Array.from(event.dataTransfer.files));
    }
  }

  processFiles(files: File[]) {
    this.droppedFiles = files;
    this.submitForm();
  }

  submitForm() {
    if (this.uploadStatus === 'uploading') return;

    this.uploadStatus = 'uploading';

    if (this.droppedFiles) {
      const formData = new FormData();
      this.droppedFiles.forEach(file => {
        formData.append('file', file, file.name);
      });

      this.http.post('http://localhost:8080/process', formData).subscribe({
        next: (response: any) => {
          if (response.redirectUrl) {
            if (response.redirectUrl.startsWith('/')) {
              this.router.navigateByUrl(response.redirectUrl);
            } else {
              window.location.href = response.redirectUrl;
            }
          }
        },
        error: (error) => {
          this.uploadStatus = 'error';
          this.errorMessage = 'Upload failed. Please try again.';
          console.error('Upload error', error);
        }
      });
    }
  }

  // Drag and drop event handlers
  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.addDragOverClass();
  }

  onDragEnter(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.addDragOverClass();
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.removedragOverClass();
  }

  addDragOverClass() {
    const box = event?.currentTarget as HTMLElement;
    box?.classList.add('is-dragover');
  }

  removedragOverClass() {
    const box = event?.currentTarget as HTMLElement;
    box?.classList.remove('is-dragover');
  }

  restart() {
    this.uploadStatus = 'initial';
    this.droppedFiles = null;
    this.errorMessage = '';
    // Trigger file input click
    this.fileInput.nativeElement.click();
  }

  getFileName(): string {
    if (!this.droppedFiles) return 'No file chosen';
    
    return this.droppedFiles.length > 1 
      ? `${this.droppedFiles.length} files selected`
      : this.droppedFiles[0].name;
  }
}