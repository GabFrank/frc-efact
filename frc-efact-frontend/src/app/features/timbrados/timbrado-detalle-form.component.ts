import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

import { TimbradoDetalle } from '../../models/timbrado.model';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-timbrado-detalle-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="detalle-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>{{ isEditMode ? 'Editar Punto de Expedición' : 'Nuevo Punto de Expedición' }}</h2>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Loading State -->
          <app-loading-spinner *ngIf="loading"></app-loading-spinner>

          <!-- Error State -->
          <app-error-message 
            *ngIf="error"
            [message]="error">
          </app-error-message>

          <!-- Form -->
          <form [formGroup]="detalleForm" (ngSubmit)="onSubmit()" *ngIf="!loading">
            
            <!-- Identificación Section -->
            <div class="form-section">
              <h3>Identificación</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Punto de Expedición</mat-label>
                  <input matInput formControlName="puntoExpedicion" placeholder="001" maxlength="10">
                  <mat-hint>Código del punto de expedición</mat-hint>
                  <mat-error *ngIf="detalleForm.get('puntoExpedicion')?.hasError('required')">
                    El punto de expedición es requerido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Código Establecimiento</mat-label>
                  <input matInput formControlName="codigoEstablecimientoFactura" placeholder="001" maxlength="10">
                  <mat-hint>Código del establecimiento</mat-hint>
                  <mat-error *ngIf="detalleForm.get('codigoEstablecimientoFactura')?.hasError('required')">
                    El código de establecimiento es requerido
                  </mat-error>
                </mat-form-field>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Rango de Numeración Section -->
            <div class="form-section">
              <h3>Rango de Numeración</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Rango Desde</mat-label>
                  <input matInput formControlName="rangoDesde" type="number" placeholder="1">
                  <mat-error *ngIf="detalleForm.get('rangoDesde')?.hasError('required')">
                    El rango desde es requerido
                  </mat-error>
                  <mat-error *ngIf="detalleForm.get('rangoDesde')?.hasError('min')">
                    El valor debe ser mayor a 0
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Rango Hasta</mat-label>
                  <input matInput formControlName="rangoHasta" type="number" placeholder="10000">
                  <mat-error *ngIf="detalleForm.get('rangoHasta')?.hasError('required')">
                    El rango hasta es requerido
                  </mat-error>
                  <mat-error *ngIf="detalleForm.hasError('rangoInvalido')">
                    El rango hasta debe ser mayor que el rango desde
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Cantidad Total</mat-label>
                  <input matInput [value]="calcularCantidad()" readonly>
                  <mat-hint>Calculado automáticamente</mat-hint>
                </mat-form-field>
              </div>

              <div class="validation-message" *ngIf="detalleForm.hasError('rangoInvalido')">
                <mat-icon color="warn">error</mat-icon>
                <span>El rango hasta debe ser mayor que el rango desde</span>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Ubicación Section -->
            <div class="form-section">
              <h3>Ubicación del Punto de Expedición</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Departamento</mat-label>
                  <input matInput formControlName="departamento" placeholder="Central">
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Ciudad</mat-label>
                  <input matInput formControlName="ciudad" placeholder="Asunción">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Código de Ciudad</mat-label>
                  <input matInput formControlName="codigoCiudad" placeholder="001">
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Localidad</mat-label>
                  <input matInput formControlName="localidad" placeholder="Centro">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Barrio</mat-label>
                  <input matInput formControlName="barrio" placeholder="Barrio Obrero">
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Teléfono</mat-label>
                  <input matInput formControlName="telefono" placeholder="+595 21 123456">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Dirección</mat-label>
                  <textarea matInput formControlName="direccion" 
                            placeholder="Dirección completa del punto de expedición" rows="2"></textarea>
                </mat-form-field>
              </div>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
              <button mat-button type="button" (click)="onCancel()">
                <mat-icon>cancel</mat-icon>
                Cancelar
              </button>
              <button mat-raised-button color="primary" type="submit" [disabled]="detalleForm.invalid || saving">
                <mat-icon>{{ isEditMode ? 'save' : 'add' }}</mat-icon>
                {{ isEditMode ? 'Guardar Cambios' : 'Crear Punto de Expedición' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .detalle-form-container {
      padding: 20px;
      max-width: 1000px;
      margin: 0 auto;
    }

    mat-card-header {
      margin-bottom: 20px;
    }

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }

    .form-section {
      margin: 24px 0;
    }

    .form-section h3 {
      margin: 0 0 16px 0;
      font-size: 18px;
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

    mat-divider {
      margin: 24px 0;
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

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 16px;
      margin-top: 24px;
      padding-top: 24px;
      border-top: 1px solid #e0e0e0;
    }

    button mat-icon {
      margin-right: 4px;
    }

    @media (max-width: 768px) {
      .form-row {
        flex-direction: column;
      }

      .half-width {
        width: 100%;
      }
    }
  `]
})
export class TimbradoDetalleFormComponent implements OnInit {
  detalleForm: FormGroup;
  isEditMode = false;
  timbradoId: number | null = null;
  detalleId: number | null = null;
  loading = false;
  saving = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private timbradoApiService: TimbradoApiService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.detalleForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.timbradoId = +params['id'];
      
      if (params['detalleId'] && params['detalleId'] !== 'new') {
        this.isEditMode = true;
        this.detalleId = +params['detalleId'];
        this.loadDetalle(this.detalleId);
      }
    });

    // Add custom validator for rango
    this.detalleForm.setValidators(this.rangoValidator.bind(this));
  }

  private createForm(): FormGroup {
    return this.fb.group({
      puntoExpedicion: ['', Validators.required],
      codigoEstablecimientoFactura: ['', Validators.required],
      rangoDesde: [1, [Validators.required, Validators.min(1)]],
      rangoHasta: [10000, Validators.required],
      departamento: [''],
      ciudad: [''],
      codigoCiudad: [''],
      localidad: [''],
      barrio: [''],
      direccion: [''],
      telefono: ['']
    });
  }

  private rangoValidator(control: any): { [key: string]: boolean } | null {
    if (!control || !control.get) {
      return null;
    }
    
    const rangoDesde = control.get('rangoDesde')?.value;
    const rangoHasta = control.get('rangoHasta')?.value;

    if (rangoDesde && rangoHasta && rangoHasta <= rangoDesde) {
      return { rangoInvalido: true };
    }

    return null;
  }

  private loadDetalle(id: number): void {
    this.loading = true;
    this.timbradoApiService.getDetalleById(id).subscribe({
      next: (detalle) => {
        this.detalleForm.patchValue(detalle);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el punto de expedición';
        this.loading = false;
        console.error('Error loading detalle:', err);
      }
    });
  }

  calcularCantidad(): number {
    const rangoDesde = this.detalleForm.get('rangoDesde')?.value || 0;
    const rangoHasta = this.detalleForm.get('rangoHasta')?.value || 0;
    return rangoHasta - rangoDesde + 1;
  }

  onSubmit(): void {
    if (this.detalleForm.invalid) {
      this.detalleForm.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.error = null;

    const formValue = this.detalleForm.value;
    const detalleData = {
      ...formValue,
      timbradoId: this.timbradoId,
      cantidad: this.calcularCantidad(),
      numeroActual: this.isEditMode ? undefined : formValue.rangoDesde,
      activo: true
    };

    const request = this.isEditMode && this.detalleId
      ? this.timbradoApiService.updateDetalle(this.detalleId, detalleData)
      : this.timbradoApiService.createDetalle(detalleData);

    request.subscribe({
      next: () => {
        this.router.navigate(['/timbrados', this.timbradoId, 'detalles']);
      },
      error: (err) => {
        this.error = `Error al ${this.isEditMode ? 'actualizar' : 'crear'} el punto de expedición`;
        this.saving = false;
        console.error('Error saving detalle:', err);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/timbrados', this.timbradoId, 'detalles']);
  }
}
