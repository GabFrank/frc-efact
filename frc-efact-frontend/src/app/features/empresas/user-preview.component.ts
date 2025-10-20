import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-preview',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatDividerModule
  ],
  template: `
    <mat-card class="user-preview-card">
      <mat-card-header>
        <mat-card-title>
          <div class="header-content">
            <mat-icon>person</mat-icon>
            <span>Vista Previa del Usuario</span>
          </div>
        </mat-card-title>
      </mat-card-header>

      <mat-card-content>
        <div class="user-details" *ngIf="user">
          <!-- Información Principal -->
          <div class="main-info">
            <div class="user-name">
              <h3>{{ user.username }}</h3>
              <p class="user-email">{{ user.email }}</p>
            </div>
            <div class="user-status">
              <mat-chip [class]="user.isActive ? 'active-chip' : 'inactive-chip'">
                <mat-icon>{{ user.isActive ? 'check_circle' : 'cancel' }}</mat-icon>
                {{ user.isActive ? 'Activo' : 'Inactivo' }}
              </mat-chip>
            </div>
          </div>

          <mat-divider></mat-divider>

          <!-- Roles del Sistema -->
          <div class="roles-section">
            <h4>Roles del Sistema</h4>
            <div class="roles-container">
              <mat-chip-set>
                <mat-chip *ngFor="let role of getNormalizedRoles(user.roles)" [class]="'role-' + role.toLowerCase()">
                  <mat-icon>{{ getRoleIcon(role) }}</mat-icon>
                  {{ role }}
                </mat-chip>
              </mat-chip-set>
            </div>
          </div>

          <mat-divider></mat-divider>

          <!-- Información Adicional -->
          <div class="additional-info">
            <div class="info-item">
              <mat-icon>calendar_today</mat-icon>
              <div class="info-content">
                <span class="info-label">Creado:</span>
                <span class="info-value">{{ formatDate(user.creadoEn) }}</span>
              </div>
            </div>

            <div class="info-item" *ngIf="user.ultimoLogin">
              <mat-icon>login</mat-icon>
              <div class="info-content">
                <span class="info-label">Último acceso:</span>
                <span class="info-value">{{ formatDateTime(user.ultimoLogin) }}</span>
              </div>
            </div>

            <div class="info-item" *ngIf="user.failedLoginAttempts && user.failedLoginAttempts > 0">
              <mat-icon>warning</mat-icon>
              <div class="info-content">
                <span class="info-label">Intentos fallidos:</span>
                <span class="info-value">{{ user.failedLoginAttempts }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="no-user" *ngIf="!user">
          <mat-icon>person_off</mat-icon>
          <p>No hay usuario seleccionado</p>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .user-preview-card {
      margin: 16px 0;
    }

    .header-content {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .user-details {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .main-info {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
    }

    .user-name h3 {
      margin: 0;
      font-size: 20px;
      font-weight: 500;
    }

    .user-name p {
      margin: 4px 0 0 0;
      color: #666;
      font-size: 14px;
    }

    .roles-section h4 {
      margin: 0 0 8px 0;
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .roles-container {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .additional-info {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .info-content {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .info-label {
      font-size: 12px;
      color: #666;
      font-weight: 500;
    }

    .info-value {
      font-size: 14px;
      color: #333;
    }

    .no-user {
      text-align: center;
      padding: 32px;
      color: #666;
    }

    .no-user mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
    }

    /* Chip styles */
    .role-admin {
      background-color: #ff9800 !important;
      color: white;
    }

    .role-empresa_admin {
      background-color: #2196f3 !important;
      color: white;
    }

    .role-facturador {
      background-color: #4caf50 !important;
      color: white;
    }

    .role-lector {
      background-color: #9c27b0 !important;
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

    mat-chip mat-icon {
      margin-right: 4px;
    }
  `]
})
export class UserPreviewComponent {
  @Input() user: User | null = null;

  getNormalizedRoles(roles: any[]): string[] {
    if (!roles || !Array.isArray(roles)) return [];
    return roles.map(role => {
      if (typeof role === 'string') {
        return role;
      } else if (role && typeof role === 'object') {
        // Si es un objeto Role, usar la propiedad 'nombre'
        return role.nombre || role.name || 'Rol desconocido';
      }
      return 'Rol inválido';
    }).filter(role => role && role !== 'Rol desconocido' && role !== 'Rol inválido');
  }

  getRoleIcon(role: string): string {
    const iconMap: { [key: string]: string } = {
      'ADMIN': 'admin_panel_settings',
      'EMPRESA_ADMIN': 'business',
      'FACTURADOR': 'receipt',
      'LECTOR': 'visibility'
    };
    return iconMap[role] || 'person';
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  formatDateTime(dateString: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
