import { Component, OnInit, signal, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { Subject, takeUntil } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotaRemisionApiService } from '../../core/api/nota-remision-api.service';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { NotaRemision } from '../../models/nota.model';
import { Cliente } from '../../models/cliente.model';
import { DataTableComponent, TableColumn, TableAction } from '../../shared/components/data-table/data-table.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { CancelarDeDialogComponent } from '../documentos/cancelar-de-dialog.component';
import { SifenApiService } from '../../core/api/sifen-api.service';
import { NotaRemisionEstadoDialogComponent } from '../../shared/components/nota-remision-estado-dialog/nota-remision-estado-dialog.component';
import { EmailEnviarDialogComponent } from '../../shared/components/email-enviar-dialog/email-enviar-dialog.component';

@Component({
  selector: 'app-nota-remision-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatDialogModule,
    DataTableComponent,
    LoadingSpinnerComponent,
    EmailEnviarDialogComponent
  ],
  template: `
    <div class="notas-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title><h2>Notas de Remisión</h2></mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="header-actions">
            <button mat-raised-button color="primary" (click)="crearNotaRemision()">
              <mat-icon>add</mat-icon>
              Nueva Nota de Remisión
            </button>
          </div>
          <app-loading-spinner [loading]="loading()" />
          <app-data-table
            *ngIf="!loading()"
            [columns]="columns"
            [data]="notasPaginas()"
            [actions]="tableActions"
            [pageSize]="pageSize"
            [pageIndex]="pageIndex"
            [totalItems]="totalItems()"
            (actionClick)="onActionClick($event)"
            (pageChange)="onPageChange($event)"
          />
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .notas-container { padding: 24px; }
    .header-actions { margin-bottom: 16px; }
  `]
})
export class NotaRemisionListComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  notas = signal<NotaRemision[]>([]);
  notasPaginas = signal<NotaRemision[]>([]);
  loading = signal(false);
  pageSize = 20;
  pageIndex = 0;
  totalItems = signal(0);

  columns: TableColumn[] = [
    { key: 'numeroFormateado', label: 'Número', sortable: true },
    { key: 'fecha', label: 'Fecha', sortable: true, format: (v: string) => new Date(v).toLocaleDateString('es-PY') },
    { key: 'nombreDestinatario', label: 'Destinatario', sortable: true },
    { key: 'motivoEmision', label: 'Motivo', sortable: true },
    {
      key: 'estadoDocumentoElectronico',
      label: 'Estado DE',
      format: (v: string, row: any) => this.formatEstadoDE(v || 'SIN_DE', row)
    }
  ];

  tableActions: TableAction[] = [
    // Grupo: Información
    {
      icon: 'info',
      label: 'Ver estado',
      color: 'primary',
      tooltip: 'Ver estado completo de nota de remisión, DE y lote',
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
    {
      icon: 'email',
      label: 'Enviar vía email',
      color: 'primary',
      tooltip: 'Enviar documento electrónico por correo al destinatario',
      visible: (row: any) => row.documentoElectronicoId != null,
      group: 'Información'
    },
    // Grupo: Gestión NRE
    {
      icon: 'description',
      label: 'Generar DE',
      color: 'accent',
      tooltip: 'Generar y enviar documento electrónico a SIFEN',
      visible: (row: any) => !row.documentoElectronicoId,
      group: 'Gestión NRE'
    },
    {
      icon: 'cancel',
      label: 'Cancelar',
      color: 'warn',
      tooltip: 'Cancelar documento electrónico aprobado',
      visible: (row: any) => row.estadoDocumentoElectronico === 'APROBADO',
      group: 'Gestión NRE'
    },
    {
      icon: 'delete',
      label: 'Eliminar',
      color: 'warn',
      tooltip: 'Eliminar nota de remisión',
      visible: (row: any) => !row.documentoElectronicoId,
      group: 'Gestión NRE'
    },
    {
      icon: 'content_copy',
      label: 'Copiar nota',
      color: 'primary',
      tooltip: 'Copiar nota de remisión con todos sus datos',
      group: 'Gestión NRE'
    },
    // Grupo: SIFEN
    {
      icon: 'sync',
      label: 'Consultar SIFEN',
      color: 'primary',
      tooltip: 'Consultar estado en SIFEN (por lote o documento)',
      visible: (row: any) => !!row.cdcDocumentoElectronico || !!row.loteDeId,
      group: 'SIFEN'
    },
    {
      icon: 'search',
      label: 'Consultar por CDC',
      color: 'primary',
      tooltip: 'Consultar documento electrónico directamente por CDC',
      visible: (row: any) => !!row.cdcDocumentoElectronico,
      group: 'SIFEN'
    },
    {
      icon: 'link',
      label: 'Vincular a lote',
      color: 'accent',
      tooltip: 'Crear lote y enviar a SIFEN (para DEs generados sin lote)',
      visible: (row: any) => !!row.documentoElectronicoId && !row.loteDeId,
      group: 'SIFEN'
    }
  ];

  constructor(
    private notaRemisionApi: NotaRemisionApiService,
    private clienteApi: ClienteApiService,
    private sifenApi: SifenApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      if (params['empresaId']) this.cargarNotas(+params['empresaId']);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  cargarNotas(empresaId: number): void {
    this.loading.set(true);
    this.notaRemisionApi.getAll(empresaId, this.pageIndex, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => {
          this.notas.set(r.content);
          this.totalItems.set(r.totalElements);
          this.actualizarPaginacion();
          this.loading.set(false);
        },
        error: () => {
          this.snackBar.open('Error al cargar notas de remisión', 'Cerrar', { duration: 3000 });
          this.loading.set(false);
        }
      });
  }

  actualizarPaginacion(): void {
    const todas = this.notas();
    const start = this.pageIndex * this.pageSize;
    this.notasPaginas.set(todas.slice(start, start + this.pageSize));
  }

  onPageChange(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    if (empresaId) this.cargarNotas(+empresaId);
  }

  crearNotaRemision(): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    this.router.navigate(['/notas/notas-remision/nueva'], {
      queryParams: { empresaId: empresaId ? +empresaId : undefined }
    });
  }

  onActionClick(e: { action: string; row: any }): void {
    const nota = e.row as NotaRemision;
    switch (e.action) {
      case 'Ver estado':
        this.verEstado(nota);
        break;
             case 'Abrir PDF':
               this.abrirPdfKude(nota);
               break;
             case 'Enviar vía email':
               this.enviarEmailViaEmail(nota);
               break;
             case 'Generar DE':
        this.generarDE(nota);
        break;
      case 'Cancelar':
        this.cancelarDE(nota);
        break;
      case 'Eliminar':
        this.eliminarNota(nota);
        break;
      case 'Consultar SIFEN':
        this.consultarSifen(nota);
        break;
      case 'Consultar por CDC':
        this.consultarPorCdc(nota);
        break;
      case 'Vincular a lote':
        this.vincularLote(nota);
        break;
      case 'Copiar nota':
        this.copiarNota(nota);
        break;
    }
  }

  vincularLote(nota: NotaRemision): void {
    if (!nota.id) return;

    this.loading.set(true);
    this.snackBar.open('Vinculando a lote y enviando a SIFEN...', 'Cerrar', { duration: 2000 });

    this.notaRemisionApi.vincularLote(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (result) => {
        this.snackBar.open('Lote creado y enviado exitosamente', 'Cerrar', { duration: 4000 });
        this.recargarYMostrarEstado(nota);
        this.loading.set(false);
      },
      error: (err) => {
        this.snackBar.open(`Error al vincular lote: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
        this.loading.set(false);
      }
    });
  }

  consultarSifen(nota: NotaRemision): void {
    if (nota.loteDeId) {
      // Consultar lote
      this.snackBar.open('Consultando estado del lote...', 'Cerrar', { duration: 2000 });
      this.sifenApi.consultarLote(nota.loteDeId).pipe(takeUntil(this.destroy$)).subscribe({
        next: (lote) => {
          this.snackBar.open(`Lote consultado. Estado: ${lote.estado}`, 'Cerrar', { duration: 3000 });
          // Recargar nota y abrir diálogo de estado
          this.recargarYMostrarEstado(nota);
        },
        error: (err) => {
          this.snackBar.open(`Error al consultar lote: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
        }
      });
    } else if (nota.cdcDocumentoElectronico) {
      // Consultar documento individual
      this.snackBar.open('Consultando estado del documento...', 'Cerrar', { duration: 2000 });
      this.sifenApi.consultarDocumento(nota.cdcDocumentoElectronico).pipe(takeUntil(this.destroy$)).subscribe({
        next: (doc) => {
          this.snackBar.open(`Documento consultado. Estado: ${doc.estado}`, 'Cerrar', { duration: 3000 });
          // Recargar nota y abrir diálogo de estado
          this.recargarYMostrarEstado(nota);
        },
        error: (err) => {
          console.error('❌ ERROR al consultar documento:', err);
          this.snackBar.open(`Error al consultar documento: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
        }
      });
    } else {
      this.snackBar.open('Esta nota de remisión no tiene DE asociado para consultar', 'Cerrar', { duration: 3000 });
    }
  }

  consultarPorCdc(nota: NotaRemision): void {
    if (!nota.cdcDocumentoElectronico) {
      this.snackBar.open('Esta nota de remisión no tiene CDC para consultar', 'Cerrar', { duration: 3000 });
      return;
    }

    this.snackBar.open('Consultando documento por CDC...', 'Cerrar', { duration: 2000 });
    this.sifenApi.consultarDocumento(nota.cdcDocumentoElectronico).pipe(takeUntil(this.destroy$)).subscribe({
      next: (doc) => {
        this.snackBar.open(`Documento consultado. Estado: ${doc.estado}`, 'Cerrar', { duration: 3000 });
        // Recargar nota y abrir diálogo de estado
        this.recargarYMostrarEstado(nota);
      },
      error: (err) => {
        console.error('❌ ERROR al consultar documento por CDC:', err);
        this.snackBar.open(`Error al consultar documento: ${err.error?.message || err.message}`, 'Cerrar', { duration: 5000 });
      }
    });
  }

  private recargarYMostrarEstado(nota: NotaRemision): void {
    // Recargar la nota para obtener datos actualizados
    if (nota.id) {
      this.notaRemisionApi.getById(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: (notaActualizada) => {
          // Actualizar la nota en la lista
          const notas = this.notas();
          const index = notas.findIndex(n => n.id === nota.id);
          if (index !== -1) {
            notas[index] = notaActualizada;
            this.notas.set([...notas]);
            this.actualizarPaginacion();
          }
          // Abrir diálogo de estado con datos actualizados
          this.verEstado(notaActualizada);
        },
        error: () => {
          // Si falla la recarga, mostrar estado con los datos que tenemos
          this.verEstado(nota);
        }
      });
    } else {
      // Si no hay ID, mostrar estado con los datos que tenemos
      this.verEstado(nota);
    }
  }

  cancelarDE(nota: NotaRemision): void {
    if (!nota.cdcDocumentoElectronico) return;

    const dialogRef = this.dialog.open(CancelarDeDialogComponent, {
      width: '500px',
      data: {
        factura: nota,
        cdc: nota.cdcDocumentoElectronico
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
      if (result && result.cdc && result.motivo) {
        this.loading.set(true);
        this.sifenApi.cancelarDocumento(result.cdc, result.motivo).subscribe({
          next: () => {
            this.snackBar.open('Nota de remisión cancelada exitosamente', 'Cerrar', { duration: 4000 });
            const empresaId = this.route.snapshot.queryParams['empresaId'];
            if (empresaId) this.cargarNotas(+empresaId);
            else this.loading.set(false);
          },
          error: (error) => {
            this.snackBar.open(error.error?.message || 'Error al cancelar', 'Cerrar', { duration: 5000 });
            this.loading.set(false);
          }
        });
      }
    });
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

    if (row?.urlQrDocumentoElectronico && estado !== 'SIN_DE') {
      const urlQr = row.urlQrDocumentoElectronico;
      const urlEscaped = urlQr.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
      return `${estadoText} <a href="${urlEscaped}" target="_blank" class="qr-link" title="Abrir consulta en SIFEN" onclick="event.stopPropagation(); return true;" style="vertical-align: middle; margin-left: 6px; display: inline-block; color: #1976d2; text-decoration: none; font-size: 18px;">🔗</a>`;
    }

    return estadoText;
  }

  generarDE(nota: NotaRemision): void {
    if (!nota.id) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Generar Documento Electrónico',
        message: `¿Desea generar y enviar el documento electrónico para la nota de remisión ${nota.numeroFormateado}?`,
        confirmText: 'Generar y Enviar',
        cancelText: 'Cancelar'
      }
    });

    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && nota.id) {
        this.loading.set(true);
        this.notaRemisionApi.generarYEnviar(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Documento electrónico enviado a SIFEN', 'Cerrar', { duration: 4000 });
            const empresaId = this.route.snapshot.queryParams['empresaId'];
            if (empresaId) this.cargarNotas(+empresaId);
            else this.loading.set(false);
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || 'Error al generar el documento electrónico',
              'Cerrar',
              { duration: 5000 }
            );
            this.loading.set(false);
          }
        });
      }
    });
  }

  editarNota(nota: NotaRemision): void {
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    this.router.navigate(['/notas/notas-remision', nota.id], {
      queryParams: { empresaId: empresaId ? +empresaId : undefined }
    });
  }

  copiarNota(nota: NotaRemision): void {
    if (!nota.id) return;
    const empresaId = this.route.snapshot.queryParams['empresaId'];
    this.router.navigate(['/notas/notas-remision/nueva'], {
      queryParams: {
        empresaId: empresaId ? +empresaId : undefined,
        copyFromId: nota.id
      }
    });
  }

  eliminarNota(nota: NotaRemision): void {
    if (!nota.id) return;
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar Nota de Remisión',
        message: `¿Está seguro de eliminar la nota de remisión ${nota.numeroFormateado}?`,
        confirmText: 'Eliminar',
        cancelText: 'Cancelar'
      }
    });
    dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirmed => {
      if (confirmed && nota.id) {
        this.notaRemisionApi.delete(nota.id).pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.snackBar.open('Nota de remisión eliminada', 'Cerrar', { duration: 3000 });
            const empresaId = this.route.snapshot.queryParams['empresaId'];
            if (empresaId) this.cargarNotas(+empresaId);
          },
          error: () => this.snackBar.open('Error al eliminar', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }

  verEstado(nota: NotaRemision): void {
    this.dialog.open(NotaRemisionEstadoDialogComponent, {
      width: '800px',
      maxWidth: '90vw',
      data: { notaRemision: nota },
      disableClose: false
    });
  }

  abrirPdfKude(nota: NotaRemision): void {
    if (!nota.id) return;

    if (!nota.documentoElectronicoId) {
      this.snackBar.open('Esta nota de remisión no tiene documento electrónico asociado', 'Cerrar', { duration: 3000 });
      return;
    }

    const token = localStorage.getItem('auth_token');
    const url = `${environment.apiUrl}/notas-remision/${nota.id}/kude-pdf?token=${token}`;

    // Abrir en nueva pestaña directamente desde la URL del servidor
    // Esto permite que el navegador maneje el nombre del archivo correctamente
    // gracias al header Content-Disposition: inline; filename="..."
    const win = window.open(url, '_blank');
    if (win) {
      win.focus();
      this.snackBar.open('Abriendo PDF...', 'Cerrar', { duration: 2000 });
    } else {
      this.snackBar.open('Por favor, permite las ventanas emergentes para ver el PDF', 'Cerrar', { duration: 5000 });
    }
  }

         enviarEmailViaEmail(nota: NotaRemision): void {
           if (!nota.id) return;

           const dialogRef = this.dialog.open(EmailEnviarDialogComponent, {
             width: '500px',
             data: {
               titulo: 'Enviar Nota de Remisión por Email',
               clienteNombre: nota.nombreDestinatario || nota.nombreCliente || 'Cliente',
               emailActual: (nota as any).emailCliente
             }
           });

           dialogRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(result => {
             if (result) {
               this.loading.set(true);
               this.snackBar.open('Enviando email...', 'Cerrar', { duration: 2000 });

               // Enviar el email sin actualizar el cliente todavía
               this.notaRemisionApi.enviarEmail(nota.id!, { ...result, actualizarCliente: false }).pipe(takeUntil(this.destroy$)).subscribe({
                 next: () => {
                   this.snackBar.open('Email enviado exitosamente', 'Cerrar', { duration: 3000 });
                   this.loading.set(false);

                   // Consultar si desea actualizar el cliente
                   const emailUsado = result.email;
                   const emailActual = (nota as any).emailCliente;

                   if (nota.clienteId && emailUsado !== emailActual) {
                     const confirmRef = this.dialog.open(ConfirmDialogComponent, {
                       data: {
                         title: 'Actualizar Cliente',
                         message: `¿Desea actualizar el correo del cliente a "${emailUsado}"?`,
                         confirmText: 'Actualizar',
                         cancelText: 'No'
                       }
                     });

                     confirmRef.afterClosed().pipe(takeUntil(this.destroy$)).subscribe(confirm => {
                       if (confirm) {
                         this.actualizarEmailCliente(nota.empresaId, nota.clienteId!, emailUsado);
                       }
                     });
                   }
                 },
                 error: (err) => {
                   console.error('Error al enviar email:', err);
                   this.snackBar.open(
                     `Error al enviar email: ${err.error?.error || err.error?.message || err.message}`,
                     'Cerrar',
                     { duration: 5000 }
                   );
                   this.loading.set(false);
                 }
               });
             }
           });
         }

         actualizarEmailCliente(empresaId: number, clienteId: number, nuevoEmail: string): void {
           this.clienteApi.getById(empresaId, clienteId).pipe(takeUntil(this.destroy$)).subscribe({
             next: (cliente: Cliente) => {
               cliente.email = nuevoEmail;
               this.clienteApi.update(empresaId, clienteId, cliente).pipe(takeUntil(this.destroy$)).subscribe({
                 next: () => {
                   this.snackBar.open('Correo del cliente actualizado correctamente', 'Cerrar', { duration: 3000 });
                   // Recargar para ver el cambio si es necesario
                   this.cargarNotas(empresaId);
                 },
                 error: (err) => {
                   console.error('Error al actualizar cliente:', err);
                   this.snackBar.open('No se pudo actualizar el correo del cliente', 'Cerrar', { duration: 3000 });
                 }
               });
             }
           });
         }
       }

