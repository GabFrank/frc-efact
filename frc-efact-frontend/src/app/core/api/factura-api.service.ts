import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FacturaLegal } from '../../models/factura.model';
import { FacturaFiltro } from '../../models/reporte.model';

@Injectable({
  providedIn: 'root'
})
export class FacturaApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/facturas`;

  getAll(filtro?: FacturaFiltro): Observable<FacturaLegal[]> {
    let params = new HttpParams();
    if (filtro) {
      if (filtro.empresaId) params = params.set('empresaId', filtro.empresaId.toString());
      if (filtro.fechaDesde) params = params.set('fechaDesde', filtro.fechaDesde);
      if (filtro.fechaHasta) params = params.set('fechaHasta', filtro.fechaHasta);
      if (filtro.clienteId) params = params.set('clienteId', filtro.clienteId.toString());
      if (filtro.estado) params = params.set('estado', filtro.estado);
      if (filtro.montoMinimo) params = params.set('montoMinimo', filtro.montoMinimo.toString());
      if (filtro.montoMaximo) params = params.set('montoMaximo', filtro.montoMaximo.toString());
    }
    return this.http.get<FacturaLegal[]>(this.baseUrl, { params });
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

  generarDE(facturaId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${facturaId}/generar-de`, {});
  }

  descargarPDF(facturaId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${facturaId}/pdf`, {
      responseType: 'blob'
    });
  }
}
