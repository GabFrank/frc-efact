import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '../../../environments/environment';

/**
 * Servicio para abrir y compartir PDFs KuDE.
 *
 * En móviles con soporte Web Share API (Android Chrome 93+, iOS Safari 15+),
 * descarga el PDF como Blob y abre el sheet nativo del SO para compartir
 * (WhatsApp, Gmail, Drive, visor PDF, etc.).
 *
 * En desktop o navegadores sin soporte, abre el PDF en una nueva pestaña.
 */
@Injectable({
  providedIn: 'root'
})
export class PdfShareService {
  private http = inject(HttpClient);
  private snackBar = inject(MatSnackBar);

  /**
   * Abre o comparte un PDF KuDE según la plataforma.
   *
   * @param pdfUrl URL relativa del endpoint (ej: '/facturas/123/kude-pdf')
   * @param filename Nombre del archivo PDF (ej: 'KuDE-001-001-0000016.pdf')
   */
  openOrShare(pdfUrl: string, filename: string): void {
    if (this.canShareFiles()) {
      this.shareAsPdf(pdfUrl, filename);
    } else {
      this.openInNewTab(pdfUrl);
    }
  }

  /**
   * Decide si conviene usar el menú de compartir nativo (móvil) en lugar de
   * abrir el PDF en una nueva pestaña (desktop).
   *
   * OJO: no alcanza con que existan navigator.share / navigator.canShare.
   * Safari de macOS (desktop) también las expone, lo que hacía que en la versión
   * web de escritorio se abriera la hoja de compartir del SO en vez de abrir el
   * PDF en una pestaña. Por eso restringimos el "share" a dispositivos
   * táctiles/móviles reales.
   */
  private canShareFiles(): boolean {
    if (!navigator.share || !navigator.canShare) {
      return false;
    }
    const ua = navigator.userAgent || '';
    const isIOS = /iPhone|iPad|iPod/.test(ua)
      // iPadOS se identifica como Mac; se distingue por los puntos táctiles
      || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
    const isAndroid = /Android/.test(ua);
    const isTouchPrimary = window.matchMedia?.('(pointer: coarse)').matches ?? false;
    return isIOS || isAndroid || isTouchPrimary;
  }

  /**
   * Descarga el PDF y abre el sheet nativo de compartir del SO.
   */
  private shareAsPdf(pdfUrl: string, filename: string): void {
    const url = `${environment.apiUrl}${pdfUrl}`;

    this.snackBar.open('Preparando PDF...', 'Cerrar', { duration: 3000 });

    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const file = new File([blob], filename, { type: 'application/pdf' });

        if (navigator.canShare && navigator.canShare({ files: [file] })) {
          navigator.share({
            files: [file],
            title: filename
          }).catch((err) => {
            // AbortError = usuario canceló el share sheet, no es un error real
            if (err.name !== 'AbortError') {
              console.error('Error al compartir PDF:', err);
              // Fallback: abrir en nueva pestaña
              this.openBlobInNewTab(blob, filename);
            }
          });
        } else {
          // canShare dice que no se puede compartir este archivo → fallback
          this.openBlobInNewTab(blob, filename);
        }
      },
      error: (err) => {
        console.error('Error descargando PDF:', err);
        this.snackBar.open('Error al descargar el PDF', 'Cerrar', { duration: 3000 });
      }
    });
  }

  /**
   * Abre el PDF en una nueva pestaña usando el token como query param.
   */
  private openInNewTab(pdfUrl: string): void {
    const token = localStorage.getItem('auth_token');
    const url = `${environment.apiUrl}${pdfUrl}?token=${token}`;

    const win = window.open(url, '_blank');
    if (win) {
      win.focus();
      this.snackBar.open('Abriendo PDF...', 'Cerrar', { duration: 2000 });
    } else {
      this.snackBar.open('Por favor, permite las ventanas emergentes para ver el PDF', 'Cerrar', { duration: 5000 });
    }
  }

  /**
   * Fallback: abre un Blob PDF en nueva pestaña si el share no está soportado.
   */
  private openBlobInNewTab(blob: Blob, filename: string): void {
    const blobUrl = URL.createObjectURL(blob);
    const win = window.open(blobUrl, '_blank');
    if (win) {
      win.focus();
      // Liberar el objeto URL después de un tiempo prudencial
      setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
    } else {
      // Si no se puede abrir pestaña, forzar descarga
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = filename;
      a.click();
      setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
    }
  }
}
