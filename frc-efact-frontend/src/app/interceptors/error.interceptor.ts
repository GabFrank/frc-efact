import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 401:
            errorMessage = 'Unauthorized. Please login again.';
            authService.logout();
            // Reiniciar la aplicación para limpiar completamente el estado
            window.location.href = '/login';
            break;
          case 403:
            errorMessage = 'Access forbidden.';
            break;
          case 404:
            errorMessage = 'Resource not found.';
            break;
          case 500:
            errorMessage = 'Internal server error.';
            break;
          default:
            errorMessage = error.error?.message || `Error Code: ${error.status}`;
        }
      }

      // Para errores 400 (validación), preservar el error original para que los componentes puedan acceder a los detalles
      if (error.status === 400 && error.error) {
        // Mantener el HttpErrorResponse para que los componentes puedan acceder a error.error.errors
        return throwError(() => error);
      }

      // Para otros errores, usar el mensaje simplificado pero preservar el error original si es necesario
      return throwError(() => {
        const newError: any = new Error(errorMessage);
        newError.originalError = error;
        newError.status = error.status;
        newError.statusText = error.statusText;
        newError.error = error.error;
        return newError;
      });
    })
  );
};
