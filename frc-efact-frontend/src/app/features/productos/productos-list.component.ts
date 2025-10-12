import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { ProductoApiService } from '../../core/api/producto-api.service';
import { Producto } from '../../models/producto.model';
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
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="productos-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <h2>Gestión de Productos</h2>
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
                     (ngModelChange)="onSearchChange()"
                     placeholder="Código o descripción">
              <mat-icon matPrefix>search</mat-icon>
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
          
          <app-data-table
            *ngIf="!loading()"
            [columns]="columns"
            [data]="productosFiltrados()"
            [actions]="tableActions"
            (actionClick)="onActionClick($event)"
          />
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
  `]
})
export class ProductosListComponent implements OnInit {
  loading = signal(false);
  productos = signal<Producto[]>([]);
  productosFiltrados = signal<Producto[]>([]);
  
  searchTerm = '';
  ivaFilter: number | null = null;
  estadoFilter: boolean | null = true; // Por defecto mostrar solo activos

  columns: TableColumn[] = [
    { key: 'codigo', label: 'Código', sortable: true },
    { key: 'descripcion', label: 'Descripción', sortable: true },
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

  tableActions: TableAction[] = [
    { 
      icon: 'edit', 
      label: 'Editar', 
      color: 'primary',
      tooltip: 'Editar producto',
      handler: (row: Producto) => this.editProducto(row)
    },
    { 
      icon: 'delete', 
      label: 'Eliminar', 
      color: 'warn',
      tooltip: 'Eliminar producto',
      handler: (row: Producto) => this.deleteProducto(row)
    }
  ];

  constructor(
    private productoApi: ProductoApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.loading.set(true);
    // TODO: Obtener empresaId del state
    const empresaId = 1;
    
    this.productoApi.getByEmpresa(empresaId).subscribe({
      next: (productos) => {
        this.productos.set(productos);
        this.aplicarFiltros();
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar productos', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onSearchChange(): void {
    this.aplicarFiltros();
  }

  onFilterChange(): void {
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    let filtrados = [...this.productos()];

    // Filtro de búsqueda
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtrados = filtrados.filter(p => 
        p.codigo?.toLowerCase().includes(term) ||
        p.descripcion.toLowerCase().includes(term)
      );
    }

    // Filtro de IVA
    if (this.ivaFilter !== null) {
      filtrados = filtrados.filter(p => p.iva === this.ivaFilter);
    }

    // Filtro de estado
    if (this.estadoFilter !== null) {
      filtrados = filtrados.filter(p => p.activo === this.estadoFilter);
    }

    this.productosFiltrados.set(filtrados);
  }

  limpiarFiltros(): void {
    this.searchTerm = '';
    this.ivaFilter = null;
    this.estadoFilter = true;
    this.aplicarFiltros();
  }

  crearProducto(): void {
    const dialogRef = this.dialog.open(ProductoFormComponent, {
      width: '600px',
      data: { producto: null }
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
    const dialogRef = this.dialog.open(ProductoFormComponent, {
      width: '600px',
      data: { producto }
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
        this.productoApi.delete(producto.id).subscribe({
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
      
      // TODO: Obtener empresaId del state
      const empresaId = 1;
      
      this.productoApi.importarExcel(empresaId, file).subscribe({
        next: (result) => {
          this.snackBar.open(
            `Importación exitosa: ${result.imported} productos importados`,
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
