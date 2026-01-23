import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotaRemision } from '../../models/nota.model';

@Injectable({
  providedIn: 'root'
})
export class NotaRemisionApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notas-remision`;

  getAll(empresaId: number, page = 0, size = 20): Observable<{ content: NotaRemision[]; totalElements: number }> {
    let params = new HttpParams()
      .set('empresaId', empresaId.toString())
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', 'fecha')
      .set('sortDirection', 'DESC');

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

  descargarPdfKude(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/kude-pdf`, {
      responseType: 'blob'
    });
  }

  enviarEmail(id: number, data: { email: string, actualizarCliente: boolean }): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/enviar-email`, data);
  }
}

