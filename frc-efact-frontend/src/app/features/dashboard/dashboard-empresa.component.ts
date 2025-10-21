import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { Empresa } from '../../models/empresa.model';
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
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatChipsModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <h1 class="dashboard-title">Mis Empresas</h1>
        <p class="dashboard-subtitle">Gestiona las empresas que administras</p>
      </div>

      <!-- Filtro de búsqueda -->
      <mat-card class="filter-card">
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Buscar por nombre o RUC</mat-label>
          <input matInput [formControl]="searchControl" placeholder="Ingresa nombre o RUC de la empresa">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      </mat-card>

      <app-loading-spinner *ngIf="loading"></app-loading-spinner>
      <app-error-message *ngIf="error" [message]="error"></app-error-message>

      <!-- Tabla de empresas -->
      <mat-card *ngIf="!loading && !error" class="empresas-card">
        <mat-card-content>
          <div class="table-container">
            <table mat-table [dataSource]="empresas" class="empresas-table">
              <!-- Columna RUC -->
              <ng-container matColumnDef="ruc">
                <th mat-header-cell *matHeaderCellDef>RUC</th>
                <td mat-cell *matCellDef="let empresa">
                  <span class="ruc-text">{{ empresa.ruc }}</span>
                </td>
              </ng-container>

              <!-- Columna Razón Social -->
              <ng-container matColumnDef="razonSocial">
                <th mat-header-cell *matHeaderCellDef>Razón Social</th>
                <td mat-cell *matCellDef="let empresa">
                  <div class="empresa-info">
                    <span class="razon-social">{{ empresa.razonSocial }}</span>
                    <span class="nombre-comercial" *ngIf="empresa.nombreFantasia">
                      {{ empresa.nombreFantasia }}
                    </span>
                  </div>
                </td>
              </ng-container>

              <!-- Columna Estado -->
              <ng-container matColumnDef="estado">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let empresa">
                  <mat-chip [class.active-chip]="empresa.activo" [class.inactive-chip]="!empresa.activo">
                    {{ empresa.activo ? 'Activa' : 'Inactiva' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Columna Acciones -->
              <ng-container matColumnDef="acciones">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let empresa">
                  <div class="actions-container">
                    <button
                      mat-icon-button
                      color="primary"
                      [matTooltip]="'Gestionar ' + empresa.razonSocial"
                      (click)="goToEmpresaManagement(empresa.id)">
                      <mat-icon>settings</mat-icon>
                    </button>
                    <button
                      mat-icon-button
                      color="accent"
                      [matTooltip]="'Ver detalles de ' + empresa.razonSocial"
                      (click)="goToEmpresaDetails(empresa.id)">
                      <mat-icon>visibility</mat-icon>
                    </button>
                  </div>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>

            <!-- Mensaje cuando no hay empresas -->
            <div *ngIf="empresas.length === 0" class="no-data">
              <mat-icon>business</mat-icon>
              <p>No se encontraron empresas</p>
            </div>
          </div>

          <!-- Paginación -->
          <mat-paginator
            *ngIf="empresas.length > 0"
            [length]="totalEmpresas"
            [pageSize]="pageSize"
            [pageSizeOptions]="[5, 10, 25, 50]"
            [pageIndex]="currentPage"
            (page)="onPageChange($event)"
            showFirstLastButtons>
          </mat-paginator>
        </mat-card-content>
      </mat-card>
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
      margin-bottom: 24px;
    }

    .search-field {
      width: 100%;
      max-width: 400px;
    }

    .empresas-card {
      margin-bottom: 24px;
    }

    .table-container {
      overflow-x: auto;
    }

    .empresas-table {
      width: 100%;
    }

    .empresas-table th {
      font-weight: 600;
      color: rgba(0, 0, 0, 0.87);
      background-color: #f5f5f5;
    }

    .empresas-table td {
      padding: 16px 8px;
    }

    .ruc-text {
      font-family: 'Courier New', monospace;
      font-weight: 500;
      color: #1976d2;
    }

    .empresa-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .razon-social {
      font-weight: 500;
      color: rgba(0, 0, 0, 0.87);
    }

    .nombre-comercial {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.6);
      font-style: italic;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .inactive-chip {
      background-color: #f44336 !important;
      color: white !important;
    }

    .actions-container {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .no-data {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px 24px;
      color: rgba(0, 0, 0, 0.6);
    }

    .no-data mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      color: rgba(0, 0, 0, 0.4);
    }

    .no-data p {
      margin: 0;
      font-size: 16px;
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 16px;
      }

      .dashboard-title {
        font-size: 24px;
      }

      .empresas-table td {
        padding: 12px 4px;
      }

      .actions-container {
        flex-direction: column;
        gap: 4px;
      }
    }
  `]
})
export class DashboardEmpresaComponent implements OnInit {
  empresas: Empresa[] = [];
  loading = false;
  error?: string;

  // Paginación
  totalEmpresas = 0;
  currentPage = 0;
  pageSize = 10;

  // Filtro de búsqueda
  searchControl = new FormControl('');

  // Columnas de la tabla
  displayedColumns: string[] = ['ruc', 'razonSocial', 'estado', 'acciones'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private empresaApi: EmpresaApiService
  ) {}

  ngOnInit(): void {
    // Verificar si hay parámetro de ruta (para rutas específicas como /dashboard/empresa/123)
    this.route.params.subscribe(params => {
      const routeEmpresaId = params['id'];
      if (routeEmpresaId) {
        // Si hay parámetro de ruta, redirigir a gestión de empresa específica
        this.router.navigate(['/empresas', routeEmpresaId]);
      } else {
        // Si no hay parámetro de ruta, cargar lista de empresas
        this.setupSearchFilter();
        this.loadEmpresas();
      }
    });
  }

  private setupSearchFilter(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.currentPage = 0;
        this.loadEmpresas();
      });
  }

  loadEmpresas(): void {
    this.loading = true;
    this.error = undefined;

    const searchTerm = this.searchControl.value || '';

    this.empresaApi.getMisEmpresas().subscribe({
      next: (empresas) => {
        // Filtrar empresas por término de búsqueda
        let filteredEmpresas = empresas;
        if (searchTerm.trim()) {
          const term = searchTerm.toLowerCase().trim();
          filteredEmpresas = empresas.filter(empresa =>
            empresa.razonSocial.toLowerCase().includes(term) ||
            empresa.nombreFantasia?.toLowerCase().includes(term) ||
            empresa.ruc.includes(term)
          );
        }

        this.totalEmpresas = filteredEmpresas.length;

        // Aplicar paginación
        const startIndex = this.currentPage * this.pageSize;
        const endIndex = startIndex + this.pageSize;
        this.empresas = filteredEmpresas.slice(startIndex, endIndex);

        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar las empresas';
        this.loading = false;
        console.error('Error loading empresas:', err);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEmpresas();
  }

  goToEmpresaManagement(empresaId: number): void {
    this.router.navigate(['/empresas', empresaId]);
  }

  goToEmpresaDetails(empresaId: number): void {
    this.router.navigate(['/empresas', empresaId, 'edit']);
  }
}
