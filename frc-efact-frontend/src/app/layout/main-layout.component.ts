import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, RouterModule, NavigationEnd } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil, map, filter } from 'rxjs/operators';
import { selectUserRole, selectCurrentUser, selectHasRole, selectUserRoles } from '../core/state/auth/auth.selectors';
import { selectAllEmpresas, selectSelectedEmpresa, selectEmpresasLoading } from '../core/state/empresas/empresas.selectors';
import { loadMisEmpresas, selectEmpresa } from '../core/state/empresas/empresas.actions';
import { User } from '../models/user.model';
import { Empresa } from '../models/empresa.model';
import { AuthService } from '../services/auth.service';
import { ConnectionStatusBannerComponent } from '../components/connection-status-banner/connection-status-banner.component';
import { PermissionsService } from '../core/services/permissions.service';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule, ConnectionStatusBannerComponent, MatIconModule],
  template: `
    <div class="main-layout">
      <!-- Top Navigation Bar -->
      <nav class="navbar">
        <div class="navbar-brand">
          <!-- Botón hamburguesa para mobile -->
          <button class="mobile-menu-toggle" (click)="toggleMobileMenu()" aria-label="Toggle menu" type="button">
            <span class="mobile-menu-icon" *ngIf="!isMobileMenuOpen">☰</span>
            <span class="mobile-menu-icon" *ngIf="isMobileMenuOpen">✕</span>
          </button>
          <!-- <span class="brand-text">FRC eFact</span> -->
        </div>

        <!-- Company Selector -->
        <div class="navbar-company" *ngIf="currentUser">
          <label class="company-label">Empresa seleccionada:</label>
          <div class="company-selector-container">
            <button class="company-selector-btn" (click)="toggleCompanyMenu($event)" [title]="selectedEmpresa?.razonSocial || 'Seleccionar empresa'">
              <i class="fas fa-building"></i>
              <span class="company-name">{{ selectedEmpresa?.razonSocial || 'Seleccionar empresa' }}</span>
              <i class="fas fa-chevron-down"></i>
            </button>

            <!-- Company Dropdown Menu -->
            <div class="company-menu" *ngIf="showCompanyMenu" (click)="$event.stopPropagation()">
              <ul class="company-menu-list">
                <li class="company-menu-item" (click)="navigateToNewEmpresa()" *ngIf="canAccessEmpresas">
                  <i class="fas fa-plus-circle"></i>
                  <span>Crear nueva empresa</span>
                </li>
                <li class="company-menu-divider" *ngIf="canAccessEmpresas && empresas.length > 0"></li>
                <li 
                  class="company-menu-item" 
                  *ngFor="let empresa of empresas"
                  [class.selected]="selectedEmpresa?.id === empresa.id"
                  (click)="selectEmpresaById(empresa.id)">
                  <i class="fas fa-building"></i>
                  <div class="company-menu-item-info">
                    <span class="company-menu-item-name">{{ empresa.razonSocial }}</span>
                    <span class="company-menu-item-ruc" *ngIf="empresa.ruc">RUC: {{ empresa.ruc }}</span>
                  </div>
                  <i class="fas fa-check" *ngIf="selectedEmpresa?.id === empresa.id"></i>
                </li>
                <li class="company-menu-empty" *ngIf="!empresasLoading && empresas.length === 0">
                  <span>No tienes empresas asignadas</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div class="navbar-user">
          <div class="user-avatar-container" *ngIf="currentUser">
            <button class="avatar-btn" (click)="toggleUserMenu($event)" [title]="currentUser.username">
              <div class="avatar-circle">
                <img *ngIf="currentUser.imagenPerfil" [src]="currentUser.imagenPerfil" [alt]="currentUser.username" class="avatar-img" />
                <mat-icon *ngIf="!currentUser.imagenPerfil" class="avatar-placeholder-icon">person</mat-icon>
              </div>
              <span class="user-name-desktop">{{ currentUser.username }}</span>
              <i class="fas fa-chevron-down"></i>
            </button>

            <!-- Dropdown Menu -->
            <div class="user-menu" *ngIf="showUserMenu" (click)="$event.stopPropagation()">
              <div class="user-menu-header">
                <div class="avatar-circle-large">
                  <img *ngIf="currentUser.imagenPerfil" [src]="currentUser.imagenPerfil" [alt]="currentUser.username" class="avatar-img" />
                  <mat-icon *ngIf="!currentUser.imagenPerfil" class="avatar-placeholder-icon avatar-placeholder-icon-large">person</mat-icon>
                </div>
                <div class="user-menu-info">
                  <span class="user-menu-name">{{ currentUser.username }}</span>
                  <span class="user-menu-role">{{ userRole || 'Usuario' }}</span>
                </div>
              </div>
              <div class="user-menu-divider"></div>
              <ul class="user-menu-list">
                <li class="user-menu-item" (click)="navigateToEmpresas()" title="Gestión de empresas" *ngIf="canAccessEmpresas">
                  <i class="fas fa-building"></i>
                  <span>Gestión de Empresas</span>
                </li>
                <li class="user-menu-item" (click)="navigateToProfile()" title="Ver perfil de usuario">
                  <i class="fas fa-user-circle"></i>
                  <span>Perfil</span>
                </li>
                <li class="user-menu-item" (click)="navigateToSettings()" title="Configuración del sistema">
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

      <!-- Overlay para mobile cuando el menú está abierto -->
      <div class="mobile-overlay" *ngIf="isMobileMenuOpen" (click)="closeMobileMenu()"></div>

      <div class="layout-content">
        <!-- Side Navigation Menu -->
        <aside class="sidebar" [class.open]="isMobileMenuOpen">
          <!-- Logo en la parte superior de la sidebar -->
          <div class="sidebar-logo">
            <div class="sidebar-logo-circle">
              <img src="assets/256.png" alt="FRC eFact" class="sidebar-logo-img" />
            </div>
            <!-- Botón cerrar para mobile -->
            <button class="sidebar-close-mobile" (click)="closeMobileMenu()" aria-label="Cerrar menú">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <nav class="sidebar-nav">
            <ul class="nav-menu">
              <!-- Items Simples -->
              <li class="nav-item">
                <a routerLink="/dashboard" routerLinkActive="active" class="nav-link" (click)="onNavLinkClick()">
                  <i class="fas fa-tachometer-alt"></i>
                  <span>Dashboard</span>
                </a>
              </li>

              <!-- Sección: Gestión de Empresa -->
              <li class="nav-item nav-section" 
                  [class.expanded]="expandedSections['gestionEmpresa']"
                  [class.disabled]="!selectedEmpresa"
                  *ngIf="selectedEmpresa">
                <button 
                  class="nav-section-header" 
                  (click)="toggleSection('gestionEmpresa')"
                  [disabled]="!selectedEmpresa"
                  [title]="!selectedEmpresa ? 'Selecciona una empresa primero' : ''">
                  <i class="fas fa-building"></i>
                  <span>Gestión de Empresa</span>
                  <i class="fas fa-chevron-right nav-chevron" [class.rotated]="expandedSections['gestionEmpresa']"></i>
                </button>
                <ul class="nav-section-content" *ngIf="expandedSections['gestionEmpresa']">
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? ['/empresas', selectedEmpresa.id, 'productos'] : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [class.disabled]="!selectedEmpresa"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-box"></i>
                      <span>Productos</span>
                    </a>
                  </li>
                  <li class="nav-sub-item" *ngIf="canAccessClientes">
                    <a [routerLink]="selectedEmpresa ? '/clientes' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [class.disabled]="!selectedEmpresa"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                      <i class="fas fa-users"></i>
                      <span>Clientes</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? ['/transporte/vehiculos/empresa', selectedEmpresa.id] : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [class.disabled]="!selectedEmpresa">
                      <i class="fas fa-truck"></i>
                      <span>Vehículos</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? ['/transporte/choferes/empresa', selectedEmpresa.id] : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [class.disabled]="!selectedEmpresa">
                      <i class="fas fa-id-card"></i>
                      <span>Choferes</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? ['/timbrados'] : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                      <i class="fas fa-stamp"></i>
                      <span>Timbrados</span>
                    </a>
                  </li>
                  <li class="nav-sub-item" *ngIf="canAccessUsuarios">
                    <a [routerLink]="selectedEmpresa ? '/usuarios' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                      <i class="fas fa-user-cog"></i>
                      <span>Usuarios</span>
                    </a>
                  </li>
                </ul>
              </li>

              <!-- Sección: Facturación -->
              <li class="nav-item nav-section" 
                  [class.expanded]="expandedSections['facturacion']"
                  [class.disabled]="!selectedEmpresa"
                  *ngIf="canAccessFacturacion">
                <button 
                  class="nav-section-header" 
                  (click)="toggleSection('facturacion')"
                  [disabled]="!selectedEmpresa"
                  [title]="!selectedEmpresa ? 'Selecciona una empresa primero' : ''">
                  <i class="fas fa-file-invoice"></i>
                  <span>Facturación</span>
                  <i class="fas fa-chevron-right nav-chevron" [class.rotated]="expandedSections['facturacion']"></i>
                </button>
                <ul class="nav-section-content" *ngIf="expandedSections['facturacion']">
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/facturacion' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-list"></i>
                      <span>Lista de Facturas</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/facturacion/nueva' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-plus-circle"></i>
                      <span>Nueva Factura</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/notas/notas-credito' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-file-invoice-dollar"></i>
                      <span>Notas de Crédito</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/notas/notas-debito' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-file-invoice-dollar"></i>
                      <span>Notas de Débito</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/notas/notas-remision' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-file-alt"></i>
                      <span>Notas de Remisión</span>
                    </a>
                  </li>
                </ul>
              </li>

              <!-- Sección: Documentos Electrónicos -->
              <li class="nav-item nav-section" 
                  [class.expanded]="expandedSections['documentos']"
                  [class.disabled]="!selectedEmpresa"
                  *ngIf="canAccessDocumentos">
                <button 
                  class="nav-section-header" 
                  (click)="toggleSection('documentos')"
                  [disabled]="!selectedEmpresa"
                  [title]="!selectedEmpresa ? 'Selecciona una empresa primero' : ''">
                  <i class="fas fa-file-alt"></i>
                  <span>Documentos Electrónicos</span>
                  <i class="fas fa-chevron-right nav-chevron" [class.rotated]="expandedSections['documentos']"></i>
                </button>
                <ul class="nav-section-content" *ngIf="expandedSections['documentos']">
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/documentos' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-list"></i>
                      <span>Lista de Documentos</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <div class="nav-sub-section" [class.expanded]="expandedSections['eventos']">
                      <button 
                        class="nav-sub-section-header" 
                        (click)="toggleSection('eventos')">
                        <i class="fas fa-cog"></i>
                        <span>Gestión de Eventos</span>
                        <i class="fas fa-chevron-right nav-chevron" [class.rotated]="expandedSections['eventos']"></i>
                      </button>
                      <ul class="nav-sub-section-content" *ngIf="expandedSections['eventos']">
                        <li class="nav-sub-sub-item">
                          <a [routerLink]="selectedEmpresa ? '/documentos/cancelacion' : null" 
                             routerLinkActive="active" 
                             class="nav-link"
                             [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                             (click)="onNavLinkClick()">
                            <i class="fas fa-ban"></i>
                            <span>Cancelación</span>
                          </a>
                        </li>
                        <li class="nav-sub-sub-item">
                          <a [routerLink]="selectedEmpresa ? '/documentos/nominacion' : null" 
                             routerLinkActive="active" 
                             class="nav-link"
                             [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                             (click)="onNavLinkClick()">
                            <i class="fas fa-user-tag"></i>
                            <span>Nominación</span>
                          </a>
                        </li>
                        <li class="nav-sub-sub-item">
                          <a [routerLink]="selectedEmpresa ? '/documentos/inutilizacion' : null" 
                             routerLinkActive="active" 
                             class="nav-link"
                             [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                             (click)="onNavLinkClick()">
                            <i class="fas fa-times-circle"></i>
                            <span>Inutilización</span>
                          </a>
                        </li>
                      </ul>
                    </div>
                  </li>
                </ul>
              </li>

              <!-- Sección: Reportes -->
              <li class="nav-item nav-section" 
                  [class.expanded]="expandedSections['reportes']"
                  [class.disabled]="!selectedEmpresa"
                  *ngIf="canAccessReportes">
                <button 
                  class="nav-section-header" 
                  (click)="toggleSection('reportes')"
                  [disabled]="!selectedEmpresa"
                  [title]="!selectedEmpresa ? 'Selecciona una empresa primero' : ''">
                  <i class="fas fa-chart-bar"></i>
                  <span>Reportes</span>
                  <i class="fas fa-chevron-right nav-chevron" [class.rotated]="expandedSections['reportes']"></i>
                </button>
                <ul class="nav-section-content" *ngIf="expandedSections['reportes']">
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/reportes/facturas' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-file-invoice"></i>
                      <span>Reporte de Facturas</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/reportes/clientes' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-users"></i>
                      <span>Reporte de Clientes</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/reportes/productos' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-box"></i>
                      <span>Reporte de Productos</span>
                    </a>
                  </li>
                  <li class="nav-sub-item">
                    <a [routerLink]="selectedEmpresa ? '/reportes/usuarios' : null" 
                       routerLinkActive="active" 
                       class="nav-link"
                       [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                       (click)="onNavLinkClick()">
                      <i class="fas fa-user-cog"></i>
                      <span>Reporte de Usuarios</span>
                    </a>
                  </li>
                </ul>
              </li>

              <!-- Item Simple: Auditoría (Deshabilitado) -->
              <li class="nav-item" *ngIf="canAccessAuditoria">
                <div class="nav-link disabled">
                  <i class="fas fa-history"></i>
                  <span>Auditoría</span>
                  <span class="badge-coming-soon">Próximamente</span>
                </div>
              </li>

              <!-- Sección: Administración -->
              <li class="nav-item nav-section" 
                  [class.expanded]="expandedSections['administracion']"
                  *ngIf="isAdmin">
                <button 
                  class="nav-section-header" 
                  (click)="toggleSection('administracion')">
                  <i class="fas fa-cog"></i>
                  <span>Administración</span>
                  <i class="fas fa-chevron-right nav-chevron" [class.rotated]="expandedSections['administracion']"></i>
                </button>
                <ul class="nav-section-content" *ngIf="expandedSections['administracion']">
                  <li class="nav-sub-item">
                    <a routerLink="/usuarios" routerLinkActive="active" class="nav-link" (click)="onNavLinkClick()">
                      <i class="fas fa-user-cog"></i>
                      <span>Usuarios</span>
                    </a>
                  </li>
                </ul>
              </li>

              <!-- Item Simple: Perfil -->
              <li class="nav-item">
                <a routerLink="/perfil" routerLinkActive="active" class="nav-link" (click)="onNavLinkClick()">
                  <i class="fas fa-user-circle"></i>
                  <span>Perfil</span>
                </a>
              </li>
            </ul>
          </nav>
          <div class="sidebar-footer">
            <span class="sidebar-version">Versión: {{ appVersion }}</span>
          </div>
        </aside>

        <!-- Main Content Area -->
        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </div>
      
      <!-- Connection Status Banner -->
      <app-connection-status-banner></app-connection-status-banner>
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
      position: relative;
      min-height: 60px;
    }

    .navbar-brand {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      flex-shrink: 0;
      z-index: 1001;
    }

    .mobile-menu-toggle {
      display: none;
      background: transparent;
      border: none;
      color: white;
      cursor: pointer;
      padding: 0.5rem;
      border-radius: 4px;
      transition: background-color 0.2s;
      flex-shrink: 0;
      min-width: 44px;
      width: 44px;
      height: 44px;
      position: relative;
      z-index: 1002;
      margin-right: 0.5rem;
    }

    .mobile-menu-toggle:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .mobile-menu-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      line-height: 1;
      font-weight: bold;
      color: white;
      width: 100%;
      height: 100%;
    }

    .mobile-overlay {
      display: none;
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

    .navbar-company {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.75rem;
      margin: 0 2rem;
      min-width: 0;
      z-index: 1000;
    }

    .company-label {
      color: white;
      font-size: 0.875rem;
      font-weight: 500;
      white-space: nowrap;
    }

    .company-selector-container {
      position: relative;
      max-width: 400px;
      width: 100%;
      min-width: 0;
    }

    .company-selector-btn {
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.5rem 1rem;
      border-radius: 8px;
      transition: all 0.2s;
      width: 100%;
      font-size: 0.875rem;
    }

    .company-selector-btn:hover {
      background: rgba(255, 255, 255, 0.15);
      border-color: rgba(255, 255, 255, 0.3);
    }

    .company-selector-btn i.fa-building {
      font-size: 1rem;
    }

    .company-name {
      flex: 1;
      text-align: left;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .company-selector-btn i.fa-chevron-down {
      font-size: 0.75rem;
      transition: transform 0.2s;
    }

    .company-selector-btn:hover i.fa-chevron-down {
      transform: rotate(180deg);
    }

    /* Company Menu Dropdown */
    .company-menu {
      position: absolute;
      top: calc(100% + 0.5rem);
      left: 50%;
      transform: translateX(-50%);
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      min-width: 300px;
      max-width: 400px;
      width: 100%;
      z-index: 1001;
      overflow: hidden;
      max-height: 400px;
      overflow-y: auto;
    }

    .company-menu-list {
      list-style: none;
      margin: 0;
      padding: 0.5rem 0;
    }

    .company-menu-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
      color: #2c3e50;
      cursor: pointer;
      transition: background-color 0.2s;
      font-size: 0.875rem;
    }

    .company-menu-item:hover {
      background: #f8f9fa;
    }

    .company-menu-item.selected {
      background: #e3f2fd;
      color: #1976d2;
    }

    .company-menu-item i.fa-building,
    .company-menu-item i.fa-plus-circle {
      width: 20px;
      text-align: center;
      color: #7f8c8d;
    }

    .company-menu-item.selected i.fa-building {
      color: #1976d2;
    }

    .company-menu-item i.fa-check {
      margin-left: auto;
      color: #1976d2;
    }

    .company-menu-item-info {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-width: 0;
    }

    .company-menu-item-name {
      font-weight: 500;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .company-menu-item-ruc {
      font-size: 0.75rem;
      color: #7f8c8d;
    }

    .company-menu-divider {
      height: 1px;
      background: #ecf0f1;
      margin: 0.5rem 0;
    }

    .company-menu-empty {
      padding: 1rem;
      text-align: center;
      color: #7f8c8d;
      font-size: 0.875rem;
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
      overflow: hidden;
    }

    .avatar-placeholder-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: rgba(255, 255, 255, 0.95);
    }

    .avatar-placeholder-icon-large {
      font-size: 40px;
      width: 40px;
      height: 40px;
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
      overflow: hidden;
    }

    .avatar-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 50%;
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

    .user-menu-item:hover {
      background: #f8f9fa;
    }

    .user-menu-item:active {
      background: #e9ecef;
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
      display: flex;
      flex-direction: column;
    }

    /* Logo en la sidebar */
    .sidebar-logo {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 1.5rem 1rem;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      margin-bottom: 0.5rem;
      position: relative;
    }

    .sidebar-logo-circle {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }

    .sidebar-close-mobile {
      display: none;
      position: absolute;
      top: 1rem;
      right: 1rem;
      background: rgba(255, 255, 255, 0.1);
      border: none;
      color: white;
      font-size: 1.25rem;
      cursor: pointer;
      padding: 0.5rem;
      border-radius: 4px;
      transition: background-color 0.2s;
      z-index: 10;
    }

    .sidebar-close-mobile:hover {
      background: rgba(255, 255, 255, 0.2);
    }

    .sidebar-logo-img {
      height: 72px;
      width: auto;
      max-width: 72px;
      object-fit: contain;
    }

    .sidebar-nav {
      padding: 1rem 0;
      flex: 1;
    }

    .sidebar-footer {
      padding: 1rem 1.5rem;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      margin-top: auto;
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .sidebar-version {
      font-size: 0.8rem;
      color: rgba(255, 255, 255, 0.7);
      text-align: center;
    }

    .nav-menu {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    .nav-item {
      margin: 0;
      display: block;
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

    .nav-link.disabled:hover {
      background: transparent;
      color: #bdc3c7;
      border-left-color: transparent;
    }

    .nav-link i {
      width: 20px;
      text-align: center;
    }

    /* Secciones Expandibles */
    .nav-section {
      margin: 0;
    }

    .nav-section-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1.5rem;
      color: #bdc3c7;
      text-decoration: none;
      transition: all 0.2s;
      border-left: 3px solid transparent;
      background: transparent;
      border: none;
      width: 100%;
      text-align: left;
      cursor: pointer;
      font-size: inherit;
      font-family: inherit;
    }

    .nav-section-header:hover:not(:disabled) {
      background: #2c3e50;
      color: white;
      border-left-color: #3498db;
    }

    .nav-section-header:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .nav-section-header i {
      width: 20px;
      text-align: center;
      color: inherit;
    }

    .nav-chevron {
      margin-left: auto;
      font-size: 0.75rem;
      transition: transform 0.3s ease;
    }

    .nav-chevron.rotated {
      transform: rotate(90deg);
    }

    .nav-section-content {
      list-style: none;
      margin: 0;
      padding: 0;
      background: rgba(0, 0, 0, 0.1);
      overflow: hidden;
      max-height: 0;
      transition: max-height 0.3s ease;
    }

    .nav-section.expanded .nav-section-content {
      max-height: 1000px;
    }

    .nav-sub-item {
      margin: 0;
    }

    .nav-sub-item .nav-link {
      padding-left: 3rem;
      font-size: 0.9rem;
    }

    .nav-sub-item .nav-link.active {
      background: #2c3e50;
      color: #3498db;
      border-left-color: #3498db;
    }

    /* Sub-secciones anidadas (Gestión de Eventos) */
    .nav-sub-section {
      margin: 0;
    }

    .nav-sub-section-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.5rem 3rem;
      color: #bdc3c7;
      background: transparent;
      border: none;
      width: 100%;
      text-align: left;
      cursor: pointer;
      font-size: 0.9rem;
      transition: all 0.2s;
    }

    .nav-sub-section-header:hover {
      background: rgba(255, 255, 255, 0.05);
      color: white;
    }

    .nav-sub-section-header i {
      width: 20px;
      text-align: center;
      color: inherit;
    }

    .nav-sub-section-content {
      list-style: none;
      margin: 0;
      padding: 0;
      background: rgba(0, 0, 0, 0.15);
      overflow: hidden;
      max-height: 0;
      transition: max-height 0.3s ease;
    }

    .nav-sub-section.expanded .nav-sub-section-content {
      max-height: 500px;
    }

    .nav-sub-sub-item {
      margin: 0;
    }

    .nav-sub-sub-item .nav-link {
      padding-left: 4.5rem;
      font-size: 0.85rem;
    }

    .nav-sub-sub-item .nav-link.active {
      background: #2c3e50;
      color: #3498db;
      border-left-color: #3498db;
    }

    /* Badge para items pendientes */
    .badge-coming-soon {
      margin-left: auto;
      font-size: 0.7rem;
      padding: 0.2rem 0.5rem;
      background: rgba(255, 193, 7, 0.2);
      color: #ffc107;
      border-radius: 4px;
      font-weight: 500;
    }

    .nav-link.disabled {
      opacity: 0.6;
      cursor: not-allowed;
      pointer-events: none;
    }

    .nav-link.disabled:hover {
      background: transparent;
      color: #bdc3c7;
      border-left-color: transparent;
    }

    /* Main Content */
    .main-content {
      flex: 1;
      overflow-y: auto;
      background: #ecf0f1;
      padding: 1.5rem;
      padding-bottom: calc(1.5rem + 60px); /* Espacio para el banner de conexión */
    }

    /* Responsive Design */
    @media (max-width: 768px) {
      .mobile-menu-toggle {
        display: flex !important;
        align-items: center;
        justify-content: center;
      }

      .mobile-menu-icon {
        font-size: 1.5rem;
      }

      .sidebar {
        width: 200px;
      }

      .sidebar-logo-circle {
        width: 72px;
        height: 72px;
        padding: 3px;
      }

      .sidebar-logo-img {
        height: 64px;
        max-width: 66px;
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

      .navbar-company {
        flex: 1;
        margin: 0 0.5rem;
        min-width: 0;
        max-width: none;
      }

      .company-label {
        display: none;
      }

      .company-selector-container {
        max-width: none;
        width: 100%;
      }

      .company-name {
        display: block;
        flex: 1;
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: none;
      }

      .company-selector-btn {
        padding: 0.75rem 1rem;
        font-size: 1rem;
        min-height: 44px;
      }

      .company-selector-btn i.fa-building {
        font-size: 1rem;
      }

      .company-selector-btn i.fa-chevron-down {
        font-size: 0.875rem;
      }

      .company-menu {
        left: 0;
        transform: none;
        min-width: 250px;
        max-width: calc(100vw - 2rem);
      }

      .company-menu-item,
      .user-menu-item {
        min-height: 44px;
        padding: 0.75rem 1rem;
        font-size: 1rem;
      }

      .user-menu-logout {
        min-height: 44px;
        padding: 0.75rem 1rem;
        font-size: 1rem;
      }
    }

    @media (max-width: 576px) {
      .navbar {
        min-height: 64px;
        height: 64px;
        padding: 0 0.5rem;
      }

      .mobile-overlay {
        display: block;
        position: fixed;
        top: 64px;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 998;
        transition: opacity 0.3s ease, visibility 0.3s ease;
      }

      .sidebar {
        position: fixed;
        left: -80vw;
        top: 64px;
        width: 80vw;
        height: calc(100vh - 64px);
        z-index: 999;
        transition: left 0.3s ease;
        box-shadow: 2px 0 8px rgba(0, 0, 0, 0.2);
      }

      .sidebar.open {
        left: 0;
      }

      .sidebar-logo-circle {
        width: 64px;
        height: 64px;
        padding: 3px;
      }

      .sidebar-logo-img {
        height: 56px;
        max-width: 58px;
      }

      .sidebar-close-mobile {
        display: block;
        min-width: 44px;
        min-height: 44px;
        padding: 0.5rem;
        font-size: 1.25rem;
      }

      .nav-link {
        padding: 1rem 1.25rem;
        font-size: 1.15rem;
      }

      .nav-link i {
        font-size: 1.15rem;
      }

      .nav-section-header {
        padding: 1rem 1.25rem;
        font-size: 1.15rem;
      }

      .nav-section-header i {
        font-size: 1.15rem;
      }

      .nav-sub-item .nav-link {
        padding-left: 3rem;
        padding-top: 0.875rem;
        padding-bottom: 0.875rem;
        font-size: 1.05rem;
      }

      .nav-sub-section-header {
        padding: 0.75rem 3rem;
        font-size: 1.05rem;
      }

      .nav-sub-sub-item .nav-link {
        padding-left: 4.5rem;
        padding-top: 0.75rem;
        padding-bottom: 0.75rem;
        font-size: 1.05rem;
      }

      .main-content {
        margin-left: 0;
      }

      .navbar-company {
        flex: 1;
        margin: 0 0.25rem;
        min-width: 0;
        max-width: none;
        display: flex;
      }

      .company-selector-container {
        max-width: none;
        width: 100%;
        flex: 1;
        min-width: 0;
      }

      .company-selector-btn {
        width: 100%;
        max-width: none;
        padding: 0.65rem 0.75rem;
        font-size: 1rem;
        gap: 0.5rem;
        min-height: 44px;
      }

      .company-selector-btn i.fa-building {
        font-size: 1rem;
      }

      .company-selector-btn i.fa-chevron-down {
        font-size: 0.875rem;
      }

      .company-name {
        flex: 1;
        min-width: 0;
        max-width: none;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .avatar-btn {
        padding: 0.5rem;
        min-height: 44px;
        min-width: 44px;
      }

      .avatar-circle {
        width: 40px;
        height: 40px;
        font-size: 1rem;
      }
    }
  `]
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  currentUser$: Observable<User | null>;
  userRole$: Observable<string | null>;
  isAdmin$: Observable<boolean>;
  empresas$: Observable<Empresa[]>;
  selectedEmpresa$: Observable<Empresa | null>;
  empresasLoading$: Observable<boolean>;
  
  showUserMenu = false;
  showCompanyMenu = false;
  isMobileMenuOpen = false;
  currentUser: User | null = null;
  userRole: string | null = null;
  isAdminUser = false;
  empresas: Empresa[] = [];
  selectedEmpresa: Empresa | null = null;
  empresasLoading = false;
  
  appVersion = environment.version;

  // Estado de secciones expandidas
  expandedSections: { [key: string]: boolean } = {
    gestionEmpresa: false,
    facturacion: false,
    documentos: false,
    eventos: false,
    reportes: false,
    administracion: false
  };
  
  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private router: Router,
    private authService: AuthService,
    private permissionsService: PermissionsService
  ) {
    this.currentUser$ = this.store.select(selectCurrentUser);
    this.userRole$ = this.store.select(selectUserRole);
    this.isAdmin$ = this.store.select(selectHasRole('ADMIN'));
    this.empresas$ = this.store.select(selectAllEmpresas);
    this.selectedEmpresa$ = this.store.select(selectSelectedEmpresa).pipe(
      map(empresa => empresa ?? null)
    );
    this.empresasLoading$ = this.store.select(selectEmpresasLoading);
  }

  ngOnInit(): void {
    // Cargar empresas del usuario al inicializar
    this.store.dispatch(loadMisEmpresas());

    // Suscribirse al usuario actual desde el store
    this.currentUser$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(user => {
      this.currentUser = user;
      // Si hay usuario, cargar empresas
      if (user) {
        this.store.dispatch(loadMisEmpresas());
      }
    });

    // Suscribirse al rol del usuario desde el store
    this.userRole$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(role => {
      this.userRole = role;
    });

    // Suscribirse al estado de admin
    this.isAdmin$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(isAdmin => {
      this.isAdminUser = isAdmin;
    });

    // Suscribirse a las empresas
    this.empresas$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(empresas => {
      this.empresas = empresas;
      
      // Intentar restaurar empresa seleccionada desde localStorage
      const savedEmpresaId = localStorage.getItem('selectedEmpresaId');
      if (savedEmpresaId && !this.selectedEmpresa) {
        const id = parseInt(savedEmpresaId, 10);
        const empresa = empresas.find(e => e.id === id);
        if (empresa) {
          this.selectEmpresaById(id);
          return; // Ya seleccionamos, salimos
        }
      }

      // Auto-seleccionar la primera empresa si solo hay una y no hay empresa seleccionada
      if (empresas.length === 1 && !this.selectedEmpresa) {
        this.selectEmpresaById(empresas[0].id);
      }
    });

    // Suscribirse a la empresa seleccionada
    this.selectedEmpresa$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(empresa => {
      this.selectedEmpresa = empresa;
    });

    // Suscribirse al estado de carga
    this.empresasLoading$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(loading => {
      this.empresasLoading = loading;
    });

    // Cerrar los menús al hacer clic fuera
    document.addEventListener('click', this.handleDocumentClick.bind(this));
    
    // Expandir sección según ruta actual
    this.expandSectionByRoute();
    
    // Suscribirse a cambios de ruta para expandir sección correspondiente
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.expandSectionByRoute();
      // Cerrar menú móvil al navegar
      this.closeMobileMenu();
    });
  }
  
  expandSectionByRoute(): void {
    const url = this.router.url;
    
    // Resetear todas las secciones
    Object.keys(this.expandedSections).forEach(key => {
      this.expandedSections[key] = false;
    });
    
    // Expandir según la ruta actual
    if (url.startsWith('/empresas') || url.startsWith('/timbrados') || url.includes('/productos') || 
        url.startsWith('/clientes') || url.includes('/vehiculos') || url.includes('/choferes') || 
        url.startsWith('/usuarios')) {
      this.expandedSections['gestionEmpresa'] = true;
    } else if (url.startsWith('/facturacion') || url.startsWith('/notas')) {
      this.expandedSections['facturacion'] = true;
    } else if (url.startsWith('/documentos')) {
      this.expandedSections['documentos'] = true;
      if (url.includes('/cancelacion') || url.includes('/nominacion') || url.includes('/inutilizacion')) {
        this.expandedSections['eventos'] = true;
      }
    } else if (url.startsWith('/reportes')) {
      this.expandedSections['reportes'] = true;
    } else if (url.startsWith('/usuarios')) {
      this.expandedSections['administracion'] = true;
    }
  }
  
  toggleSection(section: string): void {
    this.expandedSections[section] = !this.expandedSections[section];
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    document.removeEventListener('click', this.handleDocumentClick.bind(this));
    // Limpiar overflow del body
    document.body.style.overflow = '';
  }

  get isAdmin(): boolean {
    return this.isAdminUser;
  }

  // Métodos de acceso basados en roles
  get canAccessEmpresas(): boolean {
    return this.hasAnyRole(['ADMIN', 'EMPRESA_ADMIN']);
  }

  get canAccessClientes(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canManageClientes(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  get canAccessFacturacion(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canManageFacturacion(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  get canAccessDocumentos(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canAccessReportes(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canAccessAuditoria(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canAccessUsuarios(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canManageUsuarios(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN']);
  }

  get canAccessTimbrados(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canManageTimbrados(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN']);
  }

  get canAccessProductos(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canManageProductos(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  get canAccessVehiculos(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canManageVehiculos(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  get canAccessChoferes(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']);
  }

  get canManageChoferes(): boolean {
    return this.permissionsService.hasAnyRoleSync(['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']);
  }

  private hasAnyRole(roles: string[]): boolean {
    if (!this.currentUser?.roles) return false;

    return this.currentUser.roles.some(userRole => {
      let roleName = typeof userRole === 'string' ? userRole : userRole.nombre;
      // Normalizar quitando el prefijo ROLE_ si existe, para coincidir con la jerarquía del frontend
      if (roleName && roleName.startsWith('ROLE_')) {
        roleName = roleName.substring(5);
      }
      return roles.includes(roleName);
    });
  }

  private handleDocumentClick(): void {
    if (this.showUserMenu) {
      this.showUserMenu = false;
    }
    if (this.showCompanyMenu) {
      this.showCompanyMenu = false;
    }
    // El menú móvil se cierra con el overlay, no aquí para evitar conflictos
  }

  toggleUserMenu(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.showUserMenu = !this.showUserMenu;
    this.showCompanyMenu = false;
  }

  toggleCompanyMenu(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.showCompanyMenu = !this.showCompanyMenu;
    this.showUserMenu = false;
  }

  selectEmpresaById(empresaId: number): void {
    // Verificar si la empresa está cambiando
    const empresaAnterior = this.selectedEmpresa?.id;
    const estaCambiandoEmpresa = empresaAnterior && empresaAnterior !== empresaId;
    
    // Cerrar todos los menús y componentes abiertos
    this.showCompanyMenu = false;
    this.showUserMenu = false;
    
    // Colapsar todas las secciones expandidas
    Object.keys(this.expandedSections).forEach(key => {
      this.expandedSections[key] = false;
    });
    
    // Guardar la nueva empresa seleccionada
    localStorage.setItem('selectedEmpresaId', empresaId.toString());
    this.store.dispatch(selectEmpresa({ empresaId }));
    
    // Si se está cambiando de empresa, redirigir al dashboard para recargar todo
    if (estaCambiandoEmpresa) {
      this.router.navigate(['/dashboard'], { 
        queryParams: { empresaId: empresaId },
        replaceUrl: true 
      }).then(() => {
        // Forzar recarga completa de la página para limpiar todo el estado
        window.location.reload();
      });
    }
  }

  navigateToNewEmpresa(): void {
    this.showCompanyMenu = false;
    this.router.navigate(['/empresas/new']);
  }

  handleNavClick(event: Event, isDisabled: boolean): void {
    if (isDisabled) {
      event.preventDefault();
      event.stopPropagation();
      // Opcional: mostrar mensaje al usuario
      return;
    }
  }

  navigateToEmpresas(): void {
    this.showUserMenu = false;
    this.router.navigate(['/empresas']);
  }

  navigateToProfile(): void {
    this.showUserMenu = false;
    this.router.navigate(['/perfil']);
  }

  navigateToSettings(): void {
    this.showUserMenu = false;
    // TODO: Implementar navegación a configuración
    console.log('Navegando a configuración...');
  }

  logout(): void {
    this.showUserMenu = false;
    localStorage.removeItem('selectedEmpresaId'); // Limpiar empresa seleccionada
    this.authService.logout().subscribe(() => {
      // Reiniciar la aplicación para limpiar completamente el estado
      window.location.href = '/login';
    });
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
    // Prevenir scroll del body cuando el menú está abierto
    if (this.isMobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
    document.body.style.overflow = '';
  }

  onNavLinkClick(): void {
    // Cerrar el menú móvil cuando se hace clic en un enlace
    this.closeMobileMenu();
  }

}
