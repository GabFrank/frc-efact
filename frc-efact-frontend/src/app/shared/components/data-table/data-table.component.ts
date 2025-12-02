import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatMenuModule } from '@angular/material/menu';
import { SelectionModel } from '@angular/cdk/collections';

export interface TableColumn {
    key: string;
    label: string;
    sortable?: boolean;
    format?: (value: any, row?: any) => string;
}

export interface TableAction {
    icon: string;
    label?: string;
    tooltip?: string;
    color?: 'primary' | 'accent' | 'warn';
    handler?: (row: any) => void;
    visible?: (row: any) => boolean;
}

@Component({
    selector: 'app-data-table',
    standalone: true,
    imports: [
        CommonModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        MatCheckboxModule,
        MatMenuModule
    ],
    template: `
    <div class="table-container">
      <table mat-table [dataSource]="data" matSort (matSortChange)="onSortChange($event)">
        <!-- Checkbox column -->
        <ng-container matColumnDef="select" *ngIf="selectable">
          <th mat-header-cell *matHeaderCellDef>
            <mat-checkbox
              (change)="$event ? toggleAllRows() : null"
              [checked]="selection.hasValue() && isAllSelected()"
              [indeterminate]="selection.hasValue() && !isAllSelected()">
            </mat-checkbox>
          </th>
          <td mat-cell *matCellDef="let row">
            <mat-checkbox
              (click)="$event.stopPropagation()"
              (change)="$event ? toggleRow(row) : null"
              [checked]="selection.isSelected(row.id)">
            </mat-checkbox>
          </td>
        </ng-container>

        <!-- Data columns -->
        <ng-container *ngFor="let column of columns" [matColumnDef]="column.key">
          <th mat-header-cell *matHeaderCellDef [mat-sort-header]="column.sortable ? column.key : ''">
            {{ column.label }}
          </th>
          <td mat-cell *matCellDef="let row" [innerHTML]="column.format ? column.format(row[column.key], row) : row[column.key]">
          </td>
        </ng-container>

        <!-- Actions column -->
        <ng-container matColumnDef="actions" *ngIf="actions && actions.length > 0">
          <th mat-header-cell *matHeaderCellDef>Acciones</th>
          <td mat-cell *matCellDef="let row">
            <ng-container *ngIf="hasVisibleActions(row)">
              <button
                mat-icon-button
                [matMenuTriggerFor]="actionMenu"
                aria-label="Acciones"
              >
                <mat-icon>more_vert</mat-icon>
              </button>
              <mat-menu #actionMenu="matMenu">
                <ng-container *ngFor="let action of actions">
                  <button
                    mat-menu-item
                    type="button"
                    *ngIf="!action.visible || action.visible(row)"
                    (click)="onActionClick(action, row, $event)"
                  >
                    <mat-icon *ngIf="action.icon">{{ action.icon }}</mat-icon>
                    <span>{{ action.label || action.tooltip || action.icon }}</span>
                  </button>
                </ng-container>
              </mat-menu>
            </ng-container>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>

        <!-- No data row -->
        <tr class="mat-row" *matNoDataRow>
          <td class="mat-cell" [attr.colspan]="displayedColumns.length">
            <div class="no-data">
              {{ noDataMessage || 'No hay datos para mostrar' }}
            </div>
          </td>
        </tr>
      </table>

      <mat-paginator
        *ngIf="showPaginator"
        [length]="totalItems"
        [pageSize]="pageSize"
        [pageSizeOptions]="pageSizeOptions"
        [pageIndex]="pageIndex"
        (page)="onPageChange($event)"
        showFirstLastButtons
      >
      </mat-paginator>
    </div>
  `,
    styles: [`
    .table-container {
      width: 100%;
      overflow: auto;
    }

    table {
      width: 100%;
    }

    .no-data {
      padding: 20px;
      text-align: center;
      color: rgba(0, 0, 0, 0.54);
    }

    mat-paginator {
      border-top: 1px solid rgba(0, 0, 0, 0.12);
    }
  `]
})
export class DataTableComponent implements OnInit {
    @Input() data: any[] = [];
    @Input() columns: TableColumn[] = [];
    @Input() actions?: TableAction[];
    @Input() showPaginator = true;
    @Input() pageSize = 10;
    @Input() pageSizeOptions = [5, 10, 25, 50];
    @Input() pageIndex = 0;
    @Input() totalItems = 0;
    @Input() noDataMessage?: string;
    @Input() selectable = false;
    @Input() selectedIds: number[] = [];

    @Output() pageChange = new EventEmitter<PageEvent>();
    @Output() sortChange = new EventEmitter<Sort>();
    @Output() actionClick = new EventEmitter<{ action: string; row: any }>();
    @Output() selectionChange = new EventEmitter<number[]>();

    displayedColumns: string[] = [];
    selection = new SelectionModel<number>(true, []);

    ngOnInit(): void {
        this.displayedColumns = [];

        if (this.selectable) {
            this.displayedColumns.push('select');
        }

        this.displayedColumns.push(...this.columns.map(col => col.key));

        if (this.actions && this.actions.length > 0) {
            this.displayedColumns.push('actions');
        }

        // Initialize selection
        if (this.selectedIds && this.selectedIds.length > 0) {
            this.selection.select(...this.selectedIds);
        }
    }

    onPageChange(event: PageEvent): void {
        this.pageChange.emit(event);
    }

    onSortChange(sort: Sort): void {
        this.sortChange.emit(sort);
    }

    onActionClick(action: TableAction, row: any, event?: Event): void {
        // No detener la propagación para que el menú se cierre correctamente
        if (action.handler) {
            action.handler(row);
        }
        // Always emit the action, using label or icon as identifier
        const actionId = action.label || action.icon;
        this.actionClick.emit({ action: actionId, row });
    }

  hasVisibleActions(row: any): boolean {
    if (!this.actions || this.actions.length === 0) {
      return false;
    }
    return this.actions.some(action => !action.visible || action.visible(row));
  }

    // Selection methods
    isAllSelected(): boolean {
        const numSelected = this.selection.selected.length;
        const numRows = this.data.length;
        return numSelected === numRows;
    }

    toggleAllRows(): void {
        if (this.isAllSelected()) {
            this.selection.clear();
        } else {
            this.data.forEach(row => this.selection.select(row.id));
        }
        this.emitSelectionChange();
    }

    toggleRow(row: any): void {
        this.selection.toggle(row.id);
        this.emitSelectionChange();
    }

    emitSelectionChange(): void {
        this.selectionChange.emit(this.selection.selected);
    }
}
