import { Component, Input, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-chart-card',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  template: `
    <mat-card class="chart-card">
      <mat-card-header *ngIf="title">
        <mat-card-title>{{ title }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <div class="chart-container">
          <canvas #chartCanvas></canvas>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .chart-card {
      height: 100%;
    }

    mat-card-header {
      padding: 16px 16px 0;
    }

    mat-card-content {
      padding: 16px;
    }

    .chart-container {
      position: relative;
      height: 300px;
      width: 100%;
    }
  `]
})
export class ChartCardComponent implements AfterViewInit {
  @ViewChild('chartCanvas', { static: false }) chartCanvas!: ElementRef<HTMLCanvasElement>;
  @Input() title?: string;
  @Input() chartConfig!: ChartConfiguration;

  private chart?: Chart;

  ngAfterViewInit(): void {
    if (this.chartCanvas && this.chartConfig) {
      this.createChart();
    }
  }

  private createChart(): void {
    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (ctx) {
      this.chart = new Chart(ctx, this.chartConfig);
    }
  }

  updateChart(config: ChartConfiguration): void {
    if (this.chart) {
      this.chart.destroy();
    }
    this.chartConfig = config;
    this.createChart();
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
    }
  }
}
