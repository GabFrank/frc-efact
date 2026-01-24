import { Component, OnInit, signal, OnDestroy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Subject, takeUntil, of, debounceTime, distinctUntilChanged, switchMap } from 'rxjs';
import { NotaRemisionApiService } from '../../core/api/nota-remision-api.service';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { ProductoApiService } from '../../core/api/producto-api.service';
import { FacturaApiService } from '../../core/api/factura-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { VehiculoApiService } from '../../core/api/vehiculo-api.service';
import { ChoferApiService } from '../../core/api/chofer-api.service';
import { SifenService, DepartamentoDto, CiudadDto, DistritoDto } from '../../services/sifen.service';
import { NotaRemision, NotaRemisionItem } from '../../models/nota.model';
import { TimbradoDetalle } from '../../models/timbrado.model';
import { Cliente } from '../../models/cliente.model';
import { Producto } from '../../models/producto.model';
import { FacturaLegal } from '../../models/factura.model';
import { Empresa } from '../../models/empresa.model';
import { Vehiculo } from '../../models/vehiculo.model';
import { Chofer } from '../../models/chofer.model';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { NotaRemisionItemDialogComponent } from './nota-remision-item-dialog.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { SifenApiService } from '../../core/api/sifen-api.service';

interface NotaRemisionItemView {
  id?: number;
  productoId?: number;
  descripcion: string;
  cantidad: number;
  unidadMedida: string;
}

