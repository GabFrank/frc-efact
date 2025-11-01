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

  create(empresa: Partial<Empresa>, certificadoFile?: File, certificadoPassword?: string): Observable<Empresa> {
    // Si hay certificado, usar FormData (multipart)
    if (certificadoFile && certificadoPassword) {
      const formData = new FormData();
      formData.append('empresa', new Blob([JSON.stringify(empresa)], { type: 'application/json' }));
      formData.append('certificadoFile', certificadoFile);
      formData.append('certificadoPassword', certificadoPassword);

      return this.http.post<Empresa>(this.baseUrl, formData);
    }
    // Si no hay certificado, enviar JSON normal (Spring detectará automáticamente el Content-Type)
    return this.http.post<Empresa>(this.baseUrl, empresa, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  update(id: number, empresa: Partial<Empresa>, certificadoFile?: File, certificadoPassword?: string): Observable<Empresa> {
    // Si hay certificado, usar FormData (multipart)
    if (certificadoFile && certificadoPassword) {
      const formData = new FormData();
      formData.append('empresa', new Blob([JSON.stringify(empresa)], { type: 'application/json' }));
      formData.append('certificadoFile', certificadoFile);
      formData.append('certificadoPassword', certificadoPassword);

      return this.http.put<Empresa>(`${this.baseUrl}/${id}`, formData);
    }
    // Si no hay certificado, enviar JSON normal (Spring detectará automáticamente el Content-Type)
    return this.http.put<Empresa>(`${this.baseUrl}/${id}`, empresa, {
      headers: { 'Content-Type': 'application/json' }
    });
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

  /**
   * Sube un certificado PFX para una empresa
   */
  uploadCertificado(empresaId: number, file: File, password: string): Observable<{ certificadoPath: string; fechaExpiracion: string | null; mensaje: string }> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('password', password);

    return this.http.post<{ certificadoPath: string; fechaExpiracion: string | null; mensaje: string }>(
      `${this.baseUrl}/${empresaId}/certificado`,
      formData
    );
  }

  /**
   * Actualiza solo la contraseña del certificado existente
   */
  actualizarPasswordCertificado(empresaId: number, nuevaPassword: string): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/${empresaId}/certificado/password`,
      { password: nuevaPassword }
    );
  }
}
