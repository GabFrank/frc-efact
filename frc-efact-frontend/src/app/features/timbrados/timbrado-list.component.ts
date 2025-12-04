import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, combineLatest, map } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';

import { Timbrado } from '../../models/timbrado.model';
import { Empresa } from '../../models/empresa.model';
import { TimbradoApiService } from '../../core/api/timbrado-api.service';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';

interface TimbradoWithStatus extends Timbrado {
  vigente: boolean;
  diasParaVencer: number;
  alertaVencimiento: boolean;
}

@Component({
  selector: 'app-timbrado-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCardModule,
    MatTooltipModule,
    MatChipsModule,
    MatSelectModule,
    MatDialogModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="timbrados-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <h2>Gestión de Timbrados</h2>
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Search and Actions Bar -->
          <div class="actions-bar">
            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Filtrar por empresa</mat-label>
              <mat-select [(ngModel)]="selectedEmpresaId" (ngModelChange)="onFilterChange()">
                <mat-option [value]="null">Todas las empresas</mat-option>
                <mat-option *ngFor="let empresa of empresas" [value]="empresa.id">
                  {{ empresa.razonSocial }}
                </mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Buscar timbrado</mat-label>
              <input
                matInput
                [(ngModel)]="searchTerm"
                (ngModelChange)="onSearchChange()"
                placeholder="Buscar por número o razón social">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <button
              mat-raised-button
              color="primary"
              (click)="onCreateTimbrado()">
              <mat-icon>add</mat-icon>
              Nuevo Timbrado
            </button>
          </div>

          <!-- Alerts Section -->
          <div class="alerts-section" *ngIf="timbradosConAlerta.length > 0">
            <mat-card class="alert-card">
              <mat-card-content>
                <div class="alert-header">
                  <mat-icon color="warn">warning</mat-icon>
                  <h3>Timbrados próximos a vencer</h3>
                </div>
                <div class="alert-list">
                  <div *ngFor="let timbrado of timbradosConAlerta" class="alert-item">
                    <span class="alert-text">
                      <strong>{{ timbrado.numero }}</strong> - {{ timbrado.razonSocial || 'Sin empresa' }}
                      <span class="dias-restantes">({{ timbrado.diasParaVencer }} días restantes)</span>
                    </span>
                    <button
                      mat-button
                      color="primary"
                      (click)="onEditTimbrado(timbrado)">
                      Renovar
                    </button>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>

          <!-- Loading State -->
          <app-loading-spinner *ngIf="loading"></app-loading-spinner>

          <!-- Error State -->
          <app-error-message
            *ngIf="error"
            [message]="error">
          </app-error-message>

          <!-- Timbrados Table -->
          <div class="table-container" *ngIf="!loading && !error">
            <table mat-table [dataSource]="filteredTimbrados" class="timbrados-table">

              <!-- Número Column -->
              <ng-container matColumnDef="numero">
                <th mat-header-cell *matHeaderCellDef>Número</th>
                <td mat-cell *matCellDef="let timbrado">{{ timbrado.numero }}</td>
              </ng-container>

              <!-- Razón Social Column -->
              <ng-container matColumnDef="razonSocial">
                <th mat-header-cell *matHeaderCellDef>Razón Social</th>
                <td mat-cell *matCellDef="let timbrado">{{ timbrado.razonSocial || 'Sin empresa' }}</td>
              </ng-container>

              <!-- RUC Column -->
              <ng-container matColumnDef="ruc">
                <th mat-header-cell *matHeaderCellDef>RUC</th>
                <td mat-cell *matCellDef="let timbrado">{{ timbrado.ruc || 'Sin RUC' }}</td>
              </ng-container>

              <!-- Tipo Column -->
              <ng-container matColumnDef="tipo">
                <th mat-header-cell *matHeaderCellDef>Tipo</th>
                <td mat-cell *matCellDef="let timbrado">
                  <mat-chip [class.electronico-chip]="timbrado.isElectronico" [class.fisico-chip]="!timbrado.isElectronico">
                    {{ timbrado.isElectronico ? 'Electrónico' : 'Físico' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Vigencia Column -->
              <ng-container matColumnDef="vigencia">
                <th mat-header-cell *matHeaderCellDef>Vigencia</th>
                <td mat-cell *matCellDef="let timbrado">
                  <div class="vigencia-info">
                    <div>{{ timbrado.fechaInicio | date:'dd/MM/yyyy' }} - {{ timbrado.fechaFin | date:'dd/MM/yyyy' }}</div>
                    <mat-chip
                      [class.vigente-chip]="timbrado.vigente && !timbrado.alertaVencimiento"
                      [class.alerta-chip]="timbrado.vigente && timbrado.alertaVencimiento"
                      [class.vencido-chip]="!timbrado.vigente">
                      <mat-icon *ngIf="timbrado.alertaVencimiento && timbrado.vigente">warning</mat-icon>
                      {{ timbrado.vigente ? (timbrado.alertaVencimiento ? 'Por vencer' : 'Vigente') : 'Vencido' }}
                    </mat-chip>
                  </div>
                </td>
              </ng-container>

              <!-- Estado Column -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let timbrado">
                  <mat-chip [class.active-chip]="timbrado.activo" [class.inactive-chip]="!timbrado.activo">
                    {{ timbrado.activo ? 'Activo' : 'Inactivo' }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Acciones</th>
                <td mat-cell *matCellDef="let timbrado">
                  <button
                    mat-icon-button
                    color="primary"
                    (click)="onViewTimbrado(timbrado)"
                    matTooltip="Ver detalles">
                    <mat-icon>visibility</mat-icon>
                  </button>
                  <button
                    mat-icon-button
                    color="accent"
                    (click)="onEditTimbrado(timbrado)"
                    matTooltip="Editar">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button
                    mat-icon-button
                    color="primary"
                    (click)="onManageDetalles(timbrado)"
                    matTooltip="Puntos de expedición">
                    <mat-icon>store</mat-icon>
                  </button>
                  <button
                    mat-icon-button
                    [color]="timbrado.activo ? 'warn' : 'primary'"
                    (click)="onToggleActive(timbrado)"
                    [matTooltip]="timbrado.activo ? 'Desactivar' : 'Activar'">
                    <mat-icon>{{ timbrado.activo ? 'block' : 'check_circle' }}</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

              <!-- No Data Row -->
              <tr class="mat-row" *matNoDataRow>
                <td class="mat-cell no-data" [attr.colspan]="displayedColumns.length">
                  <div class="no-data-message">
                    <mat-icon>receipt</mat-icon>
                    <p>No se encontraron timbrados</p>
                    <button mat-raised-button color="primary" (click)="onCreateTimbrado()">
                      <mat-icon>add</mat-icon>
                      Crear primer timbrado
                    </button>
                  </div>
                </td>
              </tr>
            </table>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .timbrados-container {
      padding: 20px;
      max-width: 1600px;
      margin: 0 auto;
    }

    mat-card {
      margin-bottom: 20px;
    }

    mat-card-header {
      margin-bottom: 20px;
    }

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }

    .actions-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      gap: 16px;
    }

    .filter-field {
      width: 250px;
    }

    .search-field {
      flex: 1;
      max-width: 400px;
    }

    .alerts-section {
      margin-bottom: 20px;
    }

    .alert-card {
      background-color: #fff3e0;
      border-left: 4px solid #ff9800;
    }

    .alert-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
    }

    .alert-header h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }

    .alert-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .alert-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px;
      background-color: white;
      border-radius: 4px;
    }

    .alert-text {
      font-size: 14px;
    }

    .dias-restantes {
      color: #ff9800;
      font-weight: 500;
    }

    .table-container {
      overflow-x: auto;
    }

    .timbrados-table {
      width: 100%;
      background: white;
    }

    .timbrados-table th {
      font-weight: 600;
      background-color: #f5f5f5;
    }

    .timbrados-table td,
    .timbrados-table th {
      padding: 12px 16px;
    }

    .vigencia-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    mat-chip {
      font-size: 12px;
      min-height: 24px;
      padding: 4px 12px;
    }

    mat-chip mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      margin-right: 4px;
    }

    .electronico-chip {
      background-color: #2196f3 !important;
      color: white;
    }

    .fisico-chip {
      background-color: #9e9e9e !important;
      color: white;
    }

    .vigente-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    .alerta-chip {
      background-color: #ff9800 !important;
      color: white;
    }

    .vencido-chip {
      background-color: #f44336 !important;
      color: white;
    }

    .active-chip {
      background-color: #4caf50 !important;
      color: white;
    }

    .inactive-chip {
      background-color: #f44336 !important;
      color: white;
    }

    .no-data {
      text-align: center;
      padding: 40px !important;
    }

    .no-data-message {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      color: #666;
    }

    .no-data-message > mat-icon:first-child {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }

    .no-data-message button mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .no-data-message p {
      margin: 0;
      font-size: 16px;
    }

    button mat-icon {
      margin-right: 4px;
    }

    @media (max-width: 768px) {
      .actions-bar {
        flex-direction: column;
        align-items: stretch;
      }

      .filter-field,
      .search-field {
        width: 100%;
        max-width: 100%;
      }
    }
  `]
})
export class TimbradoListComponent implements OnInit {
  timbrados: TimbradoWithStatus[] = [];
  empresas: Empresa[] = [];
  filteredTimbrados: TimbradoWithStatus[] = [];
  timbradosConAlerta: TimbradoWithStatus[] = [];

  loading = false;
  error: string | null = null;

  displayedColumns: string[] = ['numero', 'razonSocial', 'ruc', 'tipo', 'vigencia', 'activo', 'actions'];
  searchTerm: string = '';
  selectedEmpresaId: number | null = null;

  constructor(
    private timbradoApiService: TimbradoApiService,
    private empresaApiService: EmpresaApiService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    // Leer empresaId de query params si existe
    this.route.queryParams.subscribe(params => {
      if (params['empresaId']) {
        this.selectedEmpresaId = +params['empresaId'];
      }
      this.loadData();
    });
  }

  private loadData(): void {
    this.loading = true;
    this.error = null;

    // Si hay empresaId seleccionado, cargar solo timbrados de esa empresa
    const timbradosObservable = this.selectedEmpresaId 
      ? this.timbradoApiService.getByEmpresa(this.selectedEmpresaId)
      : this.timbradoApiService.getAll();

    combineLatest([
      timbradosObservable,
      this.empresaApiService.getAll()
    ]).subscribe({
      next: ([timbrados, empresas]) => {
        this.empresas = empresas;
        this.timbrados = timbrados.map(t => this.enrichTimbradoWithStatus(t));
        this.updateFilteredTimbrados();
        this.updateAlertTimbrados();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar los timbrados';
        this.loading = false;
        console.error('Error loading timbrados:', err);
      }
    });
  }

  private enrichTimbradoWithStatus(timbrado: Timbrado): TimbradoWithStatus {
    const today = new Date();
    const fechaFin = new Date(timbrado.fechaFin);
    const diasParaVencer = Math.ceil((fechaFin.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    const vigente = diasParaVencer >= 0;
    const alertaVencimiento = vigente && diasParaVencer <= 15;

    return {
      ...timbrado,
      vigente,
      diasParaVencer,
      alertaVencimiento
    };
  }

  private updateFilteredTimbrados(): void {
    let filtered = [...this.timbrados];

    // Filter by empresa
    if (this.selectedEmpresaId) {
      filtered = filtered.filter(t => t.empresaId === this.selectedEmpresaId);
    }

    // Filter by search term
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      const term = this.searchTerm.toLowerCase().trim();
      filtered = filtered.filter(t =>
        t.numero.toLowerCase().includes(term) ||
        (t.razonSocial && t.razonSocial.toLowerCase().includes(term)) ||
        (t.ruc && t.ruc.toLowerCase().includes(term))
      );
    }

    this.filteredTimbrados = filtered;
  }

  private updateAlertTimbrados(): void {
    this.timbradosConAlerta = this.timbrados
      .filter(t => t.alertaVencimiento && t.activo)
      .sort((a, b) => a.diasParaVencer - b.diasParaVencer);
  }

  onSearchChange(): void {
    this.updateFilteredTimbrados();
  }

  onFilterChange(): void {
    // Si cambia el filtro de empresa, recargar datos
    this.loadData();
  }

  onCreateTimbrado(): void {
    this.router.navigate(['/timbrados/new']);
  }

  onViewTimbrado(timbrado: Timbrado): void {
    this.router.navigate(['/timbrados', timbrado.id]);
  }

  onEditTimbrado(timbrado: Timbrado): void {
    this.router.navigate(['/timbrados', timbrado.id, 'edit']);
  }

  onManageDetalles(timbrado: Timbrado): void {
    this.router.navigate(['/timbrados', timbrado.id, 'detalles']);
  }

  onToggleActive(timbrado: Timbrado): void {
    const action = timbrado.activo ? 'desactivar' : 'activar';
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `${action.charAt(0).toUpperCase() + action.slice(1)} Timbrado`,
        message: `¿Está seguro que desea ${action} el timbrado "${timbrado.numero}"?`,
        confirmText: action.charAt(0).toUpperCase() + action.slice(1),
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.timbradoApiService.update(timbrado.id, { activo: !timbrado.activo })
          .subscribe({
            next: () => {
              this.loadData();
            },
            error: (err) => {
              this.error = 'Error al actualizar el timbrado';
              console.error('Error updating timbrado:', err);
            }
          });
      }
    });
  }
}
