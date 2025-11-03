import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { FormsModule } from '@angular/forms';

import { Empresa } from '../../models/empresa.model';
import { EmpresasActions } from '../../core/state/empresas/empresas.actions';
import {
  selectAllEmpresas,
  selectEmpresasLoading,
  selectEmpresasError
} from '../../core/state/empresas/empresas.selectors';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-empresas-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCardModule,
    MatTooltipModule,
    MatChipsModule,
    MatDialogModule,
    MatMenuModule,
    MatDividerModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="empresas-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>Gestión de Empresas</h2>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Search and Actions Bar -->
          <div class="actions-bar">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Buscar empresa</mat-label>
              <input
                matInput
                [(ngModel)]="searchTerm"
                (ngModelChange)="onSearchChange()"
                placeholder="Buscar por razón social, RUC o nombre fantasía">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <button
              mat-raised-button
              color="primary"
              (click)="onCreateEmpresa()">
              <mat-icon>add</mat-icon>
              Nueva Empresa
            </button>
          </div>

          <!-- Loading State -->
          <app-loading-spinner *ngIf="loading$ | async"></app-loading-spinner>

          <!-- Error State -->
          <app-error-message
            *ngIf="error$ | async as error"
            [message]="error">
          </app-error-message>

          <!-- Empresas Table -->
          <div class="table-container" *ngIf="!(loading$ | async) && !(error$ | async)">
            <table mat-table [dataSource]="filteredEmpresas" class="empresas-table">

              <!-- RUC Column -->
              <ng-container matColumnDef="ruc">
                <th mat-header-cell *matHeaderCellDef>RUC</th>
                <td mat-cell *matCellDef="let empresa">{{ empresa.ruc }}</td>
              </ng-container>

              <!-- Razón Social Column -->
              <ng-container matColumnDef="razonSocial">
                <th mat-header-cell *matHeaderCellDef>Razón Social</th>
                <td mat-cell *matCellDef="let empresa">{{ empresa.razonSocial }}</td>
              </ng-container>

              <!-- Nombre Fantasía Column -->
              <ng-container matColumnDef="nombreFantasia">
                <th mat-header-cell *matHeaderCellDef>Nombre Fantasía</th>
                <td mat-cell *matCellDef="let empresa">{{ empresa.nombreFantasia || '-' }}</td>
              </ng-container>

              <!-- Email Column -->
              <ng-container matColumnDef="email">
                <th mat-header-cell *matHeaderCellDef>Email</th>
                <td mat-cell *matCellDef="let empresa">{{ empresa.email || '-' }}</td>
              </ng-container>

              <!-- Estado Column -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let empresa">
                  <mat-chip [class.active-chip]="empresa.activo" [class.inactive-chip]="!empresa.activo">
                    {{ empresa.activo ? 'Activa' : 'Inactiva' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let empresa">
                  <button
                    mat-icon-button
                    [matMenuTriggerFor]="actionsMenu"
                    matTooltip="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>

                  <mat-menu #actionsMenu="matMenu">
                    <button mat-menu-item (click)="onViewEmpresa(empresa)">
                      <mat-icon>visibility</mat-icon>
                      <span>Ver detalles</span>
                    </button>
                    <button mat-menu-item (click)="onEditEmpresa(empresa)">
                      <mat-icon>edit</mat-icon>
                      <span>Editar</span>
                    </button>
                    <button mat-menu-item (click)="onManageUsers(empresa)">
                      <mat-icon>people</mat-icon>
                      <span>Gestionar usuarios</span>
                    </button>
                    <button mat-menu-item (click)="onManageTimbrados(empresa)">
                      <mat-icon>receipt</mat-icon>
                      <span>Gestionar timbrados</span>
                    </button>
                    <button mat-menu-item (click)="onManageProductos(empresa)">
                      <mat-icon>inventory_2</mat-icon>
                      <span>Gestionar productos</span>
                    </button>
                    <button mat-menu-item (click)="onManageClientes(empresa)">
                      <mat-icon>people</mat-icon>
                      <span>Gestionar clientes</span>
                    </button>
                    <mat-divider></mat-divider>
                    <button
                      mat-menu-item
                      (click)="onToggleActive(empresa)"
                      [class.deactivate-option]="empresa.activo"
                      [class.activate-option]="!empresa.activo">
                      <mat-icon>{{ empresa.activo ? 'block' : 'check_circle' }}</mat-icon>
                      <span>{{ empresa.activo ? 'Desactivar' : 'Activar' }}</span>
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
                    <mat-icon>business</mat-icon>
                    <p>No se encontraron empresas</p>
                    <button mat-raised-button color="primary" (click)="onCreateEmpresa()">
                      <mat-icon>add</mat-icon>
                      Crear primera empresa
                    </button>
                  </div>
                </td>
              </tr>
            </table>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .empresas-container {
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

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }

    .actions-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      gap: 16px;
    }

    .search-field {
      flex: 1;
      max-width: 500px;
    }

    .table-container {
      overflow-x: auto;
    }

    .empresas-table {
      width: 100%;
      background: white;
    }

    .empresas-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .empresas-table td,
    .empresas-table th {
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

    .no-data-message > mat-icon:first-child {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }

    .no-data-message button mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .no-data-message p {
      margin: 0;
      font-size: 16px;
    }

    button mat-icon {
      margin-right: 4px;
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

    @media (max-width: 768px) {
      .actions-bar {
        flex-direction: column;
        align-items: stretch;
      }

      .search-field {
        max-width: 100%;
      }
    }
  `]
})
export class EmpresasListComponent implements OnInit, OnDestroy {
  empresas$: Observable<Empresa[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  displayedColumns: string[] = ['ruc', 'razonSocial', 'nombreFantasia', 'email', 'activo', 'actions'];
  filteredEmpresas: Empresa[] = [];
  searchTerm: string = '';

  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private router: Router,
    private dialog: MatDialog
  ) {
    this.empresas$ = this.store.select(selectAllEmpresas);
    this.loading$ = this.store.select(selectEmpresasLoading);
    this.error$ = this.store.select(selectEmpresasError);
  }

  ngOnInit(): void {
    // Load empresas from store
    this.store.dispatch(EmpresasActions.loadEmpresas());

    // Subscribe to empresas changes
    this.empresas$.pipe(takeUntil(this.destroy$)).subscribe(empresas => {
      this.filteredEmpresas = this.filterEmpresas(empresas);
    });

    // Setup search with debounce
    this.searchSubject.pipe(
      debounceTime(300), // Wait 300ms after user stops typing
      distinctUntilChanged(), // Only emit if value changed
      takeUntil(this.destroy$)
    ).subscribe(searchTerm => {
      this.performSearch(searchTerm);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchChange(): void {
    // Emit search term to subject for debounced processing
    this.searchSubject.next(this.searchTerm);
  }

  private performSearch(searchTerm: string): void {
    if (!searchTerm || searchTerm.trim().length === 0) {
      // Si no hay término, cargar todas las empresas
      this.store.dispatch(EmpresasActions.loadEmpresas());
    } else if (searchTerm.trim().length >= 2) {
      // Para búsquedas de 2+ caracteres, usar filtro backend (cuando esté implementado)
      // TODO: Implementar searchEmpresas action
      // this.store.dispatch(EmpresasActions.searchEmpresas({
      //   searchTerm: searchTerm.trim()
      // }));

      // Por ahora, usar filtro frontend
      this.empresas$.pipe(takeUntil(this.destroy$)).subscribe(empresas => {
        this.filteredEmpresas = this.filterEmpresas(empresas, searchTerm);
      });
    } else {
      // Para 1 carácter, usar filtro frontend
      this.empresas$.pipe(takeUntil(this.destroy$)).subscribe(empresas => {
        this.filteredEmpresas = this.filterEmpresas(empresas, searchTerm);
      });
    }
  }

  private filterEmpresas(empresas: Empresa[], searchTerm?: string): Empresa[] {
    const term = searchTerm || this.searchTerm;

    if (!term || term.trim() === '') {
      return empresas;
    }

    const normalizedTerm = term.toLowerCase().trim();
    return empresas.filter(empresa =>
      empresa.razonSocial.toLowerCase().includes(normalizedTerm) ||
      empresa.ruc.toLowerCase().includes(normalizedTerm) ||
      (empresa.nombreFantasia && empresa.nombreFantasia.toLowerCase().includes(normalizedTerm))
    );
  }

  onCreateEmpresa(): void {
    this.router.navigate(['/empresas/new']);
  }

  onViewEmpresa(empresa: Empresa): void {
    this.router.navigate(['/empresas', empresa.id]);
  }

  onEditEmpresa(empresa: Empresa): void {
    this.router.navigate(['/empresas', empresa.id, 'edit']);
  }

  onManageUsers(empresa: Empresa): void {
    this.router.navigate(['/empresas', empresa.id, 'usuarios']);
  }

  onManageTimbrados(empresa: Empresa): void {
    this.router.navigate(['/empresas', empresa.id, 'timbrados']);
  }

  onManageProductos(empresa: Empresa): void {
    this.router.navigate(['/empresas', empresa.id, 'productos']);
  }

  onManageClientes(empresa: Empresa): void {
    // Navegar a la lista de clientes con el empresaId como query param
    this.router.navigate(['/clientes'], { queryParams: { empresaId: empresa.id } });
  }

  onToggleActive(empresa: Empresa): void {
    const action = empresa.activo ? 'desactivar' : 'activar';
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${action.charAt(0).toUpperCase() + action.slice(1)} Empresa`,
        message: `¿Está seguro que desea ${action} la empresa "${empresa.razonSocial}"?`,
        confirmText: action.charAt(0).toUpperCase() + action.slice(1),
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.store.dispatch(EmpresasActions.updateEmpresa({
          id: empresa.id,
          empresa: { activo: !empresa.activo }
        }));
      }
    });
  }
}
