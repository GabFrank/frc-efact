import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReporteApiService } from '../../core/api/reporte-api.service';
import { ClienteRanking } from '../../models/reporte.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-reporte-clientes',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatIconModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="reporte-clientes-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Reporte por Clientes</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <!-- Filtros -->
          <form [formGroup]="filtroForm" class="filtros-form">
            <div class="filtros-row">
              <mat-form-field appearance="outline">
                <mat-label>Empresa ID</mat-label>
                <input matInput type="number" formControlName="empresaId" required />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha Desde</mat-label>
                <input matInput [matDatepicker]="pickerDesde" formControlName="fechaDesde" />
                <mat-datepicker-toggle matSuffix [for]="pickerDesde"></mat-datepicker-toggle>
                <mat-datepicker #pickerDesde></mat-datepicker>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha Hasta</mat-label>
                <input matInput [matDatepicker]="pickerHasta" formControlName="fechaHasta" />
                <mat-datepicker-toggle matSuffix [for]="pickerHasta"></mat-datepicker-toggle>
                <mat-datepicker #pickerHasta></mat-datepicker>
              </mat-form-field>

              <div class="filtros-actions">
                <button mat-raised-button color="primary" (click)="aplicarFiltros()" 
                        [disabled]="!filtroForm.valid">
                  <mat-icon>search</mat-icon>
                  Buscar
                </button>
                <button mat-button (click)="limpiarFiltros()">
                  <mat-icon>clear</mat-icon>
                  Limpiar
                </button>
              </div>
            </div>
          </form>

          <!-- Loading -->
          <app-loading-spinner *ngIf="loading"></app-loading-spinner>

          <!-- Error -->
          <app-error-message *ngIf="error" [message]="error"></app-error-message>

          <!-- Resultados -->
          <div *ngIf="!loading && !error && clientes.length > 0" class="resultados">
            <!-- Gráfico -->
            <div class="chart-container">
              <h3>Top 10 Clientes por Monto Facturado</h3>
              <canvas #chartCanvas></canvas>
            </div>

            <!-- Tabla -->
            <div class="table-container">
              <h3>Detalle de Clientes</h3>
              <table mat-table [dataSource]="clientes" class="clientes-table">
                <ng-container matColumnDef="clienteNombre">
                  <th mat-header-cell *matHeaderCellDef>Cliente</th>
                  <td mat-cell *matCellDef="let cliente">{{ cliente.clienteNombre }}</td>
                </ng-container>

                <ng-container matColumnDef="clienteRuc">
                  <th mat-header-cell *matHeaderCellDef>RUC</th>
                  <td mat-cell *matCellDef="let cliente">{{ cliente.clienteRuc || '-' }}</td>
                </ng-container>

                <ng-container matColumnDef="cantidadFacturas">
                  <th mat-header-cell *matHeaderCellDef>Cantidad Facturas</th>
                  <td mat-cell *matCellDef="let cliente">{{ cliente.cantidadFacturas }}</td>
                </ng-container>

                <ng-container matColumnDef="totalFacturado">
                  <th mat-header-cell *matHeaderCellDef>Total Facturado</th>
                  <td mat-cell *matCellDef="let cliente">{{ cliente.totalFacturado | currency: 'PYG' }}</td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
              </table>
            </div>

            <!-- Totales -->
            <div class="totales-container">
              <mat-card>
                <mat-card-content>
                  <h3>Resumen</h3>
                  <p><strong>Total Clientes:</strong> {{ clientes.length }}</p>
                  <p><strong>Total Facturado:</strong> {{ calcularTotalFacturado() | currency: 'PYG' }}</p>
                  <p><strong>Total Facturas:</strong> {{ calcularTotalFacturas() }}</p>
                </mat-card-content>
              </mat-card>
            </div>
          </div>

          <!-- Sin resultados -->
          <div *ngIf="!loading && !error && clientes.length === 0 && filtroForm.value.empresaId" class="sin-resultados">
            <p>No se encontraron clientes con los filtros aplicados.</p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .reporte-clientes-container {
      padding: 20px;
    }

    mat-card {
      margin-bottom: 20px;
    }

    .filtros-form {
      margin-bottom: 20px;
    }

    .filtros-row {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
      flex-wrap: wrap;
      align-items: center;
    }

    mat-form-field {
      flex: 1;
      min-width: 200px;
    }

    .filtros-actions {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .resultados {
      margin-top: 20px;
    }

    .chart-container {
      margin-bottom: 30px;
      padding: 20px;
      background: #f5f5f5;
      border-radius: 8px;
    }

    .chart-container h3 {
      margin-top: 0;
      margin-bottom: 20px;
      text-align: center;
    }

    .chart-container canvas {
      max-height: 400px;
    }

    .table-container {
      margin-bottom: 20px;
    }

    .table-container h3 {
      margin-bottom: 16px;
    }

    .clientes-table {
      width: 100%;
    }

    .totales-container {
      margin-top: 20px;
    }

    .totales-container mat-card {
      background: #e3f2fd;
    }

    .totales-container h3 {
      margin-top: 0;
      margin-bottom: 16px;
    }

    .totales-container p {
      margin: 8px 0;
      font-size: 16px;
    }

    .sin-resultados {
      text-align: center;
      padding: 40px;
      color: #666;
    }
  `]
})
export class ReporteClientesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reporteService = inject(ReporteApiService);

  filtroForm!: FormGroup;
  clientes: ClienteRanking[] = [];
  loading = false;
  error: string | null = null;
  chart: Chart | null = null;

  displayedColumns = ['clienteNombre', 'clienteRuc', 'cantidadFacturas', 'totalFacturado'];

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.filtroForm = this.fb.group({
      empresaId: [null],
      fechaDesde: [null],
      fechaHasta: [null]
    });
  }

  aplicarFiltros(): void {
    if (!this.filtroForm.valid) {
      return;
    }

    this.loading = true;
    this.error = null;

    const empresaId = this.filtroForm.value.empresaId;
    const fechaDesde = this.filtroForm.value.fechaDesde 
      ? this.formatDate(this.filtroForm.value.fechaDesde) 
      : undefined;
    const fechaHasta = this.filtroForm.value.fechaHasta 
      ? this.formatDate(this.filtroForm.value.fechaHasta) 
      : undefined;

    this.reporteService.reporteClientes(empresaId, fechaDesde, fechaHasta).subscribe({
      next: (data) => {
        this.clientes = data;
        this.loading = false;
        setTimeout(() => this.renderChart(), 100);
      },
      error: (err) => {
        this.error = 'Error al cargar el reporte de clientes';
        this.loading = false;
        console.error(err);
      }
    });
  }

  limpiarFiltros(): void {
    this.filtroForm.reset();
    this.clientes = [];
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }

  private renderChart(): void {
    if (this.chart) {
      this.chart.destroy();
    }

    const canvas = document.querySelector('canvas') as HTMLCanvasElement;
    if (!canvas) {
      return;
    }

    const top10 = this.clientes.slice(0, 10);
    const labels = top10.map(c => c.clienteNombre);
    const data = top10.map(c => c.totalFacturado);

    const config: ChartConfiguration = {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: 'Total Facturado (PYG)',
          data: data,
          backgroundColor: 'rgba(54, 162, 235, 0.6)',
          borderColor: 'rgba(54, 162, 235, 1)',
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function(value) {
                return new Intl.NumberFormat('es-PY', {
                  style: 'currency',
                  currency: 'PYG',
                  minimumFractionDigits: 0
                }).format(value as number);
              }
            }
          }
        },
        plugins: {
          legend: {
            display: true,
            position: 'top'
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                return new Intl.NumberFormat('es-PY', {
                  style: 'currency',
                  currency: 'PYG',
                  minimumFractionDigits: 0
                }).format(context.parsed.y);
              }
            }
          }
        }
      }
    };

    this.chart = new Chart(canvas, config);
  }

  calcularTotalFacturado(): number {
    return this.clientes.reduce((sum, cliente) => sum + cliente.totalFacturado, 0);
  }

  calcularTotalFacturas(): number {
    return this.clientes.reduce((sum, cliente) => sum + cliente.cantidadFacturas, 0);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
