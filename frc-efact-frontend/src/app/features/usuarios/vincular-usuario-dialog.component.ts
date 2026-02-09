import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { Subject, Observable, startWith, map, takeUntil, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';

import { User } from '../../models/user.model';
import { AsignarUsuarioEmpresaRequest } from '../../models/usuario-empresa.model';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { UsuarioApiService } from '../../core/api/usuario-api.service';
import { UserPreviewCompactComponent } from '../empresas/user-preview-compact.component';

export interface VincularUsuarioDialogData {
  empresaId: number;
}

@Component({
  selector: 'app-vincular-usuario-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatAutocompleteModule,
    MatChipsModule,
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    ReactiveFormsModule,
    UserPreviewCompactComponent
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>link</mat-icon>
      Vincular Usuario a Empresa
    </h2>
    <mat-dialog-content class="dialog-content">
      <form [formGroup]="form">
        <!-- Búsqueda de Usuario -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Buscar Usuario</mat-label>
          <input
            matInput
            formControlName="usuarioSearch"
            [matAutocomplete]="auto"
            placeholder="Escriba el nombre de usuario, email o documento..."
            autocomplete="off"
            (input)="onUserSearch($event)">
          <mat-icon matSuffix>search</mat-icon>
          <mat-autocomplete
            #auto="matAutocomplete"
            [displayWith]="displayUser"
            (optionSelected)="onUserSelected($event)">
            <mat-option *ngFor="let user of filteredUsers$ | async" [value]="user">
              <div class="user-option-simple">
                <div class="user-main">
                  <strong>{{ user.username }}</strong>
                  <span class="user-email">{{ user.email }}</span>
                </div>
              </div>
            </mat-option>
            <mat-option *ngIf="(filteredUsers$ | async)?.length === 0 && searchTerm" disabled>
              <div class="no-results">
                <mat-icon>search_off</mat-icon>
                <span>No se encontraron usuarios</span>
              </div>
            </mat-option>
          </mat-autocomplete>
        </mat-form-field>

        <!-- Información del Usuario Seleccionado -->
        <div *ngIf="selectedUser" class="user-preview-container">
          <app-user-preview-compact [user]="selectedUser"></app-user-preview-compact>
          
          <!-- Información sobre Roles del Sistema vs Rol de Empresa -->
          <div class="roles-info-section" *ngIf="getNormalizedRoles(selectedUser.roles).length > 0">
            <mat-divider></mat-divider>
            <div class="roles-info">
              <div class="info-header">
                <mat-icon>info</mat-icon>
                <span class="info-title">Roles del Sistema</span>
              </div>
              <p class="info-description">
                Los roles del sistema son globales y definen los permisos generales del usuario.
                El rol de empresa define los permisos específicos dentro de esta empresa.
              </p>
              <div class="system-roles-display">
                <mat-chip *ngFor="let role of getNormalizedRoles(selectedUser.roles)" 
                          [class]="'role-' + role.toLowerCase()">
                  <mat-icon>{{ getRoleIcon(role) }}</mat-icon>
                  {{ role }}
                </mat-chip>
              </div>
            </div>
          </div>
        </div>

        <!-- Rol en Empresa -->
        <mat-form-field appearance="outline" class="full-width" *ngIf="selectedUser">
          <mat-label>Rol en Empresa</mat-label>
          <mat-select formControlName="rolEmpresa" required>
            <mat-option value="ADMINISTRADOR">
              <div class="role-option">
                <span class="role-name">Administrador</span>
                <span class="role-desc">Gestionar usuarios y configurar la empresa</span>
              </div>
            </mat-option>
            <mat-option value="FACTURADOR">
              <div class="role-option">
                <span class="role-name">Facturador</span>
                <span class="role-desc">Crear y gestionar facturas</span>
              </div>
            </mat-option>
            <mat-option value="LECTOR">
              <div class="role-option">
                <span class="role-name">Lector</span>
                <span class="role-desc">Solo lectura de información</span>
              </div>
            </mat-option>
          </mat-select>
          <mat-icon matSuffix>business</mat-icon>
          <mat-hint>Este rol es específico para esta empresa y se combina con los roles del sistema</mat-hint>
        </mat-form-field>

        <!-- Confirmación -->
        <div *ngIf="selectedUser" class="confirmation-section">
          <mat-divider></mat-divider>
          <div class="confirmation-text">
            <p><strong>¿Confirmar vinculación?</strong></p>
            <p>Se vinculará al usuario <strong>{{ selectedUser.username }}</strong>
               como <strong>{{ form.get('rolEmpresa')?.value }}</strong> de esta empresa.</p>
          </div>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="dialog-actions">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        (click)="onVincular()"
        [disabled]="!form.valid || !selectedUser || saving">
        <mat-icon>link</mat-icon>
        {{ saving ? 'Vinculando...' : 'Vincular' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2[mat-dialog-title] {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      padding: 16px 24px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.12);
    }

    .dialog-content {
      max-height: calc(90vh - 120px);
      overflow-y: auto;
      padding: 24px;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .user-option-simple {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .user-main {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .user-email {
      font-size: 12px;
      color: #666;
    }

    .no-results {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #999;
      font-style: italic;
      padding: 8px 0;
    }

    .no-results mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .user-preview-container {
      margin: 16px 0;
    }

    .roles-info-section {
      margin-top: 16px;
    }

    .roles-info {
      padding: 12px;
      background-color: #e3f2fd;
      border-radius: 8px;
      border-left: 3px solid #2196f3;
    }

    .info-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
    }

    .info-header mat-icon {
      color: #1976d2;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .info-title {
      font-weight: 500;
      color: #1976d2;
      font-size: 14px;
    }

    .info-description {
      margin: 8px 0;
      font-size: 12px;
      color: #1976d2;
      line-height: 1.4;
    }

    .system-roles-display {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 8px;
    }

    .system-roles-display mat-chip {
      font-size: 11px;
      min-height: 24px;
      padding: 4px 10px;
    }

    .system-roles-display mat-chip mat-icon {
      font-size: 14px;
      width: 14px;
      height: 14px;
      margin-right: 4px;
    }

    .role-option {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .role-name {
      font-weight: 500;
      font-size: 14px;
    }

    .role-desc {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.6);
    }

    mat-hint {
      font-size: 11px;
      color: rgba(0, 0, 0, 0.6);
    }

    .confirmation-section {
      margin-top: 16px;
    }

    .confirmation-text {
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 8px;
      margin-top: 12px;
    }

    .confirmation-text p {
      margin: 6px 0;
      font-size: 14px;
    }

    .dialog-actions {
      padding: 16px 24px;
      border-top: 1px solid rgba(0, 0, 0, 0.12);
      margin: 0;
    }

    mat-dialog-actions button mat-icon {
      margin-right: 8px;
    }

    /* Responsive adjustments */
    @media (max-width: 600px) {
      h2[mat-dialog-title] {
        padding: 12px 16px;
        font-size: 18px;
      }

      .dialog-content {
        max-height: calc(90vh - 100px);
        padding: 16px;
      }

      .dialog-actions {
        padding: 12px 16px;
        flex-direction: column-reverse;
        gap: 8px;
      }

      .dialog-actions button {
        width: 100%;
      }
    }
  `]
})
export class VincularUsuarioDialogComponent implements OnInit, OnDestroy {
  form: FormGroup;
  selectedUser: User | null = null;
  filteredUsers$: Observable<User[]>;
  usuarioSearchControl = new FormControl('');
  searchTerm: string = '';
  saving: boolean = false;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private usuarioApiService: UsuarioApiService,
    private empresaApiService: EmpresaApiService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<VincularUsuarioDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: VincularUsuarioDialogData
  ) {
    this.form = this.fb.group({
      usuarioSearch: [''],
      rolEmpresa: ['FACTURADOR', Validators.required]
    });

    // Configurar búsqueda con debounce
    this.filteredUsers$ = this.usuarioSearchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        this.searchTerm = term || '';
        if (!term || term.length < 2) {
          return of([]);
        }
        return this.usuarioApiService.simpleSearch(term).pipe(
          map(users => users.slice(0, 10)), // Limitar a 10 resultados
          takeUntil(this.destroy$)
        );
      })
    );
  }

  ngOnInit(): void {
    // No necesitamos cargar usuarios por defecto, solo cuando se busca
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  displayUser(user: User): string {
    if (!user || !user.username) {
      return '';
    }
    return `${user.username} (${user.email || 'Sin email'})`;
  }

  onUserSearch(event: any): void {
    const value = event.target.value;
    this.usuarioSearchControl.setValue(value);

    // Limpiar usuario seleccionado si se borra la búsqueda
    if (!value) {
      this.selectedUser = null;
    }
  }

  onUserSelected(event: any): void {
    this.selectedUser = event.option.value;
    this.form.patchValue({
      usuarioSearch: this.selectedUser ? this.displayUser(this.selectedUser) : ''
    });
  }

  onVincular(): void {
    if (this.form.valid && this.selectedUser && !this.saving) {
      this.saving = true;
      const { rolEmpresa } = this.form.value;

      const request: AsignarUsuarioEmpresaRequest = {
        usuarioId: this.selectedUser.id,
        rolEmpresa
      };

      this.empresaApiService.asignarUsuarioEmpresa(this.data.empresaId, request).subscribe({
        next: () => {
          this.saving = false;
          this.snackBar.open(
            `Usuario ${this.selectedUser?.username} vinculado exitosamente como ${rolEmpresa}`,
            'Cerrar',
            { duration: 4000 }
          );
          this.dialogRef.close(true);
        },
        error: (error) => {
          this.saving = false;
          console.error('Error al vincular usuario:', error);
          const errorMessage = error.error?.message || 'Error al vincular usuario';
          this.snackBar.open(errorMessage, 'Cerrar', { duration: 5000 });
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  getNormalizedRoles(roles: any[]): string[] {
    if (!roles || !Array.isArray(roles)) return [];
    return roles.map(role => {
      if (typeof role === 'string') {
        return role;
      } else if (role && typeof role === 'object') {
        return role.nombre || role.name || 'Rol desconocido';
      }
      return 'Rol inválido';
    }).filter(role => role && role !== 'Rol desconocido' && role !== 'Rol inválido');
  }

  getRoleIcon(role: string): string {
    const iconMap: { [key: string]: string } = {
      'ADMIN': 'admin_panel_settings',
      'EMPRESA_ADMIN': 'business',
      'FACTURADOR': 'receipt',
      'LECTOR': 'visibility'
    };
    return iconMap[role.toUpperCase()] || 'person';
  }
}
