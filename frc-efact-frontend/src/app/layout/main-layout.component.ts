import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, RouterModule, NavigationEnd } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil, map, filter } from 'rxjs/operators';
import { MatExpansionModule } from '@angular/material/expansion';
import { selectUserRole, selectCurrentUser, selectHasRole, selectUserRoles } from '../core/state/auth/auth.selectors';
import { selectAllEmpresas, selectSelectedEmpresa, selectEmpresasLoading } from '../core/state/empresas/empresas.selectors';
import { loadMisEmpresas, selectEmpresa } from '../core/state/empresas/empresas.actions';
import { User } from '../models/user.model';
import { Empresa } from '../models/empresa.model';
import { AuthService } from '../services/auth.service';
import { ConnectionStatusBannerComponent } from '../components/connection-status-banner/connection-status-banner.component';
import { ThemeService } from '../core/services/theme.service';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule, MatExpansionModule, ConnectionStatusBannerComponent, MatButtonModule, MatIconModule],
  template: `
    <div class="main-layout">
      <!-- Top Navigation Bar -->
      <nav class="navbar">
        <div class="navbar-brand">
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
                <li class="company-menu-item" (click)="navigateToNewEmpresa()">
                  <i class="fas fa-plus-circle"></i>
                  <span>Crear nueva empresa</span>
                </li>
                <li class="company-menu-divider" *ngIf="empresas.length > 0"></li>
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
                <li class="user-menu-item" (click)="navigateToEmpresas()" title="Gestión de empresas" *ngIf="canAccessEmpresas$ | async">
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
                <li class="user-menu-item" (click)="toggleTheme()" [title]="(isDarkMode() ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro')">
                  <i class="fas" [ngClass]="isDarkMode() ? 'fa-sun' : 'fa-moon'"></i>
                  <span>{{ isDarkMode() ? 'Modo Claro' : 'Modo Oscuro' }}</span>
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
          <!-- Logo en la parte superior de la sidebar -->
          <div class="sidebar-logo">
            <img src="assets/logo.svg" alt="FRC eFact" class="sidebar-logo-img" />
          </div>
          <nav class="sidebar-nav">
            <ul class="nav-menu">
              <!-- Items Simples -->
              <li class="nav-item">
                <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-tachometer-alt"></i>
                  <span>Dashboard</span>
                </a>
              </li>

              <!-- Sección: Gestión de Empresa -->
              <li class="nav-item" *ngIf="selectedEmpresa">
                <mat-expansion-panel
                  class="nav-expansion-panel"
                  [expanded]="expandedSections['gestionEmpresa']"
                  (opened)="toggleSection('gestionEmpresa')"
                  (closed)="toggleSection('gestionEmpresa')"
                  [disabled]="!selectedEmpresa">
                  <mat-expansion-panel-header class="nav-expansion-header">
                    <mat-panel-title>
                      <i class="fas fa-building"></i>
                      <span>Gestión de Empresa</span>
                    </mat-panel-title>
                  </mat-expansion-panel-header>
                  <ul class="nav-section-content">
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? ['/timbrados'] : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-stamp"></i>
                        <span>Timbrados</span>
                      </a>
                    </li>
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? ['/empresas', selectedEmpresa.id, 'productos'] : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [class.disabled]="!selectedEmpresa">
                        <i class="fas fa-box"></i>
                        <span>Productos</span>
                      </a>
                    </li>
                  </ul>
                </mat-expansion-panel>
              </li>

              <!-- Item Simple: Clientes -->
              <li class="nav-item" *ngIf="canAccessClientes$ | async">
                <a
                  [routerLink]="selectedEmpresa ? '/clientes' : null"
                  routerLinkActive="active"
                  class="nav-link"
                  [class.disabled]="!selectedEmpresa"
                  [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                  (click)="handleNavClick($event, !selectedEmpresa)">
                  <i class="fas fa-users"></i>
                  <span>Clientes</span>
                </a>
              </li>

              <!-- Sección: Facturación -->
              <li class="nav-item" *ngIf="canAccessFacturacion$ | async">
                <mat-expansion-panel
                  class="nav-expansion-panel"
                  [expanded]="expandedSections['facturacion']"
                  (opened)="toggleSection('facturacion')"
                  (closed)="toggleSection('facturacion')"
                  [disabled]="!selectedEmpresa">
                  <mat-expansion-panel-header class="nav-expansion-header">
                    <mat-panel-title>
                      <i class="fas fa-file-invoice"></i>
                      <span>Facturación</span>
                    </mat-panel-title>
                  </mat-expansion-panel-header>
                  <ul class="nav-section-content">
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/facturacion' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-list"></i>
                        <span>Lista de Facturas</span>
                      </a>
                    </li>
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/notas-credito' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-file-invoice-dollar"></i>
                        <span>Notas de Crédito</span>
                      </a>
                    </li>
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/notas-debito' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-file-invoice-dollar"></i>
                        <span>Notas de Débito</span>
                      </a>
                    </li>
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/notas-remision' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-file-alt"></i>
                        <span>Notas de Remisión</span>
                      </a>
                    </li>
                  </ul>
                </mat-expansion-panel>
              </li>

              <!-- Sección: Documentos Electrónicos -->
              <li class="nav-item" *ngIf="canAccessDocumentos$ | async">
                <mat-expansion-panel
                  class="nav-expansion-panel"
                  [expanded]="expandedSections['documentos']"
                  (opened)="toggleSection('documentos')"
                  (closed)="toggleSection('documentos')"
                  [disabled]="!selectedEmpresa">
                  <mat-expansion-panel-header class="nav-expansion-header">
                    <mat-panel-title>
                      <i class="fas fa-file-alt"></i>
                      <span>Documentos Electrónicos</span>
                    </mat-panel-title>
                  </mat-expansion-panel-header>
                  <ul class="nav-section-content">
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/documentos/lista' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-list"></i>
                        <span>Lista de Documentos</span>
                      </a>
                    </li>
                    <li class="nav-sub-item">
                      <mat-expansion-panel
                        class="nav-sub-expansion-panel"
                        [expanded]="expandedSections['eventos']"
                        (opened)="toggleSection('eventos')"
                        (closed)="toggleSection('eventos')">
                        <mat-expansion-panel-header class="nav-sub-expansion-header">
                          <mat-panel-title>
                            <i class="fas fa-cog"></i>
                            <span>Gestión de Eventos</span>
                          </mat-panel-title>
                        </mat-expansion-panel-header>
                        <ul class="nav-sub-section-content">
                          <li class="nav-sub-sub-item">
                            <a [routerLink]="selectedEmpresa ? '/documentos/cancelacion' : null"
                               routerLinkActive="active"
                               class="nav-link"
                               [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                              <i class="fas fa-ban"></i>
                              <span>Cancelación</span>
                            </a>
                          </li>
                          <li class="nav-sub-sub-item">
                            <a [routerLink]="selectedEmpresa ? '/documentos/nominacion' : null"
                               routerLinkActive="active"
                               class="nav-link"
                               [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                              <i class="fas fa-user-tag"></i>
                              <span>Nominación</span>
                            </a>
                          </li>
                          <li class="nav-sub-sub-item">
                            <a [routerLink]="selectedEmpresa ? '/documentos/inutilizacion' : null"
                               routerLinkActive="active"
                               class="nav-link"
                               [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                              <i class="fas fa-times-circle"></i>
                              <span>Inutilización</span>
                            </a>
                          </li>
                        </ul>
                      </mat-expansion-panel>
                    </li>
                  </ul>
                </mat-expansion-panel>
              </li>

              <!-- Sección: Reportes -->
              <li class="nav-item" *ngIf="canAccessReportes$ | async">
                <mat-expansion-panel
                  class="nav-expansion-panel"
                  [expanded]="expandedSections['reportes']"
                  (opened)="toggleSection('reportes')"
                  (closed)="toggleSection('reportes')"
                  [disabled]="!selectedEmpresa">
                  <mat-expansion-panel-header class="nav-expansion-header">
                    <mat-panel-title>
                      <i class="fas fa-chart-bar"></i>
                      <span>Reportes</span>
                    </mat-panel-title>
                  </mat-expansion-panel-header>
                  <ul class="nav-section-content">
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/reportes/facturas' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-file-invoice"></i>
                        <span>Reporte de Facturas</span>
                      </a>
                    </li>
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/reportes/clientes' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-users"></i>
                        <span>Reporte de Clientes</span>
                      </a>
                    </li>
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/reportes/productos' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-box"></i>
                        <span>Reporte de Productos</span>
                      </a>
                    </li>
                    <li class="nav-sub-item">
                      <a [routerLink]="selectedEmpresa ? '/reportes/usuarios' : null"
                         routerLinkActive="active"
                         class="nav-link"
                         [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null">
                        <i class="fas fa-user-cog"></i>
                        <span>Reporte de Usuarios</span>
                      </a>
                    </li>
                  </ul>
                </mat-expansion-panel>
              </li>

              <!-- Item Simple: Auditoría -->
              <li class="nav-item" *ngIf="canAccessAuditoria$ | async">
                <a
                  [routerLink]="selectedEmpresa ? '/auditoria' : null"
                  routerLinkActive="active"
                  class="nav-link"
                  [class.disabled]="!selectedEmpresa"
                  [queryParams]="selectedEmpresa ? { empresaId: selectedEmpresa.id } : null"
                  (click)="handleNavClick($event, !selectedEmpresa)">
                  <i class="fas fa-history"></i>
                  <span>Auditoría</span>
                </a>
              </li>

              <!-- Sección: Administración -->
              <li class="nav-item" *ngIf="isAdmin$ | async">
                <mat-expansion-panel
                  class="nav-expansion-panel"
                  [expanded]="expandedSections['administracion']"
                  (opened)="toggleSection('administracion')"
                  (closed)="toggleSection('administracion')">
                  <mat-expansion-panel-header class="nav-expansion-header">
                    <mat-panel-title>
                      <i class="fas fa-cog"></i>
                      <span>Administración</span>
                    </mat-panel-title>
                  </mat-expansion-panel-header>
                  <ul class="nav-section-content">
                    <li class="nav-sub-item">
                      <a routerLink="/usuarios" routerLinkActive="active" class="nav-link">
                        <i class="fas fa-user-cog"></i>
                        <span>Usuarios</span>
                      </a>
                    </li>
                  </ul>
                </mat-expansion-panel>
              </li>

              <!-- Item Simple: Perfil -->
              <li class="nav-item">
                <a routerLink="/perfil" routerLinkActive="active" class="nav-link">
                  <i class="fas fa-user-circle"></i>
                  <span>Perfil</span>
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

    .navbar-company {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.75rem;
      margin: 0 2rem;
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

    .sidebar-logo {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 1.5rem 1rem;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      margin-bottom: 0.5rem;
    }

    .sidebar-logo-img {
      height: 80px;
      width: auto;
      max-width: 200px;
      object-fit: contain;
    }

    .sidebar-nav {
      padding: 1rem 0;
      flex: 1;
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

    .nav-link i {
      width: 20px;
      text-align: center;
      color: inherit;
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

    .nav-link.disabled {
      opacity: 0.5;
      cursor: not-allowed;
      pointer-events: none;
    }

    /* Material Expansion Panel */
    .nav-expansion-panel {
      background: transparent !important;
      box-shadow: none !important;
      margin: 0 !important;
      border-radius: 0 !important;
      display: block;
    }

    .nav-expansion-panel ::ng-deep .mat-expansion-panel-header {
      padding: 0.75rem 1.5rem !important;
      height: auto !important;
      color: #bdc3c7 !important;
      border-left: 3px solid transparent;
    }

    .nav-expansion-panel ::ng-deep .mat-expansion-panel-header:hover {
      background: #2c3e50 !important;
      color: white !important;
      border-left-color: #3498db;
    }

    .nav-expansion-panel ::ng-deep .mat-expansion-panel-header[aria-disabled="true"] {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .nav-expansion-panel ::ng-deep .mat-expansion-panel-header.mat-expanded {
      background: #2c3e50 !important;
      color: #3498db !important;
      border-left-color: #3498db;
    }

    .nav-expansion-panel ::ng-deep .mat-expansion-panel-body {
      padding: 0 !important;
    }

    .nav-expansion-header ::ng-deep .mat-expansion-panel-header-title {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin: 0;
    }

    .nav-expansion-header ::ng-deep .mat-expansion-panel-header-title i {
      width: 20px;
      text-align: center;
      color: inherit;
    }

    .nav-expansion-panel ::ng-deep .mat-expansion-indicator {
      color: white !important;
    }

    .nav-section-content {
      list-style: none;
      margin: 0;
      padding: 0;
      background: rgba(0, 0, 0, 0.1);
    }

    .nav-sub-item {
      margin: 0;
    }

    .nav-sub-item .nav-link {
      padding-left: 3rem;
      font-size: 0.9rem;
    }

    .nav-sub-expansion-panel {
      background: transparent !important;
      box-shadow: none !important;
      margin: 0 !important;
      border-radius: 0 !important;
      display: block;
    }

    .nav-sub-expansion-panel ::ng-deep .mat-expansion-panel-header {
      padding: 0.5rem 3rem !important;
      height: auto !important;
      color: #bdc3c7 !important;
      font-size: 0.9rem;
    }

    .nav-sub-expansion-panel ::ng-deep .mat-expansion-panel-header:hover {
      background: rgba(255, 255, 255, 0.05) !important;
      color: white !important;
    }

    .nav-sub-expansion-panel ::ng-deep .mat-expansion-panel-header.mat-expanded {
      background: rgba(255, 255, 255, 0.05) !important;
      color: #3498db !important;
    }

    .nav-sub-expansion-panel ::ng-deep .mat-expansion-panel-body {
      padding: 0 !important;
    }

    .nav-sub-expansion-header ::ng-deep .mat-expansion-panel-header-title {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin: 0;
    }

    .nav-sub-expansion-header ::ng-deep .mat-expansion-panel-header-title i {
      width: 20px;
      text-align: center;
      color: inherit;
    }

    .nav-sub-expansion-panel ::ng-deep .mat-expansion-indicator {
      color: white !important;
    }

    .nav-sub-section-content {
      list-style: none;
      margin: 0;
      padding: 0;
      background: rgba(0, 0, 0, 0.15);
    }

    .nav-sub-sub-item {
      margin: 0;
    }

    .nav-sub-sub-item .nav-link {
      padding-left: 4.5rem;
      font-size: 0.85rem;
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
      .sidebar {
        width: 200px;
      }

      .sidebar-logo-img {
        height: 60px;
        max-width: 160px;
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
        margin: 0 1rem;
      }

      .company-label {
        display: none;
      }

      .company-name {
        display: none;
      }

      .company-menu {
        left: 0;
        transform: none;
        min-width: 250px;
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

      .sidebar-logo-img {
        height: 50px;
        max-width: 140px;
      }

      .main-content {
        margin-left: 0;
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

  // Permission Observables
  canAccessEmpresas$: Observable<boolean>;
  canAccessClientes$: Observable<boolean>;
  canAccessFacturacion$: Observable<boolean>;
  canAccessDocumentos$: Observable<boolean>;
  canAccessReportes$: Observable<boolean>;
  canAccessAuditoria$: Observable<boolean>;

  showUserMenu = false;
  showCompanyMenu = false;
  currentUser: User | null = null;
  userRole: string | null = null;
  isAdmin = false;
  empresas: Empresa[] = [];
  selectedEmpresa: Empresa | null = null;
  empresasLoading = false;

  // Permission flags (Removed manual flags, using async pipe)
  // canAccessEmpresas = false;
  // canAccessClientes = false;
  // canAccessFacturacion = false;
  // canAccessDocumentos = false;
  // canAccessReportes = false;
  // canAccessAuditoria = false;

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

  isDarkMode = this.themeService.darkMode;
  private readonly SELECTED_EMPRESA_KEY = 'selected_empresa_id';

  constructor(
    private store: Store,
    private router: Router,
    private authService: AuthService,
    private themeService: ThemeService
  ) {
    this.currentUser$ = this.store.select(selectCurrentUser);
    this.userRole$ = this.store.select(selectUserRole);
    this.isAdmin$ = this.store.select(selectHasRole(['ADMIN', 'SUPERADMIN']));
    this.empresas$ = this.store.select(selectAllEmpresas);
    this.selectedEmpresa$ = this.store.select(selectSelectedEmpresa).pipe(
      map(e => e ?? null)
    );
    this.empresasLoading$ = this.store.select(selectEmpresasLoading);

    // Verificar permisos
    this.canAccessEmpresas$ = this.store.select(selectHasRole(['ADMIN', 'SUPERADMIN', 'EMPRESA_ADMIN']));
    this.canAccessClientes$ = this.store.select(selectHasRole(['ADMIN', 'SUPERADMIN', 'EMPRESA_ADMIN', 'OPERADOR']));
    this.canAccessFacturacion$ = this.store.select(selectHasRole(['ADMIN', 'SUPERADMIN', 'EMPRESA_ADMIN', 'OPERADOR']));
    this.canAccessDocumentos$ = this.store.select(selectHasRole(['ADMIN', 'SUPERADMIN', 'EMPRESA_ADMIN', 'OPERADOR']));
    this.canAccessReportes$ = this.store.select(selectHasRole(['ADMIN', 'SUPERADMIN', 'EMPRESA_ADMIN']));
    this.canAccessAuditoria$ = this.store.select(selectHasRole(['ADMIN', 'SUPERADMIN', 'EMPRESA_ADMIN']));
  }

  ngOnInit(): void {
    // Suscribirse a los selectores
    this.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      this.currentUser = user;
      // Si hay usuario, cargar empresas
      if (user) {
        this.store.dispatch(loadMisEmpresas());
      }
    });

    this.userRole$.pipe(takeUntil(this.destroy$)).subscribe(role => {
      this.userRole = role;
      this.isAdmin = role === 'ADMIN' || role === 'SUPERADMIN';
    });

    this.empresas$.pipe(takeUntil(this.destroy$)).subscribe(empresas => {
      this.empresas = empresas;
      
      // Intentar recuperar la empresa seleccionada del almacenamiento local
      const savedEmpresaId = localStorage.getItem(this.SELECTED_EMPRESA_KEY);
      
      if (!this.selectedEmpresa && empresas.length > 0) {
        if (savedEmpresaId) {
          const id = parseInt(savedEmpresaId, 10);
          const empresaExists = empresas.find(e => e.id === id);
          
          if (empresaExists) {
            this.selectEmpresaById(id);
          } else {
            // Si la empresa guardada ya no existe o el usuario no tiene acceso, seleccionar la primera
            this.selectEmpresaById(empresas[0].id);
          }
        } else if (empresas.length === 1) {
          // Auto-seleccionar la primera empresa si solo hay una
          this.selectEmpresaById(empresas[0].id);
        }
      }
    });

    this.selectedEmpresa$.pipe(takeUntil(this.destroy$)).subscribe(empresa => {
      this.selectedEmpresa = empresa;
    });

    this.empresasLoading$.pipe(takeUntil(this.destroy$)).subscribe(loading => {
      this.empresasLoading = loading;
    });

    // Eliminar suscripciones manuales a permisos, ahora se usa async pipe en el template
    // this.canAccessEmpresas$.pipe...
    // this.canAccessClientes$.pipe...
    // etc.

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
    });
  }

  expandSectionByRoute(): void {
    const url = this.router.url;

    // Resetear todas las secciones
    Object.keys(this.expandedSections).forEach(key => {
      this.expandedSections[key] = false;
    });

    // Expandir según la ruta actual
    if (url.startsWith('/empresas') || url.startsWith('/timbrados') || url.includes('/productos')) {
      this.expandedSections['gestionEmpresa'] = true;
    } else if (url.startsWith('/facturacion') || url.startsWith('/notas-credito') || url.startsWith('/notas-debito') || url.startsWith('/notas-remision')) {
      this.expandedSections['facturacion'] = true;
    } else if (url.startsWith('/documentos')) {
      this.expandedSections['documentos'] = true;
      // Expandir sub-sección de eventos si la ruta corresponde
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
    // Actualizar el estado cuando el panel se abre o cierra
    // El mat-expansion-panel ya cambió su estado, solo sincronizamos
    this.expandedSections[section] = !this.expandedSections[section];

    // Si es la sub-sección de eventos, manejar de forma especial
    if (section === 'eventos') {
      // Si se expande eventos, también expandir documentos
      if (this.expandedSections['eventos']) {
        this.expandedSections['documentos'] = true;
      }
    } else {
      // Si se colapsa documentos, también colapsar eventos
      if (section === 'documentos' && !this.expandedSections['documentos']) {
        this.expandedSections['eventos'] = false;
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    document.removeEventListener('click', this.handleDocumentClick.bind(this));
  }

  toggleTheme() {
    this.themeService.toggleTheme();
    this.showUserMenu = false; // Cerrar el menú después de cambiar el tema
  }


  // Métodos de acceso basados en roles (Removed getters, using properties updated via subscription)

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
    this.store.dispatch(selectEmpresa({ empresaId }));
    localStorage.setItem(this.SELECTED_EMPRESA_KEY, empresaId.toString());
    this.showCompanyMenu = false;
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
    // Limpiar empresa seleccionada al cerrar sesión
    localStorage.removeItem(this.SELECTED_EMPRESA_KEY);
    this.authService.logout().subscribe(() => {
      // Reiniciar la aplicación para limpiar completamente el estado
      window.location.href = '/login';
    });
  }

}
