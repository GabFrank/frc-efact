import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, exhaustMap } from 'rxjs/operators';
import { FacturaApiService } from '../../api/factura-api.service';
import * as FacturacionActions from './facturacion.actions';

@Injectable()
export class FacturacionEffects {
  private readonly actions$ = inject(Actions);
  private readonly facturaApi = inject(FacturaApiService);

  loadFacturas$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacturacionActions.loadFacturas),
      exhaustMap(({ filtro }) =>
        this.facturaApi.getAll(filtro).pipe(
          map((facturas) => FacturacionActions.loadFacturasSuccess({ facturas })),
          catchError((error) =>
            of(FacturacionActions.loadFacturasFailure({ error: error.message }))
          )
        )
      )
    )
  );

  createFactura$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacturacionActions.createFactura),
      exhaustMap(({ factura }) =>
        this.facturaApi.create(factura).pipe(
          map((factura) => FacturacionActions.createFacturaSuccess({ factura })),
          catchError((error) =>
            of(FacturacionActions.createFacturaFailure({ error: error.message }))
          )
        )
      )
    )
  );

  updateFactura$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacturacionActions.updateFactura),
      exhaustMap(({ id, factura }) =>
        this.facturaApi.update(id, factura).pipe(
          map((factura) => FacturacionActions.updateFacturaSuccess({ factura })),
          catchError((error) =>
            of(FacturacionActions.updateFacturaFailure({ error: error.message }))
          )
        )
      )
    )
  );

  deleteFactura$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacturacionActions.deleteFactura),
      exhaustMap(({ id }) =>
        this.facturaApi.delete(id).pipe(
          map(() => FacturacionActions.deleteFacturaSuccess({ id })),
          catchError((error) =>
            of(FacturacionActions.deleteFacturaFailure({ error: error.message }))
          )
        )
      )
    )
  );
}
