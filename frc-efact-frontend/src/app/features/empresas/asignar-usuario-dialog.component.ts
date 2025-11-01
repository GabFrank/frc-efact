import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { EmpresaApiService } from '../../core/api/empresa-api.service';
import { UsuarioApiService } from '../../core/api/usuario-api.service';
import { User, AsignarUsuarioEmpresaRequest } from '../../models/user.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-asignar-usuario-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    LoadingSpinnerComponent
  ],
  template: `
    <h2 mat-dialog-title>Asignar Usuario a Empresa</h2>

    <mat-dialog-content>
      <app-loading-spinner *ngIf="loading"></app-loading-spinner>

      <form [formGroup]="asignarForm" *ngIf="!loading">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Usuario</mat-label>
          <mat-select formControlName="usuarioId" placeholder="Seleccione un usuario">
            <mat-option *ngFor="let usuario of usuarios" [value]="usuario.id">
              {{ usuario.username }} ({{ usuario.email }})
            </mat-option>
          </mat-select>
          <mat-error *ngIf="asignarForm.get('usuarioId')?.hasError('required')">
            Debe seleccionar un usuario
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Rol en la Empresa</mat-label>
          <mat-select formControlName="rolEmpresa" placeholder="Seleccione un rol">
            <mat-option value="ADMINISTRADOR">
              <div class="rol-option">
                <strong>Administrador</strong>
                <span class="rol-description">Acceso completo para gestionar la empresa</span>
              </div>
            </mat-option>
            <mat-option value="FACTURADOR">
              <div class="rol-option">
                <strong>Facturador</strong>
                <span class="rol-description">Puede crear facturas y gestionar documentos electrónicos</span>
              </div>
            </mat-option>
            <mat-option value="LECTOR">
              <div class="rol-option">
                <strong>Lector</strong>
                <span class="rol-description">Solo puede visualizar información</span>
              </div>
            </mat-option>
          </mat-select>
          <mat-error *ngIf="asignarForm.get('rolEmpresa')?.hasError('required')">
            Debe seleccionar un rol
          </mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        (click)="onSubmit()"
        [disabled]="!asignarForm.valid || submitting">
        <mat-icon>person_add</mat-icon>
        Asignar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content {
      min-width: 400px;
      padding: 20px 24px;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .rol-option {
      display: flex;
      flex-direction: column;
      padding: 4px 0;
    }

    .rol-description {
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }

    button mat-icon {
      margin-right: 4px;
    }
  `]
})
export class AsignarUsuarioDialogComponent implements OnInit {
  asignarForm: FormGroup;
  usuarios: User[] = [];
  loading = false;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AsignarUsuarioDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { empresaId: number },
    private empresaApiService: EmpresaApiService,
    private usuarioApiService: UsuarioApiService,
    private snackBar: MatSnackBar
  ) {
    this.asignarForm = this.fb.group({
      usuarioId: ['', Validators.required],
      rolEmpresa: ['LECTOR', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadUsuariosDisponibles();
  }

  private loadUsuariosDisponibles(): void {
    this.loading = true;

    this.empresaApiService.getUsuariosDisponibles(this.data.empresaId).subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Error al cargar usuarios disponibles', 'Cerrar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.asignarForm.valid && !this.submitting) {
      this.submitting = true;

      const request: AsignarUsuarioEmpresaRequest = {
        usuarioId: this.asignarForm.value.usuarioId,
        empresaId: this.data.empresaId,
        rolEmpresa: this.asignarForm.value.rolEmpresa
      };

      this.empresaApiService.asignarUsuarioEmpresa(request).subscribe({
        next: () => {
          this.dialogRef.close(true);
        },
        error: (error) => {
          this.snackBar.open('Error al asignar usuario', 'Cerrar', { duration: 3000 });
          this.submitting = false;
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
