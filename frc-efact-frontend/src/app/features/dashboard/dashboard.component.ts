import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil, map } from 'rxjs/operators';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { selectSelectedEmpresa } from '../../core/state/empresas/empresas.selectors';
import { Empresa } from '../../models/empresa.model';
import { DashboardEmpresa } from '../../models/dashboard.model';
import { FacturaLegal } from '../../models/factura.model';
import { DashboardApiService } from '../../core/api/dashboard-api.service';
import { FacturaApiService } from '../../core/api/factura-api.service';
import { DateFilterComponent, DateRange } from './date-filter.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, DateFilterComponent, MatPaginatorModule],
  template: `
    <div class="dashboard-container">
      <!-- Mensaje si no hay empresa seleccionada -->
      <div class="no-empresa-message" *ngIf="!(selectedEmpresa$ | async)">
        <div class="message-content">
          <i class="fas fa-building"></i>
          <h2>Selecciona una empresa</h2>
          <p>Por favor, selecciona una empresa desde el menú superior para ver el dashboard.</p>
        </div>
      </div>

      <!-- Dashboard cuando hay empresa seleccionada -->
      <div *ngIf="selectedEmpresa$ | async as empresa">
        <!-- Header -->
        <div class="dashboard-header">
          <div class="header-content">
            <h1>Dashboard - {{ empresa.razonSocial }}</h1>
            <app-date-filter
              (dateRangeChange)="onDateRangeChange($event)"
              [selectedOption]="selectedDateFilter">
            </app-date-filter>
          </div>
        </div>

        <div class="dashboard-content">
          <!-- Acciones Rápidas -->
          <div class="quick-actions-section">
            <h2>Acciones Rápidas</h2>
            <div class="actions-grid">
              <button class="action-card" (click)="navigateToFacturacion('')">
                <i class="fas fa-file-invoice"></i>
                <span>Factura Electrónica</span>
              </button>
              <button class="action-card" (click)="navigateToNotasRemision()">
                <i class="fas fa-file-alt"></i>
                <span>Nota de Remisión</span>
              </button>
              <button class="action-card" (click)="navigateTo('/clientes/new')">
                <i class="fas fa-user-plus"></i>
                <span>Nuevo Cliente</span>
              </button>
              <button class="action-card" (click)="navigateTo('/productos/new')">
                <i class="fas fa-box"></i>
                <span>Nuevo Producto</span>
              </button>
              <button class="action-card" (click)="navigateTo('/empresas/' + empresa.id + '/timbrados')">
                <i class="fas fa-stamp"></i>
                <span>Gestionar Timbrados</span>
              </button>
            </div>
          </div>

          <!-- Estadísticas -->
          <div class="stats-section">
            <div class="stats-grid">
              <!-- Facturas Aprobadas -->
              <div class="stat-card approved" *ngIf="dashboardData">
                <div class="stat-header">
                  <i class="fas fa-check-circle"></i>
                  <h3>Facturas Aprobadas</h3>
                </div>
                <div class="stat-body">
                  <div class="stat-item">
                    <span class="stat-label">Cantidad:</span>
                    <span class="stat-value">{{ dashboardData.facturasAprobadas.cantidad || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">Total Gs:</span>
                    <span class="stat-value">{{ formatCurrency(dashboardData.facturasAprobadas.totalGs || 0) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">IVA 10%:</span>
                    <span class="stat-value">{{ formatCurrency(dashboardData.facturasAprobadas.totalIva10 || 0) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">IVA 5%:</span>
                    <span class="stat-value">{{ formatCurrency(dashboardData.facturasAprobadas.totalIva5 || 0) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">Exentas:</span>
                    <span class="stat-value">{{ formatCurrency(dashboardData.facturasAprobadas.totalExentas || 0) }}</span>
                  </div>
                </div>
              </div>

              <!-- Facturas Canceladas -->
              <div class="stat-card cancelled" *ngIf="dashboardData">
                <div class="stat-header">
                  <i class="fas fa-times-circle"></i>
                  <h3>Facturas Canceladas</h3>
                </div>
                <div class="stat-body">
                  <div class="stat-item">
                    <span class="stat-label">Cantidad:</span>
                    <span class="stat-value">{{ dashboardData.facturasCanceladas.cantidad || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">Total Gs:</span>
                    <span class="stat-value">{{ formatCurrency(dashboardData.facturasCanceladas.totalGs || 0) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Últimas Facturas -->
          <div class="lists-section">
            <div class="list-container full-width">
              <div class="list-header">
              <h2>Últimas Facturas</h2>
                <button class="btn-view-all" (click)="navigateToFacturasList()">
                  <i class="fas fa-list"></i>
                  Ver todas las facturas
                </button>
              </div>
              <div class="table-container" *ngIf="!loadingFacturas">
                <table class="facturas-table">
                  <thead>
                    <tr>
                      <th>Número</th>
                      <th>Fecha</th>
                      <th>Cliente</th>
                      <th>Total</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let factura of ultimasFacturas">
                      <td>{{ factura.numeroFactura }}</td>
                      <td>{{ formatDate(factura.fecha) }}</td>
                      <td>{{ factura.nombre }}</td>
                      <td>{{ formatCurrency(factura.totalFinal) }}</td>
                      <td>
                        <span class="badge" [class]="'badge-' + getEstadoClass(factura.estadoDocumentoElectronico)">
                          {{ factura.estadoDocumentoElectronico || 'Sin DE' }}
                        </span>
                      </td>
                      <td>
                        <button class="btn-view" (click)="viewFactura(factura.id!)">
                          <i class="fas fa-eye"></i>
                        </button>
                      </td>
                    </tr>
                    <tr *ngIf="ultimasFacturas.length === 0 && !loadingFacturas">
                      <td colspan="6" class="no-data">No hay facturas disponibles</td>
                    </tr>
                  </tbody>
                </table>
                <!-- Paginación -->
                <mat-paginator
                  *ngIf="facturasTotalElements > 0"
                  [length]="facturasTotalElements"
                  [pageSize]="facturasPageSize"
                  [pageSizeOptions]="[10, 20, 50, 100]"
                  [pageIndex]="facturasCurrentPage"
                  (page)="onFacturasPageChange($event)"
                  showFirstLastButtons>
                </mat-paginator>
              </div>
              <div class="loading" *ngIf="loadingFacturas">
                <i class="fas fa-spinner fa-spin"></i> Cargando facturas...
              </div>
            </div>
          </div>

          <!-- Top Clientes y Top Productos -->
          <div class="lists-section">
            <div class="list-container">
              <h2>Top Clientes</h2>
              <div class="clientes-list" *ngIf="dashboardData && !isLoading">
                <div class="cliente-item" *ngFor="let cliente of dashboardData.top10Clientes; let i = index">
                  <div class="cliente-rank">{{ i + 1 }}</div>
                  <div class="cliente-info">
                    <div class="cliente-name">{{ cliente.nombre }}</div>
                    <div class="cliente-ruc" *ngIf="cliente.ruc">RUC: {{ cliente.ruc }}</div>
                  </div>
                  <div class="cliente-stats">
                    <div class="cliente-facturas">{{ cliente.cantidadFacturas }} facturas</div>
                    <div class="cliente-total">{{ formatCurrency(cliente.montoTotal) }}</div>
                  </div>
                </div>
                <div class="no-data" *ngIf="dashboardData.top10Clientes.length === 0">
                  No hay clientes disponibles
                </div>
              </div>
              <div class="loading" *ngIf="isLoading">
                <i class="fas fa-spinner fa-spin"></i> Cargando clientes...
              </div>
            </div>

            <div class="list-container">
              <h2>Top Productos</h2>
              <div class="productos-list" *ngIf="dashboardData && !isLoading">
                <div class="producto-item" *ngFor="let producto of dashboardData.top10Productos; let i = index">
                  <div class="producto-rank">{{ i + 1 }}</div>
                  <div class="producto-info">
                    <div class="producto-name">{{ producto.descripcion }}</div>
                    <div class="producto-codigo" *ngIf="producto.codigo">Código: {{ producto.codigo }}</div>
                  </div>
                  <div class="producto-stats">
                    <div class="producto-cantidad">{{ formatNumber(producto.cantidadVendida) }} unidades</div>
                    <div class="producto-total">{{ formatCurrency(producto.montoTotal) }}</div>
                  </div>
                </div>
                <div class="no-data" *ngIf="!dashboardData.top10Productos || dashboardData.top10Productos.length === 0">
                  No hay productos disponibles
                </div>
              </div>
              <div class="loading" *ngIf="isLoading">
                <i class="fas fa-spinner fa-spin"></i> Cargando productos...
              </div>
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
      min-height: calc(100vh - 60px);
    }

    .no-empresa-message {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 400px;
    }

    .message-content {
      text-align: center;
      padding: 40px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .message-content i {
      font-size: 4rem;
      color: #7f8c8d;
      margin-bottom: 20px;
    }

    .message-content h2 {
      color: #2c3e50;
      margin-bottom: 10px;
    }

    .message-content p {
      color: #7f8c8d;
    }

    .dashboard-header {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      margin-bottom: 20px;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 20px;
    }

    .dashboard-header h1 {
      margin: 0;
      color: #2c3e50;
      font-size: 1.75rem;
      font-weight: 600;
    }

    .dashboard-content {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .quick-actions-section,
    .stats-section,
    .lists-section {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .quick-actions-section h2,
    .lists-section h2 {
      margin: 0 0 20px 0;
      color: #2c3e50;
      font-size: 1.5rem;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 15px;
    }

    .action-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      color: white;
      border: none;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
      font-size: 0.9rem;
    }

    .action-card:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 12px rgba(0,0,0,0.15);
    }

    .action-card i {
      font-size: 2rem;
      margin-bottom: 10px;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 20px;
    }

    .stat-card {
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      background: white;
      border-left: 4px solid;
    }

    .stat-card.approved {
      border-left-color: #28a745;
    }

    .stat-card.cancelled {
      border-left-color: #dc3545;
    }

    .stat-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid #e0e0e0;
    }

    .stat-header i {
      font-size: 1.5rem;
    }

    .stat-card.approved .stat-header i {
      color: #28a745;
    }

    .stat-card.cancelled .stat-header i {
      color: #dc3545;
    }

    .stat-header h3 {
      margin: 0;
      font-size: 1.25rem;
      color: #2c3e50;
    }

    .stat-body {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .stat-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
    }

    .stat-item:last-child {
      border-bottom: none;
    }

    .stat-label {
      font-size: 0.9rem;
      color: #7f8c8d;
    }

    .stat-value {
      font-weight: 600;
      font-size: 1rem;
      color: #2c3e50;
    }

    .lists-section {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .list-container {
      width: 100%;
    }

    .list-container.full-width {
      width: 100%;
    }

    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      flex-wrap: wrap;
      gap: 15px;
    }

    .list-container h2 {
      margin: 0;
      color: #2c3e50;
      font-size: 1.25rem;
    }

    .btn-view-all {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #3498db;
      color: white;
      border: none;
      padding: 10px 20px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.95rem;
      font-weight: 500;
      transition: background 0.2s, transform 0.2s;
    }

    .btn-view-all:hover {
      background: #2980b9;
      transform: translateY(-2px);
    }

    .btn-view-all i {
      font-size: 1rem;
    }

    mat-paginator {
      border-top: 1px solid #e0e0e0;
    }

    .table-container {
      overflow-x: auto;
    }

    .facturas-table {
      width: 100%;
      border-collapse: collapse;
    }

    .facturas-table th,
    .facturas-table td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #e0e0e0;
    }

    .facturas-table th {
      background: #f8f9fa;
      font-weight: 600;
      color: #2c3e50;
    }

    .facturas-table tr:hover {
      background: #f8f9fa;
    }

    .badge {
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .badge-APROBADO {
      background: #d4edda;
      color: #155724;
    }

    .badge-CANCELADO {
      background: #f8d7da;
      color: #721c24;
    }

    .badge-PENDIENTE {
      background: #fff3cd;
      color: #856404;
    }

    .btn-view {
      background: #3498db;
      color: white;
      border: none;
      padding: 6px 12px;
      border-radius: 4px;
      cursor: pointer;
    }

    .btn-view:hover {
      background: #2980b9;
    }

    .clientes-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .cliente-item {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 6px;
    }

    .cliente-rank {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #3498db;
      color: white;
      border-radius: 50%;
      font-weight: 600;
    }

    .cliente-info {
      flex: 1;
    }

    .cliente-name {
      font-weight: 600;
      color: #2c3e50;
    }

    .cliente-ruc {
      font-size: 0.85rem;
      color: #7f8c8d;
    }

    .cliente-stats {
      text-align: right;
    }

    .cliente-facturas {
      font-size: 0.85rem;
      color: #7f8c8d;
    }

    .cliente-total {
      font-weight: 600;
      color: #2c3e50;
      font-size: 1.1rem;
    }

    .productos-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .producto-item {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 6px;
    }

    .producto-rank {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #9b59b6;
      color: white;
      border-radius: 50%;
      font-weight: 600;
    }

    .producto-info {
      flex: 1;
    }

    .producto-name {
      font-weight: 600;
      color: #2c3e50;
    }

    .producto-codigo {
      font-size: 0.85rem;
      color: #7f8c8d;
    }

    .producto-stats {
      text-align: right;
    }

    .producto-cantidad {
      font-size: 0.85rem;
      color: #7f8c8d;
    }

    .producto-total {
      font-weight: 600;
      color: #2c3e50;
      font-size: 1.1rem;
    }

    .no-data {
      text-align: center;
      padding: 40px;
      color: #7f8c8d;
    }

    .loading {
      text-align: center;
      padding: 40px;
      color: #7f8c8d;
    }

    @media (min-width: 769px) {
      .lists-section:last-child {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 20px;
      }
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 10px;
      }

      .header-content {
        flex-direction: column;
        align-items: stretch;
      }

      .actions-grid {
        grid-template-columns: 1fr;
      }

      .stats-grid {
        grid-template-columns: 1fr;
      }

      .lists-section:last-child {
        display: flex;
        flex-direction: column;
      }

      .facturas-table {
        font-size: 0.85rem;
      }

      .list-header {
        flex-direction: column;
        align-items: stretch;
      }

      .btn-view-all {
        width: 100%;
        justify-content: center;
      }
    }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  selectedEmpresa$: Observable<Empresa | null>;
  dashboardData: DashboardEmpresa | null = null;
  ultimasFacturas: FacturaLegal[] = [];
  isLoading = false;
  loadingFacturas = false;
  selectedDateFilter: 'este-mes' | 'mes-pasado' | 'personalizado' = 'este-mes';
  currentDateRange: DateRange = { fechaDesde: null, fechaHasta: null };
  facturasCurrentPage = 0;
  facturasTotalPages = 0;
  facturasTotalElements = 0;
  facturasPageSize = 10;

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private dashboardApi: DashboardApiService,
    private facturaApi: FacturaApiService,
    private router: Router
  ) {
    this.selectedEmpresa$ = this.store.select(selectSelectedEmpresa).pipe(
      map(empresa => empresa ?? null)
    );
  }

  ngOnInit(): void {
    // Inicializar rango de fechas para "este mes"
    // El date-filter emitirá las fechas en su ngOnInit, así que no necesitamos inicializar aquí
    this.selectedEmpresa$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(empresa => {
      if (empresa && this.currentDateRange.fechaDesde && this.currentDateRange.fechaHasta) {
        this.loadDashboardData(empresa.id);
      } else {
        this.dashboardData = null;
        this.ultimasFacturas = [];
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onDateRangeChange(range: DateRange): void {
    this.currentDateRange = range;
    this.facturasCurrentPage = 0; // Resetear a primera página

    // Solo cargar si tenemos fechas válidas
    if (range.fechaDesde && range.fechaHasta) {
      this.selectedEmpresa$.pipe(
        takeUntil(this.destroy$)
      ).subscribe(empresa => {
        if (empresa) {
          this.loadDashboardData(empresa.id);
        }
      });
    }
  }

  private loadDashboardData(empresaId: number): void {
    this.isLoading = true;

    const fechaDesde = this.currentDateRange.fechaDesde
      ? this.formatDateForApi(this.currentDateRange.fechaDesde)
      : undefined;
    const fechaHasta = this.currentDateRange.fechaHasta
      ? this.formatDateForApi(this.currentDateRange.fechaHasta)
      : undefined;

    this.dashboardApi.getDashboardEmpresa(empresaId, fechaDesde, fechaHasta).subscribe({
      next: (data) => {
        this.dashboardData = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar dashboard:', error);
        this.isLoading = false;
      }
    });

    this.loadUltimasFacturas(empresaId, this.facturasCurrentPage);
  }

  private loadUltimasFacturas(empresaId: number, page: number = 0): void {
    this.loadingFacturas = true;

    const fechaDesde = this.currentDateRange.fechaDesde
      ? this.formatDateForApi(this.currentDateRange.fechaDesde)
      : undefined;
    const fechaHasta = this.currentDateRange.fechaHasta
      ? this.formatDateForApi(this.currentDateRange.fechaHasta)
      : undefined;

    this.facturaApi.getAllPaginated({
      empresaId,
      fechaDesde,
      fechaHasta
    }, page, this.facturasPageSize).subscribe({
      next: (response) => {
        this.ultimasFacturas = response.content || [];
        this.facturasTotalPages = response.totalPages;
        this.facturasTotalElements = response.totalElements;
        this.facturasCurrentPage = page;
        this.loadingFacturas = false;
      },
      error: (error) => {
        console.error('Error al cargar facturas:', error);
        this.loadingFacturas = false;
      }
    });
  }

  onFacturasPageChange(event: PageEvent): void {
    this.facturasCurrentPage = event.pageIndex;
    this.facturasPageSize = event.pageSize;
    this.selectedEmpresa$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(empresa => {
      if (empresa) {
        this.loadUltimasFacturas(empresa.id, event.pageIndex);
      }
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  navigateToFacturacion(tipo: string): void {
    this.selectedEmpresa$.pipe(takeUntil(this.destroy$)).subscribe(empresa => {
      if (empresa) {
        const queryParams: any = { empresaId: empresa.id };
        if (tipo) {
          queryParams.tipo = tipo;
        }
        this.router.navigate(['/facturacion/nueva'], { queryParams });
      }
    });
  }

  navigateToNotasRemision(): void {
    this.selectedEmpresa$.pipe(takeUntil(this.destroy$)).subscribe(empresa => {
      if (empresa) {
        const queryParams: any = { empresaId: empresa.id };
        this.router.navigate(['/notas/notas-remision'], { queryParams });
      }
    });
  }

  navigateToFacturasList(): void {
    this.selectedEmpresa$.pipe(takeUntil(this.destroy$)).subscribe(empresa => {
      if (empresa) {
        const queryParams: any = { empresaId: empresa.id };
        this.router.navigate(['/facturacion'], { queryParams });
      }
    });
  }

  viewFactura(id: number): void {
    this.router.navigate(['/facturacion', id]);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('es-PY', {
      style: 'currency',
      currency: 'PYG',
      minimumFractionDigits: 0
    }).format(value);
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('es-PY', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).format(date);
  }

  formatDateForApi(date: Date): string {
    // Formato ISO_LOCAL_DATE_TIME (sin zona horaria) como espera el backend
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }

  formatNumber(value: number): string {
    return new Intl.NumberFormat('es-PY', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }

  getEstadoClass(estado?: string): string {
    if (!estado) return 'PENDIENTE';
    return estado.toUpperCase();
  }
}
