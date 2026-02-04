import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Chofer } from '../../models/chofer.model';
import { PageResponse } from './cliente-api.service';

@Injectable({
  providedIn: 'root'
})
export class ChoferApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/choferes`;

  /**
   * Obtiene todos los choferes activos de una empresa.
   */
  getByEmpresa(empresaId: number): Observable<Chofer[]> {
    return this.http.get<Chofer[]>(`${this.baseUrl}/empresa/${empresaId}`);
  }

  /**
   * Obtiene un chofer por ID.
   */
  getById(empresaId: number, choferId: number): Observable<Chofer> {
    return this.http.get<Chofer>(`${this.baseUrl}/empresa/${empresaId}/${choferId}`);
  }

  /**
   * Busca choferes por nombre o documento.
   */
  buscar(empresaId: number, query: string): Observable<Chofer[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Chofer[]>(`${this.baseUrl}/empresa/${empresaId}/buscar`, { params });
  }

  /**
   * Lista choferes con paginación.
   */
  listarPaginados(
    empresaId: number,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'nombre',
    sortDir: string = 'ASC'
  ): Observable<PageResponse<Chofer>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<PageResponse<Chofer>>(`${this.baseUrl}/empresa/${empresaId}/paginado`, { params });
  }

  /**
   * Busca choferes con filtros y paginación.
   */
  buscarConFiltros(
    empresaId: number,
    params: {
      q?: string;
      activo?: boolean;
      page?: number;
      size?: number;
      sortBy?: string;
      sortDir?: 'ASC' | 'DESC';
    }
  ): Observable<PageResponse<Chofer>> {
    let httpParams = new HttpParams()
      .set('page', (params.page ?? 0).toString())
      .set('size', (params.size ?? 20).toString())
      .set('sortBy', params.sortBy ?? 'nombre')
      .set('sortDir', params.sortDir ?? 'ASC');

    if (params.q && params.q.trim()) {
      httpParams = httpParams.set('q', params.q.trim());
    }

    if (params.activo !== undefined) {
      httpParams = httpParams.set('activo', params.activo.toString());
    }

    return this.http.get<PageResponse<Chofer>>(
      `${this.baseUrl}/empresa/${empresaId}/filtrar`,
      { params: httpParams }
    );
  }

  /**
   * Crea un nuevo chofer.
   */
  create(empresaId: number, chofer: Partial<Chofer>): Observable<Chofer> {
    return this.http.post<Chofer>(`${this.baseUrl}/empresa/${empresaId}`, chofer);
  }

  /**
   * Actualiza un chofer existente.
   */
  update(empresaId: number, choferId: number, chofer: Partial<Chofer>): Observable<Chofer> {
    return this.http.put<Chofer>(`${this.baseUrl}/empresa/${empresaId}/${choferId}`, chofer);
  }

  /**
   * Desactiva un chofer (soft delete).
   */
  delete(empresaId: number, choferId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/empresa/${empresaId}/${choferId}`);
  }

  /**
   * Reactiva un chofer desactivado.
   */
  reactivar(empresaId: number, choferId: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/empresa/${empresaId}/${choferId}/reactivar`, {});
  }

  /**
   * Cuenta los choferes activos de una empresa.
   */
  contar(empresaId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/empresa/${empresaId}/count`);
  }
}
