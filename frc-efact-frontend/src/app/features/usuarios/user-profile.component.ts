import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Store } from '@ngrx/store';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { selectCurrentUser } from '../../core/state/auth/auth.selectors';
import * as AuthActions from '../../core/state/auth/auth.actions';
import { ProfileApiService } from '../../core/api/profile-api.service';
import { AuthService } from '../../services/auth.service';
import { User, UpdateProfileRequest, ChangePasswordRequest } from '../../models/user.model';
import { AuditLog, AccionEnum } from '../../models/audit.model';
import { take } from 'rxjs/operators';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTableModule,
    MatPaginatorModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss'
})
export class UserProfileComponent implements OnInit {
  private store = inject(Store);
  private auth0 = inject(Auth0Service);
  private profileApi = inject(ProfileApiService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  user$ = this.store.select(selectCurrentUser);

  // Forms
  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  // Activity
  activityLogs = signal<AuditLog[]>([]);
  activityLoading = signal(false);
  activityPageIndex = signal(0);
  activityPageSize = signal(20);
  activityTotalElements = signal(0);

  // UI State
  profileSaving = signal(false);
  passwordChanging = signal(false);
  linkingAccount = signal(false);
  updatingFromAuth0 = signal(false);

  // Activity table columns
  displayedColumns: string[] = ['fechaHora', 'accion', 'entidad', 'descripcion'];

  constructor() {
    // Inicializar formularios con valores por defecto para evitar errores en el template
    this.profileForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    // Recargar usuario desde el backend para asegurar que tenemos los datos más actualizados (especialmente auth0Id)
    this.profileApi.getProfile().subscribe({
      next: (freshUser) => {
        const mappedUser = this.authService.mapBackendUserToFrontend(freshUser);
        this.store.dispatch(AuthActions.loadUserSuccess({ user: mappedUser }));
        this.authService.updateCurrentUser(mappedUser);
        localStorage.setItem('current_user', JSON.stringify(mappedUser));
        this.initForms();
      },
      error: (err) => {
        console.error('Error loading fresh user data:', err);
        // Si falla, usar el usuario del store
        this.initForms();
      }
    });
    
    this.loadActivity();
  }

  private initForms(): void {
    this.user$.pipe(take(1)).subscribe(user => {
      if (user) {
        this.profileForm.patchValue({
          username: user.username,
          email: user.email
        });

        // El passwordForm no necesita actualizarse ya que siempre empieza vacío
      }
    });
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const newPassword = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');

    if (!newPassword || !confirmPassword) {
      return null;
    }

    return newPassword.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  getRoles(user: User): string {
    if (!user.roles) return 'Sin roles';
    if (Array.isArray(user.roles)) {
      return user.roles.map((r: any) => (typeof r === 'string' ? r : r.nombre)).join(', ');
    }
    return '';
  }

  getRoleName(role: any): string {
    return typeof role === 'string' ? role : role.nombre;
  }

  getRolesArray(user: User): any[] {
    if (!user.roles) return [];
    if (Array.isArray(user.roles)) {
      return user.roles;
    }
    return [];
  }

  canChangePassword(user: User | null): boolean {
    if (!user) return false;
    return !user.auth0Id || user.auth0Id.trim() === '';
  }

  onSaveProfile(): void {
    if (this.profileForm.invalid) {
      this.markFormGroupTouched(this.profileForm);
      return;
    }

    this.profileSaving.set(true);
    const data: UpdateProfileRequest = this.profileForm.value;

    this.profileApi.updateProfile(data).subscribe({
      next: (updatedUser) => {
        // Asegurar que auth0Id se mapee correctamente
        const mappedUser = this.authService.mapBackendUserToFrontend(updatedUser);
        // Update NgRx store
        this.store.dispatch(AuthActions.loadUserSuccess({ user: mappedUser }));
        // Update AuthService
        this.authService.updateCurrentUser(mappedUser);
        // Update localStorage
        localStorage.setItem('current_user', JSON.stringify(mappedUser));
        
        this.snackBar.open('Perfil actualizado exitosamente', 'Cerrar', { duration: 3000 });
        this.profileSaving.set(false);
      },
      error: (err) => {
        const errorMsg = err.error?.error || 'Error al actualizar el perfil';
        this.snackBar.open(errorMsg, 'Cerrar', { duration: 5000 });
        this.profileSaving.set(false);
      }
    });
  }

  onChangePassword(): void {
    if (this.passwordForm.invalid) {
      this.markFormGroupTouched(this.passwordForm);
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Cambiar Contraseña',
        message: '¿Está seguro de que desea cambiar su contraseña?',
        confirmText: 'Cambiar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.passwordChanging.set(true);
        const data: ChangePasswordRequest = this.passwordForm.value;

        this.profileApi.changePassword(data).subscribe({
          next: () => {
            this.snackBar.open('Contraseña actualizada exitosamente', 'Cerrar', { duration: 3000 });
            this.passwordForm.reset();
            this.passwordChanging.set(false);
          },
          error: (err) => {
            const errorMsg = err.error?.error || 'Error al cambiar la contraseña';
            this.snackBar.open(errorMsg, 'Cerrar', { duration: 5000 });
            this.passwordChanging.set(false);
          }
        });
      }
    });
  }

