import { Component, OnInit, signal, computed, OnDestroy } from '@angular/core';
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
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, distinctUntilChanged, switchMap, of, Subject, takeUntil } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { FacturaApiService } from '../../core/api/factura-api.service';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { ProductoApiService } from '../../core/api/producto-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { SifenApiService } from '../../core/api/sifen-api.service';
import { FacturaLegal, FacturaLegalItem } from '../../models/factura.model';
import { TimbradoDetalle } from '../../models/timbrado.model';
import { Cliente } from '../../models/cliente.model';
import { Producto } from '../../models/producto.model';
import { Empresa } from '../../models/empresa.model';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { FacturaItemDialogComponent } from './factura-item-dialog.component';
import { ClienteFormComponent } from '../clientes/cliente-form.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

interface FacturaItemView {
  id?: number;
  productoId?: number;
  descripcion: string;
  cantidad: number;
  precioUnitario: number;
  iva: number;
  total: number;
}

// Cliente especial para operaciones B2C sin nombre
const CLIENTE_SIN_NOMBRE: Cliente = {
  id: -1, // ID especial para identificar que es "SIN NOMBRE"
  empresaId: 0,
  nombre: 'SIN NOMBRE',
  ruc: 'X',
  tipoClienteSifen: 'NO_CONTRIBUYENTE',
  tributa: false,
  activo: true
};

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
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    ErrorMessageComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="factura-form-container">
      <app-loading-spinner *ngIf="loading()" />

      <form [formGroup]="form" *ngIf="!loading()">
        <!-- Header -->
        <mat-card class="header-card">
          <mat-card-header>
            <mat-card-title>
              <div class="header-content">
                <button mat-icon-button (click)="onCancel()" matTooltip="Volver atrás">
                  <mat-icon>arrow_back</mat-icon>
                </button>
                <div class="header-text">
                  <h2>{{ isEdit ? 'Editar Factura' : 'Nueva Factura' }}</h2>
                  <p class="empresa-name" *ngIf="empresaNombre()">{{ empresaNombre() }}</p>
                </div>
              </div>
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
                <mat-select formControlName="timbradoDetalleId"
                           (selectionChange)="onTimbradoChange()">
                  <mat-option *ngFor="let td of timbradosDetalle()" [value]="td.id">
                    {{ td.timbradoNumero || 'N/A' }} - {{ td.codigoEstablecimientoFactura }} - {{ td.puntoExpedicion }}
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
                  <mat-option [value]="CLIENTE_SIN_NOMBRE" class="cliente-sin-nombre-option">
                    <div class="cliente-sin-nombre">
                      <mat-icon>person_off</mat-icon>
                      <span><strong>SIN NOMBRE</strong> - RUC: X (B2C - Consumidor Final)</span>
                    </div>
                  </mat-option>
                  <mat-option *ngFor="let cliente of clientesFiltrados()" [value]="cliente">
                    {{ cliente.nombre || cliente.razonSocial }} {{ cliente.ruc ? '- ' + cliente.ruc : '' }}
                  </mat-option>
                </mat-autocomplete>
                <mat-hint>Seleccione un cliente o "SIN NOMBRE" para operaciones B2C</mat-hint>
              </mat-form-field>

              <button mat-stroked-button
                      color="primary"
                      type="button"
                      class="nuevo-cliente-button"
                      (click)="abrirDialogoNuevoCliente()">
                <mat-icon>person_add</mat-icon>
                Nuevo Cliente
              </button>
            </div>

            <div *ngIf="clienteSeleccionado() || esClienteSinNombre()" class="cliente-info-section">
              <mat-divider></mat-divider>
              <h4 class="cliente-info-title">Información del Cliente</h4>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Nombre</mat-label>
                  <input matInput formControlName="nombre" readonly>
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>RUC</mat-label>
                  <input matInput formControlName="ruc" readonly>
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Tipo Cliente SIFEN</mat-label>
                  <input matInput [value]="getTipoClienteSifenLabel()" readonly>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Dirección</mat-label>
                  <input matInput formControlName="direccion" readonly>
                </mat-form-field>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Moneda -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Moneda</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-row">
              <mat-form-field appearance="outline" class="moneda-field">
                <mat-label>Moneda</mat-label>
                <mat-select formControlName="monedaExtranjera">
                  <mat-option *ngFor="let moneda of MONEDAS" [value]="moneda.codigo">
                    {{ moneda.codigo }} - {{ moneda.nombre }}
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('monedaExtranjera')" />
              </mat-form-field>

              <mat-form-field appearance="outline" class="cambio-field">
                <mat-label>Tipo de Cambio</mat-label>
                <input matInput
                       type="number"
                       formControlName="cambio"
                       placeholder="Ej: 6990">
                <mat-hint>Tipo de cambio respecto al guaraní ({{ simboloMoneda() }}/₲)</mat-hint>
                <app-error-message [control]="form.get('cambio')" />
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Items de la factura -->
        <mat-card class="section-card">
          <mat-card-header class="items-header">
            <mat-card-title>Items de la Factura</mat-card-title>
            <button mat-raised-button
                    color="primary"
                    type="button"
                    class="agregar-item-button"
                    (click)="agregarItem()">
              <mat-icon>add</mat-icon>
              AGREGAR ITEM
            </button>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="itemsData().length > 0; else emptyItems" class="items-table-wrapper">
              <table mat-table [dataSource]="itemsDataConvertidos()" class="items-table mat-elevation-z1">
                <ng-container matColumnDef="descripcion">
                  <th mat-header-cell *matHeaderCellDef>DESCRIPCIÓN</th>
                  <td mat-cell *matCellDef="let item">{{ item.descripcion }}</td>
                </ng-container>

                <ng-container matColumnDef="cantidad">
                  <th mat-header-cell *matHeaderCellDef>CANTIDAD</th>
                  <td mat-cell *matCellDef="let item">{{ item.cantidad }}</td>
                </ng-container>

                <ng-container matColumnDef="precioUnitario">
                  <th mat-header-cell *matHeaderCellDef>PRECIO UNITARIO</th>
                  <td mat-cell *matCellDef="let item">{{ simboloMoneda() }} {{ item.precioUnitario | number:'1.0-3':'es-PY' }}</td>
                </ng-container>

                <ng-container matColumnDef="iva">
                  <th mat-header-cell *matHeaderCellDef>IVA</th>
                  <td mat-cell *matCellDef="let item">{{ item.iva }}%</td>
                </ng-container>

                <ng-container matColumnDef="total">
                  <th mat-header-cell *matHeaderCellDef>TOTAL</th>
                  <td mat-cell *matCellDef="let item">{{ simboloMoneda() }} {{ item.total | number:'1.0-3':'es-PY' }}</td>
                </ng-container>

                <ng-container matColumnDef="acciones">
                  <th mat-header-cell *matHeaderCellDef>ACCIONES</th>
                  <td mat-cell *matCellDef="let item; let i = index" class="items-actions">
                    <button mat-icon-button
                            type="button"
                            color="primary"
                            (click)="editarItem(i)"
                            matTooltip="EDITAR ITEM">
                      <mat-icon>edit</mat-icon>
                    </button>
                    <button mat-icon-button
                            type="button"
                            color="warn"
                            (click)="eliminarItem(i)"
                            matTooltip="ELIMINAR ITEM">
                      <mat-icon>delete</mat-icon>
                    </button>
                  </td>
                </ng-container>

                <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
              </table>
            </div>

            <ng-template #emptyItems>
              <div class="empty-items">
                <mat-icon>shopping_cart</mat-icon>
                <p>NO HAY ITEMS AGREGADOS. HAGA CLIC EN "AGREGAR ITEM" PARA COMENZAR.</p>
              </div>
            </ng-template>
          </mat-card-content>
        </mat-card>

        <!-- Totales -->
        <mat-card class="section-card totales-card">
          <mat-card-header>
            <mat-card-title>TOTALES</mat-card-title>
          </mat-card-header>
          <mat-card-content class="totales-content">
            <div class="totales-grid">
              <div class="total-card iva-10">
                <div class="total-card-header">
                  <span>IVA 10%</span>
                  <span class="badge">10%</span>
                </div>
                <div class="total-card-body">
                  <div class="total-row">
                    <span>SUBTOTAL</span>
                    <strong>{{ simboloMoneda() }} {{ totales().totalParcial10 | number:'1.0-3':'es-PY' }}</strong>
                  </div>
                  <div class="total-row">
                    <span>IVA</span>
                    <strong>{{ simboloMoneda() }} {{ totales().ivaParcial10 | number:'1.0-3':'es-PY' }}</strong>
                  </div>
                </div>
              </div>

              <div class="total-card iva-5">
                <div class="total-card-header">
                  <span>IVA 5%</span>
                  <span class="badge">5%</span>
                </div>
                <div class="total-card-body">
                  <div class="total-row">
                    <span>SUBTOTAL</span>
                    <strong>{{ simboloMoneda() }} {{ totales().totalParcial5 | number:'1.0-3':'es-PY' }}</strong>
                  </div>
                  <div class="total-row">
                    <span>IVA</span>
                    <strong>{{ simboloMoneda() }} {{ totales().ivaParcial5 | number:'1.0-3':'es-PY' }}</strong>
                  </div>
                </div>
              </div>

              <div class="total-card iva-0">
                <div class="total-card-header">
                  <span>EXENTO</span>
                  <span class="badge">0%</span>
                </div>
                <div class="total-card-body">
                  <div class="total-row">
                    <span>SUBTOTAL</span>
                    <strong>{{ simboloMoneda() }} {{ totales().totalParcial0 | number:'1.0-3':'es-PY' }}</strong>
                  </div>
                  <div class="total-row">
                    <span>IVA</span>
                    <strong>{{ simboloMoneda() }} {{ totales().ivaParcial0 | number:'1.0-3':'es-PY' }}</strong>
                  </div>
                </div>
              </div>
            </div>

            <div class="totales-resumen">
              <div class="resumen-row">
                <span>SUBTOTAL GENERAL</span>
                <span class="amount">{{ simboloMoneda() }} {{ totales().totalParcial | number:'1.0-3':'es-PY' }}</span>
              </div>

              <div class="resumen-row">
                <span>IVA TOTAL</span>
                <span class="amount">{{ simboloMoneda() }} {{ totales().ivaTotal | number:'1.0-3':'es-PY' }}</span>
              </div>

              <div class="resumen-row descuento-row">
                <div class="descuento-label">
                  <span>DESCUENTO</span>
                  <small>Aplicado a la factura</small>
                </div>
                <mat-form-field appearance="outline" class="descuento-field">
                  <mat-label>DESCUENTO FINAL</mat-label>
                  <span matPrefix>{{ simboloMoneda() }}&nbsp;</span>
                  <input matInput
                         type="number"
                         formControlName="descuentoFinal"
                         min="0"
                         step="1000">
                </mat-form-field>
              </div>

              <div class="resumen-row total-final">
                <span>TOTAL A PAGAR</span>
                <span class="amount">{{ simboloMoneda() }} {{ totales().totalFinal | number:'1.0-3':'es-PY' }}</span>
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

    .moneda-field {
      flex: 0 0 calc(50% - 8px);
      min-width: 200px;
    }

    .cambio-field {
      flex: 0 0 calc(50% - 8px);
      min-width: 200px;
    }

    .nuevo-cliente-button {
      align-self: flex-end;
      height: 56px;
      display: flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;
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
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      flex-wrap: wrap;
    }

    .items-header mat-card-title {
      margin: 0;
      flex: 1;
      min-width: 200px;
    }

    .agregar-item-button {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;
      flex-shrink: 0;
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

    .items-table-wrapper {
      overflow-x: auto;
    }

    .items-table {
      width: 100%;
      min-width: 640px;
    }

    .items-table .mat-header-cell,
    .items-table .mat-cell {
      padding: 12px 16px;
    }

    .items-actions {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 8px;
    }

    .totales-card {
      background: linear-gradient(180deg, #f7f9fc 0%, #ffffff 100%);
    }

    .totales-content {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .totales-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 20px;
    }

    .total-card {
      background: white;
      border: 1px solid #e3e7ef;
      border-radius: 12px;
      padding: 16px 18px;
      box-shadow: 0 6px 16px rgba(25, 118, 210, 0.08);
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .total-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
      color: #1f2937;
    }

    .total-card-header span:first-child {
      letter-spacing: 0.04em;
    }

    .total-card .badge {
      background: #1976d2;
      color: white;
      border-radius: 999px;
      padding: 4px 10px;
      font-size: 12px;
      letter-spacing: 0.06em;
    }

    .total-card-body {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .total-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #4b5563;
    }

    .total-row strong {
      font-size: 16px;
      font-weight: 600;
      color: #111827;
    }

    .iva-5 .badge {
      background: #2e7d32;
    }

    .iva-0 .badge {
      background: #9e9e9e;
    }

    .totales-resumen {
      display: flex;
      flex-direction: column;
      gap: 16px;
      padding: 20px;
      background: rgba(25, 118, 210, 0.06);
      border-radius: 12px;
      border: 1px solid rgba(25, 118, 210, 0.15);
    }

    .resumen-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      font-size: 16px;
      color: #1f2937;
    }

    .resumen-row .amount {
      font-weight: 600;
      font-size: 18px;
      color: #0d47a1;
    }

    .descuento-row {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .descuento-label {
      display: flex;
      flex-direction: column;
      gap: 4px;
      color: #374151;
    }

    .descuento-label small {
      font-size: 12px;
      color: #6b7280;
    }

    .descuento-field {
      max-width: 260px;
      margin: 0;
    }

    .total-final {
      padding-top: 12px;
      border-top: 1px dashed rgba(13, 71, 161, 0.3);
      margin-top: 8px;
      font-size: 18px;
      font-weight: 700;
    }

    .total-final .amount {
      font-size: 22px;
      font-weight: 700;
      color: #0d47a1;
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

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .header-text {
      flex: 1;
    }

    .empresa-name {
      margin: 4px 0 0 0;
      font-size: 14px;
      color: #666;
      font-weight: 500;
    }

    .cliente-info-section {
      margin-top: 24px;
    }

    .cliente-info-title {
      margin: 16px 0 16px 0;
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .cliente-sin-nombre-option {
      border-bottom: 1px solid #e0e0e0;
      margin-bottom: 4px;
    }

    .cliente-sin-nombre {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 0;
    }

    .cliente-sin-nombre mat-icon {
      color: #ff9800;
    }

    .cliente-sin-nombre strong {
      color: #ff9800;
    }
  `]
})
export class FacturaFormComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  form!: FormGroup;
  isEdit = false;
  facturaId?: number;
  loading = signal(false);
  saving = signal(false);
  displayedColumns: string[] = ['descripcion', 'cantidad', 'precioUnitario', 'iva', 'total', 'acciones'];

  timbradosDetalle = signal<TimbradoDetalle[]>([]);
  clientesFiltrados = signal<Cliente[]>([]);
  productos = signal<Producto[]>([]);
  empresaNombre = signal<string>('');
  clienteSeleccionado = signal<Cliente | null>(null);
  itemsData = signal<FacturaItemView[]>([]);
  descuentoFinalSignal = signal(0);

  // Cliente especial "SIN NOMBRE" para operaciones B2C
  readonly CLIENTE_SIN_NOMBRE = CLIENTE_SIN_NOMBRE;

  // Monedas disponibles (códigos ISO 4217)
  readonly MONEDAS = [
    { codigo: 'PYG', nombre: 'Guaraní Paraguayo', simbolo: '₲' },
    { codigo: 'USD', nombre: 'Dólar Americano', simbolo: '$' },
    { codigo: 'EUR', nombre: 'Euro', simbolo: '€' },
    { codigo: 'BRL', nombre: 'Real Brasileño', simbolo: 'R$' },
    { codigo: 'ARS', nombre: 'Peso Argentino', simbolo: '$' }
  ];

  timbradoDetalleDeshabilitado = computed(() => {
    const detalles = this.timbradosDetalle();
    return detalles.length === 1;
  });

  // Actualizar estado disabled del timbrado cuando cambia
  private actualizarEstadoTimbrado(): void {
    const timbradoControl = this.form.get('timbradoDetalleId');
    if (this.timbradoDetalleDeshabilitado()) {
      timbradoControl?.disable({ emitEvent: false });
    } else {
      timbradoControl?.enable({ emitEvent: false });
    }
  }

  // Signal para moneda extranjera que reacciona a cambios del formulario
  monedaExtranjeraSignal = signal<string>('PYG');

  // Signal para tipo de cambio que reacciona a cambios del formulario
  cambioSignal = signal<number>(1);

  // Moneda seleccionada y tipo de cambio
  monedaSeleccionada = computed(() => {
    const moneda = this.monedaExtranjeraSignal();
    return this.MONEDAS.find(m => m.codigo === moneda) || this.MONEDAS[0];
  });

  // Tipo de cambio: siempre devuelve un número (1 si no hay moneda extranjera)
  // Esto permite usar siempre valorGs / cambio sin necesidad de validaciones
  // Usa el signal cambioSignal que se actualiza cuando cambia el control
  tipoCambio = computed(() => {
    const cambio = this.cambioSignal();
    const cambioNumero = cambio && cambio > 0 ? cambio : 1;

    // Log para debugging
    console.log('🔍 tipoCambio computed:', {
      cambioSignal: cambio,
      cambioNumero: cambioNumero,
      monedaExtranjera: this.monedaExtranjeraSignal()
    });

    return cambioNumero;
  });

  esMonedaExtranjera = computed(() => {
    const moneda = this.monedaExtranjeraSignal();
    return moneda !== 'PYG';
  });

  // Función para convertir valor a moneda extranjera SOLO para visualización
  // Siempre divide por cambio (que es 1 si no hay moneda extranjera)
  convertirAMonedaExtranjera = (valorGs: number): number => {
    return valorGs / this.tipoCambio();
  };

  // Función para obtener símbolo de moneda
  simboloMoneda = computed(() => {
    return this.monedaSeleccionada().simbolo;
  });

  totales = computed(() => {
    const descuentoFinalGs = this.descuentoFinalSignal();
    const totalesGs = this.calcularTotales(this.itemsData(), descuentoFinalGs);
    const cambio = this.tipoCambio(); // Siempre es un número (1 si no hay moneda extranjera)

    // Convertir valores para visualización (dividir por cambio, que es 1 si no hay moneda extranjera)
    return {
      ...totalesGs,
      totalParcial0: totalesGs.totalParcial0 / cambio,
      totalParcial5: totalesGs.totalParcial5 / cambio,
      totalParcial10: totalesGs.totalParcial10 / cambio,
      ivaParcial0: totalesGs.ivaParcial0 / cambio,
      ivaParcial5: totalesGs.ivaParcial5 / cambio,
      ivaParcial10: totalesGs.ivaParcial10 / cambio,
      totalParcial: totalesGs.totalParcial / cambio,
      descuentoFinal: descuentoFinalGs / cambio,
      totalFinal: totalesGs.totalFinal / cambio
    };
  });

  // Items con valores convertidos para visualización
  // Siempre divide por cambio (que es 1 si no hay moneda extranjera)
  itemsDataConvertidos = computed(() => {
    const items = this.itemsData();
    const cambio = this.tipoCambio(); // Siempre es un número (1 si no hay moneda extranjera)

    return items.map(item => ({
      ...item,
      precioUnitario: item.precioUnitario / cambio,
      total: item.total / cambio
    }));
  });

  constructor(
    private fb: FormBuilder,
    private facturaApi: FacturaApiService,
    private timbradoApi: TimbradoApiService,
    private clienteApi: ClienteApiService,
    private productoApi: ProductoApiService,
    private empresaApi: EmpresaApiService,
    private sifenApi: SifenApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.facturaId = this.route.snapshot.params['id'];
    this.isEdit = !!this.facturaId;

    // Obtener empresaId de query params o snapshot
    const empresaIdFromParams = this.route.snapshot.queryParams['empresaId'];
    const empresaId = empresaIdFromParams ? +empresaIdFromParams : 1; // Default para compatibilidad

    // Inicializar con el empresaId disponible
    this.initForm(empresaId);
    if (empresaIdFromParams) {
      this.cargarEmpresa(empresaId);
    }
    this.cargarDatos(empresaId);
    this.setupClienteAutocomplete(empresaId);

    // Suscribirse a cambios en query params
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const empresaIdParam = params['empresaId'];
      if (empresaIdParam && +empresaIdParam !== empresaId) {
        const id = +empresaIdParam;
        this.cargarEmpresa(id);
        // Actualizar empresaId en el formulario si cambia
        this.form.patchValue({ empresaId: id });
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initForm(empresaId: number): void {
    this.form = this.fb.group({
      empresaId: [empresaId],
      timbradoDetalleId: [null, Validators.required],
      clienteId: [null], // Ya no es requerido, puede ser null para "SIN NOMBRE"
      fecha: [new Date(), Validators.required],
      credito: [false],

      // Datos del cliente
      clienteSearch: [''],
      nombre: ['', [Validators.required, Validators.maxLength(200)]],
      ruc: ['', Validators.maxLength(20)],
      direccion: ['', Validators.maxLength(500)],

      // Moneda extranjera
      monedaExtranjera: ['PYG'],
      cambio: [{ value: 1, disabled: true }], // Inicializar en 1 para que tipoCambio siempre sea un número

      // Items
      items: this.fb.array([]),

      // Totales (calculados automáticamente)
      descuentoFinal: [0, [Validators.min(0)]]
    });

    // Inicializar signals con los valores iniciales del formulario
    const monedaInicial = this.form.get('monedaExtranjera')?.value || 'PYG';
    const cambioInicial = this.form.get('cambio')?.getRawValue() || 1;
    this.monedaExtranjeraSignal.set(monedaInicial);
    this.cambioSignal.set(cambioInicial);

    // Validación condicional para cambio cuando moneda != PYG
    this.form.get('monedaExtranjera')?.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(moneda => {
      // Actualizar signal de moneda inmediatamente cuando cambia
      this.monedaExtranjeraSignal.set(moneda || 'PYG');

      const cambioControl = this.form.get('cambio');
      if (moneda && moneda !== 'PYG') {
        cambioControl?.setValidators([Validators.required, Validators.min(0.0001)]);
        cambioControl?.enable();
        // Actualizar signal con el valor actual del control
        const cambioActual = cambioControl?.getRawValue() || 1;
        this.cambioSignal.set(cambioActual);
      } else {
        cambioControl?.clearValidators();
        cambioControl?.setValue(1); // Establecer en 1 cuando es PYG
        cambioControl?.disable();
        this.cambioSignal.set(1); // Actualizar signal a 1 cuando es PYG
      }
      cambioControl?.updateValueAndValidity({ emitEvent: false });

      // Actualizar itemsData para que los computed signals se actualicen (solo para visualización)
      this.actualizarItemsData();
    });

    // Cuando cambia el tipo de cambio, actualizar el signal y itemsData
    this.form.get('cambio')?.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(cambio => {
      const cambioNumero = cambio && cambio > 0 ? cambio : 1;
      this.cambioSignal.set(cambioNumero);
      this.actualizarItemsData();
    });

    // Inicializar estado del control de cambio
    const cambioControl = this.form.get('cambio');
    if (monedaInicial === 'PYG') {
      cambioControl?.disable();
    }

    this.descuentoFinalSignal.set(this.form.get('descuentoFinal')?.value || 0);

    this.form.get('descuentoFinal')?.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(value => {
      const parsed = Number(value) || 0;
      const sanitized = parsed < 0 ? 0 : parsed;
      if (sanitized !== value) {
        this.form.get('descuentoFinal')?.setValue(sanitized, { emitEvent: false });
      }
      this.descuentoFinalSignal.set(sanitized);
    });
  }

  cargarEmpresa(empresaId: number): void {
    this.empresaApi.getById(empresaId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (empresa: Empresa) => {
        this.empresaNombre.set(empresa.razonSocial || empresa.nombreFantasia || '');
      },
      error: () => {
        this.snackBar.open('Error al cargar información de la empresa', 'Cerrar', { duration: 3000 });
      }
    });
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  cargarDatos(empresaId: number): void {
    this.loading.set(true);

    // Cargar timbrados detalle
    this.timbradoApi.getDetallesByEmpresa(empresaId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (detalles) => {
        const detallesActivos = detalles.filter(d => d.activo);
        this.timbradosDetalle.set(detallesActivos);

        // Si hay solo un timbrado detalle activo, preseleccionarlo
        if (detallesActivos.length === 1 && !this.isEdit) {
          this.form.patchValue({
            timbradoDetalleId: detallesActivos[0].id
          });
        }

        // Actualizar estado disabled del control
        this.actualizarEstadoTimbrado();
      },
      error: () => {
        this.snackBar.open('Error al cargar timbrados', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });

    // Cargar productos
    this.productoApi.getByEmpresa(empresaId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (productos) => {
        this.productos.set(productos.content.filter((p: Producto) => p.activo));
        this.actualizarItemsData();
      },
      error: () => {
        this.snackBar.open('Error al cargar productos', 'Cerrar', { duration: 3000 });
      }
    });

    // Si es edición, cargar factura
    if (this.isEdit && this.facturaId) {
      this.facturaApi.getById(this.facturaId).pipe(takeUntil(this.destroy$)).subscribe({
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
    const monedaFactura = factura.monedaExtranjera || 'PYG';
    this.monedaExtranjeraSignal.set(monedaFactura);
    this.form.patchValue({
      empresaId: factura.empresaId,
      timbradoDetalleId: factura.timbradoDetalleId,
      clienteId: factura.clienteId,
      fecha: new Date(factura.fecha),
      credito: factura.credito,
      monedaExtranjera: monedaFactura,
      cambio: factura.cambio || null,
      nombre: factura.nombre,
      ruc: factura.ruc,
      direccion: factura.direccion,
      descuentoFinal: factura.descuentoFinal
    });
    this.descuentoFinalSignal.set(factura.descuentoFinal || 0);

    // Si hay clienteId, cargar información completa del cliente
    if (factura.clienteId) {
      this.clienteApi.getById(factura.empresaId, factura.clienteId).pipe(takeUntil(this.destroy$)).subscribe({
        next: (cliente) => {
          this.clienteSeleccionado.set(cliente);
          this.form.patchValue({
            clienteSearch: this.displayCliente(cliente)
          });
        },
        error: () => {
          // Si no se puede cargar el cliente, usar los datos de la factura
          console.warn('No se pudo cargar información completa del cliente');
        }
      });
    } else if (factura.ruc === 'X' && factura.nombre === 'SIN NOMBRE') {
      // Si no hay clienteId y el RUC es "X" con nombre "SIN NOMBRE", es un cliente B2C
      this.clienteSeleccionado.set(CLIENTE_SIN_NOMBRE);
      this.form.patchValue({
        clienteSearch: 'SIN NOMBRE - RUC: X'
      });
    }

    // Cargar items
    factura.items.forEach(item => {
      this.items.push(this.crearItemFormGroup(item));
    });

    this.actualizarItemsData();
  }

  setupClienteAutocomplete(empresaId: number): void {
    this.form.get('clienteSearch')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
      switchMap(value => {
        // Si el valor es un objeto Cliente, extraer el string para buscar
        const searchTerm = typeof value === 'string' ? value : (value?.nombre || '');

        // Si el término de búsqueda incluye "sin nombre" o "x", incluir la opción especial
        const searchLower = typeof searchTerm === 'string' ? searchTerm.toLowerCase() : '';
        const incluirSinNombre = searchLower.includes('sin nombre') ||
                                 searchLower.includes('sin nombre') ||
                                 searchLower === 'x' ||
                                 searchTerm === '';

        if (typeof searchTerm === 'string' && searchTerm.length >= 2) {
          return this.clienteApi.buscar(empresaId, searchTerm).pipe(
            switchMap(clientes => {
              // Si se debe incluir "SIN NOMBRE", agregarlo a la lista
              if (incluirSinNombre) {
                return of([CLIENTE_SIN_NOMBRE, ...clientes]);
              }
              return of(clientes);
            })
          );
        } else if (incluirSinNombre) {
          // Si el término es muy corto pero se debe mostrar "SIN NOMBRE", mostrarlo
          return of([CLIENTE_SIN_NOMBRE]);
        }
        return of([]);
      })
    ).subscribe(clientes => {
      this.clientesFiltrados.set(clientes);
    });
  }

  displayCliente(cliente: Cliente | null): string {
    if (!cliente) return '';
    const nombre = cliente.nombre || cliente.razonSocial || '';
    return `${nombre}${cliente.ruc ? ' - ' + cliente.ruc : ''}`;
  }

  esClienteSinNombre(): boolean {
    const cliente = this.clienteSeleccionado();
    return cliente?.id === CLIENTE_SIN_NOMBRE.id || this.form.get('ruc')?.value === 'X';
  }

  onClienteSelected(cliente: Cliente): void {
    if (!cliente) return;

    // Verificar si es el cliente "SIN NOMBRE"
    if (cliente.id === CLIENTE_SIN_NOMBRE.id) {
      this.clienteSeleccionado.set(CLIENTE_SIN_NOMBRE);
      this.form.patchValue({
        clienteId: null, // No hay clienteId para "SIN NOMBRE"
        nombre: 'SIN NOMBRE',
        ruc: 'X',
        direccion: '',
        clienteSearch: 'SIN NOMBRE - RUC: X'
      });
    } else {
      this.clienteSeleccionado.set(cliente);
      const nombreCliente = cliente.nombre || cliente.razonSocial || '';

      this.form.patchValue({
        clienteId: cliente.id,
        nombre: nombreCliente,
        ruc: cliente.ruc || '',
        direccion: cliente.direccion || '',
        clienteSearch: this.displayCliente(cliente) // Mostrar el nombre en el input
      });
    }
  }

  abrirDialogoNuevoCliente(): void {
    const empresaId = this.form.get('empresaId')?.value;

    const dialogRef = this.dialog.open(ClienteFormComponent, {
      width: '700px',
      data: { cliente: null, empresaId }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((cliente: Cliente | undefined) => {
      if (cliente) {
        this.clienteSeleccionado.set(cliente);
        const nombreCliente = cliente.nombre || cliente.razonSocial || '';

        this.form.patchValue({
          clienteId: cliente.id,
          nombre: nombreCliente,
          ruc: cliente.ruc || '',
          direccion: cliente.direccion || '',
          clienteSearch: this.displayCliente(cliente)
        });

        const clientesActuales = this.clientesFiltrados();
        this.clientesFiltrados.set([cliente, ...clientesActuales.filter(c => c.id !== cliente.id)]);
      }
    });
  }

  getTipoClienteSifenLabel(): string {
    const cliente = this.clienteSeleccionado();
    if (!cliente?.tipoClienteSifen) {
      // Si es cliente "SIN NOMBRE", mostrar tipo B2C
      if (this.esClienteSinNombre()) {
        return 'No Contribuyente (B2C)';
      }
      return 'No especificado';
    }

    const labels: { [key: string]: string } = {
      'PERSONA_FISICA': 'Persona Física',
      'PERSONA_JURIDICA': 'Persona Jurídica',
      'NO_CONTRIBUYENTE': 'No Contribuyente (B2C)',
      'EXTRANJERO': 'Extranjero',
      'GUBERNAMENTAL': 'Gubernamental'
    };

    return labels[cliente.tipoClienteSifen] || cliente.tipoClienteSifen;
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
    this.abrirDialogoItem();
  }

  crearItemFormGroup(item?: FacturaLegalItem): FormGroup {
    return this.fb.group({
      id: [item?.id],
      productoId: [item?.productoId, Validators.required],
      cantidad: [item?.cantidad || 1, [Validators.required, Validators.min(0.001)]],
      descripcion: [item?.descripcion || '', [Validators.required, Validators.maxLength(500)]],
      precioUnitario: [item?.precioUnitario || 0, [Validators.required, Validators.min(0)]],
      total: [{ value: item?.total || (item?.cantidad || 0) * (item?.precioUnitario || 0), disabled: true }]
    });
  }

  eliminarItem(index: number): void {
    this.items.removeAt(index);
    this.actualizarItemsData();
  }

  editarItem(index: number): void {
    const itemGroup = this.items.at(index) as FormGroup;
    if (!itemGroup) {
      return;
    }
    const itemData = itemGroup.getRawValue() as FacturaLegalItem;
    this.abrirDialogoItem(itemData, index);
  }

  private abrirDialogoItem(item?: FacturaLegalItem, index?: number): void {
    // IMPORTANTE: El diálogo siempre recibe y devuelve valores en GUARANÍES
    // La conversión para visualización se hace dentro del diálogo usando el tipo de cambio
    const cambioControl = this.form.get('cambio');
    const cambioRawValue = cambioControl?.getRawValue(); // Usar getRawValue() para obtener el valor incluso si está disabled
    const tipoCambioValue = (cambioRawValue && cambioRawValue > 0) ? cambioRawValue : 1;
    const monedaValue = this.form.get('monedaExtranjera')?.value || 'PYG';

    console.log('🔍 abrirDialogoItem - Pasando datos al diálogo:', {
      itemPrecioUnitario: item?.precioUnitario,
      monedaExtranjera: monedaValue,
      cambioControlValue: cambioControl?.value,
      cambioRawValue: cambioRawValue,
      tipoCambioComputed: this.tipoCambio(),
      tipoCambioValue: tipoCambioValue,
      simboloMoneda: this.simboloMoneda()
    });

    const dialogRef = this.dialog.open(FacturaItemDialogComponent, {
      width: '600px',
      data: {
        item: item, // Siempre en guaraníes
        productos: this.productos(),
        monedaExtranjera: monedaValue,
        tipoCambio: tipoCambioValue, // Siempre un número (1 si no hay moneda extranjera) - usando getRawValue()
        simboloMoneda: this.simboloMoneda()
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((result?: FacturaLegalItem) => {
      if (!result) {
        return;
      }

      // El diálogo siempre devuelve valores en guaraníes - no se necesita conversión
      if (typeof index === 'number') {
        this.actualizarItem(index, result);
      } else {
        this.items.push(this.crearItemFormGroup(result));
      }

      this.actualizarItemsData();
    });
  }

  /**
   * Actualiza un item en el formulario.
   * IMPORTANTE: El item recibido ya está en guaraníes (viene del diálogo convertido).
   */
  private actualizarItem(index: number, item: FacturaLegalItem): void {
    const group = this.items.at(index) as FormGroup;
    if (!group) {
      return;
    }

    // El item ya está en guaraníes - no se necesita conversión
    group.patchValue({
      id: item.id,
      productoId: item.productoId,
      cantidad: item.cantidad,
      descripcion: item.descripcion,
      precioUnitario: item.precioUnitario
    }, { emitEvent: false });

    group.get('total')?.setValue(item.total ?? item.cantidad * item.precioUnitario, { emitEvent: false });
  }

  /**
   * Actualiza itemsData() con los valores del formulario.
   * IMPORTANTE: Los valores del formulario SIEMPRE están en guaraníes.
   * No se realiza ninguna conversión aquí - solo se leen los valores.
   */
  private actualizarItemsData(): void {
    const productos = this.productos();

    const itemsView = this.items.controls.map((control) => {
      const value = control.getRawValue() as FacturaLegalItem;
      const producto = productos.find(p => p.id === value.productoId);
      const cantidad = Number(value.cantidad) || 0;
      const precioUnitario = Number(value.precioUnitario) || 0;
      const descripcion = value.descripcion || producto?.descripcion || '';
      const total = Number(value.total ?? cantidad * precioUnitario) || 0;

      // Los valores ya están en guaraníes - no se necesita conversión
      return {
        id: value.id,
        productoId: value.productoId,
        descripcion,
        cantidad,
        precioUnitario,
        iva: producto?.iva ?? 0,
        total
      };
    });

    this.itemsData.set(itemsView);
  }

  calcularTotales(itemsView: FacturaItemView[], descuentoFinal: number) {
    let totalParcial0 = 0;
    let totalParcial5 = 0;
    let totalParcial10 = 0;
    let ivaParcial5 = 0;
    let ivaParcial10 = 0;

    // Calcular totales por tasa de IVA
    // Nota: El total del item ya incluye el IVA, por lo que:
    // - Para IVA 5%: el IVA es total / 21
    // - Para IVA 10%: el IVA es total / 11
    itemsView.forEach(item => {
      if (item.iva === 0) {
        totalParcial0 += item.total;
      } else if (item.iva === 5) {
        // IVA 5%: iva = total / 21
        ivaParcial5 += item.total / 21;
        totalParcial5 += item.total;
      } else if (item.iva === 10) {
        // IVA 10%: iva = total / 11
        ivaParcial10 += item.total / 11;
        totalParcial10 += item.total;
      } else {
        totalParcial0 += item.total;
      }
    });

    // Redondear IVA a 2 decimales
    ivaParcial5 = Math.round(ivaParcial5 * 100) / 100;
    ivaParcial10 = Math.round(ivaParcial10 * 100) / 100;
    const ivaParcial0 = 0;
    const ivaTotal = ivaParcial5 + ivaParcial10;

    const totalParcial = totalParcial0 + totalParcial5 + totalParcial10;
    const totalFinal = Math.max(0, totalParcial - descuentoFinal);

    return {
      ivaParcial0,
      ivaParcial5,
      ivaParcial10,
      totalParcial0,
      totalParcial5,
      totalParcial10,
      totalParcial,
      ivaTotal,
      totalFinal
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

    // IMPORTANTE: Los valores del formulario SIEMPRE están en guaraníes.
    // No se necesita conversión - solo actualizar itemsData y calcular totales.
    this.actualizarItemsData();

    // Calcular totales en guaraníes para enviar al backend
    const descuentoFinalGs = this.descuentoFinalSignal();
    const itemsDataGs = this.itemsData(); // Ya están en guaraníes

    // Calcular totales en guaraníes
    const totalesGs = this.calcularTotales(itemsDataGs, descuentoFinalGs);

    const { ivaTotal: _ivaTotal, ...totalesParaEnviar } = totalesGs;

    // Obtener timbradoDetalleId correctamente incluso si el control está deshabilitado
    const timbradoControl = this.form.get('timbradoDetalleId');
    const timbradoDetalleId = timbradoControl?.disabled
      ? timbradoControl.getRawValue()
      : this.form.value.timbradoDetalleId;

    const facturaData: Partial<FacturaLegal> = {
      empresaId: this.form.value.empresaId,
      timbradoDetalleId: timbradoDetalleId,
      clienteId: this.form.value.clienteId,
      fecha: this.formatDateForBackend(this.form.value.fecha),
      credito: this.form.value.credito,
      nombre: this.form.value.nombre,
      ruc: this.form.value.ruc,
      direccion: this.form.value.direccion,
      monedaExtranjera: this.form.value.monedaExtranjera || 'PYG',
      cambio: this.form.value.monedaExtranjera !== 'PYG'
        ? (this.form.get('cambio')?.getRawValue() || null)
        : null,
      // Los items ya están en guaraníes - no se necesita conversión
      items: itemsDataGs.map(item => ({
        id: item.id,
        productoId: item.productoId,
        cantidad: item.cantidad,
        descripcion: item.descripcion,
        precioUnitario: item.precioUnitario,
        total: item.total
      })),
      descuentoFinal: descuentoFinalGs,
      ...totalesParaEnviar
    };

    // DEBUG: Imprimir datos que se enviarían al servidor
    console.log('=== DATOS A ENVIAR AL SERVIDOR ===');
    console.log('Operación:', this.isEdit ? 'ACTUALIZAR' : 'CREAR');
    if (this.isEdit) {
      console.log('Factura ID:', this.facturaId);
    }
    console.log('Moneda:', facturaData.monedaExtranjera || 'PYG');
    console.log('Tipo de Cambio:', facturaData.cambio || 'N/A');
    console.log('\n--- TOTALES (en Guaraníes) ---');
    console.log('Total Parcial:', facturaData.totalParcial);
    console.log('Total Parcial 0%:', facturaData.totalParcial0);
    console.log('Total Parcial 5%:', facturaData.totalParcial5);
    console.log('Total Parcial 10%:', facturaData.totalParcial10);
    console.log('IVA Parcial 0%:', facturaData.ivaParcial0);
    console.log('IVA Parcial 5%:', facturaData.ivaParcial5);
    console.log('IVA Parcial 10%:', facturaData.ivaParcial10);
    console.log('Descuento Final:', facturaData.descuentoFinal);
    console.log('Total Final:', facturaData.totalFinal);
    console.log('\n--- ITEMS (en Guaraníes) ---');
    if (facturaData.items && facturaData.items.length > 0) {
      facturaData.items.forEach((item, index) => {
        console.log(`Item ${index + 1}:`, {
          id: item.id,
          productoId: item.productoId,
          descripcion: item.descripcion,
          cantidad: item.cantidad,
          precioUnitario: `${item.precioUnitario} Gs`,
          total: `${item.total} Gs`
        });
      });
    } else {
      console.log('No hay items');
    }
    console.log('\n--- DATOS COMPLETOS (JSON) ---');
    console.log(JSON.stringify(facturaData, null, 2));
    console.log('================================\n');

    // Guardar factura
    const request = this.isEdit && this.facturaId
      ? this.facturaApi.update(this.facturaId, facturaData)
      : this.facturaApi.create(facturaData);

    request.subscribe({
      next: (factura) => {
        this.saving.set(false);
        this.snackBar.open(
          `Factura ${this.isEdit ? 'actualizada' : 'creada'} correctamente`,
          'Cerrar',
          { duration: 3000 }
        );

        // Si es una nueva factura (no edición), ofrecer crear DE
        if (!this.isEdit && factura.id) {
          this.preguntarCrearDE(factura.id);
        } else {
          // Si es edición, navegar de vuelta
          this.navegarAtras();
        }
      },
      error: (error) => {
        this.saving.set(false);
        console.error('=== ERROR AL GUARDAR FACTURA ===');
        console.error('Status:', error.status);
        console.error('Error completo:', error);
        console.error('Error body:', error.error);
        if (error.error?.errors) {
          console.error('Errores de validación:', error.error.errors);
        }
        if (error.error?.message) {
          console.error('Mensaje de error:', error.error.message);
        }
        console.error('================================');

        this.snackBar.open(
          error.error?.message || `Error al ${this.isEdit ? 'actualizar' : 'crear'} factura`,
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/facturacion']);
  }

  private navegarAtras(): void {
    const empresaId = this.form.get('empresaId')?.value;
    const queryParams: any = { refresh: 'true' };
    if (empresaId) {
      queryParams.empresaId = empresaId;
    }
    this.router.navigate(['/facturacion'], { queryParams });
  }

  private preguntarCrearDE(facturaId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Crear Documento Electrónico',
        message: '¿Desea crear el documento electrónico (DE) para esta factura?',
        confirmText: 'Sí, crear DE',
        cancelText: 'No, más tarde'
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed) {
        this.crearDE(facturaId);
      } else {
        this.navegarAtras();
      }
    });
  }

  private crearDE(facturaId: number): void {
    this.saving.set(true);
    this.snackBar.open('Creando documento electrónico...', 'Cerrar', { duration: 2000 });

    this.facturaApi.generarDE(facturaId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        this.saving.set(false);
        this.snackBar.open('Documento electrónico creado exitosamente', 'Cerrar', { duration: 3000 });

        // Si se creó un lote, preguntar si quiere consultar SIFEN
        if (response.lote?.id) {
          this.preguntarConsultarSifen(response.lote.id);
        } else {
          this.navegarAtras();
        }
      },
      error: (error) => {
        this.saving.set(false);
        this.snackBar.open(
          error.error?.message || 'Error al crear el documento electrónico',
          'Cerrar',
          { duration: 5000 }
        );
        this.navegarAtras();
      }
    });
  }

  private preguntarConsultarSifen(loteId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Consultar Estado en SIFEN',
        message: '¿Desea consultar el estado del documento electrónico en SIFEN ahora?',
        confirmText: 'Sí, consultar',
        cancelText: 'No, más tarde'
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed) {
        this.consultarSifen(loteId);
      } else {
        this.navegarAtras();
      }
    });
  }

  private consultarSifen(loteId: number): void {
    this.saving.set(true);
    this.snackBar.open('Consultando estado en SIFEN...', 'Cerrar', { duration: 2000 });

    this.sifenApi.consultarLote(loteId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (lote) => {
        this.saving.set(false);
        this.snackBar.open(
          `Consulta completada. Estado del lote: ${lote.estado}`,
          'Cerrar',
          { duration: 4000 }
        );
        this.navegarAtras();
      },
      error: (error) => {
        this.saving.set(false);
        this.snackBar.open(
          error.error?.message || 'Error al consultar el estado en SIFEN',
          'Cerrar',
          { duration: 5000 }
        );
        this.navegarAtras();
      }
    });
  }

  private formatDateForBackend(date: Date): string {
    if (!(date instanceof Date) || Number.isNaN(date.getTime())) {
      return new Date().toISOString().slice(0, 19);
    }

    const pad = (value: number) => value.toString().padStart(2, '0');
    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());
    const seconds = pad(date.getSeconds());

    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }
}
