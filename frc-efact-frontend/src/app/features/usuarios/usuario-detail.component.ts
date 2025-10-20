import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil, filter, switchMap } from 'rxjs/operators';

import { User } from '../../models/user.model';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { ResetPasswordDialogComponent, ResetPasswordDialogData, ResetPasswordDialogResult } from './reset-password-dialog.component';
import * as UsuariosActions from '../../core/state/usuarios/usuarios.actions';
import {
  selectUserById,
  selectUsersLoading,
  selectUsersError,
  selectUsersOperationLoading
} from '../../core/state/usuarios/usuarios.selectors';
import { selectCurrentUser } from '../../core/state/auth/auth.selectors';

@Component({
  selector: 'app-usuario-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  template: `
    <div class="usuario-detail-container">
      <div class="header-actions">
        <button mat-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
          Volver
        </button>

        <div class="action-buttons" *ngIf="user">
          <button
            mat-raised-button
            color="primary"
            (click)="editUser()"
            [disabled]="loading$ | async">
            <mat-icon>edit</mat-icon>
            Editar
          </button>

          <button
            mat-raised-button
            color="accent"
            (click)="resetPassword()"
            [disabled]="(loading$ | async) || !canResetPassword()"
            [matTooltip]="!canResetPassword() ? 'No tiene permisos para restablecer contraseñas' : ''">
            <mat-icon>lock_reset</mat-icon>
            Resetear Contraseña
          </button>

          <button
            mat-raised-button
            [color]="user.isActive ? 'warn' : 'primary'"
            (click)="toggleUserStatus()"
            [disabled]="(loading$ | async) || !canToggleStatus()"
            [matTooltip]="!canToggleStatus() ? 'No puede desactivar su propia cuenta' : ''">
            <mat-icon>{{ user.isActive ? 'person_off' : 'person' }}</mat-icon>
            {{ user.isActive ? 'Desactivar' : 'Activar' }}
          </button>

          <button
            *ngIf="isUserLocked(user)"
            mat-raised-button
            color="primary"
            (click)="unlockUser()"
            [disabled]="loading$ | async"
            matTooltip="Desbloquear cuenta">
            <mat-icon>lock_open</mat-icon>
            Desbloquear
          </button>
        </div>
      </div>

      <div *ngIf="loading$ | async" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Cargando información del usuario...</p>
      </div>

      <div *ngIf="error$ | async as error" class="error-container">
        <mat-icon color="warn">error</mat-icon>
        <p>{{ error }}</p>
        <button mat-raised-button color="primary" (click)="loadUser()">
          Reintentar
        </button>
      </div>

      <div *ngIf="user && !(loading$ | async)" class="user-details">
        <!-- Basic Information Card -->
        <mat-card class="info-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>person</mat-icon>
              Información Básica
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-grid">
              <div class="info-item">
                <label>Usuario:</label>
                <span class="value">{{ user.username }}</span>
              </div>
              <div class="info-item">
                <label>Email:</label>
                <span class="value">{{ user.email }}</span>
              </div>
              <div class="info-item">
                <label>Estado:</label>
                <mat-chip-set>
                  <mat-chip [class]="getStatusChipClass(user)">
                    <mat-icon>{{ getStatusIcon(user) }}</mat-icon>
                    {{ getStatusText(user) }}
                  </mat-chip>
                </mat-chip-set>
              </div>
              <div class="info-item">
                <label>Roles:</label>
                <mat-chip-set>
                  <mat-chip *ngFor="let role of normalizedRoles" class="role-chip">
                    {{ getRoleName(role) }}
                  </mat-chip>
                </mat-chip-set>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Account Status Card -->
        <mat-card class="info-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>security</mat-icon>
              Estado de la Cuenta
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-grid">
              <div class="info-item">
                <label>Último Acceso:</label>
                <span class="value">{{ formatDate(user.ultimoLogin) || 'Nunca' }}</span>
              </div>
              <div class="info-item">
                <label>Intentos Fallidos:</label>
                <span class="value" [class.warning]="(user.failedLoginAttempts || 0) > 0">
                  {{ user.failedLoginAttempts || 0 }}
                </span>
              </div>
              <div class="info-item" *ngIf="user.lockedUntil">
                <label>Bloqueado Hasta:</label>
                <span class="value warning">{{ formatDate(user.lockedUntil) }}</span>
              </div>
              <div class="info-item">
                <label>Fecha de Creación:</label>
                <span class="value">{{ formatDate(user.creadoEn) }}</span>
              </div>
              <div class="info-item">
                <label>Última Modificación:</label>
                <span class="value">{{ formatDate(user.actualizadoEn) }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Company Assignments Card -->
        <mat-card class="info-card" *ngIf="user.empresas && user.empresas.length > 0">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>business</mat-icon>
              Asignaciones de Empresa
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="companies-list">
              <div *ngFor="let empresa of user.empresas" class="company-item">
                <div class="company-info">
                  <span class="company-id">Empresa ID: {{ empresa.empresaId }}</span>
                  <mat-chip-set>
                    <mat-chip [class]="empresa.activo ? 'active-chip' : 'inactive-chip'">
                      {{ empresa.activo ? 'Activo' : 'Inactivo' }}
                    </mat-chip>
                    <mat-chip class="role-chip">
                      {{ empresa.rolEmpresa }}
                    </mat-chip>
                  </mat-chip-set>
                </div>
                <div class="company-dates">
                  <small>Asignado: {{ formatDate(empresa.creadoEn) }}</small>
                  <small>Actualizado: {{ formatDate(empresa.actualizadoEn) }}</small>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- No Companies Message -->
        <mat-card class="info-card" *ngIf="!user.empresas || user.empresas.length === 0">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>business</mat-icon>
              Asignaciones de Empresa
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="no-companies">
              <mat-icon>info</mat-icon>
              <p>Este usuario no tiene empresas asignadas.</p>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .usuario-detail-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      flex-wrap: wrap;
      gap: 10px;
    }

    .action-buttons {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
    }

    .loading-container, .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;
      text-align: center;
    }

    .error-container mat-icon {
      font-size: 48px;
      height: 48px;
      width: 48px;
      margin-bottom: 16px;
    }

    .user-details {
      display: grid;
      gap: 20px;
      grid-template-columns: 1fr;
    }

    .info-card {
      margin-bottom: 0;
    }

    .info-card mat-card-header {
      margin-bottom: 16px;
    }

    .info-card mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 16px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .info-item label {
      font-weight: 500;
      color: rgba(0, 0, 0, 0.6);
      font-size: 14px;
    }

    .info-item .value {
      font-size: 16px;
      color: rgba(0, 0, 0, 0.87);
    }

    .info-item .value.warning {
      color: #f57c00;
      font-weight: 500;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .inactive-chip {
      background-color: #f44336 !important;
      color: white !important;
    }

    .locked-chip {
      background-color: #ff9800 !important;
      color: white !important;
    }

    .role-chip {
      background-color: #2196f3 !important;
      color: white !important;
    }

    .companies-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .company-item {
      padding: 16px;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      background-color: #fafafa;
    }

    .company-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      flex-wrap: wrap;
      gap: 8px;
    }

    .company-id {
      font-weight: 500;
      color: rgba(0, 0, 0, 0.87);
    }

    .company-dates {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .company-dates small {
      color: rgba(0, 0, 0, 0.6);
      font-size: 12px;
    }

    .no-companies {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      color: rgba(0, 0, 0, 0.6);
    }

    .no-companies mat-icon {
      font-size: 48px;
      height: 48px;
      width: 48px;
      margin-bottom: 16px;
    }

    /* Responsive design */
    @media (max-width: 768px) {
      .usuario-detail-container {
        padding: 10px;
      }

      .header-actions {
        flex-direction: column;
        align-items: stretch;
      }

      .action-buttons {
        justify-content: center;
      }

      .info-grid {
        grid-template-columns: 1fr;
      }

      .company-info {
        flex-direction: column;
        align-items: flex-start;
      }
    }

    /* Material chip customization */
    mat-chip-set {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }

    mat-chip {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    mat-chip mat-icon {
      font-size: 16px;
      height: 16px;
      width: 16px;
    }
  `]
})
export class UsuarioDetailComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly store = inject(Store);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly destroy$ = new Subject<void>();

  user: User | null = null;
  userId: number | null = null;
  currentUser: User | null = null;

  // Observables
  loading$ = this.store.select(selectUsersLoading);
  operationLoading$ = this.store.select(selectUsersOperationLoading);
  error$ = this.store.select(selectUsersError);
  currentUser$ = this.store.select(selectCurrentUser);

  ngOnInit(): void {
    this.route.params.pipe(
      takeUntil(this.destroy$),
      filter(params => params['id'])
    ).subscribe(params => {
      this.userId = +params['id'];
      this.loadUser();
    });

    // Subscribe to current user for security checks
    this.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      this.currentUser = user;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadUser(): void {
    if (this.userId) {
      // First try to get user from store
      this.store.select(selectUserById(this.userId)).pipe(
        takeUntil(this.destroy$)
      ).subscribe(user => {
        if (user) {
          this.user = user;
        } else {
          // If not in store, load from API
          this.store.dispatch(UsuariosActions.loadUserById({ id: this.userId! }));
        }
      });

      // Also subscribe to the selected user from store
      this.store.select(selectUserById(this.userId)).pipe(
        takeUntil(this.destroy$),
        filter(user => !!user)
      ).subscribe(user => {
        this.user = user || null;
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/usuarios']);
  }

  editUser(): void {
    if (this.userId) {
      this.router.navigate(['/usuarios', this.userId, 'edit']);
    }
  }

  resetPassword(): void {
    if (!this.user) return;

    if (!this.canResetPassword()) {
      this.snackBar.open('No tiene permisos para restablecer la contraseña de este usuario', 'Cerrar', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    const dialogRef = this.dialog.open(ResetPasswordDialogComponent, {
      data: {
        userId: this.user.id,
        username: this.user.username
      } as ResetPasswordDialogData,
      width: '500px',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe((result: ResetPasswordDialogResult | null) => {
      if (result && this.user) {
        this.logAdminAction('RESET_PASSWORD', {
          forcePasswordChange: result.forcePasswordChange
        });

        this.store.dispatch(UsuariosActions.resetUserPassword({
          userId: this.user.id,
          newPassword: result.newPassword,
          forcePasswordChange: result.forcePasswordChange
        }));

        this.snackBar.open('Contraseña reseteada exitosamente', 'Cerrar', {
          duration: 3000
        });
      }
    });
  }

  toggleUserStatus(): void {
    if (!this.user) return;

    if (!this.canToggleStatus()) {
      this.snackBar.open('No puede desactivar su propia cuenta', 'Cerrar', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    const action = this.user.isActive ? 'desactivar' : 'activar';
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${action.charAt(0).toUpperCase() + action.slice(1)} Usuario`,
        message: `¿Está seguro que desea ${action} al usuario "${this.user.username}"?`,
        confirmText: action.charAt(0).toUpperCase() + action.slice(1),
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.user) {
        this.logAdminAction('TOGGLE_STATUS', {
          previousStatus: this.user.isActive ? 'active' : 'inactive',
          newStatus: this.user.isActive ? 'inactive' : 'active'
        });

        this.store.dispatch(UsuariosActions.toggleUserStatus({ id: this.user.id }));
      }
    });
  }

  unlockUser(): void {
    if (!this.user) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Desbloquear Usuario',
        message: `¿Está seguro que desea desbloquear al usuario "${this.user.username}"?`,
        confirmText: 'Desbloquear',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.user) {
        this.logAdminAction('UNLOCK_USER');
        this.store.dispatch(UsuariosActions.unlockUser({ id: this.user.id }));
      }
    });
  }

  isUserLocked(user: User): boolean {
    if (!user.lockedUntil) return false;
    return new Date(user.lockedUntil) > new Date();
  }

  getStatusChipClass(user: User): string {
    if (this.isUserLocked(user)) return 'locked-chip';
    return user.isActive ? 'active-chip' : 'inactive-chip';
  }

  getStatusIcon(user: User): string {
    if (this.isUserLocked(user)) return 'lock';
    return user.isActive ? 'check_circle' : 'cancel';
  }

  getStatusText(user: User): string {
    if (this.isUserLocked(user)) return 'Bloqueado';
    return user.isActive ? 'Activo' : 'Inactivo';
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '';

    try {
      const date = new Date(dateString);
      return date.toLocaleString('es-ES', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return dateString;
    }
  }

  // Security methods
  canDeleteUser(): boolean {
    // Prevent self-deletion
    if (this.currentUser && this.user && this.currentUser.id === this.user.id) {
      return false;
    }
    return true;
  }

  canModifyUser(): boolean {
    // Allow modification of all users
    return true;
  }

  canResetPassword(): boolean {
    // Allow password reset for all users
    return true;
  }

  canToggleStatus(): boolean {
    // Prevent self-deactivation
    if (this.currentUser && this.user && this.currentUser.id === this.user.id && this.user.isActive) {
      return false;
    }
    return true;
  }

  get normalizedRoles(): any[] {
    if (!this.user?.roles) return [];
    return Array.isArray(this.user.roles) ? this.user.roles : [];
  }

  getRoleName(role: any): string {
    // Si el rol es un objeto con propiedad 'nombre', devolver el nombre
    if (typeof role === 'object' && role && 'nombre' in role) {
      return role.nombre;
    }
    // Si es un string, devolverlo directamente
    return role;
  }

  private logAdminAction(action: string, details?: any): void {
    // Log administrative actions for audit trail
    const logData = {
      action,
      targetUserId: this.user?.id,
      targetUsername: this.user?.username,
      performedBy: this.currentUser?.id,
      performedByUsername: this.currentUser?.username,
      timestamp: new Date().toISOString(),
      details
    };

    console.log('Admin Action:', logData);

    // TODO: Send to backend audit service when available
    // this.auditService.logAdminAction(logData);
  }
}
