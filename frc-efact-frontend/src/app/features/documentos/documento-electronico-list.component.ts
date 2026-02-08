import { Component, OnInit, OnDestroy, AfterViewInit, signal, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd, ActivatedRoute, Params } from '@angular/router';
import { filter, takeUntil, distinctUntilChanged } from 'rxjs/operators';
import { Subject, Observable, combineLatest } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Store } from '@ngrx/store';
import { DocumentoElectronicoApiService } from '../../core/api/documento-electronico-api.service';
import { DocumentoElectronico, EstadoDE } from '../../models/documento-electronico.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { CancelarDeDialogComponent } from './cancelar-de-dialog.component';
import { selectSelectedEmpresa } from '../../core/state/empresas/empresas.selectors';
import { Empresa } from '../../models/empresa.model';

@Component({
  selector: 'app-documento-electronico-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatChipsModule,
    MatPaginatorModule,
    MatMenuModule,
    MatTooltipModule,
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="documentos-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <h2>Documentos Electrónicos</h2>
              <div class="header-actions">
                <button mat-stroked-button color="primary" (click)="crearLote()">
                  <mat-icon>folder</mat-icon>
                  Crear Lote
                </button>
                <button mat-raised-button color="primary" (click)="verLotes()">
                  <mat-icon>list</mat-icon>
                  Ver Lotes
                </button>
              </div>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Filtros -->
          <div class="filters-section">
            <mat-form-field appearance="outline">
              <mat-label>Estado</mat-label>
              <mat-select [(ngModel)]="estadoFiltro" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option [value]="EstadoDE.PENDIENTE">Pendiente</mat-option>
                <mat-option [value]="EstadoDE.EN_PROCESO">En Proceso</mat-option>
                <mat-option [value]="EstadoDE.APROBADO">Aprobado</mat-option>
                <mat-option [value]="EstadoDE.RECHAZADO">Rechazado</mat-option>
                <mat-option [value]="EstadoDE.CANCELADO">Cancelado</mat-option>
                <mat-option [value]="EstadoDE.ERROR">Error</mat-option>
              </mat-select>
            </mat-form-field>

            <button mat-stroked-button (click)="limpiarFiltros()">
              <mat-icon>clear</mat-icon>
              Limpiar
            </button>

            <button mat-stroked-button (click)="cargarDocumentos()">
              <mat-icon>refresh</mat-icon>
              Actualizar
            </button>
          </div>

          <!-- Estadísticas rápidas -->
          <div class="stats-section">
            <div class="stat-card">
              <div class="stat-value">{{ getCountByEstado(EstadoDE.PENDIENTE) }}</div>
              <div class="stat-label">Pendientes</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ getCountByEstado(EstadoDE.EN_PROCESO) }}</div>
              <div class="stat-label">En Proceso</div>
            </div>
            <div class="stat-card success">
              <div class="stat-value">{{ getCountByEstado(EstadoDE.APROBADO) }}</div>
              <div class="stat-label">Aprobados</div>
            </div>
            <div class="stat-card error">
              <div class="stat-value">{{ getCountByEstado(EstadoDE.RECHAZADO) }}</div>
              <div class="stat-label">Rechazados</div>
            </div>
          </div>

          <!-- Tabla de documentos -->
          <app-loading-spinner *ngIf="loading()" />

          <div class="list-desktop" *ngIf="!loading() && !isMobile()">
            <app-data-table
              [columns]="columns"
              [data]="documentos()"
              [actions]="tableActions"
              [pageSize]="pageSize"
              [pageIndex]="pageIndex"
              [totalItems]="totalElements()"
              (actionClick)="onActionClick($event)"
              (pageChange)="onPageChange($event)"
            />
          </div>
          <div class="list-mobile" *ngIf="!loading() && isMobile()">
            <div class="mobile-cards" *ngIf="documentos().length > 0">
              <mat-card class="list-card" *ngFor="let item of documentos()">
                <mat-card-header class="list-card-header">
                  <mat-card-title class="list-card-title">
                    <span class="list-card-num">#{{ item.id }}</span>
                    <span class="list-card-date">{{ formatDateForCard(item.fechaEmision) }}</span>
                  </mat-card-title>
                  <button class="list-card-menu-trigger" mat-icon-button color="primary"
                    [matMenuTriggerFor]="cardActionMenu" (click)="setMenuContext(item)"
                    matTooltip="Acciones" aria-label="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>
                </mat-card-header>
                <mat-card-content class="list-card-content">
                  <div class="list-card-field">
                    <span class="list-card-label">Número</span>
                    <span class="list-card-value">{{ item.numeroDocumento || '—' }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Estado</span>
                    <span class="list-card-value">{{ getEstadoLabel(item.estado) }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Fecha Recepción</span>
                    <span class="list-card-value">{{ item.fechaRecepcionSifen ? formatDateForCard(item.fechaRecepcionSifen) : '—' }}</span>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
            <div class="mobile-empty" *ngIf="documentos().length === 0">No hay documentos electrónicos para mostrar.</div>
            <mat-paginator *ngIf="documentos().length > 0" [length]="totalElements()" [pageSize]="pageSize"
              [pageIndex]="pageIndex" [pageSizeOptions]="[5, 10, 25, 50]" (page)="onPageChange($event)"
              showFirstLastButtons></mat-paginator>
            <mat-menu #cardActionMenu="matMenu" class="card-action-menu">
              <button mat-menu-item *ngFor="let a of menuActions" type="button" class="list-card-menu-item"
                (click)="menuRow && onActionClick({ action: (a.label || a.tooltip || a.icon) || '', row: menuRow })">
                <mat-icon>{{ a.icon }}</mat-icon>
                <span>{{ a.label || a.tooltip || a.icon }}</span>
              </button>
            </mat-menu>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .documentos-container {
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

    mat-form-field {
      min-width: 200px;
    }

    mat-card-header {
      margin-bottom: 24px;
    }

    h2 {
      margin: 0;
    }

    .stats-section {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .stat-card {
      background: #f5f5f5;
      padding: 16px;
      border-radius: 8px;
      text-align: center;
      border-left: 4px solid #2196f3;
    }

    .stat-card.success {
      border-left-color: #4caf50;
    }

    .stat-card.error {
      border-left-color: #f44336;
    }

    .list-desktop { width: 100%; }
    .list-mobile { width: 100%; }
    .mobile-cards { display: flex; flex-direction: column; gap: 12px; margin-bottom: 16px; }
    .list-card { margin: 0; }
    .list-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; margin-bottom: 0; }
    .list-card-title { display: flex; flex-direction: column; gap: 2px; margin: 0; font-size: 1rem; }
    .list-card-num { font-weight: 600; color: #2c3e50; }
    .list-card-date { font-size: 0.875rem; color: rgba(0,0,0,0.6); }
    :host-context(body.dark-theme) .list-card-num { color: #e0e0e0; }
    :host-context(body.dark-theme) .list-card-date { color: rgba(255,255,255,0.6); }
    .list-card-menu-trigger { flex-shrink: 0; }
    .list-card-menu-trigger .mat-icon { font-size: 1.5rem; width: 24px; height: 24px; }
    .list-card-content { display: flex; flex-direction: column; gap: 8px; padding-top: 0; }
    .list-card-field { display: flex; flex-direction: column; gap: 2px; }
    .list-card-label { font-size: 0.75rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; color: rgba(0,0,0,0.6); }
    .list-card-value { font-size: 0.9375rem; color: #2c3e50; word-break: break-word; }
    :host-context(body.dark-theme) .list-card-label { color: rgba(255,255,255,0.6); }
    :host-context(body.dark-theme) .list-card-value { color: #e0e0e0; }
    .mobile-empty { padding: 24px 16px; text-align: center; color: rgba(0,0,0,0.6); }
    :host-context(body.dark-theme) .mobile-empty { color: rgba(255,255,255,0.6); }
    .list-mobile mat-paginator { border-top: 1px solid rgba(0,0,0,0.12); }
    :host-context(body.dark-theme) .list-mobile mat-paginator { border-top-color: rgba(255,255,255,0.12); }

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #333;
    }

    .stat-label {
      font-size: 14px;
      color: #666;
      margin-top: 4px;
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

    .estado-EN_PROCESO {
      background-color: #cfe2ff;
      color: #084298;
    }

    .estado-APROBADO {
      background-color: #d1e7dd;
      color: #0f5132;
    }

    .estado-RECHAZADO {
      background-color: #f8d7da;
      color: #842029;
    }

    .estado-CANCELADO {
      background-color: #e2e3e5;
      color: #41464b;
    }

    .estado-ERROR {
      background-color: #f8d7da;
      color: #842029;
    }
  `]
})
export class DocumentoElectronicoListComponent implements OnInit, AfterViewInit, OnDestroy {
  loading = signal(false);
  documentos = signal<DocumentoElectronico[]>([]);
  totalElements = signal(0);
  estadoFiltro: EstadoDE | null = null;
  selectedEmpresa$: Observable<Empresa | null | undefined>;
  empresaId: number | null = null;
  
  // Paginación
  pageSize = 20;
  pageIndex = 0;
  
  // Expose enum to template
  EstadoDE = EstadoDE;
  
  private destroy$ = new Subject<void>();
  private breakpointObserver = inject(BreakpointObserver);
  isMobile = signal(false);
  menuRow: DocumentoElectronico | null = null;
  menuActions: TableAction[] = [];

  columns: TableColumn[] = [
    { 
      key: 'id', 
      label: 'ID', 
      sortable: true
    },
    { 
      key: 'cdc', 
      label: 'CDC', 
      sortable: false,
      format: (value: string) => value ? value.substring(0, 20) + '...' : 'N/A'
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
      key: 'estado',
      label: 'Estado',
      sortable: true,
      format: (value: EstadoDE) => this.formatEstado(value)
    },
    { 
      key: 'fechaRecepcionSifen', 
      label: 'Fecha Recepción', 
      sortable: true,
      format: (value: string) => value ? new Date(value).toLocaleDateString('es-PY') : '-'
    },
    {
      key: 'codigoRespuestaSifen',
      label: 'Código SIFEN',
      sortable: false,
      format: (value: string) => value || '-'
    }
  ];

  tableActions: TableAction[] = [
    { 
      icon: 'visibility', 
      label: 'Ver', 
      color: 'primary',
      tooltip: 'Ver documento',
      handler: (row: any) => this.verDocumento(row)
    },
    { 
      icon: 'refresh', 
      label: 'Consultar Estado', 
      color: 'accent',
      tooltip: 'Consultar estado en SIFEN',
      handler: (row: any) => this.consultarEstado(row)
    },
    { 
      icon: 'download', 
      label: 'Descargar XML', 
      color: 'primary',
      tooltip: 'Descargar XML',
      handler: (row: any) => this.descargarXML(row)
    },
    { 
      icon: 'qr_code', 
      label: 'Ver QR', 
      color: 'primary',
      tooltip: 'Ver código QR',
      handler: (row: any) => this.verQR(row)
    },
    { 
      icon: 'cancel', 
      label: 'Cancelar', 
      color: 'warn',
      tooltip: 'Cancelar documento',
      handler: (row: any) => this.cancelarDocumento(row)
    }
  ];

  constructor(
    private deApi: DocumentoElectronicoApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute,
    private store: Store,
    private cdr: ChangeDetectorRef
  ) {
    this.selectedEmpresa$ = this.store.select(selectSelectedEmpresa);
  }

  ngOnInit(): void {
    this.breakpointObserver.observe(['(max-width: 768px)']).pipe(takeUntil(this.destroy$)).subscribe(s => this.isMobile.set(s.matches));
    // Combinar query params y empresa del store para determinar empresaId
    combineLatest([
      this.route.queryParams,
      this.selectedEmpresa$
    ]).pipe(takeUntil(this.destroy$)).subscribe(([params, empresa]: [Params, Empresa | null | undefined]) => {
      // Priorizar empresaId de query params, si no existe usar el del store
      this.empresaId = params['empresaId'] ? +params['empresaId'] : (empresa?.id || null);
      // Cargar documentos con el empresaId
      this.cargarDocumentos();
    });

    // Suscribirse a cambios de ruta para recargar cuando se navega a esta ruta
    // Usar distinctUntilChanged para evitar múltiples llamadas con la misma URL
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        distinctUntilChanged((prev, curr) => prev.urlAfterRedirects === curr.urlAfterRedirects),
        takeUntil(this.destroy$)
      )
      .subscribe((event) => {
        // Si la ruta es /documentos/lista, recargar los datos solo si no está cargando
        if ((event.urlAfterRedirects === '/documentos/lista' || event.urlAfterRedirects.startsWith('/documentos/lista?')) && !this.loading()) {
          // Actualizar empresaId y recargar
          combineLatest([this.route.queryParams, this.selectedEmpresa$])
            .pipe(takeUntil(this.destroy$))
            .subscribe(([params, empresa]: [Params, Empresa | null | undefined]) => {
              this.empresaId = params['empresaId'] ? +params['empresaId'] : (empresa?.id || null);
              this.cargarDocumentos();
            });
        }
      });
  }

  ngAfterViewInit(): void {
    // No-op; carga realizada en ngOnInit
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  formatDateForCard(fecha: string | null | undefined): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-PY');
  }

  getVisibleActions(row: DocumentoElectronico | null): TableAction[] {
    return row && this.tableActions?.length ? this.tableActions.filter(a => !a.visible || a.visible(row)) : [];
  }

  setMenuContext(item: DocumentoElectronico): void {
    this.menuRow = item;
    this.menuActions = this.getVisibleActions(item);
  }

  cargarDocumentos(): void {
    this.loading.set(true);
    
    this.deApi.getAllPaginated(
      this.estadoFiltro || undefined,
      this.empresaId || undefined,
      this.pageIndex,
      this.pageSize
    ).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        this.documentos.set(response.content || []);
        this.totalElements.set(response.totalElements || 0);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar documentos electrónicos', 'Cerrar', { duration: 3000 });
        this.documentos.set([]);
        this.totalElements.set(0);
        this.loading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarDocumentos();
  }

  formatEstado(estado: EstadoDE): string {
    const html = `<span class="estado-chip estado-${estado}">${this.getEstadoLabel(estado)}</span>`;
    return html;
  }

  getEstadoLabel(estado: EstadoDE): string {
    const labels: { [key in EstadoDE]: string } = {
      [EstadoDE.PENDIENTE]: '⏳ Pendiente',
      [EstadoDE.EN_PROCESO]: '🔄 En Proceso',
      [EstadoDE.APROBADO]: '✅ Aprobado',
      [EstadoDE.RECHAZADO]: '❌ Rechazado',
      [EstadoDE.CANCELADO]: '🚫 Cancelado',
      [EstadoDE.ERROR]: '⚠️ Error'
    };
    return labels[estado];
  }

  getCountByEstado(estado: EstadoDE): number {
    const docs = this.documentos();
    // Asegurar que siempre sea un array
    if (!Array.isArray(docs)) {
      return 0;
    }
    return docs.filter(d => d.estado === estado).length;
  }

  onFilterChange(): void {
    // Resetear paginación al cambiar filtros
    this.pageIndex = 0;
    this.cargarDocumentos();
  }

  limpiarFiltros(): void {
    this.estadoFiltro = null;
    // Resetear paginación al limpiar filtros
    this.pageIndex = 0;
    this.cargarDocumentos();
  }

  crearLote(): void {
    // Filtrar documentos pendientes
    const docs = this.documentos();
    // Asegurar que siempre sea un array
    if (!Array.isArray(docs)) {
      this.snackBar.open('Error: No se pueden cargar los documentos', 'Cerrar', { duration: 3000 });
      return;
    }
    const pendientes = docs.filter(d => d.estado === EstadoDE.PENDIENTE);
    
    if (pendientes.length === 0) {
      this.snackBar.open('No hay documentos pendientes para crear un lote', 'Cerrar', { duration: 3000 });
      return;
    }

    this.router.navigate(['/documentos/lotes/nuevo']);
  }

  verLotes(): void {
    this.router.navigate(['/documentos/lotes']);
  }

  onActionClick(event: { action: string; row: any }): void {
    const documento = event.row as DocumentoElectronico;
    
    switch (event.action) {
      case 'Ver':
        this.verDocumento(documento);
        break;
      case 'Consultar Estado':
        this.consultarEstado(documento);
        break;
      case 'Descargar XML':
        this.descargarXML(documento);
        break;
      case 'Ver QR':
        this.verQR(documento);
        break;
      case 'Cancelar':
        this.cancelarDocumento(documento);
        break;
    }
  }

  verDocumento(documento: DocumentoElectronico): void {
    this.router.navigate(['/documentos', documento.id]);
  }

  consultarEstado(documento: DocumentoElectronico): void {
    if (!documento.cdc) {
      this.snackBar.open('El documento no tiene CDC generado', 'Cerrar', { duration: 3000 });
      return;
    }

    this.loading.set(true);
    this.deApi.consultarEstado(documento.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (documentoActualizado) => {
        this.snackBar.open('Estado actualizado correctamente', 'Cerrar', { duration: 3000 });
        this.cargarDocumentos();
      },
      error: (error) => {
        this.snackBar.open(
          error.error?.message || 'Error al consultar estado',
          'Cerrar',
          { duration: 5000 }
        );
        this.loading.set(false);
      }
    });
  }

  descargarXML(documento: DocumentoElectronico): void {
    if (!documento.cdc) {
      this.snackBar.open('El documento no tiene XML generado', 'Cerrar', { duration: 3000 });
      return;
    }

    this.loading.set(true);
    this.deApi.descargarXML(documento.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `DE-${documento.cdc}.xml`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.loading.set(false);
        this.snackBar.open('XML descargado correctamente', 'Cerrar', { duration: 2000 });
      },
      error: (error) => {
        this.snackBar.open('Error al descargar XML', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  verQR(documento: DocumentoElectronico): void {
    if (!documento.urlQr) {
      this.snackBar.open('El documento no tiene código QR generado', 'Cerrar', { duration: 3000 });
      return;
    }

    // Navigate to view component which will show the QR
    this.router.navigate(['/documentos', documento.id]);
  }

  cancelarDocumento(documento: DocumentoElectronico): void {
    if (documento.estado !== EstadoDE.APROBADO) {
      this.snackBar.open('Solo se pueden cancelar documentos aprobados', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(CancelarDeDialogComponent, {
      width: '500px',
      data: { documento }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(motivo => {
      if (motivo) {
        this.loading.set(true);
        this.deApi.cancelar(documento.id, motivo).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Solicitud de cancelación enviada', 'Cerrar', { duration: 3000 });
            this.cargarDocumentos();
          },
          error: (error) => {
            // Extraer el mensaje de error del backend
            let errorMessage = 'Error al cancelar documento';
            
            if (error.error?.message) {
              errorMessage = error.error.message;
            } else if (error.message) {
              errorMessage = error.message;
            }
            
            // Decodificar entidades HTML si existen (ej: &#243; -> ó)
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = errorMessage;
            errorMessage = tempDiv.textContent || tempDiv.innerText || errorMessage;
            
            this.snackBar.open(errorMessage, 'Cerrar', { duration: 7000 });
            this.loading.set(false);
          }
        });
      }
    });
  }
}
