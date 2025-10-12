import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Empresa } from '../../models/empresa.model';
import { User, UsuarioEmpresa, AsignarUsuarioEmpresaRequest } from '../../models/user.model';

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

  getUsuariosDisponibles(empresaId: number): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/${empresaId}/usuarios/disponibles`);
  }

  asignarUsuarioEmpresa(request: AsignarUsuarioEmpresaRequest): Observable<UsuarioEmpresa> {
    return this.http.post<UsuarioEmpresa>(
      `${this.baseUrl}/${request.empresaId}/usuarios`,
      request
    );
  }

  actualizarRolUsuarioEmpresa(empresaId: number, usuarioEmpresaId: number, nuevoRol: string): Observable<UsuarioEmpresa> {
    return this.http.put<UsuarioEmpresa>(
      `${this.baseUrl}/${empresaId}/usuarios/${usuarioEmpresaId}/rol`,
      { rolEmpresa: nuevoRol }
    );
  }

  toggleUsuarioEmpresaActivo(empresaId: number, usuarioEmpresaId: number): Observable<UsuarioEmpresa> {
    return this.http.patch<UsuarioEmpresa>(
      `${this.baseUrl}/${empresaId}/usuarios/${usuarioEmpresaId}/toggle-activo`,
      {}
    );
  }

  removerUsuarioEmpresa(empresaId: number, usuarioEmpresaId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${empresaId}/usuarios/${usuarioEmpresaId}`
    );
  }
}
