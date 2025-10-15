import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import * as AuthActions from '../core/state/auth/auth.actions';
import { selectCurrentUser, selectUserRole } from '../core/state/auth/auth.selectors';
import { User } from '../models/user.model';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <div class="main-layout">
      <!-- Top Navigation Bar -->
      <nav class="navbar">
        <div class="navbar-brand">
          <img src="assets/logo.png" alt="FRC eFact" class="logo" />
          <span class="brand-text">FRC eFact</span>
        </div>
        
        <div class="navbar-user">
          <div class="user-avatar-container" *ngIf="currentUser">
            <button class="avatar-btn" (click)="toggleUserMenu($event)" [title]="currentUser.username">
              <div class="avatar-circle">
                <i class="fas fa-user"></i>
              </div>
              <span class="user-name-desktop">{{ currentUser.username }}</span>
              <i class="fas fa-chevron-down"></i>
            </button>
            
            <!-- Dropdown Menu -->
            <div class="user-menu" *ngIf="showUserMenu" (click)="$event.stopPropagation()">
              <div class="user-menu-header">
                <div class="avatar-circle-large">
                  <i class="fas fa-user"></i>
                </div>
                <div class="user-menu-info">
                  <span class="user-menu-name">{{ currentUser.username }}</span>
                  <span class="user-menu-role">{{ userRole || 'Usuario' }}</span>
                </div>
              </div>
              <div class="user-menu-divider"></div>
              <ul class="user-menu-list">
                <li class="user-menu-item disabled" title="Próximamente">
                  <i class="fas fa-plus-circle"></i>
                  <span>Nuevo</span>
                </li>
                <li class="user-menu-item disabled" title="Próximamente">
                  <i class="fas fa-user-circle"></i>
                  <span>Perfil</span>
                </li>
                <li class="user-menu-item disabled" title="Próximamente">
                  <i class="fas fa-cog"></i>
                  <span>Configuración</span>
                </li>
              </ul>
              <div class="user-menu-divider"></div>
              <button class="user-menu-logout" (click)="logout()">
                <i class="fas fa-sign-out-alt"></i>
                <span>Salir</span>
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div class="layout-content">
        <!-- Side Navigation Menu -->
        <aside class="sidebar">
          <nav class="sidebar-nav">
            <ul class="nav-menu">
              <li class="nav-item">
                <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-tachometer-alt"></i>
                  <span>Dashboard</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/empresas" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-building"></i>
                  <span>Empresas</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/timbrados" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-stamp"></i>
                  <span>Timbrados</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/productos" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-box"></i>
                  <span>Productos</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/clientes" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-users"></i>
                  <span>Clientes</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/facturacion" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-file-invoice"></i>
                  <span>Facturación</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/documentos" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-file-alt"></i>
                  <span>Documentos Electrónicos</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/reportes" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-chart-bar"></i>
                  <span>Reportes</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/auditoria" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-history"></i>
                  <span>Auditoría</span>
                </a>
              </li>
              
              <li class="nav-item">
                <a routerLink="/test" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-flask"></i>
                  <span>Prueba</span>
                </a>
              </li>
            </ul>
          </nav>
        </aside>

        <!-- Main Content Area -->
        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .main-layout {
      height: 100vh;
      display: flex;
      flex-direction: column;
    }

    /* Top Navigation Bar */
    .navbar {
      background: #2c3e50;
      color: white;
      padding: 0 1rem;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      z-index: 1000;
    }

    .navbar-brand {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .logo {
      height: 32px;
      width: auto;
    }

    .brand-text {
      font-size: 1.5rem;
      font-weight: bold;
      color: #3498db;
    }

    .navbar-user {
      position: relative;
    }

    .user-avatar-container {
      position: relative;
    }

    .avatar-btn {
      background: transparent;
      border: none;
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem;
      border-radius: 8px;
      transition: background-color 0.2s;
    }

    .avatar-btn:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .avatar-circle {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 1rem;
    }

    .avatar-circle-large {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 1.25rem;
    }

    .user-name-desktop {
      font-size: 0.875rem;
      font-weight: 500;
    }

    .avatar-btn i.fa-chevron-down {
      font-size: 0.75rem;
      transition: transform 0.2s;
    }

    .avatar-btn:hover i.fa-chevron-down {
      transform: rotate(180deg);
    }

    /* User Menu Dropdown */
    .user-menu {
      position: absolute;
      top: calc(100% + 0.5rem);
      right: 0;
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      min-width: 250px;
      z-index: 1001;
      overflow: hidden;
    }

    .user-menu-header {
      padding: 1rem;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      background: #f8f9fa;
    }

    .user-menu-info {
      display: flex;
      flex-direction: column;
    }

    .user-menu-name {
      font-weight: 600;
      color: #2c3e50;
      font-size: 0.875rem;
    }

    .user-menu-role {
      color: #7f8c8d;
      font-size: 0.75rem;
    }

    .user-menu-divider {
      height: 1px;
      background: #ecf0f1;
      margin: 0.5rem 0;
    }

    .user-menu-list {
      list-style: none;
      margin: 0;
      padding: 0.5rem 0;
    }

    .user-menu-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
      color: #2c3e50;
      cursor: pointer;
      transition: background-color 0.2s;
      font-size: 0.875rem;
    }

    .user-menu-item:hover:not(.disabled) {
      background: #f8f9fa;
    }

    .user-menu-item.disabled {
      opacity: 0.5;
      cursor: not-allowed;
      color: #95a5a6;
    }

    .user-menu-item i {
      width: 20px;
      text-align: center;
    }

    .user-menu-logout {
      width: 100%;
      background: transparent;
      border: none;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
      color: #e74c3c;
      cursor: pointer;
      transition: background-color 0.2s;
      font-size: 0.875rem;
      font-weight: 500;
    }

    .user-menu-logout:hover {
      background: #fee;
    }

    .user-menu-logout i {
      width: 20px;
      text-align: center;
    }

    /* Layout Content */
    .layout-content {
      flex: 1;
      display: flex;
      overflow: hidden;
    }

    /* Sidebar */
    .sidebar {
      width: 250px;
      background: #34495e;
      color: white;
      overflow-y: auto;
      box-shadow: 2px 0 4px rgba(0,0,0,0.1);
    }

    .sidebar-nav {
      padding: 1rem 0;
    }

    .nav-menu {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    .nav-item {
      margin: 0;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1.5rem;
      color: #bdc3c7;
      text-decoration: none;
      transition: all 0.2s;
      border-left: 3px solid transparent;
    }

    .nav-link:hover {
      background: #2c3e50;
      color: white;
      border-left-color: #3498db;
    }

    .nav-link.active {
      background: #2c3e50;
      color: #3498db;
      border-left-color: #3498db;
      font-weight: 600;
    }

    .nav-link i {
      width: 20px;
      text-align: center;
    }

    /* Main Content */
    .main-content {
      flex: 1;
      overflow-y: auto;
      background: #ecf0f1;
      padding: 1.5rem;
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .sidebar {
        width: 200px;
      }
      
      .main-content {
        padding: 1rem;
      }
      
      .user-name-desktop {
        display: none;
      }

      .user-menu {
        right: -0.5rem;
      }
    }

    @media (max-width: 576px) {
      .sidebar {
        position: fixed;
        left: -250px;
        height: 100%;
        z-index: 999;
        transition: left 0.3s;
      }
      
      .sidebar.open {
        left: 0;
      }
      
      .main-content {
        margin-left: 0;
      }
    }
  `]
})
export class MainLayoutComponent implements OnInit {
  currentUser$: Observable<User | null>;
  userRole$: Observable<string | null>;
  showUserMenu = false;
  currentUser: User | null = null;
  userRole: string | null = null;

  constructor(
    private store: Store,
    private router: Router,
    private authService: AuthService
  ) {
    this.currentUser$ = this.authService.currentUser$;
    this.userRole$ = this.store.select(selectUserRole);
  }

  ngOnInit(): void {
    // Suscribirse al usuario actual
    this.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.userRole = user?.roles?.[0] || null;
      console.log('Usuario actual en layout:', user);
      console.log('Rol del usuario:', this.userRole);
    });

    // Cerrar el menú al hacer clic fuera
    document.addEventListener('click', (event) => {
      if (this.showUserMenu) {
        this.showUserMenu = false;
      }
    });
  }

  toggleUserMenu(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.showUserMenu = !this.showUserMenu;
  }

  logout(): void {
    this.showUserMenu = false;
    this.authService.logout().subscribe(() => {
      this.router.navigate(['/login']);
    });
  }
}