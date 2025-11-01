import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { Empresa } from '../../models/empresa.model';
import { UsuarioEmpresa } from '../../models/user.model';
import { selectEmpresaById } from '../../core/state/empresas/empresas.selectors';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { AsignarUsuarioDialogComponent } from './asignar-usuario-dialog.component';

@Component({
  selector: 'app-usuario-empresa',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="usuario-empresa-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <div>
                <h2>Usuarios de la Empresa</h2>
                <p class="empresa-name" *ngIf="empresa$ | async as empresa">
                  {{ empresa.razonSocial }}
                </p>
              </div>
              <button mat-icon-button (click)="onBack()" matTooltip="Volver">
                <mat-icon>arrow_back</mat-icon>
              </button>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Actions Bar -->
          <div class="actions-bar">
            <button
              mat-raised-button
              color="primary"
              (click)="onAsignarUsuario()">
              <mat-icon>person_add</mat-icon>
              Asignar Usuario
            </button>
          </div>

          <!-- Loading State -->
          <app-loading-spinner *ngIf="loading"></app-loading-spinner>

          <!-- Usuarios Table -->
          <div class="table-container" *ngIf="!loading">
            <table mat-table [dataSource]="usuariosEmpresa" class="usuarios-table">

              <!-- Username Column -->
              <ng-container matColumnDef="username">
                <th mat-header-cell *matHeaderCellDef>Usuario</th>
                <td mat-cell *matCellDef="let usuarioEmpresa">
                  {{ usuarioEmpresa.usuario?.username || '-' }}
                </td>
              </ng-container>

              <!-- Email Column -->
              <ng-container matColumnDef="email">
                <th mat-header-cell *matHeaderCellDef>Email</th>
                <td mat-cell *matCellDef="let usuarioEmpresa">
                  {{ usuarioEmpresa.usuario?.email || '-' }}
                </td>
              </ng-container>

              <!-- Rol Column -->
              <ng-container matColumnDef="rolEmpresa">
                <th mat-header-cell *matHeaderCellDef>Rol</th>
                <td mat-cell *matCellDef="let usuarioEmpresa">
                  <mat-chip
                    [class.admin-chip]="usuarioEmpresa.rolEmpresa === 'ADMINISTRADOR'"
                    [class.facturador-chip]="usuarioEmpresa.rolEmpresa === 'FACTURADOR'"
                    [class.lector-chip]="usuarioEmpresa.rolEmpresa === 'LECTOR'">
                    {{ usuarioEmpresa.rolEmpresa }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Estado Column -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let usuarioEmpresa">
                  <mat-chip
                    [class.active-chip]="usuarioEmpresa.activo"
                    [class.inactive-chip]="!usuarioEmpresa.activo">
                    {{ usuarioEmpresa.activo ? 'Activo' : 'Inactivo' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Fecha Column -->
              <ng-container matColumnDef="creadoEn">
                <th mat-header-cell *matHeaderCellDef>Fecha Asignación</th>
                <td mat-cell *matCellDef="let usuarioEmpresa">
                  {{ usuarioEmpresa.creadoEn | date:'dd/MM/yyyy' }}
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let usuarioEmpresa">
                  <button
                    mat-icon-button
                    color="accent"
                    (click)="onCambiarRol(usuarioEmpresa)"
                    matTooltip="Cambiar rol">
                    <mat-icon>swap_horiz</mat-icon>
                  </button>
                  <button
                    mat-icon-button
                    [color]="usuarioEmpresa.activo ? 'warn' : 'primary'"
                    (click)="onToggleActive(usuarioEmpresa)"
                    [matTooltip]="usuarioEmpresa.activo ? 'Desactivar' : 'Activar'">
                    <mat-icon>{{ usuarioEmpresa.activo ? 'block' : 'check_circle' }}</mat-icon>
                  </button>
                  <button
                    mat-icon-button
                    color="warn"
                    (click)="onRemoverUsuario(usuarioEmpresa)"
                    matTooltip="Remover acceso">
                    <mat-icon>delete</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

              <!-- No Data Row -->
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell no-data" [attr.colspan]="displayedColumns.length">
                  <div class="no-data-message">
                    <mat-icon>people_outline</mat-icon>
                    <p>No hay usuarios asignados a esta empresa</p>
                    <button mat-raised-button color="primary" (click)="onAsignarUsuario()">
                      <mat-icon>person_add</mat-icon>
                      Asignar primer usuario
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
    .usuario-empresa-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    mat-card-header {
      margin-bottom: 20px;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }

    .empresa-name {
      margin: 8px 0 0 0;
      font-size: 14px;
      color: #666;
    }

    .actions-bar {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 20px;
    }

    .table-container {
      overflow-x: auto;
    }

    .usuarios-table {
      width: 100%;
      background: white;
    }

    .usuarios-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .usuarios-table td,
    .usuarios-table th {
      padding: 12px 16px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    .admin-chip {
      background-color: #2196f3 !important;
      color: white;
    }

    .facturador-chip {
      background-color: #ff9800 !important;
      color: white;
    }

    .lector-chip {
      background-color: #9e9e9e !important;
      color: white;
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
  `]
})
export class UsuarioEmpresaComponent implements OnInit {
  empresa$: Observable<Empresa | null>;
  empresaId: number | null = null;
  usuariosEmpresa: UsuarioEmpresa[] = [];
  loading = false;
  displayedColumns: string[] = ['username', 'email', 'rolEmpresa', 'activo', 'creadoEn', 'actions'];

  constructor(
    private store: Store,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private empresaApiService: EmpresaApiService
  ) {
    this.empresa$ = this.store.select(selectEmpresaById(0));
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.empresaId = parseInt(id, 10);
      this.empresa$ = this.store.select(selectEmpresaById(this.empresaId));
      this.loadUsuariosEmpresa();
    }
  }

  private loadUsuariosEmpresa(): void {
    if (this.empresaId) {
      this.loading = true;
      this.empresaApiService.getUsuariosEmpresa(this.empresaId).subscribe({
        next: (usuarios) => {
          this.usuariosEmpresa = usuarios;
          this.loading = false;
        },
        error: (error) => {
          this.snackBar.open('Error al cargar usuarios', 'Cerrar', { duration: 3000 });
          this.loading = false;
        }
      });
    }
  }

  onAsignarUsuario(): void {
    const dialogRef = this.dialog.open(AsignarUsuarioDialogComponent, {
      width: '500px',
      data: { empresaId: this.empresaId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsuariosEmpresa();
        this.snackBar.open('Usuario asignado exitosamente', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onCambiarRol(usuarioEmpresa: UsuarioEmpresa): void {
    const nuevoRol = usuarioEmpresa.rolEmpresa === 'ADMINISTRADOR' ? 'LECTOR' : 'ADMINISTRADOR';

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Cambiar Rol',
        message: `¿Está seguro que desea cambiar el rol de ${usuarioEmpresa.usuario?.username} a ${nuevoRol}?`,
        confirmText: 'Cambiar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.empresaId) {
        this.empresaApiService.actualizarRolUsuarioEmpresa(
          this.empresaId,
          usuarioEmpresa.id,
          nuevoRol
        ).subscribe({
          next: () => {
            this.loadUsuariosEmpresa();
            this.snackBar.open('Rol actualizado exitosamente', 'Cerrar', { duration: 3000 });
          },
          error: () => {
            this.snackBar.open('Error al actualizar rol', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }

  onToggleActive(usuarioEmpresa: UsuarioEmpresa): void {
    const action = usuarioEmpresa.activo ? 'desactivar' : 'activar';

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${action.charAt(0).toUpperCase() + action.slice(1)} Usuario`,
        message: `¿Está seguro que desea ${action} el acceso de ${usuarioEmpresa.usuario?.username}?`,
        confirmText: action.charAt(0).toUpperCase() + action.slice(1),
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.empresaId) {
        this.empresaApiService.toggleUsuarioEmpresaActivo(
          this.empresaId,
          usuarioEmpresa.id
        ).subscribe({
          next: () => {
            this.loadUsuariosEmpresa();
            this.snackBar.open(`Usuario ${action} exitosamente`, 'Cerrar', { duration: 3000 });
          },
          error: () => {
            this.snackBar.open(`Error al ${action} usuario`, 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }

  onRemoverUsuario(usuarioEmpresa: UsuarioEmpresa): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Remover Usuario',
        message: `¿Está seguro que desea remover el acceso de ${usuarioEmpresa.usuario?.username} a esta empresa? Esta acción no se puede deshacer.`,
        confirmText: 'Remover',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.empresaId) {
        this.empresaApiService.removerUsuarioEmpresa(
          this.empresaId,
          usuarioEmpresa.id
        ).subscribe({
          next: () => {
            this.loadUsuariosEmpresa();
            this.snackBar.open('Usuario removido exitosamente', 'Cerrar', { duration: 3000 });
          },
          error: () => {
            this.snackBar.open('Error al remover usuario', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }

  onBack(): void {
    this.router.navigate(['/empresas']);
  }
}
