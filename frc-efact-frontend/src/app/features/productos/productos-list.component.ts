import { Component, OnInit, OnDestroy, signal, ViewChild, AfterViewInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { BreakpointObserver } from '@angular/cdk/layout';
import { ProductoApiService } from '../../core/api/producto-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { Producto, TIPO_TRANSACCION_DESCRIPCIONES, TipoTransaccionProducto } from '../../models/producto.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ProductoFormComponent } from './producto-form.component';

@Component({
  selector: 'app-productos-list',
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
    MatChipsModule,
    MatTooltipModule,
    MatMenuModule,
    MatDividerModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="productos-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <div class="header-title-section">
                <button mat-icon-button (click)="goBack()" matTooltip="Volver a empresas">
                  <mat-icon>arrow_back</mat-icon>
                </button>
                <div class="title-wrapper">
                  <h2>Gestión de Productos</h2>
                  <p class="empresa-subtitle" *ngIf="empresaNombre">{{ empresaNombre }}</p>
                </div>
              </div>
              <div class="header-actions">
                <button mat-raised-button color="accent" (click)="importarExcel()">
                  <mat-icon>upload_file</mat-icon>
                  Importar Excel
                </button>
                <button mat-raised-button color="primary" (click)="crearProducto()">
                  <mat-icon>add</mat-icon>
                  Nuevo Producto
                </button>
              </div>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Filtros -->
          <div class="filters-section">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Buscar producto</mat-label>
              <input matInput
                     [(ngModel)]="searchTerm"
                     (input)="onSearchInput($event)"
                     (ngModelChange)="onSearchChange()"
                     placeholder="Código o nombre"
                     [style.text-transform]="'uppercase'">
              <mat-icon matPrefix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Tipo de Transacción</mat-label>
              <mat-select [(ngModel)]="tipoTransaccionFilter" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option *ngFor="let tipo of tiposTransaccion" [value]="tipo.value">
                  {{ tipo.label }}
                </mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Filtrar por IVA</mat-label>
              <mat-select [(ngModel)]="ivaFilter" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option [value]="0">0%</mat-option>
                <mat-option [value]="5">5%</mat-option>
                <mat-option [value]="10">10%</mat-option>
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

          <!-- Tabla de productos -->
          <app-loading-spinner *ngIf="loading()" />

          <div class="list-desktop" *ngIf="!loading() && !isMobile()">
          <div class="table-wrapper">
            <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)" class="productos-table">
              <!-- Código Column -->
              <ng-container matColumnDef="codigo">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Código</th>
                <td mat-cell *matCellDef="let producto">{{ producto.codigo || '-' }}</td>
              </ng-container>

              <!-- Descripción Column -->
              <ng-container matColumnDef="descripcion">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Descripción</th>
                <td mat-cell *matCellDef="let producto">{{ producto.descripcion }}</td>
              </ng-container>

              <!-- Tipo Transacción Column -->
              <ng-container matColumnDef="tipoTransaccion">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Tipo Transacción</th>
                <td mat-cell *matCellDef="let producto">
                  {{ getTipoTransaccionDescripcion(producto.tipoTransaccion) }}
                </td>
              </ng-container>

              <!-- Unidad Medida Column -->
              <ng-container matColumnDef="unidadMedida">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Unidad</th>
                <td mat-cell *matCellDef="let producto">{{ producto.unidadMedida }}</td>
              </ng-container>

              <!-- Precio Column -->
              <ng-container matColumnDef="precio">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Precio</th>
                <td mat-cell *matCellDef="let producto">₲ {{ producto.precio.toLocaleString('es-PY') }}</td>
              </ng-container>

              <!-- IVA Column -->
              <ng-container matColumnDef="iva">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>IVA</th>
                <td mat-cell *matCellDef="let producto">{{ producto.iva }}%</td>
              </ng-container>

              <!-- Balanza Column -->
              <ng-container matColumnDef="balanza">
                <th mat-header-cell *matHeaderCellDef>Balanza</th>
                <td mat-cell *matCellDef="let producto">{{ producto.balanza ? 'Sí' : 'No' }}</td>
              </ng-container>

              <!-- Estado Column -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let producto">
                  <mat-chip [class.active-chip]="producto.activo" [class.inactive-chip]="!producto.activo">
                    {{ producto.activo ? 'Activo' : 'Inactivo' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let producto">
                  <button
                    mat-icon-button
                    [matMenuTriggerFor]="actionsMenu"
                    matTooltip="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>

                  <mat-menu #actionsMenu="matMenu">
                    <button mat-menu-item (click)="editarProducto(producto)">
                      <mat-icon>edit</mat-icon>
                      <span>Editar</span>
                    </button>
                    <mat-divider></mat-divider>
                    <button mat-menu-item (click)="eliminarProducto(producto)" class="delete-option">
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
                    No hay productos para mostrar
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
          </div>

          <div class="list-mobile" *ngIf="!loading() && isMobile()">
            <div class="mobile-cards" *ngIf="dataSource.data.length">
              <mat-card class="list-card" *ngFor="let item of dataSource.data">
                <mat-card-header class="list-card-header">
                  <mat-card-title class="list-card-title">
                    <span class="list-card-num">{{ item.descripcion || '—' }}</span>
                    <span class="list-card-date">{{ item.codigo || '—' }}</span>
                  </mat-card-title>
                  <button class="list-card-menu-trigger" mat-icon-button color="primary"
                    [matMenuTriggerFor]="cardActionMenu" (click)="setMenuContext(item)"
                    matTooltip="Acciones" aria-label="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>
                </mat-card-header>
                <mat-card-content class="list-card-content productos-card-content">
                  <div class="list-card-field">
                    <span class="list-card-label">Tipo</span>
                    <span class="list-card-value">{{ getTipoTransaccionDescripcion(item.tipoTransaccion) }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Precio</span>
                    <span class="list-card-value">₲ {{ (item.precio || 0).toLocaleString('es-PY') }}</span>
                  </div>
                  <div class="list-card-field list-card-field-full">
                    <span class="list-card-label">Estado</span>
                    <span class="list-card-value">{{ item.activo ? 'Activo' : 'Inactivo' }}</span>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
            <div class="mobile-empty" *ngIf="!dataSource.data.length">No hay productos para mostrar.</div>
            <mat-paginator
              *ngIf="dataSource.data.length"
              [length]="totalElements"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 20, 50, 100]"
              [pageIndex]="currentPage"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
            <mat-menu #cardActionMenu="matMenu" class="card-action-menu">
              <button mat-menu-item type="button" class="list-card-menu-item" (click)="menuRow && editarProducto(menuRow)">
                <mat-icon>edit</mat-icon>
                <span>Editar</span>
              </button>
              <mat-divider></mat-divider>
              <button mat-menu-item type="button" class="list-card-menu-item delete-option" (click)="menuRow && eliminarProducto(menuRow)">
                <mat-icon>delete</mat-icon>
                <span>Eliminar</span>
              </button>
            </mat-menu>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Input oculto para importación de Excel -->
      <input #fileInput
             type="file"
             accept=".xlsx,.xls"
             style="display: none"
             (change)="onFileSelected($event)">
    </div>
  `,
  styles: [`
    .productos-container {
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

    @media (max-width: 768px) {
      .header-content {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;
      }
      .header-title-section { flex: none; }
      .header-actions {
        flex-wrap: wrap;
        justify-content: flex-start;
      }
      .header-actions button { flex: 1; min-width: 140px; }
    }

    .filters-section {
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

    mat-card-header {
      margin-bottom: 24px;
    }

    h2 {
      margin: 0;
    }

    .table-wrapper {
      overflow-x: auto;
    }

    .productos-table {
      width: 100%;
      background: white;
    }

    mat-paginator {
      border-top: 1px solid rgba(0, 0, 0, 0.12);
    }

    .productos-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .productos-table td,
    .productos-table th {
      padding: 12px 16px;
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
    .list-desktop { width: 100%; }
    .list-mobile { width: 100%; }
    .mobile-cards { display: flex; flex-direction: column; gap: 10px; margin-bottom: 12px; }
    .list-card { margin: 0; }
    .list-card-header { display: flex; align-items: flex-start; justify-content: space-between; padding: 10px 14px 4px; margin-bottom: 0; gap: 8px; }
    .list-card-title { display: flex; flex-direction: column; gap: 1px; margin: 0; font-size: 1rem; min-width: 0; flex: 1; }
    .list-card-num { font-weight: 600; color: #2c3e50; font-size: 1rem; line-height: 1.3; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; word-break: break-word; }
    .list-card-date { font-size: 0.75rem; color: rgba(0,0,0,0.55); font-weight: 500; }
    :host-context(body.dark-theme) .list-card-num { color: #e0e0e0; }
    :host-context(body.dark-theme) .list-card-date { color: rgba(255,255,255,0.55); }
    .list-card-menu-trigger { flex-shrink: 0; margin: -4px -4px 0 0; }
    .list-card-menu-trigger .mat-icon { font-size: 1.5rem; width: 24px; height: 24px; }
    .list-card-content { padding: 0 !important; }
    .list-card-content.productos-card-content { display: grid; grid-template-columns: 1fr 1fr; gap: 6px 14px; padding: 4px 14px 12px !important; }
    .list-card-content.productos-card-content .list-card-field { display: flex; flex-direction: column; gap: 1px; }
    .list-card-content.productos-card-content .list-card-field-full { grid-column: 1 / -1; }
    .list-card-content.productos-card-content .list-card-label { font-size: 0.6875rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.04em; color: rgba(0,0,0,0.55); }
    .list-card-content.productos-card-content .list-card-value { font-size: 0.875rem; color: #2c3e50; word-break: break-word; line-height: 1.3; }
    :host-context(body.dark-theme) .list-card-content.productos-card-content .list-card-label { color: rgba(255,255,255,0.55); }
    :host-context(body.dark-theme) .list-card-content.productos-card-content .list-card-value { color: #e0e0e0; }
    .list-card-field { display: flex; flex-direction: column; gap: 2px; }
    .list-card-label { font-size: 0.75rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; color: rgba(0,0,0,0.6); }
    .list-card-value { font-size: 0.9375rem; color: #2c3e50; word-break: break-word; }
    :host-context(body.dark-theme) .list-card-label { color: rgba(255,255,255,0.6); }
    :host-context(body.dark-theme) .list-card-value { color: #e0e0e0; }
    .mobile-empty { padding: 24px 16px; text-align: center; color: rgba(0,0,0,0.6); }
    :host-context(body.dark-theme) .mobile-empty { color: rgba(255,255,255,0.6); }
    .list-mobile mat-paginator { border-top: 1px solid rgba(0,0,0,0.12); }
    :host-context(body.dark-theme) .list-mobile mat-paginator { border-top-color: rgba(255,255,255,0.12); }
  `]
})
export class ProductosListComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild(MatSort) sort!: MatSort;

  loading = signal(false);
  productos = signal<Producto[]>([]);
  productosFiltrados = signal<Producto[]>([]);
  dataSource = new MatTableDataSource<Producto>([]);
  isMobile = signal(false);
  menuRow: Producto | null = null;
  private breakpointObserver = inject(BreakpointObserver);

  empresaId: number | null = null;
  empresaNombre: string = '';

  searchTerm = '';
  ivaFilter: number | null = null;
  estadoFilter: boolean | null = true; // Por defecto mostrar solo activos
  tipoTransaccionFilter: TipoTransaccionProducto | null = null;

  // Paginación
  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  sortBy = 'descripcion';
  sortDir: 'asc' | 'desc' = 'asc';

  displayedColumns: string[] = ['codigo', 'descripcion', 'tipoTransaccion', 'unidadMedida', 'precio', 'iva', 'balanza', 'activo', 'actions'];

  tiposTransaccion = Object.keys(TipoTransaccionProducto).map(key => ({
    value: TipoTransaccionProducto[key as keyof typeof TipoTransaccionProducto],
    label: TIPO_TRANSACCION_DESCRIPCIONES[TipoTransaccionProducto[key as keyof typeof TipoTransaccionProducto]]
  }));

  private destroy$ = new Subject<void>();

  // Mantener estas referencias para compatibilidad si se usan en otros lugares
  columns: TableColumn[] = [
    { key: 'codigo', label: 'Código', sortable: true },
    { key: 'descripcion', label: 'Descripción', sortable: true },
    {
      key: 'tipoTransaccion',
      label: 'Tipo Transacción',
      sortable: true,
      format: (value: string) => TIPO_TRANSACCION_DESCRIPCIONES[value as keyof typeof TIPO_TRANSACCION_DESCRIPCIONES] || value
    },
    {
      key: 'unidadMedida',
      label: 'Unidad',
      sortable: true
    },
    {
      key: 'precio',
      label: 'Precio',
      sortable: true,
      format: (value: number) => `₲ ${value.toLocaleString('es-PY')}`
    },
    {
      key: 'iva',
      label: 'IVA',
      sortable: true,
      format: (value: number) => `${value}%`
    },
    {
      key: 'balanza',
      label: 'Balanza',
      format: (value: boolean) => value ? 'Sí' : 'No'
    },
    {
      key: 'activo',
      label: 'Estado',
      format: (value: boolean) => value ? 'Activo' : 'Inactivo'
    }
  ];

  // Mantener para compatibilidad
  tableActions: TableAction[] = [];

  constructor(
    private productoApi: ProductoApiService,
    private empresaApi: EmpresaApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.breakpointObserver.observe(['(max-width: 768px)']).pipe(takeUntil(this.destroy$)).subscribe(s => this.isMobile.set(s.matches));
    this.route.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = +params['id'];
        console.log('ID de empresa desde ruta:', id);
        if (id && !isNaN(id)) {
          this.empresaId = id;
          this.cargarInformacionEmpresa(id);
          this.cargarProductos();
        } else {
          console.error('ID de empresa no válido:', params['id']);
          this.snackBar.open('ID de empresa no válido', 'Cerrar', { duration: 3000 });
          this.goBack();
        }
      });
  }

  ngAfterViewInit(): void {
    if (this.sort) {
      this.dataSource.sort = this.sort;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarInformacionEmpresa(empresaId: number): void {
    this.empresaApi.getById(empresaId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (empresa) => {
        this.empresaNombre = empresa.razonSocial;
      },
      error: (error) => {
        console.error('Error al cargar información de la empresa:', error);
      }
    });
  }

  cargarProductos(): void {
    if (!this.empresaId) {
      return;
    }

    this.loading.set(true);

    // Enviar filtros al backend con paginación
    this.productoApi.getByEmpresa(
      this.empresaId,
      this.currentPage, // page
      this.pageSize, // size
      this.sortBy, // sortBy
      this.sortDir, // sortDir
      this.estadoFilter,
      this.searchTerm || null,
      this.tipoTransaccionFilter || null,
      this.ivaFilter
    ).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        console.log('Productos cargados desde backend con filtros y paginación:', response);
        const productos = response.content || [];
        this.productos.set(productos);
        this.productosFiltrados.set(productos);
        this.dataSource.data = productos;
        this.totalElements = response.totalElements || 0;
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error al cargar productos:', error);
        const errorMessage = error.error?.message || error.message || 'Error desconocido al cargar productos';
        this.snackBar.open(`Error: ${errorMessage}`, 'Cerrar', { duration: 5000 });
        this.productos.set([]);
        this.productosFiltrados.set([]);
        this.dataSource.data = [];
        this.totalElements = 0;
        this.loading.set(false);
      }
    });
  }

  setMenuContext(item: Producto): void {
    this.menuRow = item;
  }

  goBack(): void {
    this.router.navigate(['/empresas']);
  }

  getTipoTransaccionDescripcion(tipo: string): string {
    return TIPO_TRANSACCION_DESCRIPCIONES[tipo as TipoTransaccionProducto] || tipo;
  }

  onSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase();
    this.searchTerm = input.value;
    this.onSearchChange();
  }

  onSearchChange(): void {
    // Resetear a primera página al buscar
    this.currentPage = 0;
    // Recargar productos con filtros desde backend
    this.cargarProductos();
  }

  onFilterChange(): void {
    // Resetear a primera página al cambiar filtros
    this.currentPage = 0;
    // Recargar productos con filtros desde backend
    this.cargarProductos();
  }

  limpiarFiltros(): void {
    this.searchTerm = '';
    this.tipoTransaccionFilter = null;
    this.ivaFilter = null;
    this.estadoFilter = true;
    // Resetear a primera página al limpiar filtros
    this.currentPage = 0;
    // Recargar productos sin filtros desde backend
    this.cargarProductos();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarProductos();
  }

  onSortChange(sort: Sort): void {
    if (sort.active && sort.direction) {
      this.sortBy = sort.active;
      this.sortDir = sort.direction === 'asc' ? 'asc' : 'desc';
      // Resetear a primera página al ordenar
      this.currentPage = 0;
      this.cargarProductos();
    }
  }

  crearProducto(): void {
    if (!this.empresaId) {
      this.snackBar.open('No se puede crear producto sin empresa seleccionada', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(ProductoFormComponent, {
      width: '95vw',
      maxWidth: '600px',
      data: { producto: null, empresaId: this.empresaId }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result) {
        this.cargarProductos();
      }
    });
  }

  onActionClick(event: { action: string; row: Producto }): void {
    if (event.action === 'Editar') {
      this.editarProducto(event.row);
    } else if (event.action === 'Eliminar') {
      this.eliminarProducto(event.row);
    }
  }

  editProducto(producto: Producto): void {
    this.editarProducto(producto);
  }

  deleteProducto(producto: Producto): void {
    this.eliminarProducto(producto);
  }

  editarProducto(producto: Producto): void {
    if (!this.empresaId) {
      this.snackBar.open('Error: empresa no identificada', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(ProductoFormComponent, {
      width: '95vw',
      maxWidth: '600px',
      data: { producto, empresaId: this.empresaId }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result) {
        this.cargarProductos();
      }
    });
  }

  eliminarProducto(producto: Producto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de eliminar el producto "${producto.descripcion}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        if (!this.empresaId) {
          this.snackBar.open('Error: empresa no identificada', 'Cerrar', { duration: 3000 });
          return;
        }

        this.productoApi.delete(producto.id, this.empresaId).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Producto eliminado correctamente', 'Cerrar', { duration: 3000 });
            this.cargarProductos();
          },
          error: () => {
            this.snackBar.open('Error al eliminar producto', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }

  importarExcel(): void {
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    fileInput?.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.loading.set(true);

      if (!this.empresaId) {
        this.snackBar.open('Error: empresa no identificada', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
        input.value = '';
        return;
      }

      this.productoApi.importarExcel(this.empresaId, file).pipe(takeUntil(this.destroy$)).subscribe({
        next: (result) => {
          this.snackBar.open(
            `Importación exitosa: ${result.cantidadImportada} productos importados`,
            'Cerrar',
            { duration: 5000 }
          );
          this.cargarProductos();
          input.value = ''; // Limpiar input
        },
        error: (error) => {
          this.snackBar.open(
            error.error?.message || 'Error al importar productos',
            'Cerrar',
            { duration: 5000 }
          );
          this.loading.set(false);
          input.value = ''; // Limpiar input
        }
      });
    }
  }
}
