import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { LoginRequest } from '../../models/login-request.model';
import * as AuthActions from '../../core/state/auth/auth.actions';
import { selectAuthLoading, selectAuthError } from '../../core/state/auth/auth.selectors';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  private router = inject(Router);
  private auth0 = inject(Auth0Service);

  loginForm: FormGroup;
  isLoading$: Observable<boolean>;
  errorMessage$: Observable<string | null>;
  hidePassword = true;

  constructor() {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    // Inicializar observables desde el store
    this.isLoading$ = this.store.select(selectAuthLoading);
    this.errorMessage$ = this.store.select(selectAuthError);
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }

    const credentials: LoginRequest = this.loginForm.value;

    // Despachar acción de login usando NgRx
    this.store.dispatch(AuthActions.login({
      username: credentials.username,
      password: credentials.password
    }));
  }

  loginWithAuth0(): void {
    this.auth0.loginWithRedirect();
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  private getErrorMessage(error: any): string {
    if (error.status === 401) {
      return 'Usuario o contraseña incorrectos';
    } else if (error.status === 429) {
      return 'Demasiados intentos. Por favor, intente más tarde';
    } else if (error.status === 0) {
      return 'No se pudo conectar con el servidor';
    } else {
      return error.error?.message || 'Error al iniciar sesión. Por favor, intente nuevamente';
    }
  }

  getFieldError(fieldName: string): string {
    const control = this.loginForm.get(fieldName);
    if (control?.hasError('required') && control.touched) {
      return 'Este campo es requerido';
    }
    if (control?.hasError('minlength') && control.touched) {
      const minLength = control.errors?.['minlength'].requiredLength;
      return `Mínimo ${minLength} caracteres`;
    }
    return '';
  }
}
