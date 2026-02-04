import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, of, from } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { environment } from '../../environments/environment';
import { AuthResponse } from '../models/auth-response.model';
import { LoginRequest } from '../models/login-request.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private auth0 = inject(Auth0Service);
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
    
    // También cerrar sesión de Auth0 si está activo
    this.auth0.logout({
      logoutParams: {
        returnTo: window.location.origin
      }
    }).subscribe();
    
    return of(undefined);
  }

  isAuthenticated(): boolean {
    const localToken = this.getToken();
    if (localToken) {
      return true;
    }
    // Verificar si Auth0 está autenticado (síncrono, puede no ser 100% preciso)
    // Para verificación precisa, usar isAuthenticated$ observable
    return false;
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Obtener token (local o Auth0)
   * Retorna un Observable porque Auth0 puede requerir llamadas asíncronas
   */
  getTokenAsync(): Observable<string | null> {
    const localToken = this.getToken();
    if (localToken) {
      return of(localToken);
    }
    
    // Intentar obtener token de Auth0
    return from(this.auth0.getAccessTokenSilently({ 
      authorizationParams: {
        audience: environment.auth0.authorizationParams.audience
      }
    })).pipe(
      tap(token => {
        // Guardar token de Auth0 en localStorage para uso futuro
        if (token) {
          localStorage.setItem(this.TOKEN_KEY, token);
        }
      }),
      catchError(() => of(null))
    );
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  // Método para actualizar el usuario actual desde NgRx
  updateCurrentUser(user: User): void {
    this.currentUserSubject.next(user);
  }

  private handleAuthResponse(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);

    // Mapear los campos del backend a los nombres del frontend
    const mappedUser = this.mapBackendUserToFrontend(response.usuario);
    localStorage.setItem(this.USER_KEY, JSON.stringify(mappedUser));
    this.currentUserSubject.next(mappedUser);
  }

  mapBackendUserToFrontend(backendUser: any): User {
    return {
      id: backendUser.id,
      username: backendUser.username,
      email: backendUser.email,
      auth0Id: backendUser.auth0Id,
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
