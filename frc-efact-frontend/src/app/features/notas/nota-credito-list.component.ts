import { Component, OnInit, signal, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Subject, takeUntil } from 'rxjs';
import { NotaCreditoApiService } from '../../core/api/nota-credito-api.service';
import { NotaCredito } from '../../models/nota.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { NotaCreditoFormDialogComponent } from './nota-credito-form-dialog.component';
import { NotaCreditoEstadoDialogComponent } from '../../shared/components/nota-credito-estado-dialog/nota-credito-estado-dialog.component';

@Component({
  selector: 'app-nota-credito-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatMenuModule,
    MatTooltipModule,
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="notas-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>Notas de Crédito</h2>
          </mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="header-actions">
            <button mat-raised-button color="primary" (click)="crearNotaCredito()">
              <mat-icon>add</mat-icon>
              Nueva Nota de Crédito
            </button>
          </div>

          <app-loading-spinner *ngIf="loading()" />

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
          <div class="list-mobile" *ngIf="!loading() && isMobile()">
            <div class="mobile-cards" *ngIf="notasPaginas().length > 0">
              <mat-card class="list-card" *ngFor="let item of notasPaginas()">
                <mat-card-header class="list-card-header">
                  <mat-card-title class="list-card-title">
                    <span class="list-card-num">{{ item.numeroFormateado }}</span>
                    <span class="list-card-date">{{ formatDateForCard(item.fecha) }}</span>
                  </mat-card-title>
                  <button class="list-card-menu-trigger" mat-icon-button color="primary"
                    [matMenuTriggerFor]="cardActionMenu" (click)="setMenuContext(item)"
                    matTooltip="Acciones" aria-label="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>
                </mat-card-header>
                <mat-card-content class="list-card-content">
                  <div class="list-card-field">
                    <span class="list-card-label">Cliente</span>
                    <span class="list-card-value">{{ item.nombre || '—' }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Total</span>
                    <span class="list-card-value">{{ formatTotal(item.totalFinal) }}</span>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
            <div class="mobile-empty" *ngIf="notasPaginas().length === 0">No hay notas de crédito para mostrar.</div>
            <mat-paginator *ngIf="notasPaginas().length > 0" [length]="totalItems()" [pageSize]="pageSize"
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
    .notas-container { padding: 24px; }
    .header-actions { margin-bottom: 16px; }
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
  `]
})
export class NotaCreditoListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private breakpointObserver = inject(BreakpointObserver);
  isMobile = signal(false);
  menuRow: NotaCredito | null = null;
  menuActions: TableAction[] = [];

  notas = signal<NotaCredito[]>([]);
  notasPaginas = signal<NotaCredito[]>([]);
  loading = signal(false);
  pageSize = 20;
  pageIndex = 0;
  totalItems = signal(0);

  columns: TableColumn[] = [
    { key: 'numeroFormateado', label: 'Número', sortable: true },
    { 
      key: 'fecha', 
      label: 'Fecha', 
      sortable: true,
      format: (value: string) => new Date(value).toLocaleDateString('es-PY')
    },
    { key: 'nombre', label: 'Cliente', sortable: true },
    { 
      key: 'totalFinal', 
      label: 'Total', 
      sortable: true,
      format: (value: number) => `₲ ${value.toLocaleString('es-PY')}`
    }
  ];

  tableActions: TableAction[] = [
    { icon: 'info', label: 'Ver estado', tooltip: 'Ver estado completo de nota de crédito, DE y lote' },
    { icon: 'edit', label: 'Editar', tooltip: 'Editar nota de crédito' },
    { icon: 'delete', label: 'Eliminar', color: 'warn', tooltip: 'Eliminar nota de crédito' }
  ];

  constructor(
    private notaCreditoApi: NotaCreditoApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.breakpointObserver.observe(['(max-width: 768px)']).pipe(takeUntil(this.destroy$)).subscribe(s => this.isMobile.set(s.matches));
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const empresaId = params['empresaId'];
      if (empresaId) {
        this.cargarNotas(+empresaId);
      }
    });
  }

  formatDateForCard(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-PY');
  }

  formatTotal(v: number): string {
    return v != null ? `₲ ${Number(v).toLocaleString('es-PY')}` : '—';
  }

  getVisibleActions(row: NotaCredito | null): TableAction[] {
    return row && this.tableActions?.length ? this.tableActions.filter(a => !a.visible || a.visible(row)) : [];
  }

  setMenuContext(item: NotaCredito): void {
    this.menuRow = item;
    this.menuActions = this.getVisibleActions(item);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarNotas(empresaId: number): void {
    this.loading.set(true);
    this.notaCreditoApi.getAll(empresaId, this.pageIndex, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.notas.set(response.content);
          this.totalItems.set(response.totalElements);
          this.actualizarPaginacion();
          this.loading.set(false);
        },
        error: (error) => {
          this.snackBar.open('Error al cargar notas de crédito', 'Cerrar', { duration: 3000 });
          this.loading.set(false);
        }
      });
  }

  actualizarPaginacion(): void {
    const todas = this.notas();
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.notasPaginas.set(todas.slice(startIndex, endIndex));
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    if (empresaId) {
      this.cargarNotas(+empresaId);
    }
  }

  crearNotaCredito(): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    const dialogRef = this.dialog.open(NotaCreditoFormDialogComponent, {
      width: '900px',
      data: { empresaId: empresaId ? +empresaId : undefined }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result && empresaId) {
        this.cargarNotas(+empresaId);
      }
    });
  }

  onActionClick(event: { action: string; row: any }): void {
    const nota = event.row as NotaCredito;

    switch (event.action) {
      case 'Ver estado':
        this.verEstado(nota);
        break;
      case 'Editar':
        this.editarNota(nota);
        break;
      case 'Eliminar':
        this.eliminarNota(nota);
        break;
    }
  }

  verEstado(nota: NotaCredito): void {
    this.dialog.open(NotaCreditoEstadoDialogComponent, {
      width: '800px',
      maxWidth: '90vw',
      data: { nota },
      disableClose: false
    });
  }

  editarNota(nota: NotaCredito): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    const dialogRef = this.dialog.open(NotaCreditoFormDialogComponent, {
      width: '900px',
      data: { nota, empresaId: empresaId ? +empresaId : undefined }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result && empresaId) {
        this.cargarNotas(+empresaId);
      }
    });
  }

  eliminarNota(nota: NotaCredito): void {
    if (!nota.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar Nota de Crédito',
        message: `¿Está seguro de eliminar la nota de crédito ${nota.numeroFormateado}?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && nota.id) {
        this.notaCreditoApi.delete(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Nota de crédito eliminada', 'Cerrar', { duration: 3000 });
            const empresaId = this.route.snapshot.queryParams['empresaId'];
            if (empresaId) {
              this.cargarNotas(+empresaId);
            }
          },
          error: () => {
            this.snackBar.open('Error al eliminar nota de crédito', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }
}

