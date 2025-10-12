import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatGridListModule } from '@angular/material/grid-list';

@Component({
  selector: 'app-reportes-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatGridListModule
  ],
  template: `
    <div class="reportes-list-container">
      <h1>Reportes</h1>
      <p class="subtitle">Seleccione el tipo de reporte que desea generar</p>

      <div class="reportes-grid">
        <mat-card class="reporte-card" routerLink="facturas">
          <mat-card-header>
            <mat-icon class="card-icon">receipt_long</mat-icon>
          </mat-card-header>
          <mat-card-content>
            <h2>Reporte de Facturas</h2>
            <p>Consulte y exporte facturas con filtros avanzados por fecha, cliente, estado y monto.</p>
          </mat-card-content>
          <mat-card-actions>
            <button mat-raised-button color="primary">
              Ver Reporte
              <mat-icon>arrow_forward</mat-icon>
            </button>
          </mat-card-actions>
        </mat-card>

        <mat-card class="reporte-card" routerLink="clientes">
          <mat-card-header>
            <mat-icon class="card-icon">people</mat-icon>
          </mat-card-header>
          <mat-card-content>
            <h2>Reporte por Clientes</h2>
            <p>Analice totales facturados por cliente con gráficos y ranking de mejores clientes.</p>
          </mat-card-content>
          <mat-card-actions>
            <button mat-raised-button color="primary">
              Ver Reporte
              <mat-icon>arrow_forward</mat-icon>
            </button>
          </mat-card-actions>
        </mat-card>

        <mat-card class="reporte-card" routerLink="productos">
          <mat-card-header>
            <mat-icon class="card-icon">inventory_2</mat-icon>
          </mat-card-header>
          <mat-card-content>
            <h2>Reporte por Productos</h2>
            <p>Vea cantidad vendida y monto total por producto con estadísticas detalladas.</p>
          </mat-card-content>
          <mat-card-actions>
            <button mat-raised-button color="primary">
              Ver Reporte
              <mat-icon>arrow_forward</mat-icon>
            </button>
          </mat-card-actions>
        </mat-card>

        <mat-card class="reporte-card" routerLink="usuarios">
          <mat-card-header>
            <mat-icon class="card-icon">person</mat-icon>
          </mat-card-header>
          <mat-card-content>
            <h2>Reporte por Usuarios</h2>
            <p>Consulte facturas creadas por cada usuario con totales y promedios.</p>
          </mat-card-content>
          <mat-card-actions>
            <button mat-raised-button color="primary">
              Ver Reporte
              <mat-icon>arrow_forward</mat-icon>
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .reportes-list-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    h1 {
      margin: 0 0 8px 0;
      font-size: 32px;
      font-weight: 500;
    }

    .subtitle {
      margin: 0 0 32px 0;
      color: #666;
      font-size: 16px;
    }

    .reportes-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 24px;
    }

    .reporte-card {
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      flex-direction: column;
      height: 100%;
    }

    .reporte-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(0,0,0,0.2);
    }

    .reporte-card mat-card-header {
      display: flex;
      justify-content: center;
      padding: 24px 16px 16px;
    }

    .card-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #1976d2;
    }

    .reporte-card mat-card-content {
      flex: 1;
      padding: 16px;
    }

    .reporte-card h2 {
      margin: 0 0 12px 0;
      font-size: 20px;
      font-weight: 500;
      text-align: center;
    }

    .reporte-card p {
      margin: 0;
      color: #666;
      font-size: 14px;
      text-align: center;
      line-height: 1.5;
    }

    .reporte-card mat-card-actions {
      padding: 16px;
      display: flex;
      justify-content: center;
    }

    .reporte-card button {
      width: 100%;
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 8px;
    }

    .reporte-card button mat-icon {
      margin-left: auto;
    }
  `]
})
export class ReportesListComponent {}
