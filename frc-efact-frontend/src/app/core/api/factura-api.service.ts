import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { FacturaLegal } from '../../models/factura.model';
import { FacturaFiltro } from '../../models/reporte.model';
import { GenerarDeResponse } from '../../models/documento-electronico.model';

@Injectable({
  providedIn: 'root'
})
export class FacturaApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/facturas`;

  getAll(filtro?: FacturaFiltro): Observable<FacturaLegal[]> {
    let params = new HttpParams();

    // El backend requiere empresaId como parámetro obligatorio
    if (filtro?.empresaId) {
      params = params.set('empresaId', filtro.empresaId.toString());
    }

    // Parámetros de paginación (valores por defecto para obtener todas las facturas)
    params = params.set('page', '0');
    params = params.set('size', '1000'); // Número grande para obtener todas
    params = params.set('sortBy', 'fecha');
    params = params.set('sortDirection', 'DESC');

    if (filtro) {
      if (filtro.fechaDesde) params = params.set('fechaDesde', filtro.fechaDesde);
      if (filtro.fechaHasta) params = params.set('fechaHasta', filtro.fechaHasta);
      if (filtro.clienteId) params = params.set('clienteId', filtro.clienteId.toString());
      if (filtro.estado) params = params.set('estado', filtro.estado);
      if (filtro.montoMinimo) params = params.set('montoMinimo', filtro.montoMinimo.toString());
      if (filtro.montoMaximo) params = params.set('montoMaximo', filtro.montoMaximo.toString());
    }

    // El backend retorna Page<FacturaLegalDto>, necesitamos extraer el content
    return this.http.get<{ content: FacturaLegal[]; totalElements: number; totalPages: number }>(this.baseUrl, { params }).pipe(
      map(response => response.content || [])
    );
  }

  getById(id: number): Observable<FacturaLegal> {
    return this.http.get<FacturaLegal>(`${this.baseUrl}/${id}`);
  }

  create(factura: Partial<FacturaLegal>): Observable<FacturaLegal> {
    return this.http.post<FacturaLegal>(this.baseUrl, factura);
  }

  update(id: number, factura: Partial<FacturaLegal>): Observable<FacturaLegal> {
    return this.http.put<FacturaLegal>(`${this.baseUrl}/${id}`, factura);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  generarDE(facturaId: number): Observable<GenerarDeResponse> {
    return this.http.post<GenerarDeResponse>(`${this.baseUrl}/${facturaId}/generar-de`, {});
  }

  desvincularDE(facturaId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${facturaId}/desvincular-de`, {});
  }

  descargarPDF(facturaId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${facturaId}/pdf`, {
      responseType: 'blob'
    });
  }

  descargarPdfKude(facturaId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${facturaId}/kude-pdf`, {
      responseType: 'blob'
    });
  }

  getResumen(empresaId: number, fechaDesde?: string, fechaHasta?: string): Observable<ResumenFacturas> {
    let params = new HttpParams().set('empresaId', empresaId.toString());
    if (fechaDesde) params = params.set('fechaDesde', fechaDesde);
    if (fechaHasta) params = params.set('fechaHasta', fechaHasta);
    return this.http.get<ResumenFacturas>(`${this.baseUrl}/resumen`, { params });
  }

  reenviarEmail(facturaId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${facturaId}/reenviar-email`, {});
  }
}

export interface ResumenFacturas {
  cantidadFacturasAprobadas: number;
  totalFacturasAprobadas: number;
  totalIva10Aprobadas: number;
  totalIva5Aprobadas: number;
  totalExentasAprobadas: number;
  cantidadFacturasNoAprobadas: number;
  totalFacturasNoAprobadas: number;
}
