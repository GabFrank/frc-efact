import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../environments/environment';

/**
 * Interceptor para asegurar que todas las requests usen HTTPS en producción
 */
export const httpsInterceptor: HttpInterceptorFn = (req, next) => {
  // Solo aplicar en producción
  if (environment.production && environment.enableHttps) {
    // Si la URL comienza con http:// (no https://), convertirla a https://
    if (req.url.startsWith('http://')) {
      const httpsReq = req.clone({
        url: req.url.replace('http://', 'https://')
      });
      return next(httpsReq);
    }
  }
  
  return next(req);
};
