import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
  eventoId: string;
  cdcDocumento: string;
  motivoCancelacion: string;
  estado: string;
  codigoRespuesta?: string;
  mensajeRespuesta?: string;
  protocoloAutorizacion?: string;
  activo: boolean;
}

export interface EventoNominacionDE {
  id: number;
  eventoId: string;
  cdcDocumento: string;
  clienteId: number;
  nombreReceptor: string;
  documentoReceptor: string;
  tipoReceptor: string;
  estado: string;
  codigoRespuesta?: string;
  mensajeRespuesta?: string;
  protocoloAutorizacion?: string;
  activo: boolean;
}

export interface EventoInutilizacionDE {
  id: number;
  eventoId: string;
  timbradoId: number;
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
  activo: boolean;
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
}