@Component({
  selector: 'app-nota-remision-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatAutocompleteModule,
    MatCardModule,
    MatDividerModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDialogModule,
    ErrorMessageComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="nota-form-container">
      <app-loading-spinner [loading]="loading()" />

      <form [formGroup]="form" *ngIf="!loading()">
        <!-- Header -->
        <mat-card class="header-card">
          <mat-card-header>
            <mat-card-title>
              <div class="header-content">
                <button mat-icon-button type="button" (click)="onCancel()" matTooltip="Volver atrás">
                  <mat-icon>arrow_back</mat-icon>
                </button>
                <div class="header-text">
                  <h2>{{ isEdit ? 'Editar' : 'Nueva' }} Nota de Remisión</h2>
                  <p class="empresa-name" *ngIf="empresaNombre()">{{ empresaNombre() }}</p>
                </div>
              </div>
            </mat-card-title>
          </mat-card-header>
        </mat-card>

        <!-- Datos de la Nota -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Datos de la Nota</mat-card-title>
          </mat-card-header>
          <mat-card-content>
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

              <mat-form-field appearance="outline">
                <mat-label>Cargar desde Factura</mat-label>
                <mat-select formControlName="facturaLegalId" (selectionChange)="onFacturaChange($event.value)">
                  <mat-option [value]="null">Ninguna</mat-option>
                  <mat-option *ngFor="let f of facturas()" [value]="f.id">
                    {{ f.numeroFactura }} - {{ f.nombre }}
                  </mat-option>
                </mat-select>
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Datos de Traslado -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Datos de Traslado</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Motivo de Emisión</mat-label>
                <mat-select formControlName="motivoEmision" (selectionChange)="onMotivoChange($event.value)">
                  <mat-option *ngFor="let m of motivosEmision" [value]="m.id">
                    {{ m.id }} - {{ m.descripcion }}
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('motivoEmision')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha Inicio Traslado</mat-label>
                <input matInput [matDatepicker]="pickerInicio" formControlName="fechaInicioTraslado">
                <mat-datepicker-toggle matSuffix [for]="pickerInicio"></mat-datepicker-toggle>
                <mat-datepicker #pickerInicio></mat-datepicker>
                <app-error-message [control]="form.get('fechaInicioTraslado')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha Fin Traslado</mat-label>
                <input matInput [matDatepicker]="pickerFin" formControlName="fechaFinTraslado">
                <mat-datepicker-toggle matSuffix [for]="pickerFin"></mat-datepicker-toggle>
                <mat-datepicker #pickerFin></mat-datepicker>
                <app-error-message [control]="form.get('fechaFinTraslado')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Km Estimado</mat-label>
                <input matInput type="number" formControlName="kmEstimado">
              </mat-form-field>

              <mat-form-field appearance="outline" *ngIf="mostrarFechaEstimadaFactura()">
                <mat-label>Fecha Estimada Factura</mat-label>
                <input matInput [matDatepicker]="pickerFecEm" formControlName="fechaEstimadaFactura"
                       [min]="minDateFactura()" [max]="maxDateFactura()">
                <mat-datepicker-toggle matSuffix [for]="pickerFecEm"></mat-datepicker-toggle>
                <mat-datepicker #pickerFecEm></mat-datepicker>
                <app-error-message [control]="form.get('fechaEstimadaFactura')" />
                <mat-hint>Máximo 5 días desde la fecha de la nota</mat-hint>
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Datos de Salida -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Datos de Salida (Origen)</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Departamento de Salida</mat-label>
                <mat-select formControlName="departamentoPartidaId" (selectionChange)="onDepartamentoSalidaChange($event.value)">
                  <mat-option *ngFor="let d of departamentos()" [value]="d.id">
                    {{ d.nombre }}
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('departamentoPartidaId')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Distrito de Salida</mat-label>
                <mat-select formControlName="distritoPartidaId" (selectionChange)="onDistritoSalidaChange($event.value)">
                  <mat-option *ngFor="let d of distritosSalida()" [value]="d.id">
                    {{ d.nombre }}
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('distritoPartidaId')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Ciudad de Salida</mat-label>
                <mat-select formControlName="ciudadPartidaId" (selectionChange)="onCiudadSalidaSelected($event.value)">
                  <mat-option *ngFor="let c of ciudadesSalida()" [value]="c.id">
                    {{ c.nombre }}
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('ciudadPartidaId')" />
              </mat-form-field>
            </div>
            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Dirección de Partida</mat-label>
                <input matInput formControlName="direccionPartida">
                <app-error-message [control]="form.get('direccionPartida')" />
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Datos de Llegada (Cliente Destinatario) -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Datos de Llegada (Destinatario)</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-row" *ngIf="!esTrasladoEntreLocales()">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Buscar Cliente Destinatario</mat-label>
                <input matInput formControlName="clienteSearch" [matAutocomplete]="autoCliente" placeholder="Buscar por nombre o RUC">
                <mat-icon matPrefix>search</mat-icon>
                <mat-autocomplete #autoCliente="matAutocomplete" [displayWith]="displayCliente" (optionSelected)="onClienteSelected($event.option.value)">
                  <mat-option *ngFor="let c of clientesFiltrados()" [value]="c">
                    {{ c.nombre }} - {{ c.ruc }}
                  </mat-option>
                </mat-autocomplete>
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Nombre Destinatario</mat-label>
                <input matInput formControlName="nombreDestinatario" [readonly]="esTrasladoEntreLocales()">
                <app-error-message [control]="form.get('nombreDestinatario')" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>RUC Destinatario</mat-label>
                <input matInput formControlName="rucDestinatario" [readonly]="esTrasladoEntreLocales()">
                <app-error-message [control]="form.get('rucDestinatario')" />
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Departamento de Llegada</mat-label>
                <mat-select formControlName="departamentoDestinatarioId" (selectionChange)="onDepartamentoLlegadaChange($event.value)">
                  <mat-option *ngFor="let d of departamentos()" [value]="d.id">
                    {{ d.nombre }}
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('departamentoDestinatarioId')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Distrito de Llegada</mat-label>
                <mat-select formControlName="distritoDestinatarioId" (selectionChange)="onDistritoLlegadaChange($event.value)">
                  <mat-option *ngFor="let d of distritosLlegada()" [value]="d.id">
                    {{ d.nombre }}
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('distritoDestinatarioId')" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Ciudad de Llegada</mat-label>
                <mat-select formControlName="ciudadDestinatarioId" (selectionChange)="onCiudadLlegadaSelected($event.value)">
                  <mat-option *ngFor="let c of ciudadesLlegada()" [value]="c.id">
                    {{ c.nombre }}
                  </mat-option>
                </mat-select>
                <app-error-message [control]="form.get('ciudadDestinatarioId')" />
              </mat-form-field>
            </div>

            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Dirección de Llegada</mat-label>
                <input matInput formControlName="direccionDestinatario">
                <app-error-message [control]="form.get('direccionDestinatario')" />
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Transporte -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>Transporte</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Tipo Transporte</mat-label>
                <mat-select formControlName="tipoTransporte" (selectionChange)="onTipoTransporteChange($event.value)">
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
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Buscar Vehículo</mat-label>
                <input matInput formControlName="vehiculoSearch" [matAutocomplete]="autoVehiculo" placeholder="Buscar por matrícula o marca">
                <mat-icon matPrefix>search</mat-icon>
                <mat-autocomplete #autoVehiculo="matAutocomplete" [displayWith]="displayVehiculo" (optionSelected)="onVehiculoSelected($event.option.value)">
                  <mat-option *ngFor="let v of vehiculosFiltrados()" [value]="v">
                    {{ v.matricula }} - {{ v.marca }}
                  </mat-option>
                  <mat-option (click)="abrirGestionVehiculos()" class="add-option">
                    <mat-icon>add</mat-icon>
                    <span>Adicionar Vehículo</span>
                  </mat-option>
                </mat-autocomplete>
              </mat-form-field>
            </div>
            <div class="form-row" *ngIf="form.get('vehiculoId')?.value">
              <mat-form-field appearance="outline">
                <mat-label>Marca</mat-label>
                <input matInput formControlName="vehiculoMarca" readonly>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Matrícula</mat-label>
                <input matInput formControlName="vehiculoMatricula" readonly>
              </mat-form-field>
            </div>

            <h3>Transportista</h3>
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Nombre o Razón Social</mat-label>
                <input matInput formControlName="transportistaNombre">
                <app-error-message [control]="form.get('transportistaNombre')" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>RUC o Documento</mat-label>
                <input matInput formControlName="transportistaRuc">
                <app-error-message [control]="form.get('transportistaRuc')" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Dirección</mat-label>
                <input matInput formControlName="transportistaDireccion">
              </mat-form-field>
            </div>

            <h3>Conductor</h3>
            <div class="form-row">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Buscar Chofer</mat-label>
                <input matInput formControlName="choferSearch" [matAutocomplete]="autoChofer" placeholder="Buscar por nombre o documento">
                <mat-icon matPrefix>search</mat-icon>
                <mat-autocomplete #autoChofer="matAutocomplete" [displayWith]="displayChofer" (optionSelected)="onChoferSelected($event.option.value)">
                  <mat-option *ngFor="let c of choferesFiltrados()" [value]="c">
                    {{ c.nombre }} {{ c.documento ? '- ' + c.documento : '' }}
                  </mat-option>
                  <mat-option (click)="abrirGestionChoferes()" class="add-option">
                    <mat-icon>add</mat-icon>
                    <span>Adicionar Chofer</span>
                  </mat-option>
                </mat-autocomplete>
              </mat-form-field>
            </div>
            <div class="form-row" *ngIf="form.get('choferId')?.value">
              <mat-form-field appearance="outline">
                <mat-label>Nombre</mat-label>
                <input matInput formControlName="conductorNombre" readonly>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Documento</mat-label>
                <input matInput formControlName="conductorDoc" readonly>
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Dirección</mat-label>
                <input matInput formControlName="conductorDireccion" readonly>
              </mat-form-field>
            </div>
            <div class="form-row" *ngIf="!form.get('choferId')?.value">
              <mat-form-field appearance="outline">
                <mat-label>Nombre</mat-label>
                <input matInput formControlName="conductorNombre" placeholder="Ingrese manualmente si no selecciona chofer">
                <app-error-message [control]="form.get('conductorNombre')" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Documento</mat-label>
                <input matInput formControlName="conductorDoc" placeholder="Ingrese manualmente si no selecciona chofer">
                <app-error-message [control]="form.get('conductorDoc')" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Dirección</mat-label>
                <input matInput formControlName="conductorDireccion" placeholder="Ingrese manualmente si no selecciona chofer">
              </mat-form-field>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Items -->
        <mat-card class="section-card">
          <mat-card-header class="items-header">
            <mat-card-title>Items de la Nota</mat-card-title>
            <button mat-raised-button color="primary" type="button" (click)="agregarItem()">
              <mat-icon>add</mat-icon> AGREGAR ITEM
            </button>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="itemsData().length > 0; else emptyItems" class="items-table-wrapper">
              <table mat-table [dataSource]="itemsData()" class="items-table mat-elevation-z1">
                <ng-container matColumnDef="descripcion">
                  <th mat-header-cell *matHeaderCellDef>DESCRIPCIÓN</th>
                  <td mat-cell *matCellDef="let item">{{ item.descripcion }}</td>
                </ng-container>

                <ng-container matColumnDef="cantidad">
                  <th mat-header-cell *matHeaderCellDef>CANTIDAD</th>
                  <td mat-cell *matCellDef="let item">{{ item.cantidad | number:'1.0-3':'es-PY' }}</td>
                </ng-container>

                <ng-container matColumnDef="unidad">
                  <th mat-header-cell *matHeaderCellDef>UNIDAD</th>
                  <td mat-cell *matCellDef="let item">{{ item.unidadMedida || 'UNI' }}</td>
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

        <!-- Acciones -->
        <div class="actions-container">
          <button mat-button type="button" (click)="onCancel()">Cancelar</button>
          <button mat-raised-button color="primary" type="button" (click)="onSave()" [disabled]="form.invalid || saving() || itemsData().length === 0">
            {{ saving() ? 'Guardando...' : 'Guardar Nota de Remisión' }}
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .nota-form-container { padding: 20px; max-width: 1400px; margin: 0 auto; }
    .header-card, .section-card { margin-bottom: 20px; }
    .header-content { display: flex; align-items: center; gap: 12px; }
    .header-text { flex: 1; }
    .empresa-name { margin: 4px 0 0 0; font-size: 14px; color: #666; font-weight: 500; }
    .form-row { display: flex; gap: 16px; flex-wrap: wrap; align-items: flex-start; }
    .full-width { flex: 1 1 100%; }
    mat-form-field { flex: 1; min-width: 200px; }
    .items-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
    .items-table-wrapper { overflow-x: auto; }
    .items-table { width: 100%; min-width: 640px; }
    .items-table .mat-header-cell, .items-table .mat-cell { padding: 12px 16px; }
    .items-actions { display: flex; justify-content: flex-end; align-items: center; gap: 8px; }
    .empty-items { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 40px; color: #666; text-align: center; }
    .empty-items mat-icon { font-size: 48px; width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.5; }
    .actions-container { display: flex; justify-content: flex-end; gap: 16px; margin-top: 24px; padding: 20px; background-color: white; border-top: 1px solid #e0e0e0; position: sticky; bottom: 0; z-index: 10; }
    h3 { margin-top: 24px; margin-bottom: 16px; border-left: 4px solid #3f51b5; padding-left: 12px; }
    .add-option {
      color: #3f51b5;
      font-weight: 500;
    }
    .add-option mat-icon {
      margin-right: 8px;
    }
  `]
})
export class NotaRemisionFormComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  form!: FormGroup;
  isEdit = false;
  loading = signal(false);
  saving = signal(false);

  timbradosDetalle = signal<TimbradoDetalle[]>([]);
  clientesFiltrados = signal<Cliente[]>([]);
  productos = signal<Producto[]>([]);
  facturas = signal<FacturaLegal[]>([]);
  vehiculosFiltrados = signal<Vehiculo[]>([]);
  choferesFiltrados = signal<Chofer[]>([]);
  empresaNombre = signal<string>('');
  empresaActual = signal<Empresa | null>(null);

  departamentos = signal<DepartamentoDto[]>([]);
  distritosSalida = signal<DistritoDto[]>([]);
  ciudadesSalida = signal<CiudadDto[]>([]);
  distritosLlegada = signal<DistritoDto[]>([]);
  ciudadesLlegada = signal<CiudadDto[]>([]);

  displayedColumns: string[] = ['descripcion', 'cantidad', 'unidad', 'acciones'];
  itemsData = signal<NotaRemisionItemView[]>([]);
  mostrarFechaEstimadaFactura = signal<boolean>(false);
  minDateFactura = signal<Date | null>(null);
  maxDateFactura = signal<Date | null>(null);

  motivosEmision = [
    { id: '1', descripcion: 'Traslado por ventas' },
    { id: '2', descripcion: 'Traslado por compras' },
    { id: '3', descripcion: 'Traslado por devolución' },
    { id: '4', descripcion: 'Traslado por exportación' },
    { id: '5', descripcion: 'Traslado por importación' },
    { id: '6', descripcion: 'Traslado por consignación' },
    { id: '7', descripcion: 'Traslado entre locales de la misma empresa' },
    { id: '8', descripcion: 'Traslado por ferias' },
    { id: '9', descripcion: 'Traslado por reparación' },
    { id: '10', descripcion: 'Traslado por entrega de productos en carácter de préstamo' },
    { id: '11', descripcion: 'Traslado por exhibición' },
    { id: '12', descripcion: 'Traslado por publicidad' },
    { id: '13', descripcion: 'Traslado por transformación' },
    { id: '14', descripcion: 'Traslado por recolección de productos' },
    { id: '99', descripcion: 'Otros' }
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private notaRemisionApi: NotaRemisionApiService,
    private timbradoApi: TimbradoApiService,
    private clienteApi: ClienteApiService,
    private productoApi: ProductoApiService,
    private facturaApi: FacturaApiService,
    private empresaApi: EmpresaApiService,
    private vehiculoApi: VehiculoApiService,
    private choferApi: ChoferApiService,
    private sifenService: SifenService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private sifenApi: SifenApiService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.isEdit = !!id;
    const empresaId = this.route.snapshot.queryParams['empresaId'] || 1;
    const copyFromId = this.route.snapshot.queryParams['copyFromId'];
    const returnUrl = this.route.snapshot.queryParams['returnUrl'];

    this.initForm(empresaId);
    this.cargarDatosBase(empresaId, !!copyFromId);
    if (this.isEdit) {
      this.cargarNota(id);
    } else if (copyFromId) {
      this.copiarDesdeNota(+copyFromId);
    }

    // Si hay returnUrl, significa que se regresó desde gestión de vehículos/choferes
    // Recargar listas para mostrar los nuevos elementos
    if (returnUrl) {
      // Limpiar returnUrl de la URL
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { returnUrl: null },
        queryParamsHandling: 'merge',
        replaceUrl: true
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initForm(empresaId: number): void {
    this.form = this.fb.group({
      empresaId: [empresaId, Validators.required],
      timbradoDetalleId: [null, Validators.required],
      clienteId: [null],
      facturaLegalId: [null],
      fecha: [new Date(), Validators.required],

      // Salida
      departamentoPartidaId: [null, Validators.required],
      distritoPartidaId: [null, Validators.required],
      ciudadPartidaId: [null, Validators.required],
      departamentoPartida: [''],
      ciudadPartida: [''],
      direccionPartida: ['', Validators.required],

      // Traslado
      motivoEmision: ['', Validators.required],
      fechaInicioTraslado: [null, Validators.required],
      fechaFinTraslado: [null, Validators.required],
      kmEstimado: [0],

      // Llegada / Cliente
      clienteSearch: [''],
      nombreDestinatario: ['', Validators.required],
      rucDestinatario: ['', Validators.required],
      departamentoDestinatarioId: [null, Validators.required],
      distritoDestinatarioId: [null, Validators.required],
      ciudadDestinatarioId: [null, Validators.required],
      departamentoDestinatario: [''],
      ciudadDestinatario: [''],
      direccionDestinatario: ['', Validators.required],

      // Transporte
      tipoTransporte: ['PROPIO'],
      modalidadTransporte: ['TERRESTRE'],
      vehiculoId: [null],
      vehiculoSearch: [''],
      vehiculoMarca: [''],
      vehiculoMatricula: [''],

      // Transportista
      transportistaNombre: ['', Validators.required],
      transportistaRuc: ['', Validators.required],
      transportistaDireccion: [''],

      // Conductor
      choferId: [null],
      choferSearch: [''],
      conductorNombre: [''], // Se valida condicionalmente
      conductorDoc: [''], // Se valida condicionalmente
      conductorDireccion: [''],
      fechaEstimadaFactura: [null],

      items: this.fb.array([]) // Mantener FormArray para validación, pero usar itemsData para visualización
    });

    this.setupClienteAutocomplete(empresaId);
    this.setupVehiculoAutocomplete(empresaId);
    this.setupChoferAutocomplete(empresaId);

    // Escuchar cambios en la fecha de la nota para actualizar límites de fecha estimada factura
    this.form.get('fecha')?.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.actualizarValidacionFechaEstimada();
    });

    // Escuchar cambios en la fecha de inicio de traslado para establecer automáticamente fecha fin y fecha estimada
    this.form.get('fechaInicioTraslado')?.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((fechaInicio) => {
      if (fechaInicio) {
        // Establecer la misma fecha para fecha fin de traslado
        this.form.patchValue({ fechaFinTraslado: fechaInicio }, { emitEvent: false });

        // Establecer la última fecha disponible para fecha estimada de factura (fecha de la nota + 5 días)
        const fechaNota = this.form.get('fecha')?.value;
        if (fechaNota) {
          const fechaMaxima = new Date(fechaNota);
          fechaMaxima.setDate(fechaMaxima.getDate() + 5);
          this.form.patchValue({ fechaEstimadaFactura: fechaMaxima }, { emitEvent: false });
        }
      }
    });
  }

  cargarDatosBase(empresaId: number, isCopying: boolean = false): void {
    this.loading.set(true);

    this.empresaApi.getById(empresaId).subscribe(empresa => {
      this.empresaActual.set(empresa);
      this.empresaNombre.set(empresa.razonSocial || empresa.nombreFantasia || '');

      this.sifenService.getDepartamentos().subscribe(depts => {
        this.departamentos.set(depts);

        // Solo establecer valores por defecto si no es edición y no estamos copiando
        if (!this.isEdit && !isCopying) {
          this.form.patchValue({
            direccionPartida: empresa.domicilioFiscalDireccion || empresa.direccion,
            ciudadPartidaId: empresa.ciudadId,
            transportistaNombre: empresa.razonSocial,
            transportistaRuc: empresa.ruc,
            transportistaDireccion: empresa.domicilioFiscalDireccion || empresa.direccion
          });

          if (empresa.ciudadId) {
            this.sifenService.getCiudadById(empresa.ciudadId).subscribe(ciudad => {
              const dept = depts.find(d => d.codigo === ciudad.departamentoCodigo);
              if (dept) {
                this.form.patchValue({
                  departamentoPartidaId: dept.id,
                  departamentoPartida: dept.nombre
                });
                this.cargarGeografiaCompleta('salida', dept.id, ciudad.distritoCodigo, ciudad.id);
              }
            });
          }
        }
      });
    });

    this.timbradoApi.getDetallesByEmpresa(empresaId).subscribe(t => this.timbradosDetalle.set(t.filter(d => d.activo)));
    this.productoApi.getByEmpresa(empresaId, 0, 1000).subscribe(p => this.productos.set(p.content.filter((prod: Producto) => prod.activo)));
    this.facturaApi.getAll({ empresaId }).subscribe(f => this.facturas.set(f));

    this.loading.set(false);
  }

  cargarNota(id: number): void {
    this.loading.set(true);
    this.notaRemisionApi.getById(id).subscribe({
      next: (nota) => {
        this.form.patchValue({
          ...nota,
          fecha: new Date(nota.fecha),
          fechaInicioTraslado: nota.fechaInicioTraslado ? new Date(nota.fechaInicioTraslado) : null,
          fechaFinTraslado: nota.fechaFinTraslado ? new Date(nota.fechaFinTraslado) : null,
          vehiculoId: nota.vehiculoId || null,
          choferId: nota.choferId || null,
          transportistaNombre: nota.transportistaNombre,
          transportistaRuc: nota.transportistaRuc,
          transportistaDireccion: nota.transportistaDireccion,
          conductorNombre: nota.conductorNombre,
          conductorDoc: nota.conductorDoc,
          conductorDireccion: nota.conductorDireccion,
          fechaEstimadaFactura: nota.fechaEstimadaFactura ? new Date(nota.fechaEstimadaFactura) : null,
        });

        // Cargar vehículo si existe
        if (nota.vehiculoId) {
          const empresaId = nota.empresaId;
          this.vehiculoApi.getById(empresaId, nota.vehiculoId).subscribe({
            next: (vehiculo) => {
              this.form.patchValue({
                vehiculoSearch: `${vehiculo.matricula} - ${vehiculo.marca}`,
                vehiculoMarca: vehiculo.marca,
                vehiculoMatricula: vehiculo.matricula
              });
            },
            error: () => {
              // Si no se puede cargar, usar campos legacy
              this.form.patchValue({
                vehiculoMarca: nota.vehiculoMarca,
                vehiculoMatricula: nota.vehiculoMatricula
              });
            }
          });
        } else {
          // Usar campos legacy
          this.form.patchValue({
            vehiculoMarca: nota.vehiculoMarca,
            vehiculoMatricula: nota.vehiculoMatricula
          });
        }

        // Cargar chofer si existe
        if (nota.choferId) {
          const empresaId = nota.empresaId;
          this.choferApi.getById(empresaId, nota.choferId).subscribe({
            next: (chofer) => {
              this.form.patchValue({
                choferSearch: `${chofer.nombre}${chofer.documento ? ' - ' + chofer.documento : ''}`,
                conductorNombre: chofer.nombre,
                conductorDoc: chofer.documento || '',
                conductorDireccion: chofer.direccion || ''
              });
            },
            error: () => {
              // Si no se puede cargar, usar campos legacy
              this.form.patchValue({
                conductorNombre: nota.conductorNombre,
                conductorDoc: nota.conductorDoc,
                conductorDireccion: nota.conductorDireccion
              });
            }
          });
        } else {
          // Usar campos legacy
          this.form.patchValue({
            conductorNombre: nota.conductorNombre,
            conductorDoc: nota.conductorDoc,
            conductorDireccion: nota.conductorDireccion
          });
        }

        this.actualizarValidacionFechaEstimada();

        if (nota.ciudadPartidaId) {
          this.sifenService.getCiudadById(nota.ciudadPartidaId).subscribe(ciu => {
            const depts = this.departamentos();
            const dept = depts.find(d => d.codigo === ciu.departamentoCodigo);
            if (dept) {
              this.form.patchValue({ departamentoPartidaId: dept.id });
              this.cargarGeografiaCompleta('salida', dept.id, ciu.distritoCodigo, ciu.id);
            }
          });
        }

        if (nota.ciudadDestinatarioId) {
          this.sifenService.getCiudadById(nota.ciudadDestinatarioId).subscribe(ciu => {
            const depts = this.departamentos();
            const dept = depts.find(d => d.codigo === ciu.departamentoCodigo);
            if (dept) {
              this.form.patchValue({ departamentoDestinatarioId: dept.id });
              this.cargarGeografiaCompleta('llegada', dept.id, ciu.distritoCodigo, ciu.id);
            }
          });
        }

        // Cargar items en itemsData
        const itemsView: NotaRemisionItemView[] = nota.items.map(item => ({
          id: item.id,
          productoId: item.productoId,
          descripcion: item.descripcion,
          cantidad: item.cantidad,
          unidadMedida: item.unidadMedida || 'UNI'
        }));
        this.itemsData.set(itemsView);
        this.actualizarItemsFormArray();
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Error al cargar la nota', 'Cerrar');
        this.router.navigate(['/notas/notas-remision']);
      }
    });
  }

  copiarDesdeNota(id: number): void {
    this.loading.set(true);
    this.notaRemisionApi.getById(id).subscribe({
      next: async (nota) => {
        // Copiar todos los campos excepto fecha, fechaInicioTraslado y fechaFinTraslado
        this.form.patchValue({
          empresaId: nota.empresaId,
          timbradoDetalleId: nota.timbradoDetalleId,
          clienteId: nota.clienteId,
          facturaLegalId: nota.facturaLegalId,
          fecha: new Date(), // Fecha actual
          // Salida
          departamentoPartidaId: nota.departamentoPartidaId,
          // No establecer distritoPartidaId y ciudadPartidaId aquí, se establecerán en cargarGeografiaCompleta
          departamentoPartida: nota.departamentoPartida,
          direccionPartida: nota.direccionPartida,
          // Traslado
          motivoEmision: nota.motivoEmision,
          fechaInicioTraslado: null, // Limpiar fecha inicio
          fechaFinTraslado: null, // Limpiar fecha fin
          kmEstimado: nota.kmEstimado,
          // Llegada / Cliente
          nombreDestinatario: nota.nombreDestinatario,
          rucDestinatario: nota.rucDestinatario,
          departamentoDestinatarioId: nota.departamentoDestinatarioId,
          // No establecer distritoDestinatarioId y ciudadDestinatarioId aquí, se establecerán en cargarGeografiaCompleta
          departamentoDestinatario: nota.departamentoDestinatario,
          direccionDestinatario: nota.direccionDestinatario,
          // Transporte
          tipoTransporte: nota.tipoTransporte,
          modalidadTransporte: nota.modalidadTransporte,
          vehiculoId: null, // No copiar el ID, permitir seleccionar nuevo vehículo
          vehiculoMarca: nota.vehiculoMarca,
          vehiculoMatricula: nota.vehiculoMatricula,
          // Transportista
          transportistaNombre: nota.transportistaNombre,
          transportistaRuc: nota.transportistaRuc,
          transportistaDireccion: nota.transportistaDireccion,
          // Conductor
          choferId: null, // No copiar el ID, permitir seleccionar nuevo chofer
          conductorNombre: nota.conductorNombre,
          conductorDoc: nota.conductorDoc,
          conductorDireccion: nota.conductorDireccion,
          fechaEstimadaFactura: null, // Limpiar fecha estimada
        });

        this.actualizarValidacionFechaEstimada();

        // Preparar promesas para cargar geografía
        const promesasGeografia: Promise<void>[] = [];

        // Cargar geografía de salida
        if (nota.ciudadPartidaId) {
          const promesaSalida = this.sifenService.getCiudadById(nota.ciudadPartidaId).toPromise().then(async ciu => {
            if (!ciu) return;
            const depts = this.departamentos();
            const dept = depts.find(d => d.codigo === ciu.departamentoCodigo);
            if (dept) {
              this.form.patchValue({ departamentoPartidaId: dept.id });
              await this.cargarGeografiaCompleta('salida', dept.id, ciu.distritoCodigo, ciu.id);
            }
          });
          promesasGeografia.push(promesaSalida);
        }

        // Cargar geografía de llegada
        if (nota.ciudadDestinatarioId) {
          const promesaLlegada = this.sifenService.getCiudadById(nota.ciudadDestinatarioId).toPromise().then(async ciu => {
            if (!ciu) return;
            const depts = this.departamentos();
            const dept = depts.find(d => d.codigo === ciu.departamentoCodigo);
            if (dept) {
              this.form.patchValue({ departamentoDestinatarioId: dept.id });
              await this.cargarGeografiaCompleta('llegada', dept.id, ciu.distritoCodigo, ciu.id);
            }
          });
          promesasGeografia.push(promesaLlegada);
        }

        // Esperar a que todas las operaciones de geografía terminen
        await Promise.all(promesasGeografia);

        // Copiar items sin sus IDs para que se creen como nuevos
        const itemsView: NotaRemisionItemView[] = nota.items.map(item => ({
          // No incluir id para que se cree como nuevo item
          productoId: item.productoId,
          descripcion: item.descripcion,
          cantidad: item.cantidad,
          unidadMedida: item.unidadMedida || 'UNI'
        }));
        this.itemsData.set(itemsView);
        this.actualizarItemsFormArray();
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('Error al cargar la nota para copiar', 'Cerrar');
        this.router.navigate(['/notas/notas-remision']);
      }
    });
  }

  private async cargarGeografiaCompleta(tipo: 'salida' | 'llegada', deptId: number, distritoCodigo: string, ciudadId: number) {
    const dept = this.departamentos().find(d => d.id === deptId);
    if (!dept) return;

    // Cargar distritos
    const distritos = await this.sifenService.getDistritosByDepartamento(dept.codigo).toPromise();
    if (distritos) {
      if (tipo === 'salida') this.distritosSalida.set(distritos);
      else this.distritosLlegada.set(distritos);

      const distrito = distritos.find(d => d.codigo === distritoCodigo);
      if (distrito) {
        if (tipo === 'salida') this.form.patchValue({ distritoPartidaId: distrito.id });
        else this.form.patchValue({ distritoDestinatarioId: distrito.id });

        // Cargar ciudades
        const ciudades = await this.sifenService.getCiudadesByDistrito(distrito.codigo).toPromise();
        if (ciudades) {
          if (tipo === 'salida') this.ciudadesSalida.set(ciudades);
          else this.ciudadesLlegada.set(ciudades);

          const ciudad = ciudades.find(c => c.id === ciudadId);
          if (ciudad) {
            if (tipo === 'salida') {
              this.form.patchValue({ ciudadPartidaId: ciudad.id, ciudadPartida: ciudad.nombre });
            } else {
              this.form.patchValue({ ciudadDestinatarioId: ciudad.id, ciudadDestinatario: ciudad.nombre });
            }
          }
        }
      }
    }
  }

  onDepartamentoSalidaChange(id: number): void {
    const dept = this.departamentos().find(d => d.id === id);
    if (dept) {
      this.form.patchValue({
        departamentoPartida: dept.nombre,
        distritoPartidaId: null,
        ciudadPartidaId: null
      });
      this.distritosSalida.set([]);
      this.ciudadesSalida.set([]);
      this.sifenService.getDistritosByDepartamento(dept.codigo).subscribe(distritos => {
        this.distritosSalida.set(distritos);
      });
    }
  }

  onDistritoSalidaChange(id: number): void {
    const distrito = this.distritosSalida().find(d => d.id === id);
    if (distrito) {
      this.form.patchValue({ ciudadPartidaId: null });
      this.ciudadesSalida.set([]);
      this.sifenService.getCiudadesByDistrito(distrito.codigo).subscribe(ciudades => {
        this.ciudadesSalida.set(ciudades);
      });
    }
  }

  onCiudadSalidaSelected(id: number): void {
    const ciudad = this.ciudadesSalida().find(c => c.id === id);
    if (ciudad) {
      this.form.patchValue({ ciudadPartida: ciudad.nombre });
    }
  }

  onDepartamentoLlegadaChange(id: number): void {
    const dept = this.departamentos().find(d => d.id === id);
    if (dept) {
      this.form.patchValue({
        departamentoDestinatario: dept.nombre,
        distritoDestinatarioId: null,
        ciudadDestinatarioId: null
      });
      this.distritosLlegada.set([]);
      this.ciudadesLlegada.set([]);
      this.sifenService.getDistritosByDepartamento(dept.codigo).subscribe(distritos => {
        this.distritosLlegada.set(distritos);
      });
    }
  }

  onDistritoLlegadaChange(id: number): void {
    const distrito = this.distritosLlegada().find(d => d.id === id);
    if (distrito) {
      this.form.patchValue({ ciudadDestinatarioId: null });
      this.ciudadesLlegada.set([]);
      this.sifenService.getCiudadesByDistrito(distrito.codigo).subscribe(ciudades => {
        this.ciudadesLlegada.set(ciudades);
      });
    }
  }

  onCiudadLlegadaSelected(id: number): void {
    const ciudad = this.ciudadesLlegada().find(c => c.id === id);
    if (ciudad) {
      this.form.patchValue({ ciudadDestinatario: ciudad.nombre });
    }
  }

  onMotivoChange(id: string): void {
    this.actualizarValidacionFechaEstimada();
    if (id === '7') {
      const empresa = this.empresaActual();
      if (empresa) {
        this.form.patchValue({
          nombreDestinatario: empresa.razonSocial,
          rucDestinatario: empresa.ruc,
          direccionDestinatario: empresa.domicilioFiscalDireccion || empresa.direccion,
          ciudadDestinatarioId: empresa.ciudadId
        });

        this.sifenService.getCiudadById(empresa.ciudadId).subscribe(ciudad => {
          const depts = this.departamentos();
          const dept = depts.find(d => d.codigo === ciudad.departamentoCodigo);
          if (dept) {
            this.form.patchValue({
              departamentoDestinatarioId: dept.id,
              departamentoDestinatario: dept.nombre
            });
            this.cargarGeografiaCompleta('llegada', dept.id, ciudad.distritoCodigo, ciudad.id);
          }
        });
      }
    }
  }

  onTipoTransporteChange(tipo: string): void {
    if (tipo === 'PROPIO') {
      const empresa = this.empresaActual();
      if (empresa) {
        this.form.patchValue({
          transportistaNombre: empresa.razonSocial,
          transportistaRuc: empresa.ruc,
          transportistaDireccion: empresa.domicilioFiscalDireccion || empresa.direccion
        });
      }
    } else {
      // Si cambia a TERCERO, limpiar para que el usuario ingrese los datos de la empresa transportista
      this.form.patchValue({
        transportistaNombre: '',
        transportistaRuc: '',
        transportistaDireccion: ''
      });
    }
  }

  actualizarValidacionFechaEstimada(): void {
    const motivo = this.form.get('motivoEmision')?.value;
    const facturaId = this.form.get('facturaLegalId')?.value;
    const fechaNota = this.form.get('fecha')?.value;
    const fechaEstimadaControl = this.form.get('fechaEstimadaFactura');

    const mostrar = motivo === '1' && !facturaId;
    this.mostrarFechaEstimadaFactura.set(mostrar);

    if (mostrar && fechaNota) {
      const min = new Date(fechaNota);
      const max = new Date(fechaNota);
      max.setDate(max.getDate() + 5);

      this.minDateFactura.set(min);
      this.maxDateFactura.set(max);

      fechaEstimadaControl?.setValidators([Validators.required]);
    } else {
      this.minDateFactura.set(null);
      this.maxDateFactura.set(null);
      fechaEstimadaControl?.clearValidators();
    }
    fechaEstimadaControl?.updateValueAndValidity();
  }

  esTrasladoEntreLocales(): boolean {
    return this.form.get('motivoEmision')?.value === '7';
  }

  setupClienteAutocomplete(empresaId: number): void {
    this.form.get('clienteSearch')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
      switchMap(value => {
        const searchTerm = typeof value === 'string' ? value : (value?.nombre || '');
        if (searchTerm.length >= 2) return this.clienteApi.buscar(empresaId, searchTerm);
        return of([]);
      })
    ).subscribe(clientes => this.clientesFiltrados.set(clientes));
  }

  displayCliente(cliente: Cliente | null): string {
    return cliente ? `${cliente.nombre}${cliente.ruc ? ' - ' + cliente.ruc : ''}` : '';
  }

  onClienteSelected(cliente: Cliente): void {
    this.form.patchValue({
      clienteId: cliente.id,
      nombreDestinatario: cliente.nombre,
      rucDestinatario: cliente.ruc,
      direccionDestinatario: cliente.direccion,
      ciudadDestinatarioId: cliente.ciudadId
    });

    if (cliente.ciudadId) {
      this.sifenService.getCiudadById(cliente.ciudadId).subscribe(ciudad => {
        const depts = this.departamentos();
        const dept = depts.find(d => d.codigo === ciudad.departamentoCodigo);
        if (dept) {
          this.form.patchValue({
            departamentoDestinatarioId: dept.id,
            departamentoDestinatario: dept.nombre
          });
          this.cargarGeografiaCompleta('llegada', dept.id, ciudad.distritoCodigo, ciudad.id);
        }
      });
    }
  }

  setupVehiculoAutocomplete(empresaId: number): void {
    this.form.get('vehiculoSearch')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
      switchMap(value => {
        const searchTerm = typeof value === 'string' ? value : (value?.matricula || '');
        if (searchTerm.length >= 2) return this.vehiculoApi.buscar(empresaId, searchTerm);
        return of([]);
      })
    ).subscribe(vehiculos => this.vehiculosFiltrados.set(vehiculos));
  }

  displayVehiculo(vehiculo: Vehiculo | null): string {
    return vehiculo ? `${vehiculo.matricula} - ${vehiculo.marca}` : '';
  }

  onVehiculoSelected(vehiculo: Vehiculo): void {
    if (!vehiculo || !vehiculo.id) return;

    this.form.patchValue({
      vehiculoId: vehiculo.id,
      vehiculoMarca: vehiculo.marca,
      vehiculoMatricula: vehiculo.matricula
    });
  }

  setupChoferAutocomplete(empresaId: number): void {
    this.form.get('choferSearch')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
      switchMap(value => {
        const searchTerm = typeof value === 'string' ? value : (value?.nombre || '');
        if (searchTerm.length >= 2) return this.choferApi.buscar(empresaId, searchTerm);
        return of([]);
      })
    ).subscribe(choferes => this.choferesFiltrados.set(choferes));
  }

  displayChofer(chofer: Chofer | null): string {
    return chofer ? `${chofer.nombre}${chofer.documento ? ' - ' + chofer.documento : ''}` : '';
  }

  onChoferSelected(chofer: Chofer): void {
    if (!chofer || !chofer.id) return;

    this.form.patchValue({
      choferId: chofer.id,
      conductorNombre: chofer.nombre,
      conductorDoc: chofer.documento || '',
      conductorDireccion: chofer.direccion || ''
    });
  }

  abrirGestionVehiculos(): void {
    const empresaId = this.form.get('empresaId')?.value;
    if (!empresaId) {
      this.snackBar.open('No se puede abrir gestión de vehículos sin empresa seleccionada', 'Cerrar', { duration: 3000 });
      return;
    }

    this.router.navigate(['/transporte/vehiculos/empresa', empresaId], {
      queryParams: { returnUrl: this.router.url }
    });
  }

  abrirGestionChoferes(): void {
    const empresaId = this.form.get('empresaId')?.value;
    if (!empresaId) {
      this.snackBar.open('No se puede abrir gestión de choferes sin empresa seleccionada', 'Cerrar', { duration: 3000 });
      return;
    }

    this.router.navigate(['/transporte/choferes/empresa', empresaId], {
      queryParams: { returnUrl: this.router.url }
    });
  }

  onFacturaChange(facturaId: number): void {
    this.actualizarValidacionFechaEstimada();
    const factura = this.facturas().find(f => f.id === facturaId);
    if (factura) {
      this.form.patchValue({
        clienteId: factura.clienteId,
        nombreDestinatario: factura.nombre,
        rucDestinatario: factura.ruc,
        direccionDestinatario: factura.direccion,
        ciudadDestinatarioId: (factura as any).ciudadId
      });

      const ciudadId = (factura as any).ciudadId;
      if (ciudadId) {
        this.sifenService.getCiudadById(ciudadId).subscribe(ciudad => {
          const depts = this.departamentos();
          const dept = depts.find(d => d.codigo === ciudad.departamentoCodigo);
          if (dept) {
            this.form.patchValue({
              departamentoDestinatarioId: dept.id,
              departamentoDestinatario: dept.nombre
            });
            this.cargarGeografiaCompleta('llegada', dept.id, ciudad.distritoCodigo, ciudadId);
          }
        });
      }

      this.facturaApi.getById(facturaId).subscribe(full => {
        if (full.items?.length > 0) {
          const itemsView: NotaRemisionItemView[] = full.items.map(fi => ({
            productoId: fi.productoId,
            descripcion: fi.descripcion,
            cantidad: fi.cantidad,
            unidadMedida: (fi as any).unidadMedida || 'UNI'
          }));
          this.itemsData.set(itemsView);
          this.actualizarItemsFormArray();
        }
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
    const item = this.itemsData()[index];
    if (!item) return;

    const itemData: NotaRemisionItem = {
      id: item.id,
      productoId: item.productoId,
      descripcion: item.descripcion,
      cantidad: item.cantidad,
      unidadMedida: item.unidadMedida
    };

    this.abrirDialogoItem(itemData, index);
  }

  private abrirDialogoItem(item?: NotaRemisionItem, index?: number): void {
    const dialogRef = this.dialog.open(NotaRemisionItemDialogComponent, {
      width: '600px',
      data: {
        item: item,
        productos: this.productos()
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((result?: NotaRemisionItem) => {
      if (!result) return;

      const itemsActuales = this.itemsData();
      if (typeof index === 'number') {
        // Editar item existente
        const nuevosItems = [...itemsActuales];
        nuevosItems[index] = {
          id: result.id,
          productoId: result.productoId,
          descripcion: result.descripcion,
          cantidad: result.cantidad,
          unidadMedida: result.unidadMedida || 'UNI'
        };
        this.itemsData.set(nuevosItems);
      } else {
        // Agregar nuevo item
        this.itemsData.set([...itemsActuales, {
          id: result.id,
          productoId: result.productoId,
          descripcion: result.descripcion,
          cantidad: result.cantidad,
          unidadMedida: result.unidadMedida || 'UNI'
        }]);
      }
      this.actualizarItemsFormArray();
    });
  }

  eliminarItem(index: number): void {
    const itemsActuales = this.itemsData();
    itemsActuales.splice(index, 1);
    this.itemsData.set([...itemsActuales]);
    this.actualizarItemsFormArray();
  }

  private actualizarItemsFormArray(): void {
    const itemsArray = this.itemsArray;
    itemsArray.clear();
    this.itemsData().forEach(item => {
      itemsArray.push(this.fb.group({
        productoId: [item.productoId],
        descripcion: [item.descripcion],
        cantidad: [item.cantidad],
        unidadMedida: [item.unidadMedida || 'UNI']
      }));
    });
  }

  onCancel(): void {
    const empresaId = this.form.get('empresaId')?.value;
    this.router.navigate(['/notas/notas-remision'], { queryParams: { empresaId } });
  }

  onSave(): void {
    // Validar campos condicionales
    const vehiculoId = this.form.get('vehiculoId')?.value;
    const vehiculoMatricula = this.form.get('vehiculoMatricula')?.value;
    if (!vehiculoId && (!vehiculoMatricula || vehiculoMatricula.trim() === '')) {
      this.form.get('vehiculoMatricula')?.setErrors({ required: true });
      this.form.get('vehiculoMatricula')?.markAsTouched();
    }

    const choferId = this.form.get('choferId')?.value;
    const conductorNombre = this.form.get('conductorNombre')?.value;
    const conductorDoc = this.form.get('conductorDoc')?.value;
    if (!choferId && (!conductorNombre || conductorNombre.trim() === '')) {
      this.form.get('conductorNombre')?.setErrors({ required: true });
      this.form.get('conductorNombre')?.markAsTouched();
    }
    if (!choferId && (!conductorDoc || conductorDoc.trim() === '')) {
      this.form.get('conductorDoc')?.setErrors({ required: true });
      this.form.get('conductorDoc')?.markAsTouched();
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackBar.open('Por favor complete todos los campos requeridos', 'Cerrar', { duration: 3000 });
      return;
    }

    if (this.itemsData().length === 0) {
      this.snackBar.open('Debe agregar al menos un item a la nota de remisión', 'Cerrar', { duration: 3000 });
      return;
    }

    this.saving.set(true);
    this.actualizarItemsFormArray(); // Asegurar que FormArray esté sincronizado

    const formValue = this.form.getRawValue();
    const nota: NotaRemision = {
      ...formValue,
      fecha: formValue.fecha.toISOString(),
      fechaInicioTraslado: formValue.fechaInicioTraslado?.toISOString().split('T')[0],
      fechaFinTraslado: formValue.fechaFinTraslado?.toISOString().split('T')[0],
      vehiculoId: formValue.vehiculoId || undefined,
      choferId: formValue.choferId || undefined,
      transportistaNombre: formValue.transportistaNombre,
      transportistaRuc: formValue.transportistaRuc,
      transportistaDireccion: formValue.transportistaDireccion,
      conductorNombre: formValue.conductorNombre,
      conductorDoc: formValue.conductorDoc,
      conductorDireccion: formValue.conductorDireccion,
      fechaEstimadaFactura: formValue.fechaEstimadaFactura?.toISOString().split('T')[0],
      items: this.itemsData().map(item => ({
        id: item.id,
        productoId: item.productoId,
        descripcion: item.descripcion,
        cantidad: item.cantidad,
        unidadMedida: item.unidadMedida || 'UNI'
      }))
    };
    if (this.isEdit) nota.id = this.route.snapshot.params['id'];

    const request = this.isEdit && nota.id
      ? this.notaRemisionApi.create(nota) // Para edición, usar create que internamente hace update si tiene ID
      : this.notaRemisionApi.create(nota);

    request.subscribe({
      next: (notaGuardada) => {
        this.saving.set(false);
        this.snackBar.open(
          `Nota de remisión ${this.isEdit ? 'actualizada' : 'creada'} correctamente`,
          'Cerrar',
          { duration: 3000 }
        );

        // Si es una nueva nota de remisión (no edición), ofrecer crear DE
        if (!this.isEdit && notaGuardada.id) {
          this.preguntarCrearDE(notaGuardada.id);
        } else {
          // Si es edición, navegar de vuelta
          this.navegarAtras();
        }
      },
      error: (e) => {
        this.saving.set(false);
        console.error('=== ERROR AL GUARDAR NOTA DE REMISIÓN ===');
        console.error('Status:', e.status);
        console.error('Error completo:', e);
        console.error('Error body:', e.error);
        if (e.error?.errors) {
          console.error('Errores de validación:', e.error.errors);
        }
        if (e.error?.message) {
          console.error('Mensaje de error:', e.error.message);
        }
        console.error('==========================================');

        this.snackBar.open(
          e.error?.message || `Error al ${this.isEdit ? 'actualizar' : 'crear'} la nota de remisión`,
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }

  private navegarAtras(): void {
    const empresaId = this.form.get('empresaId')?.value;
    const queryParams: any = { refresh: 'true' };
    if (empresaId) {
      queryParams.empresaId = empresaId;
    }
    this.router.navigate(['/notas/notas-remision'], { queryParams });
  }

  private preguntarCrearDE(notaRemisionId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Crear Documento Electrónico',
        message: '¿Desea crear el documento electrónico (DE) para esta nota de remisión?',
        confirmText: 'Sí, crear DE',
        cancelText: 'No, más tarde'
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed) {
        this.crearDE(notaRemisionId);
      } else {
        this.navegarAtras();
      }
    });
  }

  private crearDE(notaRemisionId: number): void {
    this.saving.set(true);
    this.snackBar.open('Creando documento electrónico...', 'Cerrar', { duration: 2000 });

    this.notaRemisionApi.generarDE(notaRemisionId).pipe(takeUntil(this.destroy$)).subscribe({
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
}
