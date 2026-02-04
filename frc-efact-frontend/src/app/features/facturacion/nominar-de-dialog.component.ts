import { Component, Inject, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { SifenApiService } from '../../core/api/sifen-api.service';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { Cliente } from '../../models/cliente.model';
import { AutocompleteSelectComponent, AutocompleteOption } from '../../shared/components/autocomplete-select/autocomplete-select.component';
import { FacturaLegal } from '../../models/factura.model';

export interface NominarDeDialogData {
  factura: FacturaLegal;
  cdc: string;
  empresaId: number;
}

@Component({
  selector: 'app-nominar-de-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    AutocompleteSelectComponent
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>person_add</mat-icon>
      Nominar Receptor del Documento Electrónico
    </h2>

    <mat-dialog-content>
      <div class="dialog-content">
        <div class="info-section">
          <mat-icon color="primary">info</mat-icon>
          <div>
            <h3>Información</h3>
            <p>Está nominando un receptor para el documento electrónico con CDC:</p>
            <code class="cdc-code">{{ data.cdc }}</code>
            <p class="info-text">Seleccione el cliente que será nominado como receptor del documento.</p>
          </div>
        </div>

        <form [formGroup]="form" class="nominar-form">
          <app-autocomplete-select
            [label]="'Cliente'"
            [placeholder]="'Buscar cliente por nombre o RUC...'"
            [options]="clienteOptions()"
            [value]="clienteInputValue()"
            [hasError]="clienteControl.invalid && clienteControl.touched"
            [errorMessage]="'El cliente es obligatorio'"
            [required]="true"
            [showAllOnEmpty]="false"
            (optionSelected)="onClienteOptionSelected($event)"
            (valueChange)="onClienteValueChange($event)"
            (searchChange)="onClienteSearchChange($event)">
          </app-autocomplete-select>

          <div class="cliente-preview" *ngIf="clienteSeleccionado()">
            <mat-icon>person</mat-icon>
            <div class="cliente-info">
              <strong>{{ clienteSeleccionado()?.nombre || clienteSeleccionado()?.razonSocial }}</strong>
              <span *ngIf="clienteSeleccionado()?.ruc">RUC: {{ clienteSeleccionado()?.ruc }}</span>
            </div>
          </div>
        </form>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" [disabled]="submitting()">
        Cancelar
      </button>
      <button
        mat-raised-button
        color="primary"
        (click)="onConfirm()"
        [disabled]="form.invalid || submitting()">
        <mat-spinner *ngIf="submitting()" diameter="20" style="display: inline-block; margin-right: 8px;"></mat-spinner>
        <mat-icon *ngIf="!submitting()">person_add</mat-icon>
        {{ submitting() ? 'Nominando...' : 'Confirmar Nominación' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2 {
      display: flex;
      align-items: center;
      gap: 12px;
      color: #1976d2;
    }

    h2 mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .dialog-content {
      min-width: 500px;
      padding: 16px 0;
    }

    .info-section {
      display: flex;
      gap: 16px;
      background: #e3f2fd;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 24px;
      border-left: 4px solid #1976d2;
    }

    .info-section mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      flex-shrink: 0;
    }

    .info-section h3 {
      margin: 0 0 8px 0;
      color: #1565c0;
    }

    .info-section p {
      margin: 8px 0;
      color: #666;
    }

    .info-text {
      font-weight: 500;
      color: #1976d2 !important;
    }

    .cdc-code {
      display: block;
      background: #f5f5f5;
      padding: 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
      word-break: break-all;
      margin: 8px 0;
      border: 1px solid #ddd;
    }

    .nominar-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .cliente-preview {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: #e8f5e9;
      border-radius: 8px;
      border-left: 4px solid #4caf50;
    }

    .cliente-preview mat-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
      color: #4caf50;
    }

    .cliente-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .cliente-info strong {
      font-size: 16px;
      color: #2e7d32;
    }

    .cliente-info span {
      font-size: 14px;
      color: #666;
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

      .info-section {
        flex-direction: column;
      }
    }
  `]
})
export class NominarDeDialogComponent implements OnInit, OnDestroy {
  protected readonly String = String;
  form: FormGroup;
  clienteControl: FormControl;
  clienteOptions = signal<AutocompleteOption[]>([]);
  clienteSeleccionado = signal<Cliente | null>(null);
  clienteInputValue = signal<string | null>(null);
  submitting = signal(false);

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private sifenApi: SifenApiService,
    private clienteApi: ClienteApiService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<NominarDeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: NominarDeDialogData
  ) {
    this.form = this.fb.group({
      clienteId: [null, Validators.required]
    });

    this.clienteControl = this.form.get('clienteId') as FormControl;
  }

  ngOnInit(): void {
    // No necesitamos setupClienteAutocomplete ya que usamos searchChange
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onClienteSearchChange(searchTerm: string): void {
    // Si hay un cliente seleccionado y el término de búsqueda coincide con el valor del input,
    // no buscar ni limpiar (esto evita buscar cuando se actualiza el input con el nombre del cliente)
    const currentInputValue = this.clienteInputValue();
    if (this.clienteSeleccionado() && currentInputValue) {
      // Verificar si el término de búsqueda coincide con el valor actual o es parte de él
      if (searchTerm === currentInputValue || currentInputValue.toLowerCase().includes(searchTerm.toLowerCase())) {
        return;
      }
    }

    if (!searchTerm || searchTerm.trim().length < 2) {
      this.clienteOptions.set([]);
      // Solo limpiar la selección si el usuario realmente está limpiando el campo
      // y no hay un cliente seleccionado
      if ((!searchTerm || searchTerm.trim() === '') && !this.clienteSeleccionado()) {
        this.clienteControl.setValue(null);
        this.clienteSeleccionado.set(null);
        this.clienteInputValue.set(null);
      }
      return;
    }

    this.clienteApi.buscar(this.data.empresaId, searchTerm.trim())
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (clientes) => {
          const options: AutocompleteOption[] = clientes.map(cliente => ({
            value: String(cliente.id),
            label: cliente.nombre || cliente.razonSocial || 'Sin nombre',
            codigo: cliente.ruc
          }));
          this.clienteOptions.set(options);
        },
        error: (error) => {
          console.error('Error al buscar clientes:', error);
          this.clienteOptions.set([]);
        }
      });
  }

  onClienteOptionSelected(option: AutocompleteOption): void {
    const clienteId = parseInt(option.value, 10);
    this.clienteControl.setValue(clienteId);

    // Actualizar el valor del input con el nombre del cliente (similar a factura-form)
    const displayText = this.displayCliente(option);
    this.clienteInputValue.set(displayText);

    // Buscar el cliente completo para mostrar en preview
    this.clienteApi.getById(this.data.empresaId, clienteId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (cliente) => {
          this.clienteSeleccionado.set(cliente);
          // Actualizar el valor del input con el cliente completo
          this.clienteInputValue.set(this.displayClienteFromCliente(cliente));
        },
        error: () => {
          // Si no se encuentra, al menos mostrar la opción seleccionada
          const cliente: Partial<Cliente> = {
            id: clienteId,
            nombre: option.label,
            ruc: option.codigo
          };
          this.clienteSeleccionado.set(cliente as Cliente);
        }
      });
  }

  displayCliente(option: AutocompleteOption): string {
    const nombre = option.label || 'Sin nombre';
    return option.codigo ? `${nombre} - ${option.codigo}` : nombre;
  }

  displayClienteFromCliente(cliente: Cliente | null): string {
    if (!cliente) return '';
    const nombre = cliente.nombre || cliente.razonSocial || 'Sin nombre';
    return cliente.ruc ? `${nombre} - ${cliente.ruc}` : nombre;
  }

  onClienteValueChange(value: string): void {
    // Solo limpiar si el valor está vacío Y no hay un cliente seleccionado
    // Esto evita que se limpie cuando se selecciona una opción (que emite el ID del cliente)
    if ((!value || value.trim() === '') && !this.clienteSeleccionado()) {
      this.clienteControl.setValue(null);
      this.clienteSeleccionado.set(null);
      this.clienteInputValue.set(null);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.form.invalid || !this.clienteControl.value) {
      this.snackBar.open('Por favor seleccione un cliente', 'Cerrar', { duration: 3000 });
      return;
    }

    const clienteId = this.clienteControl.value;
    this.submitting.set(true);

    this.sifenApi.nominarDocumento(this.data.cdc, clienteId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.snackBar.open('Receptor nominado exitosamente', 'Cerrar', { duration: 4000 });
          this.dialogRef.close(true);
        },
        error: (error) => {
          this.submitting.set(false);
          const errorMessage = error.error?.message || error.message || 'Error al nominar receptor';
          this.snackBar.open(errorMessage, 'Cerrar', { duration: 5000 });
        }
      });
  }
}

