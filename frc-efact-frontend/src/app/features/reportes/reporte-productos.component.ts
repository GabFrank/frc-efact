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
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReporteApiService } from '../../core/api/reporte-api.service';
import { ProductoReporte } from '../../models/reporte.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-reporte-productos',
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
    MatSortModule,
    MatIconModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="reporte-productos-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Reporte por Productos</mat-card-title>
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
          <div *ngIf="!loading && !error && productos.length > 0" class="resultados">
            <!-- Tabla -->
            <div class="table-container">
              <table mat-table [dataSource]="sortedProductos" matSort (matSortChange)="sortData($event)" 
                     class="productos-table">
                <ng-container matColumnDef="productoId">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
                  <td mat-cell *matCellDef="let producto">{{ producto.productoId }}</td>
                </ng-container>

                <ng-container matColumnDef="descripcion">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>Descripción</th>
                  <td mat-cell *matCellDef="let producto">{{ producto.descripcion }}</td>
                </ng-container>

                <ng-container matColumnDef="cantidadVendida">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>Cantidad Vendida</th>
                  <td mat-cell *matCellDef="let producto">{{ producto.cantidadVendida | number: '1.2-2' }}</td>
                </ng-container>

                <ng-container matColumnDef="montoTotal">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>Monto Total</th>
                  <td mat-cell *matCellDef="let producto">{{ producto.montoTotal | currency: 'PYG' }}</td>
                </ng-container>

                <ng-container matColumnDef="promedioUnitario">
                  <th mat-header-cell *matHeaderCellDef>Precio Promedio</th>
                  <td mat-cell *matCellDef="let producto">
                    {{ calcularPrecioPromedio(producto) | currency: 'PYG' }}
                  </td>
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
                  <div class="totales-grid">
                    <div class="total-item">
                      <span class="total-label">Total Productos:</span>
                      <span class="total-value">{{ productos.length }}</span>
                    </div>
                    <div class="total-item">
                      <span class="total-label">Cantidad Total Vendida:</span>
                      <span class="total-value">{{ calcularCantidadTotal() | number: '1.2-2' }}</span>
                    </div>
                    <div class="total-item">
                      <span class="total-label">Monto Total:</span>
                      <span class="total-value">{{ calcularMontoTotal() | currency: 'PYG' }}</span>
                    </div>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>

            <!-- Top 5 Productos -->
            <div class="top-productos-container">
              <mat-card>
                <mat-card-header>
                  <mat-card-title>Top 5 Productos Más Vendidos</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <div class="top-list">
                    <div *ngFor="let producto of getTop5Productos(); let i = index" class="top-item">
                      <span class="top-rank">{{ i + 1 }}</span>
                      <div class="top-info">
                        <div class="top-nombre">{{ producto.descripcion }}</div>
                        <div class="top-stats">
                          Cantidad: {{ producto.cantidadVendida | number: '1.2-2' }} | 
                          Monto: {{ producto.montoTotal | currency: 'PYG' }}
                        </div>
                      </div>
                    </div>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          </div>

          <!-- Sin resultados -->
          <div *ngIf="!loading && !error && productos.length === 0 && filtroForm.value.empresaId" class="sin-resultados">
            <p>No se encontraron productos con los filtros aplicados.</p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .reporte-productos-container {
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

    .table-container {
      margin-bottom: 20px;
    }

    .productos-table {
      width: 100%;
    }

    .totales-container {
      margin-bottom: 20px;
    }

    .totales-container mat-card {
      background: #e8f5e9;
    }

    .totales-container h3 {
      margin-top: 0;
      margin-bottom: 16px;
    }

    .totales-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
    }

    .total-item {
      display: flex;
      flex-direction: column;
      padding: 12px;
      background: white;
      border-radius: 4px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .total-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }

    .total-value {
      font-size: 20px;
      font-weight: bold;
      color: #2e7d32;
    }

    .top-productos-container mat-card {
      background: #fff3e0;
    }

    .top-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .top-item {
      display: flex;
      align-items: center;
      padding: 12px;
      background: white;
      border-radius: 4px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .top-rank {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      background: #ff9800;
      color: white;
      border-radius: 50%;
      font-weight: bold;
      font-size: 18px;
      margin-right: 16px;
      flex-shrink: 0;
    }

    .top-info {
      flex: 1;
    }

    .top-nombre {
      font-weight: 500;
      margin-bottom: 4px;
    }

    .top-stats {
      font-size: 14px;
      color: #666;
    }

    .sin-resultados {
      text-align: center;
      padding: 40px;
      color: #666;
    }
  `]
})
export class ReporteProductosComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reporteService = inject(ReporteApiService);

  filtroForm!: FormGroup;
  productos: ProductoReporte[] = [];
  sortedProductos: ProductoReporte[] = [];
  loading = false;
  error: string | null = null;

  displayedColumns = ['productoId', 'descripcion', 'cantidadVendida', 'montoTotal', 'promedioUnitario'];

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

    this.reporteService.reporteProductos(empresaId, fechaDesde, fechaHasta).subscribe({
      next: (data) => {
        this.productos = data;
        this.sortedProductos = [...data];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el reporte de productos';
        this.loading = false;
        console.error(err);
      }
    });
  }

  limpiarFiltros(): void {
    this.filtroForm.reset();
    this.productos = [];
    this.sortedProductos = [];
  }

  sortData(sort: Sort): void {
    const data = this.productos.slice();
    if (!sort.active || sort.direction === '') {
      this.sortedProductos = data;
      return;
    }

    this.sortedProductos = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case 'productoId':
          return this.compare(a.productoId, b.productoId, isAsc);
        case 'descripcion':
          return this.compare(a.descripcion, b.descripcion, isAsc);
        case 'cantidadVendida':
          return this.compare(a.cantidadVendida, b.cantidadVendida, isAsc);
        case 'montoTotal':
          return this.compare(a.montoTotal, b.montoTotal, isAsc);
        default:
          return 0;
      }
    });
  }

  private compare(a: number | string, b: number | string, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  calcularPrecioPromedio(producto: ProductoReporte): number {
    if (producto.cantidadVendida === 0) {
      return 0;
    }
    return producto.montoTotal / producto.cantidadVendida;
  }

  calcularCantidadTotal(): number {
    return this.productos.reduce((sum, producto) => sum + producto.cantidadVendida, 0);
  }

  calcularMontoTotal(): number {
    return this.productos.reduce((sum, producto) => sum + producto.montoTotal, 0);
  }

  getTop5Productos(): ProductoReporte[] {
    return [...this.productos]
      .sort((a, b) => b.cantidadVendida - a.cantidadVendida)
      .slice(0, 5);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
