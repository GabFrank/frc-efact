import { Component, OnInit, signal, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Subject, takeUntil } from 'rxjs';
import { NotaDebitoApiService } from '../../core/api/nota-debito-api.service';
import { NotaDebito } from '../../models/nota.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { NotaDebitoFormDialogComponent } from './nota-debito-form-dialog.component';

@Component({
  selector: 'app-nota-debito-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
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
          <mat-card-title><h2>Notas de Débito</h2></mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="header-actions">
            <button mat-raised-button color="primary" (click)="crearNotaDebito()">
              <mat-icon>add</mat-icon>
              Nueva Nota de Débito
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
            <div class="mobile-empty" *ngIf="notasPaginas().length === 0">No hay notas de débito para mostrar.</div>
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
export class NotaDebitoListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private breakpointObserver = inject(BreakpointObserver);
  isMobile = signal(false);
  menuRow: NotaDebito | null = null;
  menuActions: TableAction[] = [];
  notas = signal<NotaDebito[]>([]);
  notasPaginas = signal<NotaDebito[]>([]);
  loading = signal(false);
  pageSize = 20;
  pageIndex = 0;
  totalItems = signal(0);

  columns: TableColumn[] = [
    { key: 'numeroFormateado', label: 'Número', sortable: true },
    { key: 'fecha', label: 'Fecha', sortable: true, format: (v: string) => new Date(v).toLocaleDateString('es-PY') },
    { key: 'nombre', label: 'Cliente', sortable: true },
    { key: 'totalFinal', label: 'Total', sortable: true, format: (v: number) => `₲ ${v.toLocaleString('es-PY')}` }
  ];

  tableActions: TableAction[] = [
    { icon: 'edit', label: 'Editar', tooltip: 'Editar nota de débito' },
    { icon: 'delete', label: 'Eliminar', color: 'warn', tooltip: 'Eliminar nota de débito' }
  ];

  constructor(
    private notaDebitoApi: NotaDebitoApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.breakpointObserver.observe(['(max-width: 768px)']).pipe(takeUntil(this.destroy$)).subscribe(s => this.isMobile.set(s.matches));
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['empresaId']) this.cargarNotas(+params['empresaId']);
    });
  }

  formatDateForCard(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-PY');
  }

  formatTotal(v: number): string {
    return v != null ? `₲ ${Number(v).toLocaleString('es-PY')}` : '—';
  }

  getVisibleActions(row: NotaDebito | null): TableAction[] {
    return row && this.tableActions?.length ? this.tableActions.filter(a => !a.visible || a.visible(row)) : [];
  }

  setMenuContext(item: NotaDebito): void {
    this.menuRow = item;
    this.menuActions = this.getVisibleActions(item);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarNotas(empresaId: number): void {
    this.loading.set(true);
    this.notaDebitoApi.getAll(empresaId, this.pageIndex, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => {
          this.notas.set(r.content);
          this.totalItems.set(r.totalElements);
          this.actualizarPaginacion();
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Error al cargar notas de débito', 'Cerrar', { duration: 3000 });
          this.loading.set(false);
        }
      });
  }

  actualizarPaginacion(): void {
    const todas = this.notas();
    const start = this.pageIndex * this.pageSize;
    this.notasPaginas.set(todas.slice(start, start + this.pageSize));
  }

  onPageChange(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    if (empresaId) this.cargarNotas(+empresaId);
  }

  crearNotaDebito(): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    const dialogRef = this.dialog.open(NotaDebitoFormDialogComponent, {
      width: '900px',
      data: { empresaId: empresaId ? +empresaId : undefined }
    });
    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(r => {
      if (r && empresaId) this.cargarNotas(+empresaId);
    });
  }

  onActionClick(e: { action: string; row: any }): void {
    const nota = e.row as NotaDebito;
    if (e.action === 'Editar') this.editarNota(nota);
    else if (e.action === 'Eliminar') this.eliminarNota(nota);
  }

  editarNota(nota: NotaDebito): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    const dialogRef = this.dialog.open(NotaDebitoFormDialogComponent, {
      width: '900px',
      data: { nota, empresaId: empresaId ? +empresaId : undefined }
    });
    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(r => {
      if (r && empresaId) this.cargarNotas(+empresaId);
    });
  }

  eliminarNota(nota: NotaDebito): void {
    if (!nota.id) return;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar Nota de Débito',
        message: `¿Está seguro de eliminar la nota de débito ${nota.numeroFormateado}?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });
    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && nota.id) {
        this.notaDebitoApi.delete(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Nota de débito eliminada', 'Cerrar', { duration: 3000 });
            const empresaId = this.route.snapshot.queryParams['empresaId'];
            if (empresaId) this.cargarNotas(+empresaId);
          },
          error: () => this.snackBar.open('Error al eliminar', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }
}

