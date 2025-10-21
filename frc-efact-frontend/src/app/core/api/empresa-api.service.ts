import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Empresa } from '../../models/empresa.model';
import { User } from '../../models/user.model';
import { UsuarioEmpresa, AsignarUsuarioEmpresaRequest } from '../../models/usuario-empresa.model';

@Injectable({
  providedIn: 'root'
})
export class EmpresaApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/empresas`;

  getAll(): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(this.baseUrl);
  }

  getById(id: number): Observable<Empresa> {
    return this.http.get<Empresa>(`${this.baseUrl}/${id}`);
  }

  getMisEmpresas(): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(`${this.baseUrl}/mis-empresas`);
  }

  create(empresa: Partial<Empresa>): Observable<Empresa> {
    return this.http.post<Empresa>(this.baseUrl, empresa);
  }

  update(id: number, empresa: Partial<Empresa>): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.baseUrl}/${id}`, empresa);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  asignarUsuario(empresaId: number, usuarioId: number, rolEmpresa: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${empresaId}/usuarios`, {
      usuarioId,
      rolEmpresa
    });
  }

  // Usuario-Empresa management methods
  getUsuariosEmpresa(empresaId: number): Observable<UsuarioEmpresa[]> {
    return this.http.get<UsuarioEmpresa[]>(`${this.baseUrl}/${empresaId}/usuarios`);
  }

  asignarUsuarioEmpresa(empresaId: number, request: AsignarUsuarioEmpresaRequest): Observable<UsuarioEmpresa> {
    return this.http.post<UsuarioEmpresa>(`${this.baseUrl}/${empresaId}/usuarios`, request);
  }

  removeUsuarioEmpresa(empresaId: number, usuarioId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${empresaId}/usuarios/${usuarioId}`);
  }

  // Get users available for assignment to companies (excludes ADMIN)
  getUsuariosDisponibles(empresaId: number): Observable<User[]> {
    return this.http.get<User[]>(`${environment.apiUrl}/usuarios/asignables`);
  }
}
