import { createReducer, on } from '@ngrx/store';
import { Timbrado, TimbradoDetalle } from '../../../models/timbrado.model';
import * as TimbradosActions from './timbrados.actions';

export interface TimbradosState {
  // Timbrados
  timbrados: Timbrado[];
  timbrado: Timbrado | null;
  loading: boolean;
  error: string | null;

  // Timbrado Detalles
  detalles: TimbradoDetalle[];
  detalle: TimbradoDetalle | null;
  loadingDetalles: boolean;
  errorDetalles: string | null;
}

export const initialState: TimbradosState = {
  timbrados: [],
  timbrado: null,
  loading: false,
  error: null,
  detalles: [],
  detalle: null,
  loadingDetalles: false,
  errorDetalles: null,
};

export const timbradosReducer = createReducer(
  initialState,

  // Load Timbrados
  on(TimbradosActions.loadTimbrados, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TimbradosActions.loadTimbradosSuccess, (state, { timbrados }) => ({
    ...state,
    timbrados,
    loading: false,
  })),
  on(TimbradosActions.loadTimbradosFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Load Timbrados By Empresa
  on(TimbradosActions.loadTimbradosByEmpresa, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TimbradosActions.loadTimbradosByEmpresaSuccess, (state, { timbrados }) => ({
    ...state,
    timbrados,
    loading: false,
  })),
  on(TimbradosActions.loadTimbradosByEmpresaFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Load Single Timbrado
  on(TimbradosActions.loadTimbrado, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TimbradosActions.loadTimbradoSuccess, (state, { timbrado }) => ({
    ...state,
    timbrado,
    loading: false,
  })),
  on(TimbradosActions.loadTimbradoFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Create Timbrado
  on(TimbradosActions.createTimbrado, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TimbradosActions.createTimbradoSuccess, (state, { timbrado }) => ({
    ...state,
    timbrados: [...state.timbrados, timbrado],
    loading: false,
  })),
  on(TimbradosActions.createTimbradoFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Update Timbrado
  on(TimbradosActions.updateTimbrado, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TimbradosActions.updateTimbradoSuccess, (state, { timbrado }) => ({
    ...state,
    timbrados: state.timbrados.map(t => t.id === timbrado.id ? timbrado : t),
    timbrado: timbrado,
    loading: false,
  })),
  on(TimbradosActions.updateTimbradoFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Delete Timbrado
  on(TimbradosActions.deleteTimbrado, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TimbradosActions.deleteTimbradoSuccess, (state, { id }) => ({
    ...state,
    timbrados: state.timbrados.filter(t => t.id !== id),
    loading: false,
  })),
  on(TimbradosActions.deleteTimbradoFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Verificar Vigencia
  on(TimbradosActions.verificarVigenciaTimbrado, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),
  on(TimbradosActions.verificarVigenciaTimbradoSuccess, (state, { id, vigente }) => ({
    ...state,
    timbrados: state.timbrados.map(t =>
      t.id === id ? { ...t, activo: vigente } : t
    ),
    loading: false,
  })),
  on(TimbradosActions.verificarVigenciaTimbradoFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // Load Timbrado Detalles
  on(TimbradosActions.loadTimbradoDetalles, (state) => ({
    ...state,
    loadingDetalles: true,
    errorDetalles: null,
  })),
  on(TimbradosActions.loadTimbradoDetallesSuccess, (state, { detalles }) => ({
    ...state,
    detalles,
    loadingDetalles: false,
  })),
  on(TimbradosActions.loadTimbradoDetallesFailure, (state, { error }) => ({
    ...state,
    loadingDetalles: false,
    errorDetalles: error,
  })),

  // Load Detalles By Timbrado
  on(TimbradosActions.loadDetallesByTimbrado, (state) => ({
    ...state,
    loadingDetalles: true,
    errorDetalles: null,
  })),
  on(TimbradosActions.loadDetallesByTimbradoSuccess, (state, { detalles }) => ({
    ...state,
    detalles,
    loadingDetalles: false,
  })),
  on(TimbradosActions.loadDetallesByTimbradoFailure, (state, { error }) => ({
    ...state,
    loadingDetalles: false,
    errorDetalles: error,
  })),

  // Load Detalles By Empresa
  on(TimbradosActions.loadDetallesByEmpresa, (state) => ({
    ...state,
    loadingDetalles: true,
    errorDetalles: null,
  })),
  on(TimbradosActions.loadDetallesByEmpresaSuccess, (state, { detalles }) => ({
    ...state,
    detalles,
    loadingDetalles: false,
  })),
  on(TimbradosActions.loadDetallesByEmpresaFailure, (state, { error }) => ({
    ...state,
    loadingDetalles: false,
    errorDetalles: error,
  })),

  // Load Single Timbrado Detalle
  on(TimbradosActions.loadTimbradoDetalle, (state) => ({
    ...state,
    loadingDetalles: true,
    errorDetalles: null,
  })),
  on(TimbradosActions.loadTimbradoDetalleSuccess, (state, { detalle }) => ({
    ...state,
    detalle,
    loadingDetalles: false,
  })),
  on(TimbradosActions.loadTimbradoDetalleFailure, (state, { error }) => ({
    ...state,
    loadingDetalles: false,
    errorDetalles: error,
  })),

  // Create Timbrado Detalle
  on(TimbradosActions.createTimbradoDetalle, (state) => ({
    ...state,
    loadingDetalles: true,
    errorDetalles: null,
  })),
  on(TimbradosActions.createTimbradoDetalleSuccess, (state, { detalle }) => ({
    ...state,
    detalles: [...state.detalles, detalle],
    loadingDetalles: false,
  })),
  on(TimbradosActions.createTimbradoDetalleFailure, (state, { error }) => ({
    ...state,
    loadingDetalles: false,
    errorDetalles: error,
  })),

  // Update Timbrado Detalle
  on(TimbradosActions.updateTimbradoDetalle, (state) => ({
    ...state,
    loadingDetalles: true,
    errorDetalles: null,
  })),
  on(TimbradosActions.updateTimbradoDetalleSuccess, (state, { detalle }) => ({
    ...state,
    detalles: state.detalles.map(d => d.id === detalle.id ? detalle : d),
    detalle: detalle,
    loadingDetalles: false,
  })),
  on(TimbradosActions.updateTimbradoDetalleFailure, (state, { error }) => ({
    ...state,
    loadingDetalles: false,
    errorDetalles: error,
  })),

  // Delete Timbrado Detalle
  on(TimbradosActions.deleteTimbradoDetalle, (state) => ({
    ...state,
    loadingDetalles: true,
    errorDetalles: null,
  })),
  on(TimbradosActions.deleteTimbradoDetalleSuccess, (state, { id }) => ({
    ...state,
    detalles: state.detalles.filter(d => d.id !== id),
    loadingDetalles: false,
  })),
  on(TimbradosActions.deleteTimbradoDetalleFailure, (state, { error }) => ({
    ...state,
    loadingDetalles: false,
    errorDetalles: error,
  })),

  // Verificar Disponibilidad
  on(TimbradosActions.verificarDisponibilidadDetalle, (state) => ({
    ...state,
    loadingDetalles: true,
    errorDetalles: null,
  })),
  on(TimbradosActions.verificarDisponibilidadDetalleSuccess, (state, { id, disponible }) => ({
    ...state,
    detalles: state.detalles.map(d =>
      d.id === id ? { ...d, activo: disponible } : d
    ),
    loadingDetalles: false,
  })),
  on(TimbradosActions.verificarDisponibilidadDetalleFailure, (state, { error }) => ({
    ...state,
    loadingDetalles: false,
    errorDetalles: error,
  }))
);
