import { Component, Inject, OnInit, signal, computed, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDialog } from '@angular/material/dialog';
import { Subject, combineLatest, takeUntil } from 'rxjs';
import { FacturaItemDialogComponent } from '../facturacion/factura-item-dialog.component';
import { NotaCreditoApiService } from '../../core/api/nota-credito-api.service';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { ProductoApiService } from '../../core/api/producto-api.service';
import { FacturaApiService } from '../../core/api/factura-api.service';
import { SifenApiService } from '../../core/api/sifen-api.service';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { NotaCredito, NotaItem } from '../../models/nota.model';
import { TimbradoDetalle } from '../../models/timbrado.model';
import { Cliente } from '../../models/cliente.model';
import { Producto } from '../../models/producto.model';
import { FacturaLegal } from '../../models/factura.model';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-nota-credito-form-dialog',
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
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatDividerModule,
    MatExpansionModule,
    ErrorMessageComponent
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>{{ data.nota ? 'edit' : 'add_circle' }}</mat-icon>
      {{ data.nota ? 'Editar' : 'Nueva' }} Nota de Crédito
    </h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <!-- Sección: Información General -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>info</mat-icon>
              Información General
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-grid">
              <mat-form-field appearance="outline" class="field-half">
                <mat-label>Timbrado Detalle</mat-label>
                <mat-select formControlName="timbradoDetalleId" [disabled]="data.facturaLegalId != null">
                  <mat-option *ngFor="let td of timbradosDetalle()" [value]="td.id">
                    {{ getTimbradoDisplay(td) }}
                  </mat-option>
                </mat-select>
                <mat-icon matPrefix>stamp</mat-icon>
                <app-error-message [control]="form.get('timbradoDetalleId')" />
              </mat-form-field>

              <mat-form-field appearance="outline" class="field-half">
                <mat-label>Fecha</mat-label>
                <input matInput [matDatepicker]="picker" formControlName="fecha">
                <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
                <mat-datepicker #picker></mat-datepicker>
                <mat-icon matPrefix>calendar_today</mat-icon>
                <app-error-message [control]="form.get('fecha')" />
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Sección: Factura y Cliente -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>receipt</mat-icon>
              Factura Asociada y Cliente
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-grid">
              <!-- Factura Asociada - Card Expandible -->
              <div class="field-full" *ngIf="form.get('facturaLegalId')?.value">
                <mat-expansion-panel [expanded]="true" class="factura-card">
                  <mat-expansion-panel-header>
                    <mat-panel-title>
                      <mat-icon>receipt_long</mat-icon>
                      <span>Factura Asociada: {{ getFacturaAsociada()?.numeroFactura ? getFacturaAsociada()!.numeroFactura!.toString().padStart(7, '0') : 'N/A' }}</span>
                    </mat-panel-title>
                  </mat-expansion-panel-header>
                  <div class="factura-info" *ngIf="getFacturaAsociada()">
                    <div class="info-grid">
                      <div class="info-item">
                        <mat-icon>description</mat-icon>
                        <div>
                          <span class="info-label">Número:</span>
                          <span class="info-value">{{ getFacturaAsociada()!.numeroFactura ? getFacturaAsociada()!.numeroFactura!.toString().padStart(7, '0') : 'N/A' }}</span>
                        </div>
                      </div>
                      <div class="info-item">
                        <mat-icon>calendar_today</mat-icon>
                        <div>
                          <span class="info-label">Fecha:</span>
                          <span class="info-value">{{ formatFecha(getFacturaAsociada()!.fecha) }}</span>
                        </div>
                      </div>
                      <div class="info-item">
                        <mat-icon>person</mat-icon>
                        <div>
                          <span class="info-label">Cliente:</span>
                          <span class="info-value">{{ getFacturaAsociada()!.nombre || '-' }}</span>
                        </div>
                      </div>
                      <div class="info-item">
                        <mat-icon>badge</mat-icon>
                        <div>
                          <span class="info-label">RUC:</span>
                          <span class="info-value">{{ getFacturaAsociada()!.ruc || '-' }}</span>
                        </div>
                      </div>
                      <div class="info-item">
                        <mat-icon>attach_money</mat-icon>
                        <div>
                          <span class="info-label">Monto Total:</span>
                          <span class="info-value">₲ {{ getFacturaAsociada()!.totalFinal | number:'1.2-2' }}</span>
                        </div>
                      </div>
                      <div class="info-item">
                        <mat-icon>shopping_cart</mat-icon>
                        <div>
                          <span class="info-label">Items:</span>
                          <span class="info-value">{{ getFacturaAsociada()!.items.length || 0 }}</span>
                        </div>
                      </div>
                    </div>
                    <!-- Items de la factura -->
                    <div class="factura-items" *ngIf="getFacturaAsociada()!.items && getFacturaAsociada()!.items.length > 0">
                      <h4>Items de la Factura:</h4>
                      <table class="items-table">
                        <thead>
                          <tr>
                            <th>#</th>
                            <th>Descripción</th>
                            <th>Cantidad</th>
                            <th>Precio Unit.</th>
                            <th>Total</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr *ngFor="let item of getFacturaAsociada()!.items; let i = index">
                            <td>{{ i + 1 }}</td>
                            <td>{{ item.descripcion }}</td>
                            <td>{{ item.cantidad | number:'1.2-2' }}</td>
                            <td>₲ {{ item.precioUnitario | number:'1.2-2' }}</td>
                            <td>₲ {{ item.total | number:'1.2-2' }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </mat-expansion-panel>
              </div>

              <!-- Selector de factura (solo si no hay factura asociada predefinida) -->
              <mat-form-field appearance="outline" class="field-full" *ngIf="!data.facturaLegalId">
                <mat-label>Factura Asociada</mat-label>
                <mat-select formControlName="facturaLegalId" 
                            (selectionChange)="onFacturaSeleccionada($event.value)">
                  <mat-option [value]="null">Ninguna</mat-option>
                  <mat-option *ngFor="let f of facturas()" [value]="f.id">
                    {{ f.numeroFactura ? f.numeroFactura.toString().padStart(7, '0') : 'N/A' }} - {{ f.nombre }}
                  </mat-option>
                </mat-select>
                <mat-icon matPrefix>description</mat-icon>
                <app-error-message [control]="form.get('facturaLegalId')" />
              </mat-form-field>

              <mat-form-field appearance="outline" class="field-full">
                <mat-label>Cliente</mat-label>
                <mat-select formControlName="clienteId" 
                            (selectionChange)="onClienteSeleccionado($event.value)"
                            [disabled]="data.facturaLegalId != null">
                  <mat-option [value]="null">Ninguno</mat-option>
                  <mat-option *ngFor="let c of clientes()" [value]="c.id">
                    {{ c.nombre }} - {{ c.ruc }}
                  </mat-option>
                </mat-select>
                <mat-icon matPrefix>person</mat-icon>
              </mat-form-field>

              <!-- Información del Cliente (solo lectura) -->
              <div class="cliente-info" *ngIf="form.get('nombre')?.value || form.get('ruc')?.value">
                <div class="info-row">
                  <mat-icon>person_outline</mat-icon>
                  <span class="info-label">Nombre:</span>
                  <span class="info-value">{{ form.get('nombre')?.value || '-' }}</span>
                </div>
                <div class="info-row">
                  <mat-icon>badge</mat-icon>
                  <span class="info-label">RUC:</span>
                  <span class="info-value">{{ form.get('ruc')?.value || '-' }}</span>
                </div>
                <div class="info-row" *ngIf="form.get('direccion')?.value">
                  <mat-icon>location_on</mat-icon>
                  <span class="info-label">Dirección:</span>
                  <span class="info-value">{{ form.get('direccion')?.value }}</span>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Sección: Motivo -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>note</mat-icon>
              Motivo de Emisión
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-grid">
              <mat-form-field appearance="outline" class="field-half">
                <mat-label>Motivo de Emisión</mat-label>
                <input matInput formControlName="motivoEmision" placeholder="Ej: Devolución, Anulación, etc.">
                <mat-icon matPrefix>label</mat-icon>
                <app-error-message [control]="form.get('motivoEmision')" />
              </mat-form-field>

              <mat-form-field appearance="outline" class="field-full">
                <mat-label>Descripción del Motivo</mat-label>
                <textarea matInput formControlName="descripcionMotivo" rows="3" 
                          placeholder="Descripción detallada del motivo de emisión"></textarea>
                <mat-icon matPrefix>description</mat-icon>
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Sección: Items -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>shopping_cart</mat-icon>
              Items
            </mat-card-title>
            <div class="header-actions">
              <button mat-stroked-button type="button" (click)="agregarItem()" color="primary">
                <mat-icon>add</mat-icon>
                Agregar Item
              </button>
            </div>
          </mat-card-header>
          <mat-card-content>
            <div formArrayName="items">
              <div class="items-list" *ngIf="itemsArray.length > 0">
                <div class="item-row" *ngFor="let item of itemsArray.controls; let i = index">
                  <div class="item-info">
                    <div class="item-header">
                      <span class="item-number">{{ i + 1 }}</span>
                      <span class="item-descripcion">{{ getItemControl(i, 'descripcion').value || 'Sin descripción' }}</span>
                    </div>
                    <div class="item-details">
                      <span class="detail-item">
                        <strong>Cantidad:</strong> {{ getItemControl(i, 'cantidad').value | number:'1.2-2' }}
                      </span>
                      <span class="detail-item">
                        <strong>Precio Unit.:</strong> ₲ {{ getItemControl(i, 'precioUnitario').value | number:'1.2-2' }}
                      </span>
                      <span class="detail-item">
                        <strong>IVA:</strong> {{ getItemControl(i, 'iva').value }}%
                      </span>
                      <span class="detail-item" *ngIf="getItemControl(i, 'descuento').value > 0">
                        <strong>Descuento:</strong> ₲ {{ getItemControl(i, 'descuento').value | number:'1.2-2' }}
                      </span>
                      <span class="detail-item total">
                        <strong>Total:</strong> ₲ {{ getItemTotal(i) | number:'1.2-2' }}
                      </span>
                    </div>
                  </div>
                  <div class="item-actions">
                    <button mat-icon-button (click)="editarItem(i)" color="primary" matTooltip="Editar item">
                      <mat-icon>edit</mat-icon>
                    </button>
                    <button mat-icon-button (click)="eliminarItem(i)" color="warn" matTooltip="Eliminar item">
                      <mat-icon>delete</mat-icon>
                    </button>
                  </div>
                </div>
              </div>
              <div class="empty-state" *ngIf="itemsArray.length === 0">
                <mat-icon>inbox</mat-icon>
                <p>No hay items agregados. Haz clic en "Agregar Item" para comenzar.</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Sección: Totales -->
        <mat-card class="section-card totales-card">
          <mat-card-content>
            <div class="totales-container">
              <div class="total-row final">
                <span class="total-label">
                  <mat-icon>attach_money</mat-icon>
                  Total Final:
                </span>
                <span class="total-value">₲ {{ totalFinal() | number:'1.2-2' }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">
        <mat-icon>close</mat-icon>
        Cancelar
      </button>
      <button mat-raised-button color="primary" (click)="onSave()" [disabled]="form.invalid">
        <mat-icon>save</mat-icon>
        Guardar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    :host {
      display: block;
    }

    h2 mat-dialog-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    mat-dialog-content {
      min-width: 900px;
      max-width: 1200px;
      max-height: calc(85vh - 100px);
      overflow-y: auto;
      padding: 24px;
      overflow-y: auto;
      padding: 0 !important;
    }

    form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .section-card {
      margin-bottom: 0;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .section-card mat-card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px;
      border-bottom: 1px solid rgba(0,0,0,0.12);
      background-color: rgba(0,0,0,0.02);
    }

    .section-card mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      margin: 0;
    }

    .section-card mat-card-title mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .section-card mat-card-content {
      padding: 20px;
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
    }

    .field-full {
      grid-column: 1 / -1;
    }

    .field-half {
      grid-column: span 1;
    }

    mat-form-field {
      width: 100%;
    }

    mat-form-field mat-icon[matPrefix] {
      margin-right: 8px;
      color: rgba(0,0,0,0.54);
    }

    .cliente-info {
      grid-column: 1 / -1;
      background-color: rgba(0,0,0,0.02);
      border-radius: 4px;
      padding: 16px;
      margin-top: 8px;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .info-row {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .info-row mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: rgba(0,0,0,0.54);
    }

    .info-label {
      font-weight: 500;
      min-width: 80px;
      color: rgba(0,0,0,0.7);
    }

    .info-value {
      color: rgba(0,0,0,0.87);
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }

    .items-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 16px;
    }

    .item-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border: 1px solid rgba(0,0,0,0.12);
      border-radius: 4px;
      background-color: #fafafa;
      transition: background-color 0.2s;
    }

    .item-row:hover {
      background-color: #f5f5f5;
    }

    .item-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .item-header {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .item-number {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background-color: #1976d2;
      color: white;
      font-weight: 600;
      font-size: 14px;
    }

    .item-descripcion {
      font-weight: 500;
      font-size: 16px;
      color: rgba(0,0,0,0.87);
    }

    .item-details {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      margin-left: 40px;
      font-size: 14px;
    }

    .detail-item {
      color: rgba(0,0,0,0.6);
    }

    .detail-item.total {
      font-weight: 600;
      color: #1976d2;
      font-size: 16px;
    }

    .item-actions {
      display: flex;
      gap: 8px;
    }

    .factura-card {
      margin-bottom: 16px;
    }

    .factura-card mat-panel-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .factura-info {
      padding: 16px 0;
    }

    .factura-info .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
      margin-bottom: 16px;
    }

    .factura-info .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }

    .factura-info .info-item mat-icon {
      color: #1976d2;
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .factura-info .info-item .info-label {
      display: block;
      font-size: 12px;
      color: rgba(0,0,0,0.6);
      margin-bottom: 4px;
    }

    .factura-info .info-item .info-value {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: rgba(0,0,0,0.87);
    }

    .factura-items {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid rgba(0,0,0,0.12);
    }

    .factura-items h4 {
      margin: 0 0 12px 0;
      font-size: 16px;
      font-weight: 500;
      color: rgba(0,0,0,0.87);
    }

    .factura-items .items-table {
      width: 100%;
      border-collapse: collapse;
    }

    .factura-items .items-table th,
    .factura-items .items-table td {
      padding: 8px 12px;
      text-align: left;
      border-bottom: 1px solid rgba(0,0,0,0.12);
    }

    .factura-items .items-table th {
      background-color: #f5f5f5;
      font-weight: 500;
      font-size: 12px;
      color: rgba(0,0,0,0.6);
    }

    .factura-items .items-table td {
      font-size: 14px;
    }

    .factura-items .items-table tbody tr:hover {
      background-color: #fafafa;
    }

    .total-cell {
      text-align: right;
      font-weight: 500;
    }

    .actions-cell {
      text-align: center;
    }

    .empty-state {
      text-align: center;
      padding: 40px 20px;
      color: rgba(0,0,0,0.54);
    }

    .empty-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      opacity: 0.5;
    }

    .empty-state p {
      margin: 0;
    }

    .totales-card {
      background-color: rgba(25, 118, 210, 0.05);
    }

    .totales-container {
      padding: 8px 0;
    }

    .total-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
    }

    .total-row.final {
      border-top: 2px solid rgba(25, 118, 210, 0.3);
      padding-top: 16px;
      margin-top: 8px;
    }

    .total-label {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 500;
      color: rgba(0,0,0,0.87);
    }

    .total-label mat-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .total-value {
      font-size: 24px;
      font-weight: 700;
      color: #1976d2;
    }

    mat-dialog-actions {
      padding: 16px 24px;
      border-top: 1px solid rgba(0,0,0,0.12);
    }

    mat-dialog-actions button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    @media (max-width: 960px) {
      .form-grid {
        grid-template-columns: 1fr;
      }

      .field-half {
        grid-column: 1;
      }
    }
  `]
})
export class NotaCreditoFormDialogComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  form: FormGroup;
  timbradosDetalle = signal<TimbradoDetalle[]>([]);
  clientes = signal<Cliente[]>([]);
  productos = signal<Producto[]>([]);
  facturas = signal<FacturaLegal[]>([]);
  facturaAsociada = signal<FacturaLegal | null>(null);


  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<NotaCreditoFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { nota?: NotaCredito; empresaId?: number; facturaLegalId?: number },
    private notaCreditoApi: NotaCreditoApiService,
    private timbradoApi: TimbradoApiService,
    private clienteApi: ClienteApiService,
    private productoApi: ProductoApiService,
    private facturaApi: FacturaApiService,
    private sifenApi: SifenApiService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.form = this.fb.group({
      empresaId: [data.empresaId, Validators.required],
      timbradoDetalleId: [null, Validators.required],
      clienteId: [null],
      facturaLegalId: [null, Validators.required],
      fecha: [new Date(), Validators.required],
      motivoEmision: ['', Validators.required],
      descripcionMotivo: [''],
      nombre: [''],
      ruc: [''],
      direccion: [''],
      items: this.fb.array([], [Validators.required, Validators.minLength(1)])
    });
  }

  ngOnInit(): void {
    if (this.data.nota) {
      if (this.data.empresaId) {
        this.cargarDatos();
      }
      this.cargarNota();
    } else {
      // Si se proporciona facturaLegalId, cargar datos y factura en paralelo
      if (this.data.facturaLegalId && this.data.empresaId) {
        // Usar combineLatest para esperar a que todos los datos estén cargados
        combineLatest([
          this.timbradoApi.getDetallesByEmpresa(this.data.empresaId),
          this.clienteApi.getByEmpresa(this.data.empresaId),
          this.productoApi.getAll(this.data.empresaId),
          this.facturaApi.getAll({ empresaId: this.data.empresaId }),
          this.facturaApi.getById(this.data.facturaLegalId)
        ]).pipe(takeUntil(this.destroy$))
          .subscribe(([timbrados, clientes, productos, facturas, factura]) => {
            // Establecer los datos cargados
            this.timbradosDetalle.set(timbrados);
            this.clientes.set(clientes);
            this.productos.set(productos);
            this.facturas.set(facturas);
            
            if (factura) {
              // Guardar factura asociada completa
              this.facturaAsociada.set(factura);
              
              // Establecer factura asociada
              this.form.patchValue({ facturaLegalId: factura.id! });
              
              // Establecer timbrado detalle desde la factura
              if (factura.timbradoDetalleId) {
                this.form.patchValue({ timbradoDetalleId: factura.timbradoDetalleId });
              }
              
              // Establecer cliente y datos del cliente desde la factura
              if (factura.clienteId) {
                this.form.patchValue({
                  clienteId: factura.clienteId,
                  nombre: factura.nombre || '',
                  ruc: factura.ruc || '',
                  direccion: factura.direccion || ''
                });
              } else if (factura.nombre) {
                // Si no hay clienteId pero hay nombre (cliente sin nombre), establecer solo los datos
                this.form.patchValue({
                  nombre: factura.nombre || '',
                  ruc: factura.ruc || '',
                  direccion: factura.direccion || ''
                });
              }
            }
          });
      } else if (this.data.empresaId) {
        // Si no hay facturaLegalId pero hay empresaId, cargar datos normalmente
        this.cargarDatos();
      }
      this.agregarItem();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarDatos(): void {
    if (!this.data.empresaId) return;

    // Cargar timbrados
    this.timbradoApi.getDetallesByEmpresa(this.data.empresaId)
      .pipe(takeUntil(this.destroy$))
      .subscribe((t: TimbradoDetalle[]) => this.timbradosDetalle.set(t));

    // Cargar clientes
    this.clienteApi.getByEmpresa(this.data.empresaId)
      .pipe(takeUntil(this.destroy$))
      .subscribe((c: Cliente[]) => this.clientes.set(c));

    // Cargar productos
    this.productoApi.getAll(this.data.empresaId)
      .pipe(takeUntil(this.destroy$))
      .subscribe((p: Producto[]) => this.productos.set(p));

    // Cargar facturas
    this.facturaApi.getAll({ empresaId: this.data.empresaId })
      .pipe(takeUntil(this.destroy$))
      .subscribe((f: FacturaLegal[]) => this.facturas.set(f));
  }

  cargarNota(): void {
    if (!this.data.nota) return;
    const nota = this.data.nota;

    this.form.patchValue({
      empresaId: nota.empresaId,
      timbradoDetalleId: nota.timbradoDetalleId,
      clienteId: nota.clienteId,
      facturaLegalId: nota.facturaLegalId,
      fecha: new Date(nota.fecha),
      motivoEmision: nota.motivoEmision,
      descripcionMotivo: nota.descripcionMotivo,
      nombre: nota.nombre || '',
      ruc: nota.ruc || '',
      direccion: nota.direccion || ''
    });

    const itemsArray = this.form.get('items') as FormArray;
    itemsArray.clear();
    nota.items.forEach(item => {
      itemsArray.push(this.crearItemFormGroup(item));
    });
  }

  onFacturaSeleccionada(facturaId: number | null): void {
    if (!facturaId) {
      this.facturaAsociada.set(null);
      return;
    }
    
    // Cargar factura completa
    this.facturaApi.getById(facturaId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (factura) => {
        this.facturaAsociada.set(factura);
        if (factura && factura.clienteId) {
          this.form.patchValue({
            clienteId: factura.clienteId,
            nombre: factura.nombre || '',
            ruc: factura.ruc || '',
            direccion: factura.direccion || ''
          });
        }
      },
      error: (err) => {
        console.error('Error al cargar factura:', err);
        this.snackBar.open('Error al cargar la factura', 'Cerrar', { duration: 3000 });
      }
    });
  }

  getFacturaAsociada(): FacturaLegal | null {
    return this.facturaAsociada();
  }

  getTimbradoDisplay(td: TimbradoDetalle): string {
    const numero = td.timbradoNumero || 'N/A';
    return `${numero}-${td.codigoEstablecimientoFactura}-${td.puntoExpedicion}`;
  }

  formatFecha(fecha: string): string {
    if (!fecha) return '-';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-PY', { year: 'numeric', month: '2-digit', day: '2-digit' });
  }

  onClienteSeleccionado(clienteId: number | null): void {
    if (!clienteId) {
      this.form.patchValue({ nombre: '', ruc: '', direccion: '' });
      return;
    }
    const cliente = this.clientes().find(c => c.id === clienteId);
    if (cliente) {
      this.form.patchValue({
        nombre: cliente.nombre || '',
        ruc: cliente.ruc || '',
        direccion: cliente.direccion || ''
      });
    }
  }

  get itemsArray(): FormArray {
    return this.form.get('items') as FormArray;
  }

  agregarItem(): void {
    this.abrirDialogoItem();
  }

  editarItem(index: number): void {
    const itemGroup = this.itemsArray.at(index);
    const itemData: NotaItem = {
      id: itemGroup.get('id')?.value,
      productoId: itemGroup.get('productoId')?.value,
      descripcion: itemGroup.get('descripcion')?.value,
      cantidad: itemGroup.get('cantidad')?.value,
      precioUnitario: itemGroup.get('precioUnitario')?.value,
      descuento: itemGroup.get('descuento')?.value || 0,
      iva: itemGroup.get('iva')?.value || 10,
      total: itemGroup.get('total')?.value || 0
    };
    this.abrirDialogoItem(itemData, index);
  }

  private abrirDialogoItem(item?: NotaItem, index?: number): void {
    const monedaValue = this.form.get('monedaExtranjera')?.value || 'PYG';
    const tipoCambioValue = this.form.get('cambio')?.value || 1;
    const simboloMoneda = monedaValue === 'PYG' ? '₲' : (monedaValue === 'USD' ? '$' : monedaValue);

    const dialogRef = this.dialog.open(FacturaItemDialogComponent, {
      width: '600px',
      data: {
        item: item,
        productos: this.productos(),
        monedaExtranjera: monedaValue,
        tipoCambio: tipoCambioValue,
        simboloMoneda: simboloMoneda,
        showIva: true,
        showDescuento: true
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((result?: NotaItem) => {
      if (!result) {
        return;
      }

      if (typeof index === 'number') {
        // Actualizar item existente
        const itemGroup = this.itemsArray.at(index);
        itemGroup.patchValue({
          productoId: result.productoId,
          descripcion: result.descripcion,
          cantidad: result.cantidad,
          precioUnitario: result.precioUnitario,
          descuento: result.descuento || 0,
          iva: result.iva || 10,
          total: result.total
        });
      } else {
        // Agregar nuevo item
        this.itemsArray.push(this.crearItemFormGroup(result));
      }

      this.recalcularTotales();
    });
  }

  crearItemFormGroup(item?: NotaItem): FormGroup {
    return this.fb.group({
      productoId: [item?.productoId || null],
      descripcion: [item?.descripcion || '', Validators.required],
      cantidad: [item?.cantidad || 1, [Validators.required, Validators.min(0.001)]],
      precioUnitario: [item?.precioUnitario || 0, [Validators.required, Validators.min(0.01)]],
      descuento: [item?.descuento || 0],
      iva: [item?.iva ?? 10, Validators.required],
      total: [item?.total || 0]
    });
  }

  eliminarItem(index: number): void {
    this.itemsArray.removeAt(index);
    this.recalcularTotales();
  }

  getItemControl(index: number, controlName: string): FormControl {
    return this.itemsArray.at(index).get(controlName) as FormControl;
  }

  calcularItem(index: number): void {
    const itemGroup = this.itemsArray.at(index);
    const cantidad = itemGroup.get('cantidad')?.value || 0;
    const precio = itemGroup.get('precioUnitario')?.value || 0;
    const descuento = itemGroup.get('descuento')?.value || 0;
    const total = (cantidad * precio) - descuento;
    itemGroup.patchValue({ total }, { emitEvent: false });
    this.recalcularTotales();
  }

  getItemTotal(index: number): number {
    return this.itemsArray.at(index).get('total')?.value || 0;
  }

  totalFinal = computed(() => {
    return this.itemsArray.controls.reduce((sum, control) => {
      return sum + (control.get('total')?.value || 0);
    }, 0);
  });

  recalcularTotales(): void {
    // Calcular totales por IVA
    let totalParcial0 = 0;
    let totalParcial5 = 0;
    let totalParcial10 = 0;
    let ivaParcial0 = 0;
    let ivaParcial5 = 0;
    let ivaParcial10 = 0;

    this.itemsArray.controls.forEach(control => {
      const cantidad = control.get('cantidad')?.value || 0;
      const precioUnitario = control.get('precioUnitario')?.value || 0;
      const descuento = control.get('descuento')?.value || 0;
      const iva = control.get('iva')?.value || 10;
      
      const subtotal = (cantidad * precioUnitario) - descuento;
      const totalItem = subtotal;
      
      // Calcular según IVA
      // Nota: El total del item ya incluye el IVA, por lo que:
      // - Para IVA 5%: el IVA es total / 21
      // - Para IVA 10%: el IVA es total / 11
      // - El total parcial es simplemente el total del item (no se resta el IVA)
      if (iva === 0) {
        totalParcial0 += totalItem;
      } else if (iva === 5) {
        // IVA 5%: iva = total / 21
        const ivaItem = totalItem / 21;
        ivaParcial5 += ivaItem;
        // El total parcial 5 es el total del item (ya incluye IVA)
        totalParcial5 += totalItem;
      } else if (iva === 10) {
        // IVA 10%: iva = total / 11
        const ivaItem = totalItem / 11;
        ivaParcial10 += ivaItem;
        // El total parcial 10 es el total del item (ya incluye IVA)
        totalParcial10 += totalItem;
      }
    });

    // Actualizar totales en el formulario (si existen campos para ellos)
    // Los totales se calculan en el backend, pero podemos pre-calcularlos aquí
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.form.invalid) return;

    const formValue = this.form.value;
    
    // Calcular totales por IVA
    let totalParcial0 = 0;
    let totalParcial5 = 0;
    let totalParcial10 = 0;
    let ivaParcial0 = 0;
    let ivaParcial5 = 0;
    let ivaParcial10 = 0;
    let totalParcial = 0;

    formValue.items.forEach((item: any) => {
      const cantidad = item.cantidad || 0;
      const precioUnitario = item.precioUnitario || 0;
      const descuento = item.descuento || 0;
      const iva = item.iva || 10;
      
      // El total del item ya incluye el IVA (cantidad × precio unitario - descuento)
      const totalItem = (cantidad * precioUnitario) - descuento;
      
      // Calcular según IVA
      // Nota: El total del item ya incluye el IVA, por lo que:
      // - Para IVA 5%: el IVA es total / 21
      // - Para IVA 10%: el IVA es total / 11
      // - El total parcial es simplemente el total del item (no se resta el IVA)
      if (iva === 0) {
        totalParcial0 += totalItem;
      } else if (iva === 5) {
        // IVA 5%: iva = total / 21
        const ivaItem = totalItem / 21;
        ivaParcial5 += ivaItem;
        // El total parcial 5 es el total del item (ya incluye IVA)
        totalParcial5 += totalItem;
      } else if (iva === 10) {
        // IVA 10%: iva = total / 11
        const ivaItem = totalItem / 11;
        ivaParcial10 += ivaItem;
        // El total parcial 10 es el total del item (ya incluye IVA)
        totalParcial10 += totalItem;
      }
    });

    totalParcial = totalParcial0 + totalParcial5 + totalParcial10;
    const descuentoFinal = formValue.descuentoFinal || 0;
    const totalFinal = totalParcial - descuentoFinal;

    const nota: NotaCredito = {
      ...formValue,
      fecha: formValue.fecha.toISOString(),
      nombre: formValue.nombre || '',
      ruc: formValue.ruc || '',
      direccion: formValue.direccion || '',
      items: formValue.items.map((item: any) => ({
        ...item,
        total: (item.cantidad * item.precioUnitario) - (item.descuento || 0)
      })),
      totalFinal: totalFinal,
      ivaParcial0: ivaParcial0,
      ivaParcial5: ivaParcial5,
      ivaParcial10: ivaParcial10,
      totalParcial0: totalParcial0,
      totalParcial5: totalParcial5,
      totalParcial10: totalParcial10,
      totalParcial: totalParcial,
      descuentoFinal: descuentoFinal
    };

    if (this.data.nota?.id) {
      nota.id = this.data.nota.id;
    }

    // Guardar la nota
    this.notaCreditoApi.create(nota)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (notaCreada) => {
          // Si es una nueva nota (no edición), ofrecer crear DE
          if (!this.data.nota?.id && notaCreada.id) {
            this.preguntarCrearDE(notaCreada.id);
          } else {
            this.snackBar.open('Nota de crédito guardada', 'Cerrar', { duration: 3000 });
            this.dialogRef.close(true);
          }
        },
        error: (error) => {
          console.error('Error al guardar nota de crédito:', error);
          this.snackBar.open(
            error.error?.message || 'Error al guardar nota de crédito',
            'Cerrar',
            { duration: 5000 }
          );
        }
      });
  }

  private preguntarCrearDE(notaCreditoId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Crear Documento Electrónico',
        message: '¿Desea crear el documento electrónico (DE) para esta nota de crédito?',
        confirmText: 'Sí, crear DE',
        cancelText: 'No, más tarde'
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed) {
        this.crearDE(notaCreditoId);
      } else {
        this.snackBar.open('Nota de crédito guardada', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      }
    });
  }

  private crearDE(notaCreditoId: number): void {
    this.snackBar.open('Creando documento electrónico...', 'Cerrar', { duration: 2000 });

    this.notaCreditoApi.generarDE(notaCreditoId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response) => {
        this.snackBar.open('Documento electrónico creado exitosamente', 'Cerrar', { duration: 3000 });

        // Si se creó un lote, preguntar si quiere consultar SIFEN
        if (response.lote?.id) {
          this.preguntarConsultarSifen(response.lote.id);
        } else {
          this.dialogRef.close(true);
        }
      },
      error: (error) => {
        console.error('Error al crear DE:', error);
        this.snackBar.open(
          error.error?.message || 'Error al crear el documento electrónico',
          'Cerrar',
          { duration: 5000 }
        );
        this.dialogRef.close(true);
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
        this.dialogRef.close(true);
      }
    });
  }

  private consultarSifen(loteId: number): void {
    this.snackBar.open('Consultando estado en SIFEN...', 'Cerrar', { duration: 2000 });

    this.sifenApi.consultarLote(loteId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (lote: any) => {
        this.snackBar.open(
          `Consulta completada. Estado del lote: ${lote.estado}`,
          'Cerrar',
          { duration: 4000 }
        );
        this.dialogRef.close(true);
      },
      error: (error: any) => {
        console.error('Error al consultar SIFEN:', error);
        this.snackBar.open(
          error.error?.message || 'Error al consultar el estado en SIFEN',
          'Cerrar',
          { duration: 5000 }
        );
        this.dialogRef.close(true);
      }
    });
  }
}

