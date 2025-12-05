import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError, tap } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ConnectionStatusService } from '../services/connection-status.service';
import { environment } from '../../environments/environment';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const connectionStatusService = inject(ConnectionStatusService);

  // Solo monitorear peticiones a la API del backend
  // Verificar si la URL contiene la URL base de la API o si es una ruta relativa que empieza con /api/
  const apiUrlBase = environment.apiUrl.replace('/api', ''); // Obtener la base sin /api
  const isApiRequest = req.url.includes(environment.apiUrl) || 
                       (req.url.startsWith('http') && req.url.includes(apiUrlBase)) ||
                       req.url.startsWith('/api/');

  return next(req).pipe(
    tap(() => {
      // Si la petición es exitosa a la API, el servidor está online
      if (isApiRequest) {
        connectionStatusService.setServerOnline();
      }
    }),
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An error occurred';

      // Solo monitorear errores de peticiones a la API
      if (isApiRequest) {
        // SOLO marcar como offline en errores de conexión reales, NO en errores HTTP normales
        
        // Status 0: Error de conexión real (sin respuesta del servidor, timeout, CORS, red)
        if (error.status === 0 || error.status === null || error.status === undefined) {
          // Verificar si hay conexión a internet
          if (navigator.onLine) {
            // Hay internet pero no se puede conectar al servidor
            connectionStatusService.setServerOffline('No se pudo conectar con el servidor');
          } else {
            // No hay internet, el servicio de conexión ya lo maneja
            connectionStatusService.setServerOffline('Sin conexión a internet');
          }
        } 
        // Errores 502, 503, 504: Servidor no disponible (gateway/proxy)
        else if (error.status === 502 || error.status === 503 || error.status === 504) {
          connectionStatusService.setServerOffline('El servidor no está disponible');
        }
        // Timeout (408): El servidor no responde a tiempo
        else if (error.status === 408) {
          connectionStatusService.setServerOffline('Timeout - El servidor no responde');
        }
        // NO marcar como offline en:
        // - Errores 4xx (400, 401, 403, 404, etc.): Son errores de validación/autenticación, el servidor está respondiendo
        // - Errores 5xx (500, 501, 505, etc.): El servidor está respondiendo, solo tiene un error interno
        // - Cualquier otro error HTTP: El servidor está respondiendo, no es un problema de conexión
      }

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error - Intentar extraer mensaje detallado del backend
        if (error.error) {
          // Si hay un mensaje directo en error.error.message
          if (error.error.message) {
            errorMessage = error.error.message;
          }
          // Si hay errores de validación (objeto errors)
          else if (error.error.errors) {
            const validationErrors = error.error.errors;
            const errorMessages = Object.keys(validationErrors)
              .map(key => `${key}: ${Array.isArray(validationErrors[key]) ? validationErrors[key].join(', ') : validationErrors[key]}`)
              .join('; ');
            errorMessage = `Errores de validación: ${errorMessages}`;
          }
          // Si error.error es un string directamente
          else if (typeof error.error === 'string') {
            errorMessage = error.error;
          }
        }

        // Fallback a mensajes genéricos por código de estado si no hay mensaje específico
        if (errorMessage === 'An error occurred') {
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
              errorMessage = `Error Code: ${error.status}`;
          }
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
