import { Component, Inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject, takeUntil } from 'rxjs';
import { DocumentoElectronico } from '../../models/documento-electronico.model';
import { FacturaLegal } from '../../models/factura.model';
import { SifenApiService } from '../../core/api/sifen-api.service';

export interface CancelarDeDialogData {
  factura?: FacturaLegal; // Si viene factura, obtener CDC desde ahí
  documento?: DocumentoElectronico; // O directamente el documento
  cdc?: string; // O directamente el CDC
}

@Component({
  selector: 'app-cancelar-de-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>cancel</mat-icon>
      Cancelar Documento Electrónico
    </h2>

    <mat-dialog-content>
      <div class="dialog-content">
        <div class="loading-section" *ngIf="loading()">
          <mat-spinner diameter="40"></mat-spinner>
          <p>Cargando información del documento...</p>
        </div>

        <div class="warning-section" *ngIf="!loading() && cdc()">
          <mat-icon color="warn">warning</mat-icon>
          <div>
            <h3>¡Atención!</h3>
            <p>Está a punto de cancelar el documento electrónico con CDC:</p>
            <code class="cdc-code">{{ cdc() }}</code>
            <p class="warning-text">Esta acción enviará una solicitud de cancelación a SIFEN y no se puede deshacer.</p>
          </div>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Motivo de cancelación</mat-label>
          <textarea 
            matInput
            [(ngModel)]="motivo"
            placeholder="Ingrese el motivo de la cancelación (mínimo 10 caracteres)"
            rows="4"
            maxlength="500"
            (input)="onMotivoChange()">
          </textarea>
          <mat-hint align="start">{{ motivo.length }}/500 caracteres</mat-hint>
          <mat-hint align="end" *ngIf="!isValid() && motivo.length > 0" class="error-hint">
            Mínimo 10 caracteres requeridos
          </mat-hint>
        </mat-form-field>

        <div class="validation-info" *ngIf="!isValid() && motivo.length > 0">
          <mat-icon color="warn">error</mat-icon>
          <span>El motivo debe tener al menos 10 caracteres</span>
        </div>

        <div class="validation-info success" *ngIf="isValid()">
          <mat-icon color="primary">check_circle</mat-icon>
          <span>Motivo válido</span>
        </div>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">
        Cancelar
      </button>
      <button 
        mat-raised-button 
        color="warn" 
        (click)="onConfirm()"
        [disabled]="!isValid() || loading()">
        <mat-icon>cancel</mat-icon>
        Confirmar Cancelación
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
      min-width: 450px;
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

    .full-width {
      width: 100%;
    }

    .validation-info {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      border-radius: 4px;
      background: #ffebee;
      color: #c62828;
      margin-top: 8px;
    }

    .validation-info.success {
      background: #e8f5e9;
      color: #2e7d32;
    }

    .validation-info mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .error-hint {
      color: #d32f2f;
    }

    mat-dialog-actions {
      padding: 16px 0 0 0;
      margin: 0;
    }

    .loading-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;
      gap: 16px;
    }

    .loading-section p {
      margin: 0;
      color: #666;
    }

    @media (max-width: 600px) {
      .dialog-content {
        min-width: auto;
        width: 100%;
      }

      .warning-section {
        flex-direction: column;
      }
    }
  `]
})
export class CancelarDeDialogComponent implements OnInit, OnDestroy {
  motivo = '';
  cdc = signal<string | null>(null);
  loading = signal(false);
  
  private destroy$ = new Subject<void>();

  constructor(
    private sifenApi: SifenApiService,
    public dialogRef: MatDialogRef<CancelarDeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CancelarDeDialogData
  ) {}

  ngOnInit(): void {
    this.obtenerCDC();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  obtenerCDC(): void {
    // Si ya tenemos CDC directamente
    if (this.data.cdc) {
      this.cdc.set(this.data.cdc);
      return;
    }

    // Si tenemos documento con CDC
    if (this.data.documento?.cdc) {
      this.cdc.set(this.data.documento.cdc);
      return;
    }

    // Si tenemos factura, buscar el documento electrónico
    if (this.data.factura) {
      if (this.data.factura.cdcDocumentoElectronico) {
        this.cdc.set(this.data.factura.cdcDocumentoElectronico);
        return;
      }

      // Si no tiene CDC en la factura pero tiene documentoElectronicoId, obtenerlo
      if (this.data.factura.documentoElectronicoId) {
        this.loading.set(true);
        // Aquí asumimos que podemos obtener el documento, pero el API actual no tiene este endpoint
        // Por ahora, intentamos obtenerlo por factura
        if (this.data.factura.id) {
          this.sifenApi.obtenerDocumentoPorFactura(this.data.factura.id)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (documento) => {
                this.cdc.set(documento.cdc || null);
                this.loading.set(false);
              },
              error: () => {
                this.loading.set(false);
                // Si no se puede obtener, cerrar el diálogo con error
                this.dialogRef.close(false);
              }
            });
        } else {
          this.loading.set(false);
        }
        return;
      }
    }

    // Si no hay forma de obtener el CDC, cerrar diálogo
    this.dialogRef.close(false);
  }

  onMotivoChange(): void {
    // Trigger validation
  }

  isValid(): boolean {
    return this.motivo.trim().length >= 10 && !!this.cdc();
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.isValid() && this.cdc()) {
      this.dialogRef.close({ cdc: this.cdc(), motivo: this.motivo.trim() });
    }
  }
}
