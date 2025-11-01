import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router } from '@angular/router';

import { AuditApiService } from '../../core/api/audit-api.service';
import { AuditLog, AccionEnum, AuditLogFilter } from '../../models/audit.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { AuditoriaDetailComponent } from './auditoria-detail.component';

/**
 * Componente para listar y filtrar registros de auditoría
 */
@Component({
  selector: 'app-auditoria-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTooltipModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="auditoria-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>history</mat-icon>
            Historial de Auditoría
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Filtros -->
          <form [formGroup]="filterForm" class="filter-form">
            <div class="filter-row">
              <mat-form-field appearance="outline">
                <mat-label>Tipo de Entidad</mat-label>
                <mat-select formControlName="entidadTipo">
                  <mat-option [value]="null">Todos</mat-option>
                  <mat-option value="Empresa">Empresa</mat-option>
                  <mat-option value="Factura">Factura</mat-option>
                  <mat-option value="Cliente">Cliente</mat-option>
                  <mat-option value="Producto">Producto</mat-option>
                  <mat-option value="Timbrado">Timbrado</mat-option>
                  <mat-option value="Usuario">Usuario</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Acción</mat-label>
                <mat-select formControlName="accion">
                  <mat-option [value]="null">Todas</mat-option>
                  <mat-option [value]="AccionEnum.CREATE">Crear</mat-option>
                  <mat-option [value]="AccionEnum.UPDATE">Actualizar</mat-option>
                  <mat-option [value]="AccionEnum.DELETE">Eliminar</mat-option>
                  <mat-option [value]="AccionEnum.READ">Leer</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha Desde</mat-label>
                <input matInput [matDatepicker]="pickerDesde" formControlName="fechaDesde">
                <mat-datepicker-toggle matIconSuffix [for]="pickerDesde"></mat-datepicker-toggle>
                <mat-datepicker #pickerDesde></mat-datepicker>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha Hasta</mat-label>
                <input matInput [matDatepicker]="pickerHasta" formControlName="fechaHasta">
                <mat-datepicker-toggle matIconSuffix [for]="pickerHasta"></mat-datepicker-toggle>
                <mat-datepicker #pickerHasta></mat-datepicker>
              </mat-form-field>
            </div>

            <div class="filter-actions">
              <button mat-raised-button color="primary" (click)="aplicarFiltros()">
                <mat-icon>search</mat-icon>
                Buscar
              </button>
              <button mat-button (click)="limpiarFiltros()">
                <mat-icon>clear</mat-icon>
                Limpiar
              </button>
            </div>
          </form>

          <!-- Loading -->
          <app-loading-spinner *ngIf="loading"></app-loading-spinner>

          <!-- Error -->
          <app-error-message 
            *ngIf="error" 
            [error]="error"
            [showRetry]="true"
            (retry)="cargarDatos()">
          </app-error-message>

          <!-- Tabla de resultados -->
          <div *ngIf="!loading && !error" class="table-container">
            <table mat-table [dataSource]="auditLogs" class="audit-table">
              <!-- Fecha Column -->
              <ng-container matColumnDef="fechaHora">
                <th mat-header-cell *matHeaderCellDef>Fecha y Hora</th>
                <td mat-cell *matCellDef="let log">
                  {{ log.fechaHora | date:'dd/MM/yyyy HH:mm:ss' }}
                </td>
              </ng-container>

              <!-- Usuario Column -->
              <ng-container matColumnDef="usuario">
                <th mat-header-cell *matHeaderCellDef>Usuario</th>
                <td mat-cell *matCellDef="let log">
                  <div class="user-info">
                    <strong>{{ log.usuario.username }}</strong>
                    <small *ngIf="log.usuario.nombreCompleto">
                      {{ log.usuario.nombreCompleto }}
                    </small>
                  </div>
                </td>
              </ng-container>

              <!-- Acción Column -->
              <ng-container matColumnDef="accion">
                <th mat-header-cell *matHeaderCellDef>Acción</th>
                <td mat-cell *matCellDef="let log">
                  <mat-chip [class]="'chip-' + log.accion.toLowerCase()">
                    {{ getAccionLabel(log.accion) }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Entidad Column -->
              <ng-container matColumnDef="entidad">
                <th mat-header-cell *matHeaderCellDef>Entidad</th>
                <td mat-cell *matCellDef="let log">
                  <div class="entity-info">
                    <strong>{{ log.entidadTipo }}</strong>
                    <small>ID: {{ log.entidadId }}</small>
                  </div>
                </td>
              </ng-container>

              <!-- Empresa Column -->
              <ng-container matColumnDef="empresa">
                <th mat-header-cell *matHeaderCellDef>Empresa</th>
                <td mat-cell *matCellDef="let log">
                  {{ log.empresa?.razonSocial || '-' }}
                </td>
              </ng-container>

              <!-- IP Column -->
              <ng-container matColumnDef="ipAddress">
                <th mat-header-cell *matHeaderCellDef>IP</th>
                <td mat-cell *matCellDef="let log">
                  {{ log.ipAddress || '-' }}
                </td>
              </ng-container>

              <!-- Acciones Column -->
              <ng-container matColumnDef="acciones">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let log">
                  <button 
                    mat-icon-button 
                    color="primary"
                    (click)="verDetalle(log)"
                    matTooltip="Ver detalle de cambios"
                    *ngIf="log.accion === AccionEnum.UPDATE">
                    <mat-icon>visibility</mat-icon>
                  </button>
                  <button 
                    mat-icon-button 
                    color="accent"
                    (click)="verHistorialEntidad(log)"
                    matTooltip="Ver historial completo">
                    <mat-icon>history</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

              <!-- No data row -->
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell" [attr.colspan]="displayedColumns.length">
                  <div class="no-data">
                    <mat-icon>info</mat-icon>
                    <p>No se encontraron registros de auditoría</p>
                  </div>
                </td>
              </tr>
            </table>

            <!-- Paginador -->
            <mat-paginator
              [length]="totalElements"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 20, 50, 100]"
              [pageIndex]="pageIndex"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .auditoria-container {
      padding: 20px;
    }

    mat-card-header {
      margin-bottom: 20px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 24px;
    }

    .filter-form {
      margin-bottom: 20px;
    }

    .filter-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-bottom: 16px;
    }

    .filter-actions {
      display: flex;
      gap: 10px;
    }

    .table-container {
      overflow-x: auto;
      margin-top: 20px;
    }

    .audit-table {
      width: 100%;
    }

    .user-info, .entity-info {
      display: flex;
      flex-direction: column;
    }

    .user-info small, .entity-info small {
      color: rgba(0, 0, 0, 0.6);
      font-size: 12px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
    }

    .chip-create {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .chip-update {
      background-color: #2196f3 !important;
      color: white !important;
    }

    .chip-delete {
      background-color: #f44336 !important;
      color: white !important;
    }

    .chip-read {
      background-color: #9e9e9e !important;
      color: white !important;
    }

    .no-data {
      text-align: center;
      padding: 40px;
      color: rgba(0, 0, 0, 0.6);
    }

    .no-data mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 10px;
    }
  `]
})
export class AuditoriaListComponent implements OnInit {
  filterForm: FormGroup;
  auditLogs: AuditLog[] = [];
  displayedColumns: string[] = ['fechaHora', 'usuario', 'accion', 'entidad', 'empresa', 'ipAddress', 'acciones'];
  
  loading = false;
  error: string | null = null;
  
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;

  AccionEnum = AccionEnum;

  constructor(
    private fb: FormBuilder,
    private auditApiService: AuditApiService,
    private dialog: MatDialog,
    private router: Router
  ) {
    this.filterForm = this.fb.group({
      entidadTipo: [null],
      accion: [null],
      fechaDesde: [null],
      fechaHasta: [null]
    });
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading = true;
    this.error = null;

    const filtros: AuditLogFilter = {
      ...this.filterForm.value,
      page: this.pageIndex,
      size: this.pageSize
    };

    // Convertir fechas a ISO string si existen
    if (filtros.fechaDesde) {
      filtros.fechaDesde = new Date(filtros.fechaDesde).toISOString();
    }
    if (filtros.fechaHasta) {
      filtros.fechaHasta = new Date(filtros.fechaHasta).toISOString();
    }

    this.auditApiService.buscarConFiltros(filtros).subscribe({
      next: (response) => {
        this.auditLogs = response.content;
        this.totalElements = response.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los registros de auditoría';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }

  aplicarFiltros(): void {
    this.pageIndex = 0;
    this.cargarDatos();
  }

  limpiarFiltros(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.cargarDatos();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarDatos();
  }

  verDetalle(log: AuditLog): void {
    this.dialog.open(AuditoriaDetailComponent, {
      width: '800px',
      data: log
    });
  }

  verHistorialEntidad(log: AuditLog): void {
    // Navigate to entity history view
    this.router.navigate(['/auditoria/entidad', log.entidadTipo, log.entidadId]);
  }

  getAccionLabel(accion: AccionEnum): string {
    const labels: Record<AccionEnum, string> = {
      [AccionEnum.CREATE]: 'Crear',
      [AccionEnum.UPDATE]: 'Actualizar',
      [AccionEnum.DELETE]: 'Eliminar',
      [AccionEnum.READ]: 'Leer'
    };
    return labels[accion] || accion;
  }
}
