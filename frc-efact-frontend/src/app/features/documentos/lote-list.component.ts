import { Component, OnInit, signal, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Subject, takeUntil } from 'rxjs';
import { DocumentoElectronicoApiService } from '../../core/api/documento-electronico-api.service';
import { LoteDE, EstadoLote } from '../../models/documento-electronico.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-lote-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatChipsModule,
    MatMenuModule,
    MatTooltipModule,
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="lotes-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <h2>Lotes de Documentos Electrónicos</h2>
              <div class="header-actions">
                <button mat-stroked-button (click)="volver()">
                  <mat-icon>arrow_back</mat-icon>
                  Volver a Documentos
                </button>
                <button mat-raised-button color="primary" (click)="crearLote()">
                  <mat-icon>add</mat-icon>
                  Crear Lote
                </button>
              </div>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Estadísticas rápidas -->
          <div class="stats-section">
            <div class="stat-card">
              <div class="stat-value">{{ getCountByEstado(EstadoLote.PENDIENTE) }}</div>
              <div class="stat-label">Pendientes</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ getCountByEstado(EstadoLote.EN_PROCESO) }}</div>
              <div class="stat-label">En Proceso</div>
            </div>
            <div class="stat-card success">
              <div class="stat-value">{{ getCountByEstado(EstadoLote.APROBADO) }}</div>
              <div class="stat-label">Aprobados</div>
            </div>
            <div class="stat-card error">
              <div class="stat-value">{{ getCountByEstado(EstadoLote.RECHAZADO) }}</div>
              <div class="stat-label">Rechazados</div>
            </div>
          </div>

          <!-- Botón de actualizar -->
          <div class="actions-bar">
            <button mat-stroked-button (click)="cargarLotes()">
              <mat-icon>refresh</mat-icon>
              Actualizar
            </button>
          </div>

          <!-- Tabla de lotes -->
          <app-loading-spinner *ngIf="loading()" />

          <div class="list-desktop" *ngIf="!loading() && !isMobile()">
            <app-data-table
              [columns]="columns"
              [data]="lotes()"
              [actions]="tableActions"
              (actionClick)="onActionClick($event)"
            />
          </div>
          <div class="list-mobile" *ngIf="!loading() && isMobile()">
            <div class="mobile-cards" *ngIf="lotes().length > 0">
              <mat-card class="list-card" *ngFor="let item of lotes()">
                <mat-card-header class="list-card-header">
                  <mat-card-title class="list-card-title">
                    <span class="list-card-num">#{{ item.id }}</span>
                    <span class="list-card-date">{{ formatDateForCard(item.fechaProcesado) }}</span>
                  </mat-card-title>
                  <button class="list-card-menu-trigger" mat-icon-button color="primary"
                    [matMenuTriggerFor]="cardActionMenu" (click)="setMenuContext(item)"
                    matTooltip="Acciones" aria-label="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>
                </mat-card-header>
                <mat-card-content class="list-card-content">
                  <div class="list-card-field">
                    <span class="list-card-label">Empresa ID</span>
                    <span class="list-card-value">{{ item.empresaId }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Estado</span>
                    <span class="list-card-value">{{ getEstadoLabel(item.estado) }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Protocolo</span>
                    <span class="list-card-value">{{ item.protocolo || '—' }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Intentos</span>
                    <span class="list-card-value">{{ item.intentos }}</span>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
            <div class="mobile-empty" *ngIf="lotes().length === 0">No hay lotes para mostrar.</div>
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
    .lotes-container {
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

    .actions-bar {
      margin-bottom: 16px;
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

    .estado-ERROR {
      background-color: #f8d7da;
      color: #842029;
    }
  `]
})
export class LoteListComponent implements OnInit {
  loading = signal(false);
  lotes = signal<LoteDE[]>([]);
  private destroy$ = new Subject<void>();
  private breakpointObserver = inject(BreakpointObserver);
  isMobile = signal(false);
  menuRow: LoteDE | null = null;
  menuActions: TableAction[] = [];

  // Expose enum to template
  EstadoLote = EstadoLote;

  columns: TableColumn[] = [
    { 
      key: 'id', 
      label: 'ID', 
      sortable: true
    },
    { 
      key: 'empresaId', 
      label: 'Empresa', 
      sortable: true
    },
    {
      key: 'estado',
      label: 'Estado',
      sortable: true,
      format: (value: EstadoLote) => this.formatEstado(value)
    },
    { 
      key: 'protocolo', 
      label: 'Protocolo', 
      sortable: false,
      format: (value: string) => value || 'N/A'
    },
    { 
      key: 'intentos', 
      label: 'Intentos', 
      sortable: true
    },
    { 
      key: 'fechaProcesado', 
      label: 'Fecha Procesado', 
      sortable: true,
      format: (value: string) => value ? new Date(value).toLocaleDateString('es-PY') : '-'
    },
    { 
      key: 'fechaUltimoIntento', 
      label: 'Último Intento', 
      sortable: true,
      format: (value: string) => value ? new Date(value).toLocaleDateString('es-PY') : '-'
    }
  ];

  tableActions: TableAction[] = [
    { 
      icon: 'send', 
      label: 'Enviar a SIFEN', 
      color: 'primary',
      tooltip: 'Enviar lote a SIFEN',
      handler: (row: any) => this.enviarLote(row)
    },
    { 
      icon: 'refresh', 
      label: 'Consultar Estado', 
      color: 'accent',
      tooltip: 'Consultar estado del lote',
      handler: (row: any) => this.consultarEstado(row)
    },
    { 
      icon: 'visibility', 
      label: 'Ver Detalles', 
      color: 'primary',
      tooltip: 'Ver detalles del lote',
      handler: (row: any) => this.verDetalles(row)
    }
  ];

  constructor(
    private deApi: DocumentoElectronicoApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.breakpointObserver.observe(['(max-width: 768px)']).pipe(takeUntil(this.destroy$)).subscribe(s => this.isMobile.set(s.matches));
    this.cargarLotes();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  formatDateForCard(fecha: string | null | undefined): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-PY');
  }

  getVisibleActions(row: LoteDE | null): TableAction[] {
    return row && this.tableActions?.length ? this.tableActions.filter(a => !a.visible || a.visible(row)) : [];
  }

  setMenuContext(item: LoteDE): void {
    this.menuRow = item;
    this.menuActions = this.getVisibleActions(item);
  }

  cargarLotes(): void {
    this.loading.set(true);
    
    this.deApi.getAllLotes().subscribe({
      next: (lotes) => {
        this.lotes.set(lotes);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar lotes', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  formatEstado(estado: EstadoLote): string {
    const html = `<span class="estado-chip estado-${estado}">${this.getEstadoLabel(estado)}</span>`;
    return html;
  }

  getEstadoLabel(estado: EstadoLote): string {
    const labels: { [key in EstadoLote]: string } = {
      [EstadoLote.PENDIENTE]: '⏳ Pendiente',
      [EstadoLote.EN_PROCESO]: '🔄 En Proceso',
      [EstadoLote.APROBADO]: '✅ Aprobado',
      [EstadoLote.RECHAZADO]: '❌ Rechazado',
      [EstadoLote.ERROR]: '⚠️ Error'
    };
    return labels[estado];
  }

  getCountByEstado(estado: EstadoLote): number {
    return this.lotes().filter(l => l.estado === estado).length;
  }

  crearLote(): void {
    this.router.navigate(['/documentos/lotes/nuevo']);
  }

  volver(): void {
    this.router.navigate(['/documentos/lista']);
  }

  onActionClick(event: { action: string; row: any }): void {
    const lote = event.row as LoteDE;
    
    switch (event.action) {
      case 'Enviar a SIFEN':
        this.enviarLote(lote);
        break;
      case 'Consultar Estado':
        this.consultarEstado(lote);
        break;
      case 'Ver Detalles':
        this.verDetalles(lote);
        break;
    }
  }

  enviarLote(lote: LoteDE): void {
    if (lote.estado !== EstadoLote.PENDIENTE && lote.estado !== EstadoLote.ERROR) {
      this.snackBar.open('Solo se pueden enviar lotes pendientes o con error', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Enviar Lote a SIFEN',
        message: `¿Desea enviar el lote #${lote.id} a SIFEN?`,
        confirmText: 'Enviar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.deApi.enviarLote(lote.id).subscribe({
          next: () => {
            this.snackBar.open('Lote enviado a SIFEN correctamente', 'Cerrar', { duration: 3000 });
            this.cargarLotes();
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al enviar lote',
              'Cerrar',
              { duration: 5000 }
            );
            this.loading.set(false);
          }
        });
      }
    });
  }

  consultarEstado(lote: LoteDE): void {
    if (!lote.protocolo) {
      this.snackBar.open('El lote no tiene protocolo asignado', 'Cerrar', { duration: 3000 });
      return;
    }

    this.loading.set(true);
    this.deApi.consultarEstadoLote(lote.id).subscribe({
      next: (loteActualizado) => {
        this.snackBar.open('Estado del lote actualizado', 'Cerrar', { duration: 3000 });
        this.cargarLotes();
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

  verDetalles(lote: LoteDE): void {
    // TODO: Implement details view
    this.snackBar.open('Vista de detalles en desarrollo', 'Cerrar', { duration: 2000 });
  }
}
