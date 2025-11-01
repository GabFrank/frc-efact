import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cliente } from '../../models/cliente.model';

@Injectable({
  providedIn: 'root'
})
export class ClienteApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/clientes`;

  getAll(empresaId?: number): Observable<Cliente[]> {
    let params = new HttpParams();
    if (empresaId) {
      params = params.set('empresaId', empresaId.toString());
    }
    return this.http.get<Cliente[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.baseUrl}/${id}`);
  }

  buscar(empresaId: number | string, query: string): Observable<Cliente[]> {
    let params = new HttpParams().set('q', query);
    params = params.set('empresaId', empresaId.toString());
    return this.http.get<Cliente[]>(`${this.baseUrl}/buscar`, { params });
  }

  getByEmpresa(empresaId: number): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.baseUrl}/empresa/${empresaId}`);
  }

  create(cliente: Partial<Cliente>): Observable<Cliente> {
    return this.http.post<Cliente>(this.baseUrl, cliente);
  }

  update(id: number, cliente: Partial<Cliente>): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.baseUrl}/${id}`, cliente);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
