import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';

import { Timbrado } from '../../models/timbrado.model';
import { Empresa } from '../../models/empresa.model';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-timbrado-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatDividerModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="timbrado-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>{{ isEditMode ? 'Editar Timbrado' : 'Nuevo Timbrado' }}</h2>
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
          <form [formGroup]="timbradoForm" (ngSubmit)="onSubmit()" *ngIf="!loading">
            
            <!-- Datos Básicos Section -->
            <div class="form-section">
              <h3>Datos Básicos</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Empresa</mat-label>
                  <mat-select formControlName="empresaId" required>
                    <mat-option *ngFor="let empresa of empresas" [value]="empresa.id">
                      {{ empresa.razonSocial }}
                    </mat-option>
                  </mat-select>
                  <mat-error *ngIf="timbradoForm.get('empresaId')?.hasError('required')">
                    La empresa es requerida
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Número de Timbrado</mat-label>
                  <input matInput formControlName="numero" placeholder="12345678" maxlength="20">
                  <mat-error *ngIf="timbradoForm.get('numero')?.hasError('required')">
                    El número de timbrado es requerido
                  </mat-error>
                  <mat-error *ngIf="timbradoForm.get('numero')?.hasError('pattern')">
                    El número debe contener solo dígitos
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>RUC</mat-label>
                  <input matInput formControlName="ruc" placeholder="12345678-9" maxlength="20">
                  <mat-error *ngIf="timbradoForm.get('ruc')?.hasError('required')">
                    El RUC es requerido
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Razón Social</mat-label>
                  <input matInput formControlName="razonSocial" placeholder="Razón social de la empresa">
                  <mat-error *ngIf="timbradoForm.get('razonSocial')?.hasError('required')">
                    La razón social es requerida
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-checkbox formControlName="isElectronico" class="checkbox-field">
                  Timbrado Electrónico
                </mat-checkbox>
              </div>

              <!-- CSC Field (conditional) -->
              <div class="form-row" *ngIf="timbradoForm.get('isElectronico')?.value">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>CSC (Código de Seguridad del Contribuyente)</mat-label>
                  <input matInput formControlName="csc" type="password" placeholder="Código de seguridad">
                  <mat-icon matSuffix>lock</mat-icon>
                  <mat-hint>Este código será encriptado al guardarse</mat-hint>
                  <mat-error *ngIf="timbradoForm.get('csc')?.hasError('required')">
                    El CSC es requerido para timbrados electrónicos
                  </mat-error>
                </mat-form-field>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Vigencia Section -->
            <div class="form-section">
              <h3>Vigencia</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Fecha de Inicio</mat-label>
                  <input matInput [matDatepicker]="pickerInicio" formControlName="fechaInicio">
                  <mat-datepicker-toggle matSuffix [for]="pickerInicio"></mat-datepicker-toggle>
                  <mat-datepicker #pickerInicio></mat-datepicker>
                  <mat-error *ngIf="timbradoForm.get('fechaInicio')?.hasError('required')">
                    La fecha de inicio es requerida
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Fecha de Fin</mat-label>
                  <input matInput [matDatepicker]="pickerFin" formControlName="fechaFin">
                  <mat-datepicker-toggle matSuffix [for]="pickerFin"></mat-datepicker-toggle>
                  <mat-datepicker #pickerFin></mat-datepicker>
                  <mat-error *ngIf="timbradoForm.get('fechaFin')?.hasError('required')">
                    La fecha de fin es requerida
                  </mat-error>
                  <mat-error *ngIf="timbradoForm.hasError('fechaFinMenor')">
                    La fecha de fin debe ser posterior a la fecha de inicio
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="validation-message" *ngIf="timbradoForm.hasError('fechaFinMenor')">
                <mat-icon color="warn">error</mat-icon>
                <span>La fecha de fin debe ser posterior a la fecha de inicio</span>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Domicilio Fiscal Section -->
            <div class="form-section">
              <h3>Domicilio Fiscal</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Departamento</mat-label>
                  <input matInput formControlName="domicilioFiscalDepartamento" placeholder="Central">
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Ciudad</mat-label>
                  <input matInput formControlName="domicilioFiscalCiudad" placeholder="Asunción">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Código de Ciudad</mat-label>
                  <input matInput formControlName="domicilioFiscalCodigoCiudad" placeholder="001">
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Localidad</mat-label>
                  <input matInput formControlName="domicilioFiscalLocalidad" placeholder="Centro">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Barrio</mat-label>
                  <input matInput formControlName="domicilioFiscalBarrio" placeholder="Barrio Obrero">
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Teléfono</mat-label>
                  <input matInput formControlName="telefono" placeholder="+595 21 123456">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Dirección</mat-label>
                  <textarea matInput formControlName="domicilioFiscalDireccion" 
                            placeholder="Dirección completa" rows="2"></textarea>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Email</mat-label>
                  <input matInput formControlName="email" type="email" placeholder="empresa@ejemplo.com">
                  <mat-error *ngIf="timbradoForm.get('email')?.hasError('email')">
                    Email inválido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Tipo de Sociedad</mat-label>
                  <input matInput formControlName="tipoSociedad" placeholder="S.A., S.R.L., etc.">
                </mat-form-field>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Actividad Económica Section -->
            <div class="form-section">
              <h3>Actividad Económica</h3>
              
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Código Actividad Principal</mat-label>
                  <input matInput formControlName="codActividadEconomicaPrincipal" placeholder="12345">
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Descripción Actividad Principal</mat-label>
                  <input matInput formControlName="descActividadEconomicaPrincipal" 
                         placeholder="Comercio al por menor">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Códigos Actividades Secundarias (separados por coma)</mat-label>
                  <input matInput formControlName="listCodigoActividadEconomicaSecundaria" 
                         placeholder="12346,12347">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Descripciones Actividades Secundarias (separadas por coma)</mat-label>
                  <textarea matInput formControlName="listDescripcionActividadEconomicaSecundaria" 
                            placeholder="Servicios de consultoría, Importación" rows="2"></textarea>
                </mat-form-field>
              </div>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
              <button mat-button type="button" (click)="onCancel()">
                <mat-icon>cancel</mat-icon>
                Cancelar
              </button>
              <button mat-raised-button color="primary" type="submit" [disabled]="timbradoForm.invalid || saving">
                <mat-icon>{{ isEditMode ? 'save' : 'add' }}</mat-icon>
                {{ isEditMode ? 'Guardar Cambios' : 'Crear Timbrado' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .timbrado-form-container {
      padding: 20px;
      max-width: 1200px;
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

    .checkbox-field {
      margin: 8px 0;
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
export class TimbradoFormComponent implements OnInit {
  timbradoForm: FormGroup;
  empresas: Empresa[] = [];
  isEditMode = false;
  timbradoId: number | null = null;
  loading = false;
  saving = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private timbradoApiService: TimbradoApiService,
    private empresaApiService: EmpresaApiService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.timbradoForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadEmpresas();
    
    // Check if we're in edit mode
    this.route.params.subscribe(params => {
      if (params['id'] && params['id'] !== 'new') {
        this.isEditMode = true;
        this.timbradoId = +params['id'];
        this.loadTimbrado(this.timbradoId);
      }
    });

    // Add custom validator for fecha fin
    this.timbradoForm.setValidators(this.fechaFinValidator.bind(this));
  }

  private createForm(): FormGroup {
    return this.fb.group({
      empresaId: [null, Validators.required],
      numero: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      ruc: ['', Validators.required],
      razonSocial: ['', Validators.required],
      isElectronico: [false],
      csc: [''],
      fechaInicio: [null, Validators.required],
      fechaFin: [null, Validators.required],
      email: ['', Validators.email],
      telefono: [''],
      tipoSociedad: [''],
      domicilioFiscalDepartamento: [''],
      domicilioFiscalCiudad: [''],
      domicilioFiscalCodigoCiudad: [''],
      domicilioFiscalLocalidad: [''],
      domicilioFiscalBarrio: [''],
      domicilioFiscalDireccion: [''],
      codActividadEconomicaPrincipal: [''],
      descActividadEconomicaPrincipal: [''],
      listCodigoActividadEconomicaSecundaria: [''],
      listDescripcionActividadEconomicaSecundaria: ['']
    });
  }

  private fechaFinValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const form = control as FormGroup;
    const fechaInicio = form.get('fechaInicio')?.value;
    const fechaFin = form.get('fechaFin')?.value;

    if (fechaInicio && fechaFin) {
      const inicio = new Date(fechaInicio);
      const fin = new Date(fechaFin);
      
      if (fin <= inicio) {
        return { fechaFinMenor: true };
      }
    }

    return null;
  }

  private loadEmpresas(): void {
    this.empresaApiService.getAll().subscribe({
      next: (empresas) => {
        this.empresas = empresas.filter(e => e.activo);
      },
      error: (err) => {
        this.error = 'Error al cargar las empresas';
        console.error('Error loading empresas:', err);
      }
    });
  }

  private loadTimbrado(id: number): void {
    this.loading = true;
    this.timbradoApiService.getById(id).subscribe({
      next: (timbrado) => {
        this.timbradoForm.patchValue({
          ...timbrado,
          fechaInicio: new Date(timbrado.fechaInicio),
          fechaFin: new Date(timbrado.fechaFin)
        });
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el timbrado';
        this.loading = false;
        console.error('Error loading timbrado:', err);
      }
    });
  }

  onSubmit(): void {
    if (this.timbradoForm.invalid) {
      this.timbradoForm.markAllAsTouched();
      return;
    }

    // Validate CSC for electronic timbrados
    if (this.timbradoForm.get('isElectronico')?.value && !this.timbradoForm.get('csc')?.value) {
      this.timbradoForm.get('csc')?.setErrors({ required: true });
      return;
    }

    this.saving = true;
    this.error = null;

    const formValue = this.timbradoForm.value;
    const timbradoData = {
      ...formValue,
      fechaInicio: this.formatDate(formValue.fechaInicio),
      fechaFin: this.formatDate(formValue.fechaFin),
      activo: true
    };

    const request = this.isEditMode && this.timbradoId
      ? this.timbradoApiService.update(this.timbradoId, timbradoData)
      : this.timbradoApiService.create(timbradoData);

    request.subscribe({
      next: () => {
        this.router.navigate(['/timbrados']);
      },
      error: (err) => {
        this.error = `Error al ${this.isEditMode ? 'actualizar' : 'crear'} el timbrado`;
        this.saving = false;
        console.error('Error saving timbrado:', err);
      }
    });
  }

  private formatDate(date: Date): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }

  onCancel(): void {
    this.router.navigate(['/timbrados']);
  }
}
