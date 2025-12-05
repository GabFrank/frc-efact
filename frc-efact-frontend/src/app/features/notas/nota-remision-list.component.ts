import { Component, OnInit, signal, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { Subject, takeUntil } from 'rxjs';
import { NotaRemisionApiService } from '../../core/api/nota-remision-api.service';
import { NotaRemision } from '../../models/nota.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { NotaRemisionFormDialogComponent } from './nota-remision-form-dialog.component';

@Component({
  selector: 'app-nota-remision-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    DataTableComponent,
    LoadingSpinnerComponent
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
          <app-loading-spinner *ngIf="loading()" />
          <app-data-table
            *ngIf="!loading()"
            [columns]="columns"
            [data]="notasPaginas()"
            [actions]="tableActions"
            [pageSize]="pageSize"
            [pageIndex]="pageIndex"
            [totalItems]="totalItems()"
            (actionClick)="onActionClick($event)"
            (pageChange)="onPageChange($event)"
          />
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .notas-container { padding: 24px; }
    .header-actions { margin-bottom: 16px; }
  `]
})
export class NotaRemisionListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  notas = signal<NotaRemision[]>([]);
  notasPaginas = signal<NotaRemision[]>([]);
  loading = signal(false);
  pageSize = 20;
  pageIndex = 0;
  totalItems = signal(0);

  columns: TableColumn[] = [
    { key: 'numeroFormateado', label: 'Número', sortable: true },
    { key: 'fecha', label: 'Fecha', sortable: true, format: (v: string) => new Date(v).toLocaleDateString('es-PY') },
    { key: 'nombreDestinatario', label: 'Destinatario', sortable: true },
    { key: 'motivoEmision', label: 'Motivo', sortable: true }
  ];

  tableActions: TableAction[] = [
    { icon: 'edit', label: 'Editar', tooltip: 'Editar nota de remisión' },
    { icon: 'delete', label: 'Eliminar', color: 'warn', tooltip: 'Eliminar nota de remisión' }
  ];

  constructor(
    private notaRemisionApi: NotaRemisionApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['empresaId']) this.cargarNotas(+params['empresaId']);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarNotas(empresaId: number): void {
    this.loading.set(true);
    this.notaRemisionApi.getAll(empresaId, this.pageIndex, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => {
          this.notas.set(r.content);
          this.totalItems.set(r.totalElements);
          this.actualizarPaginacion();
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Error al cargar notas de remisión', 'Cerrar', { duration: 3000 });
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

  crearNotaRemision(): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    const dialogRef = this.dialog.open(NotaRemisionFormDialogComponent, {
      width: '1000px',
      data: { empresaId: empresaId ? +empresaId : undefined }
    });
    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(r => {
      if (r && empresaId) this.cargarNotas(+empresaId);
    });
  }

  onActionClick(e: { action: string; row: any }): void {
    const nota = e.row as NotaRemision;
    if (e.action === 'Editar') this.editarNota(nota);
    else if (e.action === 'Eliminar') this.eliminarNota(nota);
  }

  editarNota(nota: NotaRemision): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    const dialogRef = this.dialog.open(NotaRemisionFormDialogComponent, {
      width: '1000px',
      data: { nota, empresaId: empresaId ? +empresaId : undefined }
    });
    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(r => {
      if (r && empresaId) this.cargarNotas(+empresaId);
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
}

