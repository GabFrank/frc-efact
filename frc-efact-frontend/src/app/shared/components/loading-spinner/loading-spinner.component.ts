import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule],
  template: `
    <div class="loading-container" *ngIf="loading">
      <mat-spinner [diameter]="diameter" [color]="color"></mat-spinner>
      <p *ngIf="message" class="loading-message">{{ message }}</p>
    </div>
  `,
  styles: [`
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }

    .loading-message {
      margin-top: 16px;
      color: rgba(0, 0, 0, 0.54);
      font-size: 14px;
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() loading = false;
  @Input() message?: string;
  @Input() diameter = 50;
  @Input() color: 'primary' | 'accent' | 'warn' = 'primary';
}
