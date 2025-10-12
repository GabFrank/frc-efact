import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ChartConfiguration } from 'chart.js';
import { DashboardApiService } from '../../core/api/dashboard-api.service';
import { DashboardEmpresa, ClienteRanking } from '../../models/dashboard.model';
import { MetricCardComponent } from '../../shared/components/metric-card/metric-card.component';
import { ChartCardComponent } from '../../shared/components/chart-card/chart-card.component';
import { RankingListComponent, RankingItem } from '../../shared/components/ranking-list/ranking-list.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-dashboard-empresa',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MetricCardComponent,
    ChartCardComponent,
    RankingListComponent,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <div>
          <h1 class="dashboard-title">Dashboard de Empresa</h1>
          <p class="dashboard-subtitle" *ngIf="dashboard">{{ dashboard.razonSocial }}</p>
        </div>
        
        <!-- Filtro de fechas -->
        <mat-card class="filter-card">
          <form [formGroup]="filterForm" class="filter-form">
            <mat-form-field appearance="outline">
              <mat-label>Fecha Inicio</mat-label>
              <input matInput [matDatepicker]="pickerInicio" formControlName="fechaInicio">
              <mat-datepicker-toggle matIconSuffix [for]="pickerInicio"></mat-datepicker-toggle>
              <mat-datepicker #pickerInicio></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Fecha Fin</mat-label>
              <input matInput [matDatepicker]="pickerFin" formControlName="fechaFin">
              <mat-datepicker-toggle matIconSuffix [for]="pickerFin"></mat-datepicker-toggle>
              <mat-datepicker #pickerFin></mat-datepicker>
            </mat-form-field>

            <button mat-raised-button color="primary" (click)="aplicarFiltros()">
              <mat-icon>filter_list</mat-icon>
              Filtrar
            </button>

            <button mat-button (click)="limpiarFiltros()">
              <mat-icon>clear</mat-icon>
              Limpiar
            </button>
          </form>
        </mat-card>
      </div>

      <app-loading-spinner *ngIf="loading"></app-loading-spinner>
      <app-error-message *ngIf="error" [message]="error"></app-error-message>

      <div *ngIf="!loading && !error && dashboard" class="dashboard-content">
        <!-- Métricas principales -->
        <div class="metrics-grid">
          <app-metric-card
            label="Total Facturas"
            [value]="dashboard.totalFacturasEmitidas"
            subtitle="Facturas emitidas"
            icon="receipt_long"
            iconColor="primary">
          </app-metric-card>

          <app-metric-card
            label="Ventas del Mes"
            [value]="formatCurrency(dashboard.totalGuaraniesMesActual)"
            subtitle="Total en guaraníes"
            icon="payments"
            iconColor="success">
          </app-metric-card>

          <app-metric-card
            label="IVA 10%"
            [value]="formatCurrency(dashboard.totalesPorIva.totalIva10)"
            subtitle="Ventas con IVA 10%"
            icon="percent"
            iconColor="accent">
          </app-metric-card>

          <app-metric-card
            label="Total General"
            [value]="formatCurrency(dashboard.totalesPorIva.totalGeneral)"
            subtitle="Suma de todas las tasas"
            icon="account_balance"
            iconColor="warn">
          </app-metric-card>
        </div>

        <!-- Gráficos y rankings -->
        <div class="charts-grid">
          <!-- Gráfico de torta por IVA -->
          <app-chart-card
            title="Ventas por Tasa de IVA"
            [chartConfig]="ivaChartConfig">
          </app-chart-card>

          <!-- Ranking de clientes -->
          <app-ranking-list
            title="Top 10 Clientes"
            [items]="clientesRanking"
            emptyMessage="No hay datos de clientes">
          </app-ranking-list>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .dashboard-header {
      margin-bottom: 24px;
    }

    .dashboard-title {
      font-size: 28px;
      font-weight: 500;
      margin: 0 0 4px 0;
      color: rgba(0, 0, 0, 0.87);
    }

    .dashboard-subtitle {
      font-size: 16px;
      color: rgba(0, 0, 0, 0.6);
      margin: 0 0 16px 0;
    }

    .filter-card {
      padding: 16px;
      margin-top: 16px;
    }

    .filter-form {
      display: flex;
      gap: 16px;
      align-items: center;
      flex-wrap: wrap;
    }

    .filter-form mat-form-field {
      flex: 0 1 200px;
    }

    .dashboard-content {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
    }

    .charts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 24px;
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 16px;
      }

      .metrics-grid {
        grid-template-columns: 1fr;
      }

      .charts-grid {
        grid-template-columns: 1fr;
      }

      .filter-form {
        flex-direction: column;
        align-items: stretch;
      }

      .filter-form mat-form-field {
        flex: 1 1 auto;
      }

      .filter-form button {
        width: 100%;
      }
    }
  `]
})
export class DashboardEmpresaComponent implements OnInit {
  dashboard?: DashboardEmpresa;
  loading = false;
  error?: string;
  empresaId!: number;

  filterForm = new FormGroup({
    fechaInicio: new FormControl<Date | null>(null),
    fechaFin: new FormControl<Date | null>(null)
  });

  ivaChartConfig!: ChartConfiguration;
  clientesRanking: RankingItem[] = [];

  constructor(
    private route: ActivatedRoute,
    private dashboardApi: DashboardApiService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.empresaId = +params['id'];
      this.loadDashboard();
    });
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = undefined;

    const fechaInicio = this.filterForm.value.fechaInicio 
      ? this.formatDate(this.filterForm.value.fechaInicio) 
      : undefined;
    const fechaFin = this.filterForm.value.fechaFin 
      ? this.formatDate(this.filterForm.value.fechaFin) 
      : undefined;

    this.dashboardApi.getDashboardEmpresa(this.empresaId, fechaInicio, fechaFin).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.updateCharts();
        this.updateRankings();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el dashboard de la empresa';
        this.loading = false;
        console.error('Error loading empresa dashboard:', err);
      }
    });
  }

  aplicarFiltros(): void {
    this.loadDashboard();
  }

  limpiarFiltros(): void {
    this.filterForm.reset();
    this.loadDashboard();
  }

  private updateCharts(): void {
    if (!this.dashboard) return;

    const totales = this.dashboard.totalesPorIva;
    
    this.ivaChartConfig = {
      type: 'pie',
      data: {
        labels: ['IVA 10%', 'IVA 5%', 'IVA 0%'],
        datasets: [{
          data: [
            totales.totalIva10,
            totales.totalIva5,
            totales.totalIva0
          ],
          backgroundColor: [
            '#1976d2',
            '#4caf50',
            '#ff9800'
          ],
          borderWidth: 2,
          borderColor: '#ffffff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: {
                size: 12
              }
            }
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                return `${label}: ${this.formatCurrency(value)}`;
              }
            }
          }
        }
      }
    };
  }

  private updateRankings(): void {
    if (!this.dashboard) return;

    this.clientesRanking = this.dashboard.top10Clientes.map((cliente, index) => ({
      position: index + 1,
      title: cliente.nombre,
      subtitle: cliente.ruc || 'Sin RUC',
      value: this.formatCurrency(cliente.montoTotal)
    }));
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-PY', {
      style: 'currency',
      currency: 'PYG',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
