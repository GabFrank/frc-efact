import { Component, DestroyRef, Inject, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FacturaLegalItem } from '../../models/factura.model';
import { Producto } from '../../models/producto.model';
import { AutocompleteOption, AutocompleteSelectComponent } from '../../shared/components/autocomplete-select/autocomplete-select.component';

interface FacturaItemDialogData {
  item?: FacturaLegalItem | any; // Permite NotaItem también
  productos: Producto[];
  monedaExtranjera?: string;
  tipoCambio?: number; // Siempre un número (1 si no hay moneda extranjera)
  simboloMoneda?: string;
  showIva?: boolean; // Si true, muestra campo de IVA
  showDescuento?: boolean; // Si true, muestra campo de descuento
}

@Component({
  selector: 'app-factura-item-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
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
            <mat-label>PRECIO UNITARIO</mat-label>
            <span matPrefix>{{ simboloMoneda }}&nbsp;</span>
            <input matInput type="number" formControlName="precioUnitario" min="0" [step]="monedaExtranjera === 'PYG' ? 100 : 0.01">
            <mat-error *ngIf="form.get('precioUnitario')?.hasError('required')">
              EL PRECIO UNITARIO ES OBLIGATORIO
            </mat-error>
            <mat-error *ngIf="form.get('precioUnitario')?.hasError('min')">
              EL PRECIO UNITARIO NO PUEDE SER NEGATIVO
            </mat-error>
          </mat-form-field>
        </div>

        <div class="dialog-row" *ngIf="data.showDescuento">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>DESCUENTO</mat-label>
            <span matPrefix>{{ simboloMoneda }}&nbsp;</span>
            <input matInput type="number" formControlName="descuento" min="0" [step]="monedaExtranjera === 'PYG' ? 100 : 0.01">
            <mat-error *ngIf="form.get('descuento')?.hasError('min')">
              EL DESCUENTO NO PUEDE SER NEGATIVO
            </mat-error>
          </mat-form-field>
        </div>

        <div class="dialog-row" *ngIf="data.showIva">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>IVA</mat-label>
            <mat-select formControlName="iva">
              <mat-option [value]="0">0%</mat-option>
              <mat-option [value]="5">5%</mat-option>
              <mat-option [value]="10">10%</mat-option>
            </mat-select>
            <mat-error *ngIf="form.get('iva')?.hasError('required')">
              EL IVA ES OBLIGATORIO
            </mat-error>
          </mat-form-field>
        </div>

        <div class="total-preview">
          <span>TOTAL:</span>
          <strong>{{ simboloMoneda }} {{ total() | number:'1.0-3':'es-PY' }}</strong>
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

    .total-preview {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 12px;
      font-size: 18px;
      margin-top: 16px;
      font-weight: 500;
    }
  `]
})
export class FacturaItemDialogComponent implements OnInit {
  form: FormGroup;
  total = signal(0);
  productoOptions: AutocompleteOption[] = [];
  productoControl: FormControl;
  private destroyRef = inject(DestroyRef);
  
  // Información de moneda
  monedaExtranjera: string = 'PYG';
  tipoCambio: number = 1; // Siempre un número (1 si no hay moneda extranjera)
  simboloMoneda: string = '₲';

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<FacturaItemDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: FacturaItemDialogData
  ) {
    // Guardar información de moneda
    this.monedaExtranjera = data.monedaExtranjera || 'PYG';
    // tipoCambio siempre es un número (1 si no hay moneda extranjera o si es null/undefined)
    // Si viene como número y es mayor a 0, usarlo; de lo contrario, usar 1
    const tipoCambioRecibido = data.tipoCambio;
    this.tipoCambio = (typeof tipoCambioRecibido === 'number' && tipoCambioRecibido > 0) ? tipoCambioRecibido : 1;
    this.simboloMoneda = data.simboloMoneda || '₲';
    
    console.log('🔍 Diálogo - Datos recibidos:', {
      monedaExtranjera: this.monedaExtranjera,
      tipoCambioRecibido: tipoCambioRecibido,
      tipoCambioFinal: this.tipoCambio,
      itemPrecioUnitario: data.item?.precioUnitario,
      itemTotal: data.item?.total
    });
    
    // IMPORTANTE: El item viene en guaraníes, pero para mostrar al usuario
    // lo convertimos dividiendo por el tipo de cambio
    const precioUnitarioGs = data.item?.precioUnitario || 0;
    const totalGs = data.item?.total || 0;
    const precioUnitarioParaMostrar = precioUnitarioGs / this.tipoCambio;
    const totalParaMostrar = totalGs / this.tipoCambio;
    
    console.log('🔍 Diálogo - Conversión para mostrar:', {
      precioUnitarioGs,
      totalGs,
      tipoCambio: this.tipoCambio,
      precioUnitarioParaMostrar,
      totalParaMostrar
    });
    
    const formGroupConfig: any = {
      id: [data.item?.id],
      productoId: [data.item?.productoId ?? null, Validators.required],
      descripcion: [data.item?.descripcion ?? '', [Validators.required, Validators.maxLength(500)]],
      cantidad: [data.item?.cantidad ?? 1, [Validators.required, Validators.min(0.001)]],
      // El formulario guarda valores en la moneda que el usuario ve (extranjera o PYG)
      // Al guardar, convertiremos a guaraníes multiplicando por tipoCambio
      precioUnitario: [precioUnitarioParaMostrar, [Validators.required, Validators.min(0)]]
    };

    // Agregar campos opcionales si se requieren
    if (data.showDescuento) {
      const descuentoGs = data.item?.descuento || 0;
      const descuentoParaMostrar = descuentoGs / this.tipoCambio;
      formGroupConfig.descuento = [descuentoParaMostrar, [Validators.min(0)]];
    }

    if (data.showIva) {
      formGroupConfig.iva = [data.item?.iva ?? 10, Validators.required];
    }

    this.form = this.fb.group(formGroupConfig);
    
    // Verificar que el valor se estableció correctamente
    console.log('🔍 Diálogo - Formulario inicializado:', {
      precioUnitarioForm: this.form.get('precioUnitario')?.value,
      esperado: precioUnitarioParaMostrar,
      coinciden: this.form.get('precioUnitario')?.value === precioUnitarioParaMostrar
    });
    
    this.productoControl = this.form.get('productoId') as FormControl;
    this.productoOptions = data.productos.map((producto) => ({
      value: producto.id != null ? String(producto.id) : '',
      label: producto.descripcion,
      codigo: producto.codigo
    }));
  }

  ngOnInit(): void {
    this.actualizarTotal();

    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.actualizarTotal();
    });
  }

  onProductoOptionSelected(option: AutocompleteOption): void {
    const productoId = option.value ? Number(option.value) : null;
    this.productoControl.setValue(productoId);
    this.productoControl.markAsTouched();

    const descripcionActual = this.form.get('descripcion')?.value ?? '';
    const producto = this.data.productos.find(p => String(p.id) === option.value);
    const precioControl = this.form.get('precioUnitario') as FormControl;

    if (producto && (!descripcionActual || descripcionActual.trim().length === 0)) {
      this.form.patchValue({ descripcion: producto.descripcion }, { emitEvent: false });
    }

    if (producto && (!precioControl.dirty || !precioControl.value)) {
      // El precio del producto está en guaraníes, convertir para mostrar
      // (dividir por tipoCambio, que es 1 si no hay moneda extranjera)
      const precioParaMostrar = (producto.precio ?? 0) / this.tipoCambio;
      precioControl.setValue(precioParaMostrar);
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
    const { id, productoId, descripcion, cantidad, precioUnitario, descuento, iva } = formValue;
    const total = this.total();

    // IMPORTANTE: El formulario tiene valores en la moneda que el usuario ve (extranjera o PYG)
    // Convertir a guaraníes antes de devolver (multiplicar por tipoCambio, que es 1 si no hay moneda extranjera)
    const precioUnitarioGs = precioUnitario * this.tipoCambio;
    const descuentoGs = (descuento || 0) * this.tipoCambio;
    const totalGs = total * this.tipoCambio;

    const item: any = {
      id,
      productoId,
      descripcion,
      cantidad,
      precioUnitario: precioUnitarioGs, // Siempre en guaraníes
      total: totalGs // Siempre en guaraníes
    };

    // Agregar campos opcionales si existen
    if (descuento !== undefined && descuento !== null) {
      item.descuento = descuentoGs;
    }
    if (iva !== undefined && iva !== null) {
      item.iva = iva;
    }

    this.dialogRef.close(item);
  }

  private actualizarTotal(): void {
    const cantidad = Number(this.form.get('cantidad')?.value) || 0;
    const precioUnitario = Number(this.form.get('precioUnitario')?.value) || 0;
    const descuento = Number(this.form.get('descuento')?.value) || 0;
    const subtotal = cantidad * precioUnitario;
    this.total.set(Math.max(0, subtotal - descuento));
  }
}

