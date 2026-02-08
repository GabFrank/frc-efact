import { Component, OnInit, signal, computed, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BreakpointObserver } from '@angular/cdk/layout';
import { FacturaApiService, ResumenFacturas } from '../../core/api/factura-api.service';
import { SifenApiService } from '../../core/api/sifen-api.service';
import { FacturaLegal } from '../../models/factura.model';
import { EstadoDE } from '../../models/documento-electronico.model';
import { FacturaFiltro } from '../../models/reporte.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { FacturaEstadoDialogComponent } from '../../shared/components/factura-estado-dialog/factura-estado-dialog.component';
import { CancelarDeDialogComponent } from '../documentos/cancelar-de-dialog.component';
import { NominarDeDialogComponent } from './nominar-de-dialog.component';
import { InutilizarNumerosDialogComponent } from './inutilizar-numeros-dialog.component';
import { ClienteFormComponent } from '../clientes/cliente-form.component';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { Cliente } from '../../models/cliente.model';
import { NotaCreditoFormDialogComponent } from '../notas/nota-credito-form-dialog.component';
import { NotaDebitoFormDialogComponent } from '../notas/nota-debito-form-dialog.component';
import * as FacturacionActions from '../../core/state/facturacion/facturacion.actions';
import {
  selectAllFacturas,
  selectFacturacionLoading,
  selectFacturacionError
} from '../../core/state/facturacion/facturacion.selectors';

