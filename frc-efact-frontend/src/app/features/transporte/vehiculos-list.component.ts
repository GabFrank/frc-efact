import { Component, OnInit, OnDestroy, signal } from '@angular/core';
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
import { VehiculoApiService } from '../../core/api/vehiculo-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { Vehiculo } from '../../models/vehiculo.model';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-vehiculos-list',
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
    <div class="vehiculos-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <div class="header-title-section">
                <button mat-icon-button (click)="goBack()" matTooltip="Volver a empresas">
                  <mat-icon>arrow_back</mat-icon>
                </button>
                <div class="title-wrapper">
                  <h2>Gestión de Vehículos</h2>
                  <p class="empresa-subtitle" *ngIf="empresaNombre">{{ empresaNombre }}</p>
                </div>
              </div>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Formulario para agregar nuevo vehículo -->
          <mat-card class="form-card" *ngIf="!loading()">
            <mat-card-header>
              <mat-card-title>{{ editingVehiculo() ? 'Editar Vehículo' : 'Nuevo Vehículo' }}</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <form [formGroup]="vehiculoForm" (ngSubmit)="guardarVehiculo()">
                <div class="form-row">
                  <mat-form-field appearance="outline">
                    <mat-label>Marca</mat-label>
                    <input matInput formControlName="marca" placeholder="Marca del vehículo" style="text-transform: uppercase" (input)="onTextInput($event, 'marca')">
                    <app-error-message [control]="vehiculoForm.get('marca')" />
                  </mat-form-field>

                  <mat-form-field appearance="outline">
                    <mat-label>Matrícula</mat-label>
                    <input matInput formControlName="matricula" placeholder="Matrícula del vehículo" style="text-transform: uppercase" (input)="onTextInput($event, 'matricula')">
                    <app-error-message [control]="vehiculoForm.get('matricula')" />
                  </mat-form-field>

                  <div class="form-actions">
                    <button mat-raised-button color="primary" type="submit" [disabled]="vehiculoForm.invalid || saving()">
                      {{ saving() ? 'Guardando...' : (editingVehiculo() ? 'Actualizar' : 'Agregar') }}
                    </button>
                    <button mat-button type="button" (click)="cancelarEdicion()" *ngIf="editingVehiculo()">Cancelar</button>
                  </div>
                </div>
              </form>
            </mat-card-content>
          </mat-card>

          <!-- Filtros -->
          <div class="filters-section" *ngIf="!loading()">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Buscar vehículo</mat-label>
              <input matInput
                     [(ngModel)]="searchTerm"
                     (input)="onSearchInput($event)"
                     (ngModelChange)="onSearchChange()"
                     placeholder="Matrícula o marca"
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

          <!-- Tabla de vehículos -->
          <app-loading-spinner *ngIf="loading()" />

          <div *ngIf="!loading()" class="table-wrapper">
            <table mat-table [dataSource]="dataSource" class="vehiculos-table">
              <!-- Matrícula Column -->
              <ng-container matColumnDef="matricula">
                <th mat-header-cell *matHeaderCellDef>Matrícula</th>
                <td mat-cell *matCellDef="let vehiculo">{{ vehiculo.matricula }}</td>
              </ng-container>

              <!-- Marca Column -->
              <ng-container matColumnDef="marca">
                <th mat-header-cell *matHeaderCellDef>Marca</th>
                <td mat-cell *matCellDef="let vehiculo">{{ vehiculo.marca }}</td>
              </ng-container>

              <!-- Estado Column -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let vehiculo">
                  <mat-chip [class.active-chip]="vehiculo.activo" [class.inactive-chip]="!vehiculo.activo">
                    {{ vehiculo.activo ? 'Activo' : 'Inactivo' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let vehiculo">
                  <button
                    mat-icon-button
                    [matMenuTriggerFor]="actionsMenu"
                    matTooltip="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>

                  <mat-menu #actionsMenu="matMenu">
                    <button mat-menu-item (click)="editarVehiculo(vehiculo)">
                      <mat-icon>edit</mat-icon>
                      <span>Editar</span>
                    </button>
                    <mat-divider></mat-divider>
                    <button mat-menu-item (click)="eliminarVehiculo(vehiculo)" class="delete-option">
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
                    No hay vehículos para mostrar
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
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .vehiculos-container {
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

    .table-wrapper {
      overflow-x: auto;
    }

    .vehiculos-table {
      width: 100%;
      background: white;
    }

    mat-paginator {
      border-top: 1px solid rgba(0, 0, 0, 0.12);
    }

    .vehiculos-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .vehiculos-table td,
    .vehiculos-table th {
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
export class VehiculosListComponent implements OnInit, OnDestroy {
  loading = signal(false);
  saving = signal(false);
  vehiculos = signal<Vehiculo[]>([]);
  dataSource = new MatTableDataSource<Vehiculo>([]);
  editingVehiculo = signal<Vehiculo | null>(null);

  empresaId: number | null = null;
  empresaNombre: string = '';

  searchTerm = '';
  estadoFilter: boolean | null = true;

  currentPage = 0;
  pageSize = 20;
  totalElements = 0;
  sortBy = 'matricula';
  sortDir: 'asc' | 'desc' = 'asc';

  displayedColumns: string[] = ['matricula', 'marca', 'activo', 'actions'];
  vehiculoForm!: FormGroup;

  private destroy$ = new Subject<void>();

  constructor(
    private vehiculoApi: VehiculoApiService,
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
    this.route.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = +params['empresaId'];
        if (id && !isNaN(id)) {
          this.empresaId = id;
          this.cargarInformacionEmpresa(id);
          this.cargarVehiculos();
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
    this.vehiculoForm = this.fb.group({
      marca: ['', [Validators.required, Validators.maxLength(100)]],
      matricula: ['', [Validators.required, Validators.maxLength(20)]]
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

  cargarVehiculos(): void {
    if (!this.empresaId) return;

    this.loading.set(true);

    this.vehiculoApi.buscarConFiltros(this.empresaId, {
      q: this.searchTerm || undefined,
      activo: this.estadoFilter ?? undefined,
      page: this.currentPage,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDir: this.sortDir.toUpperCase() as 'ASC' | 'DESC'
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        const vehiculos = response.content || [];
        this.vehiculos.set(vehiculos);
        this.dataSource.data = vehiculos;
        this.totalElements = response.totalElements || 0;
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error al cargar vehículos:', error);
        this.snackBar.open(`Error: ${error.error?.message || 'Error desconocido al cargar vehículos'}`, 'Cerrar', { duration: 5000 });
        this.vehiculos.set([]);
        this.dataSource.data = [];
        this.totalElements = 0;
        this.loading.set(false);
      }
    });
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
    this.cargarVehiculos();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.cargarVehiculos();
  }

  limpiarFiltros(): void {
    this.searchTerm = '';
    this.estadoFilter = true;
    this.currentPage = 0;
    this.cargarVehiculos();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.cargarVehiculos();
  }

  onTextInput(event: Event, field: string): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.toUpperCase();
    this.vehiculoForm.get(field)?.setValue(value, { emitEvent: false });
  }

  guardarVehiculo(): void {
    if (this.vehiculoForm.invalid || !this.empresaId) {
      this.vehiculoForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.vehiculoForm.getRawValue();
    const vehiculo: Partial<Vehiculo> = {
      marca: formValue.marca,
      matricula: formValue.matricula
    };

    const editing = this.editingVehiculo();
    const request = editing && editing.id
      ? this.vehiculoApi.update(this.empresaId, editing.id, vehiculo)
      : this.vehiculoApi.create(this.empresaId, vehiculo);

    request.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open(
          `Vehículo ${editing ? 'actualizado' : 'creado'} correctamente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.vehiculoForm.reset();
        this.editingVehiculo.set(null);
        this.cargarVehiculos();
      },
      error: (error) => {
        this.saving.set(false);
        this.snackBar.open(
          error.error?.message || `Error al ${editing ? 'actualizar' : 'crear'} vehículo`,
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }

  cancelarEdicion(): void {
    this.vehiculoForm.reset();
    this.editingVehiculo.set(null);
  }

  editarVehiculo(vehiculo: Vehiculo): void {
    this.editingVehiculo.set(vehiculo);
    this.vehiculoForm.patchValue({
      marca: vehiculo.marca,
      matricula: vehiculo.matricula
    });
    // Scroll al formulario
    document.querySelector('.form-card')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  eliminarVehiculo(vehiculo: Vehiculo): void {
    if (!this.empresaId || !vehiculo.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de eliminar el vehículo con matrícula "${vehiculo.matricula}"?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed) {
        this.loading.set(true);
        this.vehiculoApi.delete(this.empresaId!, vehiculo.id!).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Vehículo eliminado correctamente', 'Cerrar', { duration: 3000 });
            this.cargarVehiculos();
          },
          error: () => {
            this.snackBar.open('Error al eliminar vehículo', 'Cerrar', { duration: 3000 });
            this.loading.set(false);
          }
        });
      }
    });
  }
}
