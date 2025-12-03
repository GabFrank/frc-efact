import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { map, take, switchMap } from 'rxjs/operators';
import { of, Observable } from 'rxjs';

export const authGuard: CanActivateFn = (route, state): Observable<boolean> | boolean => {
  const authService = inject(AuthService);
  const auth0 = inject(Auth0Service);
  const router = inject(Router);

  // Verificar autenticación local primero
  if (authService.isAuthenticated()) {
    return true;
  }

  // Verificar Auth0 y asegurar que el token esté disponible
  return auth0.isAuthenticated$.pipe(
    take(1),
    switchMap(isAuthenticated => {
      if (isAuthenticated) {
        // Si Auth0 está autenticado, verificar que el token esté guardado
        return authService.getTokenAsync().pipe(
          map(token => {
            if (token) {
              return true;
            }
            // Si no hay token, redirigir al login
            router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
            return false;
          })
        );
      }
  // Redirect to login page with return url
  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return of(false);
    })
  );
};
