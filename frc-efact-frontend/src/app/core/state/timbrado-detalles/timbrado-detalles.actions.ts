import { createAction, props } from '@ngrx/store';
import { TimbradoDetalle } from '../../../models/timbrado.model';

// Load detalles by timbrado
export const loadDetallesByTimbrado = createAction(
  '[TimbradoDetalle] Load Detalles By Timbrado',
  props<{ timbradoId: number }>()
);

export const loadDetallesByTimbradoSuccess = createAction(
  '[TimbradoDetalle] Load Detalles By Timbrado Success',
  props<{ detalles: TimbradoDetalle[] }>()
);

export const loadDetallesByTimbradoFailure = createAction(
  '[TimbradoDetalle] Load Detalles By Timbrado Failure',
  props<{ error: string }>()
);

// Create detalle
export const createDetalle = createAction(
  '[TimbradoDetalle] Create Detalle',
  props<{ timbradoId: number; detalle: Partial<TimbradoDetalle> }>()
);

export const createDetalleSuccess = createAction(
  '[TimbradoDetalle] Create Detalle Success',
  props<{ detalle: TimbradoDetalle }>()
);

export const createDetalleFailure = createAction(
  '[TimbradoDetalle] Create Detalle Failure',
  props<{ error: string }>()
);

// Update detalle
export const updateDetalle = createAction(
  '[TimbradoDetalle] Update Detalle',
  props<{ id: number; detalle: Partial<TimbradoDetalle> }>()
);

export const updateDetalleSuccess = createAction(
  '[TimbradoDetalle] Update Detalle Success',
  props<{ detalle: TimbradoDetalle }>()
);

export const updateDetalleFailure = createAction(
  '[TimbradoDetalle] Update Detalle Failure',
  props<{ error: string }>()
);

// Delete detalle
export const deleteDetalle = createAction(
  '[TimbradoDetalle] Delete Detalle',
  props<{ id: number }>()
);

export const deleteDetalleSuccess = createAction(
  '[TimbradoDetalle] Delete Detalle Success',
  props<{ id: number }>()
);

export const deleteDetalleFailure = createAction(
  '[TimbradoDetalle] Delete Detalle Failure',
  props<{ error: string }>()
);

// Clear detalles (when navigating away)
export const clearDetalles = createAction(
  '[TimbradoDetalle] Clear Detalles'
);

