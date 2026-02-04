import { Component, DestroyRef, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NotaRemisionItem } from '../../models/nota.model';
import { Producto } from '../../models/producto.model';
import { AutocompleteOption, AutocompleteSelectComponent } from '../../shared/components/autocomplete-select/autocomplete-select.component';

interface NotaRemisionItemDialogData {
  item?: NotaRemisionItem;
  productos: Producto[];
}

@Component({
  selector: 'app-nota-remision-item-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    AutocompleteSelectComponent
  ],
  template: `
    <h2 mat-dialog-title>{{ data.item ? 'EDITAR ITEM' : 'AGREGAR ITEM' }}</h2>

    <form [formGroup]="form" (ngSubmit)="guardarItem()">
      <mat-dialog-content>
        <app-autocomplete-select
          [label]="'PRODUCTO'"
          [placeholder]="'SELECCIONAR PRODUCTO'"
          [options]="productoOptions"
          [value]="productoControl.value !== null && productoControl.value !== undefined ? ('' + productoControl.value) : null"
          [hasError]="productoControl.invalid && productoControl.touched"
          [errorMessage]="'EL PRODUCTO ES OBLIGATORIO'"
          [required]="true"
          [showAllOnEmpty]="false"
          (optionSelected)="onProductoOptionSelected($event)"
          (valueChange)="onProductoValueChange($event)">
        </app-autocomplete-select>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>DESCRIPCIÓN</mat-label>
          <input matInput formControlName="descripcion" maxlength="500">
          <mat-error *ngIf="form.get('descripcion')?.hasError('required')">
            LA DESCRIPCIÓN ES OBLIGATORIA
          </mat-error>
        </mat-form-field>

        <div class="dialog-row">
          <mat-form-field appearance="outline">
            <mat-label>CANTIDAD</mat-label>
            <input matInput type="number" formControlName="cantidad" min="0.001" step="0.001">
            <mat-error *ngIf="form.get('cantidad')?.hasError('required')">
              LA CANTIDAD ES OBLIGATORIA
            </mat-error>
            <mat-error *ngIf="form.get('cantidad')?.hasError('min')">
              LA CANTIDAD DEBE SER MAYOR A CERO
            </mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>UNIDAD DE MEDIDA</mat-label>
            <input matInput formControlName="unidadMedida" placeholder="Ej: UNI, KG, L">
            <mat-hint>Ej: UNI, KG, L, M, etc.</mat-hint>
          </mat-form-field>
        </div>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" (click)="dialogRef.close()">CANCELAR</button>
        <button mat-raised-button color="primary" type="submit">
          {{ data.item ? 'GUARDAR CAMBIOS' : 'GUARDAR ITEM' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .full-width {
      width: 100%;
    }

    .dialog-row {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
    }

    .dialog-row mat-form-field {
      flex: 1;
      min-width: 200px;
    }
  `]
})
export class NotaRemisionItemDialogComponent implements OnInit {
  form: FormGroup;
  productoOptions: AutocompleteOption[] = [];
  productoControl: FormControl;
  private destroyRef = inject(DestroyRef);

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<NotaRemisionItemDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: NotaRemisionItemDialogData
  ) {
    this.form = this.fb.group({
      id: [data.item?.id],
      productoId: [data.item?.productoId ?? null, Validators.required],
      descripcion: [data.item?.descripcion ?? '', [Validators.required, Validators.maxLength(500)]],
      cantidad: [data.item?.cantidad ?? 1, [Validators.required, Validators.min(0.001)]],
      unidadMedida: [data.item?.unidadMedida ?? 'UNI']
    });

    this.productoControl = this.form.get('productoId') as FormControl;
    this.productoOptions = data.productos.map((producto) => ({
      value: producto.id != null ? String(producto.id) : '',
      label: producto.descripcion,
      codigo: producto.codigo
    }));
  }

  ngOnInit(): void {
    // No hay totales que calcular para notas de remisión
  }

  onProductoOptionSelected(option: AutocompleteOption): void {
    const productoId = option.value ? Number(option.value) : null;
    this.productoControl.setValue(productoId);
    this.productoControl.markAsTouched();

    const descripcionActual = this.form.get('descripcion')?.value ?? '';
    const producto = this.data.productos.find(p => String(p.id) === option.value);

    if (producto && (!descripcionActual || descripcionActual.trim().length === 0)) {
      this.form.patchValue({ 
        descripcion: producto.descripcion,
        unidadMedida: producto.unidadMedida || 'UNI'
      }, { emitEvent: false });
    }
  }

  onProductoValueChange(value: string): void {
    if (!value) {
      this.productoControl.setValue(null);
    }
  }

  guardarItem(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const formValue = this.form.getRawValue();
    const item: NotaRemisionItem = {
      id: formValue.id,
      productoId: formValue.productoId,
      descripcion: formValue.descripcion,
      cantidad: formValue.cantidad,
      unidadMedida: formValue.unidadMedida || 'UNI'
    };

    this.dialogRef.close(item);
  }
}
