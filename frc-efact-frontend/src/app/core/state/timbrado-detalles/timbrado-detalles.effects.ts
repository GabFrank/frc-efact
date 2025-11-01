import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { of } from 'rxjs';

import { TimbradoDetalleApiService } from '../../api/timbrado-detalle-api.service';
import * as TimbradoDetallesActions from './timbrado-detalles.actions';

@Injectable()
export class TimbradoDetallesEffects {
  private actions$ = inject(Actions);
  private timbradoDetalleApiService = inject(TimbradoDetalleApiService);
  private snackBar = inject(MatSnackBar);

  // Load detalles by timbrado
  loadDetallesByTimbrado$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.loadDetallesByTimbrado),
      switchMap(({ timbradoId }) =>
        this.timbradoDetalleApiService.getByTimbrado(timbradoId).pipe(
          map((detalles) =>
            TimbradoDetallesActions.loadDetallesByTimbradoSuccess({ detalles })
          ),
          catchError((error) =>
            of(TimbradoDetallesActions.loadDetallesByTimbradoFailure({
              error: error.message || 'Error al cargar los detalles del timbrado'
            }))
          )
        )
      )
    )
  );

  // Create detalle
  createDetalle$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.createDetalle),
      switchMap(({ timbradoId, detalle }) =>
        this.timbradoDetalleApiService.create(timbradoId, detalle).pipe(
          map((detalleCreado) =>
            TimbradoDetallesActions.createDetalleSuccess({ detalle: detalleCreado })
          ),
          catchError((error) =>
            of(TimbradoDetallesActions.createDetalleFailure({
              error: error.message || 'Error al crear el detalle del timbrado'
            }))
          )
        )
      )
    )
  );

  // Update detalle
  updateDetalle$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.updateDetalle),
      switchMap(({ id, detalle }) =>
        this.timbradoDetalleApiService.update(id, detalle).pipe(
          map((detalleActualizado) =>
            TimbradoDetallesActions.updateDetalleSuccess({ detalle: detalleActualizado })
          ),
          catchError((error) =>
            of(TimbradoDetallesActions.updateDetalleFailure({
              error: error.message || 'Error al actualizar el detalle del timbrado'
            }))
          )
        )
      )
    )
  );

  // Delete detalle
  deleteDetalle$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.deleteDetalle),
      switchMap(({ id }) =>
        this.timbradoDetalleApiService.delete(id).pipe(
          map(() =>
            TimbradoDetallesActions.deleteDetalleSuccess({ id })
          ),
          catchError((error) =>
            of(TimbradoDetallesActions.deleteDetalleFailure({
              error: error.message || 'Error al eliminar el detalle del timbrado'
            }))
          )
        )
      )
    )
  );

  // Success notifications
  createDetalleSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.createDetalleSuccess),
      tap(() => {
        this.snackBar.open('Punto de expedición creado exitosamente', 'Cerrar', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      })
    ),
    { dispatch: false }
  );

  updateDetalleSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.updateDetalleSuccess),
      tap(() => {
        this.snackBar.open('Punto de expedición actualizado exitosamente', 'Cerrar', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      })
    ),
    { dispatch: false }
  );

  deleteDetalleSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.deleteDetalleSuccess),
      tap(() => {
        this.snackBar.open('Punto de expedición desactivado exitosamente', 'Cerrar', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
      })
    ),
    { dispatch: false }
  );

  // Error notifications
  createDetalleFailure$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.createDetalleFailure),
      tap(({ error }) => {
        this.snackBar.open(`Error: ${error}`, 'Cerrar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      })
    ),
    { dispatch: false }
  );

  updateDetalleFailure$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.updateDetalleFailure),
      tap(({ error }) => {
        this.snackBar.open(`Error: ${error}`, 'Cerrar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      })
    ),
    { dispatch: false }
  );

  deleteDetalleFailure$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.deleteDetalleFailure),
      tap(({ error }) => {
        this.snackBar.open(`Error: ${error}`, 'Cerrar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      })
    ),
    { dispatch: false }
  );

  loadDetallesByTimbradoFailure$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradoDetallesActions.loadDetallesByTimbradoFailure),
      tap(({ error }) => {
        this.snackBar.open(`Error: ${error}`, 'Cerrar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      })
    ),
    { dispatch: false }
  );
}








