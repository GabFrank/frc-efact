import { Component, Inject, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors, FormControl } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, takeUntil, forkJoin, of } from 'rxjs';
import { SifenApiService } from '../../core/api/sifen-api.service';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { TimbradoDetalleApiService } from '../../core/api/timbrado-detalle-api.service';
import { Timbrado, TimbradoDetalle } from '../../models/timbrado.model';
import { FacturaLegal } from '../../models/factura.model';
import { AutocompleteSelectComponent, AutocompleteOption } from '../../shared/components/autocomplete-select/autocomplete-select.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

export interface InutilizarNumerosDialogData {
  factura?: FacturaLegal; // Opcional: si viene, es modo desde factura
  empresaId: number;
  timbradoId?: number; // Opcional: si viene, pre-seleccionar este timbrado
  timbradoDetalleId?: number; // Opcional: si viene, usar este detalle
  establecimiento?: string;
  puntoExpedicion?: string;
  numeroInicio?: number; // Si viene, pre-llenar y deshabilitar
  numeroFin?: number; // Si viene, pre-llenar y deshabilitar
}

// Tipos de DE según SIFEN
export enum TipoDE {
  FACTURA_ELECTRONICA = 'FACTURA_ELECTRONICA',
  FACTURA_ELECTRONICA_EXPORTACION = 'FACTURA_ELECTRONICA_EXPORTACION',
  FACTURA_ELECTRONICA_IMPORTACION = 'FACTURA_ELECTRONICA_IMPORTACION',
  AUTOFACTURA_ELECTRONICA = 'AUTOFACTURA_ELECTRONICA',
  NOTA_DE_CREDITO_ELECTRONICA = 'NOTA_DE_CREDITO_ELECTRONICA',
  NOTA_DE_DEBITO_ELECTRONICA = 'NOTA_DE_DEBITO_ELECTRONICA',
  NOTA_DE_REMISION_ELECTRONICA = 'NOTA_DE_REMISION_ELECTRONICA',
  COMPROBANTE_RETENCION_ELECTRONICO = 'COMPROBANTE_RETENCION_ELECTRONICO'
}

const TIPO_DE_OPCIONES = [
  { value: TipoDE.FACTURA_ELECTRONICA, label: '1 - Factura electrónica' },
  { value: TipoDE.FACTURA_ELECTRONICA_EXPORTACION, label: '2 - Factura electrónica de exportación' },
  { value: TipoDE.FACTURA_ELECTRONICA_IMPORTACION, label: '3 - Factura electrónica de importación' },
  { value: TipoDE.AUTOFACTURA_ELECTRONICA, label: '4 - Autofactura electrónica' },
  { value: TipoDE.NOTA_DE_CREDITO_ELECTRONICA, label: '5 - Nota de crédito electrónica' },
  { value: TipoDE.NOTA_DE_DEBITO_ELECTRONICA, label: '6 - Nota de débito electrónica' },
  { value: TipoDE.NOTA_DE_REMISION_ELECTRONICA, label: '7 - Nota de remisión electrónica' },
  { value: TipoDE.COMPROBANTE_RETENCION_ELECTRONICO, label: '8 - Comprobante de retención electrónico' }
];

