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

  /**
   * Obtiene todos los clientes activos de una empresa.
   */
  getByEmpresa(empresaId: number): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.baseUrl}/empresa/${empresaId}`);
  }

  /**
   * Obtiene un cliente por ID.
   */
  getById(empresaId: number, clienteId: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.baseUrl}/empresa/${empresaId}/${clienteId}`);
  }

  /**
   * Busca clientes por nombre, razón social o RUC.
   */
  buscar(empresaId: number, query: string): Observable<Cliente[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Cliente[]>(`${this.baseUrl}/empresa/${empresaId}/buscar`, { params });
  }

  /**
   * Lista clientes con paginación.
   */
  listarPaginados(
    empresaId: number,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'nombre',
    sortDir: string = 'ASC'
  ): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<any>(`${this.baseUrl}/empresa/${empresaId}/paginado`, { params });
  }

  /**
   * Busca clientes con paginación.
   */
  buscarPaginados(
    empresaId: number,
    query: string,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'nombre',
    sortDir: string = 'ASC'
  ): Observable<any> {
    const params = new HttpParams()
      .set('q', query)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<any>(`${this.baseUrl}/empresa/${empresaId}/buscar/paginado`, { params });
  }

  /**
   * Busca un cliente por RUC.
   */
  buscarPorRuc(empresaId: number, ruc: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.baseUrl}/empresa/${empresaId}/ruc/${ruc}`);
  }

  /**
   * Verifica si existe un cliente activo con el RUC especificado para la empresa.
   * Útil para validación en tiempo real desde el formulario.
   */
  verificarRucExiste(empresaId: number, ruc: string, excluirClienteId?: number): Observable<boolean> {
    let url = `${this.baseUrl}/empresa/${empresaId}/ruc/${ruc}/existe`;
    if (excluirClienteId !== undefined) {
      url += `?excluirClienteId=${excluirClienteId}`;
    }
    return this.http.get<boolean>(url);
  }

  /**
   * Crea un nuevo cliente.
   */
  create(empresaId: number, cliente: Partial<Cliente>): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.baseUrl}/empresa/${empresaId}`, cliente);
  }

  /**
   * Actualiza un cliente existente.
   */
  update(empresaId: number, clienteId: number, cliente: Partial<Cliente>): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.baseUrl}/empresa/${empresaId}/${clienteId}`, cliente);
  }

  /**
   * Desactiva un cliente (soft delete).
   */
  delete(empresaId: number, clienteId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/empresa/${empresaId}/${clienteId}`);
  }

  /**
   * Reactiva un cliente desactivado.
   */
  reactivar(empresaId: number, clienteId: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/empresa/${empresaId}/${clienteId}/reactivar`, {});
  }

  /**
   * Cuenta los clientes activos de una empresa.
   */
  contar(empresaId: number): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/empresa/${empresaId}/count`);
  }

  /**
   * Busca clientes con filtros múltiples y paginación.
   */
  buscarConFiltros(
    empresaId: number,
    params: {
      q?: string;
      tipoClienteSifen?: string;
      activo?: boolean;
      page?: number;
      size?: number;
      sortBy?: string;
      sortDir?: 'ASC' | 'DESC';
    }
  ): Observable<PageResponse<Cliente>> {
    let httpParams = new HttpParams()
      .set('page', (params.page ?? 0).toString())
      .set('size', (params.size ?? 20).toString())
      .set('sortBy', params.sortBy ?? 'razonSocial')
      .set('sortDir', params.sortDir ?? 'ASC');

    if (params.q && params.q.trim()) {
      httpParams = httpParams.set('q', params.q.trim());
    }

    if (params.tipoClienteSifen) {
      httpParams = httpParams.set('tipoClienteSifen', params.tipoClienteSifen);
    }

    if (params.activo !== undefined) {
      httpParams = httpParams.set('activo', params.activo.toString());
    }

    return this.http.get<PageResponse<Cliente>>(
      `${this.baseUrl}/empresa/${empresaId}/filtrar`,
      { params: httpParams }
    );
  }
}

/**
 * Interfaz para respuestas paginadas del backend.
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}
