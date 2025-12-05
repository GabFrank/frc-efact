import { Component, OnInit, signal, computed, OnDestroy } from '@angular/core';
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
import { PageEvent } from '@angular/material/paginator';
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
    .notas-container {
      padding: 24px;
    }
    .header-actions {
      margin-bottom: 16px;
    }
  `]
})
export class NotaCreditoListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

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
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const empresaId = params['empresaId'];
      if (empresaId) {
        this.cargarNotas(+empresaId);
      }
    });
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

