import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

import { UsuarioEmpresa } from '../../models/usuario-empresa.model';
import { EmpresaApiService } from '../../core/api/empresa-api.service';

@Component({
  selector: 'app-edit-user-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    ReactiveFormsModule
  ],
  template: `
    <h2 mat-dialog-title>Editar Rol de Usuario</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Usuario</mat-label>
          <input matInput [value]="usuarioEmpresa.usuarioUsername" readonly>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Rol en Empresa</mat-label>
          <mat-select formControlName="rolEmpresa" required>
            <mat-option value="ADMINISTRADOR">Administrador</mat-option>
            <mat-option value="FACTURADOR">Facturador</mat-option>
            <mat-option value="LECTOR">Lector</mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancelar</button>
      <button mat-raised-button color="primary" (click)="onSave()" [disabled]="!form.valid">
        Guardar
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }
  `]
})
export class EditUserDialogComponent implements OnInit {
  form: FormGroup;
  usuarioEmpresa: UsuarioEmpresa;

  constructor(
    private fb: FormBuilder,
    private empresaApiService: EmpresaApiService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<EditUserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { usuarioEmpresa: UsuarioEmpresa }
  ) {
    this.usuarioEmpresa = data.usuarioEmpresa;
    this.form = this.fb.group({
      rolEmpresa: [this.usuarioEmpresa.rolEmpresa, Validators.required]
    });
  }

  ngOnInit(): void {
    // Form already initialized in constructor
  }

  onSave(): void {
    if (this.form.valid) {
      const { rolEmpresa } = this.form.value;

      this.empresaApiService.asignarUsuarioEmpresa(this.usuarioEmpresa.empresaId, {
        usuarioId: this.usuarioEmpresa.usuarioId,
        rolEmpresa
      }).subscribe({
        next: () => {
          this.snackBar.open('Rol actualizado exitosamente', 'Cerrar', { duration: 3000 });
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Error al actualizar rol:', error);
          this.snackBar.open('Error al actualizar rol', 'Cerrar', { duration: 3000 });
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
