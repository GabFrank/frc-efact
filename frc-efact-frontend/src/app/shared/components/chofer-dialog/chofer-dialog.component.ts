import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ChoferApiService } from '../../../core/api/chofer-api.service';
import { Chofer } from '../../../models/chofer.model';
import { ErrorMessageComponent } from '../error-message/error-message.component';

@Component({
  selector: 'app-chofer-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSnackBarModule,
    ErrorMessageComponent
  ],
  template: `
    <div class="chofer-dialog">
      <h2 mat-dialog-title>
        <mat-icon>person</mat-icon>
        Nuevo Chofer
      </h2>
      
      <mat-dialog-content>
        <form [formGroup]="choferForm">
          <div class="form-row">
            <mat-form-field appearance="outline">
              <mat-label>Nombre</mat-label>
              <input matInput formControlName="nombre" placeholder="Nombre del chofer" style="text-transform: uppercase" (input)="onTextInput($event, 'nombre')">
              <app-error-message [control]="choferForm.get('nombre')" />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Documento</mat-label>
              <input matInput formControlName="documento" placeholder="Número de documento" style="text-transform: uppercase" (input)="onTextInput($event, 'documento')">
              <app-error-message [control]="choferForm.get('documento')" />
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Dirección</mat-label>
              <textarea matInput formControlName="direccion" placeholder="Dirección del chofer" rows="2"></textarea>
              <app-error-message [control]="choferForm.get('direccion')" />
            </mat-form-field>
          </div>
        </form>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="onSave()" [disabled]="choferForm.invalid || saving">
          {{ saving ? 'Guardando...' : 'Guardar' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .chofer-dialog {
      min-width: 500px;
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
    }

    .form-row {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      margin-bottom: 16px;
    }

    mat-form-field {
      flex: 1;
      min-width: 200px;
    }

    .full-width {
      flex: 1 1 100%;
    }

    mat-dialog-actions {
      padding: 16px 24px;
      border-top: 1px solid rgba(0, 0, 0, 0.12);
      margin: 0;
    }
  `]
})
export class ChoferDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private choferApi = inject(ChoferApiService);
  private snackBar = inject(MatSnackBar);
  public dialogRef = inject(MatDialogRef<ChoferDialogComponent>);

  choferForm!: FormGroup;
  empresaId!: number;
  saving = false;

  ngOnInit(): void {
    this.choferForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      documento: ['', [Validators.maxLength(20)]],
      direccion: ['']
    });
  }

  onTextInput(event: Event, field: string): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.toUpperCase();
    this.choferForm.patchValue({ [field]: value }, { emitEvent: false });
  }

  onSave(): void {
    if (this.choferForm.invalid) {
      this.choferForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const choferData: Partial<Chofer> = {
      nombre: this.choferForm.value.nombre,
      documento: this.choferForm.value.documento || undefined,
      direccion: this.choferForm.value.direccion || undefined,
      activo: true
    };

    this.choferApi.create(this.empresaId, choferData).subscribe({
      next: (chofer) => {
        this.saving = false;
        this.snackBar.open('Chofer creado exitosamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(chofer);
      },
      error: (error) => {
        this.saving = false;
        this.snackBar.open(
          error.error?.message || 'Error al crear el chofer',
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
