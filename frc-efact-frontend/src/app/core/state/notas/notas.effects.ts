import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';
import { NotaCreditoApiService } from '../../../core/api/nota-credito-api.service';
import { NotaDebitoApiService } from '../../../core/api/nota-debito-api.service';
import { NotaRemisionApiService } from '../../../core/api/nota-remision-api.service';
import * as NotasActions from './notas.actions';

@Injectable()
export class NotasEffects {
  private actions$ = inject(Actions);
  private notaCreditoApi = inject(NotaCreditoApiService);
  private notaDebitoApi = inject(NotaDebitoApiService);
  private notaRemisionApi = inject(NotaRemisionApiService);

  // Nota Credito Effects
  loadNotasCredito$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.loadNotasCredito),
      switchMap(({ empresaId, page = 0, size = 20 }) =>
        this.notaCreditoApi.getAll(empresaId, page, size).pipe(
          map(response => NotasActions.loadNotasCreditoSuccess({ 
            notas: response.content, 
            total: response.totalElements 
          })),
          catchError(error => of(NotasActions.loadNotasCreditoFailure({ error })))
        )
      )
    )
  );

  createNotaCredito$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.createNotaCredito),
      switchMap(({ nota }) =>
        this.notaCreditoApi.create(nota).pipe(
          map(created => NotasActions.createNotaCreditoSuccess({ nota: created })),
          catchError(error => of(NotasActions.createNotaCreditoFailure({ error })))
        )
      )
    )
  );

  deleteNotaCredito$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.deleteNotaCredito),
      switchMap(({ id }) =>
        this.notaCreditoApi.delete(id).pipe(
          map(() => NotasActions.deleteNotaCreditoSuccess({ id })),
          catchError(error => of(NotasActions.loadNotasCreditoFailure({ error })))
        )
      )
    )
  );

  // Nota Debito Effects
  loadNotasDebito$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.loadNotasDebito),
      switchMap(({ empresaId, page = 0, size = 20 }) =>
        this.notaDebitoApi.getAll(empresaId, page, size).pipe(
          map(response => NotasActions.loadNotasDebitoSuccess({ 
            notas: response.content, 
            total: response.totalElements 
          })),
          catchError(error => of(NotasActions.loadNotasDebitoFailure({ error })))
        )
      )
    )
  );

  createNotaDebito$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.createNotaDebito),
      switchMap(({ nota }) =>
        this.notaDebitoApi.create(nota).pipe(
          map(created => NotasActions.createNotaDebitoSuccess({ nota: created })),
          catchError(error => of(NotasActions.createNotaDebitoFailure({ error })))
        )
      )
    )
  );

  deleteNotaDebito$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.deleteNotaDebito),
      switchMap(({ id }) =>
        this.notaDebitoApi.delete(id).pipe(
          map(() => NotasActions.deleteNotaDebitoSuccess({ id })),
          catchError(error => of(NotasActions.loadNotasDebitoFailure({ error })))
        )
      )
    )
  );

  // Nota Remision Effects
  loadNotasRemision$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.loadNotasRemision),
      switchMap(({ empresaId, page = 0, size = 20 }) =>
        this.notaRemisionApi.getAll(empresaId, page, size).pipe(
          map(response => NotasActions.loadNotasRemisionSuccess({ 
            notas: response.content, 
            total: response.totalElements 
          })),
          catchError(error => of(NotasActions.loadNotasRemisionFailure({ error })))
        )
      )
    )
  );

  createNotaRemision$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.createNotaRemision),
      switchMap(({ nota }) =>
        this.notaRemisionApi.create(nota).pipe(
          map(created => NotasActions.createNotaRemisionSuccess({ nota: created })),
          catchError(error => of(NotasActions.createNotaRemisionFailure({ error })))
        )
      )
    )
  );

  deleteNotaRemision$ = createEffect(() =>
    this.actions$.pipe(
      ofType(NotasActions.deleteNotaRemision),
      switchMap(({ id }) =>
        this.notaRemisionApi.delete(id).pipe(
          map(() => NotasActions.deleteNotaRemisionSuccess({ id })),
          catchError(error => of(NotasActions.loadNotasRemisionFailure({ error })))
        )
      )
    )
  );
}

