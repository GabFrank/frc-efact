import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Store } from '@ngrx/store';

import { Timbrado } from '../../models/timbrado.model';
import { TimbradosActions } from '../../core/state/timbrados';
import {
  selectAllTimbrados,
  selectTimbradosLoading,
  selectTimbradosError
} from '../../core/state/timbrados/timbrados.selectors';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-empresa-timbrados',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatMenuModule,
    MatDividerModule,
    ReactiveFormsModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="timbrados-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <button mat-icon-button (click)="goBack()" matTooltip="Volver a empresas">
                <mat-icon>arrow_back</mat-icon>
              </button>
              <h2>Gestión de Timbrados - {{ empresaNombre }}</h2>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Filters Section -->
          <div class="filters-section">
            <form [formGroup]="filterForm" class="filters-form">
              <mat-form-field appearance="outline" class="filter-field">
                <mat-label>Buscar por número</mat-label>
                <input matInput formControlName="numero" placeholder="Número de timbrado">
                <mat-icon matSuffix>search</mat-icon>
              </mat-form-field>

              <mat-form-field appearance="outline" class="filter-field">
                <mat-label>Fecha desde</mat-label>
                <input matInput [matDatepicker]="fechaDesdePicker" formControlName="fechaDesde">
                <mat-datepicker-toggle matIconSuffix [for]="fechaDesdePicker"></mat-datepicker-toggle>
                <mat-datepicker #fechaDesdePicker></mat-datepicker>
              </mat-form-field>

              <mat-form-field appearance="outline" class="filter-field">
                <mat-label>Fecha hasta</mat-label>
                <input matInput [matDatepicker]="fechaHastaPicker" formControlName="fechaHasta">
                <mat-datepicker-toggle matIconSuffix [for]="fechaHastaPicker"></mat-datepicker-toggle>
                <mat-datepicker #fechaHastaPicker></mat-datepicker>
              </mat-form-field>

              <mat-form-field appearance="outline" class="filter-field">
                <mat-label>Tipo</mat-label>
                <mat-select formControlName="isElectronico">
                  <mat-option [value]="null">Todos</mat-option>
                  <mat-option [value]="true">Electrónico</mat-option>
                  <mat-option [value]="false">Manual</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" class="filter-field">
                <mat-label>Estado</mat-label>
                <mat-select formControlName="activo">
                  <mat-option [value]="null">Todos</mat-option>
                  <mat-option [value]="true">Activo</mat-option>
                  <mat-option [value]="false">Inactivo</mat-option>
                </mat-select>
              </mat-form-field>
            </form>
          </div>

          <!-- Actions Bar -->
          <div class="actions-bar">
            <button
              mat-raised-button
              color="primary"
              (click)="onCreateTimbrado()">
              <mat-icon>add</mat-icon>
              Nuevo Timbrado
            </button>
          </div>

          <!-- Loading State -->
          <app-loading-spinner *ngIf="loading$ | async"></app-loading-spinner>

          <!-- Error State -->
          <app-error-message
            *ngIf="error$ | async as error"
            [message]="error">
          </app-error-message>

          <!-- Timbrados Table -->
          <div class="table-container" *ngIf="!(loading$ | async) && !(error$ | async)">
            <table mat-table [dataSource]="paginatedTimbrados" class="timbrados-table">

              <!-- Numero Column -->
              <ng-container matColumnDef="numero">
                <th mat-header-cell *matHeaderCellDef>Número</th>
                <td mat-cell *matCellDef="let timbrado">{{ timbrado.numero }}</td>
              </ng-container>

              <!-- Fecha Inicio Column -->
              <ng-container matColumnDef="fechaInicio">
                <th mat-header-cell *matHeaderCellDef>Fecha Inicio</th>
                <td mat-cell *matCellDef="let timbrado">{{ timbrado.fechaInicio | date:'dd/MM/yyyy' }}</td>
              </ng-container>

              <!-- Fecha Fin Column -->
              <ng-container matColumnDef="fechaFin">
                <th mat-header-cell *matHeaderCellDef>Fecha Fin</th>
                <td mat-cell *matCellDef="let timbrado">{{ timbrado.fechaFin | date:'dd/MM/yyyy' }}</td>
              </ng-container>

              <!-- Is Electronico Column -->
              <ng-container matColumnDef="isElectronico">
                <th mat-header-cell *matHeaderCellDef>Tipo</th>
                <td mat-cell *matCellDef="let timbrado">
                  <mat-chip [class.electronico-chip]="timbrado.isElectronico"
                           [class.manual-chip]="!timbrado.isElectronico">
                    {{ timbrado.isElectronico ? 'Electrónico' : 'Manual' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let timbrado">
                  <button
                    mat-icon-button
                    [matMenuTriggerFor]="actionsMenu"
                    matTooltip="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>

                  <mat-menu #actionsMenu="matMenu">
                    <button mat-menu-item (click)="onViewTimbrado(timbrado)">
                      <mat-icon>visibility</mat-icon>
                      <span>Ver detalles</span>
                    </button>
                    <button mat-menu-item (click)="onEditTimbrado(timbrado)">
                      <mat-icon>edit</mat-icon>
                      <span>Editar</span>
                    </button>
                    <mat-divider></mat-divider>
                    <button
                      mat-menu-item
                      (click)="onToggleActive(timbrado)"
                      [class.deactivate-option]="timbrado.activo"
                      [class.activate-option]="!timbrado.activo">
                      <mat-icon>{{ timbrado.activo ? 'block' : 'check_circle' }}</mat-icon>
                      <span>{{ timbrado.activo ? 'Desactivar' : 'Activar' }}</span>
                    </button>
                    <button
                      mat-menu-item
                      (click)="onDeleteTimbrado(timbrado)"
                      class="delete-option">
                      <mat-icon>delete</mat-icon>
                      <span>Eliminar</span>
                    </button>
                  </mat-menu>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

              <!-- No Data Row -->
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell no-data" [attr.colspan]="displayedColumns.length">
                  <div class="no-data-message">
                    <mat-icon>receipt</mat-icon>
                    <p>No se encontraron timbrados</p>
                    <button mat-raised-button color="primary" (click)="onCreateTimbrado()">
                      <mat-icon>add</mat-icon>
                      Crear primer timbrado
                    </button>
                  </div>
                </td>
              </tr>
            </table>

            <!-- Paginator -->
            <mat-paginator
              [length]="filteredTimbrados.length"
              [pageSize]="pageSize"
              [pageSizeOptions]="[5, 10, 25, 50]"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .timbrados-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    mat-card {
      margin-bottom: 20px;
    }

    mat-card-header {
      margin-bottom: 20px;
    }

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }

    .filters-section {
      margin-bottom: 20px;
    }

    .filters-form {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      align-items: end;
    }

    .filter-field {
      width: 100%;
    }

    .actions-bar {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 20px;
    }

    .table-container {
      overflow-x: auto;
    }

    .timbrados-table {
      width: 100%;
      background: white;
    }

    .timbrados-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .timbrados-table td,
    .timbrados-table th {
      padding: 12px 16px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    .electronico-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    .manual-chip {
      background-color: #ff9800 !important;
      color: white;
    }

    .no-data {
      text-align: center;
      padding: 40px !important;
    }

    .no-data-message {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      color: #666;
    }

    .no-data-message mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }

    .no-data-message p {
      margin: 0;
      font-size: 16px;
    }

    /* Menu styles */
    .mat-mdc-menu-item {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .mat-mdc-menu-item mat-icon {
      margin-right: 0;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .deactivate-option {
      color: #f44336;
    }

    .activate-option {
      color: #4caf50;
    }

    .delete-option {
      color: #f44336;
    }

    @media (max-width: 768px) {
      .timbrados-container {
        padding: 10px;
      }

      .filters-form {
        grid-template-columns: 1fr;
      }

      .actions-bar {
        justify-content: center;
      }
    }
  `]
})
export class EmpresaTimbradosComponent implements OnInit, OnDestroy {
  empresaId!: number;
  empresaNombre: string = '';
  displayedColumns: string[] = ['numero', 'fechaInicio', 'fechaFin', 'isElectronico', 'actions'];

  // Pagination
  pageSize = 10;
  currentPage = 0;
  paginatedTimbrados: Timbrado[] = [];

  // Filters
  filterForm: FormGroup;
  filteredTimbrados: Timbrado[] = [];

  // Observables
  timbrados$ = this.store.select(selectAllTimbrados);
  loading$ = this.store.select(selectTimbradosLoading);
  error$ = this.store.select(selectTimbradosError);

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      numero: [''],
      fechaDesde: [null],
      fechaHasta: [null],
      isElectronico: [null],
      activo: [null]
    });
  }

  ngOnInit(): void {
    // Obtener empresaId de la ruta
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.empresaId = +params['id'];
      this.loadTimbradosEmpresa();
    });

    // Suscribirse a cambios en timbrados
    this.timbrados$.pipe(takeUntil(this.destroy$)).subscribe(timbrados => {
      this.filteredTimbrados = this.applyFilters(timbrados);
      this.updatePagination();
    });

    // Suscribirse a cambios en filtros
    this.filterForm.valueChanges.pipe(
      takeUntil(this.destroy$),
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.timbrados$.pipe(takeUntil(this.destroy$)).subscribe(timbrados => {
        this.filteredTimbrados = this.applyFilters(timbrados);
        this.currentPage = 0; // Reset to first page
        this.updatePagination();
      });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goBack(): void {
    this.router.navigate(['/empresas']);
  }

  private loadTimbradosEmpresa(): void {
    this.store.dispatch(TimbradosActions.loadTimbradosByEmpresa({ empresaId: this.empresaId }));
    // TODO: Load empresa name - could be from route data or separate call
    this.empresaNombre = `Empresa ${this.empresaId}`; // Placeholder
  }

  private applyFilters(timbrados: Timbrado[]): Timbrado[] {
    const filters = this.filterForm.value;

    return timbrados.filter(timbrado => {
      // Filter by numero
      if (filters.numero && !timbrado.numero.toLowerCase().includes(filters.numero.toLowerCase())) {
        return false;
      }

      // Filter by fechaDesde
      if (filters.fechaDesde) {
        const fechaInicio = new Date(timbrado.fechaInicio);
        const fechaDesde = new Date(filters.fechaDesde);
        if (fechaInicio < fechaDesde) {
          return false;
        }
      }

      // Filter by fechaHasta
      if (filters.fechaHasta) {
        const fechaFin = new Date(timbrado.fechaFin);
        const fechaHasta = new Date(filters.fechaHasta);
        if (fechaFin > fechaHasta) {
          return false;
        }
      }

      // Filter by isElectronico
      if (filters.isElectronico !== null && timbrado.isElectronico !== filters.isElectronico) {
        return false;
      }

      // Filter by activo
      if (filters.activo !== null && timbrado.activo !== filters.activo) {
        return false;
      }

      return true;
    });
  }

  private updatePagination(): void {
    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedTimbrados = this.filteredTimbrados.slice(startIndex, endIndex);
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updatePagination();
  }

  onCreateTimbrado(): void {
    // TODO: Implement create timbrado dialog
    console.log('Crear nuevo timbrado');
  }

  onViewTimbrado(timbrado: Timbrado): void {
    // TODO: Implement view timbrado details
    console.log('Ver timbrado:', timbrado);
  }

  onEditTimbrado(timbrado: Timbrado): void {
    // TODO: Implement edit timbrado dialog
    console.log('Editar timbrado:', timbrado);
  }

  onToggleActive(timbrado: Timbrado): void {
    const action = timbrado.activo ? 'desactivar' : 'activar';
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${action.charAt(0).toUpperCase() + action.slice(1)} Timbrado`,
        message: `¿Está seguro que desea ${action} el timbrado "${timbrado.numero}"?`,
        confirmText: action.charAt(0).toUpperCase() + action.slice(1),
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.store.dispatch(TimbradosActions.updateTimbrado({
          id: timbrado.id,
          timbrado: { activo: !timbrado.activo }
        }));
      }
    });
  }

  onDeleteTimbrado(timbrado: Timbrado): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar Timbrado',
        message: `¿Está seguro que desea eliminar el timbrado "${timbrado.numero}"? Esta acción no se puede deshacer.`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar',
        confirmColor: 'warn'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.store.dispatch(TimbradosActions.deleteTimbrado({ id: timbrado.id }));
      }
    });
  }
}
