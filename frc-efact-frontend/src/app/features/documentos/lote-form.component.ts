import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DocumentoElectronicoApiService } from '../../core/api/documento-electronico-api.service';
import { DocumentoElectronico, EstadoDE } from '../../models/documento-electronico.model';
import { DataTableComponent, TableColumn } from '../../shared/components/data-table/data-table.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-lote-form',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatCheckboxModule,
    MatSnackBarModule,
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="lote-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <h2>Crear Nuevo Lote</h2>
              <button mat-stroked-button (click)="volver()">
                <mat-icon>arrow_back</mat-icon>
                Cancelar
              </button>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <div class="info-section">
            <p>Seleccione los documentos electrónicos pendientes que desea incluir en el lote.</p>
            <p class="warning-text">
              <mat-icon>info</mat-icon>
              Solo se pueden incluir documentos en estado PENDIENTE.
            </p>
          </div>

          <!-- Selección de documentos -->
          <div class="selection-section">
            <div class="selection-header">
              <h3>Documentos Disponibles ({{ documentosPendientes().length }})</h3>
              <div class="selection-actions">
                <button mat-stroked-button (click)="seleccionarTodos()" [disabled]="documentosPendientes().length === 0">
                  <mat-icon>select_all</mat-icon>
                  Seleccionar Todos
                </button>
                <button mat-stroked-button (click)="limpiarSeleccion()" [disabled]="documentosSeleccionados().size === 0">
                  <mat-icon>clear</mat-icon>
                  Limpiar Selección
                </button>
              </div>
            </div>

            <div class="selected-count">
              <strong>{{ documentosSeleccionados().size }}</strong> documentos seleccionados
            </div>

            <app-loading-spinner *ngIf="loading()" />

            <app-data-table
              *ngIf="!loading()"
              [columns]="columns"
              [data]="documentosPendientes()"
              [selectable]="true"
              [selectedIds]="Array.from(documentosSeleccionados())"
              (selectionChange)="onSelectionChange($event)"
            />
          </div>

          <!-- Acciones -->
          <div class="actions-section">
            <button 
              mat-raised-button 
              color="primary" 
              (click)="crearLote()"
              [disabled]="documentosSeleccionados().size === 0 || loading()">
              <mat-icon>add</mat-icon>
              Crear Lote con {{ documentosSeleccionados().size }} Documentos
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .lote-form-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    h2 {
      margin: 0;
    }

    mat-card-header {
      margin-bottom: 24px;
    }

    .info-section {
      background: #e3f2fd;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 24px;
      border-left: 4px solid #2196f3;
    }

    .info-section p {
      margin: 8px 0;
    }

    .warning-text {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #1976d2;
      font-weight: 500;
    }

    .warning-text mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .selection-section {
      margin-bottom: 24px;
    }

    .selection-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .selection-header h3 {
      margin: 0;
    }

    .selection-actions {
      display: flex;
      gap: 12px;
    }

    .selected-count {
      background: #f5f5f5;
      padding: 12px;
      border-radius: 8px;
      margin-bottom: 16px;
      text-align: center;
      font-size: 16px;
    }

    .selected-count strong {
      color: #2196f3;
      font-size: 20px;
    }

    .actions-section {
      display: flex;
      justify-content: center;
      padding-top: 24px;
      border-top: 1px solid #ddd;
    }

    .actions-section button {
      min-width: 300px;
    }

    @media (max-width: 768px) {
      .header-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }

      .selection-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }

      .selection-actions {
        width: 100%;
        flex-direction: column;
      }

      .selection-actions button {
        width: 100%;
      }

      .actions-section button {
        width: 100%;
        min-width: auto;
      }
    }
  `]
})
export class LoteFormComponent implements OnInit {
  loading = signal(false);
  documentosPendientes = signal<DocumentoElectronico[]>([]);
  documentosSeleccionados = signal<Set<number>>(new Set());
  
  // Expose Array to template
  Array = Array;

  columns: TableColumn[] = [
    { 
      key: 'id', 
      label: 'ID', 
      sortable: true
    },
    { 
      key: 'facturaLegalId', 
      label: 'Factura ID', 
      sortable: true
    },
    { 
      key: 'numeroDocumento', 
      label: 'Número', 
      sortable: true
    },
    { 
      key: 'fechaEmision', 
      label: 'Fecha Emisión', 
      sortable: true,
      format: (value: string) => new Date(value).toLocaleDateString('es-PY')
    },
    {
      key: 'cdc',
      label: 'CDC',
      sortable: false,
      format: (value: string) => value ? value.substring(0, 20) + '...' : 'Generando...'
    }
  ];

  constructor(
    private deApi: DocumentoElectronicoApiService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarDocumentosPendientes();
  }

  cargarDocumentosPendientes(): void {
    this.loading.set(true);
    
    this.deApi.getAll(EstadoDE.PENDIENTE).subscribe({
      next: (documentos) => {
        this.documentosPendientes.set(documentos);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar documentos pendientes', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  onSelectionChange(selectedIds: number[]): void {
    this.documentosSeleccionados.set(new Set(selectedIds));
  }

  seleccionarTodos(): void {
    const allIds = this.documentosPendientes().map(d => d.id);
    this.documentosSeleccionados.set(new Set(allIds));
  }

  limpiarSeleccion(): void {
    this.documentosSeleccionados.set(new Set());
  }

  crearLote(): void {
    const selectedIds = Array.from(this.documentosSeleccionados());
    
    if (selectedIds.length === 0) {
      this.snackBar.open('Debe seleccionar al menos un documento', 'Cerrar', { duration: 3000 });
      return;
    }

    this.loading.set(true);
    
    this.deApi.crearLote(selectedIds).subscribe({
      next: (lote) => {
        this.snackBar.open(
          `Lote #${lote.id} creado correctamente con ${selectedIds.length} documentos`,
          'Cerrar',
          { duration: 3000 }
        );
        this.router.navigate(['/documentos/lotes']);
      },
      error: (error) => {
        this.snackBar.open(
          error.error?.message || 'Error al crear lote',
          'Cerrar',
          { duration: 5000 }
        );
        this.loading.set(false);
      }
    });
  }

  volver(): void {
    this.router.navigate(['/documentos/lotes']);
  }
}
