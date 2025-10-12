import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProductoApiService } from '../../core/api/producto-api.service';
import { Producto } from '../../models/producto.model';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-producto-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatSnackBarModule,
    ErrorMessageComponent
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Editar Producto' : 'Nuevo Producto' }}</h2>
    
    <mat-dialog-content>
      <form [formGroup]="form" class="producto-form">
        <mat-form-field appearance="outline">
          <mat-label>Código</mat-label>
          <input matInput formControlName="codigo" placeholder="Código del producto">
          <app-error-message [control]="form.get('codigo')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Descripción</mat-label>
          <input matInput formControlName="descripcion" placeholder="Descripción del producto">
          <app-error-message [control]="form.get('descripcion')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Precio</mat-label>
          <input matInput 
                 type="number" 
                 formControlName="precio" 
                 placeholder="0.00"
                 min="0"
                 step="0.01">
          <span matPrefix>₲&nbsp;</span>
          <app-error-message [control]="form.get('precio')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>IVA</mat-label>
          <mat-select formControlName="iva">
            <mat-option [value]="0">0% - Exento</mat-option>
            <mat-option [value]="5">5%</mat-option>
            <mat-option [value]="10">10%</mat-option>
          </mat-select>
          <app-error-message [control]="form.get('iva')" />
        </mat-form-field>

        <div class="checkbox-field">
          <mat-checkbox formControlName="balanza">
            Requiere balanza
          </mat-checkbox>
        </div>

        <div class="checkbox-field" *ngIf="isEdit">
          <mat-checkbox formControlName="activo">
            Activo
          </mat-checkbox>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button mat-raised-button 
              color="primary" 
              (click)="onSubmit()"
              [disabled]="form.invalid || saving">
        {{ saving ? 'Guardando...' : 'Guardar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .producto-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 500px;
      padding: 20px 0;
    }

    mat-form-field {
      width: 100%;
    }

    .checkbox-field {
      margin: 8px 0;
    }

    mat-dialog-content {
      max-height: 70vh;
      overflow-y: auto;
    }
  `]
})
export class ProductoFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private productoApi: ProductoApiService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ProductoFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { producto: Producto | null }
  ) {}

  ngOnInit(): void {
    this.isEdit = !!this.data.producto;
    this.initForm();
  }

  initForm(): void {
    this.form = this.fb.group({
      codigo: [this.data.producto?.codigo || '', [Validators.maxLength(50)]],
      descripcion: [
        this.data.producto?.descripcion || '', 
        [Validators.required, Validators.maxLength(500)]
      ],
      precio: [
        this.data.producto?.precio || 0, 
        [Validators.required, Validators.min(0)]
      ],
      iva: [
        this.data.producto?.iva ?? 10, 
        [Validators.required, this.ivaValidator]
      ],
      balanza: [this.data.producto?.balanza || false],
      activo: [this.data.producto?.activo ?? true]
    });
  }

  ivaValidator(control: any): { [key: string]: boolean } | null {
    const validValues = [0, 5, 10];
    if (control.value !== null && !validValues.includes(Number(control.value))) {
      return { invalidIva: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const productoData: Partial<Producto> = {
      ...this.form.value,
      empresaId: 1 // TODO: Obtener del state
    };

    const request = this.isEdit
      ? this.productoApi.update(this.data.producto!.id, productoData)
      : this.productoApi.create(productoData);

    request.subscribe({
      next: (producto) => {
        this.snackBar.open(
          `Producto ${this.isEdit ? 'actualizado' : 'creado'} correctamente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.dialogRef.close(producto);
      },
      error: (error) => {
        this.snackBar.open(
          error.error?.message || `Error al ${this.isEdit ? 'actualizar' : 'crear'} producto`,
          'Cerrar',
          { duration: 3000 }
        );
        this.saving = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
