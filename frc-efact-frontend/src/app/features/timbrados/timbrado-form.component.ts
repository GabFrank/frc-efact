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
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Timbrado } from '../../models/timbrado.model';
import { Empresa } from '../../models/empresa.model';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { TimbradoDetalleDialogComponent, TimbradoDetalleDialogData } from './timbrado-detalle-dialog.component';
import { TimbradoDetalle } from '../../models/timbrado.model';
import * as TimbradoDetallesActions from '../../core/state/timbrado-detalles/timbrado-detalles.actions';
import { selectDetallesByTimbrado, selectDetallesLoading } from '../../core/state/timbrado-detalles/timbrado-detalles.selectors';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

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
    MatTableModule,
    MatChipsModule,
    MatMenuModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="timbrado-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <button mat-icon-button (click)="onCancel()" matTooltip="Volver">
                <mat-icon>arrow_back</mat-icon>
              </button>
              <h2>{{ getFormTitle() }}</h2>
              <!-- Debug info -->
              <div style="font-size: 12px; color: #666; margin-top: 8px;">
                Debug: isViewMode={{ isViewMode }}, isEditMode={{ isEditMode }}, timbradoId={{ timbradoId }}
              </div>
            </div>
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

            <!-- Información de Empresa Section -->
            <div class="form-section" *ngIf="empresaSeleccionada">
              <h3>Empresa</h3>
              <mat-card class="empresa-info-card">
                <mat-card-content>
                  <div class="empresa-info">
                    <div class="empresa-details">
                      <h4>{{ empresaSeleccionada.razonSocial }}</h4>
                      <p class="ruc-info">
                        <mat-icon>business</mat-icon>
                        RUC: {{ empresaSeleccionada.ruc }}
                      </p>
                      <p class="empresa-type" *ngIf="empresaSeleccionada.tipoContribuyente">
                        <mat-icon>person</mat-icon>
                        {{ empresaSeleccionada.tipoContribuyente === 'PF' ? 'Persona Física' : 'Persona Jurídica' }}
                      </p>
                    </div>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>

            <!-- Datos del Timbrado Section -->
            <div class="form-section">
              <h3>Datos del Timbrado</h3>

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
                  <mat-error *ngIf="timbradoForm.get('numero')?.hasError('maxlength')">
                    Máximo 20 caracteres
                  </mat-error>
                </mat-form-field>

                <div class="checkbox-container">
                  <mat-checkbox formControlName="isElectronico" class="checkbox-field">
                    Timbrado Electrónico
                  </mat-checkbox>
                </div>
              </div>

              <!-- CSC Field (conditional) -->
              <div class="form-row" *ngIf="timbradoForm.get('isElectronico')?.value">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>CSC (Código de Seguridad del Contribuyente)</mat-label>
                  <input matInput
                         formControlName="csc"
                         [type]="showCsc ? 'text' : 'password'"
                         placeholder="Código de seguridad"
                         [readonly]="isViewMode">
                  <button mat-icon-button
                          matSuffix
                          type="button"
                          (click)="toggleCscVisibility()"
                          [attr.aria-label]="'Mostrar/ocultar CSC'"
                          [attr.aria-pressed]="showCsc">
                    <mat-icon>{{ showCsc ? 'visibility_off' : 'visibility' }}</mat-icon>
                  </button>
                  <mat-hint>
                    <span *ngIf="isViewMode">
                      CSC configurado
                    </span>
                    <span *ngIf="isEditMode">
                      Deje vacío para mantener el CSC actual, o ingrese uno nuevo
                    </span>
                    <span *ngIf="!isEditMode && !isViewMode">
                      Este código será encriptado al guardarse
                    </span>
                  </mat-hint>
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

            <!-- Estado Section -->
            <div class="form-section">
              <h3>Estado</h3>

              <div class="form-row">
                <mat-checkbox formControlName="activo" class="checkbox-field">
                  Timbrado Activo
                </mat-checkbox>
              </div>
            </div>

            <!-- Puntos de Expedición Section (solo para timbrados existentes) -->
            <div class="form-section" *ngIf="timbradoId">
              <mat-divider></mat-divider>
              <h3>Puntos de Expedición</h3>

              <!-- Actions Bar -->
              <div class="detalles-actions-bar">
                <button
                  mat-raised-button
                  color="primary"
                  (click)="onAddDetalle()"
                  [disabled]="isViewMode">
                  <mat-icon>add</mat-icon>
                  Agregar Punto de Expedición
                </button>
              </div>

              <!-- Loading State -->
              <app-loading-spinner *ngIf="detallesLoading$ | async"></app-loading-spinner>

              <!-- Detalles Table -->
              <div class="detalles-table-container" *ngIf="!(detallesLoading$ | async)">
                <table mat-table [dataSource]="(detalles$ | async)!" class="detalles-table">

                  <!-- Punto Expedición Column -->
                  <ng-container matColumnDef="puntoExpedicion">
                    <th mat-header-cell *matHeaderCellDef>Punto Expedición</th>
                    <td mat-cell *matCellDef="let detalle">{{ detalle.puntoExpedicion }}</td>
                  </ng-container>

                  <!-- Establecimiento Column -->
                  <ng-container matColumnDef="codigoEstablecimientoFactura">
                    <th mat-header-cell *matHeaderCellDef>Establecimiento</th>
                    <td mat-cell *matCellDef="let detalle">{{ detalle.codigoEstablecimientoFactura }}</td>
                  </ng-container>

                  <!-- Rango Column -->
                  <ng-container matColumnDef="rango">
                    <th mat-header-cell *matHeaderCellDef>Rango</th>
                    <td mat-cell *matCellDef="let detalle">
                      {{ detalle.rangoDesde }} - {{ detalle.rangoHasta }}
                    </td>
                  </ng-container>

                  <!-- Disponibles Column -->
                  <ng-container matColumnDef="disponibles">
                    <th mat-header-cell *matHeaderCellDef>Disponibles</th>
                    <td mat-cell *matCellDef="let detalle">
                      <span class="disponibles-text">{{ detalle.numerosDisponibles || 0 }}</span>
                    </td>
                  </ng-container>

                  <!-- Porcentaje Utilizado Column -->
                  <ng-container matColumnDef="porcentajeUtilizado">
                    <th mat-header-cell *matHeaderCellDef>% Utilizado</th>
                    <td mat-cell *matCellDef="let detalle">
                      <mat-chip
                        [class.porcentaje-bajo]="(detalle.porcentajeUtilizado || 0) < 50"
                        [class.porcentaje-medio]="(detalle.porcentajeUtilizado || 0) >= 50 && (detalle.porcentajeUtilizado || 0) < 80"
                        [class.porcentaje-alto]="(detalle.porcentajeUtilizado || 0) >= 80">
                        {{ (detalle.porcentajeUtilizado || 0).toFixed(1) }}%
                      </mat-chip>
                    </td>
                  </ng-container>

                  <!-- Estado Column -->
                  <ng-container matColumnDef="estado">
                    <th mat-header-cell *matHeaderCellDef>Estado</th>
                    <td mat-cell *matCellDef="let detalle">
                      <mat-chip [class.activo-chip]="detalle.activo" [class.inactivo-chip]="!detalle.activo">
                        {{ detalle.activo ? 'Activo' : 'Inactivo' }}
                      </mat-chip>
                    </td>
                  </ng-container>

                  <!-- Actions Column -->
                  <ng-container matColumnDef="actions">
                    <th mat-header-cell *matHeaderCellDef>Acciones</th>
                    <td mat-cell *matCellDef="let detalle">
                      <button
                        mat-icon-button
                        [matMenuTriggerFor]="detalleActionsMenu"
                        matTooltip="Acciones"
                        [disabled]="isViewMode">
                        <mat-icon>more_vert</mat-icon>
                      </button>

                      <mat-menu #detalleActionsMenu="matMenu">
                        <button mat-menu-item (click)="onEditDetalle(detalle)">
                          <mat-icon>edit</mat-icon>
                          <span>Editar</span>
                        </button>
                        <mat-divider></mat-divider>
                        <button
                          mat-menu-item
                          (click)="onDeleteDetalle(detalle)"
                          class="delete-option">
                          <mat-icon>delete</mat-icon>
                          <span>Desactivar</span>
                        </button>
                      </mat-menu>
                    </td>
                  </ng-container>

                  <tr mat-header-row *matHeaderRowDef="detallesDisplayedColumns"></tr>
                  <tr mat-row *matRowDef="let row; columns: detallesDisplayedColumns;"></tr>

                  <!-- No Data Row -->
                  <tr class="mat-row" *matNoDataRow>
                    <td class="mat-cell no-data" [attr.colspan]="detallesDisplayedColumns.length">
                      <div class="no-data-message">
                        <mat-icon>location_on</mat-icon>
                        <p>No hay puntos de expedición configurados</p>
                        <button mat-raised-button color="primary" (click)="onAddDetalle()" [disabled]="isViewMode">
                          <mat-icon>add</mat-icon>
                          Agregar primer punto de expedición
                        </button>
                      </div>
                    </td>
                  </tr>
                </table>
              </div>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
              <button mat-button type="button" (click)="onCancel()">
                <mat-icon>cancel</mat-icon>
                Cancelar
              </button>

              <!-- Botón Editar (solo en modo vista) -->
              <button mat-raised-button color="primary" *ngIf="isViewMode" (click)="onEdit()">
                <mat-icon>edit</mat-icon>
                Editar Timbrado
              </button>

              <!-- Botón Guardar (solo en modo crear/editar) -->
              <button mat-raised-button color="primary" *ngIf="!isViewMode" type="submit" [disabled]="timbradoForm.invalid || saving">
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

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
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

    .checkbox-container {
      display: flex;
      align-items: center;
      padding-top: 8px;
    }

    .empresa-info-card {
      margin-bottom: 16px;
      background-color: #f8f9fa;
      border-left: 4px solid #3f51b5;
    }

    .empresa-info {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .empresa-details h4 {
      margin: 0 0 8px 0;
      font-size: 18px;
      font-weight: 500;
      color: #333;
    }

    .ruc-info, .empresa-type {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 4px 0;
      font-size: 14px;
      color: #666;
    }

    .ruc-info mat-icon, .empresa-type mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
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

    /* Detalles Section Styles */
    .detalles-actions-bar {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 20px;
    }

    .detalles-table-container {
      overflow-x: auto;
    }

    .detalles-table {
      width: 100%;
      background: white;
    }

    .detalles-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .detalles-table td,
    .detalles-table th {
      padding: 12px 16px;
    }

    .disponibles-text {
      font-weight: 500;
      color: #1976d2;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    .porcentaje-bajo {
      background-color: #4caf50 !important;
      color: white;
    }

    .porcentaje-medio {
      background-color: #ff9800 !important;
      color: white;
    }

    .porcentaje-alto {
      background-color: #f44336 !important;
      color: white;
    }

    .activo-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    .inactivo-chip {
      background-color: #f44336 !important;
      color: white;
    }

    .no-data {
      text-align: center;
      padding: 40px !important;
    }

    .no-data-message {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      color: #666;
    }

    .no-data-message > mat-icon:first-child {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }

    .no-data-message button mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .no-data-message p {
      margin: 0;
      font-size: 16px;
    }

    /* Menu styles */
    .mat-mdc-menu-item {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .mat-mdc-menu-item mat-icon {
      margin-right: 0;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .delete-option {
      color: #f44336;
    }

    @media (max-width: 768px) {
      .form-row {
        flex-direction: column;
      }

      .half-width {
        width: 100%;
      }

      .detalles-actions-bar {
        justify-content: center;
      }
    }
  `]
})
export class TimbradoFormComponent implements OnInit {
  timbradoForm: FormGroup;
  empresas: Empresa[] = [];
  empresaSeleccionada: Empresa | null = null;
  isEditMode = false;
  isViewMode = false;
  timbradoId: number | null = null;
  loading = false;
  saving = false;
  error: string | null = null;
  showCsc = false; // Para controlar la visibilidad del CSC

  // Detalles properties
  detallesDisplayedColumns: string[] = ['puntoExpedicion', 'codigoEstablecimientoFactura', 'rango', 'disponibles', 'porcentajeUtilizado', 'estado', 'actions'];
  detallesLoading$ = this.store.select(selectDetallesLoading);

  get detalles$() {
    return this.store.select(selectDetallesByTimbrado(this.timbradoId || 0));
  }

  constructor(
    private fb: FormBuilder,
    private timbradoApiService: TimbradoApiService,
    private empresaApiService: EmpresaApiService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private store: Store
  ) {
    this.timbradoForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadEmpresas();

    // Check if we're in edit or view mode using route snapshot
    const urlSegments = this.route.snapshot.url;
    const url = urlSegments.map(segment => segment.path).join('/');

    console.log('URL segments:', urlSegments);
    console.log('URL:', url);

    if (url.includes('edit')) {
      this.isEditMode = true;
      this.isViewMode = false;
      console.log('Modo: EDICIÓN');
    } else if (url.match(/^\d+$/)) {
      this.isEditMode = false;
      this.isViewMode = true;
      console.log('Modo: VISTA');
    } else {
      this.isEditMode = false;
      this.isViewMode = false;
      console.log('Modo: CREACIÓN');
    }

    // Extract timbrado ID from URL
    const idMatch = url.match(/(\d+)/);
    if (idMatch) {
      this.timbradoId = +idMatch[1];
      console.log('Timbrado ID:', this.timbradoId);
      this.loadTimbrado(this.timbradoId);
      this.loadDetalles();
    }

    // Disable form in view mode
    if (this.isViewMode) {
      this.timbradoForm.disable();
      console.log('Formulario DESHABILITADO');
    }

    // Check for empresaId from query params (when coming from empresa-timbrados)
    this.route.queryParams.subscribe(queryParams => {
      if (queryParams['empresaId']) {
        const empresaId = +queryParams['empresaId'];
        this.loadEmpresaSeleccionada(empresaId);
        this.timbradoForm.patchValue({ empresaId: empresaId });
      }
    });

    // Add custom validator for fecha fin
    this.timbradoForm.setValidators(this.fechaFinValidator.bind(this));
  }

  private createForm(): FormGroup {
    return this.fb.group({
      empresaId: [null, Validators.required],
      numero: ['', [Validators.required, Validators.pattern(/^\d+$/), Validators.maxLength(20)]],
      isElectronico: [false],
      csc: [''],
      fechaInicio: [null, Validators.required],
      fechaFin: [null, Validators.required],
      activo: [true]
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

  private loadEmpresaSeleccionada(empresaId: number): void {
    this.empresaApiService.getById(empresaId).subscribe({
      next: (empresa) => {
        this.empresaSeleccionada = empresa;
      },
      error: (err) => {
        this.error = 'Error al cargar la información de la empresa';
        console.error('Error loading empresa:', err);
      }
    });
  }

  private loadTimbrado(id: number): void {
    this.loading = true;
    this.timbradoApiService.getById(id).subscribe({
      next: (timbrado) => {
        this.timbradoForm.patchValue({
          empresaId: timbrado.empresaId,
          numero: timbrado.numero,
          isElectronico: timbrado.isElectronico,
          fechaInicio: new Date(timbrado.fechaInicio),
          fechaFin: new Date(timbrado.fechaFin),
          activo: timbrado.activo
        });

        // Cargar CSC si existe
        if (timbrado.isElectronico && timbrado.csc) {
          this.timbradoForm.patchValue({
            csc: timbrado.csc
          });
        }

        // Cargar información de la empresa para mostrar en el card
        this.loadEmpresaSeleccionada(timbrado.empresaId);

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
      this.timbradoForm.get('csc')?.markAsTouched();
      return;
    }

    // Validate fecha fin is after fecha inicio
    const fechaInicio = this.timbradoForm.get('fechaInicio')?.value;
    const fechaFin = this.timbradoForm.get('fechaFin')?.value;
    if (fechaInicio && fechaFin && new Date(fechaFin) <= new Date(fechaInicio)) {
      this.timbradoForm.get('fechaFin')?.setErrors({ fechaFinMenor: true });
      this.timbradoForm.get('fechaFin')?.markAsTouched();
      return;
    }

    this.saving = true;
    this.error = null;

    const formValue = this.timbradoForm.value;
    const timbradoData: any = {
      empresaId: formValue.empresaId,
      numero: formValue.numero,
      isElectronico: formValue.isElectronico,
      fechaInicio: this.formatDate(formValue.fechaInicio),
      fechaFin: this.formatDate(formValue.fechaFin),
      activo: formValue.activo
    };

    // Solo incluir CSC si es un timbrado electrónico y se proporcionó un valor
    if (formValue.isElectronico && formValue.csc && formValue.csc.trim() !== '') {
      timbradoData.csc = formValue.csc;
    }

    const request = this.isEditMode && this.timbradoId
      ? this.timbradoApiService.update(this.timbradoId, timbradoData)
      : this.timbradoApiService.create(timbradoData);

    request.subscribe({
      next: () => {
        // Navigate back to empresa timbrados
        const empresaId = this.route.snapshot.queryParams['empresaId'] || formValue.empresaId;
        if (empresaId) {
          this.router.navigate(['/empresas', empresaId, 'timbrados']);
        } else {
          this.router.navigate(['/timbrados']);
        }
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
    // Navigate back to empresa timbrados
    const empresaId = this.route.snapshot.queryParams['empresaId'] || this.timbradoForm.get('empresaId')?.value;
    if (empresaId) {
      this.router.navigate(['/empresas', empresaId, 'timbrados']);
    } else {
      this.router.navigate(['/timbrados']);
    }
  }

  onEdit(): void {
    if (this.timbradoId) {
      this.isViewMode = false;
      this.isEditMode = true;
      this.timbradoForm.enable();
      console.log('Formulario HABILITADO para edición');
      this.router.navigate(['/timbrados', this.timbradoId, 'edit']);
    }
  }

  getFormTitle(): string {
    console.log('getFormTitle - isViewMode:', this.isViewMode, 'isEditMode:', this.isEditMode);
    if (this.isViewMode) {
      return 'Ver Timbrado';
    } else if (this.isEditMode) {
      return 'Editar Timbrado';
    } else {
      return 'Nuevo Timbrado';
    }
  }

  toggleCscVisibility(): void {
    this.showCsc = !this.showCsc;
  }

  // Detalles methods
  private loadDetalles(): void {
    if (this.timbradoId) {
      this.store.dispatch(TimbradoDetallesActions.loadDetallesByTimbrado({ timbradoId: this.timbradoId }));
    }
  }

  onAddDetalle(): void {
    if (!this.timbradoId) return;

    const dialogData: TimbradoDetalleDialogData = {
      timbradoId: this.timbradoId,
      timbradoIsElectronico: this.timbradoForm.get('isElectronico')?.value || false,
      isEditMode: false
    };

    const dialogRef = this.dialog.open(TimbradoDetalleDialogComponent, {
      data: dialogData,
      width: '800px',
      maxWidth: '90vw'
    });

    dialogRef.afterClosed().subscribe(() => {
      // Recargar detalles después de cerrar el diálogo
      this.loadDetalles();
    });
  }

  onEditDetalle(detalle: TimbradoDetalle): void {
    const dialogData: TimbradoDetalleDialogData = {
      detalle: detalle,
      timbradoId: detalle.timbradoId,
      timbradoIsElectronico: this.timbradoForm.get('isElectronico')?.value || false,
      isEditMode: true
    };

    const dialogRef = this.dialog.open(TimbradoDetalleDialogComponent, {
      data: dialogData,
      width: '800px',
      maxWidth: '90vw'
    });

    dialogRef.afterClosed().subscribe(() => {
      // Recargar detalles después de cerrar el diálogo
      this.loadDetalles();
    });
  }

  onDeleteDetalle(detalle: TimbradoDetalle): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Desactivar Punto de Expedición',
        message: `¿Está seguro que desea desactivar el punto de expedición "${detalle.puntoExpedicion}"? Esta acción no se puede deshacer.`,
        confirmText: 'Desactivar',
        cancelText: 'Cancelar',
        confirmColor: 'warn'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.store.dispatch(TimbradoDetallesActions.deleteDetalle({ id: detalle.id }));
        // Recargar detalles después de eliminar
        setTimeout(() => this.loadDetalles(), 1000);
      }
    });
  }
}
