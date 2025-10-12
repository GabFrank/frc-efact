import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { Timbrado, TimbradoDetalle } from '../../models/timbrado.model';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

interface TimbradoDetalleWithStats extends TimbradoDetalle {
  numerosDisponibles: number;
  porcentajeUsado: number;
  alertaAgotamiento: boolean;
}

@Component({
  selector: 'app-timbrado-detalle-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatTooltipModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="detalle-container">
      <!-- Timbrado Info Card -->
      <mat-card class="info-card" *ngIf="timbrado">
        <mat-card-content>
          <div class="timbrado-info">
            <div class="info-item">
              <mat-icon>receipt</mat-icon>
              <div>
                <span class="label">Timbrado:</span>
                <span class="value">{{ timbrado.numero }}</span>
              </div>
            </div>
            <div class="info-item">
              <mat-icon>business</mat-icon>
              <div>
                <span class="label">Razón Social:</span>
                <span class="value">{{ timbrado.razonSocial }}</span>
              </div>
            </div>
            <div class="info-item">
              <mat-icon>badge</mat-icon>
              <div>
                <span class="label">RUC:</span>
                <span class="value">{{ timbrado.ruc }}</span>
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Alerts Section -->
      <div class="alerts-section" *ngIf="detallesConAlerta.length > 0">
        <mat-card class="alert-card">
          <mat-card-content>
            <div class="alert-header">
              <mat-icon color="warn">warning</mat-icon>
              <h3>Puntos de expedición con números agotándose</h3>
            </div>
            <div class="alert-list">
              <div *ngFor="let detalle of detallesConAlerta" class="alert-item">
                <span class="alert-text">
                  <strong>{{ detalle.puntoExpedicion }}-{{ detalle.codigoEstablecimientoFactura }}</strong>
                  - {{ detalle.numerosDisponibles }} números disponibles ({{ detalle.porcentajeUsado }}% usado)
                </span>
                <button 
                  mat-button 
                  color="primary" 
                  (click)="onEditDetalle(detalle)">
                  Ampliar rango
                </button>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Detalles List Card -->
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>Puntos de Expedición</h2>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Actions Bar -->
          <div class="actions-bar">
            <button 
              mat-raised-button 
              color="primary" 
              (click)="onCreateDetalle()">
              <mat-icon>add</mat-icon>
              Nuevo Punto de Expedición
            </button>
            <button 
              mat-button 
              (click)="onBack()">
              <mat-icon>arrow_back</mat-icon>
              Volver a Timbrados
            </button>
          </div>

          <!-- Loading State -->
          <app-loading-spinner *ngIf="loading"></app-loading-spinner>

          <!-- Error State -->
          <app-error-message 
            *ngIf="error"
            [message]="error">
          </app-error-message>

          <!-- Detalles Table -->
          <div class="table-container" *ngIf="!loading && !error">
            <table mat-table [dataSource]="detalles" class="detalles-table">
              
              <!-- Punto Expedición Column -->
              <ng-container matColumnDef="puntoExpedicion">
                <th mat-header-cell *matHeaderCellDef>Punto Exp.</th>
                <td mat-cell *matCellDef="let detalle">
                  <strong>{{ detalle.puntoExpedicion }}-{{ detalle.codigoEstablecimientoFactura }}</strong>
                </td>
              </ng-container>

              <!-- Rango Column -->
              <ng-container matColumnDef="rango">
                <th mat-header-cell *matHeaderCellDef>Rango</th>
                <td mat-cell *matCellDef="let detalle">
                  {{ detalle.rangoDesde }} - {{ detalle.rangoHasta }}
                  <div class="rango-info">
                    <small>Total: {{ detalle.cantidad | number }}</small>
                  </div>
                </td>
              </ng-container>

              <!-- Número Actual Column -->
              <ng-container matColumnDef="numeroActual">
                <th mat-header-cell *matHeaderCellDef>Número Actual</th>
                <td mat-cell *matCellDef="let detalle">
                  <div class="numero-actual-info">
                    <strong>{{ detalle.numeroActual }}</strong>
                    <mat-progress-bar 
                      mode="determinate" 
                      [value]="detalle.porcentajeUsado"
                      [class.progress-warning]="detalle.alertaAgotamiento"
                      [class.progress-danger]="detalle.porcentajeUsado >= 95">
                    </mat-progress-bar>
                    <small>{{ detalle.porcentajeUsado }}% usado</small>
                  </div>
                </td>
              </ng-container>

              <!-- Disponibles Column -->
              <ng-container matColumnDef="disponibles">
                <th mat-header-cell *matHeaderCellDef>Disponibles</th>
                <td mat-cell *matCellDef="let detalle">
                  <mat-chip 
                    [class.disponibles-ok]="detalle.numerosDisponibles > 100"
                    [class.disponibles-warning]="detalle.numerosDisponibles <= 100 && detalle.numerosDisponibles > 20"
                    [class.disponibles-danger]="detalle.numerosDisponibles <= 20">
                    <mat-icon *ngIf="detalle.alertaAgotamiento">warning</mat-icon>
                    {{ detalle.numerosDisponibles | number }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Ubicación Column -->
              <ng-container matColumnDef="ubicacion">
                <th mat-header-cell *matHeaderCellDef>Ubicación</th>
                <td mat-cell *matCellDef="let detalle">
                  <div class="ubicacion-info">
                    <div *ngIf="detalle.ciudad">{{ detalle.ciudad }}</div>
                    <small *ngIf="detalle.direccion">{{ detalle.direccion }}</small>
                  </div>
                </td>
              </ng-container>

              <!-- Estado Column -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let detalle">
                  <mat-chip [class.active-chip]="detalle.activo" [class.inactive-chip]="!detalle.activo">
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
                    color="primary"
                    (click)="onViewDetalle(detalle)"
                    matTooltip="Ver detalles">
                    <mat-icon>visibility</mat-icon>
                  </button>
                  <button 
                    mat-icon-button 
                    color="accent"
                    (click)="onEditDetalle(detalle)"
                    matTooltip="Editar">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button 
                    mat-icon-button 
                    [color]="detalle.activo ? 'warn' : 'primary'"
                    (click)="onToggleActive(detalle)"
                    [matTooltip]="detalle.activo ? 'Desactivar' : 'Activar'">
                    <mat-icon>{{ detalle.activo ? 'block' : 'check_circle' }}</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

              <!-- No Data Row -->
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell no-data" [attr.colspan]="displayedColumns.length">
                  <div class="no-data-message">
                    <mat-icon>store</mat-icon>
                    <p>No hay puntos de expedición configurados</p>
                    <button mat-raised-button color="primary" (click)="onCreateDetalle()">
                      <mat-icon>add</mat-icon>
                      Crear primer punto de expedición
                    </button>
                  </div>
                </td>
              </tr>
            </table>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .detalle-container {
      padding: 20px;
      max-width: 1600px;
      margin: 0 auto;
    }

    .info-card {
      margin-bottom: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .timbrado-info {
      display: flex;
      gap: 32px;
      flex-wrap: wrap;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .info-item mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
    }

    .info-item .label {
      display: block;
      font-size: 12px;
      opacity: 0.9;
    }

    .info-item .value {
      display: block;
      font-size: 16px;
      font-weight: 500;
    }

    .alerts-section {
      margin-bottom: 20px;
    }

    .alert-card {
      background-color: #fff3e0;
      border-left: 4px solid #ff9800;
    }

    .alert-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
    }

    .alert-header h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }

    .alert-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .alert-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px;
      background-color: white;
      border-radius: 4px;
    }

    .alert-text {
      font-size: 14px;
    }

    mat-card-header {
      margin-bottom: 20px;
    }

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }

    .actions-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .table-container {
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

    .rango-info {
      margin-top: 4px;
    }

    .rango-info small {
      color: #666;
    }

    .numero-actual-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 120px;
    }

    .numero-actual-info strong {
      font-size: 16px;
    }

    .numero-actual-info small {
      font-size: 12px;
      color: #666;
    }

    mat-progress-bar {
      height: 8px;
      border-radius: 4px;
    }

    .progress-warning ::ng-deep .mat-progress-bar-fill::after {
      background-color: #ff9800 !important;
    }

    .progress-danger ::ng-deep .mat-progress-bar-fill::after {
      background-color: #f44336 !important;
    }

    .ubicacion-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .ubicacion-info small {
      color: #666;
      font-size: 12px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    mat-chip mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      margin-right: 4px;
    }

    .disponibles-ok {
      background-color: #4caf50 !important;
      color: white;
    }

    .disponibles-warning {
      background-color: #ff9800 !important;
      color: white;
    }

    .disponibles-danger {
      background-color: #f44336 !important;
      color: white;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    .inactive-chip {
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

    .no-data-message mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }

    .no-data-message p {
      margin: 0;
      font-size: 16px;
    }

    button mat-icon {
      margin-right: 4px;
    }

    @media (max-width: 768px) {
      .timbrado-info {
        flex-direction: column;
        gap: 16px;
      }

      .actions-bar {
        flex-direction: column;
        align-items: stretch;
        gap: 8px;
      }
    }
  `]
})
export class TimbradoDetalleListComponent implements OnInit {
  timbrado: Timbrado | null = null;
  detalles: TimbradoDetalleWithStats[] = [];
  detallesConAlerta: TimbradoDetalleWithStats[] = [];
  
  loading = false;
  error: string | null = null;
  timbradoId: number | null = null;
  
  displayedColumns: string[] = ['puntoExpedicion', 'rango', 'numeroActual', 'disponibles', 'ubicacion', 'activo', 'actions'];

  constructor(
    private timbradoApiService: TimbradoApiService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.timbradoId = +params['id'];
        this.loadData();
      }
    });
  }

  private loadData(): void {
    if (!this.timbradoId) return;

    this.loading = true;
    this.error = null;

    // Load timbrado info
    this.timbradoApiService.getById(this.timbradoId).subscribe({
      next: (timbrado) => {
        this.timbrado = timbrado;
      },
      error: (err) => {
        this.error = 'Error al cargar el timbrado';
        console.error('Error loading timbrado:', err);
      }
    });

    // Load detalles
    this.timbradoApiService.getDetallesByTimbrado(this.timbradoId).subscribe({
      next: (detalles) => {
        this.detalles = detalles.map(d => this.enrichDetalleWithStats(d));
        this.updateAlertDetalles();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los puntos de expedición';
        this.loading = false;
        console.error('Error loading detalles:', err);
      }
    });
  }

  private enrichDetalleWithStats(detalle: TimbradoDetalle): TimbradoDetalleWithStats {
    const numerosDisponibles = detalle.rangoHasta - detalle.numeroActual;
    const numerosUsados = detalle.numeroActual - detalle.rangoDesde;
    const porcentajeUsado = Math.round((numerosUsados / detalle.cantidad) * 100);
    const alertaAgotamiento = numerosDisponibles <= 100;

    return {
      ...detalle,
      numerosDisponibles,
      porcentajeUsado,
      alertaAgotamiento
    };
  }

  private updateAlertDetalles(): void {
    this.detallesConAlerta = this.detalles
      .filter(d => d.alertaAgotamiento && d.activo)
      .sort((a, b) => a.numerosDisponibles - b.numerosDisponibles);
  }

  onCreateDetalle(): void {
    this.router.navigate(['/timbrados', this.timbradoId, 'detalles', 'new']);
  }

  onViewDetalle(detalle: TimbradoDetalle): void {
    this.router.navigate(['/timbrados', this.timbradoId, 'detalles', detalle.id]);
  }

  onEditDetalle(detalle: TimbradoDetalle): void {
    this.router.navigate(['/timbrados', this.timbradoId, 'detalles', detalle.id, 'edit']);
  }

  onToggleActive(detalle: TimbradoDetalle): void {
    const action = detalle.activo ? 'desactivar' : 'activar';
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${action.charAt(0).toUpperCase() + action.slice(1)} Punto de Expedición`,
        message: `¿Está seguro que desea ${action} el punto de expedición "${detalle.puntoExpedicion}-${detalle.codigoEstablecimientoFactura}"?`,
        confirmText: action.charAt(0).toUpperCase() + action.slice(1),
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.timbradoApiService.updateDetalle(detalle.id, { activo: !detalle.activo })
          .subscribe({
            next: () => {
              this.loadData();
            },
            error: (err) => {
              this.error = 'Error al actualizar el punto de expedición';
              console.error('Error updating detalle:', err);
            }
          });
      }
    });
  }

  onBack(): void {
    this.router.navigate(['/timbrados']);
  }
}
