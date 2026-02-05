import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { filter, take } from 'rxjs/operators';
import * as AuthActions from './core/state/auth/auth.actions';
import { AuthService } from './services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'FRC eFACT';
  private readonly store = inject(Store);
  private readonly auth0 = inject(Auth0Service);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);

  ngOnInit(): void {
    // Inicializar auth desde localStorage al cargar la aplicación
    this.store.dispatch(AuthActions.initializeAuth());

    // Manejar callback de Auth0 SOLO si la URL contiene parámetros de callback de Auth0
    // Esto evita que se active cuando el usuario hace login tradicional
    const urlParams = new URLSearchParams(window.location.search);
    const hasAuth0Callback = urlParams.has('code') || urlParams.has('state') || window.location.hash.includes('access_token');

    if (hasAuth0Callback) {
      // Solo manejar callback si hay parámetros de Auth0 en la URL
      this.auth0.isAuthenticated$.pipe(
        filter(isAuthenticated => isAuthenticated === true),
        take(1)
      ).subscribe(() => {
        // Solo manejar callback si no hay token local (para evitar loops)
        if (!this.authService.isAuthenticated()) {
          // Esperar un poco para que el SDK procese completamente el callback
          setTimeout(() => {
            this.handleAuth0Callback();
          }, 1000);
        }
      });
    }
  }

  private handleAuth0Callback(): void {
    this.auth0.getAccessTokenSilently({
      authorizationParams: {
        audience: environment.auth0.authorizationParams.audience
      }
    }).subscribe({
      next: (token) => {
        // Guardar el token de Auth0 en localStorage INMEDIATAMENTE
        localStorage.setItem('auth_token', token);
        
        // Obtener información del usuario desde el backend usando el interceptor
        // Usar el token directamente en el header para asegurar que funcione
        this.http.get(`${environment.apiUrl}/usuarios/perfil`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }).subscribe({
          next: (user: any) => {
            // Mapear y guardar usuario
            const mappedUser = this.authService.mapBackendUserToFrontend(user);
            
            localStorage.setItem('current_user', JSON.stringify(mappedUser));
            this.authService.updateCurrentUser(mappedUser);
            
            // IMPORTANTE: Despachar loginSuccess al store de NgRx para que MainLayoutComponent tenga acceso al usuario
            this.store.dispatch(AuthActions.loginSuccess({
              user: mappedUser,
              token: token,
              refreshToken: '' // Auth0 no usa refresh token de la misma forma
            }));
            
            // Esperar un momento para asegurar que todo esté guardado antes de navegar
            setTimeout(() => {
              // Redirigir al dashboard
              this.router.navigate(['/dashboard']);
            }, 200);
          },
          error: (err) => {
            console.error('Error obteniendo perfil del backend:', err);
            console.error('Status:', err.status);
            console.error('Error completo:', err);
            // Si hay un error, limpiar y redirigir al login
            localStorage.removeItem('auth_token');
            this.router.navigate(['/login'], { 
              queryParams: { error: 'Error al obtener perfil. Por favor, intente nuevamente.' } 
            });
          }
        });
      },
      error: (err) => {
        console.error('Error obteniendo token de Auth0:', err);
        // Si hay un error obteniendo el token, redirigir al login
        this.router.navigate(['/login'], { 
          queryParams: { error: 'Error de autenticación. Por favor, intente nuevamente.' } 
        });
      }
    });
  }
}
