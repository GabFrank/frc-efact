import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  FacturaReporte,
  ClienteRanking,
  ProductoReporte,
  UsuarioReporte,
  FacturaFiltro
} from '../../models/reporte.model';

@Injectable({
  providedIn: 'root'
})
export class ReporteApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/reportes`;

  reporteFacturas(filtro?: FacturaFiltro): Observable<FacturaReporte[]> {
    let params = new HttpParams();
    if (filtro) {
      if (filtro.empresaId) params = params.set('empresaId', filtro.empresaId.toString());
      if (filtro.fechaDesde) params = params.set('fechaDesde', filtro.fechaDesde);
      if (filtro.fechaHasta) params = params.set('fechaHasta', filtro.fechaHasta);
      if (filtro.clienteId) params = params.set('clienteId', filtro.clienteId.toString());
    }
    return this.http.get<FacturaReporte[]>(`${this.baseUrl}/facturas`, { params });
  }

  reporteClientes(empresaId: number, fechaDesde?: string, fechaHasta?: string): Observable<ClienteRanking[]> {
    let params = new HttpParams().set('empresaId', empresaId.toString());
    if (fechaDesde) params = params.set('fechaDesde', fechaDesde);
    if (fechaHasta) params = params.set('fechaHasta', fechaHasta);
    return this.http.get<ClienteRanking[]>(`${this.baseUrl}/clientes`, { params });
  }

  reporteProductos(empresaId: number, fechaDesde?: string, fechaHasta?: string): Observable<ProductoReporte[]> {
    let params = new HttpParams().set('empresaId', empresaId.toString());
    if (fechaDesde) params = params.set('fechaDesde', fechaDesde);
    if (fechaHasta) params = params.set('fechaHasta', fechaHasta);
    return this.http.get<ProductoReporte[]>(`${this.baseUrl}/productos`, { params });
  }

  reporteUsuarios(empresaId: number, fechaDesde?: string, fechaHasta?: string): Observable<UsuarioReporte[]> {
    let params = new HttpParams().set('empresaId', empresaId.toString());
    if (fechaDesde) params = params.set('fechaDesde', fechaDesde);
    if (fechaHasta) params = params.set('fechaHasta', fechaHasta);
    return this.http.get<UsuarioReporte[]>(`${this.baseUrl}/usuarios`, { params });
  }

  exportarExcel(tipo: string, filtro?: any): Observable<Blob> {
    let params = new HttpParams();
    if (filtro) {
      Object.keys(filtro).forEach(key => {
        if (filtro[key]) {
          params = params.set(key, filtro[key].toString());
        }
      });
    }
    return this.http.get(`${this.baseUrl}/${tipo}/excel`, {
      params,
      responseType: 'blob'
    });
  }

  exportarPDF(tipo: string, filtro?: any): Observable<Blob> {
    let params = new HttpParams();
    if (filtro) {
      Object.keys(filtro).forEach(key => {
        if (filtro[key]) {
          params = params.set(key, filtro[key].toString());
        }
      });
    }
    return this.http.get(`${this.baseUrl}/${tipo}/pdf`, {
      params,
      responseType: 'blob'
    });
  }
}
