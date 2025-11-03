import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatDividerModule } from '@angular/material/divider';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { ClienteApiService, PageResponse } from '../../core/api/cliente-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { Cliente } from '../../models/cliente.model';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ClienteFormComponent } from './cliente-form.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-clientes-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatAutocompleteModule,
    MatChipsModule,
    MatTooltipModule,
    MatMenuModule,
    MatTableModule,
    MatSortModule,
    MatDividerModule,
    MatPaginatorModule,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="clientes-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <div class="header-title-section">
                <button mat-icon-button (click)="goBack()" matTooltip="Volver a empresas">
                  <mat-icon>arrow_back</mat-icon>
                </button>
                <div class="title-wrapper">
                  <h2>Gestión de Clientes</h2>
                  <p class="empresa-subtitle" *ngIf="empresaNombre">{{ empresaNombre }}</p>
                </div>
              </div>
              <div class="header-actions">
                <button mat-raised-button color="primary" (click)="crearCliente()">
                  <mat-icon>add</mat-icon>
                  Nuevo Cliente
                </button>
              </div>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Búsqueda rápida con autocompletado -->
          <div class="search-section">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Búsqueda rápida</mat-label>
              <input matInput
                     [(ngModel)]="searchTerm"
                     (ngModelChange)="onSearchChange()"
                     placeholder="Nombre, RUC o razón social">
              <mat-icon matPrefix>search</mat-icon>
              <button matSuffix
                      mat-icon-button
                      *ngIf="searchTerm"
                      (click)="limpiarBusqueda()">
                <mat-icon>clear</mat-icon>
              </button>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Tipo de Cliente</mat-label>
              <mat-select [(ngModel)]="tipoFilter" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option value="PERSONA_FISICA">Persona Física</mat-option>
                <mat-option value="PERSONA_JURIDICA">Persona Jurídica</mat-option>
                <mat-option value="NO_CONTRIBUYENTE">No Contribuyente</mat-option>
                <mat-option value="EXTRANJERO">Extranjero</mat-option>
                <mat-option value="GUBERNAMENTAL">Gubernamental</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Estado</mat-label>
              <mat-select [(ngModel)]="estadoFilter" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option [value]="true">Activos</mat-option>
                <mat-option [value]="false">Inactivos</mat-option>
              </mat-select>
            </mat-form-field>

            <button mat-stroked-button (click)="limpiarFiltros()">
              <mat-icon>clear</mat-icon>
              Limpiar
            </button>
          </div>


          <!-- Tabla de clientes -->
          <app-loading-spinner *ngIf="loading()" />

          <div *ngIf="!loading()" class="table-wrapper">
            <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)" class="clientes-table">
              <!-- Razón Social Column -->
              <ng-container matColumnDef="razonSocial">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Razón Social</th>
                <td mat-cell *matCellDef="let cliente">
                  <div class="razon-social-cell">
                    <div class="razon-social-main">{{ cliente.razonSocial || '-' }}</div>
                    <div class="nombre-subtitle" *ngIf="cliente.nombre">{{ cliente.nombre }}</div>
                  </div>
                </td>
              </ng-container>

              <!-- RUC Column -->
              <ng-container matColumnDef="ruc">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>RUC</th>
                <td mat-cell *matCellDef="let cliente">{{ cliente.ruc || '-' }}</td>
              </ng-container>

              <!-- Tipo Cliente Column -->
              <ng-container matColumnDef="tipoCliente">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Tipo Cliente</th>
                <td mat-cell *matCellDef="let cliente">
                  {{ getTipoClienteDescripcion(cliente.tipoClienteSifen) }}
                </td>
              </ng-container>

              <!-- Estado Column -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let cliente">
                  <mat-chip [class.active-chip]="cliente.activo" [class.inactive-chip]="!cliente.activo">
                    {{ cliente.activo ? 'Activo' : 'Inactivo' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let cliente">
                  <button
                    mat-icon-button
                    [matMenuTriggerFor]="actionsMenu"
                    matTooltip="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>

                  <mat-menu #actionsMenu="matMenu">
                    <button mat-menu-item (click)="editarCliente(cliente)">
                      <mat-icon>edit</mat-icon>
                      <span>Editar</span>
                    </button>
                    <mat-divider></mat-divider>
                    <button mat-menu-item (click)="eliminarCliente(cliente)" class="delete-option">
                      <mat-icon>delete</mat-icon>
                      <span>Eliminar</span>
                    </button>
                  </mat-menu>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

              <!-- No data row -->
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell" [attr.colspan]="displayedColumns.length">
                  <div class="no-data">
                    No hay clientes para mostrar
                  </div>
                </td>
              </tr>
            </table>

            <mat-paginator
              [length]="totalElements"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 20, 50, 100]"
              [pageIndex]="currentPage"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .clientes-container {
      padding: 20px;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      gap: 16px;
    }

    .header-title-section {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
    }

    .header-title-section button {
      margin-right: 8px;
    }

    .title-wrapper {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .title-wrapper h2 {
      margin: 0;
      flex: 1;
    }

    .empresa-subtitle {
      margin: 0;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.6);
      font-weight: normal;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }

    .search-section {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      flex-wrap: wrap;
      align-items: center;
    }

    .search-field {
      flex: 1;
      min-width: 300px;
    }

    mat-form-field {
      min-width: 150px;
    }

    .quick-results {
      margin-bottom: 16px;
      padding: 12px;
      background-color: #e3f2fd;
      border-radius: 4px;
    }

    .result-hint {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #1976d2;
    }

    mat-card-header {
      margin-bottom: 24px;
    }

    h2 {
      margin: 0;
    }

    .table-wrapper {
      overflow-x: auto;
    }

    .clientes-table {
      width: 100%;
      background: white;
    }

    .clientes-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .clientes-table td,
    .clientes-table th {
      padding: 12px 16px;
    }

    .razon-social-cell {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .razon-social-main {
      font-weight: 500;
      font-size: 14px;
    }

    .nombre-subtitle {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.6);
      font-weight: normal;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    .inactive-chip {
      background-color: #f44336 !important;
      color: white;
    }

    .no-data {
      padding: 40px;
      text-align: center;
      color: rgba(0, 0, 0, 0.54);
    }

    .delete-option {
      color: #f44336;
    }

    .mat-mdc-menu-item mat-icon {
      margin-right: 8px;
    }
  `]
})
export class ClientesListComponent implements OnInit {
  loading = signal(false);
  dataSource = new MatTableDataSource<Cliente>([]);

  empresaId: number | null = null;
  empresaNombre: string = '';

  searchTerm = '';
  tipoFilter: string | null = null;
  estadoFilter: boolean | null = true; // Por defecto mostrar solo activos

  // Paginación
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  sortBy = 'razonSocial';
  sortDir: 'ASC' | 'DESC' = 'ASC';

  displayedColumns: string[] = ['razonSocial', 'ruc', 'tipoCliente', 'activo', 'actions'];

  private searchSubject = new Subject<string>();

  constructor(
    private clienteApi: ClienteApiService,
    private empresaApi: EmpresaApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) {
    // Configurar debounce para búsqueda
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => {
      this.currentPage = 0; // Resetear a primera página al buscar
      this.cargarClientes();
    });
  }

  ngOnInit(): void {
    // Obtener empresaId de query params si está disponible
    this.route.queryParams.subscribe(params => {
      const empresaIdFromRoute = params['empresaId'] ? Number(params['empresaId']) : null;
      if (empresaIdFromRoute) {
        this.empresaId = empresaIdFromRoute;
        this.cargarInformacionEmpresa(empresaIdFromRoute);
        this.cargarClientes(empresaIdFromRoute);
      } else {
        this.cargarClientes();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/empresas']);
  }

  cargarInformacionEmpresa(empresaId: number): void {
    this.empresaApi.getById(empresaId).subscribe({
      next: (empresa) => {
        this.empresaNombre = empresa.razonSocial;
      },
      error: (error) => {
        // Error silencioso al cargar información de empresa
      }
    });
  }

  cargarClientes(empresaId?: number): void {
    this.loading.set(true);
    const idEmpresa = empresaId || this.empresaId || 1;

    const filtros = {
      q: this.searchTerm && this.searchTerm.trim() ? this.searchTerm.trim() : undefined,
      tipoClienteSifen: this.tipoFilter || undefined,
      activo: this.estadoFilter !== null ? this.estadoFilter : undefined,
      page: this.currentPage,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDir: this.sortDir
    };

    this.clienteApi.buscarConFiltros(idEmpresa, filtros).subscribe({
      next: (page: PageResponse<Cliente>) => {
        this.dataSource.data = page.content;
        this.totalElements = page.totalElements;
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar clientes', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onFilterChange(): void {
    this.currentPage = 0; // Resetear a primera página al cambiar filtros
    this.cargarClientes();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarClientes();
  }

  onSortChange(sort: Sort): void {
    this.sortBy = sort.active;
    this.sortDir = sort.direction === 'desc' ? 'DESC' : 'ASC';
    this.currentPage = 0; // Resetear a primera página al ordenar
    this.cargarClientes();
  }

  getTipoClienteDescripcion(tipoClienteSifen?: string): string {
    if (!tipoClienteSifen) return '-';

    const tipos: Record<string, string> = {
      'PERSONA_FISICA': 'Persona Física',
      'PERSONA_JURIDICA': 'Persona Jurídica',
      'NO_CONTRIBUYENTE': 'No Contribuyente',
      'EXTRANJERO': 'Extranjero',
      'GUBERNAMENTAL': 'Gubernamental'
    };
    return tipos[tipoClienteSifen] || tipoClienteSifen;
  }

  limpiarBusqueda(): void {
    this.searchTerm = '';
    this.currentPage = 0;
    this.cargarClientes();
  }

  limpiarFiltros(): void {
    this.searchTerm = '';
    this.tipoFilter = null;
    this.estadoFilter = true;
    this.currentPage = 0;
    this.cargarClientes();
  }

  crearCliente(): void {
    const empresaId = this.route.snapshot.queryParams['empresaId']
      ? Number(this.route.snapshot.queryParams['empresaId'])
      : null;

    const dialogRef = this.dialog.open(ClienteFormComponent, {
      width: '700px',
      data: { cliente: null, empresaId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Recargar clientes con el empresaId actual de los query params
        const empresaId = this.route.snapshot.queryParams['empresaId']
          ? Number(this.route.snapshot.queryParams['empresaId'])
          : undefined;
        this.cargarClientes(empresaId);
      }
    });
  }


  editarCliente(cliente: Cliente): void {
    const empresaId = this.route.snapshot.queryParams['empresaId']
      ? Number(this.route.snapshot.queryParams['empresaId'])
      : cliente.empresaId || null;

    const dialogRef = this.dialog.open(ClienteFormComponent, {
      width: '700px',
      data: { cliente, empresaId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Recargar clientes con el empresaId actual de los query params
        const empresaId = this.route.snapshot.queryParams['empresaId']
          ? Number(this.route.snapshot.queryParams['empresaId'])
          : undefined;
        this.cargarClientes(empresaId);
      }
    });
  }

  eliminarCliente(cliente: Cliente): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de eliminar el cliente "${cliente.nombre}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        // Obtener empresaId de query params o usar por defecto
        const empresaId = this.route.snapshot.queryParams['empresaId']
          ? Number(this.route.snapshot.queryParams['empresaId'])
          : 1;

        this.clienteApi.delete(empresaId, cliente.id).subscribe({
          next: () => {
            this.snackBar.open('Cliente eliminado correctamente', 'Cerrar', { duration: 3000 });
            // Recargar clientes con el mismo empresaId
            const empresaIdReload = this.route.snapshot.queryParams['empresaId']
              ? Number(this.route.snapshot.queryParams['empresaId'])
              : undefined;
            this.cargarClientes(empresaIdReload);
          },
          error: () => {
            this.snackBar.open('Error al eliminar cliente', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }
}
