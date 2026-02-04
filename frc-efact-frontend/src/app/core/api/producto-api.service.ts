import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Producto } from '../../models/producto.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/productos`;

  /**
   * @deprecated Use getByEmpresa instead. This method is kept for backward compatibility.
   * El backend retorna Page<ProductoDto>, no un array simple.
   */
  getAll(empresaId?: number): Observable<Producto[]> {
    // Si se necesita empresaId, debe usar getByEmpresa que retorna paginación
    // Este método podría retornar un array vacío o usar getByEmpresa internamente
    if (empresaId) {
      return this.getByEmpresa(empresaId, 0, 1000).pipe(
        map(response => response.content)
      );
    }
    // Sin empresaId, el backend requeriría empresaId, así que retornamos array vacío
    return new Observable(observer => {
      observer.next([]);
      observer.complete();
    });
  }

  getById(id: number, empresaId: number): Observable<Producto> {
    const params = new HttpParams().set('empresaId', empresaId.toString());
    return this.http.get<Producto>(`${this.baseUrl}/${id}`, { params });
  }

  buscar(query: string, empresaId: number, page: number = 0, size: number = 20, sortBy: string = 'descripcion', sortDir: string = 'asc'): Observable<{ content: Producto[]; totalElements: number; totalPages: number }> {
    let params = new HttpParams()
      .set('empresaId', empresaId.toString())
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (query) {
      params = params.set('busqueda', query);
    }

    return this.http.get<{ content: Producto[]; totalElements: number; totalPages: number }>(`${this.baseUrl}/buscar`, { params });
  }

  create(producto: Partial<Producto>): Observable<Producto> {
    return this.http.post<Producto>(this.baseUrl, producto);
  }

  update(id: number, producto: Partial<Producto>): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/${id}`, producto);
  }

  delete(id: number, empresaId: number): Observable<void> {
    const params = new HttpParams().set('empresaId', empresaId.toString());
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { params });
  }

  getByEmpresa(
    empresaId: number,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'descripcion',
    sortDir: string = 'asc',
    activo?: boolean | null,
    busqueda?: string | null,
    tipoTransaccion?: string | null,
    iva?: number | null
  ): Observable<{ content: Producto[]; totalElements: number; totalPages: number }> {
    let params = new HttpParams()
      .set('empresaId', empresaId.toString())
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (activo !== null && activo !== undefined) {
      params = params.set('activo', activo.toString());
    }

    if (busqueda && busqueda.trim() !== '') {
      params = params.set('busqueda', busqueda.trim().toUpperCase());
    }

    if (tipoTransaccion) {
      params = params.set('tipoTransaccion', tipoTransaccion);
    }

    if (iva !== null && iva !== undefined) {
      params = params.set('iva', iva.toString());
    }

    return this.http.get<{ content: Producto[]; totalElements: number; totalPages: number }>(this.baseUrl, { params });
  }

  importarExcel(empresaId: number, file: File): Observable<{ mensaje: string; cantidadImportada: number; productos: Producto[] }> {
    const formData = new FormData();
    formData.append('archivo', file);
    formData.append('empresaId', empresaId.toString());
    return this.http.post<{ mensaje: string; cantidadImportada: number; productos: Producto[] }>(
      `${this.baseUrl}/importar`,
      formData
    );
  }

  verificarCodigo(empresaId: number, codigo: string, productoId?: number): Observable<{ existe: boolean }> {
    let params = new HttpParams()
      .set('empresaId', empresaId.toString())
      .set('codigo', codigo.toUpperCase().trim());

    if (productoId) {
      params = params.set('productoId', productoId.toString());
    }

    return this.http.get<{ existe: boolean }>(`${this.baseUrl}/verificar-codigo`, { params });
  }

  verificarDescripcion(empresaId: number, descripcion: string, productoId?: number): Observable<{ existe: boolean }> {
    let params = new HttpParams()
      .set('empresaId', empresaId.toString())
      .set('descripcion', descripcion.toUpperCase().trim());

    if (productoId) {
      params = params.set('productoId', productoId.toString());
    }

    return this.http.get<{ existe: boolean }>(`${this.baseUrl}/verificar-descripcion`, { params });
  }
}
