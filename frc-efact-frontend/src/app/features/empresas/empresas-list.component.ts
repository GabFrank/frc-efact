import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
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
                    color="primary"
                    (click)="onViewEmpresa(empresa)"
                    matTooltip="Ver detalles">
                    <mat-icon>visibility</mat-icon>
                  </button>
                  <button 
                    mat-icon-button 
                    color="accent"
                    (click)="onEditEmpresa(empresa)"
                    matTooltip="Editar">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button 
                    mat-icon-button 
                    color="primary"
                    (click)="onManageUsers(empresa)"
                    matTooltip="Gestionar usuarios">
                    <mat-icon>people</mat-icon>
                  </button>
                  <button 
                    mat-icon-button 
                    [color]="empresa.activo ? 'warn' : 'primary'"
                    (click)="onToggleActive(empresa)"
                    [matTooltip]="empresa.activo ? 'Desactivar' : 'Activar'">
                    <mat-icon>{{ empresa.activo ? 'block' : 'check_circle' }}</mat-icon>
                  </button>
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

    button mat-icon {
      margin-right: 4px;
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
export class EmpresasListComponent implements OnInit {
  empresas$: Observable<Empresa[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  displayedColumns: string[] = ['ruc', 'razonSocial', 'nombreFantasia', 'email', 'activo', 'actions'];
  filteredEmpresas: Empresa[] = [];
  searchTerm: string = '';

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
    this.empresas$.subscribe(empresas => {
      this.filteredEmpresas = this.filterEmpresas(empresas);
    });
  }

  onSearchChange(): void {
    this.empresas$.subscribe(empresas => {
      this.filteredEmpresas = this.filterEmpresas(empresas);
    }).unsubscribe();
  }

  private filterEmpresas(empresas: Empresa[]): Empresa[] {
    if (!this.searchTerm || this.searchTerm.trim() === '') {
      return empresas;
    }

    const term = this.searchTerm.toLowerCase().trim();
    return empresas.filter(empresa => 
      empresa.razonSocial.toLowerCase().includes(term) ||
      empresa.ruc.toLowerCase().includes(term) ||
      (empresa.nombreFantasia && empresa.nombreFantasia.toLowerCase().includes(term))
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
