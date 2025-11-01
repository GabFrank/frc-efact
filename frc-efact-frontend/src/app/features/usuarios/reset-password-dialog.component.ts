import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

export interface ResetPasswordDialogData {
  userId: number;
  username: string;
}

export interface ResetPasswordDialogResult {
  newPassword: string;
  forcePasswordChange: boolean;
}

@Component({
  selector: 'app-reset-password-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    ErrorMessageComponent
  ],
  template: `
    <h2 mat-dialog-title>Restablecer Contraseña</h2>
    <mat-dialog-content>
      <p class="user-info">
        Usuario: <strong>{{ data.username }}</strong>
      </p>
      
      <form [formGroup]="resetForm" class="reset-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nueva Contraseña</mat-label>
          <input 
            matInput 
            [type]="hidePassword ? 'password' : 'text'"
            formControlName="newPassword"
            placeholder="Ingrese la nueva contraseña"
            [disabled]="isSubmitting">
          <button 
            mat-icon-button 
            matSuffix 
            type="button"
            (click)="hidePassword = !hidePassword"
            [attr.aria-label]="'Hide password'"
            [attr.aria-pressed]="hidePassword"
            [disabled]="isSubmitting">
            <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          
          <!-- Enhanced error handling -->
          <app-error-message [control]="resetForm.get('newPassword')"></app-error-message>
        </mat-form-field>

        <div class="password-actions">
          <button 
            mat-stroked-button 
            type="button" 
            (click)="generateSecurePassword()"
            [disabled]="isSubmitting"
            matTooltip="Generar una contraseña segura automáticamente">
            <mat-icon>auto_fix_high</mat-icon>
            Generar Contraseña Segura
          </button>
        </div>

        <div class="password-requirements">
          <h4>Requisitos de contraseña:</h4>
          <ul>
            <li [class.valid]="hasMinLength">Al menos 8 caracteres</li>
            <li [class.valid]="hasUppercase">Al menos una letra mayúscula</li>
            <li [class.valid]="hasLowercase">Al menos una letra minúscula</li>
            <li [class.valid]="hasNumber">Al menos un número</li>
            <li [class.valid]="hasSpecialChar">Al menos un carácter especial (!&#64;#$%^&*)</li>
          </ul>
        </div>

        <mat-checkbox 
          formControlName="forcePasswordChange"
          [disabled]="isSubmitting"
          class="force-change-checkbox">
          Forzar cambio de contraseña en el próximo inicio de sesión
        </mat-checkbox>
      </form>
    </mat-dialog-content>
    
    <mat-dialog-actions align="end">
      <button 
        mat-button 
        (click)="onCancel()"
        [disabled]="isSubmitting">
        Cancelar
      </button>
      <button 
        mat-raised-button 
        color="primary" 
        (click)="onConfirm()"
        [disabled]="!resetForm.valid || isSubmitting">
        <mat-progress-spinner 
          *ngIf="isSubmitting" 
          diameter="20" 
          mode="indeterminate"
          style="display: inline-block; margin-right: 8px;">
        </mat-progress-spinner>
        <mat-icon *ngIf="!isSubmitting">lock_reset</mat-icon>
        {{ isSubmitting ? 'Restableciendo...' : 'Restablecer Contraseña' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .user-info {
      margin-bottom: 20px;
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
      font-size: 14px;
    }

    .reset-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 400px;
    }

    .full-width {
      width: 100%;
    }

    .password-actions {
      display: flex;
      justify-content: center;
      margin: 8px 0;
    }

    .password-requirements {
      margin: 16px 0;
      padding: 12px;
      background-color: #fafafa;
      border-radius: 4px;
      border-left: 4px solid #2196f3;
    }

    .password-requirements h4 {
      margin: 0 0 8px 0;
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }

    .password-requirements ul {
      margin: 0;
      padding-left: 20px;
      font-size: 13px;
    }

    .password-requirements li {
      margin: 4px 0;
      color: #666;
      transition: color 0.2s ease;
    }

    .password-requirements li.valid {
      color: #4caf50;
      font-weight: 500;
    }

    .password-requirements li.valid::before {
      content: '✓ ';
      color: #4caf50;
      font-weight: bold;
    }

    .force-change-checkbox {
      margin: 16px 0 8px 0;
    }

    mat-dialog-content {
      padding: 20px 24px;
      max-height: 70vh;
      overflow-y: auto;
    }

    mat-dialog-actions {
      padding: 16px 24px;
    }
  `]
})
export class ResetPasswordDialogComponent implements OnInit {
  readonly data = inject<ResetPasswordDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ResetPasswordDialogComponent>);
  private readonly fb = inject(FormBuilder);

  resetForm!: FormGroup;
  hidePassword = true;
  isSubmitting = false;

  // Password validation flags
  hasMinLength = false;
  hasUppercase = false;
  hasLowercase = false;
  hasNumber = false;
  hasSpecialChar = false;

  ngOnInit(): void {
    this.createForm();
    this.setupPasswordValidation();
  }

  private createForm(): void {
    this.resetForm = this.fb.group({
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/)
      ]],
      forcePasswordChange: [true]
    });
  }

  private setupPasswordValidation(): void {
    this.resetForm.get('newPassword')?.valueChanges.subscribe(password => {
      if (password) {
        this.hasMinLength = password.length >= 8;
        this.hasUppercase = /[A-Z]/.test(password);
        this.hasLowercase = /[a-z]/.test(password);
        this.hasNumber = /\d/.test(password);
        this.hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);
      } else {
        this.hasMinLength = false;
        this.hasUppercase = false;
        this.hasLowercase = false;
        this.hasNumber = false;
        this.hasSpecialChar = false;
      }
    });
  }

  generateSecurePassword(): void {
    const length = 12;
    const lowercase = 'abcdefghijklmnopqrstuvwxyz';
    const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const numbers = '0123456789';
    const specialChars = '!@#$%^&*()_+-=[]{}|;:,.<>?';
    
    let password = '';
    
    // Ensure at least one character from each required category
    password += lowercase[Math.floor(Math.random() * lowercase.length)];
    password += uppercase[Math.floor(Math.random() * uppercase.length)];
    password += numbers[Math.floor(Math.random() * numbers.length)];
    password += specialChars[Math.floor(Math.random() * specialChars.length)];
    
    // Fill the rest with random characters from all categories
    const allChars = lowercase + uppercase + numbers + specialChars;
    for (let i = password.length; i < length; i++) {
      password += allChars[Math.floor(Math.random() * allChars.length)];
    }
    
    // Shuffle the password to avoid predictable patterns
    password = password.split('').sort(() => Math.random() - 0.5).join('');
    
    this.resetForm.patchValue({ newPassword: password });
    this.hidePassword = false; // Show the generated password
  }

  onConfirm(): void {
    if (this.resetForm.valid) {
      const result: ResetPasswordDialogResult = {
        newPassword: this.resetForm.value.newPassword,
        forcePasswordChange: this.resetForm.value.forcePasswordChange
      };
      this.dialogRef.close(result);
    }
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }
}