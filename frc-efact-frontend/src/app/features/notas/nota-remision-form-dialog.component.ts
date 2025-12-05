import { Component, Inject, OnInit, signal, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { NotaRemisionApiService } from '../../core/api/nota-remision-api.service';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { ProductoApiService } from '../../core/api/producto-api.service';
import { FacturaApiService } from '../../core/api/factura-api.service';
import { NotaRemision, NotaRemisionItem } from '../../models/nota.model';
import { TimbradoDetalle } from '../../models/timbrado.model';
import { Cliente } from '../../models/cliente.model';
import { Producto } from '../../models/producto.model';
import { FacturaLegal } from '../../models/factura.model';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-nota-remision-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatSnackBarModule,
    ErrorMessageComponent
  ],
  template: `
    <h2 mat-dialog-title>{{ data.nota ? 'Editar' : 'Nueva' }} Nota de Remisión</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Timbrado Detalle</mat-label>
            <mat-select formControlName="timbradoDetalleId">
              <mat-option *ngFor="let td of timbradosDetalle()" [value]="td.id">
                {{ td.codigoEstablecimientoFactura }} - {{ td.puntoExpedicion }}
              </mat-option>
            </mat-select>
            <app-error-message [control]="form.get('timbradoDetalleId')" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Fecha</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="fecha">
            <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
            <app-error-message [control]="form.get('fecha')" />
          </mat-form-field>
        </div>

        <h3>Datos de Salida</h3>
        <div class="form-row">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Dirección de Partida</mat-label>
            <input matInput formControlName="direccionPartida">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Ciudad</mat-label>
            <input matInput formControlName="ciudadPartida">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Departamento</mat-label>
            <input matInput formControlName="departamentoPartida">
          </mat-form-field>
        </div>

        <h3>Datos de Llegada</h3>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Cliente Destinatario</mat-label>
            <mat-select formControlName="clienteId">
              <mat-option [value]="null">Ninguno</mat-option>
              <mat-option *ngFor="let c of clientes()" [value]="c.id">
                {{ c.nombre }} - {{ c.ruc }}
              </mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Nombre Destinatario</mat-label>
            <input matInput formControlName="nombreDestinatario">
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>RUC Destinatario</mat-label>
            <input matInput formControlName="rucDestinatario">
          </mat-form-field>
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Dirección Destinatario</mat-label>
            <input matInput formControlName="direccionDestinatario">
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Ciudad Destinatario</mat-label>
            <input matInput formControlName="ciudadDestinatario">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Departamento Destinatario</mat-label>
            <input matInput formControlName="departamentoDestinatario">
          </mat-form-field>
        </div>

        <h3>Datos de Traslado</h3>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Motivo de Emisión</mat-label>
            <input matInput formControlName="motivoEmision">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Fecha Inicio Traslado</mat-label>
            <input matInput [matDatepicker]="pickerInicio" formControlName="fechaInicioTraslado">
            <mat-datepicker-toggle matSuffix [for]="pickerInicio"></mat-datepicker-toggle>
            <mat-datepicker #pickerInicio></mat-datepicker>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Fecha Fin Traslado</mat-label>
            <input matInput [matDatepicker]="pickerFin" formControlName="fechaFinTraslado">
            <mat-datepicker-toggle matSuffix [for]="pickerFin"></mat-datepicker-toggle>
            <mat-datepicker #pickerFin></mat-datepicker>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Km Estimado</mat-label>
            <input matInput type="number" formControlName="kmEstimado">
          </mat-form-field>
        </div>

        <h3>Transporte</h3>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Tipo Transporte</mat-label>
            <mat-select formControlName="tipoTransporte">
              <mat-option value="PROPIO">Propio</mat-option>
              <mat-option value="TERCERO">Tercero</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Modalidad</mat-label>
            <mat-select formControlName="modalidadTransporte">
              <mat-option value="TERRESTRE">Terrestre</mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <h3>Vehículo</h3>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Marca</mat-label>
            <input matInput formControlName="vehiculoMarca">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Matrícula</mat-label>
            <input matInput formControlName="vehiculoMatricula">
          </mat-form-field>
        </div>

        <h3>Conductor</h3>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Nombre</mat-label>
            <input matInput formControlName="conductorNombre">
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Documento</mat-label>
            <input matInput formControlName="conductorDoc">
          </mat-form-field>
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Dirección</mat-label>
            <input matInput formControlName="conductorDireccion">
          </mat-form-field>
        </div>

        <h3>Items</h3>
        <div formArrayName="items">
          <table mat-table [dataSource]="itemsArray.controls" class="items-table">
            <ng-container matColumnDef="producto">
              <th mat-header-cell *matHeaderCellDef>Producto</th>
              <td mat-cell *matCellDef="let item; let i = index">
                <mat-form-field appearance="outline">
                  <mat-select [formControl]="getItemControl(i, 'productoId')">
                    <mat-option [value]="null">Ninguno</mat-option>
                    <mat-option *ngFor="let p of productos()" [value]="p.id">{{ p.descripcion }}</mat-option>
                  </mat-select>
                </mat-form-field>
              </td>
            </ng-container>
            <ng-container matColumnDef="descripcion">
              <th mat-header-cell *matHeaderCellDef>Descripción</th>
              <td mat-cell *matCellDef="let item; let i = index">
                <mat-form-field appearance="outline">
                  <input matInput [formControl]="getItemControl(i, 'descripcion')">
                </mat-form-field>
              </td>
            </ng-container>
            <ng-container matColumnDef="cantidad">
              <th mat-header-cell *matHeaderCellDef>Cantidad</th>
              <td mat-cell *matCellDef="let item; let i = index">
                <mat-form-field appearance="outline">
                  <input matInput type="number" [formControl]="getItemControl(i, 'cantidad')">
                </mat-form-field>
              </td>
            </ng-container>
            <ng-container matColumnDef="unidad">
              <th mat-header-cell *matHeaderCellDef>Unidad</th>
              <td mat-cell *matCellDef="let item; let i = index">
                <mat-form-field appearance="outline">
                  <input matInput [formControl]="getItemControl(i, 'unidadMedida')">
                </mat-form-field>
              </td>
            </ng-container>
            <ng-container matColumnDef="acciones">
              <th mat-header-cell *matHeaderCellDef>Acciones</th>
              <td mat-cell *matCellDef="let item; let i = index">
                <button mat-icon-button (click)="eliminarItem(i)" color="warn"><mat-icon>delete</mat-icon></button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="itemColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: itemColumns;"></tr>
          </table>
          <button mat-stroked-button type="button" (click)="agregarItem()"><mat-icon>add</mat-icon> Agregar Item</button>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button mat-raised-button color="primary" (click)="onSave()" [disabled]="form.invalid">Guardar</button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content { min-width: 900px; max-height: 80vh; overflow-y: auto; }
    .form-row { display: flex; gap: 16px; margin-bottom: 16px; }
    .full-width { flex: 1; }
    .items-table { width: 100%; margin-bottom: 16px; }
    h3 { margin-top: 24px; margin-bottom: 16px; }
  `]
})
export class NotaRemisionFormDialogComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  form: FormGroup;
  timbradosDetalle = signal<TimbradoDetalle[]>([]);
  clientes = signal<Cliente[]>([]);
  productos = signal<Producto[]>([]);
  facturas = signal<FacturaLegal[]>([]);
  itemColumns = ['producto', 'descripcion', 'cantidad', 'unidad', 'acciones'];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<NotaRemisionFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { nota?: NotaRemision; empresaId?: number },
    private notaRemisionApi: NotaRemisionApiService,
    private timbradoApi: TimbradoApiService,
    private clienteApi: ClienteApiService,
    private productoApi: ProductoApiService,
    private facturaApi: FacturaApiService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      empresaId: [data.empresaId, Validators.required],
      timbradoDetalleId: [null, Validators.required],
      clienteId: [null],
      facturaLegalId: [null],
      fecha: [new Date(), Validators.required],
      direccionPartida: [''],
      ciudadPartida: [''],
      departamentoPartida: [''],
      nombreDestinatario: [''],
      rucDestinatario: [''],
      direccionDestinatario: [''],
      ciudadDestinatario: [''],
      departamentoDestinatario: [''],
      motivoEmision: [''],
      fechaInicioTraslado: [null],
      fechaFinTraslado: [null],
      kmEstimado: [0],
      tipoTransporte: ['PROPIO'],
      modalidadTransporte: ['TERRESTRE'],
      vehiculoMarca: [''],
      vehiculoMatricula: [''],
      conductorNombre: [''],
      conductorDoc: [''],
      conductorDireccion: [''],
      items: this.fb.array([], [Validators.required, Validators.minLength(1)])
    });
  }

  ngOnInit(): void {
    if (this.data.empresaId) this.cargarDatos();
    if (this.data.nota) this.cargarNota();
    else this.agregarItem();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarDatos(): void {
    if (!this.data.empresaId) return;
    this.timbradoApi.getDetallesByEmpresa(this.data.empresaId).pipe(takeUntil(this.destroy$)).subscribe((t: TimbradoDetalle[]) => this.timbradosDetalle.set(t));
    this.clienteApi.getByEmpresa(this.data.empresaId).pipe(takeUntil(this.destroy$)).subscribe((c: Cliente[]) => this.clientes.set(c));
    this.productoApi.getAll(this.data.empresaId).pipe(takeUntil(this.destroy$)).subscribe((p: Producto[]) => this.productos.set(p));
    this.facturaApi.getAll({ empresaId: this.data.empresaId }).pipe(takeUntil(this.destroy$)).subscribe((f: FacturaLegal[]) => this.facturas.set(f));
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
      direccionPartida: nota.direccionPartida,
      ciudadPartida: nota.ciudadPartida,
      departamentoPartida: nota.departamentoPartida,
      nombreDestinatario: nota.nombreDestinatario,
      rucDestinatario: nota.rucDestinatario,
      direccionDestinatario: nota.direccionDestinatario,
      ciudadDestinatario: nota.ciudadDestinatario,
      departamentoDestinatario: nota.departamentoDestinatario,
      motivoEmision: nota.motivoEmision,
      fechaInicioTraslado: nota.fechaInicioTraslado ? new Date(nota.fechaInicioTraslado) : null,
      fechaFinTraslado: nota.fechaFinTraslado ? new Date(nota.fechaFinTraslado) : null,
      kmEstimado: nota.kmEstimado,
      tipoTransporte: nota.tipoTransporte,
      modalidadTransporte: nota.modalidadTransporte,
      vehiculoMarca: nota.vehiculoMarca,
      vehiculoMatricula: nota.vehiculoMatricula,
      conductorNombre: nota.conductorNombre,
      conductorDoc: nota.conductorDoc,
      conductorDireccion: nota.conductorDireccion
    });
    const itemsArray = this.form.get('items') as FormArray;
    itemsArray.clear();
    nota.items.forEach(item => itemsArray.push(this.crearItemFormGroup(item)));
  }

  get itemsArray(): FormArray {
    return this.form.get('items') as FormArray;
  }

  agregarItem(): void {
    this.itemsArray.push(this.crearItemFormGroup());
  }

  crearItemFormGroup(item?: NotaRemisionItem): FormGroup {
    return this.fb.group({
      productoId: [item?.productoId || null],
      descripcion: [item?.descripcion || '', Validators.required],
      cantidad: [item?.cantidad || 1, [Validators.required, Validators.min(0.001)]],
      unidadMedida: [item?.unidadMedida || '']
    });
  }

  eliminarItem(index: number): void {
    this.itemsArray.removeAt(index);
  }

  getItemControl(index: number, controlName: string): FormControl {
    return this.itemsArray.at(index).get(controlName) as FormControl;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.form.invalid) return;
    const formValue = this.form.value;
    const nota: NotaRemision = {
      ...formValue,
      fecha: formValue.fecha.toISOString(),
      fechaInicioTraslado: formValue.fechaInicioTraslado ? formValue.fechaInicioTraslado.toISOString().split('T')[0] : '',
      fechaFinTraslado: formValue.fechaFinTraslado ? formValue.fechaFinTraslado.toISOString().split('T')[0] : '',
      items: formValue.items
    };
    if (this.data.nota?.id) nota.id = this.data.nota.id;
    this.notaRemisionApi.create(nota).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.snackBar.open('Nota de remisión guardada', 'Cerrar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: () => this.snackBar.open('Error al guardar nota de remisión', 'Cerrar', { duration: 3000 })
    });
  }
}

