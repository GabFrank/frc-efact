import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { Empresa } from '../../models/empresa.model';
import { EmpresasActions } from '../../core/state/empresas/empresas.actions';
import { selectEmpresaById, selectEmpresasLoading, selectEmpresasError } from '../../core/state/empresas/empresas.selectors';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { AutocompleteSelectComponent, AutocompleteOption } from '../../shared/components/autocomplete-select/autocomplete-select.component';
import { SifenService } from '../../services/sifen.service';
import { RucValidationService } from '../../services/ruc-validation.service';
import { rucAsyncValidator } from '../../validators/ruc-async.validator';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-empresa-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDividerModule,
    MatSnackBarModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
    AutocompleteSelectComponent
  ],
  template: `
    <div class="empresa-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <button
                mat-icon-button
                (click)="onGoBack()"
                matTooltip="Volver a la lista"
                class="back-button">
                <mat-icon>arrow_back</mat-icon>
              </button>
              <h2>{{ getPageTitle() }}</h2>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <app-loading-spinner *ngIf="loading$ | async"></app-loading-spinner>

          <form [formGroup]="empresaForm" (ngSubmit)="onSubmit()" *ngIf="!(loading$ | async)" [class.readonly-form]="isViewMode">

            <!-- Datos Básicos Section -->
            <div class="form-section">
              <h3>Datos Básicos</h3>
              <mat-divider></mat-divider>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Razón Social *</mat-label>
                  <input matInput formControlName="razonSocial" placeholder="Ingrese la razón social"
                         (input)="onUppercaseInput($event, 'razonSocial')" [readonly]="isViewMode">
                  <mat-error *ngIf="empresaForm.get('razonSocial')?.hasError('required')">
                    La razón social es requerida
                  </mat-error>
                  <mat-error *ngIf="empresaForm.get('razonSocial')?.hasError('maxlength')">
                    Máximo 200 caracteres
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Nombre Fantasía</mat-label>
                  <input matInput formControlName="nombreFantasia" placeholder="Nombre comercial"
                         (input)="onUppercaseInput($event, 'nombreFantasia')" [readonly]="isViewMode">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>RUC *</mat-label>
                  <input matInput formControlName="ruc" placeholder="12345678-9 (con guión y dígito verificador)"
                         [readonly]="isViewMode" (input)="onRucInput($event)">

                  <!-- Loading indicator -->
                  <mat-icon matSuffix *ngIf="rucValidationInProgress" class="ruc-loading">
                    <span class="loading-spinner"></span>
                  </mat-icon>

                  <!-- Success indicator -->
                  <mat-icon matSuffix *ngIf="!rucValidationInProgress && empresaForm.get('ruc')?.valid && empresaForm.get('ruc')?.value"
                            class="ruc-valid" color="primary">check_circle</mat-icon>

                  <!-- Error messages -->
                  <mat-error *ngIf="empresaForm.get('ruc')?.hasError('required')">
                    El RUC es requerido
                  </mat-error>
                  <mat-error *ngIf="empresaForm.get('ruc')?.hasError('pattern')">
                    Formato inválido. Debe tener guión y dígito verificador (ej: 12345678-9)
                  </mat-error>
                  <mat-error *ngIf="empresaForm.get('ruc')?.hasError('rucInvalidFormat')">
                    El RUC debe tener formato: 6-8 dígitos, guión (-) y dígito verificador (ej: 12345678-9)
                  </mat-error>
                  <mat-error *ngIf="empresaForm.get('ruc')?.hasError('rucExists')">
                    Este RUC ya está registrado en otra empresa
                    <span *ngIf="empresaForm.get('ruc')?.errors?.['existingRazonSocial']">
                      ({{ empresaForm.get('ruc')?.errors?.['existingRazonSocial'] }})
                    </span>
                  </mat-error>
                  <mat-error *ngIf="empresaForm.get('ruc')?.hasError('rucValidationError')">
                    {{ empresaForm.get('ruc')?.errors?.['rucValidationError'] }}
                  </mat-error>

                  <!-- Format hint -->
                  <mat-hint *ngIf="rucFormatHint && !empresaForm.get('ruc')?.errors">
                    {{ rucFormatHint }}
                  </mat-hint>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Tipo de Contribuyente *</mat-label>
                  <mat-select formControlName="tipoContribuyente">
                    <mat-option value="PF">PF - Persona Física</mat-option>
                    <mat-option value="PJ">PJ - Persona Jurídica</mat-option>
                  </mat-select>
                  <mat-error *ngIf="empresaForm.get('tipoContribuyente')?.hasError('required')">
                    El tipo de contribuyente es requerido
                  </mat-error>
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Email</mat-label>
                  <input matInput type="email" formControlName="email" placeholder="empresa@ejemplo.com" [readonly]="isViewMode">
                  <mat-error *ngIf="empresaForm.get('email')?.hasError('email')">
                    Email inválido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Teléfono</mat-label>
                  <input matInput formControlName="telefono" placeholder="+595 21 123456" [readonly]="isViewMode">
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Dirección</mat-label>
                  <textarea matInput formControlName="direccion" rows="2" placeholder="Dirección completa"
                            (input)="onUppercaseInput($event, 'direccion')" [readonly]="isViewMode"></textarea>
                </mat-form-field>
              </div>
            </div>

            <!-- Domicilio Fiscal Section -->
            <div class="form-section">
              <h3>Domicilio Fiscal</h3>
              <mat-divider></mat-divider>

              <!-- País (fijo: Paraguay) -->
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>País</mat-label>
                  <input matInput value="PRY - Paraguay" disabled>
                </mat-form-field>
              </div>

              <!-- Departamento -->
              <div class="form-row">
                <app-autocomplete-select
                  class="half-width"
                  label="Departamento *"
                  placeholder="Buscar departamento... (ej: 18 - Canindeyú)"
                  [options]="departamentoOptions"
                  [value]="selectedDepartamento"
                  [hasError]="false"
                  errorMessage="El departamento es requerido"
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
                  errorMessage="El distrito es requerido"
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
                  [hasError]="(empresaForm.get('ciudadId')?.invalid && empresaForm.get('ciudadId')?.touched) ?? false"
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

              <!-- Dirección -->
              <div class="form-row">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Dirección Fiscal *</mat-label>
                  <input matInput formControlName="domicilioFiscalDireccion" placeholder="Calle y número"
                         (input)="onUppercaseInput($event, 'domicilioFiscalDireccion')" [readonly]="isViewMode">
                  <mat-error *ngIf="empresaForm.get('domicilioFiscalDireccion')?.hasError('required')">
                    La dirección fiscal es requerida
                  </mat-error>
                </mat-form-field>
              </div>
            </div>

            <!-- Actividad Económica Section -->
            <div class="form-section" formGroupName="actividadEconomica">
              <h3>Actividad Económica</h3>
              <mat-divider></mat-divider>

              <!-- Actividad Principal -->
              <h4 class="subsection-title">Actividad Principal *</h4>
              <div class="form-row actividad-row">
                <mat-form-field appearance="outline" class="codigo-field">
                  <mat-label>Código *</mat-label>
                  <input matInput formControlName="codigoPrincipal" placeholder="Ej: 4711"
                         (input)="onUppercaseInput($event, 'codigoPrincipal', 'actividadEconomica')" [readonly]="isViewMode">
                  <mat-error *ngIf="actividadEconomica.get('codigoPrincipal')?.hasError('required')">
                    Requerido
                  </mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="descripcion-field">
                  <mat-label>Descripción *</mat-label>
                  <input matInput formControlName="descripcionPrincipal"
                         placeholder="Descripción de la actividad principal"
                         (input)="onUppercaseInput($event, 'descripcionPrincipal', 'actividadEconomica')" [readonly]="isViewMode">
                  <mat-error *ngIf="actividadEconomica.get('descripcionPrincipal')?.hasError('required')">
                    Requerido
                  </mat-error>
                </mat-form-field>
              </div>

              <!-- Actividades Secundarias -->
              <div class="actividades-secundarias-section">
                <div class="subsection-header">
                  <h4 class="subsection-title">Actividades Secundarias</h4>
                  <button type="button" mat-mini-fab color="primary"
                          (click)="agregarActividadSecundaria()"
                          matTooltip="Agregar actividad secundaria">
                    <mat-icon>add</mat-icon>
                  </button>
                </div>

                <div *ngFor="let actividad of actividadesSecundarias; let i = index"
                     class="form-row actividad-row actividad-secundaria">
                  <mat-form-field appearance="outline" class="codigo-field">
                    <mat-label>Código</mat-label>
                    <input matInput [(ngModel)]="actividad.codigo"
                           [ngModelOptions]="{standalone: true}"
                           placeholder="Ej: 4719"
                           (input)="onActividadSecundariaChange()" [readonly]="isViewMode">
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="descripcion-field">
                    <mat-label>Descripción</mat-label>
                    <input matInput [(ngModel)]="actividad.descripcion"
                           [ngModelOptions]="{standalone: true}"
                           placeholder="Descripción de la actividad"
                           (input)="onActividadSecundariaChange()" [readonly]="isViewMode">
                  </mat-form-field>

                  <button type="button" mat-icon-button color="warn"
                          (click)="eliminarActividadSecundaria(i)"
                          matTooltip="Eliminar actividad">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>

                <div *ngIf="actividadesSecundarias.length === 0" class="empty-state">
                  <mat-icon>info</mat-icon>
                  <span>No hay actividades secundarias. Haz clic en el botón + para agregar.</span>
                </div>
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
                    placeholder="Contraseña del archivo .pfx" [readonly]="isViewMode">
                  <mat-error *ngIf="empresaForm.get('certificadoPassword')?.hasError('required')">
                    La contraseña es requerida cuando se carga un certificado
                  </mat-error>
                </mat-form-field>
              </div>

              <!-- Información del certificado existente -->
              <div class="existing-certificate-info" *ngIf="isEditMode && hasExistingCertificate && !selectedFileName">
                <mat-icon class="cert-icon">lock</mat-icon>
                <div class="cert-details">
                  <p class="cert-path">
                    <strong>Certificado:</strong> {{ currentCertificatePath }}
                  </p>
                  <p class="cert-expiration" *ngIf="currentCertificateExpiration">
                    <strong>Expira:</strong> {{ currentCertificateExpiration | date:'dd/MM/yyyy' }}
                  </p>
                  <button
                    type="button"
                    mat-stroked-button
                    color="primary"
                    (click)="showPasswordEdit = !showPasswordEdit">
                    <mat-icon>{{ showPasswordEdit ? 'close' : 'edit' }}</mat-icon>
                    {{ showPasswordEdit ? 'Cancelar' : 'Cambiar Contraseña' }}
                  </button>
                </div>
              </div>

              <!-- Campo para editar solo la contraseña -->
              <mat-form-field appearance="outline" class="password-field" *ngIf="showPasswordEdit && hasExistingCertificate && !selectedFileName">
                <mat-label>Nueva Contraseña del Certificado</mat-label>
                <input matInput type="password" formControlName="certificadoPasswordNueva"
                  placeholder="Ingrese la nueva contraseña" [readonly]="isViewMode">
                <mat-error *ngIf="empresaForm.get('certificadoPasswordNueva')?.hasError('required')">
                  La contraseña es requerida
                </mat-error>
              </mat-form-field>

              <!-- Configuración SIFEN -->
              <div class="form-row">
                <mat-form-field appearance="outline" class="half-width">
                  <mat-label>Ambiente SIFEN *</mat-label>
                  <mat-select formControlName="sifenAmbiente" [disabled]="isViewMode">
                    <mat-option value="DEV">DEV - Desarrollo/Pruebas</mat-option>
                    <mat-option value="PROD">PROD - Producción</mat-option>
                  </mat-select>
                  <mat-hint>Seleccione el ambiente donde se realizarán las operaciones SIFEN</mat-hint>
                  <mat-error *ngIf="empresaForm.get('sifenAmbiente')?.hasError('required')">
                    El ambiente SIFEN es requerido
                  </mat-error>
                </mat-form-field>
              </div>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
              <button type="button" mat-button (click)="onCancel()">
                {{ isViewMode ? 'Volver' : 'Cancelar' }}
              </button>
              <button
                *ngIf="isViewMode"
                type="button"
                mat-raised-button
                color="accent"
                (click)="onEdit()">
                <mat-icon>edit</mat-icon>
                Editar
              </button>
              <button
                *ngIf="!isViewMode"
                type="submit"
                mat-raised-button
                color="primary"
                [disabled]="!empresaForm.valid || submitting">
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

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .back-button {
      color: #666;
    }

    .back-button:hover {
      color: #333;
      background-color: rgba(0, 0, 0, 0.04);
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

    app-autocomplete-select.half-width {
      flex: 1;
    }

    app-autocomplete-select.full-width {
      width: 100%;
    }

    /* Actividad Económica Styles */
    .subsection-title {
      margin: 16px 0 12px 0;
      font-size: 16px;
      font-weight: 500;
      color: #555;
    }

    .actividad-row {
      display: flex;
      gap: 12px;
      align-items: flex-start;
    }

    .codigo-field {
      flex: 0 0 30%;
      min-width: 150px;
    }

    .descripcion-field {
      flex: 1;
    }

    .actividades-secundarias-section {
      margin-top: 24px;
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 8px;
    }

    .subsection-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .subsection-header h4 {
      margin: 0;
    }

    .actividad-secundaria {
      background-color: white;
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 12px;
      border: 1px solid #e0e0e0;
    }

    .actividad-secundaria button[mat-icon-button] {
      flex-shrink: 0;
      margin-top: 8px;
    }

    .empty-state {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 16px;
      color: #666;
      font-style: italic;
      background-color: white;
      border-radius: 4px;
      border: 1px dashed #ccc;
    }

    .empty-state mat-icon {
      color: #999;
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

    .existing-certificate-info {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 8px;
      border: 1px solid #e0e0e0;
      margin-top: 16px;
    }

    .cert-icon {
      color: #4caf50;
      font-size: 28px;
      width: 28px;
      height: 28px;
      margin-top: 4px;
    }

    .cert-details {
      flex: 1;
    }

    .cert-details p {
      margin: 4px 0;
      color: #333;
      font-size: 14px;
    }

    .cert-path {
      word-break: break-all;
    }

    .cert-expiration {
      color: #666;
    }

    .cert-details button {
      margin-top: 12px;
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

    /* RUC validation styles */
    .ruc-loading {
      color: #ff9800;
    }

    .ruc-valid {
      color: #4caf50;
    }

    .loading-spinner {
      display: inline-block;
      width: 16px;
      height: 16px;
      border: 2px solid #f3f3f3;
      border-top: 2px solid #ff9800;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    /* Readonly form styles */
    .readonly-form mat-form-field {
      pointer-events: none;
    }

    .readonly-form input[readonly],
    .readonly-form textarea[readonly] {
      color: #666;
      cursor: default;
    }

    .readonly-form app-autocomplete-select {
      pointer-events: none;
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
export class EmpresaFormComponent implements OnInit, OnDestroy {
  empresaForm: FormGroup;
  loading$: Observable<boolean>;
  isEditMode = false;
  isViewMode = false;
  empresaId: number | null = null;
  submitting = false;
  selectedFileName: string | null = null;
  selectedFile: File | null = null;
  currentCertificatePath: string | null = null;
  currentCertificateExpiration: string | null = null;
  hasExistingCertificate = false;
  showPasswordEdit = false;

  // SIFEN data - Orden correcto: País → Departamento → Distrito → Ciudad → Barrio
  departamentoOptions: AutocompleteOption[] = [];
  distritoOptions: AutocompleteOption[] = [];
  ciudadOptions: AutocompleteOption[] = [];
  barrioOptions: AutocompleteOption[] = [];

  selectedDepartamento: string = '';
  selectedDistrito: string = '';
  selectedCiudad: string = '';
  selectedBarrio: string = '';

  // Actividades económicas secundarias
  actividadesSecundarias: Array<{ codigo: string, descripcion: string }> = [];

  // RUC validation
  rucValidationInProgress = false;
  rucFormatHint = '';

  // Error handling
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private sifenService: SifenService,
    private rucValidationService: RucValidationService,
    private empresaApiService: EmpresaApiService
  ) {
    this.loading$ = this.store.select(selectEmpresasLoading);
    this.empresaForm = this.createForm();
  }

  async ngOnInit(): Promise<void> {
    // Cargar datos de SIFEN primero
    await this.loadSifenData();

    const id = this.route.snapshot.paramMap.get('id');
    const mode = this.route.snapshot.url[this.route.snapshot.url.length - 1]?.path;

    if (id && id !== 'new') {
      this.empresaId = parseInt(id, 10);

      if (mode === 'edit') {
        this.isEditMode = true;
        this.isViewMode = false;
      } else {
        // Modo ver (cuando la URL es /empresas/:id sin 'edit')
        this.isEditMode = false;
        this.isViewMode = true;
      }

      this.loadEmpresa();
    }

    // Configurar estado inicial del formulario según el modo
    this.updateFormDisabledState();

    // Suscribirse a los errores para mostrar feedback
    this.store.select(selectEmpresasError).pipe(takeUntil(this.destroy$)).subscribe(error => {
      if (error) {
        // Detectar errores específicos
        const errorStr = typeof error === 'string' ? error : JSON.stringify(error);

        if (errorStr.includes('Dígito verificador del RUC es incorrecto') ||
            errorStr.includes('RUC inválido') ||
            errorStr.includes('formato')) {
          this.snackBar.open(
            '⚠️ El RUC debe tener formato válido: 6-8 dígitos, guión (-) y dígito verificador (ej: 12345678-9)',
            'Entendido',
            {
              duration: 8000,
              panelClass: ['warning-snackbar']
            }
          );
        } else if (errorStr.includes('duplicado') ||
                   errorStr.includes('ya existe') ||
                   errorStr.includes('Duplicate') ||
                   errorStr.includes('RUC')) {
          // Error de RUC duplicado
          this.snackBar.open(
            '❌ Este RUC ya está registrado en otra empresa. Verifique que no esté duplicado.',
            'Cerrar',
            {
              duration: 8000,
              panelClass: ['error-snackbar']
            }
          );
          // Marcar el campo RUC como inválido
          this.empresaForm.get('ruc')?.setErrors({ rucExists: true });
          this.empresaForm.get('ruc')?.markAsTouched();
        } else {
          // Mostrar el error completo al usuario
          this.snackBar.open(`Error: ${errorStr}`, 'Cerrar', {
            duration: 8000, // Aumentar duración para que el usuario pueda leer el mensaje completo
            panelClass: ['error-snackbar']
          });
        }
        this.submitting = false;
      }
    });

    // Suscribirse a cambios en loading para detectar cuando termina la operación
    this.store.select(selectEmpresasLoading).pipe(takeUntil(this.destroy$)).subscribe(loading => {
      if (!loading && this.submitting) {
        this.submitting = false;
        // Mostrar mensaje de éxito
        this.snackBar.open(
          this.isEditMode ? 'Empresa actualizada correctamente' : 'Empresa creada correctamente',
          'Cerrar',
          { duration: 3000 }
        );
        // Redirigir después de un breve delay
        setTimeout(() => {
          this.router.navigate(['/empresas']);
        }, 1000);
      }
    });


  }

  private getFormErrors(): any {
    const errors: any = {};

    Object.keys(this.empresaForm.controls).forEach(key => {
      const control = this.empresaForm.get(key);
      if (control && control.invalid) {
        if (control instanceof FormGroup) {
          const groupErrors: any = {};
          Object.keys(control.controls).forEach(groupKey => {
            const groupControl = control.get(groupKey);
            if (groupControl && groupControl.invalid) {
              groupErrors[groupKey] = {
                errors: groupControl.errors,
                value: groupControl.value,
                touched: groupControl.touched
              };
            }
          });
          if (Object.keys(groupErrors).length > 0) {
            errors[key] = groupErrors;
          }
        } else {
          errors[key] = {
            errors: control.errors,
            value: control.value,
            touched: control.touched
          };
        }
      }
    });

    return errors;
  }

  private async loadSifenData(): Promise<void> {
    try {
      // Cargar departamentos al inicio (País Paraguay es fijo)
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
      this.snackBar.open('Error cargando departamentos', 'Cerrar', { duration: 3000 });
    }
  }

  private createForm(): FormGroup {
    const form = this.fb.group({
      razonSocial: ['', [Validators.required, Validators.maxLength(200)]],
      // RUC debe tener formato: 6-8 dígitos, guión obligatorio, y 1 dígito verificador
      ruc: ['', [
        Validators.required,
        Validators.pattern(/^\d{6,8}-\d$/)
      ]],
      tipoContribuyente: ['PF', [Validators.required]], // PF por defecto
      nombreFantasia: [''],
      email: ['', [Validators.email]],
      telefono: [''],
      direccion: [''],
      // Domicilio fiscal - IDs directos
      ciudadId: [null, Validators.required],
      barrioId: [null],
      domicilioFiscalDireccion: ['', Validators.required],
      actividadEconomica: this.fb.group({
        codigoPrincipal: ['', Validators.required],
        descripcionPrincipal: ['', Validators.required],
        codigosSecundarios: [''],
        descripcionesSecundarias: ['']
      }),
      certificadoPassword: [''],
      certificadoPasswordNueva: [''],
      sifenAmbiente: ['DEV'] // Ambiente SIFEN por defecto DEV
    });

    // Configurar validación asíncrona del RUC después de crear el formulario
    this.setupRucValidation(form);

    return form;
  }

  private setupRucValidation(form: FormGroup): void {
    const rucControl = form.get('ruc');
    if (rucControl) {
      // Reactivada validación asíncrona con algoritmo correcto
      rucControl.setAsyncValidators([
        rucAsyncValidator(this.rucValidationService, this.empresaId || undefined)
      ]);

      // Escuchar cambios en el RUC para mostrar hints y estado de validación
      rucControl.valueChanges.subscribe(value => {
        this.rucFormatHint = this.rucValidationService.getRucFormatHint(value || '');
        this.rucValidationInProgress = rucControl.pending;
      });

      // Escuchar cambios en el estado del control
      rucControl.statusChanges.subscribe(status => {
        this.rucValidationInProgress = status === 'PENDING';
      });
    }
  }

  get actividadEconomica(): FormGroup {
    return this.empresaForm.get('actividadEconomica') as FormGroup;
  }

  private loadEmpresa(): void {
    if (this.empresaId) {
      this.store.select(selectEmpresaById(this.empresaId)).subscribe(empresa => {
        if (empresa && empresa.id) {
          this.patchFormValues(empresa);
          // El backend devuelve certificadoPath directamente, no dentro de certificado
          this.currentCertificatePath = empresa.certificadoPath || empresa.certificado?.path || null;
          this.currentCertificateExpiration = empresa.certificadoFechaExpiracion || empresa.certificado?.fechaExpiracion || null;
          this.hasExistingCertificate = !!this.currentCertificatePath;

          // Reconfigurar validación del RUC para excluir la empresa actual
          this.setupRucValidation(this.empresaForm);

          // Actualizar estado del formulario después de cargar datos
          this.updateFormDisabledState();

          // Cargar datos de geografía en cascada
          this.loadGeografiaDataForEdit(empresa);
        }
      });
    }
  }

  private async loadGeografiaDataForEdit(empresa: Empresa): Promise<void> {
    try {
      const ciudadId = empresa.ciudadId;

      if (!ciudadId) {
        console.warn('Empresa sin ID de ciudad');
        return;
      }

      // 1. Buscar la ciudad para obtener su distrito y departamento
      const ciudad = await this.sifenService.getCiudadById(ciudadId).toPromise();

      if (!ciudad) {
        console.warn('Ciudad no encontrada:', ciudadId);
        return;
      }

      // 2. Establecer el departamento seleccionado PRIMERO
      this.selectedDepartamento = ciudad.departamentoCodigo;

      // 3. Cargar distritos del departamento y esperar
      const distritos = await this.sifenService.getDistritosByDepartamento(ciudad.departamentoCodigo).toPromise();
      if (distritos && distritos.length > 0) {
        this.distritoOptions = distritos.map(d => ({
          value: d.codigo,
          label: `${d.codigo} - ${d.nombre}`,
          codigo: d.codigo
        }));

        // 4. Establecer el distrito seleccionado
        this.selectedDistrito = ciudad.distritoCodigo;

        // 5. Cargar ciudades del distrito y esperar
        const ciudades = await this.sifenService.getCiudadesByDistrito(ciudad.distritoCodigo).toPromise();
        if (ciudades && ciudades.length > 0) {
          this.ciudadOptions = ciudades.map(c => ({
            value: c.id.toString(),
            label: `${c.codigo} - ${c.nombre}`,
            codigo: c.codigo
          }));

          // 6. Establecer la ciudad seleccionada
          this.selectedCiudad = ciudad.codigo;

          // 7. Cargar barrios de la ciudad y esperar
          const barrios = await this.sifenService.getBarriosByCiudad(ciudad.codigo).toPromise();
          if (barrios && barrios.length > 0) {
            this.barrioOptions = barrios.map(b => ({
              value: b.id.toString(),
              label: `${b.codigo} - ${b.nombre}`,
              codigo: b.codigo
            }));

            // 8. Establecer el barrio seleccionado si existe
            if (empresa.barrioId) {
              const barrioSeleccionado = barrios.find(b => b.id === empresa.barrioId);
              if (barrioSeleccionado) {
                this.selectedBarrio = barrioSeleccionado.codigo;
              }
            }
          }
        }
      }

    } catch (error) {
      console.error('Error cargando datos de geografía para edición:', error);
      this.snackBar.open('Error cargando datos de ubicación', 'Cerrar', { duration: 3000 });
    }
  }

  private patchFormValues(empresa: Empresa): void {
    const actividadEconomica = empresa.actividadEconomica || {};

    this.empresaForm.patchValue({
      razonSocial: empresa.razonSocial || '',
      ruc: empresa.ruc || '',
      tipoContribuyente: empresa.tipoContribuyente || 'PF',
      nombreFantasia: empresa.nombreFantasia || '',
      email: empresa.email || '',
      telefono: empresa.telefono || '',
      direccion: empresa.direccion || '',
      // Domicilio fiscal - IDs directos
      ciudadId: empresa.ciudadId || null,
      barrioId: empresa.barrioId || null,
      domicilioFiscalDireccion: empresa.domicilioFiscalDireccion || '',
      actividadEconomica: {
        codigoPrincipal: actividadEconomica.codigoPrincipal || '',
        descripcionPrincipal: actividadEconomica.descripcionPrincipal || '',
        codigosSecundarios: actividadEconomica.codigosSecundarios?.join(', ') || '',
        descripcionesSecundarias: actividadEconomica.descripcionesSecundarias?.join(', ') || ''
      },
      sifenAmbiente: empresa.sifenAmbiente || 'DEV'
    });

    // Cargar actividades secundarias en el array
    if (actividadEconomica.codigosSecundarios && actividadEconomica.descripcionesSecundarias) {
      const codigos = actividadEconomica.codigosSecundarios;
      const descripciones = actividadEconomica.descripcionesSecundarias;

      this.actividadesSecundarias = codigos.map((codigo, index) => ({
        codigo: codigo,
        descripcion: descripciones[index] || ''
      }));
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.selectedFileName = this.selectedFile.name;
      this.showPasswordEdit = false; // Ocultar edición de contraseña cuando se selecciona nuevo archivo

      // Hacer la contraseña obligatoria cuando se selecciona un archivo
      this.empresaForm.get('certificadoPassword')?.setValidators([Validators.required]);
      this.empresaForm.get('certificadoPassword')?.updateValueAndValidity();
    } else {
      // Si no hay archivo, limpiar la validación de contraseña
      this.selectedFile = null;
      this.selectedFileName = null;
      this.empresaForm.get('certificadoPassword')?.clearValidators();
      this.empresaForm.get('certificadoPassword')?.updateValueAndValidity();
    }
  }

  onSubmit(): void {
    if (this.empresaForm.valid && !this.submitting) {
      this.submitting = true;
      const formValue = this.empresaForm.value;

      // Parse comma-separated values for secondary activities
      const actividadesSec = this.procesarActividadesSecundarias(formValue);

      const actividadEconomica = {
        codigoPrincipal: formValue.actividadEconomica.codigoPrincipal,
        descripcionPrincipal: formValue.actividadEconomica.descripcionPrincipal,
        codigosSecundarios: actividadesSec.codigosSecundarios,
        descripcionesSecundarias: actividadesSec.descripcionesSecundarias
      };

      // Preparar datos para el backend (estructura simplificada)
      // Asegurar que los campos opcionales sean null en lugar de strings vacíos

      // Normalizar RUC: debe tener formato con guión y dígito verificador
      let rucNormalized = formValue.ruc?.trim() || '';
      if (rucNormalized && !rucNormalized.includes('-')) {
        // Si no tiene guión, agregarlo antes del último dígito (asumiendo que es el DV)
        // Esto es un fallback, idealmente el usuario ya ingresó con guión
        const digitsOnly = rucNormalized.replace(/\D/g, '');
        if (digitsOnly.length >= 7) {
          const base = digitsOnly.slice(0, -1);
          const dv = digitsOnly.slice(-1);
          rucNormalized = `${base}-${dv}`;
        }
      }

      // Validar que el RUC tenga el formato correcto antes de enviar
      if (rucNormalized && !/^\d{6,8}-\d$/.test(rucNormalized)) {
        this.snackBar.open('El RUC debe tener formato válido con guión y dígito verificador (ej: 12345678-9)', 'Cerrar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.submitting = false;
        this.empresaForm.get('ruc')?.markAsTouched();
        return;
      }

      const empresaData: any = {
        razonSocial: formValue.razonSocial?.trim() || '',
        ruc: rucNormalized,
        tipoContribuyente: formValue.tipoContribuyente || 'PF',
        nombreFantasia: formValue.nombreFantasia?.trim() || null,
        email: formValue.email?.trim() || null,
        telefono: formValue.telefono?.trim() || null,
        direccion: formValue.direccion?.trim() || null,
        tipoSociedad: null,
        ciudadId: formValue.ciudadId,
        barrioId: formValue.barrioId || null,
        domicilioFiscalDireccion: formValue.domicilioFiscalDireccion?.trim() || '',
        actividadEconomica: actividadEconomica,
        sifenAmbiente: formValue.sifenAmbiente || 'DEV',
        activo: true
      };

      // Validación adicional: asegurar que ciudadId está presente
      if (!empresaData.ciudadId) {
        this.snackBar.open('La ciudad es requerida para el domicilio fiscal', 'Cerrar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.submitting = false;
        this.empresaForm.get('ciudadId')?.markAsTouched();
        return;
      }

      // Obtener certificado si hay uno seleccionado
      const certificadoFile = this.selectedFile;
      const certificadoPassword = formValue.certificadoPassword;
      const certificadoPasswordNueva = formValue.certificadoPasswordNueva;

      if (this.isEditMode && this.empresaId) {
        // Si hay un nuevo archivo, subirlo junto con la empresa
        if (certificadoFile && certificadoPassword) {
          this.store.dispatch(EmpresasActions.updateEmpresa({
            id: this.empresaId,
            empresa: empresaData,
            certificadoFile: certificadoFile,
            certificadoPassword: certificadoPassword
          }));
        } else if (certificadoPasswordNueva && this.hasExistingCertificate) {
          // Si solo se cambia la contraseña sin archivo nuevo
          this.actualizarSoloPassword(certificadoPasswordNueva);
        } else {
          // Actualizar empresa sin tocar el certificado
          this.store.dispatch(EmpresasActions.updateEmpresa({
            id: this.empresaId,
            empresa: empresaData
          }));
        }
      } else {
        // Modo creación: crear empresa con certificado opcional en el mismo request
        this.store.dispatch(EmpresasActions.createEmpresa({
          empresa: empresaData as Empresa,
          certificadoFile: certificadoFile || undefined,
          certificadoPassword: certificadoPassword || undefined
        }));
      }

      // La redirección se maneja en el subscriber de loading state existente
    } else {
      // Marcar todos los campos como touched para mostrar errores
      Object.keys(this.empresaForm.controls).forEach(key => {
        const control = this.empresaForm.get(key);
        control?.markAsTouched();

        if (control instanceof FormGroup) {
          Object.keys(control.controls).forEach(subKey => {
            control.get(subKey)?.markAsTouched();
          });
        }
      });

      this.snackBar.open('Por favor, complete todos los campos requeridos', 'Cerrar', {
        duration: 4000,
        panelClass: ['error-snackbar']
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/empresas']);
  }

  onGoBack(): void {
    this.router.navigate(['/empresas']);
  }

  onEdit(): void {
    if (this.empresaId) {
      this.isViewMode = false;
      this.isEditMode = true;
      this.updateFormDisabledState();
      this.router.navigate(['/empresas', this.empresaId, 'edit']);
    }
  }

  /**
   * Actualiza el estado disabled/enabled de todos los controles del formulario
   * según el modo de vista actual
   */
  private updateFormDisabledState(): void {
    Object.keys(this.empresaForm.controls).forEach(key => {
      const control = this.empresaForm.get(key);
      if (control) {
        if (control instanceof FormGroup) {
          // Si es un FormGroup, actualizar sus controles
          Object.keys(control.controls).forEach(subKey => {
            const subControl = control.get(subKey);
            if (subControl) {
              if (this.isViewMode) {
                subControl.disable({ emitEvent: false });
              } else {
                subControl.enable({ emitEvent: false });
              }
            }
          });
        } else {
          // Si es un FormControl simple
          if (this.isViewMode) {
            control.disable({ emitEvent: false });
          } else {
            control.enable({ emitEvent: false });
          }
        }
      }
    });
  }

  getPageTitle(): string {
    if (this.isViewMode) {
      return 'Ver Empresa';
    } else if (this.isEditMode) {
      return 'Editar Empresa';
    } else {
      return 'Nueva Empresa';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onRucInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let formattedValue = this.rucValidationService.formatRucInput(input.value);

    // Asegurar que si el usuario está escribiendo dígitos, se formatee correctamente
    // Si tiene 6-8 dígitos sin guión, sugerir agregar guión
    const cleaned = formattedValue.replace(/[^\d]/g, '');
    if (cleaned.length >= 6 && cleaned.length <= 8 && !formattedValue.includes('-')) {
      // No agregar guión automáticamente, solo formatear lo que el usuario escriba
    }

    // Actualizar el valor del input y del formulario
    input.value = formattedValue;
    this.empresaForm.get('ruc')?.setValue(formattedValue, { emitEvent: true });
  }

  // Uppercase input handler
  onUppercaseInput(event: Event, controlName: string, groupName?: string): void {
    const input = event.target as HTMLInputElement;
    const uppercaseValue = input.value.toUpperCase();

    setTimeout(() => {
      if (groupName) {
        this.empresaForm.get(groupName)?.get(controlName)?.setValue(uppercaseValue);
      } else {
        this.empresaForm.get(controlName)?.setValue(uppercaseValue);
      }
      input.value = uppercaseValue;
    });
  }

  // SIFEN handlers - Orden: Departamento → Distrito → Ciudad → Barrio

  onDepartamentoSelected(option: AutocompleteOption): void {
    const codigo = option.codigo || option.value;
    this.selectedDepartamento = codigo;

    // Solo actualizar las opciones dependientes, no necesitamos guardar el departamento

    // Reset campos dependientes
    this.resetDistritoAndBelow();

    // Cargar distritos del departamento seleccionado
    if (codigo) {
      this.sifenService.getDistritosByDepartamento(codigo).subscribe({
        next: (distritos) => {
          this.distritoOptions = distritos.map(d => ({
            value: d.codigo,
            label: `${d.codigo} - ${d.nombre}`,
            codigo: d.codigo
          }));
        },
        error: (error) => {
          console.error('Error cargando distritos:', error);
          this.snackBar.open('Error cargando distritos', 'Cerrar', { duration: 3000 });
        }
      });
    }
  }

  onDistritoSelected(option: AutocompleteOption): void {
    const codigo = option.codigo || option.value;
    this.selectedDistrito = codigo;

    // Reset campos dependientes
    this.resetCiudadAndBelow();

    // Cargar ciudades del distrito seleccionado
    if (codigo) {
      this.sifenService.getCiudadesByDistrito(codigo).subscribe({
        next: (ciudades) => {
          this.ciudadOptions = ciudades.map(c => ({
            value: c.id.toString(),
            label: `${c.codigo} - ${c.nombre}`,
            codigo: c.codigo
          }));
        },
        error: (error) => {
          console.error('Error cargando ciudades:', error);
          this.snackBar.open('Error cargando ciudades', 'Cerrar', { duration: 3000 });
        }
      });
    }
  }

  onCiudadSelected(option: AutocompleteOption): void {
    const codigo = option.codigo || option.value;
    this.selectedCiudad = codigo;

    // Buscar la ciudad seleccionada para obtener su ID
    const ciudad = this.ciudadOptions.find(c => c.codigo === codigo);
    if (ciudad) {
      this.empresaForm.get('ciudadId')?.setValue(parseInt(ciudad.value));
      this.empresaForm.get('ciudadId')?.markAsTouched();
    }

    // Reset barrio
    this.resetBarrio();

    // Cargar barrios de la ciudad seleccionada
    if (codigo) {
      this.sifenService.getBarriosByCiudad(codigo).subscribe({
        next: (barrios) => {
          this.barrioOptions = barrios.map(b => ({
            value: b.id.toString(),
            label: `${b.codigo} - ${b.nombre}`,
            codigo: b.codigo
          }));
        },
        error: (error) => {
          console.error('Error cargando barrios:', error);
          this.snackBar.open('Error cargando barrios', 'Cerrar', { duration: 3000 });
        }
      });
    }
  }

  onBarrioSelected(option: AutocompleteOption): void {
    const codigo = option.codigo || option.value;
    this.selectedBarrio = codigo;

    // Buscar el barrio seleccionado para obtener su ID
    const barrio = this.barrioOptions.find(b => b.codigo === codigo);
    if (barrio) {
      this.empresaForm.get('barrioId')?.setValue(parseInt(barrio.value));
      this.empresaForm.get('barrioId')?.markAsTouched();
    }
  }

  // Reset helpers
  private resetDistritoAndBelow(): void {
    this.selectedDistrito = '';
    this.distritoOptions = [];
    this.resetCiudadAndBelow();
  }

  private resetCiudadAndBelow(): void {
    this.selectedCiudad = '';
    this.ciudadOptions = [];
    this.empresaForm.get('ciudadId')?.setValue(null);
    this.resetBarrio();
  }

  private resetBarrio(): void {
    this.selectedBarrio = '';
    this.barrioOptions = [];
    this.empresaForm.get('barrioId')?.setValue(null);
  }

  // Actividades Económicas Secundarias
  agregarActividadSecundaria(): void {
    this.actividadesSecundarias.push({ codigo: '', descripcion: '' });
  }

  eliminarActividadSecundaria(index: number): void {
    this.actividadesSecundarias.splice(index, 1);
    this.onActividadSecundariaChange();
  }

  onActividadSecundariaChange(): void {
    // Actualizar los campos del formulario con los valores de las actividades secundarias
    const codigos = this.actividadesSecundarias
      .filter(a => a.codigo.trim() !== '')
      .map(a => a.codigo.trim());

    const descripciones = this.actividadesSecundarias
      .filter(a => a.descripcion.trim() !== '')
      .map(a => a.descripcion.trim());

    this.actividadEconomica.get('codigosSecundarios')?.setValue(codigos.join(', '));
    this.actividadEconomica.get('descripcionesSecundarias')?.setValue(descripciones.join(', '));
  }

  /**
   * Helper para procesar actividades económicas secundarias desde el formulario
   * Convierte strings separados por comas a arrays, asegurando que siempre sean arrays
   */
  private procesarActividadesSecundarias(formValue: any): { codigosSecundarios: string[], descripcionesSecundarias: string[] } {
    const codigosSecStr = formValue.actividadEconomica?.codigosSecundarios || '';
    const descripcionesSecStr = formValue.actividadEconomica?.descripcionesSecundarias || '';

    const codigosSecundarios = codigosSecStr.trim()
      ? codigosSecStr.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0)
      : [];

    const descripcionesSecundarias = descripcionesSecStr.trim()
      ? descripcionesSecStr.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0)
      : [];

    return { codigosSecundarios, descripcionesSecundarias };
  }

  /**
   * Actualiza solo la contraseña del certificado existente sin cambiar el archivo
   */
  private actualizarSoloPassword(nuevaPassword: string): void {
    if (!this.empresaId || !nuevaPassword) {
      return;
    }

    this.empresaApiService.actualizarPasswordCertificado(this.empresaId, nuevaPassword).subscribe({
      next: () => {
        this.snackBar.open('Contraseña del certificado actualizada correctamente', 'Cerrar', {
          duration: 3000
        });
        this.store.dispatch(EmpresasActions.loadEmpresas());
        this.showPasswordEdit = false;
        this.empresaForm.get('certificadoPasswordNueva')?.reset();

        // También actualizar la empresa si se está editando
        if (this.isEditMode && this.empresaId) {
          const formValue = this.empresaForm.value;
          const actividadesSec = this.procesarActividadesSecundarias(formValue);

          const empresaData: any = {
            razonSocial: formValue.razonSocial?.trim() || '',
            ruc: formValue.ruc?.trim() || '',
            tipoContribuyente: formValue.tipoContribuyente || 'PF',
            nombreFantasia: formValue.nombreFantasia?.trim() || null,
            email: formValue.email?.trim() || null,
            telefono: formValue.telefono?.trim() || null,
            direccion: formValue.direccion?.trim() || null,
            tipoSociedad: null,
            ciudadId: formValue.ciudadId,
            barrioId: formValue.barrioId || null,
            domicilioFiscalDireccion: formValue.domicilioFiscalDireccion?.trim() || '',
            actividadEconomica: {
              codigoPrincipal: formValue.actividadEconomica.codigoPrincipal,
              descripcionPrincipal: formValue.actividadEconomica.descripcionPrincipal,
              codigosSecundarios: actividadesSec.codigosSecundarios,
              descripcionesSecundarias: actividadesSec.descripcionesSecundarias
            },
            activo: true
          };

          this.store.dispatch(EmpresasActions.updateEmpresa({
            id: this.empresaId!,
            empresa: empresaData
          }));
        }
      },
      error: (error) => {
        console.error('Error al actualizar contraseña del certificado:', error);
        this.snackBar.open(`Error al actualizar contraseña: ${error.error?.message || error.message}`, 'Cerrar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.submitting = false;
      }
    });
  }

}
