import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { DocumentoElectronicoApiService } from '../../core/api/documento-electronico-api.service';
import { DocumentoElectronico, EstadoDE } from '../../models/documento-electronico.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { CancelarDeDialogComponent } from './cancelar-de-dialog.component';

@Component({
  selector: 'app-documento-electronico-view',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatChipsModule,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="documento-view-container">
      <app-loading-spinner *ngIf="loading()" />

      <div *ngIf="!loading() && documento()">
        <!-- Header con acciones -->
        <div class="header-section">
          <button mat-icon-button (click)="volver()">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <h1>Documento Electrónico #{{ documento()?.id }}</h1>
          <div class="header-actions">
            <button mat-stroked-button (click)="consultarEstado()" [disabled]="!documento()?.cdc">
              <mat-icon>refresh</mat-icon>
              Consultar Estado
            </button>
            <button mat-stroked-button (click)="descargarXML()" [disabled]="!documento()?.cdc">
              <mat-icon>download</mat-icon>
              Descargar XML
            </button>
            <button 
              mat-raised-button 
              color="warn" 
              (click)="cancelar()"
              [disabled]="documento()?.estado !== EstadoDE.APROBADO">
              <mat-icon>cancel</mat-icon>
              Cancelar Documento
            </button>
          </div>
        </div>

        <!-- Información principal -->
        <mat-card class="info-card">
          <mat-card-header>
            <mat-card-title>Información del Documento</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-grid">
              <div class="info-item">
                <label>Estado:</label>
                <mat-chip [class]="'estado-chip estado-' + documento()?.estado">
                  {{ getEstadoLabel(documento()?.estado!) }}
                </mat-chip>
              </div>

              <div class="info-item">
                <label>Factura Legal ID:</label>
                <span>{{ documento()?.facturaLegalId }}</span>
              </div>

              <div class="info-item">
                <label>Número de Documento:</label>
                <span>{{ documento()?.numeroDocumento || 'N/A' }}</span>
              </div>

              <div class="info-item">
                <label>Fecha de Emisión:</label>
                <span>{{ formatDate(documento()?.fechaEmision!) }}</span>
              </div>

              <div class="info-item">
                <label>Fecha Recepción SIFEN:</label>
                <span>{{ documento()?.fechaRecepcionSifen ? formatDate(documento()?.fechaRecepcionSifen!) : 'N/A' }}</span>
              </div>

              <div class="info-item">
                <label>Lote ID:</label>
                <span>{{ documento()?.loteDeId || 'Sin asignar' }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- CDC -->
        <mat-card class="cdc-card" *ngIf="documento()?.cdc">
          <mat-card-header>
            <mat-card-title>Código de Control (CDC)</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="cdc-content">
              <code class="cdc-code">{{ documento()?.cdc }}</code>
              <button mat-icon-button (click)="copiarCDC()" matTooltip="Copiar CDC">
                <mat-icon>content_copy</mat-icon>
              </button>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Código QR -->
        <mat-card class="qr-card" *ngIf="documento()?.urlQr">
          <mat-card-header>
            <mat-card-title>Código QR</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="qr-content">
              <img [src]="documento()?.urlQr" alt="Código QR" class="qr-image">
              <div class="qr-info">
                <p>Escanea este código QR para verificar el documento en SIFEN</p>
                <button mat-stroked-button (click)="descargarQR()">
                  <mat-icon>download</mat-icon>
                  Descargar QR
                </button>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Respuesta SIFEN -->
        <mat-card class="sifen-card" *ngIf="documento()?.codigoRespuestaSifen">
          <mat-card-header>
            <mat-card-title>Respuesta de SIFEN</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-grid">
              <div class="info-item">
                <label>Código de Respuesta:</label>
                <span class="codigo-respuesta">{{ documento()?.codigoRespuestaSifen }}</span>
              </div>

              <div class="info-item full-width">
                <label>Mensaje:</label>
                <div class="mensaje-respuesta" [class.error]="isEstadoError()">
                  {{ documento()?.mensajeRespuestaSifen || 'Sin mensaje' }}
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Mensaje si no hay CDC -->
        <mat-card class="warning-card" *ngIf="!documento()?.cdc">
          <mat-card-content>
            <div class="warning-content">
              <mat-icon color="warn">warning</mat-icon>
              <div>
                <h3>Documento sin CDC</h3>
                <p>Este documento aún no tiene un Código de Control (CDC) generado. 
                   El CDC se genera cuando el documento es procesado correctamente.</p>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Error state -->
      <mat-card *ngIf="!loading() && !documento()">
        <mat-card-content>
          <div class="error-content">
            <mat-icon color="warn">error</mat-icon>
            <h3>Documento no encontrado</h3>
            <p>No se pudo cargar la información del documento electrónico.</p>
            <button mat-raised-button color="primary" (click)="volver()">
              Volver al listado
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .documento-view-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header-section {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;
    }

    .header-section h1 {
      flex: 1;
      margin: 0;
      font-size: 24px;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }

    .info-card,
    .cdc-card,
    .qr-card,
    .sifen-card,
    .warning-card {
      margin-bottom: 24px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 24px;
      margin-top: 16px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .info-item.full-width {
      grid-column: 1 / -1;
    }

    .info-item label {
      font-weight: 500;
      color: #666;
      font-size: 14px;
    }

    .info-item span {
      font-size: 16px;
      color: #333;
    }

    .estado-chip {
      display: inline-block;
      padding: 6px 16px;
      border-radius: 16px;
      font-size: 14px;
      font-weight: 500;
      width: fit-content;
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

    .cdc-content {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-top: 16px;
    }

    .cdc-code {
      flex: 1;
      background: #f5f5f5;
      padding: 16px;
      border-radius: 8px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
      word-break: break-all;
      border: 1px solid #ddd;
    }

    .qr-content {
      display: flex;
      gap: 32px;
      align-items: center;
      margin-top: 16px;
    }

    .qr-image {
      width: 200px;
      height: 200px;
      border: 2px solid #ddd;
      border-radius: 8px;
      padding: 8px;
      background: white;
    }

    .qr-info {
      flex: 1;
    }

    .qr-info p {
      margin-bottom: 16px;
      color: #666;
    }

    .codigo-respuesta {
      font-family: 'Courier New', monospace;
      font-weight: bold;
      color: #2196f3;
    }

    .mensaje-respuesta {
      background: #f5f5f5;
      padding: 12px;
      border-radius: 8px;
      border-left: 4px solid #2196f3;
    }

    .mensaje-respuesta.error {
      background: #ffebee;
      border-left-color: #f44336;
      color: #c62828;
    }

    .warning-content,
    .error-content {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
    }

    .warning-content mat-icon,
    .error-content mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
    }

    .warning-content h3,
    .error-content h3 {
      margin: 0 0 8px 0;
    }

    .warning-content p,
    .error-content p {
      margin: 0;
      color: #666;
    }

    .error-content {
      flex-direction: column;
      text-align: center;
    }

    @media (max-width: 768px) {
      .header-section {
        flex-direction: column;
        align-items: flex-start;
      }

      .header-actions {
        width: 100%;
        flex-direction: column;
      }

      .header-actions button {
        width: 100%;
      }

      .qr-content {
        flex-direction: column;
      }

      .info-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class DocumentoElectronicoViewComponent implements OnInit {
  loading = signal(false);
  documento = signal<DocumentoElectronico | null>(null);
  
  // Expose enum to template
  EstadoDE = EstadoDE;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private deApi: DocumentoElectronicoApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarDocumento(+id);
    }
  }

  cargarDocumento(id: number): void {
    this.loading.set(true);
    
    this.deApi.getById(id).subscribe({
      next: (documento) => {
        this.documento.set(documento);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar documento', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  getEstadoLabel(estado: EstadoDE): string {
    const labels: { [key in EstadoDE]: string } = {
      [EstadoDE.PENDIENTE]: '⏳ Pendiente',
      [EstadoDE.EN_PROCESO]: '🔄 En Proceso',
      [EstadoDE.APROBADO]: '✅ Aprobado',
      [EstadoDE.RECHAZADO]: '❌ Rechazado',
      [EstadoDE.CANCELADO]: '🚫 Cancelado',
      [EstadoDE.ERROR]: '⚠️ Error'
    };
    return labels[estado];
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('es-PY', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  isEstadoError(): boolean {
    const doc = this.documento();
    return doc?.estado === EstadoDE.RECHAZADO || doc?.estado === EstadoDE.ERROR;
  }

  copiarCDC(): void {
    const cdc = this.documento()?.cdc;
    if (cdc) {
      navigator.clipboard.writeText(cdc).then(() => {
        this.snackBar.open('CDC copiado al portapapeles', 'Cerrar', { duration: 2000 });
      });
    }
  }

  consultarEstado(): void {
    const doc = this.documento();
    if (!doc) return;

    this.loading.set(true);
    this.deApi.consultarEstado(doc.id).subscribe({
      next: (documentoActualizado) => {
        this.documento.set(documentoActualizado);
        this.snackBar.open('Estado actualizado correctamente', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open(
          error.error?.message || 'Error al consultar estado',
          'Cerrar',
          { duration: 5000 }
        );
        this.loading.set(false);
      }
    });
  }

  descargarXML(): void {
    const doc = this.documento();
    if (!doc) return;

    this.loading.set(true);
    this.deApi.descargarXML(doc.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `DE-${doc.cdc}.xml`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.loading.set(false);
        this.snackBar.open('XML descargado correctamente', 'Cerrar', { duration: 2000 });
      },
      error: (error) => {
        this.snackBar.open('Error al descargar XML', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  descargarQR(): void {
    const doc = this.documento();
    if (!doc?.urlQr) return;

    // Create a temporary link to download the QR image
    const link = document.createElement('a');
    link.href = doc.urlQr;
    link.download = `QR-${doc.cdc}.png`;
    link.click();
    this.snackBar.open('QR descargado correctamente', 'Cerrar', { duration: 2000 });
  }

  cancelar(): void {
    const doc = this.documento();
    if (!doc) return;

    const dialogRef = this.dialog.open(CancelarDeDialogComponent, {
      width: '500px',
      data: { documento: doc }
    });

    dialogRef.afterClosed().subscribe(motivo => {
      if (motivo) {
        this.loading.set(true);
        this.deApi.cancelar(doc.id, motivo).subscribe({
          next: () => {
            this.snackBar.open('Solicitud de cancelación enviada', 'Cerrar', { duration: 3000 });
            this.cargarDocumento(doc.id);
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al cancelar documento',
              'Cerrar',
              { duration: 5000 }
            );
            this.loading.set(false);
          }
        });
      }
    });
  }

  volver(): void {
    this.router.navigate(['/documentos/lista']);
  }
}
