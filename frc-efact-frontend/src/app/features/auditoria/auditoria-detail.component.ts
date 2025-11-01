import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';

import { AuditLog, AccionEnum } from '../../models/audit.model';

/**
 * Componente para mostrar el detalle de un cambio de auditoría
 * Muestra un diff entre valores anteriores y nuevos
 */
@Component({
  selector: 'app-auditoria-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>compare_arrows</mat-icon>
      Detalle de Cambios
    </h2>

    <mat-dialog-content>
      <!-- Información general -->
      <mat-card class="info-card">
        <mat-card-content>
          <div class="info-grid">
            <div class="info-item">
              <label>Usuario:</label>
              <span>{{ data.usuario.username }} ({{ data.usuario.nombreCompleto }})</span>
            </div>
            <div class="info-item">
              <label>Fecha:</label>
              <span>{{ data.fechaHora | date:'dd/MM/yyyy HH:mm:ss' }}</span>
            </div>
            <div class="info-item">
              <label>Entidad:</label>
              <span>{{ data.entidadTipo }} (ID: {{ data.entidadId }})</span>
            </div>
            <div class="info-item">
              <label>Acción:</label>
              <mat-chip [class]="'chip-' + data.accion.toLowerCase()">
                {{ getAccionLabel(data.accion) }}
              </mat-chip>
            </div>
            <div class="info-item" *ngIf="data.empresa">
              <label>Empresa:</label>
              <span>{{ data.empresa.razonSocial }}</span>
            </div>
            <div class="info-item" *ngIf="data.ipAddress">
              <label>IP:</label>
              <span>{{ data.ipAddress }}</span>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-divider></mat-divider>

      <!-- Comparación de valores -->
      <div class="changes-container" *ngIf="hasChanges()">
        <h3>Cambios Realizados</h3>
        
        <div class="comparison-grid">
          <div class="comparison-header">
            <div class="old-value-header">
              <mat-icon>history</mat-icon>
              Valor Anterior
            </div>
            <div class="field-header">Campo</div>
            <div class="new-value-header">
              <mat-icon>update</mat-icon>
              Valor Nuevo
            </div>
          </div>

          <div 
            *ngFor="let field of getChangedFields()" 
            class="comparison-row"
            [class.highlighted]="isFieldChanged(field)">
            
            <div class="old-value">
              <code>{{ formatValue(data.valoresAnteriores?.[field]) }}</code>
            </div>
            
            <div class="field-name">
              <strong>{{ formatFieldName(field) }}</strong>
            </div>
            
            <div class="new-value">
              <code>{{ formatValue(data.valoresNuevos?.[field]) }}</code>
            </div>
          </div>
        </div>
      </div>

      <!-- Solo valores nuevos (CREATE) -->
      <div class="values-container" *ngIf="data.accion === AccionEnum.CREATE && data.valoresNuevos">
        <h3>Valores Creados</h3>
        <div class="values-list">
          <div *ngFor="let field of getNewFields()" class="value-item">
            <label>{{ formatFieldName(field) }}:</label>
            <code>{{ formatValue(data.valoresNuevos[field]) }}</code>
          </div>
        </div>
      </div>

      <!-- Solo valores anteriores (DELETE) -->
      <div class="values-container" *ngIf="data.accion === AccionEnum.DELETE && data.valoresAnteriores">
        <h3>Valores Eliminados</h3>
        <div class="values-list">
          <div *ngFor="let field of getOldFields()" class="value-item deleted">
            <label>{{ formatFieldName(field) }}:</label>
            <code>{{ formatValue(data.valoresAnteriores[field]) }}</code>
          </div>
        </div>
      </div>

      <!-- Descripción adicional -->
      <div class="description-container" *ngIf="data.descripcion">
        <mat-card>
          <mat-card-header>
            <mat-card-title>Descripción</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>{{ data.descripcion }}</p>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- User Agent -->
      <div class="user-agent-container" *ngIf="data.userAgent">
        <details>
          <summary>Información del navegador</summary>
          <code>{{ data.userAgent }}</code>
        </details>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="close()">Cerrar</button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2 {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    mat-dialog-content {
      min-width: 600px;
      max-height: 70vh;
      overflow-y: auto;
    }

    .info-card {
      margin-bottom: 20px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .info-item label {
      font-weight: 500;
      color: rgba(0, 0, 0, 0.6);
      font-size: 12px;
    }

    .info-item span {
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

    mat-divider {
      margin: 20px 0;
    }

    .changes-container,
    .values-container {
      margin-top: 20px;
    }

    h3 {
      margin-bottom: 16px;
      color: rgba(0, 0, 0, 0.87);
    }

    .comparison-grid {
      border: 1px solid #e0e0e0;
      border-radius: 4px;
      overflow: hidden;
    }

    .comparison-header {
      display: grid;
      grid-template-columns: 1fr auto 1fr;
      gap: 16px;
      padding: 12px;
      background-color: #f5f5f5;
      font-weight: 500;
      border-bottom: 2px solid #e0e0e0;
    }

    .comparison-header > div {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .field-header {
      text-align: center;
      min-width: 150px;
    }

    .comparison-row {
      display: grid;
      grid-template-columns: 1fr auto 1fr;
      gap: 16px;
      padding: 12px;
      border-bottom: 1px solid #e0e0e0;
      transition: background-color 0.2s;
    }

    .comparison-row:last-child {
      border-bottom: none;
    }

    .comparison-row.highlighted {
      background-color: #fff3e0;
    }

    .comparison-row:hover {
      background-color: #fafafa;
    }

    .old-value,
    .new-value {
      padding: 8px;
      border-radius: 4px;
      background-color: #f9f9f9;
    }

    .old-value {
      border-left: 3px solid #f44336;
    }

    .new-value {
      border-left: 3px solid #4caf50;
    }

    .field-name {
      display: flex;
      align-items: center;
      justify-content: center;
      text-align: center;
      min-width: 150px;
      padding: 0 16px;
    }

    code {
      font-family: 'Courier New', monospace;
      font-size: 13px;
      word-break: break-word;
      white-space: pre-wrap;
    }

    .values-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .value-item {
      display: grid;
      grid-template-columns: 200px 1fr;
      gap: 16px;
      padding: 12px;
      border: 1px solid #e0e0e0;
      border-radius: 4px;
      background-color: #f9f9f9;
    }

    .value-item.deleted {
      background-color: #ffebee;
      border-color: #f44336;
    }

    .value-item label {
      font-weight: 500;
      color: rgba(0, 0, 0, 0.87);
    }

    .description-container {
      margin-top: 20px;
    }

    .user-agent-container {
      margin-top: 16px;
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }

    .user-agent-container summary {
      cursor: pointer;
      font-weight: 500;
      margin-bottom: 8px;
    }

    .user-agent-container code {
      display: block;
      margin-top: 8px;
      padding: 8px;
      background-color: white;
      border-radius: 4px;
      font-size: 11px;
    }
  `]
})
export class AuditoriaDetailComponent {
  AccionEnum = AccionEnum;

  constructor(
    public dialogRef: MatDialogRef<AuditoriaDetailComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AuditLog
  ) {}

  close(): void {
    this.dialogRef.close();
  }

  hasChanges(): boolean {
    return this.data.accion === AccionEnum.UPDATE && 
           (!!this.data.valoresAnteriores || !!this.data.valoresNuevos);
  }

  getChangedFields(): string[] {
    const fields = new Set<string>();
    
    if (this.data.valoresAnteriores) {
      Object.keys(this.data.valoresAnteriores).forEach(key => fields.add(key));
    }
    
    if (this.data.valoresNuevos) {
      Object.keys(this.data.valoresNuevos).forEach(key => fields.add(key));
    }
    
    return Array.from(fields).sort();
  }

  getNewFields(): string[] {
    return this.data.valoresNuevos ? Object.keys(this.data.valoresNuevos).sort() : [];
  }

  getOldFields(): string[] {
    return this.data.valoresAnteriores ? Object.keys(this.data.valoresAnteriores).sort() : [];
  }

  isFieldChanged(field: string): boolean {
    const oldValue = this.data.valoresAnteriores?.[field];
    const newValue = this.data.valoresNuevos?.[field];
    return JSON.stringify(oldValue) !== JSON.stringify(newValue);
  }

  formatFieldName(field: string): string {
    // Convert camelCase to Title Case
    return field
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }

  formatValue(value: any): string {
    if (value === null || value === undefined) {
      return '(vacío)';
    }
    
    if (typeof value === 'object') {
      return JSON.stringify(value, null, 2);
    }
    
    if (typeof value === 'boolean') {
      return value ? 'Sí' : 'No';
    }
    
    return String(value);
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
}
