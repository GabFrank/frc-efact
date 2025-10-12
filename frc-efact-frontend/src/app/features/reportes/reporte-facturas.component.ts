import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReporteApiService } from '../../core/api/reporte-api.service';
import { FacturaReporte, FacturaFiltro } from '../../models/reporte.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message.component';

@Component({
  selector: 'app-reporte-facturas',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent
  ],
  template: `
    <div class="reporte-facturas-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Reporte de Facturas</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <!-- Filtros -->
          <form [formGroup]="filtroForm" class="filtros-form">
            <div class="filtros-row">
              <mat-form-field appearance="outline">
                <mat-label>Empresa ID</mat-label>
                <input matInput type="number" formControlName="empresaId" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha Desde</mat-label>
                <input matInput [matDatepicker]="pickerDesde" formControlName="fechaDesde" />
                <mat-datepicker-toggle matSuffix [for]="pickerDesde"></mat-datepicker-toggle>
                <mat-datepicker #pickerDesde></mat-datepicker>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Fecha Hasta</mat-label>
                <input matInput [matDatepicker]="pickerHasta" formControlName="fechaHasta" />
                <mat-datepicker-toggle matSuffix [for]="pickerHasta"></mat-datepicker-toggle>
                <mat-datepicker #pickerHasta></mat-datepicker>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Cliente ID</mat-label>
                <input matInput type="number" formControlName="clienteId" />
              </mat-form-field>
            </div>

            <div class="filtros-row">
              <mat-form-field appearance="outline">
                <mat-label>Estado</mat-label>
                <mat-select formControlName="estado">
                  <mat-option value="">Todos</mat-option>
                  <mat-option value="ACTIVO">Activo</mat-option>
                  <mat-option value="CANCELADO">Cancelado</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Monto Mínimo</mat-label>
                <input matInput type="number" formControlName="montoMinimo" />
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Monto Máximo</mat-label>
                <input matInput type="number" formControlName="montoMaximo" />
              </mat-form-field>

              <div class="filtros-actions">
                <button mat-raised-button color="primary" (click)="aplicarFiltros()">
                  <mat-icon>search</mat-icon>
                  Buscar
                </button>
                <button mat-button (click)="limpiarFiltros()">
                  <mat-icon>clear</mat-icon>
                  Limpiar
                </button>
              </div>
            </div>
          </form>

          <!-- Loading -->
          <app-loading-spinner *ngIf="loading"></app-loading-spinner>

          <!-- Error -->
          <app-error-message *ngIf="error" [message]="error"></app-error-message>

          <!-- Resultados -->
          <div *ngIf="!loading && !error && facturas.length > 0" class="resultados">
            <div class="export-buttons">
              <button mat-raised-button color="accent" (click)="exportarExcel()" [disabled]="exporting">
                <mat-icon>file_download</mat-icon>
                Exportar Excel
              </button>
              <button mat-raised-button color="accent" (click)="exportarPDF()" [disabled]="exporting">
                <mat-icon>picture_as_pdf</mat-icon>
                Exportar PDF
              </button>
            </div>

            <table mat-table [dataSource]="paginatedFacturas" class="facturas-table">
              <ng-container matColumnDef="id">
                <th mat-header-cell *matHeaderCellDef>ID</th>
                <td mat-cell *matCellDef="let factura">{{ factura.id }}</td>
              </ng-container>

              <ng-container matColumnDef="numeroFactura">
                <th mat-header-cell *matHeaderCellDef>Número</th>
                <td mat-cell *matCellDef="let factura">{{ factura.numeroFactura }}</td>
              </ng-container>

              <ng-container matColumnDef="fecha">
                <th mat-header-cell *matHeaderCellDef>Fecha</th>
                <td mat-cell *matCellDef="let factura">{{ factura.fecha | date: 'dd/MM/yyyy' }}</td>
              </ng-container>

              <ng-container matColumnDef="clienteNombre">
                <th mat-header-cell *matHeaderCellDef>Cliente</th>
                <td mat-cell *matCellDef="let factura">{{ factura.clienteNombre }}</td>
              </ng-container>

              <ng-container matColumnDef="clienteRuc">
                <th mat-header-cell *matHeaderCellDef>RUC</th>
                <td mat-cell *matCellDef="let factura">{{ factura.clienteRuc || '-' }}</td>
              </ng-container>

              <ng-container matColumnDef="totalFinal">
                <th mat-header-cell *matHeaderCellDef>Total</th>
                <td mat-cell *matCellDef="let factura">{{ factura.totalFinal | currency: 'PYG' }}</td>
              </ng-container>

              <ng-container matColumnDef="estado">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let factura">{{ factura.estado }}</td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>

            <mat-paginator
              [length]="facturas.length"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 25, 50, 100]"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
          </div>

          <!-- Sin resultados -->
          <div *ngIf="!loading && !error && facturas.length === 0" class="sin-resultados">
            <p>No se encontraron facturas con los filtros aplicados.</p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .reporte-facturas-container {
      padding: 20px;
    }

    mat-card {
      margin-bottom: 20px;
    }

    .filtros-form {
      margin-bottom: 20px;
    }

    .filtros-row {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
      flex-wrap: wrap;
      align-items: center;
    }

    mat-form-field {
      flex: 1;
      min-width: 200px;
    }

    .filtros-actions {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .export-buttons {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;
      justify-content: flex-end;
    }

    .facturas-table {
      width: 100%;
      margin-top: 16px;
    }

    .sin-resultados {
      text-align: center;
      padding: 40px;
      color: #666;
    }

    .resultados {
      margin-top: 20px;
    }
  `]
})
export class ReporteFacturasComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reporteService = inject(ReporteApiService);

  filtroForm!: FormGroup;
  facturas: FacturaReporte[] = [];
  paginatedFacturas: FacturaReporte[] = [];
  loading = false;
  error: string | null = null;
  exporting = false;

  displayedColumns = ['id', 'numeroFactura', 'fecha', 'clienteNombre', 'clienteRuc', 'totalFinal', 'estado'];
  pageSize = 10;
  pageIndex = 0;

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.filtroForm = this.fb.group({
      empresaId: [null],
      fechaDesde: [null],
      fechaHasta: [null],
      clienteId: [null],
      estado: [''],
      montoMinimo: [null],
      montoMaximo: [null]
    });
  }

  aplicarFiltros(): void {
    this.loading = true;
    this.error = null;

    const filtro: FacturaFiltro = {
      ...this.filtroForm.value,
      fechaDesde: this.filtroForm.value.fechaDesde 
        ? this.formatDate(this.filtroForm.value.fechaDesde) 
        : undefined,
      fechaHasta: this.filtroForm.value.fechaHasta 
        ? this.formatDate(this.filtroForm.value.fechaHasta) 
        : undefined
    };

    this.reporteService.reporteFacturas(filtro).subscribe({
      next: (data) => {
        this.facturas = data;
        this.pageIndex = 0;
        this.updatePaginatedData();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar el reporte de facturas';
        this.loading = false;
        console.error(err);
      }
    });
  }

  limpiarFiltros(): void {
    this.filtroForm.reset({
      estado: ''
    });
    this.facturas = [];
    this.paginatedFacturas = [];
  }

  onPageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
    this.updatePaginatedData();
  }

  private updatePaginatedData(): void {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedFacturas = this.facturas.slice(startIndex, endIndex);
  }

  exportarExcel(): void {
    this.exporting = true;
    const filtro = this.buildFiltroForExport();

    this.reporteService.exportarExcel('facturas', filtro).subscribe({
      next: (blob) => {
        this.downloadFile(blob, 'reporte-facturas.xlsx');
        this.exporting = false;
      },
      error: (err) => {
        this.error = 'Error al exportar a Excel';
        this.exporting = false;
        console.error(err);
      }
    });
  }

  exportarPDF(): void {
    this.exporting = true;
    const filtro = this.buildFiltroForExport();

    this.reporteService.exportarPDF('facturas', filtro).subscribe({
      next: (blob) => {
        this.downloadFile(blob, 'reporte-facturas.pdf');
        this.exporting = false;
      },
      error: (err) => {
        this.error = 'Error al exportar a PDF';
        this.exporting = false;
        console.error(err);
      }
    });
  }

  private buildFiltroForExport(): any {
    return {
      ...this.filtroForm.value,
      fechaDesde: this.filtroForm.value.fechaDesde 
        ? this.formatDate(this.filtroForm.value.fechaDesde) 
        : undefined,
      fechaHasta: this.filtroForm.value.fechaHasta 
        ? this.formatDate(this.filtroForm.value.fechaHasta) 
        : undefined
    };
  }

  private downloadFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
