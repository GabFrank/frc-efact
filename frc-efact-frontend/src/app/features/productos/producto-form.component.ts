import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, AsyncValidatorFn, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Observable, of } from 'rxjs';
import { debounceTime, map, switchMap, take } from 'rxjs/operators';
import { ProductoApiService } from '../../core/api/producto-api.service';
import {
  Producto,
  TipoTransaccionProducto,
  TIPO_TRANSACCION_DESCRIPCIONES
} from '../../models/producto.model';
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
          <input matInput formControlName="codigo" placeholder="Código del producto" (input)="onStringInput($event, 'codigo')">
          <mat-error *ngIf="form.get('codigo')?.hasError('codigoDuplicado')">
            Ya existe un producto con este código en la empresa
          </mat-error>
          <app-error-message [control]="form.get('codigo')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Descripción</mat-label>
          <input matInput formControlName="descripcion" placeholder="Descripción del producto" (input)="onStringInput($event, 'descripcion')">
          <mat-error *ngIf="form.get('descripcion')?.hasError('descripcionDuplicada')">
            Ya existe un producto con esta descripción en la empresa
          </mat-error>
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

        <mat-form-field appearance="outline">
          <mat-label>Tipo de Transacción</mat-label>
          <mat-select formControlName="tipoTransaccion">
            <mat-option
              *ngFor="let tipo of tipoTransaccionOpciones"
              [value]="tipo">
              {{ tipoTransaccionDescripciones[tipo] }}
            </mat-option>
          </mat-select>
          <mat-hint>Según Manual Técnico SIFEN v1.50</mat-hint>
          <app-error-message [control]="form.get('tipoTransaccion')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Unidad de Medida</mat-label>
          <mat-select formControlName="unidadMedida">
            <mat-option
              *ngFor="let unidad of unidadesMedidaComunes"
              [value]="unidad.value">
              {{ unidad.label }}
            </mat-option>
          </mat-select>
          <mat-hint>Códigos oficiales de SIFEN. Se emiten tal cual en el documento electrónico.</mat-hint>
          <app-error-message [control]="form.get('unidadMedida')" />
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

    @media (max-width: 768px) {
      .producto-form {
        min-width: 0;
        width: 100%;
      }
    }
  `]
})
export class ProductoFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  saving = false;

  // Enums y opciones para el template
  TipoTransaccionProducto = TipoTransaccionProducto;
  tipoTransaccionOpciones = Object.values(TipoTransaccionProducto);
  tipoTransaccionDescripciones = TIPO_TRANSACCION_DESCRIPCIONES;

  // Unidades de medida comunes según SIFEN
  /**
   * Catálogo de unidades de medida de SIFEN (`cUniMed`), espejo de `TcUniMed` en jsifenlib.
   *
   * **Esta lista antes era inventada.** Ofrecía 14 opciones de las cuales solo cuatro existían en
   * el catálogo de la SET (`UNI`, `ML`, `M2`, `M3`); las otras diez —`KG`, `G`, `L`, `M`, `H`,
   * `SERV`, `PAR`, `CAJ`, `BOL`, `TUB`— no están en SIFEN y se terminaban emitiendo como `UNI`.
   * Y `TN` (Tonelada), que es la unidad de venta de commodities agrícolas, ni figuraba: por eso
   * una factura de 575,195 toneladas de maíz se emitió como unidades y con el monto truncado.
   *
   * ⚠️ **El `value` debe coincidir EXACTAMENTE con el nombre de la constante en `TcUniMed`,
   * respetando mayúsculas y minúsculas.** `ML` (88) es Mililitros y `ml` (660) es Metro lineal:
   * son unidades distintas. No aplicar `.toUpperCase()` a estos valores en ningún lado.
   *
   * Ordenadas por uso esperado: primero las habituales del rubro, después el resto.
   */
  unidadesMedidaComunes = [
    { value: 'UNI', label: 'UNI - Unidad' },
    { value: 'kg', label: 'kg - Kilogramos' },
    { value: 'TN', label: 'TN - Tonelada' },
    { value: 'LT', label: 'LT - Litros' },
    { value: 'g', label: 'g - Gramos' },
    { value: 'ha', label: 'ha - Hectáreas' },
    { value: 'Hs', label: 'Hs - Hora' },
    { value: 'Di', label: 'Di - Día' },
    { value: 'ME', label: 'ME - Mes' },
    { value: 'AA', label: 'AA - Año' },
    { value: 'm', label: 'm - Metros' },
    { value: 'M2', label: 'M2 - Metros cuadrados' },
    { value: 'M3', label: 'M3 - Metros cúbicos' },
    { value: 'ml', label: 'ml - Metro lineal' },
    { value: 'Km', label: 'Km - Kilómetros' },
    { value: 'CM', label: 'CM - Centímetros' },
    { value: 'CM2', label: 'CM2 - Centímetros cuadrados' },
    { value: 'CM3', label: 'CM3 - Centímetros cúbicos' },
    { value: 'MM', label: 'MM - Milímetros' },
    { value: 'MM2', label: 'MM2 - Milímetros cuadrados' },
    { value: 'ML', label: 'ML - Mililitros' },
    { value: 'MG', label: 'MG - Miligramos' },
    { value: 'kg_m2', label: 'kg/m2 - Kilogramos sobre metro cuadrado' },
    { value: 'PUL', label: 'PUL - Pulgadas' },
    { value: 'Ya', label: 'Ya - Yardas' },
    { value: 'MT', label: 'MT - Metros' },
    { value: 'Mi', label: 'Mi - Minuto' },
    { value: 'Se', label: 'Se - Segundo' },
    { value: 'UI', label: 'UI - Unidad Internacional' },
    { value: 'GL', label: 'GL - Unidad de Medida Global' },
    { value: 'DET', label: 'DET - Determinación' },
    { value: 'racion', label: 'ración - Ración' },
    { value: 'CPM', label: 'CPM - Costo Por Mil' },
    { value: 'pm', label: 'pm - Por Milaje' }
  ];

  constructor(
    private fb: FormBuilder,
    private productoApi: ProductoApiService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ProductoFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { producto: Producto | null; empresaId?: number }
  ) {}

  ngOnInit(): void {
    this.isEdit = !!this.data.producto;
    this.initForm();
  }

  initForm(): void {
    // Convertir valores existentes a mayúsculas si están presentes
    const codigoInicial = this.data.producto?.codigo ? this.data.producto.codigo.toUpperCase() : '';
    const descripcionInicial = this.data.producto?.descripcion ? this.data.producto.descripcion.toUpperCase() : '';
    // Sin .toUpperCase(): los códigos de SIFEN distinguen mayúsculas de minúsculas. Ver el
    // comentario del valueChanges más abajo.
    const unidadMedidaInicial = this.data.producto?.unidadMedida?.trim() || 'UNI';

    const productoId = this.data.producto?.id;

    this.form = this.fb.group({
      codigo: [
        codigoInicial,
        [Validators.maxLength(50)],
        [this.codigoValidator()]
      ],
      descripcion: [
        descripcionInicial,
        [Validators.required, Validators.maxLength(500)],
        [this.descripcionValidator()]
      ],
      precio: [
        this.data.producto?.precio || 0,
        [Validators.required, Validators.min(0.01)]
      ],
      iva: [
        this.data.producto?.iva ?? 10,
        [Validators.required, this.ivaValidator]
      ],
      tipoTransaccion: [
        this.data.producto?.tipoTransaccion || TipoTransaccionProducto.VENTA_MERCADERIA,
        [Validators.required]
      ],
      unidadMedida: [
        unidadMedidaInicial,
        [Validators.required, Validators.maxLength(10)]
      ],
      balanza: [this.data.producto?.balanza || false],
      activo: [this.data.producto?.activo ?? true]
    });

    // NO forzar mayúsculas en unidadMedida.
    //
    // Los códigos del catálogo de SIFEN distinguen mayúsculas de minúsculas, y el `.toUpperCase()`
    // que había acá volvía INALCANZABLES a más de la mitad: kg, g, m, ml, ha, racion, pm, Hs, Km,
    // Mi, Ya, Se y Di. Aunque el selector ofreciera "kg", se guardaba "KG" y el backend fallaba al
    // mapearlo, emitiendo UNI.
    //
    // Peor todavía: ML (88) es Mililitros y ml (660) es Metro lineal. Pasar todo a mayúsculas
    // convertía metros lineales en mililitros, en silencio y sin ningún error.
    //
    // El valor sale del selector, que ya ofrece solo códigos válidos, así que no hace falta
    // normalizar nada.

    // Validación dinámica: si es promoción o donación, el precio puede ser 0
    this.form.get('tipoTransaccion')?.valueChanges.subscribe(tipo => {
      const precioControl = this.form.get('precio');
      if (tipo === TipoTransaccionProducto.PROMOCION_MUESTRAS ||
          tipo === TipoTransaccionProducto.DONACION) {
        precioControl?.setValidators([Validators.required, Validators.min(0)]);
      } else {
        precioControl?.setValidators([Validators.required, Validators.min(0.01)]);
      }
      precioControl?.updateValueAndValidity();
    });
  }

  ivaValidator(control: any): { [key: string]: boolean } | null {
    const validValues = [0, 5, 10];
    if (control.value !== null && !validValues.includes(Number(control.value))) {
      return { invalidIva: true };
    }
    return null;
  }

  onStringInput(event: Event, controlName: string): void {
    const input = event.target as HTMLInputElement;
    const upperValue = input.value.toUpperCase();
    if (input.value !== upperValue) {
      this.form.get(controlName)?.setValue(upperValue, { emitEvent: false });
      // Actualizar el valor del input directamente para que se vea en mayúsculas
      input.value = upperValue;
    }

    // Si es código o descripción, agregar validador asíncrono después de escribir
    if ((controlName === 'codigo' || controlName === 'descripcion') && upperValue.trim()) {
      const control = this.form.get(controlName);
      if (control) {
        // Remover validadores asíncronos anteriores y agregar nuevos
        if (controlName === 'codigo' && upperValue.trim()) {
          control.clearAsyncValidators();
          control.setAsyncValidators([this.codigoValidator()]);
        } else if (controlName === 'descripcion' && upperValue.trim()) {
          control.clearAsyncValidators();
          control.setAsyncValidators([this.descripcionValidator()]);
        }
        control.updateValueAndValidity({ emitEvent: true });
      }
    }
  }

  codigoValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value || !control.value.trim() || !this.data.empresaId) {
        return of(null);
      }

      const codigo = control.value.toUpperCase().trim();
      const productoId = this.data.producto?.id;

      return of(null).pipe(
        debounceTime(500),
        switchMap(() => {
          return this.productoApi.verificarCodigo(this.data.empresaId!, codigo, productoId).pipe(
            map(response => {
              return response.existe ? { codigoDuplicado: true } : null;
            })
          );
        }),
        take(1)
      );
    };
  }

  descripcionValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value || !control.value.trim() || !this.data.empresaId) {
        return of(null);
      }

      const descripcion = control.value.toUpperCase().trim();
      const productoId = this.data.producto?.id;

      return of(null).pipe(
        debounceTime(500),
        switchMap(() => {
          return this.productoApi.verificarDescripcion(this.data.empresaId!, descripcion, productoId).pipe(
            map(response => {
              return response.existe ? { descripcionDuplicada: true } : null;
            })
          );
        }),
        take(1)
      );
    };
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.data.empresaId) {
      this.snackBar.open('Error: empresa no identificada', 'Cerrar', { duration: 3000 });
      return;
    }

    this.saving = true;
    const formValue = this.form.value;

    // Convertir todos los strings a mayúsculas antes de guardar
    const productoData: Partial<Producto> = {
      codigo: formValue.codigo ? formValue.codigo.toUpperCase().trim() : undefined,
      descripcion: formValue.descripcion.toUpperCase().trim(),
      unidadMedida: formValue.unidadMedida.toUpperCase().trim(),
      precio: formValue.precio,
      iva: formValue.iva,
      tipoTransaccion: formValue.tipoTransaccion,
      balanza: formValue.balanza,
      activo: formValue.activo,
      empresaId: this.data.empresaId
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
