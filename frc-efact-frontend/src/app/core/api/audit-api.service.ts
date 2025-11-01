import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditLog, AuditLogFilter, AuditLogPage, AccionEnum } from '../../models/audit.model';
import { environment } from '../../../environments/environment';

/**
 * Servicio API para gestión de auditoría
 */
@Injectable({
  providedIn: 'root'
})
export class AuditApiService {
  private readonly apiUrl = `${environment.apiUrl}/auditoria`;

  constructor(private http: HttpClient) {}

  /**
   * Busca registros de auditoría con filtros
   */
  buscarConFiltros(filtros: AuditLogFilter): Observable<AuditLogPage> {
    let params = new HttpParams();
    
    if (filtros.usuarioId) {
      params = params.set('usuarioId', filtros.usuarioId.toString());
    }
    if (filtros.empresaId) {
      params = params.set('empresaId', filtros.empresaId.toString());
    }
    if (filtros.entidadTipo) {
      params = params.set('entidadTipo', filtros.entidadTipo);
    }
    if (filtros.accion) {
      params = params.set('accion', filtros.accion);
    }
    if (filtros.fechaDesde) {
      params = params.set('fechaDesde', filtros.fechaDesde);
    }
    if (filtros.fechaHasta) {
      params = params.set('fechaHasta', filtros.fechaHasta);
    }
    if (filtros.page !== undefined) {
      params = params.set('page', filtros.page.toString());
    }
    if (filtros.size !== undefined) {
      params = params.set('size', filtros.size.toString());
    }

    return this.http.get<AuditLogPage>(this.apiUrl, { params });
  }

  /**
   * Obtiene el historial de una entidad específica
   */
  getHistorialEntidad(tipo: string, id: number): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.apiUrl}/entidad/${tipo}/${id}`);
  }

  /**
   * Obtiene las últimas actividades del sistema
   */
  getUltimasActividades(limit: number = 10): Observable<AuditLog[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<AuditLog[]>(`${this.apiUrl}/ultimas-actividades`, { params });
  }

  /**
   * Obtiene las actividades de un usuario específico
   */
  getActividadesUsuario(usuarioId: number, limit: number = 10): Observable<AuditLog[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<AuditLog[]>(`${this.apiUrl}/usuario/${usuarioId}`, { params });
  }

  /**
   * Obtiene estadísticas de auditoría
   */
  getEstadisticas(
    fechaDesde: string,
    fechaHasta: string,
    empresaId?: number
  ): Observable<Record<AccionEnum, number>> {
    let params = new HttpParams()
      .set('fechaDesde', fechaDesde)
      .set('fechaHasta', fechaHasta);
    
    if (empresaId) {
      params = params.set('empresaId', empresaId.toString());
    }

    return this.http.get<Record<AccionEnum, number>>(`${this.apiUrl}/estadisticas`, { params });
  }

  /**
   * Cuenta el total de registros de auditoría
   */
  contarTotal(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/count`);
  }

  /**
   * Cuenta registros por empresa
   */
  contarPorEmpresa(empresaId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/count/empresa/${empresaId}`);
  }
}
