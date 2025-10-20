import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  
  constructor(private snackBar: MatSnackBar) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Ha ocurrido un error inesperado';

        if (error.error instanceof ErrorEvent) {
          // Error del lado del cliente
          errorMessage = `Error: ${error.error.message}`;
        } else {
          // Error del lado del servidor
          if (error.error && error.error.message) {
            errorMessage = error.error.message;
          } else if (error.error && error.error.errors) {
            // Errores de validación
            const validationErrors = error.error.errors;
            const errorMessages = Object.keys(validationErrors)
              .map(key => `${key}: ${validationErrors[key]}`)
              .join(', ');
            errorMessage = errorMessages;
          } else {
            switch (error.status) {
              case 400:
                errorMessage = 'Solicitud inválida. Verifique los datos ingresados.';
                break;
              case 401:
                errorMessage = 'No está autenticado. Por favor, inicie sesión.';
                break;
              case 403:
                errorMessage = 'No tiene permisos para realizar esta operación.';
                break;
              case 404:
                errorMessage = 'Recurso no encontrado.';
                break;
              case 409:
                errorMessage = 'El recurso ya existe.';
                break;
              case 500:
                errorMessage = 'Error interno del servidor. Intente nuevamente más tarde.';
                break;
              default:
                errorMessage = `Error ${error.status}: ${error.statusText}`;
            }
          }
        }

        // Mostrar el error en un snackbar
        this.snackBar.open(errorMessage, 'Cerrar', {
          duration: 5000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });

        return throwError(() => error);
      })
    );
  }
}