@Component({
  selector: 'app-inutilizar-numeros-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    AutocompleteSelectComponent,
    ErrorMessageComponent
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>block</mat-icon>
      Inutilizar Números de Documentos Electrónicos
    </h2>

    <mat-dialog-content>
      <div class="dialog-content">
        <div class="warning-section" *ngIf="data.factura">
          <mat-icon color="warn">warning</mat-icon>
          <div>
            <h3>Inutilización desde Factura</h3>
            <p>Se inutilizará el número de factura: <strong>{{ data.factura.numeroFactura }}</strong></p>
            <p class="warning-text">Esta acción marcará este número como inutilizado en SIFEN.</p>
          </div>
        </div>

        <form [formGroup]="form" class="inutilizar-form">
          <!-- Timbrado -->
          <app-autocomplete-select
            [label]="'Timbrado'"
            [placeholder]="'Buscar timbrado por número...'"
            [options]="timbradoOptions()"
            [value]="timbradoControl.value ? String(timbradoControl.value) : null"
            [hasError]="timbradoControl.invalid && timbradoControl.touched"
            [errorMessage]="'El timbrado es obligatorio'"
            [required]="true"
            [disabled]="!!data.timbradoId"
            [showAllOnEmpty]="false"
            (optionSelected)="onTimbradoSelected($event)">
          </app-autocomplete-select>

          <!-- Establecimiento y Punto de Expedición -->
          <div class="form-row">
            <mat-form-field appearance="outline">
              <mat-label>Establecimiento</mat-label>
              <input matInput
                     formControlName="establecimiento"
                     placeholder="001"
                     maxlength="10"
                     [readonly]="!!data.establecimiento">
              <app-error-message [control]="form.get('establecimiento')" />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Punto de Expedición</mat-label>
              <input matInput
                     formControlName="puntoExpedicion"
                     placeholder="001"
                     maxlength="10"
                     [readonly]="!!data.puntoExpedicion">
              <app-error-message [control]="form.get('puntoExpedicion')" />
            </mat-form-field>
          </div>

          <!-- Número Inicio y Fin -->
          <div class="form-row">
            <mat-form-field appearance="outline">
              <mat-label>Número Inicio</mat-label>
              <input matInput
                     type="number"
                     formControlName="numeroInicio"
                     placeholder="1"
                     min="1"
                     [readonly]="!!data.numeroInicio">
              <app-error-message [control]="form.get('numeroInicio')" />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Número Fin</mat-label>
              <input matInput
                     type="number"
                     formControlName="numeroFin"
                     placeholder="1"
                     min="1"
                     [readonly]="!!data.numeroFin">
              <app-error-message [control]="form.get('numeroFin')" />
            </mat-form-field>
          </div>

          <!-- Tipo DE -->
          <mat-form-field appearance="outline">
            <mat-label>Tipo de Documento Electrónico</mat-label>
            <mat-select formControlName="tipoDE">
              <mat-option *ngFor="let opcion of tipoDEOpciones" [value]="opcion.value">
                {{ opcion.label }}
              </mat-option>
            </mat-select>
            <app-error-message [control]="form.get('tipoDE')" />
          </mat-form-field>

          <!-- Motivo -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Motivo de inutilización</mat-label>
            <textarea matInput
                      formControlName="motivo"
                      placeholder="Ingrese el motivo de la inutilización (mínimo 10 caracteres)"
                      rows="4"
                      maxlength="500">
            </textarea>
            <mat-hint align="start">{{ motivoLength() }}/500 caracteres</mat-hint>
            <mat-hint align="end" *ngIf="!isMotivoValid() && motivoLength() > 0" class="error-hint">
              Mínimo 10 caracteres requeridos
            </mat-hint>
            <app-error-message [control]="form.get('motivo')" />
          </mat-form-field>
        </form>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" [disabled]="submitting()">
        Cancelar
      </button>
      <button 
        mat-raised-button 
        color="warn" 
        (click)="onConfirm()"
        [disabled]="form.invalid || !isMotivoValid() || submitting()">
        <mat-spinner *ngIf="submitting()" diameter="20" style="display: inline-block; margin-right: 8px;"></mat-spinner>
        <mat-icon *ngIf="!submitting()">block</mat-icon>
        {{ submitting() ? 'Inutilizando...' : 'Confirmar Inutilización' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2 {
      display: flex;
      align-items: center;
      gap: 12px;
      color: #d32f2f;
    }

    h2 mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .dialog-content {
      min-width: 600px;
      padding: 16px 0;
    }

    .warning-section {
      display: flex;
      gap: 16px;
      background: #fff3e0;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 24px;
      border-left: 4px solid #ff9800;
    }

    .warning-section mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      flex-shrink: 0;
    }

    .warning-section h3 {
      margin: 0 0 8px 0;
      color: #e65100;
    }

    .warning-section p {
      margin: 8px 0;
      color: #666;
    }

    .warning-text {
      font-weight: 500;
      color: #d84315 !important;
    }

    .inutilizar-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .form-row {
      display: flex;
      gap: 16px;
    }

    .form-row mat-form-field {
      flex: 1;
    }

    .full-width {
      width: 100%;
    }

    .error-hint {
      color: #d32f2f;
    }

    mat-dialog-actions {
      padding: 16px 0 0 0;
      margin: 0;
    }

    mat-spinner {
      vertical-align: middle;
    }

    @media (max-width: 600px) {
      .dialog-content {
        min-width: auto;
        width: 100%;
      }

      .warning-section {
        flex-direction: column;
      }

      .form-row {
        flex-direction: column;
      }
    }
  `]
})
export class InutilizarNumerosDialogComponent implements OnInit, OnDestroy {
  protected readonly String = String;
  form: FormGroup;
  timbradoControl: FormControl;
  timbradoOptions = signal<AutocompleteOption[]>([]);
  tipoDEOpciones = TIPO_DE_OPCIONES;
  submitting = signal(false);
  
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private sifenApi: SifenApiService,
    private timbradoApi: TimbradoApiService,
    private timbradoDetalleApi: TimbradoDetalleApiService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<InutilizarNumerosDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: InutilizarNumerosDialogData
  ) {
    this.form = this.fb.group({
      timbradoId: [data.timbradoId || null, Validators.required],
      establecimiento: [data.establecimiento || '', Validators.required],
      puntoExpedicion: [data.puntoExpedicion || '', Validators.required],
      numeroInicio: [data.numeroInicio || null, [Validators.required, Validators.min(1)]],
      numeroFin: [data.numeroFin || null, [Validators.required, Validators.min(1)]],
      tipoDE: [TipoDE.FACTURA_ELECTRONICA, Validators.required],
      motivo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]],
      timbradoDetalleId: [data.timbradoDetalleId || null]
    }, { validators: this.rangoValidator });

    this.timbradoControl = this.form.get('timbradoId') as FormControl;
  }

  ngOnInit(): void {
    this.cargarTimbrados();
    
    // Si hay datos de factura, cargar información adicional
    if (this.data.factura && this.data.factura.timbradoDetalleId) {
      this.cargarTimbradoDetalle(this.data.factura.timbradoDetalleId);
    } else if (this.data.timbradoDetalleId) {
      this.cargarTimbradoDetalle(this.data.timbradoDetalleId);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarTimbrados(): void {
    this.timbradoApi.getByEmpresa(this.data.empresaId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (timbrados) => {
          const options: AutocompleteOption[] = timbrados
            .filter(t => t.activo)
            .map(timbrado => ({
              value: String(timbrado.id),
              label: `${timbrado.numero} - ${timbrado.razonSocial || 'N/A'}`,
              codigo: timbrado.ruc
            }));
          this.timbradoOptions.set(options);

          // Pre-seleccionar timbrado si viene en data
          if (this.data.timbradoId) {
            this.timbradoControl.setValue(this.data.timbradoId);
          }
        },
        error: (error) => {
          this.snackBar.open('Error al cargar timbrados', 'Cerrar', { duration: 3000 });
        }
      });
  }

  cargarTimbradoDetalle(detalleId: number): void {
    this.timbradoDetalleApi.getById(detalleId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (detalle: TimbradoDetalle) => {
          // Si no hay datos pre-llenados, usar los del detalle
          if (!this.data.establecimiento) {
            this.form.patchValue({
              establecimiento: detalle.codigoEstablecimientoFactura,
              puntoExpedicion: detalle.puntoExpedicion,
              timbradoId: detalle.timbradoId,
              timbradoDetalleId: detalle.id
            });
            this.timbradoControl.setValue(detalle.timbradoId);
          }
        },
        error: () => {
          // Silenciar error, no crítico
        }
      });
  }

  onTimbradoSelected(option: AutocompleteOption): void {
    const timbradoId = parseInt(option.value, 10);
    this.timbradoControl.setValue(timbradoId);
  }

  motivoLength(): number {
    return this.form.get('motivo')?.value?.length || 0;
  }

  isMotivoValid(): boolean {
    const motivo = this.form.get('motivo')?.value || '';
    return motivo.trim().length >= 10;
  }

  rangoValidator(control: AbstractControl): ValidationErrors | null {
    const numeroInicio = control.get('numeroInicio')?.value;
    const numeroFin = control.get('numeroFin')?.value;

    if (numeroInicio != null && numeroFin != null && numeroInicio > numeroFin) {
      return { rangoInvalido: true };
    }

    return null;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.form.invalid || !this.isMotivoValid()) {
      this.snackBar.open('Por favor complete todos los campos correctamente', 'Cerrar', { duration: 3000 });
      return;
    }

    const formValue = this.form.value;
    const timbradoId = formValue.timbradoId;
    
    if (!timbradoId) {
      this.snackBar.open('Debe seleccionar un timbrado', 'Cerrar', { duration: 3000 });
      return;
    }

    this.submitting.set(true);

    const request = {
      establecimiento: formValue.establecimiento,
      puntoExpedicion: formValue.puntoExpedicion,
      numeroInicio: formValue.numeroInicio,
      numeroFin: formValue.numeroFin,
      tipoDE: formValue.tipoDE,
      motivo: formValue.motivo.trim(),
      timbradoDetalleId: formValue.timbradoDetalleId || undefined
    };

    this.sifenApi.inutilizarNumeros(timbradoId, request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.snackBar.open('Números inutilizados exitosamente', 'Cerrar', { duration: 4000 });
          this.dialogRef.close(true);
        },
        error: (error) => {
          this.submitting.set(false);
          const errorMessage = error.error?.message || error.message || 'Error al inutilizar números';
          this.snackBar.open(errorMessage, 'Cerrar', { duration: 5000 });
        }
      });
  }
}

