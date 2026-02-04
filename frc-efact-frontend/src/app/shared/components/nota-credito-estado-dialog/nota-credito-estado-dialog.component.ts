import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NotaCredito } from '../../../models/nota.model';
import { DocumentoElectronico, LoteDE } from '../../../models/documento-electronico.model';
import { NotaCreditoApiService } from '../../../core/api/nota-credito-api.service';
import { SifenApiService } from '../../../core/api/sifen-api.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface NotaCreditoEstadoData {
  nota: NotaCredito;
}

@Component({
  selector: 'app-nota-credito-estado-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>info</mat-icon>
      Estado de Nota de Crédito #{{ data.nota.numeroFormateado || data.nota.numeroNotaCredito }}
    </h2>

    <mat-dialog-content>
      <div class="loading-container" *ngIf="loading">
        <mat-spinner diameter="40"></mat-spinner>
        <p>Cargando información...</p>
      </div>

      <div *ngIf="!loading" class="estado-content">
        <!-- Estado de Nota de Crédito -->
        <mat-card class="estado-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>receipt</mat-icon>
              Nota de Crédito
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-row">
              <span class="label">Número:</span>
              <span class="value">{{ data.nota.numeroFormateado || data.nota.numeroNotaCredito }}</span>
            </div>
            <div class="info-row">
              <span class="label">Fecha:</span>
              <span class="value">{{ data.nota.fecha | date:'dd/MM/yyyy' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Cliente:</span>
              <span class="value">{{ data.nota.nombre || 'N/A' }}</span>
            </div>
            <div class="info-row" *ngIf="data.nota.ruc">
              <span class="label">RUC:</span>
              <span class="value">{{ data.nota.ruc }}</span>
            </div>
            <div class="info-row">
              <span class="label">Total:</span>
              <span class="value">{{ data.nota.totalFinal | number:'1.0-0' }} ₲</span>
            </div>
            <div class="info-row" *ngIf="data.nota.motivoEmision">
              <span class="label">Motivo de Emisión:</span>
              <span class="value">{{ data.nota.motivoEmision }}</span>
            </div>
            <div class="info-row" *ngIf="data.nota.descripcionMotivo">
              <span class="label">Descripción Motivo:</span>
              <span class="value">{{ data.nota.descripcionMotivo }}</span>
            </div>
            <div class="info-row" *ngIf="data.nota.numeroFacturaAsociada">
              <span class="label">Factura Asociada:</span>
              <span class="value">{{ data.nota.numeroFacturaAsociada }}</span>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Estado del Documento Electrónico -->
        <mat-card class="estado-card" *ngIf="documentoElectronico">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>description</mat-icon>
              Documento Electrónico
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-row">
              <span class="label">Estado:</span>
              <mat-chip [class]="'estado-' + getEstadoDEClass(documentoElectronico.estado)">
                {{ documentoElectronico.estado }}
              </mat-chip>
            </div>
            <div class="info-row" *ngIf="documentoElectronico.cdc">
              <span class="label">CDC:</span>
              <span class="value code">{{ documentoElectronico.cdc }}</span>
            </div>
            <div class="info-row" *ngIf="documentoElectronico.numeroDocumento">
              <span class="label">Número DE:</span>
              <span class="value">{{ documentoElectronico.numeroDocumento }}</span>
            </div>
            <div class="info-row" *ngIf="documentoElectronico.fechaEmision">
              <span class="label">Fecha Emisión:</span>
              <span class="value">{{ documentoElectronico.fechaEmision | date:'dd/MM/yyyy HH:mm' }}</span>
            </div>
            <div class="info-row" *ngIf="documentoElectronico.fechaRecepcionSifen">
              <span class="label">Fecha Recepción SIFEN:</span>
              <span class="value">{{ documentoElectronico.fechaRecepcionSifen | date:'dd/MM/yyyy HH:mm' }}</span>
            </div>
            <div class="info-row" *ngIf="documentoElectronico.codigoRespuestaSifen">
              <span class="label">Código Respuesta SIFEN:</span>
              <span class="value code">{{ documentoElectronico.codigoRespuestaSifen }}</span>
            </div>
            <div class="info-row" *ngIf="documentoElectronico.mensajeRespuestaSifen">
              <span class="label">Mensaje Respuesta SIFEN:</span>
              <span class="value message">{{ documentoElectronico.mensajeRespuestaSifen }}</span>
            </div>
            <div class="info-row" *ngIf="documentoElectronico.urlQr">
              <span class="label">URL QR:</span>
              <a [href]="documentoElectronico.urlQr" target="_blank" class="value link">
                {{ documentoElectronico.urlQr }}
                <mat-icon>open_in_new</mat-icon>
              </a>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="estado-card no-data" *ngIf="!documentoElectronico">
          <mat-card-content>
            <div class="no-data-content">
              <mat-icon>info</mat-icon>
              <p>Esta nota de crédito no tiene documento electrónico asociado</p>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Estado del Lote -->
        <mat-card class="estado-card" *ngIf="lote">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>inventory</mat-icon>
              Lote DE
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-row">
              <span class="label">Estado:</span>
              <mat-chip [class]="'estado-' + getEstadoLoteClass(lote.estado)">
                {{ lote.estado }}
              </mat-chip>
            </div>
            <div class="info-row" *ngIf="lote.protocolo">
              <span class="label">Protocolo:</span>
              <span class="value code">{{ lote.protocolo }}</span>
            </div>
            <div class="info-row" *ngIf="lote.fechaProcesado">
              <span class="label">Fecha Procesado:</span>
              <span class="value">{{ lote.fechaProcesado | date:'dd/MM/yyyy HH:mm' }}</span>
            </div>
            <div class="info-row" *ngIf="lote.fechaUltimoIntento">
              <span class="label">Último Intento:</span>
              <span class="value">{{ lote.fechaUltimoIntento | date:'dd/MM/yyyy HH:mm' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Intentos:</span>
              <span class="value">{{ lote.intentos }}</span>
            </div>
            <div class="info-row" *ngIf="lote.codigoRespuesta">
              <span class="label">Código Respuesta SIFEN:</span>
              <span class="value code">{{ lote.codigoRespuesta }}</span>
            </div>
            <div class="info-row" *ngIf="lote.mensajeRespuesta">
              <span class="label">Mensaje Respuesta SIFEN:</span>
              <span class="value message">{{ lote.mensajeRespuesta }}</span>
            </div>
            <div class="info-row" *ngIf="lote.respuestaSifen">
              <span class="label">Respuesta Completa SIFEN (XML):</span>
              <div class="xml-response">
                <pre>{{ lote.respuestaSifen }}</pre>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="estado-card no-data" *ngIf="documentoElectronico && !lote">
          <mat-card-content>
            <div class="no-data-content">
              <mat-icon>info</mat-icon>
              <p>El documento electrónico no está asociado a un lote</p>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onClose()">Cerrar</button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2[mat-dialog-title] {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
    }

    mat-dialog-content {
      min-width: 600px;
      max-width: 800px;
      max-height: 70vh;
      overflow-y: auto;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;
      gap: 16px;
    }

    .estado-content {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .estado-card {
      margin-bottom: 16px;
    }

    .estado-card.no-data {
      background-color: #f5f5f5;
    }

    mat-card-header {
      margin-bottom: 16px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 1.1rem;
      font-weight: 500;
    }

    .info-row {
      display: flex;
      align-items: flex-start;
      margin-bottom: 12px;
      gap: 12px;
    }

    .info-row .label {
      font-weight: 500;
      min-width: 180px;
      color: #666;
    }

    .info-row .value {
      flex: 1;
      word-break: break-word;
    }

    .info-row .value.code {
      font-family: 'Courier New', monospace;
      font-size: 0.9rem;
      background-color: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
    }

    .info-row .value.message {
      color: #333;
      font-style: italic;
    }

    .info-row .value.link {
      color: #1976d2;
      text-decoration: none;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .info-row .value.link:hover {
      text-decoration: underline;
    }

    .info-row .value.link mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    mat-chip {
      font-size: 0.85rem;
      font-weight: 500;
    }

    .estado-PENDIENTE {
      background-color: #fff3cd;
      color: #856404;
    }

    .estado-EN_PROCESO {
      background-color: #cfe2ff;
      color: #084298;
    }

    .estado-APROBADO {
      background-color: #d1e7dd;
      color: #0f5132;
    }

    .estado-RECHAZADO {
      background-color: #f8d7da;
      color: #842029;
    }

    .estado-CANCELADO {
      background-color: #e2e3e5;
      color: #41464b;
    }

    .estado-ERROR {
      background-color: #f8d7da;
      color: #842029;
    }

    .no-data-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px;
      gap: 8px;
      color: #666;
    }

    .no-data-content mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #999;
    }

    .xml-response {
      margin-top: 8px;
      max-height: 200px;
      overflow-y: auto;
      background-color: #f5f5f5;
      padding: 12px;
      border-radius: 4px;
    }

    .xml-response pre {
      margin: 0;
      font-size: 0.85rem;
      white-space: pre-wrap;
      word-wrap: break-word;
    }

    mat-dialog-actions {
      padding: 16px 24px;
      border-top: 1px solid #e0e0e0;
    }

    @media (max-width: 768px) {
      mat-dialog-content {
        min-width: 90vw;
      }

      .info-row {
        flex-direction: column;
        gap: 4px;
      }

      .info-row .label {
        min-width: auto;
      }
    }
  `]
})
export class NotaCreditoEstadoDialogComponent implements OnInit {
  loading = true;
  documentoElectronico: DocumentoElectronico | null = null;
  lote: LoteDE | null = null;

  constructor(
    public dialogRef: MatDialogRef<NotaCreditoEstadoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: NotaCreditoEstadoData,
    private notaCreditoApi: NotaCreditoApiService,
    private sifenApi: SifenApiService
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    if (!this.data.nota.id) {
      this.loading = false;
      return;
    }

    // Siempre crear 3 observables para forkJoin
    const notaRequest = this.notaCreditoApi.getById(this.data.nota.id).pipe(
      catchError(() => of(this.data.nota))
    );

    const deRequest = this.sifenApi.obtenerDocumentoPorNotaCredito(this.data.nota.id).pipe(
      catchError(() => of(null))
    );

    // Cargar lote si existe, sino null
    const loteRequest = this.data.nota.loteDeId
      ? this.sifenApi.consultarLote(this.data.nota.loteDeId!).pipe(
          catchError(() => of(null))
        )
      : of(null);

    forkJoin([notaRequest, deRequest, loteRequest]).subscribe({
      next: ([nota, de, lote]) => {
        if (nota) {
          // Actualizar datos de nota si se obtuvo completa
          Object.assign(this.data.nota, nota);
        }

        this.documentoElectronico = de || null;
        this.lote = lote || null;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando datos:', error);
        this.loading = false;
      }
    });
  }

  getEstadoDEClass(estado: string): string {
    return estado?.toLowerCase().replace('_', '-') || '';
  }

  getEstadoLoteClass(estado: string): string {
    return estado?.toLowerCase().replace('_', '-') || '';
  }

  onClose(): void {
    this.dialogRef.close();
  }
}

