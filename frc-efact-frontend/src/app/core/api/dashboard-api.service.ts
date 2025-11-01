import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardUsuario, DashboardEmpresa } from '../../models/dashboard.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DashboardApiService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getDashboardUsuario(): Observable<DashboardUsuario> {
    return this.http.get<DashboardUsuario>(`${this.apiUrl}/usuario`);
  }

  getDashboardEmpresa(empresaId: number, fechaInicio?: string, fechaFin?: string): Observable<DashboardEmpresa> {
    let params = new HttpParams();
    if (fechaInicio) {
      params = params.set('fechaInicio', fechaInicio);
    }
    if (fechaFin) {
      params = params.set('fechaFin', fechaFin);
    }
    
    return this.http.get<DashboardEmpresa>(`${this.apiUrl}/empresa/${empresaId}`, { params });
  }
}
