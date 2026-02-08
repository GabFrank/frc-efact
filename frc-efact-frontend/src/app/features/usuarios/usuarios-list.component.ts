import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject, combineLatest, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil, map, switchMap, catchError } from 'rxjs/operators';
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
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';

import { User } from '../../models/user.model';
import { UsuarioEmpresa } from '../../models/usuario-empresa.model';
import { NotificationService } from '../../core/services/notification.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import * as UsuariosActions from '../../core/state/usuarios/usuarios.actions';
import {
  selectFilteredUsers,
  selectUsersLoading,
  selectUsersError,
  selectSearchTerm,
  selectUsersPagination,
  selectAnyLoading
} from '../../core/state/usuarios/usuarios.selectors';
import { selectCurrentUser } from '../../core/state/auth/auth.selectors';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { ResetPasswordDialogComponent, ResetPasswordDialogData, ResetPasswordDialogResult } from './reset-password-dialog.component';
import { UsuarioDialogComponent, UsuarioDialogData } from '../../shared/components/usuario-dialog/usuario-dialog.component';
import { PermissionsService } from '../../core/services/permissions.service';

@Component({
  selector: 'app-usuarios-list',
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
    MatPaginatorModule,
    MatSortModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="usuarios-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>Gestión de Usuarios</h2>
            <p *ngIf="empresaId" class="empresa-context">Mostrando usuarios de la empresa seleccionada</p>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Search and Actions Bar -->
          <div class="actions-bar">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Buscar usuario</mat-label>
              <input
                matInput
                [(ngModel)]="searchTerm"
                (ngModelChange)="onSearchChange()"
                placeholder="Buscar por nombre de usuario o email">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <button
              mat-raised-button
              color="primary"
              (click)="onCreateUser()"
              *ngIf="canManageUsers">
              <mat-icon>add</mat-icon>
              Nuevo Usuario
            </button>
          </div>

          <!-- Loading State -->
          <app-loading-spinner *ngIf="(loading$ | async) || loadingEmpresaUsers"></app-loading-spinner>

          <!-- Error State -->
          <app-error-message
            *ngIf="error$ | async as error"
            [message]="error"
            [showRetry]="true"
            (retry)="onRetryLoad()">
          </app-error-message>

          <!-- Users Table -->
          <div class="table-container" *ngIf="!(loading$ | async) && !(error$ | async)">
            <table mat-table [dataSource]="displayedUsers" matSort (matSortChange)="onSortChange($event)" class="usuarios-table">

              <!-- Username Column -->
              <ng-container matColumnDef="username">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Usuario</th>
                <td mat-cell *matCellDef="let user">
                  <div class="user-info">
                    <span class="username">{{ user.username }}</span>
                    <span class="user-id" *ngIf="user.id">#{{ user.id }}</span>
                  </div>
                </td>
              </ng-container>

              <!-- Email Column -->
              <ng-container matColumnDef="email">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Email</th>
                <td mat-cell *matCellDef="let user">{{ user.email }}</td>
              </ng-container>

              <!-- Roles Column -->
              <ng-container matColumnDef="roles">
                <th mat-header-cell *matHeaderCellDef>Roles</th>
                <td mat-cell *matCellDef="let user">
                  <div class="roles-container">
                    <mat-chip
                      class="role-count-chip"
                      [matTooltip]="getRolesTooltip(user)"
                      matTooltipPosition="above">
                      <mat-icon>security</mat-icon>
                      {{ getRoleCount(user) }} rol{{ getRoleCount(user) !== 1 ? 'es' : '' }}
                    </mat-chip>
                  </div>
                </td>
              </ng-container>

              <!-- Status Column -->
              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let user">
                  <div class="status-container">
                    <mat-chip
                      [class.active-chip]="user.isActive && !isUserLocked(user)"
                      [class.inactive-chip]="!user.isActive"
                      [class.locked-chip]="isUserLocked(user)">
                      {{ getUserStatusText(user) }}
                    </mat-chip>
                    <mat-icon
                      *ngIf="isUserLocked(user)"
                      class="lock-icon"
                      matTooltip="Usuario bloqueado">
                      lock
                    </mat-icon>
                  </div>
                </td>
              </ng-container>

              <!-- Last Login Column -->
              <ng-container matColumnDef="lastLogin">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Último Acceso</th>
                <td mat-cell *matCellDef="let user">
                  <span [class.never-logged]="!user.ultimoLogin">
                    {{ formatDateTime(user.ultimoLogin) }}
                  </span>
                </td>
              </ng-container>

              <!-- Created Date Column -->
              <ng-container matColumnDef="createdAt">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Creado</th>
                <td mat-cell *matCellDef="let user">
                  {{ formatDate(user.creadoEn) }}
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let user">
                  <button
                    mat-icon-button
                    [matMenuTriggerFor]="actionsMenu"
                    matTooltip="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>

                  <mat-menu #actionsMenu="matMenu">
                    <button mat-menu-item (click)="onViewUser(user)">
                      <mat-icon>visibility</mat-icon>
                      <span>Ver detalles</span>
                    </button>
                    <button
                      mat-menu-item
                      (click)="onEditUser(user)"
                      [disabled]="!canModifyUser(user)">
                      <mat-icon>edit</mat-icon>
                      <span>Editar</span>
                    </button>
                    <button
                      mat-menu-item
                      (click)="onResetPassword(user)"
                      [disabled]="!canResetPassword(user)">
                      <mat-icon>lock_reset</mat-icon>
                      <span>Restablecer contraseña</span>
                    </button>
                    <mat-divider></mat-divider>

                    <!-- Unlock option for locked users -->
                    <button
                      *ngIf="isUserLocked(user)"
                      mat-menu-item
                      (click)="onUnlockUser(user)"
                      class="unlock-option">
                      <mat-icon>lock_open</mat-icon>
                      <span>Desbloquear cuenta</span>
                    </button>

                    <!-- Toggle status option -->
                    <button
                      mat-menu-item
                      (click)="onToggleUserStatus(user)"
                      [disabled]="!canToggleStatus(user)"
                      [class.deactivate-option]="user.isActive && canToggleStatus(user)"
                      [class.activate-option]="!user.isActive"
                      [matTooltip]="!canToggleStatus(user) ? 'No puede desactivar su propia cuenta' : ''">
                      <mat-icon>{{ user.isActive ? 'block' : 'check_circle' }}</mat-icon>
                      <span>{{ user.isActive ? 'Desactivar' : 'Activar' }}</span>
                    </button>

                    <mat-divider></mat-divider>
                    <button
                      mat-menu-item
                      (click)="onDeleteUser(user)"
                      [disabled]="!canDeleteUser(user)"
                      [matTooltip]="!canDeleteUser(user) ? 'No puede eliminar su propia cuenta' : ''"
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
                    <mat-icon>people</mat-icon>
                    <p *ngIf="!searchTerm">No hay usuarios registrados</p>
                    <p *ngIf="searchTerm">No se encontraron usuarios que coincidan con "{{ searchTerm }}"</p>
                    <button
                      *ngIf="!searchTerm"
                      mat-raised-button
                      color="primary"
                      (click)="onCreateUser()">
                      <mat-icon>add</mat-icon>
                      Crear primer usuario
                    </button>
                    <button
                      *ngIf="searchTerm"
                      mat-stroked-button
                      (click)="clearSearch()">
                      <mat-icon>clear</mat-icon>
                      Limpiar búsqueda
                    </button>
                  </div>
                </td>
              </tr>
            </table>

            <!-- Pagination -->
            <mat-paginator
              *ngIf="displayedUsers.length > 0"
              [length]="totalUsers"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 25, 50, 100]"
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
    .usuarios-container {
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

    .empresa-context {
      margin: 8px 0 0 0;
      font-size: 14px;
      color: #666;
      font-style: italic;
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

    .user-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .username {
      font-weight: 500;
    }

    .user-id {
      font-size: 12px;
      color: #666;
    }

    .roles-container {
      display: flex;
      gap: 4px;
      flex-wrap: wrap;
    }

    .status-container {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    .admin-role {
      background-color: #ff9800 !important;
      color: white;
    }

    .user-role {
      background-color: #2196f3 !important;
      color: white;
    }

    .role-count-chip {
      background-color: #9c27b0 !important;
      color: white;
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    .role-count-chip mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      margin-right: 4px;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    .inactive-chip {
      background-color: #f44336 !important;
      color: white;
    }

    .locked-chip {
      background-color: #ff5722 !important;
      color: white;
    }

    .lock-icon {
      color: #ff5722;
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .never-logged {
      color: #999;
      font-style: italic;
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

    .unlock-option {
      color: #ff9800;
    }

    .delete-option {
      color: #f44336;
    }

    .mat-mdc-menu-item:disabled {
      color: rgba(0, 0, 0, 0.38) !important;
      cursor: not-allowed;
    }

    .mat-mdc-menu-item:disabled mat-icon {
      color: rgba(0, 0, 0, 0.38) !important;
    }

    @media (max-width: 768px) {
      .actions-bar {
        flex-direction: column;
        align-items: stretch;
      }

      .search-field {
        max-width: 100%;
      }

      .usuarios-table {
        font-size: 14px;
      }

      .usuarios-table td,
      .usuarios-table th {
        padding: 8px 12px;
      }
    }
  `]
})
export class UsuariosListComponent implements OnInit, OnDestroy {
  users$: Observable<User[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  searchTerm$: Observable<string>;
  pagination$: Observable<any>;
  currentUser$: Observable<User | null>;

  displayedColumns: string[] = ['username', 'email', 'roles', 'status', 'lastLogin', 'createdAt', 'actions'];
  displayedUsers: User[] = [];
  searchTerm: string = '';
  currentUser: User | null = null;

  // Pagination
  currentPage: number = 0;
  pageSize: number = 25;
  totalUsers: number = 0;

  // Sorting
  currentSort: Sort = { active: 'createdAt', direction: 'desc' };

  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  empresaId?: number;
  loadingEmpresaUsers: boolean = false;

  constructor(
    private store: Store,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private notificationService: NotificationService,
    private empresaApiService: EmpresaApiService,
    private permissionsService: PermissionsService
  ) {
    this.users$ = this.store.select(selectFilteredUsers);
    this.loading$ = this.store.select(selectAnyLoading);
    this.error$ = this.store.select(selectUsersError);
    this.searchTerm$ = this.store.select(selectSearchTerm);
    this.pagination$ = this.store.select(selectUsersPagination);
    this.currentUser$ = this.store.select(selectCurrentUser);
  }

  ngOnInit(): void {
    // Check if empresaId is provided in query params
    this.route.queryParams.pipe(
      takeUntil(this.destroy$),
      map(params => {
        const empresaId = params['empresaId'] ? Number(params['empresaId']) : null;
        return empresaId;
      }),
      switchMap(empresaId => {
        this.empresaId = empresaId || undefined;
        
        if (empresaId) {
          // Load users from empresa
          this.loadingEmpresaUsers = true;
          return this.empresaApiService.getUsuariosEmpresa(empresaId).pipe(
            map((usuariosEmpresa: UsuarioEmpresa[]) => {
              // Convert UsuarioEmpresa[] to User[]
              const users: User[] = usuariosEmpresa.map(ue => {
                const user: User = {
                  id: ue.usuarioId,
                  username: ue.usuarioUsername || `Usuario ${ue.usuarioId}`,
                  email: ue.usuarioEmail || '',
                  isActive: ue.activo ?? true,
                  roles: ue.usuarioRoles || [], // Roles del usuario desde el backend
                  creadoEn: ue.creadoEn,
                  actualizadoEn: ue.actualizadoEn,
                  ultimoLogin: undefined, // UsuarioEmpresa doesn't include ultimoLogin
                };
                // Add empresa-specific info as custom properties
                (user as any).empresaRol = ue.rolEmpresa;
                (user as any).empresaId = ue.empresaId;
                (user as any).empresaRazonSocial = ue.empresaRazonSocial;
                return user;
              });
              
              this.loadingEmpresaUsers = false;
              this.store.dispatch(UsuariosActions.loadUsersSuccess({ users }));
              return users;
            }),
            catchError(error => {
              this.loadingEmpresaUsers = false;
              const errorMessage = error.error?.message || 'Error al cargar usuarios de la empresa';
              this.store.dispatch(UsuariosActions.loadUsersFailure({ error: errorMessage }));
              this.snackBar.open(errorMessage, 'Cerrar', { duration: 5000 });
              return [];
            })
          );
        } else {
          // Load all users from store
          this.store.dispatch(UsuariosActions.loadUsers({}));
          return this.users$;
        }
      })
    ).subscribe();

    // Subscribe to users changes
    this.users$.pipe(takeUntil(this.destroy$)).subscribe(users => {
      this.displayedUsers = this.sortUsers([...users]);
      this.totalUsers = users.length;
    });

    // Subscribe to search term from store
    this.searchTerm$.pipe(takeUntil(this.destroy$)).subscribe(term => {
      this.searchTerm = term;
    });

    // Subscribe to current user for security checks
    this.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      this.currentUser = user;
    });

    // Setup search with debounce
    this.searchSubject.pipe(
      debounceTime(300), // Wait 300ms after user stops typing
      distinctUntilChanged(), // Only emit if value changed
      takeUntil(this.destroy$)
    ).subscribe(searchTerm => {
      this.store.dispatch(UsuariosActions.setSearchTerm({ searchTerm }));
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

  clearSearch(): void {
    this.searchTerm = '';
    this.store.dispatch(UsuariosActions.setSearchTerm({ searchTerm: '' }));
  }

  onSortChange(sort: Sort): void {
    this.currentSort = sort;
    this.displayedUsers = this.sortUsers([...this.displayedUsers]);
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    // TODO: Implement server-side pagination when backend supports it
  }

  private sortUsers(users: User[]): User[] {
    if (!this.currentSort.active || this.currentSort.direction === '') {
      return users;
    }

    return users.sort((a, b) => {
      const isAsc = this.currentSort.direction === 'asc';

      switch (this.currentSort.active) {
        case 'username':
          return this.compare(a.username, b.username, isAsc);
        case 'email':
          return this.compare(a.email, b.email, isAsc);
        case 'lastLogin':
          return this.compare(a.ultimoLogin || '', b.ultimoLogin || '', isAsc);
        case 'createdAt':
          return this.compare(a.creadoEn, b.creadoEn, isAsc);
        default:
          return 0;
      }
    });
  }

  private compare(a: string | number, b: string | number, isAsc: boolean): number {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  isUserLocked(user: User): boolean {
    if (!user.lockedUntil) return false;
    return new Date(user.lockedUntil) > new Date();
  }

  getUserStatusText(user: User): string {
    if (this.isUserLocked(user)) return 'Bloqueado';
    return user.isActive ? 'Activo' : 'Inactivo';
  }

  getRoleDisplayName(role: any): string {
    // Si el rol es un objeto con propiedad 'nombre', devolver el nombre
    if (typeof role === 'object' && role && 'nombre' in role) {
      return role.nombre;
    }
    // Si es un string, devolverlo directamente
    return role;
  }

  getRoleCount(user: User): number {
    return user.roles ? user.roles.length : 0;
  }

  getRolesTooltip(user: User): string {
    if (!user.roles || user.roles.length === 0) {
      return 'Sin roles asignados';
    }

    const roleNames = user.roles.map(role => this.getRoleDisplayName(role));
    return roleNames.join(', ');
  }

  formatDate(date: string | null | undefined): string {
    if (!date) return 'N/A';
    try {
      return new Date(date).toLocaleDateString('es-ES');
    } catch {
      return 'N/A';
    }
  }

  formatDateTime(date: string | null | undefined): string {
    if (!date) return 'Nunca';
    try {
      return new Date(date).toLocaleString('es-ES');
    } catch {
      return 'Nunca';
    }
  }

  // Security methods
  canDeleteUser(user: User): boolean {
    // Prevent self-deletion
    if (this.currentUser && this.currentUser.id === user.id) {
      return false;
    }
    return true;
  }

  canModifyUser(user: User): boolean {
    // Allow modification of all users except prevent certain actions on self
    return true;
  }

  canResetPassword(user: User): boolean {
    // Allow password reset for all users
    return true;
  }

  canToggleStatus(user: User): boolean {
    // Prevent self-deactivation
    if (this.currentUser && this.currentUser.id === user.id && user.isActive) {
      return false;
    }
    return true;
  }

  private logAdminAction(action: string, targetUser: User, details?: any): void {
    // Log administrative actions for audit trail
    const logData = {
      action,
      targetUserId: targetUser.id,
      targetUsername: targetUser.username,
      performedBy: this.currentUser?.id,
      performedByUsername: this.currentUser?.username,
      timestamp: new Date().toISOString(),
      details
    };

    console.log('Admin Action:', logData);

    // TODO: Send to backend audit service when available
    // this.auditService.logAdminAction(logData);
  }

  onRetryLoad(): void {
    this.store.dispatch(UsuariosActions.loadUsers({}));
  }

  onCreateUser(): void {
    const dialogData: UsuarioDialogData = {};
    
    // Si hay empresaId en los query params, incluirla en el diálogo
    if (this.empresaId) {
      dialogData.empresaId = this.empresaId;
      // Por defecto asignar como FACTURADOR, pero el usuario puede cambiarlo si tiene permisos
      dialogData.rolEmpresa = 'FACTURADOR';
    }

    const dialogRef = this.dialog.open(UsuarioDialogComponent, {
      width: '600px',
      disableClose: false,
      data: dialogData
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((user?: User) => {
      if (user) {
        // Si se creó desde el contexto de una empresa, recargar usuarios de esa empresa
        if (this.empresaId) {
          // Recargar usuarios de la empresa
          this.empresaApiService.getUsuariosEmpresa(this.empresaId).pipe(
            map((usuariosEmpresa: UsuarioEmpresa[]) => {
              const users: User[] = usuariosEmpresa.map(ue => {
                const user: User = {
                  id: ue.usuarioId,
                  username: ue.usuarioUsername || `Usuario ${ue.usuarioId}`,
                  email: ue.usuarioEmail || '',
                  isActive: ue.activo ?? true,
                  roles: ue.usuarioRoles || [],
                  creadoEn: ue.creadoEn,
                  actualizadoEn: ue.actualizadoEn,
                  ultimoLogin: undefined,
                };
                (user as any).empresaRol = ue.rolEmpresa;
                (user as any).empresaId = ue.empresaId;
                (user as any).empresaRazonSocial = ue.empresaRazonSocial;
                return user;
              });
              this.store.dispatch(UsuariosActions.loadUsersSuccess({ users }));
              return users;
            }),
            catchError(error => {
              const errorMessage = error.error?.message || 'Error al cargar usuarios de la empresa';
              this.store.dispatch(UsuariosActions.loadUsersFailure({ error: errorMessage }));
              return [];
            })
          ).subscribe();
        } else {
          // Recargar todos los usuarios
          this.store.dispatch(UsuariosActions.loadUsers({}));
        }
      }
    });
  }

  onViewUser(user: User): void {
    this.router.navigate(['/usuarios', user.id]);
  }

  onEditUser(user: User): void {
    this.router.navigate(['/usuarios', user.id, 'edit']);
  }

  onResetPassword(user: User): void {
    if (!this.canResetPassword(user)) {
      this.notificationService.showError('No tiene permisos para restablecer la contraseña de este usuario');
      return;
    }

    const dialogRef = this.dialog.open(ResetPasswordDialogComponent, {
      data: {
        userId: user.id,
        username: user.username
      } as ResetPasswordDialogData,
      width: '500px',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe((result: ResetPasswordDialogResult | null) => {
      if (result) {
        this.logAdminAction('RESET_PASSWORD', user, {
          forcePasswordChange: result.forcePasswordChange
        });

        this.store.dispatch(UsuariosActions.resetUserPassword({
          userId: user.id,
          newPassword: result.newPassword,
          forcePasswordChange: result.forcePasswordChange
        }));
      }
    });
  }

  onUnlockUser(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Desbloquear Usuario',
        message: `¿Está seguro que desea desbloquear la cuenta de "${user.username}"?`,
        confirmText: 'Desbloquear',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.logAdminAction('UNLOCK_USER', user);
        this.store.dispatch(UsuariosActions.unlockUser({ id: user.id }));
      }
    });
  }

  onToggleUserStatus(user: User): void {
    if (!this.canToggleStatus(user)) {
      this.notificationService.showError('No puede desactivar su propia cuenta');
      return;
    }

    const action = user.isActive ? 'desactivar' : 'activar';
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${action.charAt(0).toUpperCase() + action.slice(1)} Usuario`,
        message: `¿Está seguro que desea ${action} la cuenta de "${user.username}"?`,
        confirmText: action.charAt(0).toUpperCase() + action.slice(1),
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.logAdminAction('TOGGLE_STATUS', user, {
          previousStatus: user.isActive ? 'active' : 'inactive',
          newStatus: user.isActive ? 'inactive' : 'active'
        });

        this.store.dispatch(UsuariosActions.toggleUserStatus({ id: user.id }));
      }
    });
  }

  onDeleteUser(user: User): void {
    if (!this.canDeleteUser(user)) {
      this.notificationService.showError('No puede eliminar su propia cuenta');
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar Usuario',
        message: `¿Está seguro que desea eliminar la cuenta de "${user.username}"? Esta acción no se puede deshacer.`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar',
        isDestructive: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.logAdminAction('DELETE_USER', user);
        this.store.dispatch(UsuariosActions.deleteUser({ id: user.id }));
      }
    });
  }

  // Permission methods
  get canManageUsers(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN']);
  }

  get canViewUsers(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }
}
