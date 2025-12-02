import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { FacturaApiService } from '../../core/api/factura-api.service';
import { FacturaLegal } from '../../models/factura.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-factura-view',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatTableModule,
    MatChipsModule,
    MatSnackBarModule,
    MatDialogModule,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="factura-view-container">
      <app-loading-spinner *ngIf="loading()" />

      <div *ngIf="!loading() && factura()" class="content">
        <!-- Header con acciones -->
        <div class="header-actions">
          <button mat-button (click)="volver()">
            <mat-icon>arrow_back</mat-icon>
            Volver
          </button>
          <div class="actions">
            <button mat-raised-button color="primary" (click)="editar()">
              <mat-icon>edit</mat-icon>
              Editar
            </button>
            <button *ngIf="!tieneDE() || !puedeDesvincularDE()"
                    mat-raised-button
                    color="accent"
                    (click)="generarDE()"
                    [disabled]="tieneDE() && !puedeDesvincularDE()">
              <mat-icon>description</mat-icon>
              Generar DE
            </button>
            <button *ngIf="tieneDE() && puedeDesvincularDE()"
                    mat-raised-button
                    color="warn"
                    (click)="desvincularDE()">
              <mat-icon>link_off</mat-icon>
              Desvincular DE
            </button>
            <button mat-raised-button (click)="imprimir()">
              <mat-icon>print</mat-icon>
              Imprimir PDF
            </button>
          </div>
        </div>

        <!-- Información de la factura -->
        <mat-card class="info-card">
          <mat-card-header>
            <mat-card-title>
              <div class="title-row">
                <h2>Factura N° {{ factura()?.numeroFactura ? factura()!.numeroFactura!.toString().padStart(7, '0') : 'N/A' }}</h2>
                <mat-chip-set>
                  <mat-chip [class]="'chip-' + (factura()?.credito ? 'credito' : 'contado')">
                    {{ factura()?.credito ? 'Crédito' : 'Contado' }}
                  </mat-chip>
                  <mat-chip *ngIf="tieneDE()" class="chip-de">
                    {{ estadoDELabel() }}
                  </mat-chip>
                </mat-chip-set>
              </div>
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-grid">
              <div class="info-section">
                <h3>Datos de la Factura</h3>
                <div class="info-row">
                  <span class="label">Fecha:</span>
                  <span class="value">{{ formatFecha(factura()?.fecha) }}</span>
                </div>
                <div class="info-row">
                  <span class="label">Empresa:</span>
                  <span class="value">{{ factura()?.empresaId }}</span>
                </div>
                <div class="info-row">
                  <span class="label">Timbrado Detalle:</span>
                  <span class="value">{{ factura()?.timbradoDetalleId }}</span>
                </div>
              </div>

              <div class="info-section">
                <h3>Datos del Cliente</h3>
                <div class="info-row">
                  <span class="label">Nombre:</span>
                  <span class="value">{{ factura()?.nombre }}</span>
                </div>
                <div class="info-row" *ngIf="factura()?.ruc">
                  <span class="label">RUC:</span>
                  <span class="value">{{ factura()?.ruc }}</span>
                </div>
                <div class="info-row" *ngIf="factura()?.direccion">
                  <span class="label">Dirección:</span>
                  <span class="value">{{ factura()?.direccion }}</span>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Items de la factura -->
        <mat-card class="items-card">
          <mat-card-header>
            <mat-card-title>Items de la Factura</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="factura()?.items || []" class="items-table">
              <!-- Columna Número -->
              <ng-container matColumnDef="numero">
                <th mat-header-cell *matHeaderCellDef>#</th>
                <td mat-cell *matCellDef="let item; let i = index">{{ i + 1 }}</td>
              </ng-container>

              <!-- Columna Descripción -->
              <ng-container matColumnDef="descripcion">
                <th mat-header-cell *matHeaderCellDef>Descripción</th>
                <td mat-cell *matCellDef="let item">{{ item.descripcion }}</td>
              </ng-container>

              <!-- Columna Cantidad -->
              <ng-container matColumnDef="cantidad">
                <th mat-header-cell *matHeaderCellDef class="text-right">Cantidad</th>
                <td mat-cell *matCellDef="let item" class="text-right">
                  {{ item.cantidad }}
                </td>
              </ng-container>

              <!-- Columna Precio Unitario -->
              <ng-container matColumnDef="precioUnitario">
                <th mat-header-cell *matHeaderCellDef class="text-right">Precio Unit.</th>
                <td mat-cell *matCellDef="let item" class="text-right">
                  ₲ {{ item.precioUnitario.toLocaleString('es-PY') }}
                </td>
              </ng-container>

              <!-- Columna Total -->
              <ng-container matColumnDef="total">
                <th mat-header-cell *matHeaderCellDef class="text-right">Total</th>
                <td mat-cell *matCellDef="let item" class="text-right total-cell">
                  ₲ {{ item.total.toLocaleString('es-PY') }}
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
          </mat-card-content>
        </mat-card>

        <!-- Totales -->
        <mat-card class="totales-card">
          <mat-card-header>
            <mat-card-title>Totales</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="totales-grid">
              <!-- IVA 10% -->
              <div class="total-section">
                <h4>IVA 10%</h4>
                <div class="total-row">
                  <span>Subtotal:</span>
                  <span class="amount">₲ {{ factura()?.totalParcial10 ? factura()!.totalParcial10.toLocaleString('es-PY') : '0' }}</span>
                </div>
                <div class="total-row">
                  <span>IVA:</span>
                  <span class="amount">₲ {{ factura()?.ivaParcial10 ? factura()!.ivaParcial10.toLocaleString('es-PY') : '0' }}</span>
                </div>
              </div>

              <!-- IVA 5% -->
              <div class="total-section">
                <h4>IVA 5%</h4>
                <div class="total-row">
                  <span>Subtotal:</span>
                  <span class="amount">₲ {{ factura()?.totalParcial5 ? factura()!.totalParcial5.toLocaleString('es-PY') : '0' }}</span>
                </div>
                <div class="total-row">
                  <span>IVA:</span>
                  <span class="amount">₲ {{ factura()?.ivaParcial5 ? factura()!.ivaParcial5.toLocaleString('es-PY') : '0' }}</span>
                </div>
              </div>

              <!-- Exento -->
              <div class="total-section">
                <h4>Exento (0%)</h4>
                <div class="total-row">
                  <span>Subtotal:</span>
                  <span class="amount">₲ {{ factura()?.totalParcial0 ? factura()!.totalParcial0.toLocaleString('es-PY') : '0' }}</span>
                </div>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Totales finales -->
            <div class="totales-finales">
              <div class="total-row">
                <span class="label">Total Parcial:</span>
                <span class="amount">₲ {{ factura()?.totalParcial ? factura()!.totalParcial.toLocaleString('es-PY') : '0' }}</span>
              </div>

              <div class="total-row" *ngIf="factura()?.descuentoFinal && factura()!.descuentoFinal > 0">
                <span class="label">Descuento:</span>
                <span class="amount descuento">- ₲ {{ factura()?.descuentoFinal ? factura()!.descuentoFinal.toLocaleString('es-PY') : '0' }}</span>
              </div>

              <mat-divider></mat-divider>

              <div class="total-row total-final">
                <span class="label">TOTAL FINAL:</span>
                <span class="amount">₲ {{ factura()?.totalFinal ? factura()!.totalFinal.toLocaleString('es-PY') : '0' }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Mensaje si no se encuentra la factura -->
      <mat-card *ngIf="!loading() && !factura()" class="error-card">
        <mat-card-content>
          <div class="error-content">
            <mat-icon>error_outline</mat-icon>
            <h3>Factura no encontrada</h3>
            <p>La factura solicitada no existe o no tiene permisos para verla.</p>
            <button mat-raised-button color="primary" (click)="volver()">
              Volver al listado
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .factura-view-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .actions {
      display: flex;
      gap: 12px;
    }

    .info-card,
    .items-card,
    .totales-card {
      margin-bottom: 20px;
    }

    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    h2 {
      margin: 0;
    }

    .chip-credito {
      background-color: #fff3cd;
      color: #856404;
    }

    .chip-contado {
      background-color: #d1e7dd;
      color: #0f5132;
    }

    .chip-de {
      background-color: #d1e7dd;
      color: #0f5132;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 32px;
      margin-top: 16px;
    }

    .info-section h3 {
      margin: 0 0 16px 0;
      color: #666;
      font-size: 14px;
      font-weight: 600;
      text-transform: uppercase;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
    }

    .info-row:last-child {
      border-bottom: none;
    }

    .info-row .label {
      font-weight: 500;
      color: #666;
    }

    .info-row .value {
      color: #333;
    }

    .items-table {
      width: 100%;
      margin-top: 16px;
    }

    .text-right {
      text-align: right;
    }

    .total-cell {
      font-weight: 600;
      color: #1976d2;
    }

    .totales-card {
      background-color: #f5f5f5;
    }

    .totales-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 24px;
      margin-bottom: 24px;
    }

    .total-section h4 {
      margin: 0 0 12px 0;
      color: #333;
      font-weight: 500;
    }

    .total-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      font-size: 14px;
    }

    .total-row .amount {
      font-weight: 500;
      color: #333;
    }

    .totales-finales {
      margin-top: 24px;
    }

    .totales-finales .total-row {
      font-size: 16px;
      padding: 12px 0;
    }

    .descuento {
      color: #d32f2f !important;
    }

    .total-final {
      font-size: 20px !important;
      font-weight: 600 !important;
      color: #1976d2 !important;
      padding: 16px 0 !important;
    }

    .total-final .amount {
      color: #1976d2 !important;
      font-weight: 700 !important;
    }

    mat-divider {
      margin: 16px 0;
    }

    .error-card {
      margin-top: 40px;
    }

    .error-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px;
      text-align: center;
    }

    .error-content mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #f44336;
      margin-bottom: 16px;
    }

    .error-content h3 {
      margin: 0 0 8px 0;
      color: #333;
    }

    .error-content p {
      margin: 0 0 24px 0;
      color: #666;
    }
  `]
})
export class FacturaViewComponent implements OnInit {
  loading = signal(false);
  factura = signal<FacturaLegal | null>(null);

  displayedColumns = ['numero', 'descripcion', 'cantidad', 'precioUnitario', 'total'];

  constructor(
    private facturaApi: FacturaApiService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.cargarFactura(Number(id));
    }
  }

  cargarFactura(id: number): void {
    this.loading.set(true);
    this.facturaApi.getById(id).subscribe({
      next: (factura) => {
        this.factura.set(factura);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al cargar la factura', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  formatFecha(fecha: string | undefined): string {
    if (!fecha) return '-';
    return new Date(fecha).toLocaleDateString('es-PY', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  tieneDE(): boolean {
    return !!this.factura()?.documentoElectronicoId;
  }

  puedeDesvincularDE(): boolean {
    const estado = this.factura()?.estadoDocumentoElectronico;
    // Solo se puede desvincular si el DE tiene error permanente (ERROR o RECHAZADO)
    return estado === 'ERROR' || estado === 'RECHAZADO';
  }

  estadoDELabel(): string {
    const estado = this.factura()?.estadoDocumentoElectronico;
    if (!estado) {
      return '✅ Con DE';
    }

    const labels: Record<string, string> = {
      PENDIENTE: '⏳ Pendiente',
      EN_PROCESO: '🔄 En Proceso',
      APROBADO: '✅ Aprobado',
      RECHAZADO: '❌ Rechazado',
      CANCELADO: '🚫 Cancelado',
      ERROR: '⚠️ Error'
    };

    return labels[estado] || estado;
  }

  volver(): void {
    this.router.navigate(['/facturacion']);
  }

  editar(): void {
    const id = this.factura()?.id;
    if (id) {
      this.router.navigate(['/facturacion', id, 'editar']);
    }
  }

  generarDE(): void {
    const factura = this.factura();
    if (!factura?.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Generar Documento Electrónico',
        message: `¿Desea generar el documento electrónico para la factura N° ${factura.numeroFactura}?`,
        confirmText: 'Generar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed && factura.id) {
        this.loading.set(true);
        this.facturaApi.generarDE(factura.id).subscribe({
          next: (response) => {
            const estado = response.documento.estado;
            this.snackBar.open(
              `Documento electrónico enviado (estado: ${estado})`,
              'Cerrar',
              { duration: 4000 }
            );
            this.cargarFactura(factura.id!);
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al generar documento electrónico',
              'Cerrar',
              { duration: 5000 }
            );
            this.loading.set(false);
          }
        });
      }
    });
  }

  desvincularDE(): void {
    const factura = this.factura();
    if (!factura?.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Desvincular Documento Electrónico',
        message: `¿Está seguro de desvincular el documento electrónico de la factura N° ${factura.numeroFactura}? ` +
                 `Esta acción eliminará el DE y permitirá generar uno nuevo.`,
        confirmText: 'Desvincular',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed && factura.id) {
        this.loading.set(true);
        this.facturaApi.desvincularDE(factura.id).subscribe({
          next: () => {
            this.snackBar.open(
              'Documento electrónico desvinculado exitosamente',
              'Cerrar',
              { duration: 4000 }
            );
            this.cargarFactura(factura.id!);
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al desvincular documento electrónico',
              'Cerrar',
              { duration: 5000 }
            );
            this.loading.set(false);
          }
        });
      }
    });
  }

  imprimir(): void {
    const id = this.factura()?.id;
    if (!id) return;

    this.loading.set(true);
    this.facturaApi.descargarPDF(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `factura-${this.factura()?.numeroFactura}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.loading.set(false);
      },
      error: (error) => {
        this.snackBar.open('Error al descargar PDF', 'Cerrar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }
}
