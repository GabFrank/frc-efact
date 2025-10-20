import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { map, catchError, debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { RucValidationService } from '../services/ruc-validation.service';

export function rucAsyncValidator(
  rucValidationService: RucValidationService,
  excludeId?: number
): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value || control.value.trim() === '') {
      return of(null);
    }

    const ruc = control.value.trim();

    // Validación de formato primero (síncrona)
    if (!rucValidationService.validateRucFormat(ruc)) {
      return of({ rucInvalidFormat: true });
    }

    // Validación con el servidor (asíncrona)
    return of(control.value).pipe(
      debounceTime(800), // Esperar 800ms después del último cambio
      distinctUntilChanged(),
      switchMap(value => {
        if (!value || value.trim() === '') {
          return of(null);
        }

        return rucValidationService.validateRucLive(value.trim(), excludeId).pipe(
          map(result => {
            if (!result.valid) {
              if (result.error === 'Formato de RUC inválido') {
                return { rucInvalidFormat: true };
              }
              return { rucValidationError: result.error };
            }

            if (result.exists) {
              return { 
                rucExists: true,
                existingRazonSocial: result.razonSocial 
              };
            }

            return null; // RUC válido y disponible
          }),
          catchError(() => {
            return of({ rucValidationError: 'Error al validar RUC' });
          })
        );
      })
    );
  };
}