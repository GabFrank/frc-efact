import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DocumentoElectronico, LoteDE, EstadoDE } from '../../models/documento-electronico.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentoElectronicoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/documentos-electronicos`;
  private readonly loteUrl = `${environment.apiUrl}/lotes`;

  // Documento Electrónico endpoints
  getAll(estado?: EstadoDE): Observable<DocumentoElectronico[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }
    return this.http.get<DocumentoElectronico[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<DocumentoElectronico> {
    return this.http.get<DocumentoElectronico>(`${this.baseUrl}/${id}`);
  }

  consultarEstado(id: number): Observable<DocumentoElectronico> {
    return this.http.post<DocumentoElectronico>(`${this.baseUrl}/${id}/consultar`, {});
  }

  cancelar(id: number, motivo: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/cancelar`, { motivo });
  }

  descargarXML(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/xml`, {
      responseType: 'blob'
    });
  }

  // Lote endpoints
  getAllLotes(): Observable<LoteDE[]> {
    return this.http.get<LoteDE[]>(this.loteUrl);
  }

  getLoteById(id: number): Observable<LoteDE> {
    return this.http.get<LoteDE>(`${this.loteUrl}/${id}`);
  }

  crearLote(documentoIds: number[]): Observable<LoteDE> {
    return this.http.post<LoteDE>(this.loteUrl, { documentoIds });
  }

  enviarLote(id: number): Observable<any> {
    return this.http.post(`${this.loteUrl}/${id}/enviar`, {});
  }

  consultarEstadoLote(id: number): Observable<LoteDE> {
    return this.http.get<LoteDE>(`${this.loteUrl}/${id}/consultar-estado`);
  }
}
