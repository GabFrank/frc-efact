import { Component, Inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { Cliente } from '../../models/cliente.model';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, switchMap, catchError, of } from 'rxjs';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatSnackBarModule,
    ErrorMessageComponent
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Editar Cliente' : 'Nuevo Cliente' }}</h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="cliente-form">
        <mat-form-field appearance="outline">
          <mat-label>Nombre</mat-label>
          <input matInput
                 formControlName="nombre"
                 placeholder="NOMBRE DEL CLIENTE"
                 style="text-transform: uppercase"
                 (input)="onTextInput($event, 'nombre')"
                 (blur)="onNombreBlur()">
          <app-error-message [control]="form.get('nombre')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Razón Social</mat-label>
          <input matInput
                 formControlName="razonSocial"
                 placeholder="RAZÓN SOCIAL"
                 style="text-transform: uppercase"
                 (input)="onTextInput($event, 'razonSocial')">
          <app-error-message [control]="form.get('razonSocial')" />
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>RUC</mat-label>
            <input matInput
                   formControlName="ruc"
                   placeholder="RUC DEL CLIENTE"
                   style="text-transform: uppercase"
                   (input)="onTextInput($event, 'ruc'); onRucInput()"
                   (blur)="onRucBlur()">
            <mat-hint *ngIf="form.get('tipoClienteSifen')?.value === 'PERSONA_FISICA' || form.get('tipoClienteSifen')?.value === 'PERSONA_JURIDICA' || form.get('tipoClienteSifen')?.value === 'GUBERNAMENTAL' || (!form.get('tipoClienteSifen')?.value && form.get('tributa')?.value)">
              Requerido para este tipo de cliente
            </mat-hint>
            <mat-error *ngIf="form.get('ruc')?.hasError('rucDuplicado')">
              Este RUC ya existe para esta empresa
            </mat-error>
            <mat-error *ngIf="rucValidating">
              Verificando RUC...
            </mat-error>
            <app-error-message [control]="form.get('ruc')" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Tipo de Cliente SIFEN</mat-label>
            <mat-select formControlName="tipoClienteSifen" (selectionChange)="onTipoClienteSifenChange()">
              <mat-option [value]="null">Seleccione...</mat-option>
              <mat-option value="PERSONA_FISICA">Persona Física Contribuyente</mat-option>
              <mat-option value="PERSONA_JURIDICA">Persona Jurídica Contribuyente</mat-option>
              <mat-option value="NO_CONTRIBUYENTE">No Contribuyente (Consumidor Final)</mat-option>
              <mat-option value="EXTRANJERO">Cliente Extranjero</mat-option>
              <mat-option value="GUBERNAMENTAL">Entidad Gubernamental</mat-option>
            </mat-select>
            <mat-hint>Según Manual Técnico SIFEN v1.50</mat-hint>
            <app-error-message [control]="form.get('tipoClienteSifen')" />
          </mat-form-field>
        </div>

        <!-- Campos legacy (ocultos o deprecated) -->
        <div class="form-row" style="display: none;">
          <div class="checkbox-field">
            <mat-checkbox formControlName="tributa" (change)="onTributaChange()">
              Tributa
            </mat-checkbox>
          </div>

          <mat-form-field appearance="outline">
            <mat-label>Tipo de Contribuyente (Legacy)</mat-label>
            <mat-select formControlName="tipoContribuyente">
              <mat-option [value]="null">Seleccione...</mat-option>
              <mat-option value="PF">Persona Física</mat-option>
              <mat-option value="PJ">Persona Jurídica</mat-option>
              <mat-option value="EG">Entidad Gubernamental</mat-option>
            </mat-select>
            <app-error-message [control]="form.get('tipoContribuyente')" />
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline">
          <mat-label>Dirección</mat-label>
          <textarea matInput
                    formControlName="direccion"
                    placeholder="DIRECCIÓN COMPLETA"
                    rows="2"
                    style="text-transform: uppercase"
                    (input)="onTextInput($event, 'direccion')"></textarea>
          <app-error-message [control]="form.get('direccion')" />
        </mat-form-field>

        <!-- Campos ocultos temporalmente -->
        <mat-form-field appearance="outline" style="display: none;">
          <mat-label>Número de Casa</mat-label>
          <input matInput formControlName="numeroCasa" placeholder="Número de casa">
          <app-error-message [control]="form.get('numeroCasa')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Celular</mat-label>
          <input matInput
                 formControlName="celular"
                 placeholder="CELULAR DE CONTACTO"
                 style="text-transform: uppercase"
                 (input)="onTextInput($event, 'celular')">
          <app-error-message [control]="form.get('celular')" />
        </mat-form-field>

        <!-- Campo teléfono oculto temporalmente -->
        <mat-form-field appearance="outline" style="display: none;">
          <mat-label>Teléfono</mat-label>
          <input matInput formControlName="telefono" placeholder="Teléfono de contacto">
          <app-error-message [control]="form.get('telefono')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Email</mat-label>
          <input matInput
                 type="email"
                 formControlName="email"
                 placeholder="correo@ejemplo.com"
                 (blur)="onEmailBlur()">
          <app-error-message [control]="form.get('email')" />
        </mat-form-field>

        <!-- Campos país y ciudad ocultos temporalmente -->
        <div class="form-row" style="display: none;">
          <mat-form-field appearance="outline">
            <mat-label>País</mat-label>
            <input matInput
                   type="number"
                   formControlName="paisId"
                   placeholder="ID del país (ej: 1 para Paraguay)">
            <mat-hint>ID numérico del país</mat-hint>
            <app-error-message [control]="form.get('paisId')" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Ciudad</mat-label>
            <input matInput
                   type="number"
                   formControlName="ciudadId"
                   placeholder="ID de la ciudad">
            <mat-hint>ID numérico de la ciudad</mat-hint>
            <app-error-message [control]="form.get('ciudadId')" />
          </mat-form-field>
        </div>

        <div class="checkbox-field" *ngIf="isEdit">
          <mat-checkbox formControlName="activo">
            Activo
          </mat-checkbox>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button mat-raised-button
              color="primary"
              (click)="onSubmit()"
              [disabled]="form.invalid || saving">
        {{ saving ? 'Guardando...' : 'Guardar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .cliente-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 600px;
      padding: 20px 0;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }

    mat-form-field {
      width: 100%;
    }

    .checkbox-field {
      display: flex;
      align-items: center;
      margin: 8px 0;
    }

    mat-dialog-content {
      max-height: 70vh;
      overflow-y: auto;
    }
  `]
})
export class ClienteFormComponent implements OnInit, OnDestroy {
  form!: FormGroup;
  isEdit = false;
  saving = false;
  rucValidating = false;
  private destroy$ = new Subject<void>();
  private rucValidationSubject = new Subject<string>();

  constructor(
    private fb: FormBuilder,
    private clienteApi: ClienteApiService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ClienteFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { cliente: Cliente | null; empresaId?: number | null }
  ) {}

  ngOnInit(): void {
    this.isEdit = !!this.data.cliente;
    this.initForm();
  }

  initForm(): void {
    this.form = this.fb.group({
      nombre: [
        this.data.cliente?.nombre || '',
        [Validators.required, Validators.maxLength(200)]
      ],
      razonSocial: [
        this.data.cliente?.razonSocial || '',
        [Validators.required, Validators.maxLength(200)]
      ],
      ruc: [
        this.data.cliente?.ruc || '',
        [Validators.maxLength(20)]
      ],
      direccion: [this.data.cliente?.direccion || null],
      numeroCasa: [
        this.data.cliente?.numeroCasa || null,
        [Validators.maxLength(50)]
      ],
      telefono: [
        this.data.cliente?.telefono || null,
        [Validators.maxLength(50)]
      ],
      celular: [
        this.data.cliente?.celular || null,
        [Validators.maxLength(50)]
      ],
      email: [
        this.data.cliente?.email || null,
        [] // Validación condicional de email se configurará después
      ],
      tipoClienteSifen: [
        this.data.cliente?.tipoClienteSifen || null,
        [Validators.required]
      ],
      // Campos legacy para compatibilidad
      tributa: [this.data.cliente?.tributa ?? true],
      tipoContribuyente: [null], // Legacy, ya no se usa
      paisId: [this.data.cliente?.paisId || null],
      ciudadId: [this.data.cliente?.ciudadId || null],
      activo: [this.data.cliente?.activo ?? true]
    });

    // Configurar validación condicional de RUC y Email
    this.setupConditionalValidation();

    // Configurar validación en tiempo real del RUC
    this.setupRucRealTimeValidation();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.rucValidationSubject.complete();
  }

  setupConditionalValidation(): void {
    const rucControl = this.form.get('ruc');
    const emailControl = this.form.get('email');
    const tipoClienteSifenControl = this.form.get('tipoClienteSifen');
    const tributaControl = this.form.get('tributa');

    // Validación basada en tipoClienteSifen (prioritario)
    tipoClienteSifenControl?.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(tipoClienteSifen => {
      this.updateRucValidation(rucControl, tipoClienteSifen);
    });

    // Validación basada en tributa (fallback legacy)
    tributaControl?.valueChanges.pipe(
      takeUntil(this.destroy$)
    ).subscribe(tributa => {
      if (!tipoClienteSifenControl?.value) {
        this.updateRucValidation(rucControl, null, tributa);
      }
    });

    // Validación condicional de email: solo validar formato si tiene contenido
    // Inicializar validación de email una vez
    const emailInitialValue = emailControl?.value;
    if (emailInitialValue && emailInitialValue.trim() !== '') {
      emailControl?.setValidators([Validators.email, Validators.maxLength(100)]);
    } else {
      emailControl?.setValidators([Validators.maxLength(100)]);
    }

    // Validar email solo cuando el usuario modifica el campo y pierde el foco
    // No usar valueChanges aquí para evitar loops infinitos

    // Aplicar validación inicial
    const tipoClienteSifen = tipoClienteSifenControl?.value;
    const tributa = tributaControl?.value;
    this.updateRucValidation(rucControl, tipoClienteSifen, tributa);
  }

  private updateRucValidation(rucControl: any, tipoClienteSifen: string | null, tributa?: boolean | null): void {
    let requiereRuc = false;

    if (tipoClienteSifen) {
      // Determinar si requiere RUC según tipoClienteSifen
      requiereRuc = tipoClienteSifen === 'PERSONA_FISICA'
        || tipoClienteSifen === 'PERSONA_JURIDICA'
        || tipoClienteSifen === 'GUBERNAMENTAL';
    } else if (tributa !== undefined && tributa !== null) {
      // Fallback a tributa legacy
      requiereRuc = tributa;
    }

    if (requiereRuc) {
      rucControl?.setValidators([Validators.required, Validators.maxLength(20)]);
    } else {
      rucControl?.setValidators([Validators.maxLength(20)]);
    }
    rucControl?.updateValueAndValidity();
  }

  onTipoClienteSifenChange(): void {
    // Sincronizar campos legacy si es necesario
    const tipoClienteSifen = this.form.get('tipoClienteSifen')?.value;

    if (tipoClienteSifen) {
      // Actualizar tributa según tipoClienteSifen
      const requiereRuc = tipoClienteSifen === 'PERSONA_FISICA'
        || tipoClienteSifen === 'PERSONA_JURIDICA'
        || tipoClienteSifen === 'GUBERNAMENTAL';
      this.form.get('tributa')?.setValue(requiereRuc, { emitEvent: false });

      // tipoContribuyente es legacy y ya no se usa
    }
  }

  onTributaChange(): void {
    // Si se cambia tributa manualmente, intentar inferir tipoClienteSifen
    // (solo si tipoClienteSifen no está definido)
    if (!this.form.get('tipoClienteSifen')?.value) {
      const tributa = this.form.get('tributa')?.value;
      // Este método mantiene compatibilidad pero es mejor usar tipoClienteSifen
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();

      // Mostrar mensaje específico si falta RUC cuando es requerido
      const tipoClienteSifen = this.form.get('tipoClienteSifen')?.value;
      const requiereRuc = tipoClienteSifen === 'PERSONA_FISICA'
        || tipoClienteSifen === 'PERSONA_JURIDICA'
        || tipoClienteSifen === 'GUBERNAMENTAL'
        || (!tipoClienteSifen && this.form.get('tributa')?.value);

      if (requiereRuc && !this.form.get('ruc')?.value) {
        this.snackBar.open(
          'El RUC es requerido para este tipo de cliente',
          'Cerrar',
          { duration: 3000 }
        );
      } else {
        // Mostrar el primer error encontrado
        const firstInvalidField = Object.keys(this.form.controls).find(key => {
          const control = this.form.get(key);
          return control && control.invalid;
        });

        if (firstInvalidField) {
          const control = this.form.get(firstInvalidField);
          const errors = control?.errors;
          let errorMessage = `Error en ${firstInvalidField}`;

          if (errors?.['required']) {
            errorMessage = `${firstInvalidField} es requerido`;
          } else if (errors?.['email']) {
            errorMessage = `${firstInvalidField} tiene un formato de email inválido`;
          } else if (errors?.['maxlength']) {
            errorMessage = `${firstInvalidField} excede el largo máximo`;
          } else if (errors?.['rucDuplicado']) {
            errorMessage = 'Este RUC ya existe para esta empresa';
          }

          this.snackBar.open(errorMessage, 'Cerrar', { duration: 3000 });
        }
      }

      return;
    }

    this.saving = true;
    // Obtener empresaId del data del dialog o usar el del cliente
    const empresaId = this.data.empresaId || this.data.cliente?.empresaId;

    if (!empresaId) {
      this.snackBar.open('Error: No se pudo identificar la empresa', 'Cerrar', { duration: 3000 });
      this.saving = false;
      return;
    }

    // Preparar datos del cliente, limpiando campos vacíos y asegurando valores por defecto
    const formValue = this.form.value;

    // Limpiar valores vacíos y convertir a null/undefined según corresponda
    const nombreValue = formValue.nombre?.trim() || '';
    const razonSocialValue = formValue.razonSocial?.trim() || '';
    const rucValue = formValue.ruc?.trim();
    const direccionValue = formValue.direccion?.trim();
    const numeroCasaValue = formValue.numeroCasa?.trim();
    const telefonoValue = formValue.telefono?.trim();
    const celularValue = formValue.celular?.trim();
    const emailValue = this.validateEmail(formValue.email);

    const clienteData: Partial<Cliente> = {
      nombre: nombreValue,
      razonSocial: razonSocialValue,
      ruc: rucValue || undefined,
      direccion: direccionValue || undefined,
      numeroCasa: numeroCasaValue || undefined,
      telefono: telefonoValue || undefined,
      celular: celularValue || undefined,
      email: emailValue || undefined,
      tipoClienteSifen: formValue.tipoClienteSifen || null,
      // Campos legacy - asegurar valores por defecto
      tributa: formValue.tributa ?? true,
      // tipoContribuyente es legacy y se ignora completamente, pero lo enviamos como null/undefined
      tipoContribuyente: undefined,
      // Campos geográficos - undefined si están vacíos
      paisId: formValue.paisId || undefined,
      ciudadId: formValue.ciudadId || undefined,
      activo: formValue.activo ?? true,
      empresaId
    };

    const request = this.isEdit
      ? this.clienteApi.update(empresaId, this.data.cliente!.id, clienteData)
      : this.clienteApi.create(empresaId, clienteData);

    request.subscribe({
      next: (cliente) => {
        this.snackBar.open(
          `Cliente ${this.isEdit ? 'actualizado' : 'creado'} correctamente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.dialogRef.close(cliente);
      },
      error: (error) => {
        // Intentar obtener el mensaje del error
        let errorMessage = `Error al ${this.isEdit ? 'actualizar' : 'crear'} cliente`;
        if (error && typeof error === 'object') {
          if ('error' in error && error.error) {
            if (error.error.message) {
              errorMessage = error.error.message;
            } else if (error.error.errors) {
              const validationErrors = error.error.errors;
              const errorMessages = Object.keys(validationErrors)
                .map(key => `${key}: ${Array.isArray(validationErrors[key]) ? validationErrors[key].join(', ') : validationErrors[key]}`)
                .join('; ');
              errorMessage = errorMessages || errorMessage;
            }
          } else if ('message' in error) {
            errorMessage = error.message;
          }
        }

        this.snackBar.open(errorMessage, 'Cerrar', { duration: 5000 });
        this.saving = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onTextInput(event: Event, controlName: string): void {
    const input = event.target as HTMLInputElement | HTMLTextAreaElement;
    const control = this.form.get(controlName);
    if (control) {
      control.setValue(input.value.toUpperCase(), { emitEvent: false });
    }
  }

  onNombreBlur(): void {
    const nombreValue = this.form.get('nombre')?.value;
    const razonSocialValue = this.form.get('razonSocial')?.value;

    // Si nombre tiene contenido y razón social está vacío, copiar nombre a razón social
    if (nombreValue && (!razonSocialValue || razonSocialValue.trim() === '')) {
      this.form.get('razonSocial')?.setValue(nombreValue);
    }
  }

  private validateEmail(email: string | null | undefined): string | null {
    if (!email || email.trim() === '') {
      return null;
    }
    const trimmedEmail = email.trim();
    // Validación simple de email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(trimmedEmail)) {
      // Si no es válido, lanzar error o retornar null
      // Por ahora retornamos null y la validación del backend lo manejará
      return null;
    }
    return trimmedEmail;
  }

  setupRucRealTimeValidation(): void {
    const empresaId = this.data.empresaId || this.data.cliente?.empresaId;
    if (!empresaId) {
      return;
    }

    // Configurar validación asíncrona del RUC con debounce
    this.rucValidationSubject.pipe(
      takeUntil(this.destroy$),
      debounceTime(500), // Esperar 500ms después de que el usuario deje de escribir
      distinctUntilChanged(), // Solo validar si el valor cambió
      switchMap(ruc => {
        if (!ruc || ruc.trim() === '') {
          // Si el RUC está vacío, no validar
          return of(false);
        }

        this.rucValidating = true;
        const rucTrimmed = ruc.trim();
        const excluirClienteId = this.isEdit ? this.data.cliente?.id : undefined;

        return this.clienteApi.verificarRucExiste(empresaId, rucTrimmed, excluirClienteId).pipe(
          catchError(() => of(false)) // En caso de error, asumir que no existe
        );
      })
    ).subscribe(existe => {
      this.rucValidating = false;
      const rucControl = this.form.get('ruc');

      if (existe && rucControl?.value && rucControl.value.trim() !== '') {
        rucControl.setErrors({ ...rucControl.errors, rucDuplicado: true });
      } else {
        // Remover error de duplicado si existe
        if (rucControl?.errors?.['rucDuplicado']) {
          const errors = { ...rucControl.errors };
          delete errors['rucDuplicado'];
          const hasOtherErrors = Object.keys(errors).length > 0;
          rucControl.setErrors(hasOtherErrors ? errors : null);
        }
      }
    });
  }

  onRucInput(): void {
    const rucControl = this.form.get('ruc');
    const rucValue = rucControl?.value;

    if (rucValue && rucValue.trim() !== '') {
      // Remover error de duplicado mientras se valida
      if (rucControl?.errors?.['rucDuplicado']) {
        const errors = { ...rucControl.errors };
        delete errors['rucDuplicado'];
        const hasOtherErrors = Object.keys(errors).length > 0;
        rucControl.setErrors(hasOtherErrors ? errors : null);
      }

      this.rucValidationSubject.next(rucValue);
    }
  }

  onRucBlur(): void {
    const rucControl = this.form.get('ruc');
    const rucValue = rucControl?.value;

    // Validar inmediatamente al perder el foco
    if (rucValue && rucValue.trim() !== '') {
      this.rucValidationSubject.next(rucValue);
    }
  }

  onEmailBlur(): void {
    const emailControl = this.form.get('email');
    const emailValue = emailControl?.value;

    // Validar email solo cuando tiene contenido
    if (emailValue && emailValue.trim() !== '') {
      emailControl?.setValidators([Validators.email, Validators.maxLength(100)]);
    } else {
      emailControl?.setValidators([Validators.maxLength(100)]);
    }
    emailControl?.updateValueAndValidity();
  }
}
