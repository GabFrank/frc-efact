import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse } from '../models/auth-response.model';
import { LoginRequest } from '../models/login-request.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {}

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => this.handleAuthResponse(response))
      );
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
      .pipe(
        tap(response => this.handleAuthResponse(response))
      );
  }

  logout(): Observable<void> {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    return of(undefined);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private handleAuthResponse(response: AuthResponse): void {
    console.log('Respuesta de autenticación:', response);
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);

    // Mapear los campos del backend a los nombres del frontend
    const mappedUser = this.mapBackendUserToFrontend(response.usuario);
    localStorage.setItem(this.USER_KEY, JSON.stringify(mappedUser));
    this.currentUserSubject.next(mappedUser);
    console.log('Usuario guardado en localStorage:', mappedUser);
  }

  private mapBackendUserToFrontend(backendUser: any): User {
    return {
      id: backendUser.id,
      username: backendUser.username,
      email: backendUser.email,
      isActive: backendUser.isActive,
      roles: backendUser.roles || [],
      ultimoLogin: backendUser.ultimoLogin,
      creadoEn: backendUser.creadoEn,
      actualizadoEn: backendUser.actualizadoEn,
      failedLoginAttempts: backendUser.failedLoginAttempts || 0,
      lockedUntil: backendUser.lockedUntil,
      empresas: backendUser.empresas || [],
      createdBy: backendUser.createdBy,
      updatedBy: backendUser.updatedBy
    };
  }

  private getUserFromStorage(): User | null {
    try {
      const userJson = localStorage.getItem(this.USER_KEY);
      if (!userJson || userJson === 'undefined' || userJson === 'null') {
        return null;
      }
      return JSON.parse(userJson);
    } catch (error) {
      console.error('Error parsing user from storage:', error);
      localStorage.removeItem(this.USER_KEY);
      return null;
    }
  }
}
