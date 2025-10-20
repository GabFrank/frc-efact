import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-empresa-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ],
  template: `
    <div class="empresa-dashboard-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <button mat-icon-button (click)="goBack()" matTooltip="Volver a empresas">
                <mat-icon>arrow_back</mat-icon>
              </button>
              <h2>Dashboard de Empresa {{ empresaId }}</h2>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <div class="dashboard-content">
            <div class="info-section">
              <h3>Información de la Empresa</h3>
              <p><strong>ID:</strong> {{ empresaId }}</p>
              <p><strong>Estado:</strong>
                <mat-chip [class.active-chip]="true">Activa</mat-chip>
              </p>
            </div>

            <div class="actions-section">
              <h3>Acciones Disponibles</h3>
              <div class="actions-grid">
                <button
                  mat-raised-button
                  color="primary"
                  (click)="goToUsers()"
                  [disabled]="!empresaId">
                  <mat-icon>people</mat-icon>
                  Gestionar Usuarios
                </button>

                <button
                  mat-raised-button
                  color="accent"
                  (click)="goToEdit()"
                  [disabled]="!empresaId">
                  <mat-icon>edit</mat-icon>
                  Editar Empresa
                </button>

                <button
                  mat-raised-button
                  color="warn"
                  (click)="goToTest()"
                  [disabled]="!empresaId">
                  <mat-icon>bug_report</mat-icon>
                  Componente de Prueba
                </button>
              </div>
            </div>

            <div class="status-section">
              <h3>Estado del Sistema</h3>
              <div class="status-items">
                <div class="status-item">
                  <mat-icon color="primary">check_circle</mat-icon>
                  <span>Navegación funcionando</span>
                </div>
                <div class="status-item">
                  <mat-icon color="primary">check_circle</mat-icon>
                  <span>Componentes cargados</span>
                </div>
                <div class="status-item">
                  <mat-icon color="primary">check_circle</mat-icon>
                  <span>Rutas configuradas</span>
                </div>
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .empresa-dashboard-container {
      padding: 20px;
      max-width: 1000px;
      margin: 0 auto;
    }

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }

    .dashboard-content {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .info-section, .actions-section, .status-section {
      padding: 16px;
      border-radius: 8px;
      background-color: #f9f9f9;
    }

    .info-section h3, .actions-section h3, .status-section h3 {
      margin: 0 0 16px 0;
      font-size: 18px;
      font-weight: 500;
      color: #333;
    }

    .info-section p {
      margin: 8px 0;
      font-size: 16px;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
    }

    .status-items {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .status-item {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .status-item span {
      font-size: 14px;
      color: #333;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    button mat-icon {
      margin-right: 8px;
    }

    @media (max-width: 768px) {
      .actions-grid {
        grid-template-columns: 1fr;
      }

      .actions-grid button {
        width: 100%;
      }
    }
  `]
})
export class EmpresaDashboardComponent implements OnInit {
  empresaId: number | null = null;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.empresaId = +params['id'];
    });
  }

  goBack(): void {
    this.router.navigate(['/empresas']);
  }

  goToUsers(): void {
    if (this.empresaId) {
      this.router.navigate(['/empresas', this.empresaId, 'usuarios']);
    }
  }

  goToEdit(): void {
    if (this.empresaId) {
      this.router.navigate(['/empresas', this.empresaId, 'edit']);
    }
  }

  goToTest(): void {
    if (this.empresaId) {
      this.router.navigate(['/empresas', this.empresaId, 'test']);
    }
  }
}
