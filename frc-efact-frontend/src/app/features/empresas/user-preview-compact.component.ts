import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';

import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-preview-compact',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule
  ],
  template: `
    <div class="user-preview-compact" *ngIf="user">
      <div class="user-main-info">
        <div class="user-details">
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

      <div class="user-roles" *ngIf="getNormalizedRoles(user.roles).length > 0">
        <span class="roles-label">Roles:</span>
        <div class="roles-container">
          <mat-chip *ngFor="let role of getNormalizedRoles(user.roles)" [class]="'role-' + role.toLowerCase()">
            {{ role }}
          </mat-chip>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .user-preview-compact {
      padding: 12px;
      background-color: #f9f9f9;
      border-radius: 8px;
      border: 1px solid #e0e0e0;
    }

    .user-main-info {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 8px;
    }

    .user-details h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }

    .user-details p {
      margin: 2px 0 0 0;
      color: #666;
      font-size: 12px;
    }

    .user-roles {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;
    }

    .roles-label {
      font-size: 12px;
      color: #666;
      font-weight: 500;
    }

    .roles-container {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }

    /* Chip styles */
    .role-admin {
      background-color: #ff9800 !important;
      color: white;
      font-size: 11px;
    }

    .role-empresa_admin {
      background-color: #2196f3 !important;
      color: white;
      font-size: 11px;
    }

    .role-facturador {
      background-color: #4caf50 !important;
      color: white;
      font-size: 11px;
    }

    .role-lector {
      background-color: #9c27b0 !important;
      color: white;
      font-size: 11px;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white;
      font-size: 11px;
    }

    .inactive-chip {
      background-color: #f44336 !important;
      color: white;
      font-size: 11px;
    }

    mat-chip mat-icon {
      margin-right: 4px;
      font-size: 14px;
      width: 14px;
      height: 14px;
    }
  `]
})
export class UserPreviewCompactComponent {
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
}
