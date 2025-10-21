import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, mergeMap, catchError } from 'rxjs/operators';
import { TimbradoApiService } from '../../api/timbrado-api.service';
import * as TimbradosActions from './timbrados.actions';

@Injectable()
export class TimbradosEffects {
  constructor(
    private actions$: Actions,
    private timbradoApiService: TimbradoApiService
  ) {}

  // Load Timbrados
  loadTimbrados$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.loadTimbrados),
      mergeMap(() =>
        this.timbradoApiService.getAll().pipe(
          map(timbrados => TimbradosActions.loadTimbradosSuccess({ timbrados })),
          catchError(error => of(TimbradosActions.loadTimbradosFailure({
            error: error.error?.message || 'Error al cargar timbrados'
          })))
        )
      )
    )
  );

  // Load Timbrados By Empresa
  loadTimbradosByEmpresa$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.loadTimbradosByEmpresa),
      mergeMap(({ empresaId }) =>
        this.timbradoApiService.getByEmpresa(empresaId).pipe(
          map(timbrados => TimbradosActions.loadTimbradosByEmpresaSuccess({ timbrados })),
          catchError(error => of(TimbradosActions.loadTimbradosByEmpresaFailure({
            error: error.error?.message || 'Error al cargar timbrados de empresa'
          })))
        )
      )
    )
  );

  // Load Single Timbrado
  loadTimbrado$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.loadTimbrado),
      mergeMap(({ id }) =>
        this.timbradoApiService.getById(id).pipe(
          map(timbrado => TimbradosActions.loadTimbradoSuccess({ timbrado })),
          catchError(error => of(TimbradosActions.loadTimbradoFailure({
            error: error.error?.message || 'Error al cargar timbrado'
          })))
        )
      )
    )
  );

  // Create Timbrado
  createTimbrado$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.createTimbrado),
      mergeMap(({ timbrado }) =>
        this.timbradoApiService.create(timbrado).pipe(
          map(createdTimbrado => TimbradosActions.createTimbradoSuccess({ timbrado: createdTimbrado })),
          catchError(error => of(TimbradosActions.createTimbradoFailure({
            error: error.error?.message || 'Error al crear timbrado'
          })))
        )
      )
    )
  );

  // Update Timbrado
  updateTimbrado$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.updateTimbrado),
      mergeMap(({ id, timbrado }) =>
        this.timbradoApiService.update(id, timbrado).pipe(
          map(updatedTimbrado => TimbradosActions.updateTimbradoSuccess({ timbrado: updatedTimbrado })),
          catchError(error => of(TimbradosActions.updateTimbradoFailure({
            error: error.error?.message || 'Error al actualizar timbrado'
          })))
        )
      )
    )
  );

  // Delete Timbrado
  deleteTimbrado$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.deleteTimbrado),
      mergeMap(({ id }) =>
        this.timbradoApiService.delete(id).pipe(
          map(() => TimbradosActions.deleteTimbradoSuccess({ id })),
          catchError(error => of(TimbradosActions.deleteTimbradoFailure({
            error: error.error?.message || 'Error al eliminar timbrado'
          })))
        )
      )
    )
  );

  // Verificar Vigencia
  verificarVigenciaTimbrado$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.verificarVigenciaTimbrado),
      mergeMap(({ id }) =>
        this.timbradoApiService.verificarVigencia(id).pipe(
          map(vigente => TimbradosActions.verificarVigenciaTimbradoSuccess({ id, vigente })),
          catchError(error => of(TimbradosActions.verificarVigenciaTimbradoFailure({
            error: error.error?.message || 'Error al verificar vigencia'
          })))
        )
      )
    )
  );

  // Load Timbrado Detalles
  loadTimbradoDetalles$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.loadTimbradoDetalles),
      mergeMap(() =>
        this.timbradoApiService.getAllDetalles().pipe(
          map(detalles => TimbradosActions.loadTimbradoDetallesSuccess({ detalles })),
          catchError(error => of(TimbradosActions.loadTimbradoDetallesFailure({
            error: error.error?.message || 'Error al cargar detalles de timbrados'
          })))
        )
      )
    )
  );

  // Load Detalles By Timbrado
  loadDetallesByTimbrado$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.loadDetallesByTimbrado),
      mergeMap(({ timbradoId }) =>
        this.timbradoApiService.getDetallesByTimbrado(timbradoId).pipe(
          map(detalles => TimbradosActions.loadDetallesByTimbradoSuccess({ detalles })),
          catchError(error => of(TimbradosActions.loadDetallesByTimbradoFailure({
            error: error.error?.message || 'Error al cargar detalles del timbrado'
          })))
        )
      )
    )
  );

  // Load Detalles By Empresa
  loadDetallesByEmpresa$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.loadDetallesByEmpresa),
      mergeMap(({ empresaId }) =>
        this.timbradoApiService.getDetallesByEmpresa(empresaId).pipe(
          map(detalles => TimbradosActions.loadDetallesByEmpresaSuccess({ detalles })),
          catchError(error => of(TimbradosActions.loadDetallesByEmpresaFailure({
            error: error.error?.message || 'Error al cargar detalles de empresa'
          })))
        )
      )
    )
  );

  // Load Single Timbrado Detalle
  loadTimbradoDetalle$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.loadTimbradoDetalle),
      mergeMap(({ id }) =>
        this.timbradoApiService.getDetalleById(id).pipe(
          map(detalle => TimbradosActions.loadTimbradoDetalleSuccess({ detalle })),
          catchError(error => of(TimbradosActions.loadTimbradoDetalleFailure({
            error: error.error?.message || 'Error al cargar detalle de timbrado'
          })))
        )
      )
    )
  );

  // Create Timbrado Detalle
  createTimbradoDetalle$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.createTimbradoDetalle),
      mergeMap(({ detalle }) =>
        this.timbradoApiService.createDetalle(detalle).pipe(
          map(createdDetalle => TimbradosActions.createTimbradoDetalleSuccess({ detalle: createdDetalle })),
          catchError(error => of(TimbradosActions.createTimbradoDetalleFailure({
            error: error.error?.message || 'Error al crear detalle de timbrado'
          })))
        )
      )
    )
  );

  // Update Timbrado Detalle
  updateTimbradoDetalle$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.updateTimbradoDetalle),
      mergeMap(({ id, detalle }) =>
        this.timbradoApiService.updateDetalle(id, detalle).pipe(
          map(updatedDetalle => TimbradosActions.updateTimbradoDetalleSuccess({ detalle: updatedDetalle })),
          catchError(error => of(TimbradosActions.updateTimbradoDetalleFailure({
            error: error.error?.message || 'Error al actualizar detalle de timbrado'
          })))
        )
      )
    )
  );

  // Delete Timbrado Detalle
  deleteTimbradoDetalle$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.deleteTimbradoDetalle),
      mergeMap(({ id }) =>
        this.timbradoApiService.deleteDetalle(id).pipe(
          map(() => TimbradosActions.deleteTimbradoDetalleSuccess({ id })),
          catchError(error => of(TimbradosActions.deleteTimbradoDetalleFailure({
            error: error.error?.message || 'Error al eliminar detalle de timbrado'
          })))
        )
      )
    )
  );

  // Verificar Disponibilidad
  verificarDisponibilidadDetalle$ = createEffect(() =>
    this.actions$.pipe(
      ofType(TimbradosActions.verificarDisponibilidadDetalle),
      mergeMap(({ id }) =>
        this.timbradoApiService.verificarDisponibilidad(id).pipe(
          map(disponible => TimbradosActions.verificarDisponibilidadDetalleSuccess({ id, disponible })),
          catchError(error => of(TimbradosActions.verificarDisponibilidadDetalleFailure({
            error: error.error?.message || 'Error al verificar disponibilidad'
          })))
        )
      )
    )
  );
}
