import { Component, OnInit, signal, ViewChild, ElementRef } from '@angular/core';
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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { FacturaApiService } from '../../core/api/factura-api.service';
import { DocumentoElectronicoApiService } from '../../core/api/documento-electronico-api.service';
import { FacturaLegal } from '../../models/factura.model';
import { EstadoDE } from '../../models/documento-electronico.model';
import { FacturaFiltro } from '../../models/reporte.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-factura-list',
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
    MatDatepickerModule,
    MatNativeDateModule,
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="facturas-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <h2>Gestión de Facturas</h2>
              <button mat-raised-button color="primary" (click)="crearFactura()">
                <mat-icon>add</mat-icon>
                Nueva Factura
              </button>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Filtros -->
          <div class="filters-section">
            <mat-form-field appearance="outline">
              <mat-label>Fecha desde</mat-label>
              <input matInput 
                     [matDatepicker]="pickerDesde"
                     [(ngModel)]="filtro.fechaDesde"
                     (ngModelChange)="onFilterChange()">
              <mat-datepicker-toggle matSuffix [for]="pickerDesde"></mat-datepicker-toggle>
              <mat-datepicker #pickerDesde></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Fecha hasta</mat-label>
              <input matInput 
                     [matDatepicker]="pickerHasta"
                     [(ngModel)]="filtro.fechaHasta"
                     (ngModelChange)="onFilterChange()">
              <mat-datepicker-toggle matSuffix [for]="pickerHasta"></mat-datepicker-toggle>
              <mat-datepicker #pickerHasta></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Cliente</mat-label>
              <input matInput 
                     [(ngModel)]="searchCliente"
                     (ngModelChange)="onFilterChange()"
                     placeholder="Buscar por nombre o RUC">
              <mat-icon matPrefix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Estado DE</mat-label>
              <mat-select [(ngModel)]="filtro.estado" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option value="PENDIENTE">Pendiente</mat-option>
                <mat-option value="EN_PROCESO">En Proceso</mat-option>
                <mat-option value="APROBADO">Aprobado</mat-option>
                <mat-option value="RECHAZADO">Rechazado</mat-option>
                <mat-option value="CANCELADO">Cancelado</mat-option>
                <mat-option value="ERROR">Error</mat-option>
                <mat-option value="SIN_DE">Sin DE</mat-option>
              </mat-select>
            </mat-form-field>

            <button mat-stroked-button (click)="limpiarFiltros()">
              <mat-icon>clear</mat-icon>
              Limpiar
            </button>
          </div>

          <!-- Tabla de facturas -->
          <app-loading-spinner *ngIf="loading()" />
          
          <app-data-table
            *ngIf="!loading()"
            [columns]="columns"
            [data]="facturasFiltradas()"
            [actions]="tableActions"
            (actionClick)="onActionClick($event)"
          />
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .facturas-container {
      padding: 20px;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .filters-section {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      flex-wrap: wrap;
      align-items: center;
    }

    mat-form-field {
      min-width: 180px;
    }

    mat-card-header {
      margin-bottom: 24px;
    }

    h2 {
      margin: 0;
    }

    .estado-chip {
      font-size: 12px;
      padding: 4px 8px;
      border-radius: 12px;
      font-weight: 500;
    }

    .estado-pendiente {
      background-color: #fff3cd;
      color: #856404;
    }

    .estado-proceso {
      background-color: #cfe2ff;
      color: #084298;
    }

    .estado-aprobado {
      background-color: #d1e7dd;
      color: #0f5132;
    }

    .estado-rechazado {
      background-color: #f8d7da;
      color: #842029;
    }

    .estado-cancelado {
      background-color: #e2e3e5;
      color: #41464b;
    }

    .estado-error {
      background-color: #f8d7da;
      color: #842029;
    }

    .estado-sin-de {
      background-color: #f8f9fa;
      color: #6c757d;
    }
  `]
})
export class FacturaListComponent implements OnInit {
  loading = signal(false);
  facturas = signal<FacturaLegal[]>([]);
  facturasFiltradas = signal<FacturaLegal[]>([]);
  
  filtro: FacturaFiltro = {
    empresaId: 1, // TODO: Obtener del state
    fechaDesde: undefined,
    fechaHasta: undefined,
    clienteId: undefined,
    estado: undefined,
    montoMinimo: undefined,
    montoMaximo: undefined
  };
  
  searchCliente = '';

  columns: TableColumn[] = [
    { 
      key: 'numeroFactura', 
      label: 'Número', 
      sortable: true,
      format: (value: number) => value.toString().padStart(7, '0')
    },
    { 
      key: 'fecha', 
      label: 'Fecha', 
      sortable: true,
      format: (value: string) => new Date(value).toLocaleDateString('es-PY')
    },
    { key: 'nombre', label: 'Cliente', sortable: true },
    { key: 'ruc', label: 'RUC', sortable: false },
    { 
      key: 'totalFinal', 
      label: 'Total', 
      sortable: true,
      format: (value: number) => `₲ ${value.toLocaleString('es-PY')}`
    },
    {
      key: 'credito',
      label: 'Tipo',
      format: (value: boolean) => value ? 'Crédito' : 'Contado'
    },
    {
      key: 'estadoDE',
      label: 'Estado DE',
      format: (value: string) => this.formatEstadoDE(value)
    }
  ];

  tableActions: TableAction[] = [
    { 
      icon: 'visibility', 
      label: 'Ver', 
      color: 'primary',
      tooltip: 'Ver factura',
      handler: (row: any) => this.verFactura(row)
    },
    { 
      icon: 'edit', 
      label: 'Editar', 
      color: 'primary',
      tooltip: 'Editar factura',
      handler: (row: any) => this.editarFactura(row)
    },
    { 
      icon: 'description', 
      label: 'Generar DE', 
      color: 'accent',
      tooltip: 'Generar documento electrónico',
      handler: (row: any) => this.generarDE(row)
    },
    { 
      icon: 'print', 
      label: 'Imprimir', 
      color: 'primary',
      tooltip: 'Imprimir factura',
      handler: (row: any) => this.imprimirFactura(row)
    },
    { 
      icon: 'delete', 
      label: 'Eliminar', 
      color: 'warn',
      tooltip: 'Eliminar factura',
      handler: (row: any) => this.eliminarFactura(row)
    }
  ];

  constructor(
    private facturaApi: FacturaApiService,
    private deApi: DocumentoElectronicoApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarFacturas();
  }

  cargarFacturas(): void {
    this.loading.set(true);
    
    this.facturaApi.getAll(this.filtro).subscribe({
      next: (facturas) => {
        // Enriquecer con estado DE
        this.facturas.set(facturas.map(f => ({
          ...f,
          estadoDE: this.getEstadoDE(f)
        } as any)));
        this.aplicarFiltros();
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar facturas', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  getEstadoDE(factura: FacturaLegal): string {
    // TODO: Implementar lógica real cuando tengamos la relación con DE
    return 'SIN_DE';
  }

  formatEstadoDE(estado: string): string {
    const estadoMap: { [key: string]: string } = {
      'PENDIENTE': '⏳ Pendiente',
      'EN_PROCESO': '🔄 En Proceso',
      'APROBADO': '✅ Aprobado',
      'RECHAZADO': '❌ Rechazado',
      'CANCELADO': '🚫 Cancelado',
      'ERROR': '⚠️ Error',
      'SIN_DE': '📄 Sin DE'
    };
    return estadoMap[estado] || estado;
  }

  onFilterChange(): void {
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    let filtradas = [...this.facturas()];

    // Filtro de búsqueda de cliente
    if (this.searchCliente) {
      const term = this.searchCliente.toLowerCase();
      filtradas = filtradas.filter(f => 
        f.nombre?.toLowerCase().includes(term) ||
        f.ruc?.toLowerCase().includes(term)
      );
    }

    this.facturasFiltradas.set(filtradas);
  }

  limpiarFiltros(): void {
    this.filtro = {
      empresaId: 1, // TODO: Obtener del state
      fechaDesde: undefined,
      fechaHasta: undefined,
      clienteId: undefined,
      estado: undefined,
      montoMinimo: undefined,
      montoMaximo: undefined
    };
    this.searchCliente = '';
    this.cargarFacturas();
  }

  crearFactura(): void {
    this.router.navigate(['/facturacion/nueva']);
  }

  onActionClick(event: { action: string; row: any }): void {
    const factura = event.row as FacturaLegal;
    
    switch (event.action) {
      case 'Ver':
        this.verFactura(factura);
        break;
      case 'Editar':
        this.editarFactura(factura);
        break;
      case 'Generar DE':
        this.generarDE(factura);
        break;
      case 'Imprimir':
        this.imprimirFactura(factura);
        break;
      case 'Eliminar':
        this.eliminarFactura(factura);
        break;
    }
  }

  verFactura(factura: FacturaLegal): void {
    this.router.navigate(['/facturacion', factura.id]);
  }

  editarFactura(factura: FacturaLegal): void {
    this.router.navigate(['/facturacion', factura.id, 'editar']);
  }

  generarDE(factura: FacturaLegal): void {
    if (!factura.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Generar Documento Electrónico',
        message: `¿Desea generar el documento electrónico para la factura N° ${factura.numeroFactura}?`,
        confirmText: 'Generar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed && factura.id) {
        this.loading.set(true);
        this.facturaApi.generarDE(factura.id).subscribe({
          next: (de) => {
            this.snackBar.open('Documento electrónico generado correctamente', 'Cerrar', { duration: 3000 });
            this.cargarFacturas();
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al generar documento electrónico',
              'Cerrar',
              { duration: 5000 }
            );
            this.loading.set(false);
          }
        });
      }
    });
  }

  imprimirFactura(factura: FacturaLegal): void {
    if (!factura.id) return;

    this.loading.set(true);
    this.facturaApi.descargarPDF(factura.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `factura-${factura.numeroFactura}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al descargar PDF', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  eliminarFactura(factura: FacturaLegal): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de eliminar la factura N° ${factura.numeroFactura}?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed && factura.id) {
        this.facturaApi.delete(factura.id).subscribe({
          next: () => {
            this.snackBar.open('Factura eliminada correctamente', 'Cerrar', { duration: 3000 });
            this.cargarFacturas();
          },
          error: () => {
            this.snackBar.open('Error al eliminar factura', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }
}
