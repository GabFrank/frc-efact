import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DashboardUsuarioDto {
  usuarioId: number;
  username: string;
  cantidadEmpresas: number;
  ultimoAcceso: string;
  facturasCreadasMesActual: number;
  ultimasActividades: ActividadRecienteDto[];
}

export interface ActividadRecienteDto {
  id: number;
  accion: string;
  entidadTipo: string;
  entidadId: number;
  descripcion: string;
  fechaHora: string;
  empresaNombre: string | null;
}

export interface DashboardEmpresaDto {
  empresaId: number;
  razonSocial: string;
  totalFacturasEmitidas: number;
  totalGuaraniesMesActual: number;
  totalesPorIva: TotalesPorIvaDto;
  top10Clientes: ClienteRankingDto[];
}

export interface TotalesPorIvaDto {
  totalIva10: number;
  totalIva5: number;
  totalIva0: number;
}

export interface ClienteRankingDto {
  clienteId: number;
  nombre: string;
  ruc: string;
  cantidadFacturas: number;
  montoTotal: number;
}

export interface DashboardGeneralDto {
  totalEmpresas: number;
  totalUsuarios: number;
  totalDocumentos: number;
  actividadHoy: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getDashboardUsuario(usuarioId: number): Observable<DashboardUsuarioDto> {
    return this.http.get<DashboardUsuarioDto>(`${this.apiUrl}/usuario/${usuarioId}`);
  }

  getDashboardEmpresa(empresaId: number, fechaDesde?: string, fechaHasta?: string): Observable<DashboardEmpresaDto> {
    let params = new HttpParams();
    if (fechaDesde) {
      params = params.set('fechaDesde', fechaDesde);
    }
    if (fechaHasta) {
      params = params.set('fechaHasta', fechaHasta);
    }
    return this.http.get<DashboardEmpresaDto>(`${this.apiUrl}/empresa/${empresaId}`, { params });
  }

  getDashboardGeneral(): Observable<DashboardGeneralDto> {
    return this.http.get<DashboardGeneralDto>(`${this.apiUrl}/general`);
  }
}
