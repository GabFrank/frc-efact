import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DocumentoElectronico, LoteDE } from '../../models/documento-electronico.model';

export interface InutilizarRequest {
  establecimiento: string;
  puntoExpedicion: string;
  numeroInicio: number;
  numeroFin: number;
  tipoDE: string;
  motivo: string;
  timbradoDetalleId?: number;
}

export interface EventoCancelacionDE {
  id: number;
  documentoId?: number;
  eventoId: string;
  cdcDocumento: string;
  estado: string;
  codigoRespuesta?: string;
  mensajeRespuesta?: string;
  protocoloAutorizacion?: string;
  fechaFirma?: string;
  fechaProcesamiento?: string;
}

export interface EventoNominacionDE {
  id: number;
  documentoId?: number;
  clienteId?: number;
  eventoId: string;
  cdcDocumento: string;
  nombreReceptor: string;
  documentoReceptor: string;
  tipoReceptor: string;
  totalFactura?: number;
  estado: string;
  codigoRespuesta?: string;
  mensajeRespuesta?: string;
  protocoloAutorizacion?: string;
  fechaFirma?: string;
  fechaProcesamiento?: string;
}

export interface EventoInutilizacionDE {
  id: number;
  timbradoId?: number;
  timbradoDetalleId?: number;
  eventoId: string;
  establecimiento: string;
  puntoExpedicion: string;
  numeroInicio: number;
  numeroFin: number;
  tipoDE: string;
  motivoInutilizacion: string;
  estado: string;
  codigoRespuesta?: string;
  mensajeRespuesta?: string;
  protocoloAutorizacion?: string;
  fechaFirma?: string;
  fechaProcesamiento?: string;
}

@Injectable({
  providedIn: 'root'
})
export class SifenApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/sifen`;

  enviarLote(loteId: number): Observable<LoteDE> {
    return this.http.post<LoteDE>(`${this.baseUrl}/lotes/${loteId}/enviar`, {});
  }

  consultarLote(loteId: number): Observable<LoteDE> {
    return this.http.post<LoteDE>(`${this.baseUrl}/lotes/${loteId}/consultar`, {});
  }

  consultarDocumento(cdc: string): Observable<DocumentoElectronico> {
    return this.http.post<DocumentoElectronico>(`${this.baseUrl}/documentos/${cdc}/consultar`, {});
  }

  cancelarDocumento(cdc: string, motivo: string): Observable<EventoCancelacionDE> {
    return this.http.post<EventoCancelacionDE>(`${this.baseUrl}/documentos/${cdc}/cancelar`, { motivo });
  }

  nominarDocumento(cdc: string, clienteId: number): Observable<EventoNominacionDE> {
    return this.http.post<EventoNominacionDE>(`${this.baseUrl}/documentos/${cdc}/nominar`, { clienteId });
  }

  inutilizarNumeros(timbradoId: number, request: InutilizarRequest): Observable<EventoInutilizacionDE> {
    return this.http.post<EventoInutilizacionDE>(`${this.baseUrl}/timbrados/${timbradoId}/inutilizar`, request);
  }

  reenviarDEEnNuevoLote(deId: number): Observable<LoteDE> {
    return this.http.post<LoteDE>(`${this.baseUrl}/documentos/${deId}/reenviar`, {});
  }

  obtenerDocumentoPorFactura(facturaId: number): Observable<DocumentoElectronico> {
    return this.http.get<DocumentoElectronico>(`${this.baseUrl}/documentos/factura/${facturaId}`);
  }

  obtenerDocumentoPorNotaCredito(notaCreditoId: number): Observable<DocumentoElectronico> {
    return this.http.get<DocumentoElectronico>(`${this.baseUrl}/documentos/nota-credito/${notaCreditoId}`);
  }

  listarEventosCancelacion(filtros: EventoCancelacionFiltros): Observable<PageResponse<EventoCancelacionDE>> {
    let params = new HttpParams();
    
    if (filtros.empresaId) params = params.set('empresaId', filtros.empresaId.toString());
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.cdcDocumento) params = params.set('cdcDocumento', filtros.cdcDocumento);
    if (filtros.fechaInicio) params = params.set('fechaInicio', filtros.fechaInicio);
    if (filtros.fechaFin) params = params.set('fechaFin', filtros.fechaFin);
    if (filtros.page !== undefined) params = params.set('page', filtros.page.toString());
    if (filtros.size !== undefined) params = params.set('size', filtros.size.toString());
    if (filtros.sort) params = params.set('sort', filtros.sort);
    
    return this.http.get<PageResponse<EventoCancelacionDE>>(`${this.baseUrl}/eventos/cancelacion`, { params });
  }

  listarEventosNominacion(filtros: EventoNominacionFiltros): Observable<PageResponse<EventoNominacionDE>> {
    let params = new HttpParams();
    
    if (filtros.empresaId) params = params.set('empresaId', filtros.empresaId.toString());
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.cdcDocumento) params = params.set('cdcDocumento', filtros.cdcDocumento);
    if (filtros.nombreReceptor) params = params.set('nombreReceptor', filtros.nombreReceptor);
    if (filtros.fechaInicio) params = params.set('fechaInicio', filtros.fechaInicio);
    if (filtros.fechaFin) params = params.set('fechaFin', filtros.fechaFin);
    if (filtros.page !== undefined) params = params.set('page', filtros.page.toString());
    if (filtros.size !== undefined) params = params.set('size', filtros.size.toString());
    if (filtros.sort) params = params.set('sort', filtros.sort);
    
    return this.http.get<PageResponse<EventoNominacionDE>>(`${this.baseUrl}/eventos/nominacion`, { params });
  }

  listarEventosInutilizacion(filtros: EventoInutilizacionFiltros): Observable<PageResponse<EventoInutilizacionDE>> {
    let params = new HttpParams();
    
    if (filtros.empresaId) params = params.set('empresaId', filtros.empresaId.toString());
    if (filtros.timbradoId) params = params.set('timbradoId', filtros.timbradoId.toString());
    if (filtros.estado) params = params.set('estado', filtros.estado);
    if (filtros.fechaInicio) params = params.set('fechaInicio', filtros.fechaInicio);
    if (filtros.fechaFin) params = params.set('fechaFin', filtros.fechaFin);
    if (filtros.page !== undefined) params = params.set('page', filtros.page.toString());
    if (filtros.size !== undefined) params = params.set('size', filtros.size.toString());
    if (filtros.sort) params = params.set('sort', filtros.sort);
    
    return this.http.get<PageResponse<EventoInutilizacionDE>>(`${this.baseUrl}/eventos/inutilizacion`, { params });
  }
}

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

export interface EventoCancelacionFiltros {
  empresaId?: number;
  estado?: string | null;
  cdcDocumento?: string;
  fechaInicio?: string;
  fechaFin?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface EventoNominacionFiltros {
  empresaId?: number;
  estado?: string | null;
  cdcDocumento?: string;
  nombreReceptor?: string;
  fechaInicio?: string;
  fechaFin?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface EventoInutilizacionFiltros {
  empresaId?: number;
  timbradoId?: number;
  estado?: string | null;
  fechaInicio?: string;
  fechaFin?: string;
  page?: number;
  size?: number;
  sort?: string;
}

