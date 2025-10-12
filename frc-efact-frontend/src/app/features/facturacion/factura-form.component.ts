import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { FacturaApiService } from '../../core/api/factura-api.service';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { ProductoApiService } from '../../core/api/producto-api.service';
import { FacturaLegal, FacturaLegalItem } from '../../models/factura.model';
import { TimbradoDetalle } from '../../models/timbrado.model';
import { Cliente } from '../../models/cliente.model';
import { Producto } from '../../models/producto.model';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { FacturaItemComponent } from './factura-item.component';

@Component({
  selector: 'app-factura-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatCheckboxModule,
    MatAutocompleteModule,
    MatDividerModule,
    MatSnackBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    ErrorMessageComponent,
    LoadingSpinnerComponent,
    FacturaItemComponent
  ],
  template: `
    <div class="factura-form-container">
      <app-loading-spinner *ngIf="loading()" />

      <form [formGroup]="form" *ngIf="!loading()">
        <!-- Header -->
        <mat-card class="header-card">
          <mat-card-header>
            <mat-card-title>
              <h2>{{ isEdit ? 'Editar Factura' : 'Nueva Factura' }}</h2>
            </mat-card-title>
          </mat-card-header>
        </mat-card>

        <!-- Datos de la factura -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Datos de la Factura</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Timbrado Detalle</mat-label>
                <mat-select formControlName="timbradoDetalleId" (selectionChange)="onTimbradoChange()">
                  <mat-option *ngFor="let td of timbradosDetalle()" [value]="td.id">
                    {{ td.puntoExpedicion }} - {{ td.codigoEstablecimientoFactura }}
                    ({{ td.numeroActual }}/{{ td.rangoHasta }})
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('timbradoDetalleId')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha</mat-label>
                <input matInput 
                       [matDatepicker]="picker"
                       formControlName="fecha">
                <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
                <mat-datepicker #picker></mat-datepicker>
                <app-error-message [control]="form.get('fecha')" />
              </mat-form-field>

              <div class="checkbox-field">
                <mat-checkbox formControlName="credito">
                  Factura a crédito
                </mat-checkbox>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Datos del cliente -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Datos del Cliente</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Buscar Cliente</mat-label>
                <input matInput
                       formControlName="clienteSearch"
                       [matAutocomplete]="autoCliente"
                       placeholder="Buscar por nombre o RUC">
                <mat-icon matPrefix>search</mat-icon>
                <mat-autocomplete #autoCliente="matAutocomplete" 
                                  [displayWith]="displayCliente"
                                  (optionSelected)="onClienteSelected($event.option.value)">
                  <mat-option *ngFor="let cliente of clientesFiltrados()" [value]="cliente">
                    {{ cliente.nombre }} {{ cliente.ruc ? '- ' + cliente.ruc : '' }}
                  </mat-option>
                </mat-autocomplete>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Nombre</mat-label>
                <input matInput formControlName="nombre" placeholder="Nombre del cliente">
                <app-error-message [control]="form.get('nombre')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>RUC</mat-label>
                <input matInput formControlName="ruc" placeholder="RUC del cliente">
                <app-error-message [control]="form.get('ruc')" />
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Dirección</mat-label>
                <input matInput formControlName="direccion" placeholder="Dirección del cliente">
                <app-error-message [control]="form.get('direccion')" />
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Items de la factura -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>
              <div class="items-header">
                <span>Items de la Factura</span>
                <button mat-raised-button 
                        color="primary" 
                        type="button"
                        (click)="agregarItem()">
                  <mat-icon>add</mat-icon>
                  Agregar Item
                </button>
              </div>
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div formArrayName="items" class="items-container">
              <div *ngFor="let item of items.controls; let i = index">
                <app-factura-item
                  [formGroup]="$any(item)"
                  [index]="i"
                  [productos]="productos()"
                  (remove)="eliminarItem(i)"
                  (itemChange)="recalcularTotales()"
                />
                <mat-divider *ngIf="i < items.controls.length - 1"></mat-divider>
              </div>

              <div *ngIf="items.controls.length === 0" class="empty-items">
                <mat-icon>shopping_cart</mat-icon>
                <p>No hay items agregados. Haga clic en "Agregar Item" para comenzar.</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Totales -->
        <mat-card class="section-card totales-card">
          <mat-card-header>
            <mat-card-title>Totales</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <!-- Desglose por IVA -->
            <div class="totales-grid">
              <div class="total-section">
                <h4>IVA 10%</h4>
                <div class="total-row">
                  <span>Subtotal:</span>
                  <span class="amount">₲ {{ totales().totalParcial10.toLocaleString('es-PY') }}</span>
                </div>
                <div class="total-row">
                  <span>IVA:</span>
                  <span class="amount">₲ {{ totales().ivaParcial10.toLocaleString('es-PY') }}</span>
                </div>
              </div>

              <div class="total-section">
                <h4>IVA 5%</h4>
                <div class="total-row">
                  <span>Subtotal:</span>
                  <span class="amount">₲ {{ totales().totalParcial5.toLocaleString('es-PY') }}</span>
                </div>
                <div class="total-row">
                  <span>IVA:</span>
                  <span class="amount">₲ {{ totales().ivaParcial5.toLocaleString('es-PY') }}</span>
                </div>
              </div>

              <div class="total-section">
                <h4>Exento (0%)</h4>
                <div class="total-row">
                  <span>Subtotal:</span>
                  <span class="amount">₲ {{ totales().totalParcial0.toLocaleString('es-PY') }}</span>
                </div>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Total parcial y descuento -->
            <div class="totales-finales">
              <div class="total-row">
                <span class="label">Total Parcial:</span>
                <span class="amount">₲ {{ totales().totalParcial.toLocaleString('es-PY') }}</span>
              </div>

              <div class="total-row descuento-row">
                <mat-form-field appearance="outline" class="descuento-field">
                  <mat-label>Descuento Final</mat-label>
                  <input matInput 
                         type="number" 
                         formControlName="descuentoFinal"
                         (ngModelChange)="recalcularTotales()"
                         min="0"
                         step="1000">
                  <span matPrefix>₲&nbsp;</span>
                </mat-form-field>
              </div>

              <mat-divider></mat-divider>

              <div class="total-row total-final">
                <span class="label">TOTAL FINAL:</span>
                <span class="amount">₲ {{ totales().totalFinal.toLocaleString('es-PY') }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Acciones -->
        <div class="actions-container">
          <button mat-button type="button" (click)="onCancel()">
            Cancelar
          </button>
          <button mat-raised-button 
                  color="primary" 
                  type="button"
                  (click)="onSubmit()"
                  [disabled]="form.invalid || saving() || items.length === 0">
            {{ saving() ? 'Guardando...' : 'Guardar Factura' }}
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .factura-form-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .header-card {
      margin-bottom: 20px;
    }

    .section-card {
      margin-bottom: 20px;
    }

    .form-row {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      align-items: flex-start;
    }

    mat-form-field {
      flex: 1;
      min-width: 200px;
    }

    .full-width {
      flex: 1 1 100%;
    }

    .checkbox-field {
      display: flex;
      align-items: center;
      padding: 8px 0;
    }

    .items-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .items-container {
      min-height: 100px;
    }

    .empty-items {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;
      color: #666;
      text-align: center;
    }

    .empty-items mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      opacity: 0.5;
    }

    .totales-card {
      background-color: #f5f5f5;
    }

    .totales-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 24px;
      margin-bottom: 24px;
    }

    .total-section h4 {
      margin: 0 0 12px 0;
      color: #333;
      font-weight: 500;
    }

    .total-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      font-size: 14px;
    }

    .total-row .amount {
      font-weight: 500;
      color: #333;
    }

    .totales-finales {
      margin-top: 24px;
    }

    .totales-finales .total-row {
      font-size: 16px;
      padding: 12px 0;
    }

    .descuento-row {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .descuento-field {
      max-width: 300px;
      margin: 8px 0;
    }

    .total-final {
      font-size: 20px !important;
      font-weight: 600 !important;
      color: #1976d2 !important;
      padding: 16px 0 !important;
    }

    .total-final .amount {
      color: #1976d2 !important;
      font-weight: 700 !important;
    }

    .actions-container {
      display: flex;
      justify-content: flex-end;
      gap: 16px;
      margin-top: 24px;
      padding: 20px;
      background-color: white;
      border-top: 1px solid #e0e0e0;
      position: sticky;
      bottom: 0;
      z-index: 10;
    }

    mat-divider {
      margin: 16px 0;
    }

    h2 {
      margin: 0;
    }
  `]
})
export class FacturaFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  facturaId?: number;
  loading = signal(false);
  saving = signal(false);
  
  timbradosDetalle = signal<TimbradoDetalle[]>([]);
  clientesFiltrados = signal<Cliente[]>([]);
  productos = signal<Producto[]>([]);
  
  totales = computed(() => this.calcularTotales());

  constructor(
    private fb: FormBuilder,
    private facturaApi: FacturaApiService,
    private timbradoApi: TimbradoApiService,
    private clienteApi: ClienteApiService,
    private productoApi: ProductoApiService,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.facturaId = this.route.snapshot.params['id'];
    this.isEdit = !!this.facturaId;
    
    this.initForm();
    this.cargarDatos();
    this.setupClienteAutocomplete();
  }

  initForm(): void {
    this.form = this.fb.group({
      empresaId: [1], // TODO: Obtener del state
      timbradoDetalleId: [null, Validators.required],
      clienteId: [null],
      fecha: [new Date(), Validators.required],
      credito: [false],
      
      // Datos del cliente
      clienteSearch: [''],
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      ruc: ['', Validators.maxLength(20)],
      direccion: ['', Validators.maxLength(500)],
      
      // Items
      items: this.fb.array([]),
      
      // Totales (calculados automáticamente)
      descuentoFinal: [0, [Validators.min(0)]]
    });
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  cargarDatos(): void {
    this.loading.set(true);
    const empresaId = 1; // TODO: Obtener del state

    // Cargar timbrados detalle
    this.timbradoApi.getDetallesByEmpresa(empresaId).subscribe({
      next: (detalles) => {
        this.timbradosDetalle.set(detalles.filter(d => d.activo));
      },
      error: () => {
        this.snackBar.open('Error al cargar timbrados', 'Cerrar', { duration: 3000 });
      }
    });

    // Cargar productos
    this.productoApi.getByEmpresa(empresaId).subscribe({
      next: (productos) => {
        this.productos.set(productos.filter(p => p.activo));
      },
      error: () => {
        this.snackBar.open('Error al cargar productos', 'Cerrar', { duration: 3000 });
      }
    });

    // Si es edición, cargar factura
    if (this.isEdit && this.facturaId) {
      this.facturaApi.getById(this.facturaId).subscribe({
        next: (factura) => {
          this.cargarFactura(factura);
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Error al cargar factura', 'Cerrar', { duration: 3000 });
          this.loading.set(false);
        }
      });
    } else {
      this.loading.set(false);
    }
  }

  cargarFactura(factura: FacturaLegal): void {
    this.form.patchValue({
      empresaId: factura.empresaId,
      timbradoDetalleId: factura.timbradoDetalleId,
      clienteId: factura.clienteId,
      fecha: new Date(factura.fecha),
      credito: factura.credito,
      nombre: factura.nombre,
      ruc: factura.ruc,
      direccion: factura.direccion,
      descuentoFinal: factura.descuentoFinal
    });

    // Cargar items
    factura.items.forEach(item => {
      this.items.push(this.crearItemFormGroup(item));
    });

    this.recalcularTotales();
  }

  setupClienteAutocomplete(): void {
    this.form.get('clienteSearch')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(value => {
        if (typeof value === 'string' && value.length >= 2) {
          const empresaId = 1; // TODO: Obtener del state
          return this.clienteApi.buscar(empresaId, value);
        }
        return of([]);
      })
    ).subscribe(clientes => {
      this.clientesFiltrados.set(clientes);
    });
  }

  displayCliente(cliente: Cliente | null): string {
    return cliente ? `${cliente.nombre}${cliente.ruc ? ' - ' + cliente.ruc : ''}` : '';
  }

  onClienteSelected(cliente: Cliente): void {
    this.form.patchValue({
      clienteId: cliente.id,
      nombre: cliente.nombre,
      ruc: cliente.ruc || '',
      direccion: cliente.direccion || ''
    });
  }

  onTimbradoChange(): void {
    // Validar que el timbrado tenga números disponibles
    const timbradoDetalleId = this.form.get('timbradoDetalleId')?.value;
    const timbrado = this.timbradosDetalle().find(td => td.id === timbradoDetalleId);
    
    if (timbrado && timbrado.numeroActual >= timbrado.rangoHasta) {
      this.snackBar.open(
        'Advertencia: Este timbrado no tiene números disponibles',
        'Cerrar',
        { duration: 5000 }
      );
    }
  }

  agregarItem(): void {
    this.items.push(this.crearItemFormGroup());
  }

  crearItemFormGroup(item?: FacturaLegalItem): FormGroup {
    return this.fb.group({
      id: [item?.id],
      productoId: [item?.productoId],
      cantidad: [item?.cantidad || 1, [Validators.required, Validators.min(0.001)]],
      descripcion: [item?.descripcion || '', [Validators.required, Validators.maxLength(500)]],
      precioUnitario: [item?.precioUnitario || 0, [Validators.required, Validators.min(0)]],
      total: [{ value: item?.total || 0, disabled: true }]
    });
  }

  eliminarItem(index: number): void {
    this.items.removeAt(index);
    this.recalcularTotales();
  }

  recalcularTotales(): void {
    // Forzar recálculo del computed signal
    this.form.updateValueAndValidity();
  }

  calcularTotales() {
    const items = this.items.value as FacturaLegalItem[];
    const descuentoFinal = this.form.get('descuentoFinal')?.value || 0;

    let totalParcial0 = 0;
    let totalParcial5 = 0;
    let totalParcial10 = 0;

    items.forEach(item => {
      const producto = this.productos().find(p => p.id === item.productoId);
      const iva = producto?.iva || 0;
      const total = item.cantidad * item.precioUnitario;

      if (iva === 0) {
        totalParcial0 += total;
      } else if (iva === 5) {
        totalParcial5 += total;
      } else if (iva === 10) {
        totalParcial10 += total;
      }
    });

    // Calcular IVA
    const ivaParcial5 = totalParcial5 * 0.05;
    const ivaParcial10 = totalParcial10 * 0.10;
    const ivaParcial0 = 0;

    const totalParcial = totalParcial0 + totalParcial5 + totalParcial10;
    const totalFinal = totalParcial - descuentoFinal;

    return {
      ivaParcial0,
      ivaParcial5,
      ivaParcial10,
      totalParcial0,
      totalParcial5,
      totalParcial10,
      totalParcial,
      totalFinal: Math.max(0, totalFinal)
    };
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackBar.open('Por favor complete todos los campos requeridos', 'Cerrar', { duration: 3000 });
      return;
    }

    if (this.items.length === 0) {
      this.snackBar.open('Debe agregar al menos un item a la factura', 'Cerrar', { duration: 3000 });
      return;
    }

    this.saving.set(true);
    const totales = this.totales();
    
    const facturaData: Partial<FacturaLegal> = {
      empresaId: this.form.value.empresaId,
      timbradoDetalleId: this.form.value.timbradoDetalleId,
      clienteId: this.form.value.clienteId,
      fecha: this.form.value.fecha.toISOString(),
      credito: this.form.value.credito,
      nombre: this.form.value.nombre,
      ruc: this.form.value.ruc,
      direccion: this.form.value.direccion,
      items: this.items.value,
      descuentoFinal: this.form.value.descuentoFinal,
      ...totales
    };

    const request = this.isEdit && this.facturaId
      ? this.facturaApi.update(this.facturaId, facturaData)
      : this.facturaApi.create(facturaData);

    request.subscribe({
      next: (factura) => {
        this.snackBar.open(
          `Factura ${this.isEdit ? 'actualizada' : 'creada'} correctamente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.router.navigate(['/facturacion']);
      },
      error: (error) => {
        this.snackBar.open(
          error.error?.message || `Error al ${this.isEdit ? 'actualizar' : 'crear'} factura`,
          'Cerrar',
          { duration: 5000 }
        );
        this.saving.set(false);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/facturacion']);
  }
}
