import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { Producto } from '../../models/producto.model';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-factura-item',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatAutocompleteModule,
    MatTooltipModule,
    ErrorMessageComponent
  ],
  template: `
    <div class="item-container" [formGroup]="formGroup">
      <div class="item-number">
        <span class="number-badge">{{ index + 1 }}</span>
      </div>

      <div class="item-fields">
        <!-- Búsqueda de producto -->
        <mat-form-field appearance="outline" class="producto-field">
          <mat-label>Producto</mat-label>
          <input matInput
                 [matAutocomplete]="autoProducto"
                 (input)="onProductoSearch($event)"
                 [value]="getProductoDisplay()"
                 placeholder="Buscar producto">
          <mat-icon matPrefix>search</mat-icon>
          <mat-autocomplete #autoProducto="matAutocomplete" 
                            [displayWith]="displayProducto"
                            (optionSelected)="onProductoSelected($event.option.value)">
            <mat-option *ngFor="let producto of productosFiltrados" [value]="producto">
              <div class="producto-option">
                <span class="producto-desc">{{ producto.descripcion }}</span>
                <span class="producto-precio">₲ {{ producto.precio.toLocaleString('es-PY') }}</span>
                <span class="producto-iva">IVA {{ producto.iva }}%</span>
              </div>
            </mat-option>
          </mat-autocomplete>
        </mat-form-field>

        <!-- Descripción -->
        <mat-form-field appearance="outline" class="descripcion-field">
          <mat-label>Descripción</mat-label>
          <input matInput 
                 formControlName="descripcion"
                 placeholder="Descripción del item">
          <app-error-message [control]="formGroup.get('descripcion')" />
        </mat-form-field>

        <!-- Cantidad -->
        <mat-form-field appearance="outline" class="cantidad-field">
          <mat-label>Cantidad</mat-label>
          <input matInput 
                 type="number"
                 formControlName="cantidad"
                 (ngModelChange)="calcularTotal()"
                 min="0.001"
                 step="0.001"
                 placeholder="0">
          <app-error-message [control]="formGroup.get('cantidad')" />
        </mat-form-field>

        <!-- Precio Unitario -->
        <mat-form-field appearance="outline" class="precio-field">
          <mat-label>Precio Unit.</mat-label>
          <input matInput 
                 type="number"
                 formControlName="precioUnitario"
                 (ngModelChange)="calcularTotal()"
                 min="0"
                 step="1000"
                 placeholder="0">
          <span matPrefix>₲&nbsp;</span>
          <app-error-message [control]="formGroup.get('precioUnitario')" />
        </mat-form-field>

        <!-- Total (readonly) -->
        <mat-form-field appearance="outline" class="total-field">
          <mat-label>Total</mat-label>
          <input matInput 
                 [value]="getTotal()"
                 readonly
                 class="total-input">
          <span matPrefix>₲&nbsp;</span>
        </mat-form-field>

        <!-- IVA Badge -->
        <div class="iva-badge" *ngIf="getIvaProducto() !== null">
          <mat-icon [matTooltip]="'IVA ' + getIvaProducto() + '%'">
            {{ getIvaIcon() }}
          </mat-icon>
          <span>{{ getIvaProducto() }}%</span>
        </div>

        <!-- Botón eliminar -->
        <button mat-icon-button 
                color="warn"
                type="button"
                (click)="onRemove()"
                matTooltip="Eliminar item"
                class="remove-button">
          <mat-icon>delete</mat-icon>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .item-container {
      display: flex;
      gap: 16px;
      padding: 16px;
      background-color: white;
      border-radius: 4px;
      margin-bottom: 8px;
      align-items: flex-start;
    }

    .item-container:hover {
      background-color: #f5f5f5;
    }

    .item-number {
      display: flex;
      align-items: center;
      padding-top: 8px;
    }

    .number-badge {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      background-color: #1976d2;
      color: white;
      border-radius: 50%;
      font-weight: 600;
      font-size: 14px;
    }

    .item-fields {
      display: flex;
      gap: 12px;
      flex: 1;
      flex-wrap: wrap;
      align-items: flex-start;
    }

    .producto-field {
      flex: 2;
      min-width: 250px;
    }

    .descripcion-field {
      flex: 3;
      min-width: 300px;
    }

    .cantidad-field {
      flex: 0 0 120px;
      min-width: 120px;
    }

    .precio-field {
      flex: 0 0 150px;
      min-width: 150px;
    }

    .total-field {
      flex: 0 0 150px;
      min-width: 150px;
    }

    .total-input {
      font-weight: 600;
      color: #1976d2;
    }

    .iva-badge {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 8px;
      background-color: #e3f2fd;
      border-radius: 4px;
      min-width: 50px;
    }

    .iva-badge mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      color: #1976d2;
    }

    .iva-badge span {
      font-size: 12px;
      font-weight: 600;
      color: #1976d2;
      margin-top: 4px;
    }

    .remove-button {
      margin-top: 4px;
    }

    .producto-option {
      display: flex;
      gap: 12px;
      align-items: center;
      width: 100%;
    }

    .producto-desc {
      flex: 1;
      font-weight: 500;
    }

    .producto-precio {
      color: #1976d2;
      font-weight: 600;
    }

    .producto-iva {
      background-color: #e3f2fd;
      color: #1976d2;
      padding: 2px 8px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
    }

    mat-form-field {
      margin-bottom: 0;
    }
  `]
})
export class FacturaItemComponent implements OnInit {
  @Input() formGroup!: FormGroup;
  @Input() index!: number;
  @Input() productos: Producto[] = [];
  @Output() remove = new EventEmitter<void>();
  @Output() itemChange = new EventEmitter<void>();

