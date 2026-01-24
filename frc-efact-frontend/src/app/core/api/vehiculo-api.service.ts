import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Vehiculo } from '../../models/vehiculo.model';
import { PageResponse } from './cliente-api.service';

@Injectable({
  providedIn: 'root'
})
export class VehiculoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/vehiculos`;

  /**
   * Obtiene todos los vehículos activos de una empresa.
   */
  getByEmpresa(empresaId: number): Observable<Vehiculo[]> {
    return this.http.get<Vehiculo[]>(`${this.baseUrl}/empresa/${empresaId}`);
  }

  /**
   * Obtiene un vehículo por ID.
   */
  getById(empresaId: number, vehiculoId: number): Observable<Vehiculo> {
    return this.http.get<Vehiculo>(`${this.baseUrl}/empresa/${empresaId}/${vehiculoId}`);
  }

  /**
   * Busca vehículos por matrícula o marca.
   */
  buscar(empresaId: number, query: string): Observable<Vehiculo[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Vehiculo[]>(`${this.baseUrl}/empresa/${empresaId}/buscar`, { params });
  }

  /**
   * Lista vehículos con paginación.
   */
  listarPaginados(
    empresaId: number,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'matricula',
    sortDir: string = 'ASC'
  ): Observable<PageResponse<Vehiculo>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<PageResponse<Vehiculo>>(`${this.baseUrl}/empresa/${empresaId}/paginado`, { params });
  }

  /**
   * Busca vehículos con filtros y paginación.
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
  ): Observable<PageResponse<Vehiculo>> {
    let httpParams = new HttpParams()
      .set('page', (params.page ?? 0).toString())
      .set('size', (params.size ?? 20).toString())
      .set('sortBy', params.sortBy ?? 'matricula')
      .set('sortDir', params.sortDir ?? 'ASC');

    if (params.q && params.q.trim()) {
      httpParams = httpParams.set('q', params.q.trim());
    }

    if (params.activo !== undefined) {
      httpParams = httpParams.set('activo', params.activo.toString());
    }

    return this.http.get<PageResponse<Vehiculo>>(
      `${this.baseUrl}/empresa/${empresaId}/filtrar`,
      { params: httpParams }
    );
  }

  /**
   * Crea un nuevo vehículo.
   */
  create(empresaId: number, vehiculo: Partial<Vehiculo>): Observable<Vehiculo> {
    return this.http.post<Vehiculo>(`${this.baseUrl}/empresa/${empresaId}`, vehiculo);
  }

  /**
   * Actualiza un vehículo existente.
   */
  update(empresaId: number, vehiculoId: number, vehiculo: Partial<Vehiculo>): Observable<Vehiculo> {
    return this.http.put<Vehiculo>(`${this.baseUrl}/empresa/${empresaId}/${vehiculoId}`, vehiculo);
  }

  /**
   * Desactiva un vehículo (soft delete).
   */
  delete(empresaId: number, vehiculoId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/empresa/${empresaId}/${vehiculoId}`);
  }

  /**
   * Reactiva un vehículo desactivado.
   */
  reactivar(empresaId: number, vehiculoId: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/empresa/${empresaId}/${vehiculoId}/reactivar`, {});
  }

  /**
   * Cuenta los vehículos activos de una empresa.
   */
  contar(empresaId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/empresa/${empresaId}/count`);
  }
}
