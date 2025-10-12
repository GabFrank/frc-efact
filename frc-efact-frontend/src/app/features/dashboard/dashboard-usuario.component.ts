import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { DashboardApiService } from '../../core/api/dashboard-api.service';
import { DashboardUsuario, ActividadReciente } from '../../models/dashboard.model';
import { MetricCardComponent } from '../../shared/components/metric-card/metric-card.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-dashboard-usuario',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatListModule,
    MetricCardComponent,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="dashboard-container">
      <h1 class="dashboard-title">Mi Dashboard</h1>

      <app-loading-spinner *ngIf="loading"></app-loading-spinner>
      <app-error-message *ngIf="error" [message]="error"></app-error-message>

      <div *ngIf="!loading && !error && dashboard" class="dashboard-content">
        <!-- Métricas principales -->
        <div class="metrics-grid">
          <app-metric-card
            label="Empresas"
            [value]="dashboard.cantidadEmpresas"
            subtitle="Empresas con acceso"
            icon="business"
            iconColor="primary">
          </app-metric-card>

          <app-metric-card
            label="Facturas del Mes"
            [value]="dashboard.facturasCreadasMesActual"
            subtitle="Facturas creadas este mes"
            icon="receipt"
            iconColor="success">
          </app-metric-card>

          <app-metric-card
            label="Último Acceso"
            [value]="formatUltimoAcceso(dashboard.ultimoAcceso)"
            subtitle="Última vez que ingresaste"
            icon="schedule"
            iconColor="accent">
          </app-metric-card>
        </div>

        <!-- Últimas actividades -->
        <mat-card class="activities-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>history</mat-icon>
              Últimas Actividades
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <mat-list *ngIf="dashboard.ultimasActividades && dashboard.ultimasActividades.length > 0; else noActivities">
              <mat-list-item *ngFor="let actividad of dashboard.ultimasActividades" class="activity-item">
                <div class="activity-content">
                  <div class="activity-icon">
                    <mat-icon [class]="'action-icon ' + getActionClass(actividad.accion)">
                      {{ getActionIcon(actividad.accion) }}
                    </mat-icon>
                  </div>
                  <div class="activity-details">
                    <div class="activity-description">{{ actividad.descripcion }}</div>
                    <div class="activity-meta">
                      <span class="activity-entity">{{ actividad.entidadTipo }}</span>
                      <span class="activity-separator">•</span>
                      <span class="activity-empresa" *ngIf="actividad.empresaNombre">{{ actividad.empresaNombre }}</span>
                      <span class="activity-separator" *ngIf="actividad.empresaNombre">•</span>
                      <span class="activity-time">{{ formatFechaHora(actividad.fechaHora) }}</span>
                    </div>
                  </div>
                </div>
              </mat-list-item>
            </mat-list>
            <ng-template #noActivities>
              <div class="no-activities">
                <mat-icon>info</mat-icon>
                <p>No hay actividades recientes</p>
              </div>
            </ng-template>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .dashboard-title {
      font-size: 28px;
      font-weight: 500;
      margin: 0 0 24px 0;
      color: rgba(0, 0, 0, 0.87);
    }

    .dashboard-content {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
    }

    .activities-card {
      width: 100%;
    }

    mat-card-header {
      padding: 16px 16px 0;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 500;
    }

    mat-card-title mat-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    mat-card-content {
      padding: 16px 0 0 0;
    }

    mat-list {
      padding: 0;
    }

    .activity-item {
      height: auto !important;
      padding: 16px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.12);
    }

    .activity-item:last-child {
      border-bottom: none;
    }

    .activity-content {
      display: flex;
      gap: 12px;
      width: 100%;
    }

    .activity-icon {
      flex-shrink: 0;
    }

    .action-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .action-icon.create {
      color: #4caf50;
    }

    .action-icon.update {
      color: #2196f3;
    }

    .action-icon.delete {
      color: #f44336;
    }

    .action-icon.login {
      color: #9c27b0;
    }

    .action-icon.logout {
      color: #607d8b;
    }

    .activity-details {
      flex: 1;
      min-width: 0;
    }

    .activity-description {
      font-size: 14px;
      font-weight: 500;
      color: rgba(0, 0, 0, 0.87);
      margin-bottom: 4px;
    }

    .activity-meta {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.54);
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;
    }

    .activity-separator {
      color: rgba(0, 0, 0, 0.38);
    }

    .no-activities {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 20px;
      color: rgba(0, 0, 0, 0.54);
    }

    .no-activities mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      opacity: 0.5;
    }

    .no-activities p {
      margin: 0;
      font-size: 14px;
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 16px;
      }

      .metrics-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class DashboardUsuarioComponent implements OnInit {
  dashboard?: DashboardUsuario;
  loading = false;
  error?: string;

  constructor(private dashboardApi: DashboardApiService) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = undefined;

    this.dashboardApi.getDashboardUsuario().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el dashboard';
        this.loading = false;
        console.error('Error loading dashboard:', err);
      }
    });
  }

  formatUltimoAcceso(fecha?: string): string {
    if (!fecha) return 'Nunca';
    
    const date = new Date(fecha);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Ahora';
    if (diffMins < 60) return `Hace ${diffMins} min`;
    if (diffHours < 24) return `Hace ${diffHours}h`;
    if (diffDays < 7) return `Hace ${diffDays}d`;
    
    return date.toLocaleDateString('es-PY');
  }

  formatFechaHora(fecha: string): string {
    const date = new Date(fecha);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Ahora';
    if (diffMins < 60) return `Hace ${diffMins} minutos`;
    if (diffHours < 24) return `Hace ${diffHours} horas`;
    if (diffDays === 1) return 'Ayer';
    if (diffDays < 7) return `Hace ${diffDays} días`;
    
    return date.toLocaleDateString('es-PY', { 
      day: '2-digit', 
      month: 'short', 
      year: 'numeric' 
    });
  }

  getActionIcon(accion: string): string {
    const icons: Record<string, string> = {
      'CREATE': 'add_circle',
      'UPDATE': 'edit',
      'DELETE': 'delete',
      'LOGIN': 'login',
      'LOGOUT': 'logout'
    };
    return icons[accion] || 'info';
  }

  getActionClass(accion: string): string {
    return accion.toLowerCase();
  }
}
