import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { UsuarioApiService } from '../../../core/api/usuario-api.service';
import { User, Role, CreateUserRequest } from '../../../models/user.model';
import { ErrorMessageComponent } from '../error-message/error-message.component';
import { Store } from '@ngrx/store';
import * as UsuariosActions from '../../../core/state/usuarios/usuarios.actions';
import { Observable, takeUntil, Subject } from 'rxjs';

export interface UsuarioDialogData {
  empresaId?: number;
  rolEmpresa?: 'ADMINISTRADOR' | 'FACTURADOR' | 'LECTOR';
}

@Component({
  selector: 'app-usuario-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatSnackBarModule,
    MatCheckboxModule,
    ErrorMessageComponent
  ],
  template: `
    <div class="usuario-dialog">
      <h2 mat-dialog-title>
        <mat-icon>person_add</mat-icon>
        Nuevo Usuario
      </h2>
      
      <mat-dialog-content>
        <form [formGroup]="usuarioForm">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Nombre de Usuario</mat-label>
            <input matInput formControlName="username" placeholder="Ingrese el nombre de usuario" autocomplete="off">
            <mat-icon matSuffix>person</mat-icon>
            <app-error-message [control]="usuarioForm.get('username')" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Correo Electrónico</mat-label>
            <input matInput formControlName="email" type="email" placeholder="ejemplo@correo.com" autocomplete="off">
            <mat-icon matSuffix>email</mat-icon>
            <app-error-message [control]="usuarioForm.get('email')" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Contraseña</mat-label>
            <input 
              matInput 
              [type]="hidePassword ? 'password' : 'text'"
              formControlName="password" 
              placeholder="Mínimo 8 caracteres"
              autocomplete="new-password">
            <button
              mat-icon-button
              matSuffix
              type="button"
              (click)="hidePassword = !hidePassword"
              [attr.aria-label]="'Mostrar/ocultar contraseña'">
              <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
            <app-error-message [control]="usuarioForm.get('password')" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Roles</mat-label>
            <mat-select formControlName="roles" multiple>
              <mat-option *ngFor="let role of availableRoles" [value]="role">
                {{ getRoleDisplayName(role) }}
              </mat-option>
            </mat-select>
            <mat-icon matSuffix>security</mat-icon>
            <app-error-message [control]="usuarioForm.get('roles')" />
          </mat-form-field>

          <div class="checkbox-field">
            <mat-checkbox formControlName="isActive">
              Usuario Activo
            </mat-checkbox>
          </div>
        </form>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="onSave()" [disabled]="usuarioForm.invalid || saving">
          {{ saving ? 'Guardando...' : 'Guardar' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .usuario-dialog {
      min-width: 500px;
      max-width: 600px;
    }

    h2[mat-dialog-title] {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      padding: 16px 24px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.12);
    }

    mat-dialog-content {
      padding: 24px;
      max-height: 70vh;
      overflow-y: auto;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .checkbox-field {
      margin-top: 8px;
      margin-bottom: 16px;
    }

    mat-dialog-actions {
      padding: 16px 24px;
      border-top: 1px solid rgba(0, 0, 0, 0.12);
      margin: 0;
    }

    /* Responsive adjustments for mobile */
    @media (max-width: 600px) {
      .usuario-dialog {
        min-width: auto;
        max-width: 100vw;
        width: 100%;
      }

      h2[mat-dialog-title] {
        padding: 12px 16px;
        font-size: 18px;
      }

      mat-dialog-content {
        padding: 16px;
        max-height: calc(90vh - 140px);
      }

      .full-width {
        margin-bottom: 12px;
      }

      mat-dialog-actions {
        padding: 12px 16px;
        flex-direction: column-reverse;
        gap: 8px;
      }

      mat-dialog-actions button {
        width: 100%;
        margin: 0;
      }

      .checkbox-field {
        margin-top: 4px;
        margin-bottom: 12px;
      }
    }
  `]
})
export class UsuarioDialogComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private usuarioApi = inject(UsuarioApiService);
  private snackBar = inject(MatSnackBar);
  private store = inject(Store);
  public dialogRef = inject(MatDialogRef<UsuarioDialogComponent>);
  public data = inject<UsuarioDialogData>(MAT_DIALOG_DATA);

  usuarioForm!: FormGroup;
  availableRoles: string[] = [];
  roles: Role[] = [];
  saving = false;
  hidePassword = true;
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.usuarioForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      roles: [[], Validators.required],
      isActive: [true]
    });

    this.loadAvailableRoles();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadAvailableRoles(): void {
    this.usuarioApi.getAllRoles().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (roles) => {
        this.roles = roles;
        // Filtrar el rol ADMIN de la lista
        this.availableRoles = roles
          .map(role => role.nombre)
          .filter(roleName => roleName !== 'ADMIN');
      },
      error: (error) => {
        console.error('Error al cargar roles:', error);
        // Fallback a roles por defecto sin ADMIN
        this.availableRoles = ['EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR'];
      }
    });
  }

  getRoleDisplayName(roleName: string): string {
    const role = this.roles.find(r => r.nombre === roleName);
    if (role) {
      return `${role.nombre} - ${role.descripcion || ''}`;
    }
    // Fallback para roles conocidos
    switch (roleName) {
      case 'EMPRESA_ADMIN':
        return 'EMPRESA_ADMIN - Administrador de empresa';
      case 'FACTURADOR':
        return 'FACTURADOR - Puede crear facturas';
      case 'LECTOR':
        return 'LECTOR - Solo lectura';
      default:
        return roleName;
    }
  }

  onSave(): void {
    if (this.usuarioForm.invalid) {
      this.usuarioForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const formValue = this.usuarioForm.value;
    
    const createRequest: CreateUserRequest = {
      username: formValue.username,
      email: formValue.email,
      password: formValue.password,
      roles: formValue.roles,
      isActive: formValue.isActive ?? true,
      empresaId: this.data?.empresaId,
      rolEmpresa: this.data?.rolEmpresa
    };

    this.usuarioApi.create(createRequest).subscribe({
      next: (user) => {
        this.saving = false;
        this.snackBar.open('Usuario creado exitosamente', 'Cerrar', { duration: 3000 });
        // Recargar usuarios
        this.store.dispatch(UsuariosActions.loadUsers({}));
        this.dialogRef.close(user);
      },
      error: (error) => {
        this.saving = false;
        const errorMessage = error.error?.message || 'Error al crear el usuario';
        this.snackBar.open(errorMessage, 'Cerrar', { duration: 5000 });
        
        // Manejar errores de validación específicos
        if (error.error?.errors) {
          const errors = error.error.errors;
          if (errors.username) {
            this.usuarioForm.get('username')?.setErrors({ serverValidation: errors.username });
          }
          if (errors.email) {
            this.usuarioForm.get('email')?.setErrors({ serverValidation: errors.email });
          }
        }
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
