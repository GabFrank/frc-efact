import { createAction, props } from '@ngrx/store';
import { Timbrado, TimbradoDetalle } from '../../../models/timbrado.model';

// Timbrado Actions
export const loadTimbrados = createAction('[Timbrados] Load Timbrados');
export const loadTimbradosSuccess = createAction(
  '[Timbrados] Load Timbrados Success',
  props<{ timbrados: Timbrado[] }>()
);
export const loadTimbradosFailure = createAction(
  '[Timbrados] Load Timbrados Failure',
  props<{ error: string }>()
);

export const loadTimbradosByEmpresa = createAction(
  '[Timbrados] Load Timbrados By Empresa',
  props<{ empresaId: number }>()
);
export const loadTimbradosByEmpresaSuccess = createAction(
  '[Timbrados] Load Timbrados By Empresa Success',
  props<{ timbrados: Timbrado[] }>()
);
export const loadTimbradosByEmpresaFailure = createAction(
  '[Timbrados] Load Timbrados By Empresa Failure',
  props<{ error: string }>()
);

export const loadTimbrado = createAction(
  '[Timbrados] Load Timbrado',
  props<{ id: number }>()
);
export const loadTimbradoSuccess = createAction(
  '[Timbrados] Load Timbrado Success',
  props<{ timbrado: Timbrado }>()
);
export const loadTimbradoFailure = createAction(
  '[Timbrados] Load Timbrado Failure',
  props<{ error: string }>()
);

export const createTimbrado = createAction(
  '[Timbrados] Create Timbrado',
  props<{ timbrado: Partial<Timbrado> }>()
);
export const createTimbradoSuccess = createAction(
  '[Timbrados] Create Timbrado Success',
  props<{ timbrado: Timbrado }>()
);
export const createTimbradoFailure = createAction(
  '[Timbrados] Create Timbrado Failure',
  props<{ error: string }>()
);

export const updateTimbrado = createAction(
  '[Timbrados] Update Timbrado',
  props<{ id: number; timbrado: Partial<Timbrado> }>()
);
export const updateTimbradoSuccess = createAction(
  '[Timbrados] Update Timbrado Success',
  props<{ timbrado: Timbrado }>()
);
export const updateTimbradoFailure = createAction(
  '[Timbrados] Update Timbrado Failure',
  props<{ error: string }>()
);

export const deleteTimbrado = createAction(
  '[Timbrados] Delete Timbrado',
  props<{ id: number }>()
);
export const deleteTimbradoSuccess = createAction(
  '[Timbrados] Delete Timbrado Success',
  props<{ id: number }>()
);
export const deleteTimbradoFailure = createAction(
  '[Timbrados] Delete Timbrado Failure',
  props<{ error: string }>()
);

export const verificarVigenciaTimbrado = createAction(
  '[Timbrados] Verificar Vigencia Timbrado',
  props<{ id: number }>()
);
export const verificarVigenciaTimbradoSuccess = createAction(
  '[Timbrados] Verificar Vigencia Timbrado Success',
  props<{ id: number; vigente: boolean }>()
);
export const verificarVigenciaTimbradoFailure = createAction(
  '[Timbrados] Verificar Vigencia Timbrado Failure',
  props<{ error: string }>()
);

// Timbrado Detalle Actions
export const loadTimbradoDetalles = createAction('[Timbrados] Load Timbrado Detalles');
export const loadTimbradoDetallesSuccess = createAction(
  '[Timbrados] Load Timbrado Detalles Success',
  props<{ detalles: TimbradoDetalle[] }>()
);
export const loadTimbradoDetallesFailure = createAction(
  '[Timbrados] Load Timbrado Detalles Failure',
  props<{ error: string }>()
);

export const loadDetallesByTimbrado = createAction(
  '[Timbrados] Load Detalles By Timbrado',
  props<{ timbradoId: number }>()
);
export const loadDetallesByTimbradoSuccess = createAction(
  '[Timbrados] Load Detalles By Timbrado Success',
  props<{ detalles: TimbradoDetalle[] }>()
);
export const loadDetallesByTimbradoFailure = createAction(
  '[Timbrados] Load Detalles By Timbrado Failure',
  props<{ error: string }>()
);

export const loadDetallesByEmpresa = createAction(
  '[Timbrados] Load Detalles By Empresa',
  props<{ empresaId: number }>()
);
export const loadDetallesByEmpresaSuccess = createAction(
  '[Timbrados] Load Detalles By Empresa Success',
  props<{ detalles: TimbradoDetalle[] }>()
);
export const loadDetallesByEmpresaFailure = createAction(
  '[Timbrados] Load Detalles By Empresa Failure',
  props<{ error: string }>()
);

