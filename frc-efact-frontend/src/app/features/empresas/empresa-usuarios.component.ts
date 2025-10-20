import { Component, OnInit, OnDestroy, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { UsuarioEmpresa } from '../../models/usuario-empresa.model';
import { User } from '../../models/user.model';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { UsuarioApiService } from '../../core/api/usuario-api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { AddUserDialogComponent } from './add-user-dialog.component';
import { EditUserDialogComponent } from './edit-user-dialog.component';

@Component({
  selector: 'app-empresa-usuarios',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatCardModule,
    MatTooltipModule,
    MatSnackBarModule,
    ReactiveFormsModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="usuarios-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <button mat-icon-button (click)="goBack()" matTooltip="Volver a empresas">
                <mat-icon>arrow_back</mat-icon>
              </button>
              <h2>Gestión de Usuarios - {{ empresaNombre }}</h2>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Loading State -->
          <app-loading-spinner *ngIf="isLoading"></app-loading-spinner>

          <!-- Error State -->
          <app-error-message
            *ngIf="error"
            [message]="error">
          </app-error-message>

          <!-- Content -->
          <div *ngIf="!isLoading && !error">
            <!-- Add User Button -->
            <div class="actions-bar">
              <button
                mat-raised-button
                color="primary"
                (click)="openAddUserDialog()">
                <mat-icon>person_add</mat-icon>
                Agregar Usuario
              </button>
            </div>

            <!-- Users Table -->
            <div class="table-container">
              <table mat-table [dataSource]="usuariosEmpresa" class="usuarios-table">

                <!-- Username Column -->
                <ng-container matColumnDef="username">
                  <th mat-header-cell *matHeaderCellDef>Usuario</th>
                  <td mat-cell *matCellDef="let usuarioEmpresa">{{ usuarioEmpresa.usuarioUsername }}</td>
                </ng-container>

                <!-- Role Column -->
                <ng-container matColumnDef="rolEmpresa">
                  <th mat-header-cell *matHeaderCellDef>Rol en Empresa</th>
                  <td mat-cell *matCellDef="let usuarioEmpresa">
                    <mat-chip [class.admin-chip]="usuarioEmpresa.rolEmpresa === 'ADMINISTRADOR'"
                             [class.lector-chip]="usuarioEmpresa.rolEmpresa === 'LECTOR'">
                      {{ usuarioEmpresa.rolEmpresa }}
                    </mat-chip>
                  </td>
                </ng-container>

                <!-- Status Column -->
                <ng-container matColumnDef="activo">
                  <th mat-header-cell *matHeaderCellDef>Estado</th>
                  <td mat-cell *matCellDef="let usuarioEmpresa">
                    <mat-chip [class.active-chip]="usuarioEmpresa.activo"
                             [class.inactive-chip]="!usuarioEmpresa.activo">
                      {{ usuarioEmpresa.activo ? 'Activo' : 'Inactivo' }}
                    </mat-chip>
                  </td>
                </ng-container>

                <!-- Actions Column -->
                <ng-container matColumnDef="actions">
                  <th mat-header-cell *matHeaderCellDef>Acciones</th>
                  <td mat-cell *matCellDef="let usuarioEmpresa">
                    <button
                      mat-icon-button
                      (click)="openEditUserDialog(usuarioEmpresa)"
                      matTooltip="Editar rol">
                      <mat-icon>edit</mat-icon>
                    </button>
                    <button
                      mat-icon-button
                      color="warn"
                      (click)="removeUser(usuarioEmpresa)"
                      matTooltip="Remover usuario">
                      <mat-icon>person_remove</mat-icon>
                    </button>
                  </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

                <!-- No Data Row -->
                <tr class="mat-row" *matNoDataRow>
                  <td class="mat-cell no-data" [attr.colspan]="displayedColumns.length">
                    <div class="no-data-message">
                      <mat-icon>people</mat-icon>
                      <p>No hay usuarios asignados a esta empresa</p>
                      <button mat-raised-button color="primary" (click)="openAddUserDialog()">
                        <mat-icon>person_add</mat-icon>
                        Agregar primer usuario
                      </button>
                    </div>
                  </td>
                </tr>
              </table>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .usuarios-container {
      padding: 20px;
      max-width: 1200px;
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
      background-color: #4caf50 !important;
      color: white;
    }

    .lector-chip {
      background-color: #2196f3 !important;
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
      .usuarios-container {
        padding: 10px;
      }
    }
  `]
})
export class EmpresaUsuariosComponent implements OnInit, OnDestroy {
  empresaId!: number;
  empresaNombre: string = '';
  usuariosEmpresa: UsuarioEmpresa[] = [];
  displayedColumns: string[] = ['username', 'rolEmpresa', 'activo', 'actions'];
  isLoading: boolean = false;
  error: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private empresaApiService: EmpresaApiService,
    private usuarioApiService: UsuarioApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Obtener empresaId de la ruta
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.empresaId = +params['id'];
      this.loadUsuariosEmpresa();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goBack(): void {
    this.router.navigate(['/empresas']);
  }

  private loadUsuariosEmpresa(): void {
    this.isLoading = true;
    this.error = null;

    this.empresaApiService.getUsuariosEmpresa(this.empresaId).subscribe({
      next: (usuarios) => {
        this.usuariosEmpresa = usuarios;
        if (usuarios.length > 0) {
          this.empresaNombre = usuarios[0].empresaRazonSocial || 'Empresa';
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar usuarios de empresa:', error);
        this.error = 'Error al cargar usuarios de empresa';
        this.isLoading = false;
      }
    });
  }

  openAddUserDialog(): void {
    const dialogRef = this.dialog.open(AddUserDialogComponent, {
      width: '700px',
      maxWidth: '95vw',
      maxHeight: '95vh',
      data: { empresaId: this.empresaId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsuariosEmpresa();
      }
    });
  }

  openEditUserDialog(usuarioEmpresa: UsuarioEmpresa): void {
    const dialogRef = this.dialog.open(EditUserDialogComponent, {
      width: '400px',
      data: { usuarioEmpresa }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsuariosEmpresa();
      }
    });
  }

  removeUser(usuarioEmpresa: UsuarioEmpresa): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Remover Usuario',
        message: `¿Está seguro que desea remover al usuario "${usuarioEmpresa.usuarioUsername}" de esta empresa?`,
        confirmText: 'Remover',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.empresaApiService.removeUsuarioEmpresa(this.empresaId, usuarioEmpresa.usuarioId).subscribe({
          next: () => {
            this.snackBar.open('Usuario removido exitosamente', 'Cerrar', { duration: 3000 });
            this.loadUsuariosEmpresa();
          },
          error: (error) => {
            console.error('Error al remover usuario:', error);
            this.snackBar.open('Error al remover usuario', 'Cerrar', { duration: 3000 });
          }
        });
      }
    });
  }
}
