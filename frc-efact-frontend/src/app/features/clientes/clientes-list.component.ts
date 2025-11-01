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
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { Cliente } from '../../models/cliente.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ClienteFormComponent } from './cliente-form.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

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
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="clientes-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <h2>Gestión de Clientes</h2>
              <button mat-raised-button color="primary" (click)="crearCliente()">
                <mat-icon>add</mat-icon>
                Nuevo Cliente
              </button>
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
              <mat-label>Tipo de Contribuyente</mat-label>
              <mat-select [(ngModel)]="tipoFilter" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option value="PF">Persona Física</mat-option>
                <mat-option value="PJ">Persona Jurídica</mat-option>
                <mat-option value="EG">Entidad Gubernamental</mat-option>
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

          <!-- Resultados de búsqueda rápida -->
          <div class="quick-results" *ngIf="searchTerm && clientesFiltrados().length > 0 && clientesFiltrados().length <= 5">
            <div class="result-hint">
              <mat-icon>info</mat-icon>
              <span>Resultados de búsqueda rápida ({{ clientesFiltrados().length }})</span>
            </div>
          </div>

          <!-- Tabla de clientes -->
          <app-loading-spinner *ngIf="loading()" />
          
          <app-data-table
            *ngIf="!loading()"
            [columns]="columns"
            [data]="clientesFiltrados()"
            [actions]="tableActions"
            (actionClick)="onActionClick($event)"
          />
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
  `]
})
export class ClientesListComponent implements OnInit {
  loading = signal(false);
  clientes = signal<Cliente[]>([]);
  clientesFiltrados = signal<Cliente[]>([]);
  
  searchTerm = '';
  tipoFilter: string | null = null;
  estadoFilter: boolean | null = true; // Por defecto mostrar solo activos
  
  private searchSubject = new Subject<string>();

  columns: TableColumn[] = [
    { key: 'nombre', label: 'Nombre', sortable: true },
    { key: 'razonSocial', label: 'Razón Social', sortable: true },
    { key: 'ruc', label: 'RUC', sortable: true },
    { 
      key: 'tipoContribuyente', 
      label: 'Tipo', 
      sortable: true,
      format: (value: string) => {
        const tipos: Record<string, string> = {
          'PF': 'Persona Física',
          'PJ': 'Persona Jurídica',
          'EG': 'Entidad Gubernamental'
        };
        return tipos[value] || '-';
      }
    },
    { key: 'telefono', label: 'Teléfono' },
    { key: 'email', label: 'Email' },
    {
      key: 'tributa',
      label: 'Tributa',
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
      tooltip: 'Editar cliente',
      handler: (row: Cliente) => this.editCliente(row)
    },
    { 
      icon: 'delete', 
      label: 'Eliminar', 
      color: 'warn',
      tooltip: 'Eliminar cliente',
      handler: (row: Cliente) => this.deleteCliente(row)
    }
  ];

  constructor(
    private clienteApi: ClienteApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
    // Configurar debounce para búsqueda
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.aplicarFiltros();
    });
  }

  ngOnInit(): void {
    this.cargarClientes();
  }

  cargarClientes(): void {
    this.loading.set(true);
    // TODO: Obtener empresaId del state
    const empresaId = 1;
    
    this.clienteApi.getByEmpresa(empresaId).subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
        this.aplicarFiltros();
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
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    let filtrados = [...this.clientes()];

    // Filtro de búsqueda (nombre, RUC o razón social)
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtrados = filtrados.filter(c => 
        c.nombre.toLowerCase().includes(term) ||
        c.ruc?.toLowerCase().includes(term) ||
        c.razonSocial?.toLowerCase().includes(term)
      );
    }

    // Filtro de tipo de contribuyente
    if (this.tipoFilter) {
      filtrados = filtrados.filter(c => c.tipoContribuyente === this.tipoFilter);
    }

    // Filtro de estado
    if (this.estadoFilter !== null) {
      filtrados = filtrados.filter(c => c.activo === this.estadoFilter);
    }

    this.clientesFiltrados.set(filtrados);
  }

  limpiarBusqueda(): void {
    this.searchTerm = '';
    this.aplicarFiltros();
  }

  limpiarFiltros(): void {
    this.searchTerm = '';
    this.tipoFilter = null;
    this.estadoFilter = true;
    this.aplicarFiltros();
  }

  crearCliente(): void {
    const dialogRef = this.dialog.open(ClienteFormComponent, {
      width: '700px',
      data: { cliente: null }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.cargarClientes();
      }
    });
  }

  onActionClick(event: { action: string; row: Cliente }): void {
    if (event.action === 'Editar') {
      this.editarCliente(event.row);
    } else if (event.action === 'Eliminar') {
      this.eliminarCliente(event.row);
    }
  }

  editCliente(cliente: Cliente): void {
    this.editarCliente(cliente);
  }

  deleteCliente(cliente: Cliente): void {
    this.eliminarCliente(cliente);
  }

  editarCliente(cliente: Cliente): void {
    const dialogRef = this.dialog.open(ClienteFormComponent, {
      width: '700px',
      data: { cliente }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.cargarClientes();
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
        this.clienteApi.delete(cliente.id).subscribe({
          next: () => {
            this.snackBar.open('Cliente eliminado correctamente', 'Cerrar', { duration: 3000 });
            this.cargarClientes();
          },
          error: () => {
            this.snackBar.open('Error al eliminar cliente', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }
}
