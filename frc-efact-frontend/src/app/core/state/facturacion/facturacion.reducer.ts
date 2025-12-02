import { createReducer, on } from '@ngrx/store';
import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { FacturaLegal } from '../../../models/factura.model';
import * as FacturacionActions from './facturacion.actions';

export interface FacturacionState extends EntityState<FacturaLegal> {
  loading: boolean;
  error: string | null;
}

export const adapter: EntityAdapter<FacturaLegal> = createEntityAdapter<FacturaLegal>({
  selectId: (factura: FacturaLegal) => factura.id!
});

export const initialState: FacturacionState = adapter.getInitialState({
  loading: false,
  error: null
});

export const facturacionReducer = createReducer(
  initialState,

  // Load facturas
  on(FacturacionActions.loadFacturas, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(FacturacionActions.loadFacturasSuccess, (state, { facturas }) =>
    adapter.setAll(facturas, {
      ...state,
      loading: false,
      error: null
    })
  ),

  on(FacturacionActions.loadFacturasFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Create factura
  on(FacturacionActions.createFactura, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(FacturacionActions.createFacturaSuccess, (state, { factura }) =>
    adapter.addOne(factura, {
      ...state,
      loading: false,
      error: null
    })
  ),

  on(FacturacionActions.createFacturaFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Update factura
  on(FacturacionActions.updateFactura, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(FacturacionActions.updateFacturaSuccess, (state, { factura }) =>
    adapter.updateOne(
      { id: factura.id!, changes: factura },
      {
        ...state,
        loading: false,
        error: null
      }
    )
  ),

  on(FacturacionActions.updateFacturaFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Delete factura
  on(FacturacionActions.deleteFactura, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(FacturacionActions.deleteFacturaSuccess, (state, { id }) =>
    adapter.removeOne(id, {
      ...state,
      loading: false,
      error: null
    })
  ),

  on(FacturacionActions.deleteFacturaFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  }))
);

export const { selectAll, selectEntities, selectIds, selectTotal } = adapter.getSelectors();
