import { Component, OnInit, OnDestroy, signal, ViewChild, AfterViewInit } from '@angular/core';
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

          <div *ngIf="!loading()" class="table-wrapper">
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
  `]
})
export class ProductosListComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild(MatSort) sort!: MatSort;

  loading = signal(false);
  productos = signal<Producto[]>([]);
  productosFiltrados = signal<Producto[]>([]);
  dataSource = new MatTableDataSource<Producto>([]);

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
    // Obtener empresaId de la ruta
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
    this.empresaApi.getById(empresaId).subscribe({
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
    ).subscribe({
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
      width: '600px',
      data: { producto: null, empresaId: this.empresaId }
    });

    dialogRef.afterClosed().subscribe(result => {
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
      width: '600px',
      data: { producto, empresaId: this.empresaId }
    });

    dialogRef.afterClosed().subscribe(result => {
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

        this.productoApi.delete(producto.id, this.empresaId).subscribe({
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

      this.productoApi.importarExcel(this.empresaId, file).subscribe({
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
