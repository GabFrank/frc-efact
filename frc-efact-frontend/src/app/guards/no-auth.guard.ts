import { inject } from '@angular/core';
import { Router, CanActivateFn, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AuthService as Auth0Service } from '@auth0/auth0-angular';
import { map, take } from 'rxjs/operators';

export const noAuthGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const auth0 = inject(Auth0Service);
  const router = inject(Router);

  // Verificar autenticación local primero
  if (authService.isAuthenticated()) {
    return router.createUrlTree(['/dashboard']);
  }

  // Verificar Auth0
  return auth0.isAuthenticated$.pipe(
    take(1),
    map(isAuthenticated =>
      isAuthenticated ? router.createUrlTree(['/dashboard']) : true
    )
  );
};
