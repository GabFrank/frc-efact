import { Component, Inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';

export interface EmailEnviarDialogData {
  titulo: string;
  clienteNombre: string;
  emailActual?: string;
}

@Component({
  selector: 'app-email-enviar-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.titulo }}</h2>
    <mat-dialog-content>
      <p>Enviar documento a: <strong>{{ data.clienteNombre }}</strong></p>
      
      <form [formGroup]="form" class="email-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Correo Electrónico</mat-label>
          <input matInput formControlName="email" type="email" placeholder="ejemplo@correo.com">
          <mat-icon matSuffix>email</mat-icon>
          <mat-error *ngIf="form.get('email')?.hasError('required')">El email es obligatorio</mat-error>
          <mat-error *ngIf="form.get('email')?.hasError('email')">El formato del email es inválido</mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancelar()">Cancelar</button>
      <button mat-raised-button color="primary" [disabled]="form.invalid" (click)="enviar()">
        <mat-icon>send</mat-icon>
        Enviar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .email-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-top: 16px;
    }
    .full-width {
      width: 100%;
    }
    mat-dialog-content {
      min-width: 400px;
    }
  `]
})
export class EmailEnviarDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<EmailEnviarDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EmailEnviarDialogData
  ) {
    this.form = this.fb.group({
      email: [data.emailActual || '', [Validators.required, Validators.email]]
    });
  }

  cancelar(): void {
    this.dialogRef.close();
  }

  enviar(): void {
    if (this.form.valid) {
      this.dialogRef.close(this.form.value);
    }
  }
}
