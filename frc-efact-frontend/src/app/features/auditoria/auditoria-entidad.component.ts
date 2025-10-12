import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';

import { AuditApiService } from '../../core/api/audit-api.service';
import { AuditLog, AccionEnum } from '../../models/audit.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { AuditoriaDetailComponent } from './auditoria-detail.component';

/**
 * Componente para mostrar el historial completo de una entidad
 */
@Component({
  selector: 'app-auditoria-entidad',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="entidad-history-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <button mat-icon-button (click)="volver()">
              <mat-icon>arrow_back</mat-icon>
            </button>
            <span>Historial de {{ entidadTipo }} (ID: {{ entidadId }})</span>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Loading -->
          <app-loading-spinner *ngIf="loading"></app-loading-spinner>

          <!-- Error -->
          <app-error-message 
            *ngIf="error" 
            [error]="error"
            [showRetry]="true"
            (retry)="cargarHistorial()">
          </app-error-message>

          <!-- Timeline de cambios -->
          <div *ngIf="!loading && !error" class="timeline-container">
            <div *ngIf="historial.length === 0" class="no-data">
              <mat-icon>info</mat-icon>
              <p>No hay historial de cambios para esta entidad</p>
            </div>

            <div class="timeline" *ngIf="historial.length > 0">
              <div 
                *ngFor="let log of historial; let i = index" 
                class="timeline-item"
                [class.first]="i === 0">
                
                <div class="timeline-marker" [class]="'marker-' + log.accion.toLowerCase()">
                  <mat-icon>{{ getAccionIcon(log.accion) }}</mat-icon>
                </div>

                <div class="timeline-content">
                  <mat-card>
                    <mat-card-header>
                      <div class="timeline-header">
                        <mat-chip [class]="'chip-' + log.accion.toLowerCase()">
                          {{ getAccionLabel(log.accion) }}
                        </mat-chip>
                        <span class="timeline-date">
                          {{ log.fechaHora | date:'dd/MM/yyyy HH:mm:ss' }}
                        </span>
                      </div>
                    </mat-card-header>

                    <mat-card-content>
                      <div class="log-info">
                        <div class="info-row">
                          <mat-icon>person</mat-icon>
                          <span>
                            <strong>{{ log.usuario.username }}</strong>
                            <small *ngIf="log.usuario.nombreCompleto">
                              ({{ log.usuario.nombreCompleto }})
                            </small>
                          </span>
                        </div>

                        <div class="info-row" *ngIf="log.empresa">
                          <mat-icon>business</mat-icon>
                          <span>{{ log.empresa.razonSocial }}</span>
                        </div>

                        <div class="info-row" *ngIf="log.ipAddress">
                          <mat-icon>computer</mat-icon>
                          <span>{{ log.ipAddress }}</span>
                        </div>

                        <div class="info-row" *ngIf="log.descripcion">
                          <mat-icon>description</mat-icon>
                          <span>{{ log.descripcion }}</span>
                        </div>
                      </div>

                      <!-- Resumen de cambios -->
                      <div class="changes-summary" *ngIf="log.accion === AccionEnum.UPDATE && hasChanges(log)">
                        <strong>Campos modificados:</strong>
                        <div class="changed-fields">
                          <mat-chip *ngFor="let field of getChangedFields(log)">
                            {{ formatFieldName(field) }}
                          </mat-chip>
                        </div>
                      </div>
                    </mat-card-content>

                    <mat-card-actions *ngIf="log.accion === AccionEnum.UPDATE">
                      <button 
                        mat-button 
                        color="primary"
                        (click)="verDetalle(log)">
                        <mat-icon>visibility</mat-icon>
                        Ver Detalle
                      </button>
                    </mat-card-actions>
                  </mat-card>
                </div>
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .entidad-history-container {
      padding: 20px;
    }

    mat-card-header {
      margin-bottom: 20px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 24px;
    }

    .timeline-container {
      margin-top: 20px;
    }

    .no-data {
      text-align: center;
      padding: 40px;
      color: rgba(0, 0, 0, 0.6);
    }

    .no-data mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 10px;
    }

    .timeline {
      position: relative;
      padding-left: 40px;
    }

    .timeline::before {
      content: '';
      position: absolute;
      left: 20px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: linear-gradient(to bottom, #2196f3, #e0e0e0);
    }

    .timeline-item {
      position: relative;
      margin-bottom: 30px;
    }

    .timeline-item.first .timeline-marker {
      animation: pulse 2s infinite;
    }

    @keyframes pulse {
      0%, 100% {
        transform: scale(1);
      }
      50% {
        transform: scale(1.1);
      }
    }

    .timeline-marker {
      position: absolute;
      left: -28px;
      top: 10px;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: white;
      border: 3px solid;
      z-index: 1;
    }

    .timeline-marker mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      color: white;
    }

    .marker-create {
      border-color: #4caf50;
      background-color: #4caf50;
    }

    .marker-update {
      border-color: #2196f3;
      background-color: #2196f3;
    }

    .marker-delete {
      border-color: #f44336;
      background-color: #f44336;
    }

    .marker-read {
      border-color: #9e9e9e;
      background-color: #9e9e9e;
    }

    .timeline-content {
      margin-left: 20px;
    }

    .timeline-content mat-card {
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      transition: box-shadow 0.3s;
    }

    .timeline-content mat-card:hover {
      box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    }

    .timeline-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .timeline-date {
      color: rgba(0, 0, 0, 0.6);
      font-size: 14px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
    }

    .chip-create {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .chip-update {
      background-color: #2196f3 !important;
      color: white !important;
    }

    .chip-delete {
      background-color: #f44336 !important;
      color: white !important;
    }

    .chip-read {
      background-color: #9e9e9e !important;
      color: white !important;
    }

    .log-info {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-top: 12px;
    }

    .info-row {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .info-row mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: rgba(0, 0, 0, 0.6);
    }

    .info-row small {
      color: rgba(0, 0, 0, 0.6);
      margin-left: 4px;
    }

    .changes-summary {
      margin-top: 16px;
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }

    .changes-summary strong {
      display: block;
      margin-bottom: 8px;
    }

    .changed-fields {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .changed-fields mat-chip {
      background-color: #fff3e0 !important;
      color: #e65100 !important;
    }
  `]
})
export class AuditoriaEntidadComponent implements OnInit {
  entidadTipo: string = '';
  entidadId: number = 0;
  historial: AuditLog[] = [];
  
  loading = false;
  error: string | null = null;

  AccionEnum = AccionEnum;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private auditApiService: AuditApiService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.entidadTipo = params['tipo'];
      this.entidadId = +params['id'];
      this.cargarHistorial();
    });
  }

  cargarHistorial(): void {
    this.loading = true;
    this.error = null;

    this.auditApiService.getHistorialEntidad(this.entidadTipo, this.entidadId).subscribe({
      next: (data) => {
        this.historial = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el historial de la entidad';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }

  volver(): void {
    this.router.navigate(['/auditoria']);
  }

  verDetalle(log: AuditLog): void {
    this.dialog.open(AuditoriaDetailComponent, {
      width: '800px',
      data: log
    });
  }

  hasChanges(log: AuditLog): boolean {
    return !!(log.valoresAnteriores || log.valoresNuevos);
  }

  getChangedFields(log: AuditLog): string[] {
    const fields = new Set<string>();
    
    if (log.valoresAnteriores) {
      Object.keys(log.valoresAnteriores).forEach(key => fields.add(key));
    }
    
    if (log.valoresNuevos) {
      Object.keys(log.valoresNuevos).forEach(key => fields.add(key));
    }
    
    return Array.from(fields).sort();
  }

  formatFieldName(field: string): string {
    return field
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }

  getAccionLabel(accion: AccionEnum): string {
    const labels: Record<AccionEnum, string> = {
      [AccionEnum.CREATE]: 'Crear',
      [AccionEnum.UPDATE]: 'Actualizar',
      [AccionEnum.DELETE]: 'Eliminar',
      [AccionEnum.READ]: 'Leer'
    };
    return labels[accion] || accion;
  }

  getAccionIcon(accion: AccionEnum): string {
    const icons: Record<AccionEnum, string> = {
      [AccionEnum.CREATE]: 'add_circle',
      [AccionEnum.UPDATE]: 'edit',
      [AccionEnum.DELETE]: 'delete',
      [AccionEnum.READ]: 'visibility'
    };
    return icons[accion] || 'help';
  }
}
