import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, exhaustMap } from 'rxjs/operators';
import { EmpresaApiService } from '../../api/empresa-api.service';
import * as EmpresasActions from './empresas.actions';

@Injectable()
export class EmpresasEffects {
  private readonly actions$ = inject(Actions);
  private readonly empresaApi = inject(EmpresaApiService);

  loadEmpresas$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EmpresasActions.loadEmpresas),
      exhaustMap(() =>
        this.empresaApi.getAll().pipe(
          map((empresas) => EmpresasActions.loadEmpresasSuccess({ empresas })),
          catchError((error) =>
            of(EmpresasActions.loadEmpresasFailure({ error: error.message }))
          )
        )
      )
    )
  );

  loadMisEmpresas$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EmpresasActions.loadMisEmpresas),
      exhaustMap(() =>
        this.empresaApi.getMisEmpresas().pipe(
          map((empresas) => EmpresasActions.loadMisEmpresasSuccess({ empresas })),
          catchError((error) =>
            of(EmpresasActions.loadMisEmpresasFailure({ error: error.message }))
          )
        )
      )
    )
  );

  createEmpresa$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EmpresasActions.createEmpresa),
      exhaustMap(({ empresa, certificadoFile, certificadoPassword }) =>
        this.empresaApi.create(empresa, certificadoFile, certificadoPassword).pipe(
          map((empresaCreada) => EmpresasActions.createEmpresaSuccess({ empresa: empresaCreada })),
          catchError((error) => {
            // Extraer mensaje de error más detallado
            let errorMessage = error.message || 'Error desconocido al crear empresa';

            // Si es un HttpErrorResponse con error.error
            if (error.error) {
              if (error.error.message) {
                errorMessage = error.error.message;
              } else if (error.error.errors) {
                // Errores de validación
                const validationErrors = error.error.errors;
                errorMessage = Object.keys(validationErrors)
                  .map(key => `${key}: ${Array.isArray(validationErrors[key]) ? validationErrors[key].join(', ') : validationErrors[key]}`)
                  .join('; ');
              } else if (typeof error.error === 'string') {
                errorMessage = error.error;
              }
            }

            // Si hay un originalError, intentar extraer de ahí también
            if (error.originalError?.error) {
              const original = error.originalError.error;
              if (original.message && errorMessage === 'Error desconocido al crear empresa') {
                errorMessage = original.message;
              } else if (original.errors) {
                const validationErrors = original.errors;
                errorMessage = Object.keys(validationErrors)
                  .map(key => `${key}: ${Array.isArray(validationErrors[key]) ? validationErrors[key].join(', ') : validationErrors[key]}`)
                  .join('; ');
              }
            }

            return of(EmpresasActions.createEmpresaFailure({ error: errorMessage }));
          })
        )
      )
    )
  );

  updateEmpresa$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EmpresasActions.updateEmpresa),
      exhaustMap(({ id, empresa, certificadoFile, certificadoPassword }) =>
        this.empresaApi.update(id, empresa, certificadoFile, certificadoPassword).pipe(
          map((empresa) => EmpresasActions.updateEmpresaSuccess({ empresa })),
          catchError((error) =>
            of(EmpresasActions.updateEmpresaFailure({ error: error.message }))
          )
        )
      )
    )
  );

  deleteEmpresa$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EmpresasActions.deleteEmpresa),
      exhaustMap(({ id }) =>
        this.empresaApi.delete(id).pipe(
          map(() => EmpresasActions.deleteEmpresaSuccess({ id })),
          catchError((error) =>
            of(EmpresasActions.deleteEmpresaFailure({ error: error.message }))
          )
        )
      )
    )
  );
}
