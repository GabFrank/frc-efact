import { Component, OnInit, OnDestroy, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { BreakpointObserver } from '@angular/cdk/layout';
import { ChoferApiService } from '../../core/api/chofer-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { Chofer } from '../../models/chofer.model';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-choferes-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatChipsModule,
    MatTooltipModule,
    MatMenuModule,
    MatDividerModule,
    MatTableModule,
    MatPaginatorModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="choferes-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <div class="header-title-section">
                <button mat-icon-button (click)="goBack()" matTooltip="Volver a empresas">
                  <mat-icon>arrow_back</mat-icon>
                </button>
                <div class="title-wrapper">
                  <h2>Gestión de Chofers</h2>
                  <p class="empresa-subtitle" *ngIf="empresaNombre">{{ empresaNombre }}</p>
                </div>
              </div>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Formulario para agregar nuevo chofer -->
          <mat-card class="form-card" *ngIf="!loading()">
            <mat-card-header>
              <mat-card-title>{{ editingChofer() ? 'Editar Chofer' : 'Nuevo Chofer' }}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <form [formGroup]="choferForm" (ngSubmit)="guardarChofer()">
                <div class="form-row">
                  <mat-form-field appearance="outline">
                    <mat-label>Nombre</mat-label>
                    <input matInput formControlName="nombre" placeholder="Nombre del chofer" style="text-transform: uppercase" (input)="onTextInput($event, 'nombre')">
                    <app-error-message [control]="choferForm.get('nombre')" />
                  </mat-form-field>

                  <mat-form-field appearance="outline">
                    <mat-label>Documento</mat-label>
                    <input matInput formControlName="documento" placeholder="Número de documento" style="text-transform: uppercase" (input)="onTextInput($event, 'documento')">
                    <app-error-message [control]="choferForm.get('documento')" />
                  </mat-form-field>

                  <mat-form-field appearance="outline">
                    <mat-label>Dirección</mat-label>
                    <textarea matInput formControlName="direccion" placeholder="Dirección del chofer" rows="2"></textarea>
                    <app-error-message [control]="choferForm.get('direccion')" />
                  </mat-form-field>

                  <div class="form-actions">
                    <button mat-raised-button color="primary" type="submit" [disabled]="choferForm.invalid || saving()">
                      {{ saving() ? 'Guardando...' : (editingChofer() ? 'Actualizar' : 'Agregar') }}
                    </button>
                    <button mat-button type="button" (click)="cancelarEdicion()" *ngIf="editingChofer()">Cancelar</button>
                  </div>
                </div>
              </form>
            </mat-card-content>
          </mat-card>

          <!-- Filtros -->
          <div class="filters-section" *ngIf="!loading()">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Buscar chofer</mat-label>
              <input matInput
                     [(ngModel)]="searchTerm"
                     (input)="onSearchInput($event)"
                     (ngModelChange)="onSearchChange()"
                     placeholder="Nombre o documento"
                     [style.text-transform]="'uppercase'">
              <mat-icon matPrefix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Estado</mat-label>
              <mat-select [(ngModel)]="estadoFilter" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option [value]="true">Activos</mat-option>
                <mat-option [value]="false">Inactivos</mat-option>
              </mat-select>
            </mat-form-field>

            <button mat-stroked-button (click)="limpiarFiltros()">
              <mat-icon>clear</mat-icon>
              Limpiar
            </button>
          </div>

          <!-- Tabla de choferes -->
          <app-loading-spinner *ngIf="loading()" />

          <div class="list-desktop" *ngIf="!loading() && !isMobile()">
          <div class="table-wrapper">
            <table mat-table [dataSource]="dataSource" class="choferes-table">
              <!-- Nombre Column -->
              <ng-container matColumnDef="nombre">
                <th mat-header-cell *matHeaderCellDef>Nombre</th>
                <td mat-cell *matCellDef="let chofer">{{ chofer.nombre }}</td>
              </ng-container>

              <!-- Documento Column -->
              <ng-container matColumnDef="documento">
                <th mat-header-cell *matHeaderCellDef>Documento</th>
                <td mat-cell *matCellDef="let chofer">{{ chofer.documento || '-' }}</td>
              </ng-container>

              <!-- Dirección Column -->
              <ng-container matColumnDef="direccion">
                <th mat-header-cell *matHeaderCellDef>Dirección</th>
                <td mat-cell *matCellDef="let chofer">{{ chofer.direccion || '-' }}</td>
              </ng-container>

              <!-- Estado Column -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let chofer">
                  <mat-chip [class.active-chip]="chofer.activo" [class.inactive-chip]="!chofer.activo">
                    {{ chofer.activo ? 'Activo' : 'Inactivo' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let chofer">
                  <button
                    mat-icon-button
                    [matMenuTriggerFor]="actionsMenu"
                    matTooltip="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>

                  <mat-menu #actionsMenu="matMenu">
                    <button mat-menu-item (click)="editarChofer(chofer)">
                      <mat-icon>edit</mat-icon>
                      <span>Editar</span>
                    </button>
                    <mat-divider></mat-divider>
                    <button mat-menu-item (click)="eliminarChofer(chofer)" class="delete-option">
                      <mat-icon>delete</mat-icon>
                      <span>Eliminar</span>
                    </button>
                  </mat-menu>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

              <!-- No data row -->
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell" [attr.colspan]="displayedColumns.length">
                  <div class="no-data">
                    No hay choferes para mostrar
                  </div>
                </td>
              </tr>
            </table>

            <mat-paginator
              [length]="totalElements"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 20, 50, 100]"
              [pageIndex]="currentPage"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
          </div>
          </div>

          <div class="list-mobile" *ngIf="!loading() && isMobile()">
            <div class="mobile-cards" *ngIf="dataSource.data.length">
              <mat-card class="list-card" *ngFor="let item of dataSource.data">
                <mat-card-header class="list-card-header">
                  <mat-card-title class="list-card-title">
                    <span class="list-card-num">{{ item.nombre }}</span>
                    <span class="list-card-date">{{ item.documento || '—' }}</span>
                  </mat-card-title>
                  <button class="list-card-menu-trigger" mat-icon-button color="primary"
                    [matMenuTriggerFor]="cardActionMenu" (click)="setMenuContext(item)"
                    matTooltip="Acciones" aria-label="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>
                </mat-card-header>
                <mat-card-content class="list-card-content">
                  <div class="list-card-field">
                    <span class="list-card-label">Dirección</span>
                    <span class="list-card-value">{{ item.direccion || '—' }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Estado</span>
                    <span class="list-card-value">{{ item.activo ? 'Activo' : 'Inactivo' }}</span>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
            <div class="mobile-empty" *ngIf="!dataSource.data.length">No hay choferes para mostrar.</div>
            <mat-paginator
              *ngIf="dataSource.data.length"
              [length]="totalElements"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 20, 50, 100]"
              [pageIndex]="currentPage"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
            <mat-menu #cardActionMenu="matMenu" class="card-action-menu">
              <button mat-menu-item type="button" class="list-card-menu-item" (click)="menuRow && editarChofer(menuRow)">
                <mat-icon>edit</mat-icon>
                <span>Editar</span>
              </button>
              <mat-divider></mat-divider>
              <button mat-menu-item type="button" class="list-card-menu-item delete-option" (click)="menuRow && eliminarChofer(menuRow)">
                <mat-icon>delete</mat-icon>
                <span>Eliminar</span>
              </button>
            </mat-menu>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .chofers-container {
      padding: 20px;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      gap: 16px;
    }

    .header-title-section {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
    }

    .title-wrapper {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .title-wrapper h2 {
      margin: 0;
    }

    .empresa-subtitle {
      margin: 0;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.6);
      font-weight: normal;
    }

    .form-card {
      margin-bottom: 24px;
      background-color: #f5f5f5;
    }

    .form-row {
      display: flex;
      gap: 16px;
      align-items: flex-start;
      flex-wrap: wrap;
    }

    .form-actions {
      display: flex;
      gap: 8px;
      align-items: center;
      padding-top: 8px;
    }

    mat-form-field {
      flex: 1;
      min-width: 200px;
    }

    .filters-section {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      flex-wrap: wrap;
      align-items: center;
    }

    .search-field {
      flex: 1;
      min-width: 300px;
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

    .table-wrapper {
      overflow-x: auto;
    }

    .chofers-table {
      width: 100%;
      background: white;
    }

    mat-paginator {
      border-top: 1px solid rgba(0, 0, 0, 0.12);
    }

    .chofers-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .chofers-table td,
    .chofers-table th {
      padding: 12px 16px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    .inactive-chip {
      background-color: #f44336 !important;
      color: white;
    }

    .no-data {
      padding: 40px;
      text-align: center;
      color: rgba(0, 0, 0, 0.54);
    }

    .delete-option {
      color: #f44336;
    }

    .mat-mdc-menu-item mat-icon {
      margin-right: 8px;
    }
  `]
})
export class ChoferesListComponent implements OnInit, OnDestroy {
  loading = signal(false);
  saving = signal(false);
  choferes = signal<Chofer[]>([]);
  isMobile = signal(false);
  menuRow: Chofer | null = null;
  private breakpointObserver = inject(BreakpointObserver);
  dataSource = new MatTableDataSource<Chofer>([]);
  editingChofer = signal<Chofer | null>(null);

  empresaId: number | null = null;
  empresaNombre: string = '';

  searchTerm = '';
  estadoFilter: boolean | null = true;

  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  sortBy = 'nombre';
  sortDir: 'asc' | 'desc' = 'asc';

  displayedColumns: string[] = ['nombre', 'documento', 'direccion', 'activo', 'actions'];
  choferForm!: FormGroup;

  private destroy$ = new Subject<void>();

  constructor(
    private choferApi: ChoferApiService,
    private empresaApi: EmpresaApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute,
    private fb: FormBuilder
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.breakpointObserver.observe(['(max-width: 768px)']).pipe(takeUntil(this.destroy$)).subscribe(s => this.isMobile.set(s.matches));
    this.route.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = +params['empresaId'];
        if (id && !isNaN(id)) {
          this.empresaId = id;
          this.cargarInformacionEmpresa(id);
          this.cargarChoferes();
        } else {
          this.snackBar.open('ID de empresa no válido', 'Cerrar', { duration: 3000 });
          this.goBack();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initForm(): void {
    this.choferForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      documento: ['', [Validators.maxLength(20)]],
      direccion: ['']
    });
  }

  cargarInformacionEmpresa(empresaId: number): void {
    this.empresaApi.getById(empresaId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (empresa) => {
        this.empresaNombre = empresa.razonSocial;
      },
      error: (error) => {
        console.error('Error al cargar información de la empresa:', error);
      }
    });
  }

  cargarChoferes(): void {
    if (!this.empresaId) return;

    this.loading.set(true);

    this.choferApi.buscarConFiltros(this.empresaId, {
      q: this.searchTerm || undefined,
      activo: this.estadoFilter ?? undefined,
      page: this.currentPage,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDir: this.sortDir.toUpperCase() as 'ASC' | 'DESC'
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        const choferes = response.content || [];
        this.choferes.set(choferes);
        this.dataSource.data = choferes;
        this.totalElements = response.totalElements || 0;
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error al cargar choferes:', error);
        this.snackBar.open(`Error: ${error.error?.message || 'Error desconocido al cargar choferes'}`, 'Cerrar', { duration: 5000 });
        this.choferes.set([]);
        this.dataSource.data = [];
        this.totalElements = 0;
        this.loading.set(false);
      }
    });
  }

  setMenuContext(item: Chofer): void {
    this.menuRow = item;
  }

  goBack(): void {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'];
    if (returnUrl) {
      this.router.navigateByUrl(returnUrl);
    } else {
      this.router.navigate(['/empresas']);
    }
  }

  onSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase();
    this.searchTerm = input.value;
    this.onSearchChange();
  }

  onSearchChange(): void {
    this.currentPage = 0;
    this.cargarChoferes();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.cargarChoferes();
  }

  limpiarFiltros(): void {
    this.searchTerm = '';
    this.estadoFilter = true;
    this.currentPage = 0;
    this.cargarChoferes();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarChoferes();
  }

  onTextInput(event: Event, field: string): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.toUpperCase();
    this.choferForm.get(field)?.setValue(value, { emitEvent: false });
  }

  guardarChofer(): void {
    if (this.choferForm.invalid || !this.empresaId) {
      this.choferForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.choferForm.getRawValue();
    const chofer: Partial<Chofer> = {
      nombre: formValue.nombre,
      documento: formValue.documento || undefined,
      direccion: formValue.direccion || undefined
    };

    const editing = this.editingChofer();
    const request = editing && editing.id
      ? this.choferApi.update(this.empresaId, editing.id, chofer)
      : this.choferApi.create(this.empresaId, chofer);

    request.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open(
          `Chofer ${editing ? 'actualizado' : 'creado'} correctamente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.choferForm.reset();
        this.editingChofer.set(null);
        this.cargarChoferes();
      },
      error: (error) => {
        this.saving.set(false);
        this.snackBar.open(
          error.error?.message || `Error al ${editing ? 'actualizar' : 'crear'} chofer`,
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }

  cancelarEdicion(): void {
    this.choferForm.reset();
    this.editingChofer.set(null);
  }

  editarChofer(chofer: Chofer): void {
    this.editingChofer.set(chofer);
    this.choferForm.patchValue({
      nombre: chofer.nombre,
      documento: chofer.documento || '',
      direccion: chofer.direccion || ''
    });
    // Scroll al formulario
    document.querySelector('.form-card')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  eliminarChofer(chofer: Chofer): void {
    if (!this.empresaId || !chofer.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de eliminar el chofer "${chofer.nombre}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.choferApi.delete(this.empresaId!, chofer.id!).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Chofer eliminado correctamente', 'Cerrar', { duration: 3000 });
            this.cargarChoferes();
          },
          error: () => {
            this.snackBar.open('Error al eliminar chofer', 'Cerrar', { duration: 3000 });
            this.loading.set(false);
          }
        });
      }
    });
  }
}
