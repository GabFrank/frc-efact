import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-metric-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  template: `
    <mat-card class="metric-card">
      <mat-card-content>
        <div class="metric-header">
          <mat-icon [class]="'metric-icon ' + iconColor">{{ icon }}</mat-icon>
          <span class="metric-label">{{ label }}</span>
        </div>
        <div class="metric-value">{{ value }}</div>
        <div class="metric-subtitle" *ngIf="subtitle">{{ subtitle }}</div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .metric-card {
      height: 100%;
      cursor: default;
    }

    mat-card-content {
      padding: 20px;
    }

    .metric-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
    }

    .metric-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .metric-icon.primary {
      color: #1976d2;
    }

    .metric-icon.accent {
      color: #ff4081;
    }

    .metric-icon.success {
      color: #4caf50;
    }

    .metric-icon.warn {
      color: #ff9800;
    }

    .metric-label {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.6);
      font-weight: 500;
    }

    .metric-value {
      font-size: 32px;
      font-weight: 700;
      color: rgba(0, 0, 0, 0.87);
      margin-bottom: 4px;
    }

    .metric-subtitle {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.54);
    }
  `]
})
export class MetricCardComponent {
  @Input() label: string = '';
  @Input() value: string | number = '';
  @Input() subtitle?: string;
  @Input() icon: string = 'analytics';
  @Input() iconColor: 'primary' | 'accent' | 'success' | 'warn' = 'primary';
}
