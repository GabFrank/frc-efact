import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';
import { TimbradoDetalle } from '../../../models/timbrado.model';
import * as TimbradoDetallesActions from './timbrado-detalles.actions';

export interface TimbradoDetallesState extends EntityState<TimbradoDetalle> {
  loading: boolean;
  error: string | null;
}

export const adapter = createEntityAdapter<TimbradoDetalle>({
  selectId: (detalle: TimbradoDetalle) => detalle.id,
  sortComparer: (a: TimbradoDetalle, b: TimbradoDetalle) =>
    a.puntoExpedicion.localeCompare(b.puntoExpedicion)
});

export const initialState: TimbradoDetallesState = adapter.getInitialState({
  loading: false,
  error: null
});

export const timbradoDetallesReducer = createReducer(
  initialState,

  // Load detalles by timbrado
  on(TimbradoDetallesActions.loadDetallesByTimbrado, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(TimbradoDetallesActions.loadDetallesByTimbradoSuccess, (state, { detalles }) =>
    adapter.setAll(detalles, {
      ...state,
      loading: false,
      error: null
    })
  ),

  on(TimbradoDetallesActions.loadDetallesByTimbradoFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Create detalle
  on(TimbradoDetallesActions.createDetalle, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(TimbradoDetallesActions.createDetalleSuccess, (state, { detalle }) =>
    adapter.addOne(detalle, {
      ...state,
      loading: false,
      error: null
    })
  ),

  on(TimbradoDetallesActions.createDetalleFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Update detalle
  on(TimbradoDetallesActions.updateDetalle, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(TimbradoDetallesActions.updateDetalleSuccess, (state, { detalle }) =>
    adapter.updateOne(
      { id: detalle.id, changes: detalle },
      {
        ...state,
        loading: false,
        error: null
      }
    )
  ),

  on(TimbradoDetallesActions.updateDetalleFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Delete detalle
  on(TimbradoDetallesActions.deleteDetalle, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(TimbradoDetallesActions.deleteDetalleSuccess, (state, { id }) =>
    adapter.removeOne(id, {
      ...state,
      loading: false,
      error: null
    })
  ),

  on(TimbradoDetallesActions.deleteDetalleFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Clear detalles
  on(TimbradoDetallesActions.clearDetalles, (state) =>
    adapter.removeAll({
      ...state,
      loading: false,
      error: null
    })
  )
);

