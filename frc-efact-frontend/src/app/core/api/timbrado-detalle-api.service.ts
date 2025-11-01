import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TimbradoDetalle } from '../../models/timbrado.model';

@Injectable({
  providedIn: 'root'
})
export class TimbradoDetalleApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}`;

  /**
   * Obtiene todos los detalles de un timbrado.
   */
  getByTimbrado(timbradoId: number): Observable<TimbradoDetalle[]> {
    return this.http.get<TimbradoDetalle[]>(`${this.baseUrl}/timbrados/${timbradoId}/detalles`);
  }

  /**
   * Obtiene detalles activos de un timbrado.
   */
  getActivosByTimbrado(timbradoId: number): Observable<TimbradoDetalle[]> {
    return this.http.get<TimbradoDetalle[]>(`${this.baseUrl}/timbrados/${timbradoId}/detalles/activos`);
  }

  /**
   * Obtiene un detalle por ID.
   */
  getById(id: number): Observable<TimbradoDetalle> {
    return this.http.get<TimbradoDetalle>(`${this.baseUrl}/timbrado-detalles/${id}`);
  }

  /**
   * Crea un nuevo detalle de timbrado.
   */
  create(timbradoId: number, detalle: Partial<TimbradoDetalle>): Observable<TimbradoDetalle> {
    return this.http.post<TimbradoDetalle>(`${this.baseUrl}/timbrados/${timbradoId}/detalles`, detalle);
  }

  /**
   * Actualiza un detalle existente.
   */
  update(id: number, detalle: Partial<TimbradoDetalle>): Observable<TimbradoDetalle> {
    return this.http.put<TimbradoDetalle>(`${this.baseUrl}/timbrado-detalles/${id}`, detalle);
  }

  /**
   * Desactiva un detalle (soft delete).
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/timbrado-detalles/${id}`);
  }

  /**
   * Obtiene detalles que están por agotarse.
   */
  getPorAgotarse(timbradoId: number, umbralPorcentaje: number = 80): Observable<TimbradoDetalle[]> {
    return this.http.get<TimbradoDetalle[]>(
      `${this.baseUrl}/timbrados/${timbradoId}/detalles/por-agotarse?umbralPorcentaje=${umbralPorcentaje}`
    );
  }
}







