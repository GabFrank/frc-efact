import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { switchMap } from 'rxjs/operators';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  
  // Primero intentar con token local (más rápido)
  const localToken = authService.getToken();

  if (localToken) {
    const clonedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${localToken}`
      }
    });
    return next(clonedReq);
  }

  // Si no hay token local, intentar obtener de Auth0 (asíncrono)
  return authService.getTokenAsync().pipe(
    switchMap(token => {
      if (token) {
        const clonedReq = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        });
        return next(clonedReq);
      }
      return next(req);
    })
  );
};
