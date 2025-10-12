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
import { UsuarioReporte } from '../../models/reporte.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-reporte-usuarios',
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
    <div class="reporte-usuarios-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Reporte por Usuarios</mat-card-title>
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
          <div *ngIf="!loading && !error && usuarios.length > 0" class="resultados">
            <!-- Tabla -->
            <div class="table-container">
              <table mat-table [dataSource]="sortedUsuarios" matSort (matSortChange)="sortData($event)" 
                     class="usuarios-table">
                <ng-container matColumnDef="usuarioId">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
                  <td mat-cell *matCellDef="let usuario">{{ usuario.usuarioId }}</td>
                </ng-container>

                <ng-container matColumnDef="usuarioNombre">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>Usuario</th>
                  <td mat-cell *matCellDef="let usuario">{{ usuario.usuarioNombre }}</td>
                </ng-container>

                <ng-container matColumnDef="cantidadFacturas">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>Cantidad Facturas</th>
                  <td mat-cell *matCellDef="let usuario">{{ usuario.cantidadFacturas }}</td>
                </ng-container>

                <ng-container matColumnDef="totalFacturado">
                  <th mat-header-cell *matHeaderCellDef mat-sort-header>Total Facturado</th>
                  <td mat-cell *matCellDef="let usuario">{{ usuario.totalFacturado | currency: 'PYG' }}</td>
                </ng-container>

                <ng-container matColumnDef="promedioFactura">
                  <th mat-header-cell *matHeaderCellDef>Promedio por Factura</th>
                  <td mat-cell *matCellDef="let usuario">
                    {{ calcularPromedioFactura(usuario) | currency: 'PYG' }}
                  </td>
                </ng-container>

                <ng-container matColumnDef="porcentaje">
                  <th mat-header-cell *matHeaderCellDef>% del Total</th>
                  <td mat-cell *matCellDef="let usuario">
                    {{ calcularPorcentaje(usuario) | number: '1.2-2' }}%
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
                  <h3>Resumen General</h3>
                  <div class="totales-grid">
                    <div class="total-item">
                      <span class="total-label">Total Usuarios:</span>
                      <span class="total-value">{{ usuarios.length }}</span>
                    </div>
                    <div class="total-item">
                      <span class="total-label">Total Facturas:</span>
                      <span class="total-value">{{ calcularTotalFacturas() }}</span>
                    </div>
                    <div class="total-item">
                      <span class="total-label">Total Facturado:</span>
                      <span class="total-value">{{ calcularTotalFacturado() | currency: 'PYG' }}</span>
                    </div>
                    <div class="total-item">
                      <span class="total-label">Promedio por Usuario:</span>
                      <span class="total-value">{{ calcularPromedioUsuario() | currency: 'PYG' }}</span>
                    </div>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>

            <!-- Ranking -->
            <div class="ranking-container">
              <mat-card>
                <mat-card-header>
                  <mat-card-title>Top 5 Usuarios por Facturación</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <div class="ranking-list">
                    <div *ngFor="let usuario of getTop5Usuarios(); let i = index" class="ranking-item">
                      <div class="ranking-position">
                        <span class="position-number">{{ i + 1 }}</span>
                      </div>
                      <div class="ranking-info">
                        <div class="ranking-nombre">{{ usuario.usuarioNombre }}</div>
                        <div class="ranking-stats">
                          <span class="stat">
                            <mat-icon>receipt</mat-icon>
                            {{ usuario.cantidadFacturas }} facturas
                          </span>
                          <span class="stat">
                            <mat-icon>attach_money</mat-icon>
                            {{ usuario.totalFacturado | currency: 'PYG' }}
                          </span>
                        </div>
                      </div>
                      <div class="ranking-badge">
                        <span class="badge-percentage">
                          {{ calcularPorcentaje(usuario) | number: '1.1-1' }}%
                        </span>
                      </div>
                    </div>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          </div>

          <!-- Sin resultados -->
          <div *ngIf="!loading && !error && usuarios.length === 0 && filtroForm.value.empresaId" class="sin-resultados">
            <p>No se encontraron usuarios con facturas en el período seleccionado.</p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .reporte-usuarios-container {
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

    .usuarios-table {
      width: 100%;
    }

    .totales-container {
      margin-bottom: 20px;
    }

    .totales-container mat-card {
      background: #e1f5fe;
    }

    .totales-container h3 {
      margin-top: 0;
      margin-bottom: 16px;
    }

    .totales-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
      color: #0277bd;
    }

    .ranking-container mat-card {
      background: #f3e5f5;
    }

    .ranking-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .ranking-item {
      display: flex;
      align-items: center;
      padding: 16px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      transition: transform 0.2s;
    }

    .ranking-item:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.15);
    }

    .ranking-position {
      margin-right: 16px;
    }

    .position-number {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 50%;
      font-weight: bold;
      font-size: 20px;
    }

    .ranking-info {
      flex: 1;
    }

    .ranking-nombre {
      font-weight: 500;
      font-size: 16px;
      margin-bottom: 8px;
    }

    .ranking-stats {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
    }

    .stat {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
      color: #666;
    }

    .stat mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .ranking-badge {
      margin-left: 16px;
    }

    .badge-percentage {
      display: inline-block;
      padding: 8px 16px;
      background: #4caf50;
      color: white;
      border-radius: 20px;
      font-weight: bold;
      font-size: 14px;
    }

    .sin-resultados {
      text-align: center;
      padding: 40px;
      color: #666;
    }
  `]
})
export class ReporteUsuariosComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reporteService = inject(ReporteApiService);

  filtroForm!: FormGroup;
  usuarios: UsuarioReporte[] = [];
  sortedUsuarios: UsuarioReporte[] = [];
  loading = false;
  error: string | null = null;

  displayedColumns = ['usuarioId', 'usuarioNombre', 'cantidadFacturas', 'totalFacturado', 'promedioFactura', 'porcentaje'];

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

    this.reporteService.reporteUsuarios(empresaId, fechaDesde, fechaHasta).subscribe({
      next: (data) => {
        this.usuarios = data;
        this.sortedUsuarios = [...data];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el reporte de usuarios';
        this.loading = false;
        console.error(err);
      }
    });
  }

  limpiarFiltros(): void {
    this.filtroForm.reset();
    this.usuarios = [];
    this.sortedUsuarios = [];
  }

  sortData(sort: Sort): void {
    const data = this.usuarios.slice();
    if (!sort.active || sort.direction === '') {
      this.sortedUsuarios = data;
      return;
    }

    this.sortedUsuarios = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case 'usuarioId':
          return this.compare(a.usuarioId, b.usuarioId, isAsc);
        case 'usuarioNombre':
          return this.compare(a.usuarioNombre, b.usuarioNombre, isAsc);
        case 'cantidadFacturas':
          return this.compare(a.cantidadFacturas, b.cantidadFacturas, isAsc);
        case 'totalFacturado':
          return this.compare(a.totalFacturado, b.totalFacturado, isAsc);
        default:
          return 0;
      }
    });
  }

  private compare(a: number | string, b: number | string, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  calcularPromedioFactura(usuario: UsuarioReporte): number {
    if (usuario.cantidadFacturas === 0) {
      return 0;
    }
    return usuario.totalFacturado / usuario.cantidadFacturas;
  }

  calcularPorcentaje(usuario: UsuarioReporte): number {
    const total = this.calcularTotalFacturado();
    if (total === 0) {
      return 0;
    }
    return (usuario.totalFacturado / total) * 100;
  }

  calcularTotalFacturas(): number {
    return this.usuarios.reduce((sum, usuario) => sum + usuario.cantidadFacturas, 0);
  }

  calcularTotalFacturado(): number {
    return this.usuarios.reduce((sum, usuario) => sum + usuario.totalFacturado, 0);
  }

  calcularPromedioUsuario(): number {
    if (this.usuarios.length === 0) {
      return 0;
    }
    return this.calcularTotalFacturado() / this.usuarios.length;
  }

  getTop5Usuarios(): UsuarioReporte[] {
    return [...this.usuarios]
      .sort((a, b) => b.totalFacturado - a.totalFacturado)
      .slice(0, 5);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
