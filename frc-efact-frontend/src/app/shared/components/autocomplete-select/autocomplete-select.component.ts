import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatOptionModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { Observable, Subject, startWith, map, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

export interface AutocompleteOption {
  value: string;
  label: string;
  codigo?: string;
}

@Component({
  selector: 'app-autocomplete-select',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatOptionModule,
    MatIconModule
  ],
  template: `
    <mat-form-field appearance="outline" class="full-width">
      <mat-label>{{ label }}</mat-label>
      <input
        matInput
        [formControl]="searchControl"
        [matAutocomplete]="auto"
        [placeholder]="placeholder"
        autocomplete="off"
        (input)="onInput($event)"
        (blur)="onBlur()">
      <mat-icon matSuffix>search</mat-icon>

      <mat-autocomplete
        #auto="matAutocomplete"
        [displayWith]="displayFn"
        (optionSelected)="onOptionSelected($event)">
        <mat-option
          *ngFor="let option of filteredOptions$ | async"
          [value]="option">
          <div class="option-content">
            <span class="option-label">{{ option.label }}</span>
            <span class="option-code" *ngIf="option.codigo">({{ option.codigo }})</span>
          </div>
        </mat-option>

        <mat-option *ngIf="(filteredOptions$ | async)?.length === 0" disabled>
          <div class="no-options">
            <mat-icon>search_off</mat-icon>
            <span>No se encontraron resultados</span>
          </div>
        </mat-option>
      </mat-autocomplete>

      <mat-error *ngIf="hasError">
        {{ errorMessage }}
      </mat-error>
    </mat-form-field>
  `,
  styles: [`
    .full-width {
      width: 100%;
    }

    .option-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .option-label {
      flex: 1;
      text-align: left;
    }

    .option-code {
      color: #666;
      font-size: 0.875rem;
      margin-left: 8px;
    }

    .no-options {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #999;
      font-style: italic;
    }

    .no-options mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    /* Disabled state styles */
    mat-form-field.mat-form-field-disabled {
      pointer-events: none;
    }

    mat-form-field.mat-form-field-disabled input {
      color: rgba(0, 0, 0, 0.38);
    }

    mat-form-field.mat-form-field-disabled mat-icon {
      color: rgba(0, 0, 0, 0.38);
    }
  `]
})
export class AutocompleteSelectComponent implements OnInit, OnDestroy, OnChanges {
  @Input() label: string = '';
  @Input() placeholder: string = '';
  @Input() options: AutocompleteOption[] = [];
  @Input() value: string | null = null;
  @Input() hasError: boolean = false;
  @Input() errorMessage: string = '';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() showAllOnEmpty: boolean = true;

  @Output() valueChange = new EventEmitter<string>();
  @Output() optionSelected = new EventEmitter<AutocompleteOption>();
  @Output() searchChange = new EventEmitter<string>();

  searchControl = new FormControl('');
  filteredOptions$!: Observable<AutocompleteOption[]>;
  private destroy$ = new Subject<void>();
  private isUpdatingFromExternal = false;

