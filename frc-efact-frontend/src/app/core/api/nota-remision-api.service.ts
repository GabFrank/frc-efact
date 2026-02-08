import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { NotaRemision } from '../../models/nota.model';

@Injectable({
  providedIn: 'root'
})
export class NotaRemisionApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notas-remision`;

  getAll(empresaId: number, page = 0, size = 20, filters: any = {}): Observable<{ content: NotaRemision[]; totalElements: number }> {
    let params = new HttpParams()
      .set('empresaId', empresaId.toString())
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', 'fecha')
      .set('sortDirection', 'DESC');

    // Agregar filtros si existen
    if (filters.numero) params = params.set('numero', filters.numero);
    if (filters.fechaDesde) params = params.set('fechaDesde', filters.fechaDesde);
    if (filters.fechaHasta) params = params.set('fechaHasta', filters.fechaHasta);
    if (filters.motivo) params = params.set('motivo', filters.motivo);
    if (filters.destinatario) params = params.set('destinatario', filters.destinatario);
    if (filters.vehiculo) params = params.set('vehiculo', filters.vehiculo);
    if (filters.chofer) params = params.set('chofer', filters.chofer);
    if (filters.estadoDE) params = params.set('estadoDE', filters.estadoDE);

    return this.http.get<{ content: NotaRemision[]; totalElements: number }>(this.baseUrl, { params });
  }

  getById(id: number): Observable<NotaRemision> {
    return this.http.get<NotaRemision>(`${this.baseUrl}/${id}`);
  }

  create(nota: NotaRemision): Observable<NotaRemision> {
    return this.http.post<NotaRemision>(this.baseUrl, nota);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  generarDE(id: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/${id}/generar-de`, {});
  }

  generarYEnviar(id: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/${id}/generar-y-enviar`, {});
  }

  vincularLote(id: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/${id}/vincular-lote`, {});
  }

  descargarPdfKude(id: number): Observable<{ blob: Blob; filename: string }> {
    return this.http.get(`${this.baseUrl}/${id}/kude-pdf`, {
      responseType: 'blob',
      observe: 'response'
    }).pipe(
      map(response => {
        // Extraer nombre del archivo del header Content-Disposition
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = 'KuDE.pdf';
        if (contentDisposition) {
          console.log('Content-Disposition header:', contentDisposition);
          // Intentar extraer el filename
          const matches = /filename\s*=\s*['"]?([^'"]+)['"]?/i.exec(contentDisposition);
          if (matches && matches[1]) {
            filename = matches[1];
          }
        }
        return { blob: response.body!, filename };
      })
    );
  }

  enviarEmail(id: number, data: { email: string, actualizarCliente: boolean }): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/enviar-email`, data);
  }
}

