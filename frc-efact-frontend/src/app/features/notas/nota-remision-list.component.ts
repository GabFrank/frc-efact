import { Component, OnInit, signal, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotaRemisionApiService } from '../../core/api/nota-remision-api.service';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { NotaRemision } from '../../models/nota.model';
import { Cliente } from '../../models/cliente.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { CancelarDeDialogComponent } from '../documentos/cancelar-de-dialog.component';
import { SifenApiService } from '../../core/api/sifen-api.service';
import { NotaRemisionEstadoDialogComponent } from '../../shared/components/nota-remision-estado-dialog/nota-remision-estado-dialog.component';
import { EmailEnviarDialogComponent } from '../../shared/components/email-enviar-dialog/email-enviar-dialog.component';

@Component({
  selector: 'app-nota-remision-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatExpansionModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatMenuModule,
    DataTableComponent,
    LoadingSpinnerComponent,
    EmailEnviarDialogComponent
  ],
  template: `
    <div class="notas-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title><h2>Notas de Remisión</h2></mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="header-actions">
            <button mat-raised-button color="primary" (click)="crearNotaRemision()">
              <mat-icon>add</mat-icon>
              Nueva Nota de Remisión
            </button>
          </div>

          <mat-expansion-panel class="filter-panel" [expanded]="true">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <mat-icon>filter_list</mat-icon> Filtros de Búsqueda
              </mat-panel-title>
            </mat-expansion-panel-header>

            <form [formGroup]="filterForm" class="filter-form">
              <div class="filter-row">
                <mat-form-field appearance="outline">
                  <mat-label>Número de Nota</mat-label>
                  <input matInput formControlName="numero" placeholder="Ej: 001-001-0000001">
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Fecha Desde</mat-label>
                  <input matInput [matDatepicker]="pickerDesde" formControlName="fechaDesde">
                  <mat-datepicker-toggle matSuffix [for]="pickerDesde"></mat-datepicker-toggle>
                  <mat-datepicker #pickerDesde></mat-datepicker>
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Fecha Hasta</mat-label>
                  <input matInput [matDatepicker]="pickerHasta" formControlName="fechaHasta">
                  <mat-datepicker-toggle matSuffix [for]="pickerHasta"></mat-datepicker-toggle>
                  <mat-datepicker #pickerHasta></mat-datepicker>
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Estado DE</mat-label>
                  <mat-select formControlName="estadoDE">
                    <mat-option value="">Todos</mat-option>
                    <mat-option value="PENDIENTE">⏳ Pendiente</mat-option>
                    <mat-option value="APROBADO">✅ Aprobado</mat-option>
                    <mat-option value="RECHAZADO">❌ Rechazado</mat-option>
                    <mat-option value="CANCELADO">🚫 Cancelado</mat-option>
                    <mat-option value="SIN_DE">📄 Sin DE</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>

              <div class="filter-row">
                <mat-form-field appearance="outline">
                  <mat-label>Destinatario</mat-label>
                  <input matInput formControlName="destinatario" placeholder="Nombre o RUC">
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Motivo</mat-label>
                  <input matInput formControlName="motivo">
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Vehículo</mat-label>
                  <input matInput formControlName="vehiculo" placeholder="Matrícula o Marca">
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Chofer</mat-label>
                  <input matInput formControlName="chofer" placeholder="Nombre o Doc">
                </mat-form-field>
              </div>

              <div class="filter-actions">
                <button mat-button color="warn" (click)="limpiarFiltros()">Limpiar Filtros</button>
              </div>
            </form>
          </mat-expansion-panel>

          <app-loading-spinner [loading]="loading()" />

          <!-- Desktop: tabla -->
          <div class="list-desktop" *ngIf="!loading() && !isMobile()">
            <app-data-table
              [columns]="columns"
              [data]="notasPaginas()"
              [actions]="tableActions"
              [pageSize]="pageSize"
              [pageIndex]="pageIndex"
              [totalItems]="totalItems()"
              (actionClick)="onActionClick($event)"
              (pageChange)="onPageChange($event)"
            />
          </div>

          <!-- Mobile: cards -->
          <div class="list-mobile" *ngIf="!loading() && isMobile()">
            <div class="mobile-cards" *ngIf="notasPaginas().length > 0">
              <mat-card class="nota-card" *ngFor="let nota of notasPaginas()">
                <mat-card-header class="nota-card-header">
                  <mat-card-title class="nota-card-title">
                    <span class="nota-numero">{{ nota.numeroFormateado }}</span>
                    <span class="nota-fecha">{{ formatDateForCard(nota.fecha) }}</span>
                  </mat-card-title>
                  <button
                    class="nota-card-menu-trigger"
                    mat-icon-button
                    color="primary"
                    [matMenuTriggerFor]="cardActionMenu"
                    (click)="setMenuContext(nota)"
                    matTooltip="Acciones"
                    aria-label="Acciones"
                  >
                    <mat-icon>more_vert</mat-icon>
                  </button>
                </mat-card-header>
                <mat-card-content class="nota-card-content">
                  <div class="nota-field">
                    <span class="nota-label">Destinatario</span>
                    <span class="nota-value">{{ nota.nombreDestinatario || '—' }}</span>
                  </div>
                  <div class="nota-field">
                    <span class="nota-label">Motivo</span>
                    <span class="nota-value">{{ formatMotivoEmision(nota.motivoEmision) || '—' }}</span>
                  </div>
                  <div class="nota-field">
                    <span class="nota-label">Estado DE</span>
                    <span class="nota-value">{{ getEstadoDELabel(nota.estadoDocumentoElectronico) }}</span>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
            <div class="mobile-empty" *ngIf="notasPaginas().length === 0">
              No hay notas de remisión para mostrar.
            </div>
            <mat-paginator
              *ngIf="notasPaginas().length > 0"
              [length]="totalItems()"
              [pageSize]="pageSize"
              [pageIndex]="pageIndex"
              [pageSizeOptions]="[5, 10, 25, 50]"
              (page)="onPageChange($event)"
              showFirstLastButtons
            ></mat-paginator>
            <mat-menu #cardActionMenu="matMenu" class="card-action-menu">
              <button
                mat-menu-item
                *ngFor="let a of menuActions"
                type="button"
                class="nota-remision-card-menu-item"
                (click)="menuRow && onActionClick({ action: (a.label || a.tooltip || a.icon) || '', row: menuRow })"
              >
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
    .notas-container { padding: 24px; }
    .header-actions { margin-bottom: 16px; }
    .filter-panel { margin-bottom: 24px; }
    .filter-form { display: flex; flex-direction: column; gap: 8px; padding-top: 8px; }
    .filter-row { display: flex; flex-wrap: wrap; gap: 16px; }
    .filter-row mat-form-field { flex: 1; min-width: 200px; }
    .filter-actions { display: flex; justify-content: flex-end; }

    mat-card-header {
      display: flex;
      margin-bottom: 16px;
    }
    mat-card-title,
    mat-card-title h2 {
      margin: 0;
      font-size: 1.5rem;
      font-weight: 600;
      color: #2c3e50;
    }
    :host-context(body.dark-theme) mat-card-title,
    :host-context(body.dark-theme) mat-card-title h2 {
      color: #e0e0e0;
    }

    .list-desktop { width: 100%; }
    .list-mobile { width: 100%; }

    .mobile-cards {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 16px;
    }
    .nota-card { margin: 0; }
    .nota-card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      margin-bottom: 0;
    }
    .nota-card-menu-trigger {
      flex-shrink: 0;
    }
    .nota-card-menu-trigger .mat-icon {
      font-size: 1.5rem;
      width: 24px;
      height: 24px;
    }
    .nota-card-title {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      gap: 2px;
      margin: 0;
      font-size: 1rem;
    }
    .nota-numero { font-weight: 600; color: #2c3e50; }
    .nota-fecha { font-size: 0.875rem; color: rgba(0,0,0,0.6); }
    :host-context(body.dark-theme) .nota-numero { color: #e0e0e0; }
    :host-context(body.dark-theme) .nota-fecha { color: rgba(255,255,255,0.6); }
    .nota-card-content {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding-top: 0;
    }
    .nota-field {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    .nota-label {
      font-size: 0.75rem;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: rgba(0,0,0,0.6);
    }
    .nota-value {
      font-size: 0.9375rem;
      color: #2c3e50;
      word-break: break-word;
    }
    :host-context(body.dark-theme) .nota-label { color: rgba(255,255,255,0.6); }
    :host-context(body.dark-theme) .nota-value { color: #e0e0e0; }
    .mobile-empty {
      padding: 24px 16px;
      text-align: center;
      color: rgba(0,0,0,0.6);
    }
    :host-context(body.dark-theme) .mobile-empty { color: rgba(255,255,255,0.6); }
    .list-mobile mat-paginator {
      border-top: 1px solid rgba(0,0,0,0.12);
    }
    :host-context(body.dark-theme) .list-mobile mat-paginator {
      border-top-color: rgba(255,255,255,0.12);
    }
  `]
})
export class NotaRemisionListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private fb = inject(FormBuilder);

  filterForm = this.fb.group({
    numero: [''],
    fechaDesde: [null as Date | null],
    fechaHasta: [null as Date | null],
    motivo: [''],
    destinatario: [''],
    vehiculo: [''],
    chofer: [''],
    estadoDE: ['']
  });

  notas = signal<NotaRemision[]>([]);
  notasPaginas = signal<NotaRemision[]>([]);
  loading = signal(false);
  pageSize = 20;
  pageIndex = 0;
  totalItems = signal(0);

  motivosEmision = [
    { id: '1', descripcion: 'Traslado por ventas' },
    { id: '2', descripcion: 'Traslado por compras' },
    { id: '3', descripcion: 'Traslado por devolución' },
    { id: '4', descripcion: 'Traslado por exportación' },
    { id: '5', descripcion: 'Traslado por importación' },
    { id: '6', descripcion: 'Traslado por consignación' },
    { id: '7', descripcion: 'Traslado entre locales de la misma empresa' },
    { id: '8', descripcion: 'Traslado por ferias' },
    { id: '9', descripcion: 'Traslado por reparación' },
    { id: '10', descripcion: 'Traslado por entrega de productos en carácter de préstamo' },
    { id: '11', descripcion: 'Traslado por exhibición' },
    { id: '12', descripcion: 'Traslado por publicidad' },
    { id: '13', descripcion: 'Traslado por transformación' },
    { id: '14', descripcion: 'Traslado por recolección de productos' },
    { id: '99', descripcion: 'Otros' }
  ];

  columns: TableColumn[] = [
    { key: 'numeroFormateado', label: 'Número', sortable: true },
    { key: 'fecha', label: 'Fecha', sortable: true, format: (v: string) => new Date(v).toLocaleDateString('es-PY') },
    { key: 'nombreDestinatario', label: 'Destinatario', sortable: true },
    { 
      key: 'motivoEmision', 
      label: 'Motivo', 
      sortable: true,
      format: (v: string) => this.formatMotivoEmision(v)
    },
    {
      key: 'estadoDocumentoElectronico',
      label: 'Estado DE',
      format: (v: string, row: any) => this.formatEstadoDE(v || 'SIN_DE', row)
    }
  ];

  tableActions: TableAction[] = [
    // Grupo: Información
    {
      icon: 'info',
      label: 'Ver estado',
      color: 'primary',
      tooltip: 'Ver estado completo de nota de remisión, DE y lote',
      group: 'Información'
    },
    {
      icon: 'picture_as_pdf',
      label: 'Abrir PDF',
      color: 'primary',
      tooltip: 'Abrir PDF del KUDE',
      visible: (row: any) => row.documentoElectronicoId != null,
      group: 'Información'
    },
    {
      icon: 'email',
      label: 'Enviar vía email',
      color: 'primary',
      tooltip: 'Enviar documento electrónico por correo al destinatario',
      visible: (row: any) => row.documentoElectronicoId != null,
      group: 'Información'
    },
    // Grupo: Gestión NRE
    {
      icon: 'description',
      label: 'Generar DE',
      color: 'accent',
      tooltip: 'Generar y enviar documento electrónico a SIFEN',
      visible: (row: any) => !row.documentoElectronicoId,
      group: 'Gestión NRE'
    },
    {
      icon: 'cancel',
      label: 'Cancelar',
      color: 'warn',
      tooltip: 'Cancelar documento electrónico aprobado',
      visible: (row: any) => row.estadoDocumentoElectronico === 'APROBADO',
      group: 'Gestión NRE'
    },
    {
      icon: 'delete',
      label: 'Eliminar',
      color: 'warn',
      tooltip: 'Eliminar nota de remisión',
      visible: (row: any) => !row.documentoElectronicoId,
      group: 'Gestión NRE'
    },
    {
      icon: 'content_copy',
      label: 'Copiar nota',
      color: 'primary',
      tooltip: 'Copiar nota de remisión con todos sus datos',
      group: 'Gestión NRE'
    },
    // Grupo: SIFEN
    {
      icon: 'sync',
      label: 'Consultar SIFEN',
      color: 'primary',
      tooltip: 'Consultar estado en SIFEN (por lote o documento)',
      visible: (row: any) => !!row.cdcDocumentoElectronico || !!row.loteDeId,
      group: 'SIFEN'
    },
    {
      icon: 'search',
      label: 'Consultar por CDC',
      color: 'primary',
      tooltip: 'Consultar documento electrónico directamente por CDC',
      visible: (row: any) => !!row.cdcDocumentoElectronico,
      group: 'SIFEN'
    },
    {
      icon: 'link',
      label: 'Vincular a lote',
      color: 'accent',
      tooltip: 'Crear lote y enviar a SIFEN (para DEs generados sin lote)',
      visible: (row: any) => !!row.documentoElectronicoId && !row.loteDeId,
      group: 'SIFEN'
    }
  ];

  isMobile = signal(false);
  menuRow: NotaRemision | null = null;
  menuActions: TableAction[] = [];
  private breakpointObserver = inject(BreakpointObserver);

  constructor(
    private notaRemisionApi: NotaRemisionApiService,
    private clienteApi: ClienteApiService,
    private sifenApi: SifenApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.breakpointObserver
      .observe([ '(max-width: 768px)' ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(state => this.isMobile.set(state.matches));

    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const empresaId = params['empresaId'];
      if (empresaId) {
        this.cargarNotas(+empresaId);
      }
    });

    // Escuchar cambios en los filtros con debounce para no saturar el backend
    this.filterForm.valueChanges.pipe(
      takeUntil(this.destroy$),
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => {
      this.pageIndex = 0; // Reiniciar a la primera página al filtrar
      const empresaId = this.route.snapshot.queryParams['empresaId'];
      if (empresaId) this.cargarNotas(+empresaId);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarNotas(empresaId: number): void {
    this.loading.set(true);

    // Preparar filtros para el backend (formatear fechas si es necesario)
    const formValue = this.filterForm.value;
    const filters = {
      ...formValue,
      fechaDesde: formValue.fechaDesde ? this.formatDate(formValue.fechaDesde) : null,
      fechaHasta: formValue.fechaHasta ? this.formatDate(formValue.fechaHasta) : null
    };

    this.notaRemisionApi.getAll(empresaId, this.pageIndex, this.pageSize, filters)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => {
          this.notas.set(r.content);
          this.notasPaginas.set(r.content); // En paginación de servidor, la página es la data completa recibida
          this.totalItems.set(r.totalElements);
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Error al cargar notas de remisión', 'Cerrar', { duration: 3000 });
          this.loading.set(false);
        }
      });
  }

  formatDate(date: Date): string {
    const d = new Date(date);
    let month = '' + (d.getMonth() + 1);
    let day = '' + d.getDate();
    const year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
  }

  limpiarFiltros(): void {
    this.filterForm.reset({
      numero: '',
      fechaDesde: null,
      fechaHasta: null,
      motivo: '',
      destinatario: '',
      vehiculo: '',
      chofer: '',
      estadoDE: ''
    });
  }

  onPageChange(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    if (empresaId) this.cargarNotas(+empresaId);
  }

  crearNotaRemision(): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    this.router.navigate(['/notas/notas-remision/nueva'], {
      queryParams: { empresaId: empresaId ? +empresaId : undefined }
    });
  }

  onActionClick(e: { action: string; row: any }): void {
    const nota = e.row as NotaRemision;
    switch (e.action) {
      case 'Ver estado':
        this.verEstado(nota);
        break;
             case 'Abrir PDF':
               this.abrirPdfKude(nota);
               break;
             case 'Enviar vía email':
               this.enviarEmailViaEmail(nota);
               break;
             case 'Generar DE':
        this.generarDE(nota);
        break;
      case 'Cancelar':
        this.cancelarDE(nota);
        break;
      case 'Eliminar':
        this.eliminarNota(nota);
        break;
      case 'Consultar SIFEN':
        this.consultarSifen(nota);
        break;
      case 'Consultar por CDC':
        this.consultarPorCdc(nota);
        break;
      case 'Vincular a lote':
        this.vincularLote(nota);
        break;
      case 'Copiar nota':
        this.copiarNota(nota);
        break;
    }
  }

  vincularLote(nota: NotaRemision): void {
    if (!nota.id) return;

    this.loading.set(true);
    this.snackBar.open('Vinculando a lote y enviando a SIFEN...', 'Cerrar', { duration: 2000 });

    this.notaRemisionApi.vincularLote(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (result) => {
        this.snackBar.open('Lote creado y enviado exitosamente', 'Cerrar', { duration: 4000 });
        this.recargarYMostrarEstado(nota);
        this.loading.set(false);
      },
      error: (err) => {
        this.snackBar.open(`Error al vincular lote: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
        this.loading.set(false);
      }
    });
  }

  consultarSifen(nota: NotaRemision): void {
    if (nota.loteDeId) {
      // Consultar lote
      this.snackBar.open('Consultando estado del lote...', 'Cerrar', { duration: 2000 });
      this.sifenApi.consultarLote(nota.loteDeId).pipe(takeUntil(this.destroy$)).subscribe({
        next: (lote) => {
          this.snackBar.open(`Lote consultado. Estado: ${lote.estado}`, 'Cerrar', { duration: 3000 });
          // Recargar nota y abrir diálogo de estado
          this.recargarYMostrarEstado(nota);
        },
        error: (err) => {
          this.snackBar.open(`Error al consultar lote: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
        }
      });
    } else if (nota.cdcDocumentoElectronico) {
      // Consultar documento individual
      this.snackBar.open('Consultando estado del documento...', 'Cerrar', { duration: 2000 });
      this.sifenApi.consultarDocumento(nota.cdcDocumentoElectronico).pipe(takeUntil(this.destroy$)).subscribe({
        next: (doc) => {
          this.snackBar.open(`Documento consultado. Estado: ${doc.estado}`, 'Cerrar', { duration: 3000 });
          // Recargar nota y abrir diálogo de estado
          this.recargarYMostrarEstado(nota);
        },
        error: (err) => {
          console.error('❌ ERROR al consultar documento:', err);
          this.snackBar.open(`Error al consultar documento: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
        }
      });
    } else {
      this.snackBar.open('Esta nota de remisión no tiene DE asociado para consultar', 'Cerrar', { duration: 3000 });
    }
  }

  consultarPorCdc(nota: NotaRemision): void {
    if (!nota.cdcDocumentoElectronico) {
      this.snackBar.open('Esta nota de remisión no tiene CDC para consultar', 'Cerrar', { duration: 3000 });
      return;
    }

    this.snackBar.open('Consultando documento por CDC...', 'Cerrar', { duration: 2000 });
    this.sifenApi.consultarDocumento(nota.cdcDocumentoElectronico).pipe(takeUntil(this.destroy$)).subscribe({
      next: (doc) => {
        this.snackBar.open(`Documento consultado. Estado: ${doc.estado}`, 'Cerrar', { duration: 3000 });
        // Recargar nota y abrir diálogo de estado
        this.recargarYMostrarEstado(nota);
      },
      error: (err) => {
        console.error('❌ ERROR al consultar documento por CDC:', err);
        this.snackBar.open(`Error al consultar documento: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
      }
    });
  }

  private recargarYMostrarEstado(nota: NotaRemision): void {
    // Recargar la nota para obtener datos actualizados
    if (nota.id) {
      this.notaRemisionApi.getById(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: (notaActualizada) => {
          // Actualizar la nota en la lista
          const notas = this.notas();
          const index = notas.findIndex(n => n.id === nota.id);
          if (index !== -1) {
            notas[index] = notaActualizada;
            this.notas.set([...notas]);
            this.notasPaginas.set([...notas]);
          }
          // Abrir diálogo de estado con datos actualizados
          this.verEstado(notaActualizada);
        },
        error: () => {
          // Si falla la recarga, mostrar estado con los datos que tenemos
          this.verEstado(nota);
        }
      });
    } else {
      // Si no hay ID, mostrar estado con los datos que tenemos
      this.verEstado(nota);
    }
  }

  cancelarDE(nota: NotaRemision): void {
    if (!nota.cdcDocumentoElectronico) return;

    const dialogRef = this.dialog.open(CancelarDeDialogComponent, {
      width: '500px',
      data: {
        factura: nota,
        cdc: nota.cdcDocumentoElectronico
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result && result.cdc && result.motivo) {
        this.loading.set(true);
        this.sifenApi.cancelarDocumento(result.cdc, result.motivo).subscribe({
          next: () => {
            this.snackBar.open('Nota de remisión cancelada exitosamente', 'Cerrar', { duration: 4000 });
            const empresaId = this.route.snapshot.queryParams['empresaId'];
            if (empresaId) this.cargarNotas(+empresaId);
            else this.loading.set(false);
          },
          error: (error) => {
            this.snackBar.open(error.error?.message || 'Error al cancelar', 'Cerrar', { duration: 5000 });
            this.loading.set(false);
          }
        });
      }
    });
  }

  formatMotivoEmision(motivoId: string): string {
    if (!motivoId) return '';
    const motivo = this.motivosEmision.find(m => m.id === motivoId);
    return motivo ? motivo.descripcion : motivoId;
  }

  formatDateForCard(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-PY');
  }

  getEstadoDELabel(estado: string | null | undefined): string {
    const map: { [key: string]: string } = {
      'PENDIENTE': '⏳ Pendiente',
      'EN_PROCESO': '🔄 En Proceso',
      'APROBADO': '✅ Aprobado',
      'RECHAZADO': '❌ Rechazado',
      'CANCELADO': '🚫 Cancelado',
      'ERROR': '⚠️ Error',
      'SIN_DE': '📄 Sin DE'
    };
    return map[estado || ''] || (estado || '—');
  }

  getVisibleActions(row: NotaRemision | null): TableAction[] {
    if (!row || !this.tableActions?.length) return [];
    return this.tableActions.filter(a => !a.visible || a.visible(row));
  }

  setMenuContext(nota: NotaRemision): void {
    this.menuRow = nota;
    this.menuActions = this.getVisibleActions(nota);
  }

  formatEstadoDE(estado: string, row: any): string {
    const estadoMap: { [key: string]: string } = {
      'PENDIENTE': '⏳ Pendiente',
      'EN_PROCESO': '🔄 En Proceso',
      'APROBADO': '✅ Aprobado',
      'RECHAZADO': '❌ Rechazado',
      'CANCELADO': '🚫 Cancelado',
      'ERROR': '⚠️ Error',
      'SIN_DE': '📄 Sin DE'
    };
    const estadoText = estadoMap[estado] || estado;

    if (row?.urlQrDocumentoElectronico && estado !== 'SIN_DE') {
      const urlQr = row.urlQrDocumentoElectronico;
      const urlEscaped = urlQr.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
      return `${estadoText} <a href="${urlEscaped}" target="_blank" class="qr-link" title="Abrir consulta en SIFEN" onclick="event.stopPropagation(); return true;" style="vertical-align: middle; margin-left: 6px; display: inline-block; color: #1976d2; text-decoration: none; font-size: 18px;">🔗</a>`;
    }

    return estadoText;
  }

  generarDE(nota: NotaRemision): void {
    if (!nota.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Generar Documento Electrónico',
        message: `¿Desea generar y enviar el documento electrónico para la nota de remisión ${nota.numeroFormateado}?`,
        confirmText: 'Generar y Enviar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && nota.id) {
        this.loading.set(true);
        this.notaRemisionApi.generarYEnviar(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Documento electrónico enviado a SIFEN', 'Cerrar', { duration: 4000 });
            const empresaId = this.route.snapshot.queryParams['empresaId'];
            if (empresaId) this.cargarNotas(+empresaId);
            else this.loading.set(false);
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al generar el documento electrónico',
              'Cerrar',
              { duration: 5000 }
            );
            this.loading.set(false);
          }
        });
      }
    });
  }

  editarNota(nota: NotaRemision): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    this.router.navigate(['/notas/notas-remision', nota.id], {
      queryParams: { empresaId: empresaId ? +empresaId : undefined }
    });
  }

  copiarNota(nota: NotaRemision): void {
    if (!nota.id) return;
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    this.router.navigate(['/notas/notas-remision/nueva'], {
      queryParams: {
        empresaId: empresaId ? +empresaId : undefined,
        copyFromId: nota.id
      }
    });
  }

  eliminarNota(nota: NotaRemision): void {
    if (!nota.id) return;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar Nota de Remisión',
        message: `¿Está seguro de eliminar la nota de remisión ${nota.numeroFormateado}?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });
    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && nota.id) {
        this.notaRemisionApi.delete(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Nota de remisión eliminada', 'Cerrar', { duration: 3000 });
            const empresaId = this.route.snapshot.queryParams['empresaId'];
            if (empresaId) this.cargarNotas(+empresaId);
          },
          error: () => this.snackBar.open('Error al eliminar', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }

  verEstado(nota: NotaRemision): void {
    this.dialog.open(NotaRemisionEstadoDialogComponent, {
      width: '800px',
      maxWidth: '90vw',
      data: { notaRemision: nota },
      disableClose: false
    });
  }

  abrirPdfKude(nota: NotaRemision): void {
    if (!nota.id) return;

    if (!nota.documentoElectronicoId) {
      this.snackBar.open('Esta nota de remisión no tiene documento electrónico asociado', 'Cerrar', { duration: 3000 });
      return;
    }

    const token = localStorage.getItem('auth_token');
    const url = `${environment.apiUrl}/notas-remision/${nota.id}/kude-pdf?token=${token}`;

    // Abrir en nueva pestaña directamente desde la URL del servidor
    // Esto permite que el navegador maneje el nombre del archivo correctamente
    // gracias al header Content-Disposition: inline; filename="..."
    const win = window.open(url, '_blank');
    if (win) {
      win.focus();
      this.snackBar.open('Abriendo PDF...', 'Cerrar', { duration: 2000 });
    } else {
      this.snackBar.open('Por favor, permite las ventanas emergentes para ver el PDF', 'Cerrar', { duration: 5000 });
    }
  }

         enviarEmailViaEmail(nota: NotaRemision): void {
           if (!nota.id) return;

           const dialogRef = this.dialog.open(EmailEnviarDialogComponent, {
             width: '500px',
             data: {
               titulo: 'Enviar Nota de Remisión por Email',
               clienteNombre: nota.nombreDestinatario || nota.nombreCliente || 'Cliente',
               emailActual: (nota as any).emailCliente
             }
           });

           dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
             if (result) {
               this.loading.set(true);
               this.snackBar.open('Enviando email...', 'Cerrar', { duration: 2000 });

               // Enviar el email sin actualizar el cliente todavía
               this.notaRemisionApi.enviarEmail(nota.id!, { ...result, actualizarCliente: false }).pipe(takeUntil(this.destroy$)).subscribe({
                 next: () => {
                   this.snackBar.open('Email enviado exitosamente', 'Cerrar', { duration: 3000 });
                   this.loading.set(false);

                   // Consultar si desea actualizar el cliente
                   const emailUsado = result.email;
                   const emailActual = (nota as any).emailCliente;

                   if (nota.clienteId && emailUsado !== emailActual) {
                     const confirmRef = this.dialog.open(ConfirmDialogComponent, {
                       data: {
                         title: 'Actualizar Cliente',
                         message: `¿Desea actualizar el correo del cliente a "${emailUsado}"?`,
                         confirmText: 'Actualizar',
                         cancelText: 'No'
                       }
                     });

                     confirmRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirm => {
                       if (confirm) {
                         this.actualizarEmailCliente(nota.empresaId, nota.clienteId!, emailUsado);
                       }
                     });
                   }
                 },
                 error: (err) => {
                   console.error('Error al enviar email:', err);
                   this.snackBar.open(
                     `Error al enviar email: ${err.error?.error || err.error?.message || err.message}`,
                     'Cerrar',
                     { duration: 5000 }
                   );
                   this.loading.set(false);
                 }
               });
             }
           });
         }

         actualizarEmailCliente(empresaId: number, clienteId: number, nuevoEmail: string): void {
           this.clienteApi.getById(empresaId, clienteId).pipe(takeUntil(this.destroy$)).subscribe({
             next: (cliente: Cliente) => {
               cliente.email = nuevoEmail;
               this.clienteApi.update(empresaId, clienteId, cliente).pipe(takeUntil(this.destroy$)).subscribe({
                 next: () => {
                   this.snackBar.open('Correo del cliente actualizado correctamente', 'Cerrar', { duration: 3000 });
                   // Recargar para ver el cambio si es necesario
                   this.cargarNotas(empresaId);
                 },
                 error: (err) => {
                   console.error('Error al actualizar cliente:', err);
                   this.snackBar.open('No se pudo actualizar el correo del cliente', 'Cerrar', { duration: 3000 });
                 }
               });
             }
           });
         }
       }

