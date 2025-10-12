import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Timbrado, TimbradoDetalle } from '../../models/timbrado.model';

@Injectable({
  providedIn: 'root'
})
export class TimbradoApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/timbrados`;
  private readonly detalleUrl = `${environment.apiUrl}/timbrados-detalle`;

  // Timbrado endpoints
  getAll(): Observable<Timbrado[]> {
    return this.http.get<Timbrado[]>(this.baseUrl);
  }

  getById(id: number): Observable<Timbrado> {
    return this.http.get<Timbrado>(`${this.baseUrl}/${id}`);
  }

  getByEmpresa(empresaId: number): Observable<Timbrado[]> {
    return this.http.get<Timbrado[]>(`${this.baseUrl}/empresa/${empresaId}`);
  }

  create(timbrado: Partial<Timbrado>): Observable<Timbrado> {
    return this.http.post<Timbrado>(this.baseUrl, timbrado);
  }

  update(id: number, timbrado: Partial<Timbrado>): Observable<Timbrado> {
    return this.http.put<Timbrado>(`${this.baseUrl}/${id}`, timbrado);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  verificarVigencia(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/${id}/vigente`);
  }

  // Timbrado Detalle endpoints
  getAllDetalles(): Observable<TimbradoDetalle[]> {
    return this.http.get<TimbradoDetalle[]>(this.detalleUrl);
  }

  getDetalleById(id: number): Observable<TimbradoDetalle> {
    return this.http.get<TimbradoDetalle>(`${this.detalleUrl}/${id}`);
  }

  getDetallesByTimbrado(timbradoId: number): Observable<TimbradoDetalle[]> {
    return this.http.get<TimbradoDetalle[]>(`${this.detalleUrl}/timbrado/${timbradoId}`);
  }

  createDetalle(detalle: Partial<TimbradoDetalle>): Observable<TimbradoDetalle> {
    return this.http.post<TimbradoDetalle>(this.detalleUrl, detalle);
  }

  updateDetalle(id: number, detalle: Partial<TimbradoDetalle>): Observable<TimbradoDetalle> {
    return this.http.put<TimbradoDetalle>(`${this.detalleUrl}/${id}`, detalle);
  }

  deleteDetalle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.detalleUrl}/${id}`);
  }

  getDetallesByEmpresa(empresaId: number): Observable<TimbradoDetalle[]> {
    return this.http.get<TimbradoDetalle[]>(`${this.detalleUrl}/empresa/${empresaId}`);
  }

  verificarDisponibilidad(id: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.detalleUrl}/${id}/disponible`);
  }
}
