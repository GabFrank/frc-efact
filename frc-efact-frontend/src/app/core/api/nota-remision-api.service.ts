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
}

