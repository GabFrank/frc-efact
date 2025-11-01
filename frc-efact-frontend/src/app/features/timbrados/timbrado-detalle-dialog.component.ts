import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { Store } from '@ngrx/store';

import { TimbradoDetalle } from '../../models/timbrado.model';
import * as TimbradoDetallesActions from '../../core/state/timbrado-detalles/timbrado-detalles.actions';
import { AutocompleteSelectComponent, AutocompleteOption } from '../../shared/components/autocomplete-select/autocomplete-select.component';
import { SifenService } from '../../services/sifen.service';

export interface TimbradoDetalleDialogData {
  detalle?: TimbradoDetalle;
  timbradoId: number;
  timbradoIsElectronico?: boolean;
  isEditMode: boolean;
}

@Component({
  selector: 'app-timbrado-detalle-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatDividerModule,
    AutocompleteSelectComponent
  ],
  template: `
    <h2 mat-dialog-title>
      {{ isEditMode ? 'Editar Punto de Expedición' : 'Nuevo Punto de Expedición' }}
    </h2>

    <mat-dialog-content>
      <form [formGroup]="detalleForm" class="detalle-form">

        <!-- Información básica -->
        <div class="form-section">
          <h3>Información Básica</h3>

          <div class="form-row">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Punto de Expedición</mat-label>
              <input matInput
                     formControlName="puntoExpedicion"
                     placeholder="001"
                     maxlength="10"
                     [readonly]="isEditMode">
              <mat-error *ngIf="detalleForm.get('puntoExpedicion')?.hasError('required')">
                El punto de expedición es requerido
              </mat-error>
              <mat-error *ngIf="detalleForm.get('puntoExpedicion')?.hasError('pattern')">
                Solo letras mayúsculas y números
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Código de Establecimiento</mat-label>
              <input matInput
                     formControlName="codigoEstablecimientoFactura"
                     placeholder="001"
                     maxlength="10">
              <mat-error *ngIf="detalleForm.get('codigoEstablecimientoFactura')?.hasError('required')">
                El código de establecimiento es requerido
              </mat-error>
              <mat-error *ngIf="detalleForm.get('codigoEstablecimientoFactura')?.hasError('pattern')">
                Solo letras mayúsculas y números
              </mat-error>
            </mat-form-field>
          </div>
        </div>

        <mat-divider></mat-divider>

        <!-- Rangos de numeración (solo para timbrados no electrónicos) -->
        <div class="form-section" *ngIf="!timbradoIsElectronico">
          <h3>Rangos de Numeración</h3>

          <div class="form-row">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Rango Desde</mat-label>
              <input matInput
                     type="number"
                     formControlName="rangoDesde"
                     placeholder="1"
                     min="1">
              <mat-error *ngIf="detalleForm.get('rangoDesde')?.hasError('required')">
                El rango desde es requerido
              </mat-error>
              <mat-error *ngIf="detalleForm.get('rangoDesde')?.hasError('min')">
                Debe ser mayor a 0
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Rango Hasta</mat-label>
              <input matInput
                     type="number"
                     formControlName="rangoHasta"
                     placeholder="1000"
                     min="1">
              <mat-error *ngIf="detalleForm.get('rangoHasta')?.hasError('required')">
                El rango hasta es requerido
              </mat-error>
              <mat-error *ngIf="detalleForm.get('rangoHasta')?.hasError('min')">
                Debe ser mayor a 0
              </mat-error>
            </mat-form-field>
          </div>

          <div class="form-row" *ngIf="cantidadCalculada > 0">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Cantidad Total</mat-label>
              <input matInput
                     [value]="cantidadCalculada"
                     readonly>
              <mat-hint>Cantidad calculada automáticamente</mat-hint>
            </mat-form-field>

            <mat-form-field appearance="outline" class="half-width" *ngIf="isEditMode">
              <mat-label>Número Actual</mat-label>
              <input matInput
                     formControlName="numeroActual"
                     readonly>
              <mat-hint>Solo lectura en modo edición</mat-hint>
            </mat-form-field>
          </div>

          <div class="validation-message" *ngIf="detalleForm.hasError('rangoInvalido')">
            <mat-icon color="warn">error</mat-icon>
            <span>El rango desde debe ser menor que el rango hasta</span>
          </div>
        </div>

        <mat-divider></mat-divider>

        <!-- Ubicación -->
        <div class="form-section">
          <h3>Ubicación</h3>

          <!-- Departamento -->
          <div class="form-row">
            <app-autocomplete-select
              class="half-width"
              label="Departamento *"
              placeholder="Buscar departamento... (ej: 18 - Canindeyú)"
              [options]="departamentoOptions"
              [value]="selectedDepartamento"
              [hasError]="false"
              errorMessage=""
              (optionSelected)="onDepartamentoSelected($event)">
            </app-autocomplete-select>
          </div>

          <!-- Distrito -->
          <div class="form-row" *ngIf="distritoOptions.length > 0">
            <app-autocomplete-select
              class="half-width"
              label="Distrito *"
              placeholder="Buscar distrito... (ej: 207 - Salto del Guairá)"
              [options]="distritoOptions"
              [value]="selectedDistrito"
              [hasError]="false"
              errorMessage=""
              (optionSelected)="onDistritoSelected($event)">
            </app-autocomplete-select>
          </div>

          <!-- Ciudad -->
          <div class="form-row" *ngIf="ciudadOptions.length > 0">
            <app-autocomplete-select
              class="half-width"
              label="Ciudad *"
              placeholder="Buscar ciudad..."
              [options]="ciudadOptions"
              [value]="selectedCiudad"
              [hasError]="(detalleForm.get('ciudadId')?.invalid && detalleForm.get('ciudadId')?.touched) ?? false"
              errorMessage="La ciudad es requerida"
              (optionSelected)="onCiudadSelected($event)">
            </app-autocomplete-select>
          </div>

          <!-- Barrio -->
          <div class="form-row" *ngIf="barrioOptions.length > 0">
            <app-autocomplete-select
              class="half-width"
              label="Barrio"
              placeholder="Buscar barrio (opcional)..."
              [options]="barrioOptions"
              [value]="selectedBarrio"
              [hasError]="false"
              errorMessage=""
              (optionSelected)="onBarrioSelected($event)">
            </app-autocomplete-select>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Dirección</mat-label>
              <textarea matInput
                        formControlName="direccion"
                        placeholder="Av. Mariscal López 1234"
                        rows="2"></textarea>
            </mat-form-field>
          </div>

          <div class="form-row">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Teléfono</mat-label>
              <input matInput formControlName="telefono" placeholder="021-123456" maxlength="50">
            </mat-form-field>
          </div>
        </div>

        <mat-divider></mat-divider>

        <!-- Estado -->
        <div class="form-section">
          <h3>Estado</h3>

          <div class="form-row">
            <mat-checkbox formControlName="activo" class="checkbox-field">
              Punto de Expedición Activo
            </mat-checkbox>
          </div>
        </div>

      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">
        <mat-icon>cancel</mat-icon>
        Cancelar
      </button>

      <button mat-raised-button
              color="primary"
              (click)="onSave()"
              [disabled]="detalleForm.invalid || saving">
        <mat-icon>{{ isEditMode ? 'save' : 'add' }}</mat-icon>
        {{ isEditMode ? 'Guardar Cambios' : 'Crear Detalle' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .detalle-form {
      min-width: 600px;
      max-width: 800px;
    }

    .form-section {
      margin: 20px 0;
    }

    .form-section h3 {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 500;
      color: #3f51b5;
    }

    .form-row {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
    }

    .full-width {
      width: 100%;
    }

    .half-width {
      flex: 1;
    }

    app-autocomplete-select.half-width {
      flex: 1;
    }

    .checkbox-field {
      margin: 8px 0;
    }

    .validation-message {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background-color: #fff3e0;
      border-left: 4px solid #ff9800;
      border-radius: 4px;
      margin-top: 8px;
    }

    .validation-message mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .validation-message span {
      font-size: 14px;
      color: #e65100;
    }

    mat-dialog-actions button mat-icon {
      margin-right: 4px;
    }

    @media (max-width: 768px) {
      .detalle-form {
        min-width: 300px;
        max-width: 100%;
      }

      .form-row {
        flex-direction: column;
      }

      .half-width {
        width: 100%;
      }
    }
  `]
})
export class TimbradoDetalleDialogComponent implements OnInit {
  detalleForm: FormGroup;
  isEditMode: boolean;
  timbradoId: number;
  timbradoIsElectronico: boolean = false;
  saving = false;