@Component({
  selector: 'app-factura-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    MatChipsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatExpansionModule,
    MatPaginatorModule,
    MatMenuModule,
    MatTooltipModule,
    DataTableComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="facturacion-dashboard">
      <!-- Header -->
      <div class="dashboard-header">
        <div class="header-content">
          <div class="header-title">
            <h1>Dashboard de Facturación</h1>
            <p class="subtitle">Gestiona y monitorea tus facturas</p>
          </div>
          <div class="header-actions">
            <button mat-stroked-button color="primary" (click)="inutilizarDesdeHeader()">
              <mat-icon>block</mat-icon>
              Inutilizar Números
            </button>
            <button mat-raised-button color="primary" (click)="crearFactura()">
              <mat-icon>add</mat-icon>
              Nueva Factura
            </button>
          </div>
        </div>
      </div>

      <!-- Resumen de Facturas -->
      <div class="stats-section" *ngIf="resumen() || cargandoResumen()">
        <!-- Facturas Aprobadas -->
        <mat-card class="stat-card aprobadas">
          <div class="stat-card-header">
            <div class="stat-icon">
              <mat-icon>check_circle</mat-icon>
            </div>
            <div class="stat-content-header">
              <div class="stat-value">{{ resumen()?.cantidadFacturasAprobadas || 0 }}</div>
              <div class="stat-label">Facturas Aprobadas</div>
              <div class="stat-total-header" *ngIf="resumen()">
                Total: ₲ {{ resumen()!.totalFacturasAprobadas | number:'1.0-0' }}
              </div>
              <div *ngIf="cargandoResumen()" class="loading-indicator">
                <mat-icon class="spinning">refresh</mat-icon>
              </div>
            </div>
          </div>
          <mat-expansion-panel class="stat-expansion-panel" *ngIf="resumen()">
            <mat-expansion-panel-header>
              <mat-panel-title>
                Ver desglose por tipo de IVA
              </mat-panel-title>
            </mat-expansion-panel-header>
            <div class="stat-breakdown">
              <div class="breakdown-item">
                <span class="breakdown-label">IVA 10%:</span>
                <span class="breakdown-value">₲ {{ resumen()!.totalIva10Aprobadas | number:'1.0-0' }}</span>
              </div>
              <div class="breakdown-item">
                <span class="breakdown-label">IVA 5%:</span>
                <span class="breakdown-value">₲ {{ resumen()!.totalIva5Aprobadas | number:'1.0-0' }}</span>
              </div>
              <div class="breakdown-item">
                <span class="breakdown-label">Exentas:</span>
                <span class="breakdown-value">₲ {{ resumen()!.totalExentasAprobadas | number:'1.0-0' }}</span>
              </div>
            </div>
          </mat-expansion-panel>
        </mat-card>

        <!-- Facturas No Aprobadas -->
        <mat-card class="stat-card no-aprobadas">
          <div class="stat-card-header">
            <div class="stat-icon">
              <mat-icon>cancel</mat-icon>
            </div>
            <div class="stat-content-header">
              <div class="stat-value">{{ resumen()?.cantidadFacturasNoAprobadas || 0 }}</div>
              <div class="stat-label">Facturas No Aprobadas</div>
              <div class="stat-total-header" *ngIf="resumen()">
                Total: ₲ {{ resumen()!.totalFacturasNoAprobadas | number:'1.0-0' }}
              </div>
              <div *ngIf="cargandoResumen()" class="loading-indicator">
                <mat-icon class="spinning">refresh</mat-icon>
              </div>
            </div>
          </div>
        </mat-card>
      </div>

      <!-- Contenido principal -->
      <mat-card class="main-card">
        <mat-card-header>
          <mat-card-title>
            <h2>Lista de Facturas</h2>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Filtros -->
          <div class="filters-section">
            <mat-form-field appearance="outline">
              <mat-label>Fecha desde</mat-label>
              <input matInput
                     [matDatepicker]="pickerDesde"
                     [(ngModel)]="filtro.fechaDesde">
              <mat-datepicker-toggle matSuffix [for]="pickerDesde"></mat-datepicker-toggle>
              <mat-datepicker #pickerDesde></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Fecha hasta</mat-label>
              <input matInput
                     [matDatepicker]="pickerHasta"
                     [(ngModel)]="filtro.fechaHasta">
              <mat-datepicker-toggle matSuffix [for]="pickerHasta"></mat-datepicker-toggle>
              <mat-datepicker #pickerHasta></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Cliente</mat-label>
              <input matInput
                     [(ngModel)]="searchCliente"
                     (ngModelChange)="onSearchClienteChange()"
                     placeholder="Buscar por nombre o RUC">
              <mat-icon matPrefix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Estado DE</mat-label>
              <mat-select [ngModel]="filtro.estado" (ngModelChange)="onEstadoChange($event)">
                <mat-option [value]="null">Todos</mat-option>
                <mat-option value="PENDIENTE">Pendiente</mat-option>
                <mat-option value="EN_PROCESO">En Proceso</mat-option>
                <mat-option value="APROBADO">Aprobado</mat-option>
                <mat-option value="RECHAZADO">Rechazado</mat-option>
                <mat-option value="CANCELADO">Cancelado</mat-option>
                <mat-option value="ERROR">Error</mat-option>
                <mat-option value="SIN_DE">Sin DE</mat-option>
              </mat-select>
            </mat-form-field>

            <div class="filter-buttons">
              <button mat-raised-button color="primary" (click)="aplicarFiltros()">
                <mat-icon>search</mat-icon>
                Filtrar
              </button>
              <button mat-stroked-button (click)="limpiarFiltros()">
                <mat-icon>clear</mat-icon>
                Limpiar
              </button>
            </div>
          </div>

          <!-- Tabla de facturas -->
          <app-loading-spinner *ngIf="loading$ | async" />

          <div class="list-desktop" *ngIf="!(loading$ | async) && !isMobile()">
            <app-data-table
              [columns]="columns"
              [data]="facturasPaginas()"
              [actions]="tableActions"
              [pageSize]="pageSize"
              [pageIndex]="pageIndex"
              [totalItems]="facturasFiltradas().length"
              (actionClick)="onActionClick($event)"
              (pageChange)="onPageChange($event)"
            />
          </div>
          <div class="list-mobile" *ngIf="!(loading$ | async) && isMobile()">
            <div class="mobile-cards" *ngIf="facturasPaginas().length > 0">
              <mat-card class="list-card" *ngFor="let item of facturasPaginas()">
                <mat-card-header class="list-card-header">
                  <mat-card-title class="list-card-title">
                    <span class="list-card-num">{{ formatNumeroFactura(item.numeroFactura) }}</span>
                    <span class="list-card-date">{{ formatDateForCard(item.fecha) }}</span>
                  </mat-card-title>
                  <button class="list-card-menu-trigger" mat-icon-button color="primary"
                    [matMenuTriggerFor]="cardActionMenu" (click)="setMenuContext(item)"
                    matTooltip="Acciones" aria-label="Acciones">
                    <mat-icon>more_vert</mat-icon>
                  </button>
                </mat-card-header>
                <mat-card-content class="list-card-content factura-card-content">
                  <div class="list-card-field list-card-field-full list-card-field-cliente">
                    <span class="list-card-label">Cliente</span>
                    <span class="list-card-value">{{ item.nombre || '—' }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">RUC</span>
                    <span class="list-card-value">{{ item.ruc || '—' }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Total</span>
                    <span class="list-card-value">{{ formatTotalForCard(item) }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Tipo</span>
                    <span class="list-card-value">{{ item.credito ? 'Crédito' : 'Contado' }}</span>
                  </div>
                  <div class="list-card-field">
                    <span class="list-card-label">Estado DE</span>
                    <span class="list-card-value">
                      <span class="estado-badge" [ngClass]="getEstadoDEClass(item.estadoDocumentoElectronico)">{{ getEstadoDELabel(item.estadoDocumentoElectronico) }}</span>
                    </span>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
            <div class="mobile-empty" *ngIf="facturasPaginas().length === 0">No hay facturas para mostrar.</div>
            <mat-paginator *ngIf="facturasPaginas().length > 0" [length]="facturasFiltradas().length"
              [pageSize]="pageSize" [pageIndex]="pageIndex" [pageSizeOptions]="[5, 10, 25, 50]"
              (page)="onPageChange($event)" showFirstLastButtons></mat-paginator>
            <mat-menu #cardActionMenu="matMenu" class="card-action-menu">
              <button mat-menu-item *ngFor="let a of menuActions" type="button" class="list-card-menu-item"
                (click)="menuRow && onActionClick({ action: (a.label || a.tooltip || a.icon) || '', row: menuRow })">
                <mat-icon>{{ a.icon }}</mat-icon>
                <span>{{ a.label || a.tooltip || a.icon }}</span>
              </button>
            </mat-menu>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .facturacion-dashboard {
      padding: 24px;
      background-color: #f5f5f5;
      min-height: 100vh;
    }

    .dashboard-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      padding: 32px;
      margin-bottom: 24px;
      color: white;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
    }

    .header-actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }

    .header-title h1 {
      margin: 0 0 8px 0;
      font-size: 2rem;
      font-weight: 600;
      color: white;
    }

    .subtitle {
      margin: 0;
      font-size: 1rem;
      opacity: 0.9;
      color: white;
    }

    .stats-section {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 24px;
    }

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 0;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: transform 0.2s, box-shadow 0.2s;
      overflow: hidden;
    }

    .stat-card-header {
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .stat-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .stat-card.total {
      border-left: 4px solid #667eea;
    }

    .stat-card.monto {
      border-left: 4px solid #4caf50;
    }

    .stat-card.credito {
      border-left: 4px solid #ff9800;
    }

    .stat-card.contado {
      border-left: 4px solid #2196f3;
    }

    .stat-card.aprobadas {
      border-left: 4px solid #4caf50;
    }

    .stat-card.no-aprobadas {
      border-left: 4px solid #f44336;
    }

    .stat-icon {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .stat-card.monto .stat-icon {
      background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
    }

    .stat-card.credito .stat-icon {
      background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
    }

    .stat-card.contado .stat-icon {
      background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
    }

    .stat-card.aprobadas .stat-icon {
      background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
    }

    .stat-card.no-aprobadas .stat-icon {
      background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%);
    }

    .stat-icon mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
    }

    .stat-content {
      flex: 1;
    }

    .stat-content-header {
      flex: 1;
    }

    .stat-value {
      font-size: 2rem;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 0.9rem;
      color: #7f8c8d;
      font-weight: 500;
    }

    .stat-total-header {
      margin-top: 8px;
      font-size: 1.1rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .stat-expansion-panel {
      box-shadow: none;
      border-top: 1px solid #e0e0e0;
    }

    .stat-expansion-panel ::ng-deep .mat-expansion-panel-body {
      padding: 16px 24px;
    }

    .stat-breakdown {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .breakdown-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;
    }

    .breakdown-item:last-child {
      border-bottom: none;
    }

    .breakdown-label {
      font-size: 0.95rem;
      color: #5f6368;
      font-weight: 500;
    }

    .breakdown-value {
      font-size: 1rem;
      color: #2c3e50;
      font-weight: 600;
    }

    .loading-indicator {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-top: 12px;
    }

    .spinning {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    .breakdown-item {
      padding: 2px 0;
    }

    .loading-indicator {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-top: 12px;
    }

    .spinning {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    .main-card {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      border-radius: 12px;
    }

    mat-card-header {
      margin-bottom: 24px;
    }

    mat-card-header h2 {
      margin: 0;
      font-size: 1.5rem;
      font-weight: 600;
      color: #2c3e50;
    }

    .filters-section {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      flex-wrap: wrap;
      align-items: center;
    }

    .filter-buttons {
      display: flex;
      flex-direction: row;
      gap: 8px;
      align-items: center;
    }

    mat-form-field {
      min-width: 180px;
    }

    .estado-chip {
      font-size: 12px;
      padding: 4px 8px;
      border-radius: 12px;
      font-weight: 500;
    }

    .estado-pendiente {
      background-color: #fff3cd;
      color: #856404;
    }

    .estado-proceso {
      background-color: #cfe2ff;
      color: #084298;
    }

    .estado-aprobado {
      background-color: #d1e7dd;
      color: #0f5132;
    }

    .estado-rechazado {
      background-color: #f8d7da;
      color: #842029;
    }

    .estado-cancelado {
      background-color: #e2e3e5;
      color: #41464b;
    }

    .estado-error {
      background-color: #f8d7da;
      color: #842029;
    }

    .estado-sin-de {
      background-color: #f8f9fa;
      color: #6c757d;
    }

    .list-desktop { width: 100%; }
    .list-mobile { width: 100%; }
    .mobile-cards { display: flex; flex-direction: column; gap: 10px; margin-bottom: 12px; }
    .list-card { margin: 0; }
    .list-card-header { display: flex; align-items: flex-start; justify-content: space-between; padding: 10px 14px 4px; margin-bottom: 0; gap: 8px; }
    .list-card-title { display: flex; flex-direction: column; gap: 1px; margin: 0; font-size: 1rem; min-width: 0; flex: 1; }
    .list-card-num { font-weight: 600; color: #2c3e50; font-size: 1rem; }
    .list-card-date { font-size: 0.75rem; color: rgba(0,0,0,0.55); }
    :host-context(body.dark-theme) .list-card-num { color: #e0e0e0; }
    :host-context(body.dark-theme) .list-card-date { color: rgba(255,255,255,0.55); }
    .list-card-menu-trigger { flex-shrink: 0; margin: -4px -4px 0 0; }
    .list-card-menu-trigger .mat-icon { font-size: 1.5rem; width: 24px; height: 24px; }
    .list-card-content { padding: 0 !important; }
    .list-card-content.factura-card-content {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 6px 14px;
      padding: 4px 14px 12px !important;
    }
    .list-card-content.factura-card-content .list-card-field { display: flex; flex-direction: column; gap: 1px; }
    .list-card-content.factura-card-content .list-card-field-full { grid-column: 1 / -1; }
    .list-card-content.factura-card-content .list-card-field-cliente .list-card-value {
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      word-break: break-word;
    }
    .list-card-content.factura-card-content .list-card-label { font-size: 0.6875rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.04em; color: rgba(0,0,0,0.55); }
    .list-card-content.factura-card-content .list-card-value { font-size: 0.875rem; color: #2c3e50; word-break: break-word; line-height: 1.3; }
    :host-context(body.dark-theme) .list-card-content.factura-card-content .list-card-label { color: rgba(255,255,255,0.55); }
    :host-context(body.dark-theme) .list-card-content.factura-card-content .list-card-value { color: #e0e0e0; }
    .list-card-content.factura-card-content .estado-badge {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 600;
      line-height: 1.3;
    }
    .list-card-field { display: flex; flex-direction: column; gap: 2px; }
    .list-card-label { font-size: 0.75rem; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; color: rgba(0,0,0,0.6); }
    .list-card-value { font-size: 0.9375rem; color: #2c3e50; word-break: break-word; }
    :host-context(body.dark-theme) .list-card-label { color: rgba(255,255,255,0.6); }
    :host-context(body.dark-theme) .list-card-value { color: #e0e0e0; }
    .mobile-empty { padding: 24px 16px; text-align: center; color: rgba(0,0,0,0.6); }
    :host-context(body.dark-theme) .mobile-empty { color: rgba(255,255,255,0.6); }
    .list-mobile mat-paginator { border-top: 1px solid rgba(0,0,0,0.12); }
    :host-context(body.dark-theme) .list-mobile mat-paginator { border-top-color: rgba(255,255,255,0.12); }

    @media (max-width: 768px) {
      .facturacion-dashboard {
        padding: 16px;
      }

      .dashboard-header {
        padding: 20px;
      }

      .header-title h1 {
        font-size: 1.5rem;
      }

      .stats-section {
        grid-template-columns: 1fr;
      }

      .filters-section {
        flex-direction: column;
      }

      mat-form-field {
        width: 100%;
      }
    }
  `]
})
export class FacturaListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private breakpointObserver = inject(BreakpointObserver);
  isMobile = signal(false);
  menuRow: FacturaLegal | null = null;
  menuActions: TableAction[] = [];

  facturas$: Observable<FacturaLegal[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  facturas = signal<FacturaLegal[]>([]);
  facturasFiltradas = signal<FacturaLegal[]>([]);
  facturasPaginas = signal<FacturaLegal[]>([]);
  resumen = signal<ResumenFacturas | null>(null);
  cargandoResumen = signal<boolean>(false);

  // Paginación
  pageSize = 10;
  pageIndex = 0;

  // Tipo local que permite Date para las fechas (viene del datepicker)
  filtro: Omit<FacturaFiltro, 'fechaDesde' | 'fechaHasta'> & {
    fechaDesde?: Date | string;
    fechaHasta?: Date | string;
  } = {
    empresaId: undefined, // Se obtiene de query params
    fechaDesde: undefined,
    fechaHasta: undefined,
    clienteId: undefined,
    estado: undefined,
    montoMinimo: undefined,
    montoMaximo: undefined
  };

  searchCliente = '';

  // Monedas disponibles
  readonly MONEDAS = [
    { codigo: 'PYG', nombre: 'Guaraní Paraguayo', simbolo: '₲' },
    { codigo: 'USD', nombre: 'Dólar Americano', simbolo: '$' },
    { codigo: 'EUR', nombre: 'Euro', simbolo: '€' },
    { codigo: 'BRL', nombre: 'Real Brasileño', simbolo: 'R$' },
    { codigo: 'ARS', nombre: 'Peso Argentino', simbolo: '$' }
  ];

  // Función helper para obtener símbolo de moneda
  private obtenerSimboloMoneda(codigo?: string): string {
    if (!codigo || codigo === 'PYG') {
      return '₲';
    }
    const moneda = this.MONEDAS.find(m => m.codigo === codigo);
    return moneda?.simbolo || '₲';
  }

  // Función para formatear el total con moneda extranjera si aplica
  private formatearTotal(value: number, row: any): string {
    // Si hay moneda extranjera y cambio, mostrar en moneda extranjera
    if (row.monedaExtranjera &&
        row.monedaExtranjera !== 'PYG' &&
        row.cambio &&
        row.cambio > 0) {
      const valorEnMonedaExtranjera = value / row.cambio;
      const simbolo = this.obtenerSimboloMoneda(row.monedaExtranjera);
      return `${simbolo} ${valorEnMonedaExtranjera.toLocaleString('es-PY', { minimumFractionDigits: 0, maximumFractionDigits: 3 })}`;
    }
    // Si no hay moneda extranjera, mostrar en guaraníes con hasta 3 decimales
    return `₲ ${value.toLocaleString('es-PY', { minimumFractionDigits: 0, maximumFractionDigits: 3 })}`;
  }

  columns: TableColumn[] = [
    {
      key: 'numeroFactura',
      label: 'Número',
      sortable: true,
      format: (value: number) => value.toString().padStart(7, '0')
    },
    {
      key: 'fecha',
      label: 'Fecha',
      sortable: true,
      format: (value: string) => new Date(value).toLocaleDateString('es-PY')
    },
    { key: 'nombre', label: 'Cliente', sortable: true },
    { key: 'ruc', label: 'RUC', sortable: false },
    {
      key: 'totalFinal',
      label: 'Total',
      sortable: true,
      format: (value: number, row: any) => this.formatearTotal(value, row)
    },
    {
      key: 'credito',
      label: 'Tipo',
      format: (value: boolean) => value ? 'Crédito' : 'Contado'
    },
    {
      key: 'estadoDE',
      label: 'Estado DE',
      format: (value: string, row: any) => this.formatEstadoDE(value, row)
    }
  ];

  tableActions: TableAction[] = [
    // Grupo: Información y Visualización
    {
      icon: 'info',
      label: 'Ver estado',
      color: 'primary',
      tooltip: 'Ver estado completo de factura, DE y lote',
      group: 'Información'
    },
    {
      icon: 'open_in_new',
      label: 'Ver / Editar',
      tooltip: 'Ver o editar factura',
      group: 'Información'
    },
    {
      icon: 'picture_as_pdf',
      label: 'Abrir PDF',
      color: 'primary',
      tooltip: 'Abrir PDF del KUDE',
      visible: (row: any) => row.documentoElectronicoId != null,
      group: 'Información'
    },
    // Grupo: Gestión DE
    {
      icon: 'description',
      label: 'Generar DE',
      color: 'accent',
      tooltip: 'Generar documento electrónico',
      visible: (row: any) => !row.documentoElectronicoId,
      group: 'Gestión DE'
    },
    {
      icon: 'link_off',
      label: 'Desvincular DE',
      color: 'warn',
      tooltip: 'Desvincular documento electrónico (solo si tiene error permanente)',
      visible: (row: any) => row.documentoElectronicoId != null && (row.estadoDocumentoElectronico === 'ERROR' || row.estadoDocumentoElectronico === 'RECHAZADO'),
      group: 'Gestión DE'
    },
    {
      icon: 'refresh',
      label: 'Reenviar DE',
      color: 'accent',
      tooltip: 'Reenviar documento electrónico en un nuevo lote',
      visible: (row: any) => row.documentoElectronicoId != null,
      group: 'Gestión DE'
    },
    {
      icon: 'cancel',
      label: 'Cancelar DE',
      color: 'warn',
      tooltip: 'Cancelar documento electrónico',
      visible: (row: any) => row.cdcDocumentoElectronico != null && row.estadoDE !== 'CANCELADO',
      group: 'Gestión DE'
    },
    {
      icon: 'person_add',
      label: 'Nominar DE',
      color: 'primary',
      tooltip: 'Nominar receptor del documento electrónico',
      visible: (row: any) => row.cdcDocumentoElectronico != null && (!row.clienteId || row.clienteId === null),
      group: 'Gestión DE'
    },
    // Grupo: SIFEN y Consultas
    {
      icon: 'sync',
      label: 'Consultar SIFEN',
      color: 'primary',
      tooltip: 'Consultar estado en SIFEN',
      group: 'SIFEN'
    },
    // Grupo: Comunicación
    {
      icon: 'email',
      label: 'Reenviar Email',
      color: 'primary',
      tooltip: 'Reenviar email con factura electrónica al cliente',
      visible: (row: any) => row.documentoElectronicoId != null && row.estadoDocumentoElectronico === 'APROBADO',
      group: 'Comunicación'
    },
    {
      icon: 'block',
      label: 'Inutilizar Número',
      color: 'warn',
      tooltip: 'Inutilizar número de documento',
      visible: (row: any) => row.timbradoDetalleId != null && row.numeroFactura != null,
      group: 'Gestión DE'
    },
    {
      icon: 'delete',
      label: 'Eliminar',
      color: 'warn',
      tooltip: 'Eliminar factura',
      group: 'Gestión DE'
    },
    // Grupo: Notas
    {
      icon: 'add_circle',
      label: 'Crear Nota de Crédito',
      color: 'primary',
      tooltip: 'Crear nota de crédito para esta factura',
      visible: (row: any) => row.id != null,
      group: 'Notas'
    },
    {
      icon: 'remove_circle',
      label: 'Crear Nota de Débito',
      color: 'accent',
      tooltip: 'Crear nota de débito para esta factura',
      visible: (row: any) => row.id != null,
      group: 'Notas'
    }
  ];

  constructor(
    private store: Store,
    private facturaApi: FacturaApiService,
    private sifenApi: SifenApiService,
    private clienteApi: ClienteApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.facturas$ = this.store.select(selectAllFacturas);
    this.loading$ = this.store.select(selectFacturacionLoading);
    this.error$ = this.store.select(selectFacturacionError);
  }

  ngOnInit(): void {
    this.breakpointObserver.observe(['(max-width: 768px)']).pipe(takeUntil(this.destroy$)).subscribe(s => this.isMobile.set(s.matches));
    // Obtener empresaId de query params
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const empresaId = params['empresaId'];
      const refresh = params['refresh'] === 'true';

      // Si viene con refresh=true, limpiar filtros y refrescar
      if (refresh) {
        // Preservar empresaId si viene en query params
        const empresaIdToKeep = empresaId ? +empresaId : this.filtro.empresaId;
        this.filtro = {
          empresaId: empresaIdToKeep,
          fechaDesde: undefined,
          fechaHasta: undefined,
          clienteId: undefined,
          estado: undefined,
          montoMinimo: undefined,
          montoMaximo: undefined
        };
        this.searchCliente = '';
        // Remover el query param refresh de la URL
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { empresaId: empresaIdToKeep || undefined },
          queryParamsHandling: 'merge',
          replaceUrl: true
        });
        // Cargar facturas con filtros limpios
        this.cargarFacturas();
      } else if (empresaId) {
        // Crear un nuevo objeto filtro en lugar de mutar el existente
        this.filtro = {
          ...this.filtro,
          empresaId: +empresaId
        };
        // Recargar facturas cuando cambia el empresaId
        this.cargarFacturas();
      }
    });

    // Suscribirse a las facturas del store
    this.facturas$.pipe(takeUntil(this.destroy$)).subscribe(facturas => {
      this.facturas.set(facturas.map(f => ({
        ...f,
        estadoDE: this.getEstadoDE(f)
      } as any)));
      // Aplicar solo filtro local cuando se cargan las facturas
      this.aplicarFiltroLocal();
    });

    // Manejar errores
    this.error$.pipe(takeUntil(this.destroy$)).subscribe(error => {
      if (error) {
        this.snackBar.open(`Error: ${error}`, 'Cerrar', { duration: 5000 });
      }
    });

    // Cargar facturas inicialmente solo si hay empresaId en query params
    // Si no hay empresaId, esperar a que se establezca desde query params
    const empresaIdFromParams = this.route.snapshot.queryParams['empresaId'];
    if (empresaIdFromParams) {
      this.filtro = {
        ...this.filtro,
        empresaId: +empresaIdFromParams
      };
      this.cargarFacturas();
      this.cargarResumen();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  formatNumeroFactura(n: number | null | undefined): string {
    return n != null ? n.toString().padStart(7, '0') : '—';
  }

  formatDateForCard(fecha: string | null | undefined): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-PY');
  }

  formatTotalForCard(row: FacturaLegal): string {
    const value = row.totalFinal;
    if (value == null) return '—';
    if (row.monedaExtranjera && row.monedaExtranjera !== 'PYG' && row.cambio && row.cambio > 0) {
      const valorEnMonedaExtranjera = value / row.cambio;
      const m = this.MONEDAS.find(x => x.codigo === row.monedaExtranjera);
      const simbolo = m?.simbolo ?? '₲';
      return `${simbolo} ${valorEnMonedaExtranjera.toLocaleString('es-PY', { minimumFractionDigits: 0, maximumFractionDigits: 3 })}`;
    }
    return `₲ ${value.toLocaleString('es-PY', { minimumFractionDigits: 0, maximumFractionDigits: 3 })}`;
  }

  getEstadoDELabel(estado: string | null | undefined): string {
    const map: { [key: string]: string } = {
      'PENDIENTE': '⏳ Pendiente',
      'EN_PROCESO': '🔄 En Proceso',
      'APROBADO': '✅ Aprobado',
      'RECHAZADO': '❌ Rechazado',
      'CANCELADO': '🚫 Cancelado',
      'ERROR': '⚠️ Error',
      'SIN_DE': '📄 Sin DE'
    };
    return map[estado || ''] || (estado || '—');
  }

  getEstadoDEClass(estado: string | null | undefined): string {
    const k = (estado || 'SIN_DE').toUpperCase();
    const map: { [key: string]: string } = {
      'PENDIENTE': 'estado-pendiente',
      'EN_PROCESO': 'estado-proceso',
      'APROBADO': 'estado-aprobado',
      'RECHAZADO': 'estado-rechazado',
      'CANCELADO': 'estado-cancelado',
      'ERROR': 'estado-error',
      'SIN_DE': 'estado-sin-de'
    };
    return map[k] || 'estado-sin-de';
  }

  getVisibleActions(row: FacturaLegal | null): TableAction[] {
    return row && this.tableActions?.length ? this.tableActions.filter(a => !a.visible || a.visible(row)) : [];
  }

  setMenuContext(item: FacturaLegal): void {
    this.menuRow = item;
    this.menuActions = this.getVisibleActions(item);
  }

  /**
   * Convierte un objeto Date a string en formato ISO_LOCAL_DATE_TIME
   * Para fechaDesde: establece la hora a 00:00:00
   * Para fechaHasta: establece la hora a 23:59:59
   * Usa la hora local (no UTC) para evitar problemas de zona horaria
   */
  private convertirFechaAString(fecha: Date | string | undefined, esFechaHasta: boolean = false): string | undefined {
    if (!fecha) return undefined;
    
    // Si ya es un string, retornarlo tal cual (asumiendo que ya está en el formato correcto)
    if (typeof fecha === 'string') return fecha;
    
    // Si es un objeto Date, convertirlo al formato ISO_LOCAL_DATE_TIME
    // Usar métodos locales (getFullYear, getMonth, getDate) en lugar de UTC
    // para evitar problemas de zona horaria
    if (!(fecha instanceof Date) || isNaN(fecha.getTime())) return undefined;
    
    // Obtener año, mes y día usando métodos locales
    const year = fecha.getFullYear();
    const month = String(fecha.getMonth() + 1).padStart(2, '0');
    const day = String(fecha.getDate()).padStart(2, '0');
    
    // Para fechaDesde: 00:00:00, para fechaHasta: 23:59:59
    const time = esFechaHasta ? '23:59:59' : '00:00:00';
    
    return `${year}-${month}-${day}T${time}`;
  }

  cargarFacturas(): void {
    // Convertir las fechas a formato string antes de enviar
    const filtroConvertido: FacturaFiltro = {
      ...this.filtro,
      fechaDesde: this.convertirFechaAString(this.filtro.fechaDesde, false),
      fechaHasta: this.convertirFechaAString(this.filtro.fechaHasta, true)
    };
    this.store.dispatch(FacturacionActions.loadFacturas({ filtro: filtroConvertido }));
  }

  getEstadoDE(factura: FacturaLegal): string {
    return factura.estadoDocumentoElectronico ?? 'SIN_DE';
  }

  formatEstadoDE(estado: string, row: any): string {
    const estadoMap: { [key: string]: string } = {
      'PENDIENTE': '⏳ Pendiente',
      'EN_PROCESO': '🔄 En Proceso',
      'APROBADO': '✅ Aprobado',
      'RECHAZADO': '❌ Rechazado',
      'CANCELADO': '🚫 Cancelado',
      'ERROR': '⚠️ Error',
      'SIN_DE': '📄 Sin DE'
    };
    const estadoText = estadoMap[estado] || estado;

    // Si hay URL del QR, agregar icono clickeable
    if (row?.urlQrDocumentoElectronico && estado !== 'SIN_DE') {
      const urlQr = row.urlQrDocumentoElectronico;
      // Escapar comillas y otros caracteres especiales en la URL
      const urlEscaped = urlQr.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
      return `${estadoText} <a href="${urlEscaped}" target="_blank" class="qr-link" title="Abrir consulta en SIFEN" onclick="event.stopPropagation(); return true;" style="vertical-align: middle; margin-left: 6px; display: inline-block; color: #1976d2; text-decoration: none; font-size: 18px;">🔗</a>`;
    }

    return estadoText;
  }

  onEstadoChange(estado: string | null): void {
    this.filtro = {
      ...this.filtro,
      estado: estado || undefined
    };
  }

  aplicarFiltros(): void {
    // Resetear paginación al aplicar filtros
    this.pageIndex = 0;
    // Cargar facturas desde el backend con los filtros aplicados
    // El filtro local se aplicará automáticamente cuando lleguen las facturas (en el subscribe)
    this.cargarFacturas();
    // Cargar resumen usando solo las fechas
    this.cargarResumen();
  }

  onSearchClienteChange(): void {
    // Resetear paginación cuando cambia la búsqueda
    this.pageIndex = 0;
    this.aplicarFiltroLocal();
  }

  cargarResumen(): void {
    if (!this.filtro.empresaId) return;

    this.cargandoResumen.set(true);
    // Convertir las fechas a formato string antes de enviar
    const fechaDesdeStr = this.convertirFechaAString(this.filtro.fechaDesde, false);
    const fechaHastaStr = this.convertirFechaAString(this.filtro.fechaHasta, true);
    
    this.facturaApi.getResumen(
      this.filtro.empresaId,
      fechaDesdeStr,
      fechaHastaStr
    ).pipe(takeUntil(this.destroy$)).subscribe({
      next: (resumen) => {
        this.resumen.set(resumen);
        this.cargandoResumen.set(false);
      },
      error: (error) => {
        console.error('Error al cargar resumen:', error);
        this.cargandoResumen.set(false);
      }
    });
  }

  aplicarFiltroLocal(): void {
    let filtradas = [...this.facturas()];

    // Filtro de búsqueda de cliente (filtro local en memoria)
    if (this.searchCliente) {
      const term = this.searchCliente.toLowerCase();
      filtradas = filtradas.filter(f =>
        f.nombre?.toLowerCase().includes(term) ||
        f.ruc?.toLowerCase().includes(term)
      );
    }

    this.facturasFiltradas.set(filtradas);
    this.actualizarPaginacion();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.actualizarPaginacion();
  }

  private actualizarPaginacion(): void {
    const todas = this.facturasFiltradas();
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    const paginadas = todas.slice(startIndex, endIndex);
    this.facturasPaginas.set(paginadas);
  }

  limpiarFiltros(): void {
    // Preservar empresaId si viene de query params
    const empresaId = this.filtro.empresaId;
    this.filtro = {
      empresaId: empresaId, // Mantener el empresaId si existe
      fechaDesde: undefined,
      fechaHasta: undefined,
      clienteId: undefined,
      estado: undefined,
      montoMinimo: undefined,
      montoMaximo: undefined
    };
    this.searchCliente = '';
    // Resetear paginación al limpiar filtros
    this.pageIndex = 0;
    this.cargarFacturas();
    this.cargarResumen();
  }

  crearFactura(): void {
    const empresaId = this.filtro.empresaId;
    if (empresaId) {
      this.router.navigate(['/facturacion/nueva'], { queryParams: { empresaId } });
    } else {
      this.router.navigate(['/facturacion/nueva']);
    }
  }

  onActionClick(event: { action: string; row: any }): void {
    const factura = event.row as FacturaLegal;

    switch (event.action) {
      case 'Ver estado':
        this.verEstado(factura);
        break;
      case 'Ver / Editar':
      case 'Ver':
      case 'Editar':
        this.editarFactura(factura);
        break;
      case 'Generar DE':
        this.generarDE(factura);
        break;
      case 'Desvincular DE':
        this.desvincularDE(factura);
        break;
      case 'Consultar SIFEN':
        this.consultarSifen(factura);
        break;
      case 'Reenviar DE':
        this.reenviarDE(factura);
        break;
      case 'Reenviar Email':
        this.reenviarEmail(factura);
        break;
      case 'Cancelar DE':
        this.cancelarDE(factura);
        break;
      case 'Nominar DE':
        this.nominarDE(factura);
        break;
      case 'Inutilizar Número':
        this.inutilizarDesdeFactura(factura);
        break;
      case 'Abrir PDF':
        this.abrirPdfKude(factura);
        break;
      case 'Eliminar':
        this.eliminarFactura(factura);
        break;
      case 'Crear Nota de Crédito':
        this.crearNotaCredito(factura);
        break;
      case 'Crear Nota de Débito':
        this.crearNotaDebito(factura);
        break;
    }
  }

  editarFactura(factura: FacturaLegal): void {
    this.router.navigate(['/facturacion', factura.id, 'editar']);
  }

  desvincularDE(factura: FacturaLegal): void {
    if (!factura.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Desvincular Documento Electrónico',
        message: `¿Está seguro de desvincular el documento electrónico de la factura N° ${factura.numeroFactura}? ` +
                 `Esta acción eliminará el DE y permitirá generar uno nuevo.`,
        confirmText: 'Desvincular',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && factura.id) {
        this.facturaApi.desvincularDE(factura.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Documento electrónico desvinculado exitosamente', 'Cerrar', { duration: 4000 });
            this.cargarFacturas();
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al desvincular documento electrónico',
              'Cerrar',
              { duration: 5000 }
            );
          }
        });
      }
    });
  }

  generarDE(factura: FacturaLegal): void {
    if (!factura.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Generar Documento Electrónico',
        message: `¿Desea generar el documento electrónico para la factura N° ${factura.numeroFactura}?`,
        confirmText: 'Generar',
        cancelText: 'Cancelar'
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && factura.id) {
        // Por ahora llamamos al API directamente; se puede migrar a NgRx más adelante.
        this.facturaApi.generarDE(factura.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Documento electrónico enviado a SIFEN', 'Cerrar', { duration: 4000 });
            this.cargarFacturas();
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al generar y enviar el documento electrónico',
              'Cerrar',
              { duration: 5000 }
            );
          }
        });
      }
    });
  }

  verEstado(factura: FacturaLegal): void {
    this.dialog.open(FacturaEstadoDialogComponent, {
      width: '800px',
      maxWidth: '90vw',
      data: { factura },
      disableClose: false
    });
  }

  consultarSifen(factura: FacturaLegal): void {
    if (factura.loteDeId) {
      // Consultar lote
      this.snackBar.open('Consultando estado del lote...', 'Cerrar', { duration: 2000 });
      this.sifenApi.consultarLote(factura.loteDeId).subscribe({
        next: (lote) => {
          this.snackBar.open(`Lote consultado. Estado: ${lote.estado}`, 'Cerrar', { duration: 4000 });
          this.cargarFacturas();
        },
        error: (err) => {
          this.snackBar.open(`Error al consultar lote: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
        }
      });
    } else if (factura.cdcDocumentoElectronico) {
      // Consultar documento individual
      this.snackBar.open('Consultando estado del documento...', 'Cerrar', { duration: 2000 });
      this.sifenApi.consultarDocumento(factura.cdcDocumentoElectronico).subscribe({
        next: (doc) => {
          // Imprimir respuesta completa en consola para debugging
          console.log('🔍 RESPUESTA COMPLETA DE CONSULTA SIFEN:', doc);
          console.log('📋 Estado del documento:', doc.estado);
          console.log('📋 Código respuesta SIFEN:', doc.codigoRespuestaSifen);
          console.log('📋 Mensaje respuesta SIFEN:', doc.mensajeRespuestaSifen);
          console.log('📋 Protocolo autorización:', doc.protocoloAutorizacion);
          console.log('📋 Respuesta SIFEN (XML):', doc.respuestaSifen);

          this.snackBar.open(`Documento consultado. Estado: ${doc.estado}`, 'Cerrar', { duration: 4000 });
          this.cargarFacturas();
        },
        error: (err) => {
          console.error('❌ ERROR al consultar documento:', err);
          this.snackBar.open(`Error al consultar documento: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
        }
      });
    } else {
      this.snackBar.open('Esta factura no tiene DE asociado para consultar', 'Cerrar', { duration: 3000 });
    }
  }

  reenviarDE(factura: FacturaLegal): void {
    if (!factura.documentoElectronicoId) {
      this.snackBar.open('Esta factura no tiene documento electrónico para reenviar', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Reenviar Documento Electrónico',
        message: `¿Desea reenviar el documento electrónico de la factura N° ${factura.numeroFactura} en un nuevo lote?`,
        confirmText: 'Reenviar',
        cancelText: 'Cancelar'
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && factura.documentoElectronicoId) {
        this.snackBar.open('Reenviando documento electrónico...', 'Cerrar', { duration: 2000 });
        this.sifenApi.reenviarDEEnNuevoLote(factura.documentoElectronicoId).pipe(takeUntil(this.destroy$)).subscribe({
          next: (lote) => {
            this.snackBar.open(
              `Documento reenviado exitosamente. Nuevo lote ID: ${lote.id}. Estado: ${lote.estado}`,
              'Cerrar',
              { duration: 5000 }
            );
            this.cargarFacturas();
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al reenviar el documento electrónico',
              'Cerrar',
              { duration: 5000 }
            );
          }
        });
      }
    });
  }

  abrirPdfKude(factura: FacturaLegal): void {
    if (!factura.id) return;

    if (!factura.documentoElectronicoId) {
      this.snackBar.open('Esta factura no tiene documento electrónico asociado', 'Cerrar', { duration: 3000 });
      return;
    }

    this.snackBar.open('Generando PDF...', 'Cerrar', { duration: 2000 });

    this.facturaApi.descargarPdfKude(factura.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (blob: Blob) => {
        // Crear URL del blob y abrir en nueva pestaña del navegador
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');

        // Limpiar la URL después de un tiempo para liberar memoria
        setTimeout(() => {
          window.URL.revokeObjectURL(url);
        }, 100);

        this.snackBar.open('PDF abierto en nueva pestaña', 'Cerrar', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error al generar PDF:', error);
        this.snackBar.open(
          error.error?.message || 'Error al generar el PDF del KUDE',
          'Cerrar',
          { duration: 5000 }
        );
      }
    });
  }

  eliminarFactura(factura: FacturaLegal): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmar eliminación',
        message: `¿Está seguro de eliminar la factura N° ${factura.numeroFactura}?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && factura.id) {
        this.store.dispatch(FacturacionActions.deleteFactura({ id: factura.id }));
      }
    });
  }

  reenviarEmail(factura: FacturaLegal): void {
    if (!factura.id) return;

    if (!factura.documentoElectronicoId) {
      this.snackBar.open('Esta factura no tiene documento electrónico asociado', 'Cerrar', { duration: 3000 });
      return;
    }

    if (factura.estadoDocumentoElectronico !== 'APROBADO') {
      this.snackBar.open('Solo se puede reenviar email para documentos electrónicos en estado APROBADO', 'Cerrar', { duration: 4000 });
      return;
    }

    // Validar que la factura tenga cliente asociado (no sea SIN NOMBRE)
    if (!factura.clienteId || factura.clienteId === null) {
      this.snackBar.open('No se puede enviar email a un cliente SIN NOMBRE. La factura debe tener un cliente nominado.', 'Cerrar', { duration: 5000 });
      return;
    }

    // Validar que el nombre no sea "SIN NOMBRE"
    if (factura.nombre && (factura.nombre.toUpperCase() === 'SIN NOMBRE' || factura.nombre === 'Sin Nombre')) {
      this.snackBar.open('No se puede enviar email a un cliente SIN NOMBRE. La factura debe tener un cliente nominado.', 'Cerrar', { duration: 5000 });
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Reenviar Email',
        message: `¿Desea reenviar el email con la factura electrónica N° ${factura.numeroFactura || 'N/A'} al cliente?`,
        confirmText: 'Reenviar',
        cancelText: 'Cancelar'
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && factura.id) {
        this.facturaApi.reenviarEmail(factura.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Email reenviado exitosamente', 'Cerrar', { duration: 4000 });
          },
          error: (error) => {
            const errorCode = error.error?.error;
            const errorMessage = error.error?.message || error.error?.error || error.message || 'Error al reenviar email';

            // Si el error es CLIENTE_SIN_EMAIL, abrir diálogo de edición del cliente
            if (errorCode === 'CLIENTE_SIN_EMAIL' && error.error?.clienteId && factura.empresaId && factura.id) {
              const clienteId = parseInt(error.error.clienteId, 10);
              this.abrirDialogoEditarClienteParaEmail(factura.empresaId, clienteId, factura.id);
            } else if (errorCode === 'CLIENTE_SIN_NOMBRE') {
              this.snackBar.open('No se puede enviar email a un cliente SIN NOMBRE. La factura debe tener un cliente nominado.', 'Cerrar', { duration: 5000 });
            } else {
              this.snackBar.open(errorMessage, 'Cerrar', { duration: 5000 });
            }
          }
        });
      }
    });
  }

  private abrirDialogoEditarClienteParaEmail(empresaId: number, clienteId: number, facturaId: number): void {
    // Obtener el cliente primero
    this.clienteApi.getById(empresaId, clienteId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (cliente: Cliente) => {
        const dialogRef = this.dialog.open(ClienteFormComponent, {
          width: '700px',
          data: {
            cliente: cliente,
            empresaId: empresaId
          },
          disableClose: true
        });

        dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe((clienteActualizado: Cliente | undefined) => {
          if (clienteActualizado) {
            // Si el cliente fue actualizado y tiene email, reenviar el email automáticamente
            if (clienteActualizado.email && clienteActualizado.email.trim()) {
              this.snackBar.open('Cliente actualizado. Reenviando email...', 'Cerrar', { duration: 2000 });
              // Esperar un momento para que el backend procese la actualización
              setTimeout(() => {
                if (facturaId) {
                  this.facturaApi.reenviarEmail(facturaId).pipe(takeUntil(this.destroy$)).subscribe({
                    next: () => {
                      this.snackBar.open('Email reenviado exitosamente', 'Cerrar', { duration: 4000 });
                    },
                    error: (error) => {
                      const errorMessage = error.error?.error || error.error?.message || error.message || 'Error al reenviar email';
                      this.snackBar.open(errorMessage, 'Cerrar', { duration: 5000 });
                    }
                  });
                }
              }, 500);
            } else {
              this.snackBar.open('El cliente aún no tiene email configurado. Por favor, configure el email del cliente.', 'Cerrar', { duration: 5000 });
            }
          }
        });
      },
      error: (error) => {
        this.snackBar.open('Error al cargar los datos del cliente', 'Cerrar', { duration: 3000 });
      }
    });
  }

  cancelarDE(factura: FacturaLegal): void {
    if (!factura.cdcDocumentoElectronico && !factura.documentoElectronicoId) {
      this.snackBar.open('Esta factura no tiene documento electrónico para cancelar', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(CancelarDeDialogComponent, {
      width: '500px',
      data: {
        factura: factura,
        cdc: factura.cdcDocumentoElectronico
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result && typeof result === 'object' && result.cdc && result.motivo) {
        this.snackBar.open('Cancelando documento electrónico...', 'Cerrar', { duration: 2000 });
        this.sifenApi.cancelarDocumento(result.cdc, result.motivo)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.snackBar.open('Documento electrónico cancelado exitosamente', 'Cerrar', { duration: 4000 });
              this.cargarFacturas();
            },
            error: (error) => {
              // Extraer el mensaje de error del backend
              let errorMessage = 'Error al cancelar documento electrónico';
              
              if (error.error?.message) {
                errorMessage = error.error.message;
              } else if (error.message) {
                errorMessage = error.message;
              }
              
              // Decodificar entidades HTML si existen (ej: &#243; -> ó)
              const tempDiv = document.createElement('div');
              tempDiv.innerHTML = errorMessage;
              errorMessage = tempDiv.textContent || tempDiv.innerText || errorMessage;
              
              this.snackBar.open(errorMessage, 'Cerrar', { duration: 7000 });
            }
          });
      }
    });
  }

  nominarDE(factura: FacturaLegal): void {
    if (!factura.cdcDocumentoElectronico) {
      this.snackBar.open('Esta factura no tiene documento electrónico para nominar', 'Cerrar', { duration: 3000 });
      return;
    }

    if (factura.clienteId) {
      this.snackBar.open('Este documento electrónico ya tiene un cliente nominado', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(NominarDeDialogComponent, {
      width: '600px',
      data: {
        factura: factura,
        cdc: factura.cdcDocumentoElectronico,
        empresaId: factura.empresaId
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result) {
        this.cargarFacturas();
      }
    });
  }

  inutilizarDesdeFactura(factura: FacturaLegal): void {
    if (!factura.timbradoDetalleId || !factura.numeroFactura) {
      this.snackBar.open('Esta factura no tiene la información necesaria para inutilizar', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(InutilizarNumerosDialogComponent, {
      width: '700px',
      data: {
        factura: factura,
        empresaId: factura.empresaId,
        timbradoDetalleId: factura.timbradoDetalleId,
        numeroInicio: factura.numeroFactura,
        numeroFin: factura.numeroFactura
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result) {
        this.cargarFacturas();
      }
    });
  }

  inutilizarDesdeHeader(): void {
    const empresaId = this.filtro.empresaId;
    if (!empresaId) {
      this.snackBar.open('Debe seleccionar una empresa primero', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(InutilizarNumerosDialogComponent, {
      width: '700px',
      data: {
        empresaId: empresaId
      },
      disableClose: false
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result) {
        this.cargarFacturas();
      }
    });
  }

  crearNotaCredito(factura: FacturaLegal): void {
    if (!factura.id || !factura.empresaId) {
      this.snackBar.open('La factura no tiene la información necesaria', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(NotaCreditoFormDialogComponent, {
      width: '900px',
      data: {
        empresaId: factura.empresaId,
        facturaLegalId: factura.id
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result) {
        this.snackBar.open('Nota de crédito creada exitosamente', 'Cerrar', { duration: 3000 });
        this.cargarFacturas();
      }
    });
  }

  crearNotaDebito(factura: FacturaLegal): void {
    if (!factura.id || !factura.empresaId) {
      this.snackBar.open('La factura no tiene la información necesaria', 'Cerrar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(NotaDebitoFormDialogComponent, {
      width: '900px',
      data: {
        empresaId: factura.empresaId,
        facturaLegalId: factura.id
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result) {
        this.snackBar.open('Nota de débito creada exitosamente', 'Cerrar', { duration: 3000 });
        this.cargarFacturas();
      }
    });
  }
}
