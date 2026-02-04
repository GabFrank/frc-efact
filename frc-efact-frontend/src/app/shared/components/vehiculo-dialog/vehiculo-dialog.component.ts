import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { VehiculoApiService } from '../../../core/api/vehiculo-api.service';
import { Vehiculo } from '../../../models/vehiculo.model';
import { ErrorMessageComponent } from '../error-message/error-message.component';

@Component({
  selector: 'app-vehiculo-dialog',
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
    <div class="vehiculo-dialog">
      <h2 mat-dialog-title>
        <mat-icon>directions_car</mat-icon>
        Nuevo Vehículo
      </h2>
      
      <mat-dialog-content>
        <form [formGroup]="vehiculoForm">
          <div class="form-row">
            <mat-form-field appearance="outline">
              <mat-label>Marca</mat-label>
              <input matInput formControlName="marca" placeholder="Marca del vehículo" style="text-transform: uppercase" (input)="onTextInput($event, 'marca')">
              <app-error-message [control]="vehiculoForm.get('marca')" />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Matrícula</mat-label>
              <input matInput formControlName="matricula" placeholder="Matrícula del vehículo" style="text-transform: uppercase" (input)="onTextInput($event, 'matricula')">
              <app-error-message [control]="vehiculoForm.get('matricula')" />
            </mat-form-field>
          </div>
        </form>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="onSave()" [disabled]="vehiculoForm.invalid || saving">
          {{ saving ? 'Guardando...' : 'Guardar' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .vehiculo-dialog {
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
    }

    mat-form-field {
      flex: 1;
      min-width: 200px;
    }

    mat-dialog-actions {
      padding: 16px 24px;
      border-top: 1px solid rgba(0, 0, 0, 0.12);
      margin: 0;
    }
  `]
})
export class VehiculoDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private vehiculoApi = inject(VehiculoApiService);
  private snackBar = inject(MatSnackBar);
  public dialogRef = inject(MatDialogRef<VehiculoDialogComponent>);

  vehiculoForm!: FormGroup;
  empresaId!: number;
  saving = false;

  ngOnInit(): void {
    this.vehiculoForm = this.fb.group({
      marca: ['', [Validators.required, Validators.maxLength(100)]],
      matricula: ['', [Validators.required, Validators.maxLength(20)]]
    });
  }

  onTextInput(event: Event, field: string): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.toUpperCase();
    this.vehiculoForm.patchValue({ [field]: value }, { emitEvent: false });
  }

  onSave(): void {
    if (this.vehiculoForm.invalid) {
      this.vehiculoForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const vehiculoData: Partial<Vehiculo> = {
      marca: this.vehiculoForm.value.marca,
      matricula: this.vehiculoForm.value.matricula,
      activo: true
    };

    this.vehiculoApi.create(this.empresaId, vehiculoData).subscribe({
      next: (vehiculo) => {
        this.saving = false;
        this.snackBar.open('Vehículo creado exitosamente', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(vehiculo);
      },
      error: (error) => {
        this.saving = false;
        this.snackBar.open(
          error.error?.message || 'Error al crear el vehículo',
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