  onLinkAccount(): void {
    this.linkingAccount.set(true);
    this.auth0.loginWithPopup().subscribe({
      next: () => {
        this.auth0.user$.pipe(take(1)).subscribe(auth0User => {
          if (auth0User && auth0User.sub) {
            this.profileApi.linkAuth0Account(auth0User.sub).subscribe({
              next: () => {
                this.snackBar.open('Cuenta vinculada exitosamente', 'Cerrar', { duration: 3000 });
                this.reloadUser();
                this.linkingAccount.set(false);
              },
              error: (err) => {
                const errorMsg = err.error?.error || 'Error al vincular la cuenta';
                this.snackBar.open(errorMsg, 'Cerrar', { duration: 5000 });
                this.linkingAccount.set(false);
              }
            });
          }
        });
      },
      error: (err) => {
        console.error('Error login popup', err);
        this.snackBar.open('Error al iniciar sesión con Auth0', 'Cerrar', { duration: 5000 });
        this.linkingAccount.set(false);
      }
    });
  }

  onUpdateFromAuth0(): void {
    this.updatingFromAuth0.set(true);
    
    // Obtener el token de Auth0 (de localStorage o directamente de Auth0)
    this.authService.getTokenAsync().subscribe({
      next: (token) => {
        if (!token) {
          this.snackBar.open('No se pudo obtener el token de Auth0. Por favor, inicie sesión nuevamente.', 'Cerrar', { duration: 5000 });
          this.updatingFromAuth0.set(false);
          return;
        }
        
        // Enviar el token en la request
        this.profileApi.updateFromAuth0(token).subscribe({
          next: (updatedUser) => {
            const mappedUser = this.authService.mapBackendUserToFrontend(updatedUser);
            this.store.dispatch(AuthActions.loadUserSuccess({ user: mappedUser }));
            this.authService.updateCurrentUser(mappedUser);
            localStorage.setItem('current_user', JSON.stringify(mappedUser));
            this.snackBar.open('Información actualizada desde Google exitosamente', 'Cerrar', { duration: 3000 });
            this.updatingFromAuth0.set(false);
            this.initForms(); // Reinitialize forms with new user data
          },
          error: (err) => {
            const errorMsg = err.error?.error || 'Error al actualizar información desde Google';
            this.snackBar.open(errorMsg, 'Cerrar', { duration: 5000 });
            this.updatingFromAuth0.set(false);
          }
        });
      },
      error: (err) => {
        this.snackBar.open('Error al obtener el token de Auth0. Por favor, inicie sesión nuevamente.', 'Cerrar', { duration: 5000 });
        this.updatingFromAuth0.set(false);
      }
    });
  }

  onUnlinkAccount(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Desvincular Cuenta',
        message: '¿Está seguro de que desea desvincular su cuenta de Auth0/Google?',
        confirmText: 'Desvincular',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.profileApi.unlinkAuth0Account().subscribe({
          next: () => {
            this.snackBar.open('Cuenta desvinculada exitosamente', 'Cerrar', { duration: 3000 });
            this.reloadUser();
          },
          error: (err) => {
            const errorMsg = err.error?.error || 'Error al desvincular la cuenta';
            this.snackBar.open(errorMsg, 'Cerrar', { duration: 5000 });
          }
        });
      }
    });
  }

  private reloadUser(): void {
    this.profileApi.getProfile().subscribe({
      next: (user) => {
        // Asegurar que auth0Id se mapee correctamente
        const mappedUser = this.authService.mapBackendUserToFrontend(user);
        this.store.dispatch(AuthActions.loadUserSuccess({ user: mappedUser }));
        this.authService.updateCurrentUser(mappedUser);
        localStorage.setItem('current_user', JSON.stringify(mappedUser));
        this.initForms(); // Reinitialize forms with new user data
      },
      error: (err) => {
        console.error('Error reloading user:', err);
      }
    });
  }

  loadActivity(): void {
    this.activityLoading.set(true);
    this.profileApi.getActivity(this.activityPageIndex(), this.activityPageSize()).subscribe({
      next: (page) => {
        this.activityLogs.set(page.content);
        this.activityTotalElements.set(page.totalElements);
        this.activityLoading.set(false);
      },
      error: (err) => {
        console.error('Error loading activity:', err);
        this.activityLoading.set(false);
      }
    });
  }

  onActivityPageChange(event: PageEvent): void {
    this.activityPageIndex.set(event.pageIndex);
    this.activityPageSize.set(event.pageSize);
    this.loadActivity();
  }

  getAccionLabel(accion: AccionEnum): string {
    const labels: Record<AccionEnum, string> = {
      [AccionEnum.CREATE]: 'Crear',
      [AccionEnum.UPDATE]: 'Actualizar',
      [AccionEnum.DELETE]: 'Eliminar',
      [AccionEnum.READ]: 'Leer'
    };
    return labels[accion] || accion;
  }

  getAccionColor(accion: AccionEnum): string {
    const colors: Record<AccionEnum, string> = {
      [AccionEnum.CREATE]: 'primary',
      [AccionEnum.UPDATE]: 'accent',
      [AccionEnum.DELETE]: 'warn',
      [AccionEnum.READ]: ''
    };
    return colors[accion] || '';
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  getFieldError(form: FormGroup, fieldName: string): string {
    const field = form.get(fieldName);
    if (field?.hasError('required')) {
      return 'Este campo es requerido';
    }
    if (field?.hasError('email')) {
      return 'Email inválido';
    }
    if (field?.hasError('minlength')) {
      return `Mínimo ${field.errors?.['minlength'].requiredLength} caracteres`;
    }
    if (field?.hasError('maxlength')) {
      return `Máximo ${field.errors?.['maxlength'].requiredLength} caracteres`;
    }
    if (form.hasError('passwordMismatch') && fieldName === 'confirmPassword') {
      return 'Las contraseñas no coinciden';
    }
    return '';
  }
}
