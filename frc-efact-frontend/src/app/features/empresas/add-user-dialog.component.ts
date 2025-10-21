import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { Subject, Observable, startWith, map, takeUntil } from 'rxjs';

import { User } from '../../models/user.model';
import { AsignarUsuarioEmpresaRequest } from '../../models/usuario-empresa.model';
import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { UsuarioApiService } from '../../core/api/usuario-api.service';
import { UserPreviewComponent } from './user-preview.component';
import { UserPreviewCompactComponent } from './user-preview-compact.component';
import { UserAssignmentConfirmationComponent, UserAssignmentConfirmationData } from './user-assignment-confirmation.component';

@Component({
  selector: 'app-add-user-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatAutocompleteModule,
    MatChipsModule,
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    ReactiveFormsModule,
    UserPreviewCompactComponent
  ],
  template: `
    <h2 mat-dialog-title>Agregar Usuario a Empresa</h2>
    <mat-dialog-content class="dialog-content">
      <form [formGroup]="form">
        <!-- Búsqueda de Usuario -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Buscar Usuario</mat-label>
          <input
            matInput
            formControlName="usuarioSearch"
            [matAutocomplete]="auto"
            placeholder="Escriba el nombre de usuario o email..."
            (input)="onUserSearch($event)">
          <mat-autocomplete
            #auto="matAutocomplete"
            [displayWith]="displayUser"
            (optionSelected)="onUserSelected($event)">
            <mat-option *ngFor="let user of filteredUsers$ | async" [value]="user">
              <div class="user-option-simple">
                <div class="user-main">
                  <strong>{{ user.username }}</strong>
                  <span class="user-email">{{ user.email }}</span>
                </div>
              </div>
            </mat-option>
          </mat-autocomplete>
        </mat-form-field>

        <!-- Información del Usuario Seleccionado -->
        <div *ngIf="selectedUser" class="user-preview-container">
          <app-user-preview-compact [user]="selectedUser"></app-user-preview-compact>
        </div>

        <!-- Rol en Empresa -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Rol en Empresa</mat-label>
          <mat-select formControlName="rolEmpresa" required>
            <mat-option value="ADMINISTRADOR">Administrador</mat-option>
            <mat-option value="FACTURADOR">Facturador</mat-option>
            <mat-option value="LECTOR">Lector</mat-option>
          </mat-select>
        </mat-form-field>

        <!-- Confirmación -->
        <div *ngIf="selectedUser" class="confirmation-section">
          <mat-divider></mat-divider>
          <div class="confirmation-text">
            <p><strong>¿Confirmar asignación?</strong></p>
            <p>Se asignará al usuario <strong>{{ selectedUser.username }}</strong>
               como <strong>{{ form.get('rolEmpresa')?.value }}</strong> de esta empresa.</p>

            <!-- Descripción del Rol -->
            <div class="role-description" *ngIf="form.get('rolEmpresa')?.value">
              <h4>Permisos del rol {{ form.get('rolEmpresa')?.value }}:</h4>
              <ul *ngIf="form.get('rolEmpresa')?.value === 'ADMINISTRADOR'">
                <li>Gestionar usuarios de la empresa</li>
                <li>Configurar parámetros de la empresa</li>
                <li>Ver toda la información de la empresa</li>
                <li>Crear y editar facturas</li>
                <li>Generar reportes completos</li>
              </ul>
              <ul *ngIf="form.get('rolEmpresa')?.value === 'LECTOR'">
                <li>Ver información de la empresa</li>
                <li>Consultar facturas existentes</li>
                <li>Generar reportes básicos</li>
                <li>Acceso de solo lectura</li>
              </ul>
            </div>
          </div>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end" class="dialog-actions">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        (click)="onSave()"
        [disabled]="!form.valid || !selectedUser">
        <mat-icon>person_add</mat-icon>
        Asignar Usuario
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-content {
      max-height: calc(90vh - 120px);
      overflow-y: auto;
      padding: 16px 0;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .user-option-simple {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .user-main {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }

    .user-email {
      font-size: 12px;
      color: #666;
    }

    .user-preview-container {
      margin: 16px 0;
      /* Sin altura máxima ni scroll - el componente compacto se ajusta automáticamente */
    }

    .confirmation-section {
      margin-top: 16px;
    }

    .confirmation-text {
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 8px;
      margin-top: 12px;
    }

    .confirmation-text p {
      margin: 6px 0;
      font-size: 14px;
    }

    .role-description {
      margin-top: 8px;
      padding: 8px;
      background-color: #e3f2fd;
      border-radius: 4px;
      border-left: 3px solid #2196f3;
    }

    .role-description h4 {
      margin: 0 0 4px 0;
      font-size: 14px;
      font-weight: 500;
      color: #1976d2;
    }

    .role-description ul {
      margin: 0;
      padding-left: 16px;
    }

    .role-description li {
      margin: 2px 0;
      font-size: 12px;
      color: #1976d2;
    }

    .dialog-actions {
      padding: 16px 0;
      border-top: 1px solid #e0e0e0;
      margin-top: 16px;
    }

    mat-dialog-actions button mat-icon {
      margin-right: 8px;
    }

    /* Responsive adjustments */
    @media (max-width: 600px) {
      .dialog-content {
        max-height: calc(90vh - 100px);
      }
    }
  `]
})
export class AddUserDialogComponent implements OnInit, OnDestroy {
  form: FormGroup;
  usuariosDisponibles: User[] = [];
  selectedUser: User | null = null;
  filteredUsers$: Observable<User[]>;
  usuarioSearchControl = new FormControl('');

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private usuarioApiService: UsuarioApiService,
    private empresaApiService: EmpresaApiService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    public dialogRef: MatDialogRef<AddUserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { empresaId: number }
  ) {
    this.form = this.fb.group({
      usuarioSearch: [''],
      rolEmpresa: ['LECTOR', Validators.required]
    });

    // Configurar autocompletado
    this.filteredUsers$ = this.usuarioSearchControl.valueChanges.pipe(
      startWith(''),
      map(value => this._filterUsers(value || ''))
    );
  }

  ngOnInit(): void {
    this.loadUsuariosDisponibles();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadUsuariosDisponibles(): void {
    this.usuarioApiService.getAsignables().pipe(takeUntil(this.destroy$)).subscribe({
      next: (usuarios) => {
        this.usuariosDisponibles = usuarios;
      },
      error: (error) => {
        console.error('Error al cargar usuarios:', error);
        this.snackBar.open('Error al cargar usuarios', 'Cerrar', { duration: 3000 });
      }
    });
  }

  private _filterUsers(value: string): User[] {
    if (!value) {
      return this.usuariosDisponibles.slice(0, 10); // Mostrar solo los primeros 10
    }

    const filterValue = value.toLowerCase();
    return this.usuariosDisponibles.filter(user =>
      user.username.toLowerCase().includes(filterValue) ||
      user.email.toLowerCase().includes(filterValue)
    ).slice(0, 10); // Limitar a 10 resultados
  }

  displayUser(user: User): string {
    if (!user || !user.username) {
      return '';
    }
    return `${user.username} (${user.email || 'Sin email'})`;
  }

  getNormalizedRoles(roles: any[]): string[] {
    if (!roles || !Array.isArray(roles)) return [];
    return roles.map(role => {
      if (typeof role === 'string') {
        return role;
      } else if (role && typeof role === 'object') {
        // Si es un objeto Role, usar la propiedad 'nombre'
        return role.nombre || role.name || 'Rol desconocido';
      }
      return 'Rol inválido';
    }).filter(role => role && role !== 'Rol desconocido' && role !== 'Rol inválido');
  }

  onUserSearch(event: any): void {
    const value = event.target.value;
    this.usuarioSearchControl.setValue(value);

    // Limpiar usuario seleccionado si se borra la búsqueda
    if (!value) {
      this.selectedUser = null;
    }
  }

  onUserSelected(event: any): void {
    this.selectedUser = event.option.value;
    this.form.patchValue({
      usuarioSearch: this.selectedUser ? this.displayUser(this.selectedUser) : ''
    });
  }

  onSave(): void {
    if (this.form.valid && this.selectedUser) {
      const { rolEmpresa } = this.form.value;

      // Mostrar diálogo de confirmación
      const confirmationData: UserAssignmentConfirmationData = {
        user: this.selectedUser,
        empresaId: this.data.empresaId,
        empresaNombre: 'Empresa', // TODO: Obtener nombre real de la empresa
        rolEmpresa
      };

      const confirmationDialog = this.dialog.open(UserAssignmentConfirmationComponent, {
        width: '600px',
        data: confirmationData
      });

      confirmationDialog.afterClosed().subscribe((confirmed: boolean) => {
        if (confirmed) {
          this.assignUser();
        }
      });
    }
  }

  private assignUser(): void {
    if (!this.selectedUser) return;

    const { rolEmpresa } = this.form.value;

    this.empresaApiService.asignarUsuarioEmpresa(this.data.empresaId, {
      usuarioId: this.selectedUser.id,
      rolEmpresa
    }).subscribe({
      next: () => {
        this.snackBar.open(
          `Usuario ${this.selectedUser?.username} agregado exitosamente como ${rolEmpresa}`,
          'Cerrar',
          { duration: 4000 }
        );
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error('Error al agregar usuario:', error);
        this.snackBar.open('Error al agregar usuario', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
