import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { selectCurrentUser, selectUserRole } from '../../core/state/auth/auth.selectors';
import { User } from '../../models/user.model';
import { DashboardEmpresaComponent } from './dashboard-empresa.component';
import { DashboardUsuarioComponent } from './dashboard-usuario.component';
import { DashboardService, DashboardGeneralDto } from '../../services/dashboard.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, DashboardEmpresaComponent, DashboardUsuarioComponent],
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <h1>Dashboard</h1>
        <p class="welcome-message" *ngIf="currentUser$ | async as user">Bienvenido, {{ user.username }}</p>
      </div>

      <div class="dashboard-content">
        <!-- Dashboard para usuarios con rol EMPRESA -->
        <app-dashboard-empresa 
          *ngIf="(userRole$ | async) === 'ROLE_EMPRESA'"
          class="dashboard-section">
        </app-dashboard-empresa>

        <!-- Dashboard para usuarios con rol USUARIO -->
        <app-dashboard-usuario 
          *ngIf="(userRole$ | async) === 'ROLE_USUARIO'"
          class="dashboard-section">
        </app-dashboard-usuario>

        <!-- Dashboard general - siempre visible -->
        <div class="admin-dashboard">
          <div class="admin-overview">
            <h2>Resumen General</h2>
            <div class="admin-stats">
              <div class="stat-card" *ngIf="!isLoading">
                <div class="stat-icon">
                  <i class="fas fa-building"></i>
                </div>
                <div class="stat-info">
                  <h3>Empresas</h3>
                  <p class="stat-number">{{ totalEmpresas }}</p>
                  <p class="stat-label">Total registradas</p>
                </div>
              </div>

              <div class="stat-card" *ngIf="!isLoading">
                <div class="stat-icon">
                  <i class="fas fa-users"></i>
                </div>
                <div class="stat-info">
                  <h3>Usuarios</h3>
                  <p class="stat-number">{{ totalUsuarios }}</p>
                  <p class="stat-label">Total activos</p>
                </div>
              </div>

              <div class="stat-card" *ngIf="!isLoading">
                <div class="stat-icon">
                  <i class="fas fa-file-invoice"></i>
                </div>
                <div class="stat-info">
                  <h3>Documentos</h3>
                  <p class="stat-number">{{ totalDocumentos }}</p>
                  <p class="stat-label">Este mes</p>
                </div>
              </div>

              <div class="stat-card" *ngIf="!isLoading">
                <div class="stat-icon">
                  <i class="fas fa-chart-line"></i>
                </div>
                <div class="stat-info">
                  <h3>Actividad</h3>
                  <p class="stat-number">{{ actividadHoy }}</p>
                  <p class="stat-label">Acciones hoy</p>
                </div>
              </div>

              <!-- Loading state -->
              <div class="stat-card loading-card" *ngIf="isLoading">
                <div class="stat-icon">
                  <i class="fas fa-spinner fa-spin"></i>
                </div>
                <div class="stat-info">
                  <h3>Cargando...</h3>
                  <p class="stat-label">Obteniendo datos</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Accesos rápidos -->
          <div class="quick-actions">
            <h2>Accesos Rápidos</h2>
            <div class="actions-grid">
              <button (click)="navigateTo('/empresas')" class="action-card">
                <i class="fas fa-building"></i>
                <span>Gestionar Empresas</span>
              </button>
              <button (click)="navigateTo('/productos')" class="action-card">
                <i class="fas fa-box"></i>
                <span>Gestionar Productos</span>
              </button>
              <button (click)="navigateTo('/clientes')" class="action-card">
                <i class="fas fa-users"></i>
                <span>Gestionar Clientes</span>
              </button>
              <button (click)="navigateTo('/facturacion')" class="action-card">
                <i class="fas fa-file-invoice"></i>
                <span>Nueva Factura</span>
              </button>
              <button (click)="navigateTo('/documentos')" class="action-card">
                <i class="fas fa-file-alt"></i>
                <span>Ver Documentos</span>
              </button>
              <button (click)="navigateTo('/reportes')" class="action-card">
                <i class="fas fa-chart-bar"></i>
                <span>Ver Reportes</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 20px;
      background-color: #f8f9fa;
      min-height: 100vh;
    }

    .dashboard-header {
      margin-bottom: 30px;
      padding: 20px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .dashboard-header h1 {
      margin: 0 0 10px 0;
      color: #2c3e50;
      font-size: 2rem;
      font-weight: 600;
    }

    .welcome-message {
      margin: 0;
      color: #7f8c8d;
      font-size: 1.1rem;
    }

    .dashboard-content {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .dashboard-section {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      overflow: hidden;
    }

    .admin-dashboard {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .admin-overview {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .admin-overview h2 {
      margin: 0 0 20px 0;
      color: #2c3e50;
      font-size: 1.5rem;
    }

    .admin-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
    }

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      color: white;
      box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    }

    .stat-card:nth-child(2) {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    .stat-card:nth-child(3) {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    .stat-card:nth-child(4) {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }

    .stat-icon {
      font-size: 2.5rem;
      margin-right: 20px;
      opacity: 0.8;
    }

    .stat-info h3 {
      margin: 0 0 5px 0;
      font-size: 1rem;
      font-weight: 500;
      opacity: 0.9;
    }

    .stat-number {
      margin: 0 0 5px 0;
      font-size: 2rem;
      font-weight: 700;
    }

    .stat-label {
      margin: 0;
      font-size: 0.85rem;
      opacity: 0.8;
    }

    .quick-actions {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .quick-actions h2 {
      margin: 0 0 20px 0;
      color: #2c3e50;
      font-size: 1.5rem;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
    }

    .action-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 30px 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      color: white;
      text-decoration: none;
      border: none;
      width: 100%;
      transition: transform 0.2s, box-shadow 0.2s;
      cursor: pointer;
    }

    .action-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 16px rgba(0,0,0,0.2);
    }

    .action-card:nth-child(2) {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    .action-card:nth-child(3) {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    .action-card:nth-child(4) {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }

    .action-card:nth-child(5) {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    }

    .action-card:nth-child(6) {
      background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
    }

    .action-card i {
      font-size: 2.5rem;
      margin-bottom: 10px;
    }

    .action-card span {
      font-size: 1rem;
      font-weight: 500;
      text-align: center;
    }

    .loading-card {
      background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%) !important;
    }

    .loading-card .stat-icon i {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from {
        transform: rotate(0deg);
      }
      to {
        transform: rotate(360deg);
      }
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 10px;
      }

      .admin-stats {
        grid-template-columns: 1fr;
      }

      .stat-card {
        padding: 15px;
      }

      .stat-icon {
        font-size: 2rem;
        margin-right: 15px;
      }

      .dashboard-header h1 {
        font-size: 1.5rem;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  currentUser$: Observable<User | null>;
  userRole$: Observable<string | null>;

  // Datos del dashboard general
  totalEmpresas = 0;
  totalUsuarios = 0;
  totalDocumentos = 0;
  actividadHoy = 0;
  isLoading = true;

  constructor(
    private store: Store,
    private dashboardService: DashboardService,
    private authService: AuthService,
    private router: Router
  ) {
    this.currentUser$ = this.authService.currentUser$;
    this.userRole$ = this.store.select(selectUserRole);
  }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    this.isLoading = true;
    console.log('Cargando datos del dashboard...');
    
    this.dashboardService.getDashboardGeneral().subscribe({
      next: (data: DashboardGeneralDto) => {
        console.log('Datos del dashboard recibidos:', data);
        this.totalEmpresas = data.totalEmpresas;
        this.totalUsuarios = data.totalUsuarios;
        this.totalDocumentos = data.totalDocumentos;
        this.actividadHoy = data.actividadHoy;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar datos del dashboard:', error);
        console.error('Detalles del error:', error.error);
        console.error('Status:', error.status);
        // Mantener valores en 0 si hay error
        this.isLoading = false;
      }
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}