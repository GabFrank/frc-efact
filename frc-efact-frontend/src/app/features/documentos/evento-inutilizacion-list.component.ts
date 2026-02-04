import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { Store } from '@ngrx/store';
import { Observable, takeUntil, combineLatest } from 'rxjs';
import { Subject } from 'rxjs';
import { selectSelectedEmpresa } from '../../core/state/empresas/empresas.selectors';
import { Empresa } from '../../models/empresa.model';
import { SifenApiService, EventoInutilizacionDE, EventoInutilizacionFiltros, PageResponse } from '../../core/api/sifen-api.service';
import { DataTableComponent, TableColumn } from '../../shared/components/data-table/data-table.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-evento-inutilizacion-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatSnackBarModule,
    MatChipsModule,
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="eventos-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>Eventos de Inutilización</h2>
          </mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <!-- Filtros -->
          <div class="filters-section">
            <mat-form-field appearance="outline">
              <mat-label>Estado</mat-label>
              <mat-select [(ngModel)]="filtros.estado" (ngModelChange)="aplicarFiltros()">
                <mat-option [value]="undefined">Todos</mat-option>
                <mat-option value="PENDIENTE">Pendiente</mat-option>
                <mat-option value="APROBADO">Aprobado</mat-option>
                <mat-option value="RECHAZADO">Rechazado</mat-option>
                <mat-option value="ERROR_ENVIO">Error Envío</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Timbrado ID</mat-label>
              <input matInput type="number" [(ngModel)]="filtros.timbradoId" (ngModelChange)="aplicarFiltros()" placeholder="ID del timbrado">
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Fecha Desde</mat-label>
              <input matInput [matDatepicker]="pickerDesde" [(ngModel)]="fechaDesde" (dateChange)="onFechaDesdeChange()">
              <mat-datepicker-toggle matSuffix [for]="pickerDesde"></mat-datepicker-toggle>
              <mat-datepicker #pickerDesde></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Fecha Hasta</mat-label>
              <input matInput [matDatepicker]="pickerHasta" [(ngModel)]="fechaHasta" (dateChange)="onFechaHastaChange()">
              <mat-datepicker-toggle matSuffix [for]="pickerHasta"></mat-datepicker-toggle>
              <mat-datepicker #pickerHasta></mat-datepicker>
            </mat-form-field>

            <button mat-stroked-button (click)="limpiarFiltros()">
              <mat-icon>clear</mat-icon>
              Limpiar
            </button>
          </div>

          <app-loading-spinner *ngIf="loading()" />
          
          <app-data-table
            *ngIf="!loading()"
            [columns]="columns"
            [data]="eventos()"
            [actions]="[]"
          />

          <!-- Paginación -->
          <mat-paginator
            *ngIf="!loading() && totalElements() > 0"
            [length]="totalElements()"
            [pageSize]="pageSize()"
            [pageIndex]="currentPage()"
            [pageSizeOptions]="[10, 20, 50, 100]"
            (page)="onPageChange($event)"
            showFirstLastButtons>
          </mat-paginator>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .eventos-container {
      padding: 20px;
    }

    h2 {
      margin: 0;
    }

    .filters-section {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      flex-wrap: wrap;
      align-items: center;
    }

    mat-form-field {
      min-width: 200px;
    }

    .estado-chip {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
    }

    .estado-PENDIENTE {
      background-color: #fff3cd;
      color: #856404;
    }

    .estado-APROBADO {
      background-color: #d1e7dd;
      color: #0f5132;
    }

    .estado-RECHAZADO {
      background-color: #f8d7da;
      color: #842029;
    }

    .estado-ERROR_ENVIO {
      background-color: #f8d7da;
      color: #842029;
    }
  `]
})
export class EventoInutilizacionListComponent implements OnInit {
  loading = signal(false);
  eventos = signal<EventoInutilizacionDE[]>([]);
  totalElements = signal(0);
  currentPage = signal(0);
  pageSize = signal(20);
  selectedEmpresa$: Observable<Empresa | null | undefined>;
  
  filtros: EventoInutilizacionFiltros = {
    estado: undefined,
    timbradoId: undefined,
    fechaInicio: undefined,
    fechaFin: undefined,
    page: 0,
    size: 20
  };

  fechaDesde: Date | null = null;
  fechaHasta: Date | null = null;
  
  columns: TableColumn[] = [
    { key: 'id', label: 'ID', sortable: true },
    { key: 'eventoId', label: 'Evento ID', sortable: true },
    { key: 'establecimiento', label: 'Establecimiento', sortable: true },
    { key: 'puntoExpedicion', label: 'Punto Expedición', sortable: true },
    { key: 'numeroInicio', label: 'Número Inicio', sortable: true },
    { key: 'numeroFin', label: 'Número Fin', sortable: true },
    { key: 'tipoDE', label: 'Tipo DE', sortable: true },
    { key: 'motivoInutilizacion', label: 'Motivo', sortable: false },
    { key: 'estado', label: 'Estado', sortable: true },
    { key: 'fechaFirma', label: 'Fecha Firma', sortable: true },
    { key: 'fechaProcesamiento', label: 'Fecha Procesamiento', sortable: true },
    { key: 'codigoRespuesta', label: 'Código Respuesta', sortable: false }
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private sifenApi: SifenApiService,
    private store: Store,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.selectedEmpresa$ = this.store.select(selectSelectedEmpresa);
  }

  ngOnInit(): void {
    // Combinar query params y empresa del store para determinar empresaId
    combineLatest([
      this.route.queryParams,
      this.selectedEmpresa$
    ]).pipe(takeUntil(this.destroy$)).subscribe(([params, empresa]) => {
      // Priorizar empresaId de query params, si no existe usar el del store
      const empresaId = params['empresaId'] ? +params['empresaId'] : (empresa?.id || null);
      
      if (empresaId && empresaId !== this.filtros.empresaId) {
        this.filtros.empresaId = empresaId;
        this.cargarEventos();
      }
    });
  }

  cargarEventos(): void {
    if (!this.filtros.empresaId) return;
    
    this.loading.set(true);
    this.filtros.page = this.currentPage();
    this.filtros.size = this.pageSize();
    
    this.sifenApi.listarEventosInutilizacion(this.filtros).subscribe({
      next: (response: PageResponse<EventoInutilizacionDE>) => {
        this.eventos.set(response.content || []);
        this.totalElements.set(response.totalElements || 0);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar eventos de inutilización', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  aplicarFiltros(): void {
    this.currentPage.set(0);
    this.cargarEventos();
  }

  limpiarFiltros(): void {
    this.filtros.estado = undefined;
    this.filtros.timbradoId = undefined;
    this.filtros.fechaInicio = undefined;
    this.filtros.fechaFin = undefined;
    this.fechaDesde = null;
    this.fechaHasta = null;
    this.aplicarFiltros();
  }

  onFechaDesdeChange(): void {
    if (this.fechaDesde) {
      this.filtros.fechaInicio = this.fechaDesde.toISOString();
    } else {
      this.filtros.fechaInicio = undefined;
    }
    this.aplicarFiltros();
  }

  onFechaHastaChange(): void {
    if (this.fechaHasta) {
      const fecha = new Date(this.fechaHasta);
      fecha.setHours(23, 59, 59, 999);
      this.filtros.fechaFin = fecha.toISOString();
    } else {
      this.filtros.fechaFin = undefined;
    }
    this.aplicarFiltros();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargarEventos();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