export const loadTimbradoDetalle = createAction(
  '[Timbrados] Load Timbrado Detalle',
  props<{ id: number }>()
);
export const loadTimbradoDetalleSuccess = createAction(
  '[Timbrados] Load Timbrado Detalle Success',
  props<{ detalle: TimbradoDetalle }>()
);
export const loadTimbradoDetalleFailure = createAction(
  '[Timbrados] Load Timbrado Detalle Failure',
  props<{ error: string }>()
);

export const createTimbradoDetalle = createAction(
  '[Timbrados] Create Timbrado Detalle',
  props<{ detalle: Partial<TimbradoDetalle> }>()
);
export const createTimbradoDetalleSuccess = createAction(
  '[Timbrados] Create Timbrado Detalle Success',
  props<{ detalle: TimbradoDetalle }>()
);
export const createTimbradoDetalleFailure = createAction(
  '[Timbrados] Create Timbrado Detalle Failure',
  props<{ error: string }>()
);

export const updateTimbradoDetalle = createAction(
  '[Timbrados] Update Timbrado Detalle',
  props<{ id: number; detalle: Partial<TimbradoDetalle> }>()
);
export const updateTimbradoDetalleSuccess = createAction(
  '[Timbrados] Update Timbrado Detalle Success',
  props<{ detalle: TimbradoDetalle }>()
);
export const updateTimbradoDetalleFailure = createAction(
  '[Timbrados] Update Timbrado Detalle Failure',
  props<{ error: string }>()
);

export const deleteTimbradoDetalle = createAction(
  '[Timbrados] Delete Timbrado Detalle',
  props<{ id: number }>()
);
export const deleteTimbradoDetalleSuccess = createAction(
  '[Timbrados] Delete Timbrado Detalle Success',
  props<{ id: number }>()
);
export const deleteTimbradoDetalleFailure = createAction(
  '[Timbrados] Delete Timbrado Detalle Failure',
  props<{ error: string }>()
);

export const verificarDisponibilidadDetalle = createAction(
  '[Timbrados] Verificar Disponibilidad Detalle',
  props<{ id: number }>()
);
export const verificarDisponibilidadDetalleSuccess = createAction(
  '[Timbrados] Verificar Disponibilidad Detalle Success',
  props<{ id: number; disponible: boolean }>()
);
export const verificarDisponibilidadDetalleFailure = createAction(
  '[Timbrados] Verificar Disponibilidad Detalle Failure',
  props<{ error: string }>()
);

// Export all actions as TimbradosActions
export const TimbradosActions = {
  // Timbrados
  loadTimbrados,
  loadTimbradosSuccess,
  loadTimbradosFailure,
  loadTimbradosByEmpresa,
  loadTimbradosByEmpresaSuccess,
  loadTimbradosByEmpresaFailure,
  loadTimbrado,
  loadTimbradoSuccess,
  loadTimbradoFailure,
  createTimbrado,
  createTimbradoSuccess,
  createTimbradoFailure,
  updateTimbrado,
  updateTimbradoSuccess,
  updateTimbradoFailure,
  deleteTimbrado,
  deleteTimbradoSuccess,
  deleteTimbradoFailure,
  verificarVigenciaTimbrado,
  verificarVigenciaTimbradoSuccess,
  verificarVigenciaTimbradoFailure,

  // Timbrados Detalle
  loadTimbradoDetalles,
  loadTimbradoDetallesSuccess,
  loadTimbradoDetallesFailure,
  loadDetallesByTimbrado,
  loadDetallesByTimbradoSuccess,
  loadDetallesByTimbradoFailure,
  loadDetallesByEmpresa,
  loadDetallesByEmpresaSuccess,
  loadDetallesByEmpresaFailure,
  loadTimbradoDetalle,
  loadTimbradoDetalleSuccess,
  loadTimbradoDetalleFailure,
  createTimbradoDetalle,
  createTimbradoDetalleSuccess,
  createTimbradoDetalleFailure,
  updateTimbradoDetalle,
  updateTimbradoDetalleSuccess,
  updateTimbradoDetalleFailure,
  deleteTimbradoDetalle,
  deleteTimbradoDetalleSuccess,
  deleteTimbradoDetalleFailure,
  verificarDisponibilidadDetalle,
  verificarDisponibilidadDetalleSuccess,
  verificarDisponibilidadDetalleFailure
};
