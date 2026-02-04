import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export type DateFilterOption = 'este-mes' | 'mes-pasado' | 'personalizado';

export interface DateRange {
  fechaDesde: Date | null;
  fechaHasta: Date | null;
}

@Component({
  selector: 'app-date-filter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="date-filter">
      <div class="filter-options">
        <button 
          *ngFor="let option of options"
          [class.active]="selectedOption === option.value"
          (click)="selectOption(option.value)"
          class="filter-btn">
          {{ option.label }}
        </button>
      </div>
      
      <div class="custom-date-range" *ngIf="selectedOption === 'personalizado'">
        <div class="date-input-group">
          <label>Desde:</label>
          <input 
            type="date" 
            [(ngModel)]="customFechaDesde"
            (change)="onCustomDateChange()"
            class="date-input">
        </div>
        <div class="date-input-group">
          <label>Hasta:</label>
          <input 
            type="date" 
            [(ngModel)]="customFechaHasta"
            (change)="onCustomDateChange()"
            class="date-input">
        </div>
      </div>
    </div>
  `,
  styles: [`
    .date-filter {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .filter-options {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .filter-btn {
      padding: 0.5rem 1rem;
      border: 1px solid #ddd;
      background: white;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
      font-size: 0.875rem;
      color: #2c3e50;
    }

    .filter-btn:hover {
      background: #f8f9fa;
      border-color: #3498db;
    }

    .filter-btn.active {
      background: #3498db;
      color: white;
      border-color: #3498db;
    }

    .custom-date-range {
      display: flex;
      gap: 1rem;
      align-items: center;
      flex-wrap: wrap;
    }

    .date-input-group {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .date-input-group label {
      font-size: 0.875rem;
      color: #2c3e50;
      font-weight: 500;
    }

    .date-input {
      padding: 0.5rem;
      border: 1px solid #ddd;
      border-radius: 6px;
      font-size: 0.875rem;
    }

    .date-input:focus {
      outline: none;
      border-color: #3498db;
    }

    @media (max-width: 768px) {
      .filter-options {
        flex-direction: column;
      }

      .filter-btn {
        width: 100%;
      }

      .custom-date-range {
        flex-direction: column;
        align-items: stretch;
      }

      .date-input-group {
        flex-direction: column;
        align-items: stretch;
      }
    }
  `]
})
export class DateFilterComponent {
  @Input() selectedOption: DateFilterOption = 'este-mes';
  @Output() dateRangeChange = new EventEmitter<DateRange>();

  options = [
    { value: 'este-mes' as DateFilterOption, label: 'Este mes' },
    { value: 'mes-pasado' as DateFilterOption, label: 'Mes pasado' },
    { value: 'personalizado' as DateFilterOption, label: 'Fecha personalizada' }
  ];

  customFechaDesde: string = '';
  customFechaHasta: string = '';

  ngOnInit(): void {
    // Inicializar fechas personalizadas si no están definidas
    if (this.selectedOption === 'personalizado' && !this.customFechaDesde && !this.customFechaHasta) {
      const now = new Date();
      this.customFechaDesde = this.formatDateInput(now);
      this.customFechaHasta = this.formatDateInput(now);
    }
    this.updateDateRange();
  }

  private formatDateInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  selectOption(option: DateFilterOption): void {
    this.selectedOption = option;
    this.updateDateRange();
  }

  onCustomDateChange(): void {
    if (this.selectedOption === 'personalizado') {
      this.updateDateRange();
    }
  }

  private updateDateRange(): void {
    let fechaDesde: Date | null = null;
    let fechaHasta: Date | null = null;

    const now = new Date();
    
    switch (this.selectedOption) {
      case 'este-mes':
        fechaDesde = new Date(now.getFullYear(), now.getMonth(), 1);
        fechaHasta = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59);
        break;
      
      case 'mes-pasado':
        fechaDesde = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        fechaHasta = new Date(now.getFullYear(), now.getMonth(), 0, 23, 59, 59);
        break;
      
      case 'personalizado':
        if (this.customFechaDesde) {
          fechaDesde = new Date(this.customFechaDesde);
          fechaDesde.setHours(0, 0, 0, 0);
        }
        if (this.customFechaHasta) {
          fechaHasta = new Date(this.customFechaHasta);
          fechaHasta.setHours(23, 59, 59, 999);
        }
        break;
    }

    this.dateRangeChange.emit({ fechaDesde, fechaHasta });
  }
}

