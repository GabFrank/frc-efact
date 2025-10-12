import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { Empresa } from '../../models/empresa.model';
import { EmpresasActions } from '../../core/state/empresas/empresas.actions';
import { selectEmpresaById, selectEmpresasLoading } from '../../core/state/empresas/empresas.selectors';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDividerModule,
    MatSnackBarModule,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="empresa-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>{{ isEditMode ? 'Editar Empresa' : 'Nueva Empresa' }}</h2>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <app-loading-spinner *ngIf="loading$ | async"></app-loading-spinner>

          <form [formGroup]="empresaForm" (ngSubmit)="onSubmit()" *ngIf="!(loading$ | async)">
            
            <!-- Datos Básicos Section -->
            <div class="form-section">
              <h3>Datos Básicos</h3>
              <mat-divider></mat-divider>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Razón Social *</mat-label>
                  <input matInput formControlName="razonSocial" placeholder="Ingrese la razón social">
                  <mat-error *ngIf="empresaForm.get('razonSocial')?.hasError('required')">
                    La razón social es requerida
                  </mat-error>
                  <mat-error *ngIf="empresaForm.get('razonSocial')?.hasError('maxlength')">
                    Máximo 200 caracteres
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>RUC *</mat-label>
                  <input matInput formControlName="ruc" placeholder="12345678-9">
                  <mat-error *ngIf="empresaForm.get('ruc')?.hasError('required')">
                    El RUC es requerido
                  </mat-error>
                  <mat-error *ngIf="empresaForm.get('ruc')?.hasError('pattern')">
                    Formato de RUC inválido (ej: 12345678-9)
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Nombre Fantasía</mat-label>
                  <input matInput formControlName="nombreFantasia" placeholder="Nombre comercial">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Email</mat-label>
                  <input matInput type="email" formControlName="email" placeholder="empresa@ejemplo.com">
                  <mat-error *ngIf="empresaForm.get('email')?.hasError('email')">
                    Email inválido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Teléfono</mat-label>
                  <input matInput formControlName="telefono" placeholder="+595 21 123456">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Dirección</mat-label>
                  <textarea matInput formControlName="direccion" rows="2" placeholder="Dirección completa"></textarea>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Tipo de Sociedad</mat-label>
                  <mat-select formControlName="tipoSociedad">
                    <mat-option value="SA">Sociedad Anónima (SA)</mat-option>
                    <mat-option value="SRL">Sociedad de Responsabilidad Limitada (SRL)</mat-option>
                    <mat-option value="EI">Empresa Individual</mat-option>
                    <mat-option value="OTRO">Otro</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>
            </div>

            <!-- Domicilio Fiscal Section -->
            <div class="form-section" formGroupName="domicilioFiscal">
              <h3>Domicilio Fiscal</h3>
              <mat-divider></mat-divider>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Departamento *</mat-label>
                  <input matInput formControlName="departamento" placeholder="Ej: Central">
                  <mat-error *ngIf="domicilioFiscal.get('departamento')?.hasError('required')">
                    El departamento es requerido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Ciudad *</mat-label>
                  <input matInput formControlName="ciudad" placeholder="Ej: Asunción">
                  <mat-error *ngIf="domicilioFiscal.get('ciudad')?.hasError('required')">
                    La ciudad es requerida
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Código de Ciudad *</mat-label>
                  <input matInput formControlName="codigoCiudad" placeholder="Ej: 001">
                  <mat-error *ngIf="domicilioFiscal.get('codigoCiudad')?.hasError('required')">
                    El código de ciudad es requerido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Localidad</mat-label>
                  <input matInput formControlName="localidad" placeholder="Localidad">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Barrio *</mat-label>
                  <input matInput formControlName="barrio" placeholder="Nombre del barrio">
                  <mat-error *ngIf="domicilioFiscal.get('barrio')?.hasError('required')">
                    El barrio es requerido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Dirección Fiscal *</mat-label>
                  <input matInput formControlName="direccion" placeholder="Calle y número">
                  <mat-error *ngIf="domicilioFiscal.get('direccion')?.hasError('required')">
                    La dirección fiscal es requerida
                  </mat-error>
                </mat-form-field>
              </div>
            </div>

            <!-- Actividad Económica Section -->
            <div class="form-section" formGroupName="actividadEconomica">
              <h3>Actividad Económica</h3>
              <mat-divider></mat-divider>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Código Principal *</mat-label>
                  <input matInput formControlName="codigoPrincipal" placeholder="Ej: 4711">
                  <mat-error *ngIf="actividadEconomica.get('codigoPrincipal')?.hasError('required')">
                    El código de actividad es requerido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Descripción Principal *</mat-label>
                  <input matInput formControlName="descripcionPrincipal" placeholder="Descripción de la actividad">
                  <mat-error *ngIf="actividadEconomica.get('descripcionPrincipal')?.hasError('required')">
                    La descripción es requerida
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Códigos Secundarios</mat-label>
                  <input matInput formControlName="codigosSecundarios" placeholder="Códigos separados por coma">
                  <mat-hint>Ingrese códigos separados por coma (ej: 4711, 4719)</mat-hint>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Descripciones Secundarias</mat-label>
                  <textarea matInput formControlName="descripcionesSecundarias" rows="2" 
                    placeholder="Descripciones separadas por coma"></textarea>
                  <mat-hint>Ingrese descripciones separadas por coma</mat-hint>
                </mat-form-field>
              </div>
            </div>

            <!-- Certificado Digital Section -->
            <div class="form-section">
              <h3>Certificado Digital</h3>
              <mat-divider></mat-divider>

              <div class="certificate-upload">
                <input 
                  type="file" 
                  #fileInput 
                  accept=".pfx,.p12" 
                  (change)="onFileSelected($event)"
                  style="display: none">
                
                <button 
                  type="button" 
                  mat-raised-button 
                  color="accent"
                  (click)="fileInput.click()">
                  <mat-icon>upload_file</mat-icon>
                  {{ selectedFileName || 'Seleccionar Certificado .pfx' }}
                </button>

                <mat-form-field appearance="outline" class="password-field" *ngIf="selectedFileName">
                  <mat-label>Contraseña del Certificado</mat-label>
                  <input matInput type="password" formControlName="certificadoPassword" 
                    placeholder="Contraseña del archivo .pfx">
                  <mat-error *ngIf="empresaForm.get('certificadoPassword')?.hasError('required')">
                    La contraseña es requerida cuando se carga un certificado
                  </mat-error>
                </mat-form-field>
              </div>

              <p class="certificate-info" *ngIf="isEditMode && !selectedFileName">
                <mat-icon>info</mat-icon>
                Certificado actual: {{ currentCertificatePath || 'No configurado' }}
              </p>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
              <button type="button" mat-button (click)="onCancel()">
                Cancelar
              </button>
              <button type="submit" mat-raised-button color="primary" [disabled]="!empresaForm.valid || submitting">
                <mat-icon>save</mat-icon>
                {{ isEditMode ? 'Actualizar' : 'Crear' }} Empresa
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .empresa-form-container {
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
      margin-bottom: 32px;
    }

    .form-section h3 {
      margin: 0 0 16px 0;
      font-size: 18px;
      font-weight: 500;
      color: #333;
    }

    mat-divider {
      margin-bottom: 24px;
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

    .certificate-upload {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 16px;
    }

    .password-field {
      flex: 1;
    }

    .certificate-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #666;
      font-size: 14px;
      margin: 16px 0 0 0;
    }

    .certificate-info mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 16px;
      margin-top: 32px;
      padding-top: 16px;
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

      .certificate-upload {
        flex-direction: column;
        align-items: stretch;
      }
    }
  `]
})
export class EmpresaFormComponent implements OnInit {
  empresaForm: FormGroup;
  loading$: Observable<boolean>;
  isEditMode = false;
  empresaId: number | null = null;
  submitting = false;
  selectedFileName: string | null = null;
  selectedFile: File | null = null;
  currentCertificatePath: string | null = null;

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loading$ = this.store.select(selectEmpresasLoading);
    this.empresaForm = this.createForm();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.empresaId = parseInt(id, 10);
      this.loadEmpresa();
    }
  }

  private createForm(): FormGroup {
    return this.fb.group({
      razonSocial: ['', [Validators.required, Validators.maxLength(200)]],
      ruc: ['', [Validators.required, Validators.pattern(/^\d{6,8}-\d$/)]],
      nombreFantasia: [''],
      email: ['', [Validators.email]],
      telefono: [''],
      direccion: [''],
      tipoSociedad: [''],
      domicilioFiscal: this.fb.group({
        departamento: ['', Validators.required],
        ciudad: ['', Validators.required],
        codigoCiudad: ['', Validators.required],
        localidad: [''],
        barrio: ['', Validators.required],
        direccion: ['', Validators.required]
      }),
      actividadEconomica: this.fb.group({
        codigoPrincipal: ['', Validators.required],
        descripcionPrincipal: ['', Validators.required],
        codigosSecundarios: [''],
        descripcionesSecundarias: ['']
      }),
      certificadoPassword: ['']
    });
  }

  get domicilioFiscal(): FormGroup {
    return this.empresaForm.get('domicilioFiscal') as FormGroup;
  }

  get actividadEconomica(): FormGroup {
    return this.empresaForm.get('actividadEconomica') as FormGroup;
  }

  private loadEmpresa(): void {
    if (this.empresaId) {
      this.store.select(selectEmpresaById(this.empresaId)).subscribe(empresa => {
        if (empresa && empresa.id) {
          this.patchFormValues(empresa);
          this.currentCertificatePath = empresa.certificado?.path || null;
        }
      });
    }
  }

  private patchFormValues(empresa: Empresa): void {
    this.empresaForm.patchValue({
      razonSocial: empresa.razonSocial,
      ruc: empresa.ruc,
      nombreFantasia: empresa.nombreFantasia,
      email: empresa.email,
      telefono: empresa.telefono,
      direccion: empresa.direccion,
      tipoSociedad: empresa.tipoSociedad,
      domicilioFiscal: {
        departamento: empresa.domicilioFiscal.departamento,
        ciudad: empresa.domicilioFiscal.ciudad,
        codigoCiudad: empresa.domicilioFiscal.codigoCiudad,
        localidad: empresa.domicilioFiscal.localidad,
        barrio: empresa.domicilioFiscal.barrio,
        direccion: empresa.domicilioFiscal.direccion
      },
      actividadEconomica: {
        codigoPrincipal: empresa.actividadEconomica.codigoPrincipal,
        descripcionPrincipal: empresa.actividadEconomica.descripcionPrincipal,
        codigosSecundarios: empresa.actividadEconomica.codigosSecundarios?.join(', '),
        descripcionesSecundarias: empresa.actividadEconomica.descripcionesSecundarias?.join(', ')
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.selectedFileName = this.selectedFile.name;
      
      // Make password required when file is selected
      this.empresaForm.get('certificadoPassword')?.setValidators([Validators.required]);
      this.empresaForm.get('certificadoPassword')?.updateValueAndValidity();
    }
  }

  onSubmit(): void {
    if (this.empresaForm.valid && !this.submitting) {
      this.submitting = true;
      const formValue = this.empresaForm.value;

      // Parse comma-separated values for secondary activities
      const actividadEconomica = {
        ...formValue.actividadEconomica,
        codigosSecundarios: formValue.actividadEconomica.codigosSecundarios
          ? formValue.actividadEconomica.codigosSecundarios.split(',').map((s: string) => s.trim()).filter((s: string) => s)
          : [],
        descripcionesSecundarias: formValue.actividadEconomica.descripcionesSecundarias
          ? formValue.actividadEconomica.descripcionesSecundarias.split(',').map((s: string) => s.trim()).filter((s: string) => s)
          : []
      };

      const empresaData: Partial<Empresa> = {
        ...formValue,
        actividadEconomica,
        activo: true
      };

      // Handle file upload if present
      if (this.selectedFile) {
        // In a real implementation, you would upload the file first
        // and get back the path to store in the database
        // For now, we'll just include the filename
        empresaData.certificado = {
          path: `/certificates/${this.selectedFile.name}`,
          fechaExpiracion: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString() // 1 year from now
        };
      }

      if (this.isEditMode && this.empresaId) {
        this.store.dispatch(EmpresasActions.updateEmpresa({
          id: this.empresaId,
          empresa: empresaData
        }));
        this.snackBar.open('Empresa actualizada exitosamente', 'Cerrar', { duration: 3000 });
      } else {
        this.store.dispatch(EmpresasActions.createEmpresa({ empresa: empresaData as Empresa }));
        this.snackBar.open('Empresa creada exitosamente', 'Cerrar', { duration: 3000 });
      }

      setTimeout(() => {
        this.submitting = false;
        this.router.navigate(['/empresas']);
      }, 1000);
    }
  }

  onCancel(): void {
    this.router.navigate(['/empresas']);
  }
}
