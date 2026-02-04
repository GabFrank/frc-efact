import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotaDebito } from '../../models/nota.model';

@Injectable({
  providedIn: 'root'
})
export class NotaDebitoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notas-debito`;

  getAll(empresaId: number, page = 0, size = 20): Observable<{ content: NotaDebito[]; totalElements: number }> {
    let params = new HttpParams()
      .set('empresaId', empresaId.toString())
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', 'fecha')
      .set('sortDirection', 'DESC');

    return this.http.get<{ content: NotaDebito[]; totalElements: number }>(this.baseUrl, { params });
  }

  getById(id: number): Observable<NotaDebito> {
    return this.http.get<NotaDebito>(`${this.baseUrl}/${id}`);
  }

  create(nota: NotaDebito): Observable<NotaDebito> {
    return this.http.post<NotaDebito>(this.baseUrl, nota);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

