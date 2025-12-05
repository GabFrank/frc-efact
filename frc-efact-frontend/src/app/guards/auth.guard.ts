import { inject } from '@angular/core';
import { Router, CanActivateFn, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { map, take, switchMap } from 'rxjs/operators';
import { of, Observable } from 'rxjs';

export const authGuard: CanActivateFn = (route, state): Observable<boolean | UrlTree> | boolean | UrlTree => {
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
          map(token => token ? true : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } }))
        );
      }
      // Redirigir al login cuando no está autenticado
      return of(router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } }));
    })
  );
};
