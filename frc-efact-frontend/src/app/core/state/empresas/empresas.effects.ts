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
          map((empresa) => EmpresasActions.createEmpresaSuccess({ empresa })),
          catchError((error) =>
            of(EmpresasActions.createEmpresaFailure({ error: error.message }))
          )
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
