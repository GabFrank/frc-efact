import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AbstractControl } from '@angular/forms';

@Component({
  selector: 'app-error-message',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  template: `
    <!-- Form control errors -->
    <div class="form-error" *ngIf="control && control.invalid && (control.dirty || control.touched)">
      <mat-icon class="error-icon-small">error</mat-icon>
      <span class="error-text-small">{{ getControlErrorMessage() }}</span>
    </div>
    
    <!-- General error message -->
    <mat-card class="error-card" *ngIf="message && !control">
      <mat-card-content>
        <div class="error-content">
          <mat-icon class="error-icon">error_outline</mat-icon>
          <div class="error-text">
            <h3>{{ title || 'Error' }}</h3>
            <p>{{ message }}</p>
          </div>
        </div>
      </mat-card-content>
      <mat-card-actions *ngIf="showRetry">
        <button mat-button color="primary" (click)="onRetry()">
          <mat-icon>refresh</mat-icon>
          Reintentar
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .error-card {
      margin: 16px;
      background-color: #ffebee;
    }

    .error-content {
      display: flex;
      align-items: flex-start;
      gap: 16px;
    }

    .error-icon {
      color: #c62828;
      font-size: 48px;
      width: 48px;
      height: 48px;
    }

    .error-text h3 {
      margin: 0 0 8px 0;
      color: #c62828;
    }

    .error-text p {
      margin: 0;
      color: rgba(0, 0, 0, 0.87);
    }

    mat-card-actions {
      padding: 8px 16px;
    }

    .form-error {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-top: 4px;
      color: #c62828;
      font-size: 12px;
    }

    .error-icon-small {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .error-text-small {
      font-size: 12px;
    }
  `]
})
export class ErrorMessageComponent {
  @Input() message: string | null = null;
  @Input() control: AbstractControl | null = null;
  @Input() title?: string;
  @Input() showRetry = false;
  @Output() retry = new EventEmitter<void>();

  // Backward compatibility
  @Input() set error(value: string | null) {
    this.message = value;
  }

  onRetry(): void {
    this.retry.emit();
  }

  getControlErrorMessage(): string {
    if (!this.control || !this.control.errors) {
      return '';
    }

    const errors = this.control.errors;

    // Required field validation
    if (errors['required']) {
      return 'Este campo es requerido';
    }

    // Email validation
    if (errors['email']) {
      return 'Ingrese un correo electrónico válido';
    }

    // Length validations
    if (errors['minlength']) {
      return `Mínimo ${errors['minlength'].requiredLength} caracteres`;
    }
    if (errors['maxlength']) {
      return `Máximo ${errors['maxlength'].requiredLength} caracteres`;
    }

    // Pattern validation
    if (errors['pattern']) {
      return 'Formato inválido';
    }

    // Numeric validations
    if (errors['min']) {
      return `Valor mínimo: ${errors['min'].min}`;
    }
    if (errors['max']) {
      return `Valor máximo: ${errors['max'].max}`;
    }

    // User-specific validations
    if (errors['usernameExists']) {
      return 'Este nombre de usuario ya está en uso';
    }
    if (errors['emailExists']) {
      return 'Este correo electrónico ya está en uso';
    }
    if (errors['usernameValidationError']) {
      return 'Error al validar el nombre de usuario. Inténtelo de nuevo.';
    }
    if (errors['emailValidationError']) {
      return 'Error al validar el correo electrónico. Inténtelo de nuevo.';
    }

    // Password validations
    if (errors['passwordComplexity']) {
      return 'La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial';
    }

    // Server validation errors
    if (errors['serverValidation']) {
      return errors['serverValidation'];
    }

    // Generic server errors
    if (errors['serverError']) {
      return errors['serverError'];
    }

    // Return first error key if no specific message
    const firstErrorKey = Object.keys(errors)[0];
    const firstError = errors[firstErrorKey];

    // If the error has a message property, use it
    if (firstError && typeof firstError === 'object' && firstError.message) {
      return firstError.message;
    }

    return `Error de validación: ${firstErrorKey}`;
  }
}
