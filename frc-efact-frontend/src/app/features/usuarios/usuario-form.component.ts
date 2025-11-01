import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Store } from '@ngrx/store';
import { Observable, Subject, of } from 'rxjs';
import { takeUntil, map, catchError, debounceTime, distinctUntilChanged, switchMap, startWith } from 'rxjs/operators';

import { User, CreateUserRequest, UpdateUserRequest, Role } from '../../models/user.model';
import { UsuarioApiService } from '../../core/api/usuario-api.service';
import { NotificationService } from '../../core/services/notification.service';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import * as UsuariosActions from '../../core/state/usuarios/usuarios.actions';
import { selectUserById, selectUsersLoading, selectUsersError } from '../../core/state/usuarios/usuarios.selectors';
import { selectCurrentUser } from '../../core/state/auth/auth.selectors';

export type FormMode = 'create' | 'edit' | 'view';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    ErrorMessageComponent
  ],
  template: `
    <div class="usuario-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>{{ getFormIcon() }}</mat-icon>
            {{ getFormTitle() }}
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="usuarioForm" (ngSubmit)="onSubmit()" class="usuario-form">
            <!-- Username Field -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Nombre de Usuario</mat-label>
              <input
                matInput
                formControlName="username"
                placeholder="Ingrese el nombre de usuario"
                [readonly]="formMode === 'view'"
                autocomplete="username"
                (blur)="onFieldBlur('username')"
                (input)="onFieldInput('username')">
              <mat-icon matSuffix [class.validating]="isValidatingUsername">
                {{ isValidatingUsername ? 'hourglass_empty' : 'person' }}
              </mat-icon>

              <!-- Real-time validation feedback -->
              <mat-hint *ngIf="!usuarioForm.get('username')?.errors && usuarioForm.get('username')?.value && !isValidatingUsername"
                        class="success-hint">
                <mat-icon class="success-icon">check_circle</mat-icon>
                Nombre de usuario disponible
              </mat-hint>

              <!-- Username validation errors -->
              <app-error-message [control]="usuarioForm.get('username')"></app-error-message>
            </mat-form-field>

            <!-- Email Field -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Correo Electrónico</mat-label>
              <input
                matInput
                formControlName="email"
                type="email"
                placeholder="Ingrese el correo electrónico"
                [readonly]="formMode === 'view'"
                autocomplete="email"
                (blur)="onFieldBlur('email')"
                (input)="onFieldInput('email')">
              <mat-icon matSuffix [class.validating]="isValidatingEmail">
                {{ isValidatingEmail ? 'hourglass_empty' : 'email' }}
              </mat-icon>

              <!-- Real-time validation feedback -->
              <mat-hint *ngIf="!usuarioForm.get('email')?.errors && usuarioForm.get('email')?.value && !isValidatingEmail && usuarioForm.get('email')?.valid"
                        class="success-hint">
                <mat-icon class="success-icon">check_circle</mat-icon>
                Correo electrónico disponible
              </mat-hint>

              <!-- Email validation errors -->
              <app-error-message [control]="usuarioForm.get('email')"></app-error-message>
            </mat-form-field>

            <!-- Password Field (only for create mode) -->
            <mat-form-field appearance="outline" class="full-width" *ngIf="formMode === 'create'">
              <mat-label>Contraseña</mat-label>
              <input
                matInput
                formControlName="password"
                [type]="hidePassword ? 'password' : 'text'"
                placeholder="Ingrese la contraseña"
                autocomplete="new-password"
                (input)="onPasswordInput()">
              <button
                mat-icon-button
                matSuffix
                type="button"
                (click)="hidePassword = !hidePassword"
                [attr.aria-label]="'Hide password'"
                [attr.aria-pressed]="hidePassword">
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>

              <!-- Password strength indicator -->
              <mat-hint *ngIf="passwordStrength && usuarioForm.get('password')?.value"
                        [class]="'password-strength-' + passwordStrength.level">
                Fortaleza: {{ passwordStrength.label }}
              </mat-hint>

              <!-- Password validation errors -->
              <app-error-message [control]="usuarioForm.get('password')"></app-error-message>

              <!-- Password requirements help -->
              <div class="password-requirements" *ngIf="usuarioForm.get('password')?.touched && usuarioForm.get('password')?.hasError('passwordComplexity')">
                <p class="requirements-title">La contraseña debe contener:</p>
                <ul class="requirements-list">
                  <li [class.met]="passwordRequirements.hasUpperCase">
                    <mat-icon>{{ passwordRequirements.hasUpperCase ? 'check' : 'close' }}</mat-icon>
                    Al menos una letra mayúscula
                  </li>
                  <li [class.met]="passwordRequirements.hasLowerCase">
                    <mat-icon>{{ passwordRequirements.hasLowerCase ? 'check' : 'close' }}</mat-icon>
                    Al menos una letra minúscula
                  </li>
                  <li [class.met]="passwordRequirements.hasNumber">
                    <mat-icon>{{ passwordRequirements.hasNumber ? 'check' : 'close' }}</mat-icon>
                    Al menos un número
                  </li>
                  <li [class.met]="passwordRequirements.hasSpecialChar">
                    <mat-icon>{{ passwordRequirements.hasSpecialChar ? 'check' : 'close' }}</mat-icon>
                    Al menos un carácter especial
                  </li>
                  <li [class.met]="passwordRequirements.hasMinLength">
                    <mat-icon>{{ passwordRequirements.hasMinLength ? 'check' : 'close' }}</mat-icon>
                    Mínimo 8 caracteres
                  </li>
                </ul>
              </div>
            </mat-form-field>

            <!-- Roles Field -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Roles</mat-label>
              <mat-select
                formControlName="roles"
                multiple>
                <mat-option *ngFor="let role of availableRoles" [value]="role">
                  {{ getRoleDisplayName(role) }}
                </mat-option>
              </mat-select>
              <mat-icon matSuffix>security</mat-icon>

              <!-- Roles validation errors -->
              <app-error-message [control]="usuarioForm.get('roles')"></app-error-message>
            </mat-form-field>

            <!-- Active Status Field -->
            <div class="checkbox-field" *ngIf="formMode !== 'view'">
              <mat-checkbox formControlName="isActive">
                Usuario Activo
              </mat-checkbox>
            </div>

            <!-- Active Status Display (view mode) -->
            <div class="status-display" *ngIf="formMode === 'view'">
              <mat-icon [class]="user?.isActive ? 'status-active' : 'status-inactive'">
                {{ user?.isActive ? 'check_circle' : 'cancel' }}
              </mat-icon>
              <span>{{ user?.isActive ? 'Usuario Activo' : 'Usuario Inactivo' }}</span>
            </div>

            <!-- Additional Info (view/edit mode) -->
            <div class="additional-info" *ngIf="formMode !== 'create' && user">
              <div class="info-row">
                <strong>Creado:</strong> {{ user.creadoEn | date:'dd/MM/yyyy HH:mm' }}
              </div>
              <div class="info-row" *ngIf="user.actualizadoEn !== user.creadoEn">
                <strong>Última modificación:</strong> {{ user.actualizadoEn | date:'dd/MM/yyyy HH:mm' }}
              </div>
              <div class="info-row" *ngIf="user.ultimoLogin">
                <strong>Último acceso:</strong> {{ user.ultimoLogin | date:'dd/MM/yyyy HH:mm' }}
              </div>
              <div class="info-row" *ngIf="(user.failedLoginAttempts || 0) > 0">
                <strong>Intentos fallidos:</strong> {{ user.failedLoginAttempts }}
              </div>
              <div class="info-row" *ngIf="user.lockedUntil">
                <strong>Bloqueado hasta:</strong> {{ user.lockedUntil | date:'dd/MM/yyyy HH:mm' }}
              </div>
            </div>
          </form>
        </mat-card-content>

        <mat-card-actions align="end">
          <button
            mat-button
            type="button"
            (click)="onCancel()">
            {{ formMode === 'view' ? 'Cerrar' : 'Cancelar' }}
          </button>

          <button
            mat-raised-button
            color="primary"
            type="submit"
            [disabled]="!canSubmit()"
            (click)="onSubmit()"
            *ngIf="formMode !== 'view'">
            <mat-progress-spinner
              *ngIf="isSubmitting"
              diameter="20"
              mode="indeterminate"
              style="display: inline-block; margin-right: 8px;">
            </mat-progress-spinner>
            <mat-icon *ngIf="!isSubmitting">save</mat-icon>
            {{ isSubmitting ? 'Guardando...' : (formMode === 'create' ? 'Crear Usuario' : 'Guardar Cambios') }}
          </button>

          <button
            mat-raised-button
            color="accent"
            type="button"
            (click)="onEdit()"
            *ngIf="formMode === 'view'">
            <mat-icon>edit</mat-icon>
            Editar
          </button>
        </mat-card-actions>
      </mat-card>

      <!-- Loading Spinner -->
      <div class="loading-overlay" *ngIf="loading$ | async">
        <mat-progress-spinner mode="indeterminate" diameter="50"></mat-progress-spinner>
      </div>
    </div>
  `,
  styles: [`
    .usuario-form-container {
      max-width: 600px;
      margin: 2rem auto;
      padding: 1rem;
      position: relative;
    }

    .usuario-form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      margin-top: 1rem;
    }

    .full-width {
      width: 100%;
    }

    .checkbox-field {
      margin: 1rem 0;
    }

    .status-display {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin: 1rem 0;
      padding: 0.5rem;
      border-radius: 4px;
      background-color: #f5f5f5;
    }

    .status-active {
      color: #4caf50;
    }

    .status-inactive {
      color: #f44336;
    }

    .additional-info {
      margin-top: 1.5rem;
      padding: 1rem;
      background-color: #f9f9f9;
      border-radius: 4px;
    }

    .info-row {
      margin-bottom: 0.5rem;
    }

    .info-row:last-child {
      margin-bottom: 0;
    }

    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(255, 255, 255, 0.8);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 1000;
    }

    mat-card-header {
      margin-bottom: 1rem;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    mat-card-actions {
      margin-top: 1.5rem;
      padding-top: 1rem;
      border-top: 1px solid #e0e0e0;
    }

    /* Validation feedback styles */
    .success-hint {
      color: #4caf50;
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
    }

    .success-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .validating {
      animation: spin 1s linear infinite;
      color: #ff9800;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    /* Password strength styles */
    .password-strength-weak {
      color: #f44336;
    }

    .password-strength-medium {
      color: #ff9800;
    }

    .password-strength-strong {
      color: #4caf50;
    }

    .password-strength-very-strong {
      color: #2e7d32;
    }

    /* Password requirements styles */
    .password-requirements {
      margin-top: 8px;
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
      border-left: 4px solid #ff9800;
    }

    .requirements-title {
      margin: 0 0 8px 0;
      font-weight: 500;
      color: rgba(0, 0, 0, 0.87);
      font-size: 14px;
    }

    .requirements-list {
      margin: 0;
      padding: 0;
      list-style: none;
    }

    .requirements-list li {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
      font-size: 12px;
      color: rgba(0, 0, 0, 0.6);
    }

    .requirements-list li.met {
      color: #4caf50;
    }

    .requirements-list li mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .requirements-list li.met mat-icon {
      color: #4caf50;
    }

    .requirements-list li:not(.met) mat-icon {
      color: #f44336;
    }
  `]
})
export class UsuarioFormComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly store = inject(Store);
  private readonly snackBar = inject(MatSnackBar);
  private readonly usuarioApiService = inject(UsuarioApiService);
  private readonly notificationService = inject(NotificationService);
  private readonly destroy$ = new Subject<void>();

  usuarioForm!: FormGroup;
  formMode: FormMode = 'create';
  user: User | null = null;
  userId: number | null = null;
  hidePassword = true;
  currentUser: User | null = null;

  // Available roles - loaded from backend
  availableRoles: string[] = [];
  roles: Role[] = [];

  // Observables
  loading$ = this.store.select(selectUsersLoading);
  error$ = this.store.select(selectUsersError);
  currentUser$ = this.store.select(selectCurrentUser);

  // Form submission state
  isSubmitting = false;

  // Validation state
  isValidatingUsername = false;
  isValidatingEmail = false;

  // Password strength tracking
  passwordStrength: { level: string; label: string; score: number } | null = null;
  passwordRequirements = {
    hasUpperCase: false,
    hasLowerCase: false,
    hasNumber: false,
    hasSpecialChar: false,
    hasMinLength: false
  };

  ngOnInit(): void {
    this.initializeComponent();
    this.subscribeToFormErrors();
    this.subscribeToCurrentUser();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeComponent(): void {
    // Determine form mode and user ID from route
    this.determineFormMode();

    // Load available roles
    this.loadAvailableRoles();

    // Initialize form
    this.initializeForm();

    // Load user data if editing or viewing
    if (this.userId && this.formMode !== 'create') {
      this.loadUser();
    }
  }

  private determineFormMode(): void {
    const url = this.router.url;
    const userId = this.route.snapshot.paramMap.get('id');

    if (url.includes('/new')) {
      this.formMode = 'create';
    } else if (url.includes('/edit')) {
      this.formMode = 'edit';
      this.userId = userId ? parseInt(userId, 10) : null;
    } else if (userId) {
      this.formMode = 'view';
      this.userId = parseInt(userId, 10);
    }
  }

  private initializeForm(): void {
    this.usuarioForm = this.fb.group({
      username: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(50)
        ],
        [this.createUsernameAsyncValidator()]
      ],
      email: [
        '',
        [
          Validators.required,
          Validators.email
        ],
        [this.createEmailAsyncValidator()]
      ],
      password: [
        '',
        this.formMode === 'create' ? [
          Validators.required,
          Validators.minLength(8),
          this.passwordComplexityValidator
        ] : []
      ],
      roles: [[], Validators.required],
      isActive: [true]
    });

    // Disable form if in view mode
    if (this.formMode === 'view') {
      this.usuarioForm.disable();
    }
  }

  private loadUser(): void {
    if (!this.userId) return;

    this.store.select(selectUserById(this.userId))
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user) {
          this.user = user;
          this.populateForm(user);
        } else {
          // Load user from API if not in store
          this.store.dispatch(UsuariosActions.loadUsers({}));
        }
      });
  }

  private loadAvailableRoles(): void {
    this.usuarioApiService.getAllRoles().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (roles) => {
        this.roles = roles;
        this.availableRoles = roles.map(role => role.nombre);
      },
      error: (error) => {
        console.error('Error al cargar roles:', error);
        // Fallback a roles por defecto si hay error
        this.availableRoles = ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR'];
      }
    });
  }

  private populateForm(user: User): void {
    this.usuarioForm.patchValue({
      username: user.username,
      email: user.email,
      roles: user.roles,
      isActive: user.isActive
    });
  }

  // Custom Validators
  private passwordComplexityValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;

    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumeric = /[0-9]/.test(value);
    const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value);

    const valid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecialChar;

    return valid ? null : { passwordComplexity: true };
  }

  // Async Validators
  private createUsernameAsyncValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value || control.value.trim() === '') {
        this.isValidatingUsername = false;
        return of(null);
      }

      const username = control.value.trim();

      return of(control.value).pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap(value => {
          if (!value || value.trim() === '') {
            this.isValidatingUsername = false;
            return of(null);
          }

          this.isValidatingUsername = true;

          return this.usuarioApiService.checkUsernameAvailability(
            value.trim(),
            this.userId || undefined
          ).pipe(
            map(result => {
              this.isValidatingUsername = false;
              return result.available ? null : { usernameExists: true };
            }),
            catchError((error) => {
              this.isValidatingUsername = false;
              console.error('Username validation error:', error);
              return of({ usernameValidationError: true });
            })
          );
        })
      );
    };
  }

  private createEmailAsyncValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value || control.value.trim() === '') {
        this.isValidatingEmail = false;
        return of(null);
      }

      const email = control.value.trim();

      return of(control.value).pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap(value => {
          if (!value || value.trim() === '') {
            this.isValidatingEmail = false;
            return of(null);
          }

          this.isValidatingEmail = true;

          return this.usuarioApiService.checkEmailAvailability(
            value.trim(),
            this.userId || undefined
          ).pipe(
            map(result => {
              this.isValidatingEmail = false;
              return result.available ? null : { emailExists: true };
            }),
            catchError((error) => {
              this.isValidatingEmail = false;
              console.error('Email validation error:', error);
              return of({ emailValidationError: true });
            })
          );
        })
      );
    };
  }

  // Field interaction handlers
  onFieldBlur(fieldName: string): void {
    const control = this.usuarioForm.get(fieldName);
    if (control) {
      control.markAsTouched();
      control.updateValueAndValidity();
    }
  }

  onFieldInput(fieldName: string): void {
    const control = this.usuarioForm.get(fieldName);
    if (control && control.errors && control.errors['serverValidation']) {
      // Clear server validation errors when user starts typing
      const errors = { ...control.errors };
      delete errors['serverValidation'];
      delete errors['serverError'];

      if (Object.keys(errors).length === 0) {
        control.setErrors(null);
      } else {
        control.setErrors(errors);
      }
    }
  }

  onPasswordInput(): void {
    const password = this.usuarioForm.get('password')?.value || '';
    this.updatePasswordRequirements(password);
    this.updatePasswordStrength(password);
  }

  private updatePasswordRequirements(password: string): void {
    this.passwordRequirements = {
      hasUpperCase: /[A-Z]/.test(password),
      hasLowerCase: /[a-z]/.test(password),
      hasNumber: /[0-9]/.test(password),
      hasSpecialChar: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password),
      hasMinLength: password.length >= 8
    };
  }

  private updatePasswordStrength(password: string): void {
    if (!password) {
      this.passwordStrength = null;
      return;
    }

    let score = 0;
    const requirements = this.passwordRequirements;

    // Calculate strength score
    if (requirements.hasMinLength) score++;
    if (requirements.hasUpperCase) score++;
    if (requirements.hasLowerCase) score++;
    if (requirements.hasNumber) score++;
    if (requirements.hasSpecialChar) score++;

    // Additional points for length
    if (password.length >= 12) score++;
    if (password.length >= 16) score++;

    // Determine strength level
    if (score <= 2) {
      this.passwordStrength = { level: 'weak', label: 'Débil', score };
    } else if (score <= 4) {
      this.passwordStrength = { level: 'medium', label: 'Media', score };
    } else if (score <= 5) {
      this.passwordStrength = { level: 'strong', label: 'Fuerte', score };
    } else {
      this.passwordStrength = { level: 'very-strong', label: 'Muy Fuerte', score };
    }
  }

  // Form Actions
  onSubmit(): void {
    if (!this.canSubmit()) return;

    // Mark form as submitted to show validation errors
    this.usuarioForm.markAllAsTouched();

    if (!this.usuarioForm.valid) {
      this.notificationService.showValidationError();
      this.scrollToFirstError();
      return;
    }

    this.isSubmitting = true;
    const formValue = this.usuarioForm.value;

    if (this.formMode === 'create') {
      this.createUser(formValue);
    } else if (this.formMode === 'edit') {
      this.updateUser(formValue);
    }
  }

  private scrollToFirstError(): void {
    const firstErrorElement = document.querySelector('.mat-mdc-form-field-error');
    if (firstErrorElement) {
      firstErrorElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }

  private createUser(formValue: any): void {
    const createRequest: CreateUserRequest = {
      username: formValue.username.trim(),
      email: formValue.email.trim(),
      password: formValue.password,
      roles: formValue.roles,
      isActive: formValue.isActive
    };

    // Log admin action
    this.logAdminAction('CREATE_USER', undefined, {
      username: createRequest.username,
      email: createRequest.email,
      roles: createRequest.roles,
      isActive: createRequest.isActive
    });

    this.store.dispatch(UsuariosActions.createUser({ user: createRequest }));

    // Mark form as pristine to avoid unsaved changes warning
    this.usuarioForm.markAsPristine();
  }

  private updateUser(formValue: any): void {
    if (!this.userId) return;

    const updateRequest: UpdateUserRequest = {
      username: formValue.username.trim(),
      email: formValue.email.trim(),
      roles: formValue.roles,
      isActive: formValue.isActive
    };

    // Log admin action with changes
    const changes: any = {};
    if (this.user) {
      if (this.user.username !== updateRequest.username) {
        changes.username = { from: this.user.username, to: updateRequest.username };
      }
      if (this.user.email !== updateRequest.email) {
        changes.email = { from: this.user.email, to: updateRequest.email };
      }
      if (JSON.stringify(this.user.roles) !== JSON.stringify(updateRequest.roles)) {
        changes.roles = { from: this.user.roles, to: updateRequest.roles };
      }
      if (this.user.isActive !== updateRequest.isActive) {
        changes.isActive = { from: this.user.isActive, to: updateRequest.isActive };
      }
    }

    this.logAdminAction('UPDATE_USER', this.user || undefined, { changes });

    this.store.dispatch(UsuariosActions.updateUser({
      id: this.userId,
      user: updateRequest
    }));

    // Mark form as pristine to avoid unsaved changes warning
    this.usuarioForm.markAsPristine();
  }

  onCancel(): void {
    // Check if form has unsaved changes
    if (this.usuarioForm.dirty && this.formMode !== 'view') {
      const confirmLeave = confirm('¿Está seguro de que desea salir? Los cambios no guardados se perderán.');
      if (!confirmLeave) {
        return;
      }
    }
    this.router.navigate(['/usuarios']);
  }

  onEdit(): void {
    if (this.userId) {
      this.router.navigate(['/usuarios', this.userId, 'edit']);
    }
  }

  // Error handling
  private subscribeToFormErrors(): void {
    // Subscribe to form submission results to reset submission state
    this.store.select(selectUsersError)
      .pipe(takeUntil(this.destroy$))
      .subscribe(error => {
        if (error) {
          this.isSubmitting = false;
          this.handleServerValidationErrors(error);
        }
      });

    // Reset submission state on successful operations
    this.store.select(selectUsersLoading)
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => {
        if (!loading && this.isSubmitting) {
          this.isSubmitting = false;
        }
      });
  }

  private handleServerValidationErrors(error: string): void {
    try {
      // Try to parse error as JSON for structured error handling
      const parsedError = JSON.parse(error);

      if (parsedError.fieldErrors) {
        // Handle field-specific errors
        Object.keys(parsedError.fieldErrors).forEach(field => {
          const control = this.usuarioForm.get(field);
          if (control) {
            control.setErrors({
              serverValidation: parsedError.fieldErrors[field]
            });
          }
        });
      }
    } catch {
      // Handle string-based errors
      if (error.toLowerCase().includes('username')) {
        if (error.toLowerCase().includes('already exists') || error.toLowerCase().includes('ya existe')) {
          this.usuarioForm.get('username')?.setErrors({ usernameExists: true });
        } else {
          this.usuarioForm.get('username')?.setErrors({ serverValidation: error });
        }
      } else if (error.toLowerCase().includes('email')) {
        if (error.toLowerCase().includes('already exists') || error.toLowerCase().includes('ya existe')) {
          this.usuarioForm.get('email')?.setErrors({ emailExists: true });
        } else {
          this.usuarioForm.get('email')?.setErrors({ serverValidation: error });
        }
      } else if (error.toLowerCase().includes('password') || error.toLowerCase().includes('contraseña')) {
        this.usuarioForm.get('password')?.setErrors({ serverValidation: error });
      } else if (error.toLowerCase().includes('roles') || error.toLowerCase().includes('rol')) {
        this.usuarioForm.get('roles')?.setErrors({ serverValidation: error });
      } else {
        // Generic server error - show as notification
        this.notificationService.showError(error);
      }
    }

    // Mark form as touched to show errors
    this.usuarioForm.markAllAsTouched();

    // Scroll to first error
    setTimeout(() => this.scrollToFirstError(), 100);
  }

  // Helper Methods
  canSubmit(): boolean {
    return this.usuarioForm.valid && !this.usuarioForm.pending && !this.isSubmitting && this.formMode !== 'view';
  }

  getFormTitle(): string {
    switch (this.formMode) {
      case 'create':
        return 'Crear Nuevo Usuario';
      case 'edit':
        return 'Editar Usuario';
      case 'view':
        return 'Detalles del Usuario';
      default:
        return 'Usuario';
    }
  }

  getFormIcon(): string {
    switch (this.formMode) {
      case 'create':
        return 'person_add';
      case 'edit':
        return 'edit';
      case 'view':
        return 'person';
      default:
        return 'person';
    }
  }

  getRoleDisplayName(roleName: string): string {
    const role = this.roles.find(r => r.nombre === roleName);
    if (role) {
      return `${role.nombre} - ${role.descripcion}`;
    }

    // Fallback para roles conocidos
    switch (roleName) {
      case 'ADMIN':
        return 'ADMIN - Administrador del sistema';
      case 'EMPRESA_ADMIN':
        return 'EMPRESA_ADMIN - Administrador de empresa';
      case 'FACTURADOR':
        return 'FACTURADOR - Usuario facturador';
      case 'LECTOR':
        return 'LECTOR - Usuario de solo lectura';
      default:
        return roleName;
    }
  }

  // Security methods
  private subscribeToCurrentUser(): void {
    this.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      this.currentUser = user;
    });
  }

  private canEditUser(): boolean {
    // Allow editing all users for now
    return true;
  }

  private logAdminAction(action: string, targetUser?: User, details?: any): void {
    // Log administrative actions for audit trail
    const logData = {
      action,
      targetUserId: targetUser?.id || this.userId,
      targetUsername: targetUser?.username || this.user?.username,
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