  productosFiltrados: Producto[] = [];
  productoSeleccionado: Producto | null = null;

  ngOnInit(): void {
    this.productosFiltrados = this.productos;
    
    // Cargar producto si ya existe
    const productoId = this.formGroup.get('productoId')?.value;
    if (productoId) {
      this.productoSeleccionado = this.productos.find(p => p.id === productoId) || null;
    }

    // Escuchar cambios en cantidad y precio para recalcular
    this.formGroup.get('cantidad')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.calcularTotal();
    });

    this.formGroup.get('precioUnitario')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.calcularTotal();
    });
  }

  onProductoSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    const searchTerm = input.value.toLowerCase();

    if (searchTerm.length >= 2) {
      this.productosFiltrados = this.productos.filter(p =>
        p.descripcion.toLowerCase().includes(searchTerm) ||
        p.codigo?.toLowerCase().includes(searchTerm)
      );
    } else {
      this.productosFiltrados = this.productos;
    }
  }

  displayProducto(producto: Producto | null): string {
    return producto ? producto.descripcion : '';
  }

  getProductoDisplay(): string {
    return this.productoSeleccionado ? this.productoSeleccionado.descripcion : '';
  }

  onProductoSelected(producto: Producto): void {
    this.productoSeleccionado = producto;
    
    this.formGroup.patchValue({
      productoId: producto.id,
      descripcion: producto.descripcion,
      precioUnitario: producto.precio
    });

    // Si la cantidad es 0 o no está definida, establecer 1
    if (!this.formGroup.get('cantidad')?.value) {
      this.formGroup.patchValue({ cantidad: 1 });
    }

    this.calcularTotal();
  }

  calcularTotal(): void {
    const cantidad = this.formGroup.get('cantidad')?.value || 0;
    const precioUnitario = this.formGroup.get('precioUnitario')?.value || 0;
    const total = cantidad * precioUnitario;

    this.formGroup.patchValue({ total }, { emitEvent: false });
    this.itemChange.emit();
  }

  getTotal(): string {
    const total = this.formGroup.get('cantidad')?.value * this.formGroup.get('precioUnitario')?.value || 0;
    return total.toLocaleString('es-PY');
  }

  getIvaProducto(): number | null {
    if (this.productoSeleccionado) {
      return this.productoSeleccionado.iva;
    }
    return null;
  }

  getIvaIcon(): string {
    const iva = this.getIvaProducto();
    if (iva === 10) return 'trending_up';
    if (iva === 5) return 'trending_flat';
    if (iva === 0) return 'remove';
    return 'help';
  }

  onRemove(): void {
    this.remove.emit();
  }
}