  // SIFEN data
  departamentoOptions: AutocompleteOption[] = [];
  distritoOptions: AutocompleteOption[] = [];
  ciudadOptions: AutocompleteOption[] = [];
  barrioOptions: AutocompleteOption[] = [];

  selectedDepartamento: string = '';
  selectedDistrito: string = '';
  selectedCiudad: string = '';
  selectedBarrio: string = '';

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private sifenService: SifenService,
    private dialogRef: MatDialogRef<TimbradoDetalleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TimbradoDetalleDialogData
  ) {
    this.isEditMode = data.isEditMode;
    this.timbradoId = data.timbradoId;
    this.timbradoIsElectronico = data.timbradoIsElectronico || false;
    this.detalleForm = this.createForm();
  }

  ngOnInit(): void {
    // Cargar datos de SIFEN
    this.loadSifenData().then(() => {
      if (this.isEditMode && this.data.detalle) {
        this.loadGeografiaDataForEdit(this.data.detalle);
      }
    });

    // Agregar validador personalizado para rangos
    this.detalleForm.setValidators(this.rangoValidator.bind(this));
  }

  get cantidadCalculada(): number {
    const rangoDesde = this.detalleForm.get('rangoDesde')?.value;
    const rangoHasta = this.detalleForm.get('rangoHasta')?.value;

    if (rangoDesde && rangoHasta && rangoDesde < rangoHasta) {
      return rangoHasta - rangoDesde + 1;
    }
    return 0;
  }

  private createForm(): FormGroup {
    const isElectronico = this.timbradoIsElectronico;

    return this.fb.group({
      puntoExpedicion: ['', [
        Validators.required,
        Validators.pattern(/^[A-Z0-9]{1,10}$/)
      ]],
      codigoEstablecimientoFactura: ['', [
        Validators.required,
        Validators.pattern(/^[A-Z0-9]{1,10}$/)
      ]],
      rangoDesde: [null, isElectronico ? null : [Validators.required, Validators.min(1)]],
      rangoHasta: [null, isElectronico ? null : [Validators.required, Validators.min(1)]],
      numeroActual: [{ value: null, disabled: true }],
      ciudadId: [null, Validators.required],
      barrioId: [null],
      direccion: [''],
      telefono: ['', [Validators.maxLength(50)]],
      activo: [true]
    });
  }

  private rangoValidator(control: AbstractControl): { [key: string]: boolean } | null {
    // Skip validation for electronic timbrados
    if (this.timbradoIsElectronico) {
      return null;
    }

    const form = control as FormGroup;
    const rangoDesde = form.get('rangoDesde')?.value;
    const rangoHasta = form.get('rangoHasta')?.value;

    if (rangoDesde && rangoHasta && rangoDesde >= rangoHasta) {
      return { rangoInvalido: true };
    }

    return null;
  }

  onSave(): void {
    // Para timbrados electrónicos, deshabilitar validadores de rangos antes de validar
    if (this.timbradoIsElectronico) {
      this.detalleForm.get('rangoDesde')?.clearValidators();
      this.detalleForm.get('rangoDesde')?.updateValueAndValidity();
      this.detalleForm.get('rangoHasta')?.clearValidators();
      this.detalleForm.get('rangoHasta')?.updateValueAndValidity();
    }

    if (this.detalleForm.invalid) {
      this.detalleForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    const formValue = this.detalleForm.value;

    const detalleData: Partial<TimbradoDetalle> = {
      timbradoId: this.timbradoId,
      puntoExpedicion: formValue.puntoExpedicion?.toUpperCase(),
      codigoEstablecimientoFactura: formValue.codigoEstablecimientoFactura?.toUpperCase(),
      ciudadId: formValue.ciudadId,
      barrioId: formValue.barrioId || null,
      direccion: formValue.direccion || null,
      telefono: formValue.telefono || null,
      activo: formValue.activo !== undefined ? formValue.activo : true
    };

    // Solo incluir campos de rango para timbrados no electrónicos
    if (!this.timbradoIsElectronico) {
      detalleData.cantidad = this.cantidadCalculada;
      detalleData.rangoDesde = formValue.rangoDesde;
      detalleData.rangoHasta = formValue.rangoHasta;
    }

    if (this.isEditMode && this.data.detalle) {
      this.store.dispatch(TimbradoDetallesActions.updateDetalle({
        id: this.data.detalle.id,
        detalle: detalleData
      }));
    } else {
      this.store.dispatch(TimbradoDetallesActions.createDetalle({
        timbradoId: this.timbradoId,
        detalle: detalleData
      }));
    }

    this.dialogRef.close();
  }

  private getFormErrors(): any {
    const errors: any = {};
    Object.keys(this.detalleForm.controls).forEach(key => {
      const control = this.detalleForm.get(key);
      if (control && control.invalid && control.touched) {
        errors[key] = control.errors;
      }
    });
    return errors;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  // SIFEN geographic data methods
  private async loadSifenData(): Promise<void> {
    try {
      const departamentos = await this.sifenService.getDepartamentos().toPromise();
      if (departamentos) {
        this.departamentoOptions = departamentos.map(d => ({
          value: d.codigo,
          label: `${d.codigo} - ${d.nombre}`,
          codigo: d.codigo
        }));
      }
    } catch (error) {
      console.error('Error cargando departamentos:', error);
    }
  }

  private async loadGeografiaDataForEdit(detalle: TimbradoDetalle): Promise<void> {
    if (!detalle.ciudadId) return;

    try {
      const ciudad = await this.sifenService.getCiudadById(detalle.ciudadId).toPromise();
      if (!ciudad) return;

      // Set departamento
      this.selectedDepartamento = ciudad.departamentoCodigo;

      // Load distritos
      const distritos = await this.sifenService.getDistritosByDepartamento(ciudad.departamentoCodigo).toPromise();
      if (distritos && distritos.length > 0) {
        this.distritoOptions = distritos.map(d => ({
          value: d.codigo,
          label: `${d.codigo} - ${d.nombre}`,
          codigo: d.codigo
        }));
        this.selectedDistrito = ciudad.distritoCodigo;
      }

      // Load ciudades
      const ciudades = await this.sifenService.getCiudadesByDistrito(ciudad.distritoCodigo).toPromise();
      if (ciudades && ciudades.length > 0) {
        this.ciudadOptions = ciudades.map(c => ({
          value: c.id.toString(),
          label: `${c.codigo} - ${c.nombre}`,
          codigo: c.codigo
        }));
        this.selectedCiudad = ciudad.codigo;
        this.detalleForm.patchValue({ ciudadId: detalle.ciudadId });
      }

      // Load barrios
      const barrios = await this.sifenService.getBarriosByCiudad(ciudad.codigo).toPromise();
      if (barrios && barrios.length > 0) {
        this.barrioOptions = barrios.map(b => ({
          value: b.id.toString(),
          label: `${b.codigo} - ${b.nombre}`,
          codigo: b.codigo
        }));

        if (detalle.barrioId) {
          const barrioSeleccionado = barrios.find(b => b.id === detalle.barrioId);
          if (barrioSeleccionado) {
            this.selectedBarrio = barrioSeleccionado.codigo;
            this.detalleForm.patchValue({ barrioId: detalle.barrioId });
          }
        }
      }
    } catch (error) {
      console.error('Error cargando datos de geografía:', error);
    }
  }

  onDepartamentoSelected(option: AutocompleteOption): void {
    const codigo = option.codigo || option.value;
    this.selectedDepartamento = codigo;
    this.resetDistritoAndBelow();

    if (codigo) {
      this.sifenService.getDistritosByDepartamento(codigo).subscribe({
        next: (distritos) => {
          this.distritoOptions = distritos.map(d => ({
            value: d.codigo,
            label: `${d.codigo} - ${d.nombre}`,
            codigo: d.codigo
          }));
        },
        error: (error) => console.error('Error cargando distritos:', error)
      });
    }
  }

  onDistritoSelected(option: AutocompleteOption): void {
    const codigo = option.codigo || option.value;
    this.selectedDistrito = codigo;
    this.resetCiudadAndBelow();

    if (codigo) {
      this.sifenService.getCiudadesByDistrito(codigo).subscribe({
        next: (ciudades) => {
          this.ciudadOptions = ciudades.map(c => ({
            value: c.id.toString(),
            label: `${c.codigo} - ${c.nombre}`,
            codigo: c.codigo
          }));
        },
        error: (error) => console.error('Error cargando ciudades:', error)
      });
    }
  }

  onCiudadSelected(option: AutocompleteOption): void {
    const ciudad = this.ciudadOptions.find(c => c.codigo === (option.codigo || option.value));
    if (ciudad) {
      this.selectedCiudad = ciudad.codigo || option.value;
      this.detalleForm.patchValue({ ciudadId: parseInt(ciudad.value) });
      this.detalleForm.get('ciudadId')?.markAsTouched();
    }

    this.resetBarrio();

    // Load barrios
    if (option.codigo || option.value) {
      this.sifenService.getBarriosByCiudad(option.codigo || option.value).subscribe({
        next: (barrios) => {
          this.barrioOptions = barrios.map(b => ({
            value: b.id.toString(),
            label: `${b.codigo} - ${b.nombre}`,
            codigo: b.codigo
          }));
        },
        error: (error) => console.error('Error cargando barrios:', error)
      });
    }
  }

  onBarrioSelected(option: AutocompleteOption): void {
    const barrio = this.barrioOptions.find(b => b.codigo === (option.codigo || option.value));
    if (barrio) {
      this.selectedBarrio = barrio.codigo || option.value;
      this.detalleForm.patchValue({ barrioId: parseInt(barrio.value) });
      this.detalleForm.get('barrioId')?.markAsTouched();
    }
  }

  private resetDistritoAndBelow(): void {
    this.selectedDistrito = '';
    this.distritoOptions = [];
    this.resetCiudadAndBelow();
  }

  private resetCiudadAndBelow(): void {
    this.selectedCiudad = '';
    this.ciudadOptions = [];
    this.detalleForm.patchValue({ ciudadId: null });
    this.resetBarrio();
  }

  private resetBarrio(): void {
    this.selectedBarrio = '';
    this.barrioOptions = [];
    this.detalleForm.patchValue({ barrioId: null });
  }
}




