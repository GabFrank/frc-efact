import { Component, Inject, OnInit } from '@angular/core';
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
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Nombre</mat-label>
            <input matInput formControlName="nombre" placeholder="Nombre del cliente">
            <app-error-message [control]="form.get('nombre')" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Razón Social</mat-label>
            <input matInput formControlName="razonSocial" placeholder="Razón social">
            <app-error-message [control]="form.get('razonSocial')" />
          </mat-form-field>
        </div>

        <div class="form-row">
          <div class="checkbox-field">
            <mat-checkbox formControlName="tributa" (change)="onTributaChange()">
              Tributa
            </mat-checkbox>
          </div>

          <mat-form-field appearance="outline">
            <mat-label>Tipo de Contribuyente</mat-label>
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
          <mat-label>RUC</mat-label>
          <input matInput formControlName="ruc" placeholder="RUC del cliente">
          <mat-hint *ngIf="form.get('tributa')?.value">
            Requerido cuando el cliente tributa
          </mat-hint>
          <app-error-message [control]="form.get('ruc')" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Dirección</mat-label>
          <textarea matInput 
                    formControlName="direccion" 
                    placeholder="Dirección completa"
                    rows="2"></textarea>
          <app-error-message [control]="form.get('direccion')" />
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Teléfono</mat-label>
            <input matInput formControlName="telefono" placeholder="Teléfono de contacto">
            <app-error-message [control]="form.get('telefono')" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Email</mat-label>
            <input matInput 
                   type="email" 
                   formControlName="email" 
                   placeholder="correo@ejemplo.com">
            <app-error-message [control]="form.get('email')" />
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
export class ClienteFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private clienteApi: ClienteApiService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ClienteFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { cliente: Cliente | null }
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
        [Validators.maxLength(200)]
      ],
      ruc: [
        this.data.cliente?.ruc || '', 
        [Validators.maxLength(20)]
      ],
      direccion: [this.data.cliente?.direccion || ''],
      telefono: [
        this.data.cliente?.telefono || '', 
        [Validators.maxLength(50)]
      ],
      email: [
        this.data.cliente?.email || '', 
        [Validators.email, Validators.maxLength(100)]
      ],
      tributa: [this.data.cliente?.tributa ?? true],
      tipoContribuyente: [this.data.cliente?.tipoContribuyente || null],
      activo: [this.data.cliente?.activo ?? true]
    });

    // Configurar validación condicional de RUC
    this.setupConditionalValidation();
  }

  setupConditionalValidation(): void {
    const rucControl = this.form.get('ruc');
    const tributaControl = this.form.get('tributa');

    // Actualizar validación cuando cambia el valor de tributa
    tributaControl?.valueChanges.subscribe(tributa => {
      if (tributa) {
        rucControl?.setValidators([Validators.required, Validators.maxLength(20)]);
      } else {
        rucControl?.setValidators([Validators.maxLength(20)]);
      }
      rucControl?.updateValueAndValidity();
    });

    // Aplicar validación inicial
    if (tributaControl?.value) {
      rucControl?.setValidators([Validators.required, Validators.maxLength(20)]);
      rucControl?.updateValueAndValidity();
    }
  }

  onTributaChange(): void {
    // La validación se actualiza automáticamente por el valueChanges subscription
    // Este método está aquí por si necesitamos lógica adicional en el futuro
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      
      // Mostrar mensaje específico si falta RUC cuando tributa
      if (this.form.get('tributa')?.value && !this.form.get('ruc')?.value) {
        this.snackBar.open(
          'El RUC es requerido cuando el cliente tributa',
          'Cerrar',
          { duration: 3000 }
        );
      }
      
      return;
    }

    this.saving = true;
    const clienteData: Partial<Cliente> = {
      ...this.form.value,
      empresaId: 1 // TODO: Obtener del state
    };

    const request = this.isEdit
      ? this.clienteApi.update(this.data.cliente!.id, clienteData)
      : this.clienteApi.create(clienteData);

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
        this.snackBar.open(
          error.error?.message || `Error al ${this.isEdit ? 'actualizar' : 'crear'} cliente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.saving = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
