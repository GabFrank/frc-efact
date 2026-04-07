import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { NotaCredito } from '../../models/nota.model';
import { GenerarDeResponse } from '../../models/documento-electronico.model';

@Injectable({
  providedIn: 'root'
})
export class NotaCreditoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notas-credito`;

  getAll(empresaId: number, page = 0, size = 20): Observable<{ content: NotaCredito[]; totalElements: number }> {
    let params = new HttpParams()
      .set('empresaId', empresaId.toString())
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', 'fecha')
      .set('sortDirection', 'DESC');

    return this.http.get<{ content: NotaCredito[]; totalElements: number }>(this.baseUrl, { params });
  }

  getById(id: number): Observable<NotaCredito> {
    return this.http.get<NotaCredito>(`${this.baseUrl}/${id}`);
  }

  create(nota: NotaCredito): Observable<NotaCredito> {
    return this.http.post<NotaCredito>(this.baseUrl, nota);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  generarDE(notaCreditoId: number): Observable<GenerarDeResponse> {
    return this.http.post<GenerarDeResponse>(`${this.baseUrl}/${notaCreditoId}/generar-de`, {});
  }

  descargarPdfKude(notaCreditoId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${notaCreditoId}/kude-pdf`, {
      responseType: 'blob'
    });
  }
}