  ngOnInit(): void {
    this.updateValue();
    this.setupFiltering();

    // Set initial disabled state
    if (this.disabled) {
      this.searchControl.disable();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Solo actualizar el valor cuando cambia el input 'value', no cuando cambian las opciones
    // Esto evita que se limpie el campo cuando se actualizan las opciones después de una búsqueda
    if (changes['value']) {
      this.updateValue();
    }

    // Si solo cambian las opciones, no hacer nada para mantener el texto del usuario
    // Las opciones se usarán para filtrar, pero no afectarán el valor del input

    if (changes['disabled']) {
      if (this.disabled) {
        this.searchControl.disable();
      } else {
        this.searchControl.enable();
      }
    }
  }

  private updateValue(): void {
    const value = this.value;
    const currentValue = this.searchControl.value;

    // Si el valor externo es null o vacío, NO limpiar automáticamente
    // Esto previene que se limpie el campo cuando el usuario está escribiendo o después de una búsqueda
    if (!value || (typeof value === 'string' && value.trim() === '')) {
      // No limpiar automáticamente - dejar que el usuario controle el campo
      // Solo se limpiará cuando el usuario explícitamente borre el contenido
      return;
    }

    // Si hay un valor externo establecido
    if (typeof value === 'string' && value.trim() !== '') {
      // Verificar si el valor actual ya es el mismo que el que queremos establecer
      if (typeof currentValue === 'string') {
        // Comparar tanto el valor exacto como si el valor actual contiene el valor que queremos establecer
        // Esto evita actualizaciones innecesarias cuando el texto ya está correcto
        if (currentValue === value || currentValue.trim() === value.trim()) {
          // Ya tiene el mismo valor, no hacer nada para evitar disparar eventos
          return;
        }
      }

      const valueToSearch = value.toLowerCase();
      const option = this.options.find(opt => {
        if (!opt) return false;
        const optLabel = opt.label || '';
        return opt.value === value ||
          opt.codigo === value ||
          (optLabel && optLabel.toLowerCase().includes(valueToSearch)) ||
          (optLabel && value.toLowerCase().includes(optLabel.toLowerCase()));
      });

      // Marcar que estamos actualizando desde un valor externo
      this.isUpdatingFromExternal = true;

      if (option) {
        // Si encontramos una opción, usar el label completo (puede incluir RUC)
        const displayValue = option.codigo ? `${option.label} - ${option.codigo}` : option.label;
        // Solo actualizar si el valor es diferente
        if (currentValue !== displayValue) {
          this.searchControl.setValue(displayValue, { emitEvent: false });
        }
      } else {
        // Si no encuentra una opción que coincida, establecer el valor directamente
        // Esto permite mantener el texto en el input (por ejemplo, cuando se establece el nombre del cliente seleccionado)
        if (currentValue !== value) {
          this.searchControl.setValue(value, { emitEvent: false });
        }
      }

      // Resetear la bandera en el siguiente ciclo del event loop
      Promise.resolve().then(() => {
        this.isUpdatingFromExternal = false;
      });
    }
  }

  private setupFiltering(): void {
    // Setup filtering
    this.filteredOptions$ = this.searchControl.valueChanges.pipe(
      startWith(''),
      debounceTime(1000),
      distinctUntilChanged(),
      map(value => {
        const searchTerm = typeof value === 'string' ? value : '';
        // No emitir searchChange si estamos actualizando desde un valor externo
        if (!this.isUpdatingFromExternal) {
          this.searchChange.emit(searchTerm);
        }
        return this.filterOptions(searchTerm);
      }),
      takeUntil(this.destroy$)
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private filterOptions(value: string): AutocompleteOption[] {
    if (!value || typeof value !== 'string') {
      return this.showAllOnEmpty ? this.options.slice(0, 50) : [];
    }

    const filterValue = value.toLowerCase().trim();

    // Si no hay texto de búsqueda, mostrar todas las opciones
    if (filterValue === '') {
      return this.showAllOnEmpty ? this.options.slice(0, 50) : [];
    }

    return this.options
      .filter(option => {
        const label = option.label.toLowerCase();
        const codigo = option.codigo?.toLowerCase() || '';
        const value = option.value.toLowerCase();

        // Buscar en label, código y value
        return label.includes(filterValue) ||
          codigo.includes(filterValue) ||
          value.includes(filterValue);
      })
      .slice(0, 50); // Limit to 50 options for performance
  }

  displayFn = (option: AutocompleteOption | string): string => {
    if (typeof option === 'string') {
      return option;
    }
    return option ? option.label : '';
  };

  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.toUpperCase(); // Convert to uppercase

    // Update only the input element, not the form control
    // This prevents the valueChanges observable from being triggered twice
    input.value = value;
  }

  onBlur(): void {
    // Si el valor actual es un objeto (opción seleccionada), no hacer nada
    const currentValue = this.searchControl.value;
    if (typeof currentValue === 'object' && currentValue !== null) {
      return;
    }

    // Si hay un valor externo establecido (value input), NUNCA limpiar el campo
    // Esto permite mantener el texto cuando se establece el nombre del cliente seleccionado
    if (this.value && typeof this.value === 'string' && this.value.trim() !== '') {
      // Asegurarse de que el valor del input coincida con el valor externo
      if (typeof currentValue === 'string' && currentValue.trim() !== this.value.trim()) {
        // Si el valor del input no coincide con el valor externo, actualizarlo
        this.isUpdatingFromExternal = true;
        this.searchControl.setValue(this.value, { emitEvent: false });
        Promise.resolve().then(() => {
          this.isUpdatingFromExternal = false;
        });
      }
      return;
    }

    // Solo verificar coincidencias si NO hay un valor externo establecido
    // Si es un string, verificar si coincide con alguna opción
    if (typeof currentValue === 'string' && currentValue.trim() !== '') {
      const matchingOption = this.options.find(opt => {
        const optLabel = opt.label.toLowerCase().trim();
        const optDisplay = opt.codigo ? `${opt.label} - ${opt.codigo}`.toLowerCase().trim() : optLabel;
        const currentLower = currentValue.toLowerCase().trim();
        return optLabel === currentLower || optDisplay === currentLower || currentLower.includes(optLabel);
      });

      // Si no hay coincidencia, solo limpiar si no hay valor externo
      // Pero solo si el usuario realmente escribió algo que no coincide
      if (!matchingOption && (!this.value || this.value.trim() === '')) {
        // No limpiar automáticamente - dejar que el usuario decida
        // this.searchControl.setValue('');
        // this.valueChange.emit('');
      }
    }
  }

  onOptionSelected(event: any): void {
    const selectedOption: AutocompleteOption = event.option.value;
    this.valueChange.emit(selectedOption.value);
    this.optionSelected.emit(selectedOption);
  }
}
