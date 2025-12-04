import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, UpdateProfileRequest, ChangePasswordRequest } from '../../models/user.model';
import { AuditLogPage } from '../../models/audit.model';

@Injectable({
  providedIn: 'root'
})
export class ProfileApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/perfil`;

  /**
   * Obtener el perfil del usuario actual
   */
  getProfile(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/usuarios/perfil`);
  }

  /**
   * Actualizar el perfil del usuario actual
   */
  updateProfile(data: UpdateProfileRequest): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/actualizar`, data);
  }

  /**
   * Cambiar la contraseña del usuario actual
   */
  changePassword(data: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/cambiar-password`, data);
  }

  /**
   * Vincular cuenta Auth0
   */
  linkAuth0Account(auth0Id: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/vincular-auth0`, { auth0Id });
  }

  /**
   * Desvincular cuenta Auth0
   */
  unlinkAuth0Account(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/desvincular-auth0`, {});
  }

  /**
   * Obtener actividad del usuario con paginación
   */
  getActivity(page: number = 0, size: number = 20): Observable<AuditLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<AuditLogPage>(`${this.baseUrl}/actividad`, { params });
  }
}

