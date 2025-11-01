import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';

import { User } from '../../models/user.model';

export interface UserAssignmentConfirmationData {
  user: User;
  empresaId: number;
  empresaNombre: string;
  rolEmpresa: string;
}

@Component({
  selector: 'app-user-assignment-confirmation',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDividerModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>warning</mat-icon>
      Confirmar Asignación de Usuario
    </h2>

    <mat-dialog-content>
      <div class="confirmation-content">
        <div class="warning-section">
          <mat-icon color="warn">warning</mat-icon>
          <p><strong>¡Atención!</strong> Está a punto de asignar un usuario a esta empresa.</p>
        </div>

        <mat-divider></mat-divider>

        <div class="assignment-details">
          <h3>Detalles de la Asignación</h3>

          <div class="detail-item">
            <mat-icon>person</mat-icon>
            <div class="detail-content">
              <span class="detail-label">Usuario:</span>
              <span class="detail-value">{{ data.user.username }}</span>
            </div>
          </div>

          <div class="detail-item">
            <mat-icon>email</mat-icon>
            <div class="detail-content">
              <span class="detail-label">Email:</span>
              <span class="detail-value">{{ data.user.email }}</span>
            </div>
          </div>

          <div class="detail-item">
            <mat-icon>business</mat-icon>
            <div class="detail-content">
              <span class="detail-label">Empresa:</span>
              <span class="detail-value">{{ data.empresaNombre }}</span>
            </div>
          </div>

          <div class="detail-item">
            <mat-icon>admin_panel_settings</mat-icon>
            <div class="detail-content">
              <span class="detail-label">Rol asignado:</span>
              <span class="detail-value">{{ data.rolEmpresa }}</span>
            </div>
          </div>
        </div>

        <mat-divider></mat-divider>

        <div class="permissions-info">
          <h4>Permisos que tendrá el usuario:</h4>
          <ul *ngIf="data.rolEmpresa === 'ADMINISTRADOR'">
            <li>Gestionar usuarios de la empresa</li>
            <li>Configurar parámetros de la empresa</li>
            <li>Ver toda la información de la empresa</li>
            <li>Crear y editar facturas</li>
          </ul>
          <ul *ngIf="data.rolEmpresa === 'FACTURADOR'">
            <li>Crear y editar facturas</li>
            <li>Gestionar productos y clientes</li>
            <li>Generar documentos electrónicos</li>
            <li>Ver información de facturación</li>
          </ul>
          <ul *ngIf="data.rolEmpresa === 'LECTOR'">
            <li>Ver información de la empresa</li>
            <li>Consultar facturas</li>
            <li>Generar reportes básicos</li>
          </ul>
        </div>

        <div class="final-warning">
          <mat-icon color="warn">info</mat-icon>
          <p>Una vez confirmado, el usuario tendrá acceso inmediato a esta empresa según los permisos asignados.</p>
        </div>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">
        <mat-icon>cancel</mat-icon>
        Cancelar
      </button>
      <button mat-raised-button color="primary" (click)="onConfirm()">
        <mat-icon>check</mat-icon>
        Confirmar Asignación
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2 mat-dialog-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .confirmation-content {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .warning-section {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background-color: #fff3e0;
      border-radius: 8px;
      border-left: 4px solid #ff9800;
    }

    .assignment-details {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .assignment-details h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
      color: #333;
    }

    .detail-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;
    }

    .detail-content {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .detail-label {
      font-size: 12px;
      color: #666;
      font-weight: 500;
    }

    .detail-value {
      font-size: 14px;
      color: #333;
      font-weight: 500;
    }

    .permissions-info {
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 8px;
    }

    .permissions-info h4 {
      margin: 0 0 12px 0;
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .permissions-info ul {
      margin: 0;
      padding-left: 20px;
    }

    .permissions-info li {
      margin: 4px 0;
      font-size: 14px;
      color: #555;
    }

    .final-warning {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px;
      background-color: #e3f2fd;
      border-radius: 8px;
      border-left: 4px solid #2196f3;
    }

    .final-warning p {
      margin: 0;
      font-size: 14px;
      color: #1976d2;
    }

    mat-dialog-actions button mat-icon {
      margin-right: 8px;
    }
  `]
})
export class UserAssignmentConfirmationComponent {
  constructor(
    public dialogRef: MatDialogRef<UserAssignmentConfirmationComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserAssignmentConfirmationData
  ) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
