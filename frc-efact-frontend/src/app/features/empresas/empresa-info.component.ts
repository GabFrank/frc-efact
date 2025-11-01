import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-empresa-info',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ],
  template: `
    <div class="empresa-info-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <div class="header-content">
              <button mat-icon-button (click)="goBack()" matTooltip="Volver a empresas">
                <mat-icon>arrow_back</mat-icon>
              </button>
              <h2>Información de Empresa</h2>
            </div>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <div class="info-content">
            <div class="info-section">
              <h3>Información de la Empresa</h3>
              <p><strong>ID de Empresa:</strong> {{ empresaId }}</p>
              <p><strong>Estado:</strong>
                <mat-chip [class.active-chip]="true">Activa</mat-chip>
              </p>
            </div>

            <div class="actions-section">
              <h3>Acciones Disponibles</h3>
              <div class="actions">
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
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .empresa-info-container {
      padding: 20px;
      max-width: 800px;
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

    .info-content {
      padding: 20px 0;
    }

    .info-section, .actions-section {
      margin-bottom: 24px;
    }

    .info-section h3, .actions-section h3 {
      margin: 0 0 16px 0;
      font-size: 18px;
      font-weight: 500;
      color: #333;
    }

    .info-content p {
      margin: 12px 0;
      font-size: 16px;
    }

    .actions {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    button mat-icon {
      margin-right: 8px;
    }

    @media (max-width: 768px) {
      .actions {
        flex-direction: column;
      }

      .actions button {
        width: 100%;
      }
    }
  `]
})
export class EmpresaInfoComponent implements OnInit {
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
}
