import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Producto } from '../../models/producto.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/productos`;

  getAll(empresaId?: number): Observable<Producto[]> {
    let params = new HttpParams();
    if (empresaId) {
      params = params.set('empresaId', empresaId.toString());
    }
    return this.http.get<Producto[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.baseUrl}/${id}`);
  }

  buscar(query: string, empresaId?: number): Observable<Producto[]> {
    let params = new HttpParams().set('q', query);
    if (empresaId) {
      params = params.set('empresaId', empresaId.toString());
    }
    return this.http.get<Producto[]>(`${this.baseUrl}/buscar`, { params });
  }

  create(producto: Partial<Producto>): Observable<Producto> {
    return this.http.post<Producto>(this.baseUrl, producto);
  }

  update(id: number, producto: Partial<Producto>): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/${id}`, producto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getByEmpresa(empresaId: number): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.baseUrl}/empresa/${empresaId}`);
  }

  importar(file: File, empresaId: number): Observable<{ imported: number; errors: string[] }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('empresaId', empresaId.toString());
    return this.http.post<{ imported: number; errors: string[] }>(
      `${this.baseUrl}/importar`,
      formData
    );
  }

  importarExcel(empresaId: number, file: File): Observable<{ imported: number; errors: string[] }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('empresaId', empresaId.toString());
    return this.http.post<{ imported: number; errors: string[] }>(
      `${this.baseUrl}/importar-excel`,
      formData
    );
  }
}
