import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

export interface RankingItem {
  position?: number;
  title: string;
  subtitle?: string;
  value: string | number;
  icon?: string;
}

@Component({
  selector: 'app-ranking-list',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatListModule, MatIconModule],
  template: `
    <mat-card class="ranking-card">
      <mat-card-header *ngIf="title">
        <mat-card-title>{{ title }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <mat-list *ngIf="items && items.length > 0; else noData">
          <mat-list-item *ngFor="let item of items; let i = index" class="ranking-item">
            <div class="ranking-content">
              <div class="ranking-left">
                <span class="ranking-position">{{ item.position || (i + 1) }}</span>
                <div class="ranking-info">
                  <div class="ranking-title">{{ item.title }}</div>
                  <div class="ranking-subtitle" *ngIf="item.subtitle">{{ item.subtitle }}</div>
                </div>
              </div>
              <div class="ranking-value">{{ item.value }}</div>
            </div>
          </mat-list-item>
        </mat-list>
        <ng-template #noData>
          <div class="no-data">
            <mat-icon>info</mat-icon>
            <p>{{ emptyMessage || 'No hay datos disponibles' }}</p>
          </div>
        </ng-template>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .ranking-card {
      height: 100%;
    }

    mat-card-header {
      padding: 16px 16px 0;
    }

    mat-card-content {
      padding: 0;
    }

    mat-list {
      padding: 0;
    }

    .ranking-item {
      height: auto !important;
      padding: 12px 16px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.12);
    }

    .ranking-item:last-child {
      border-bottom: none;
    }

    .ranking-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .ranking-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
      min-width: 0;
    }

    .ranking-position {
      font-size: 18px;
      font-weight: 700;
      color: #1976d2;
      min-width: 24px;
      text-align: center;
    }

    .ranking-info {
      flex: 1;
      min-width: 0;
    }

    .ranking-title {
      font-size: 14px;
      font-weight: 500;
      color: rgba(0, 0, 0, 0.87);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .ranking-subtitle {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.54);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .ranking-value {
      font-size: 16px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.87);
      white-space: nowrap;
      margin-left: 12px;
    }

    .no-data {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 20px;
      color: rgba(0, 0, 0, 0.54);
    }

    .no-data mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      opacity: 0.5;
    }

    .no-data p {
      margin: 0;
      font-size: 14px;
    }
  `]
})
export class RankingListComponent {
  @Input() title?: string;
  @Input() items: RankingItem[] = [];
  @Input() emptyMessage?: string;
}
