import { createFeatureSelector, createSelector } from '@ngrx/store';
import { TimbradosState } from './timbrados.reducer';

export const selectTimbradosState = createFeatureSelector<TimbradosState>('timbrados');

// Timbrados selectors
export const selectAllTimbrados = createSelector(
  selectTimbradosState,
  (state) => state.timbrados
);

export const selectTimbrado = createSelector(
  selectTimbradosState,
  (state) => state.timbrado
);

export const selectTimbradosLoading = createSelector(
  selectTimbradosState,
  (state) => state.loading
);

export const selectTimbradosError = createSelector(
  selectTimbradosState,
  (state) => state.error
);

// Timbrado Detalles selectors
export const selectAllTimbradoDetalles = createSelector(
  selectTimbradosState,
  (state) => state.detalles
);

export const selectTimbradoDetalle = createSelector(
  selectTimbradosState,
  (state) => state.detalle
);

export const selectTimbradoDetallesLoading = createSelector(
  selectTimbradosState,
  (state) => state.loadingDetalles
);

export const selectTimbradoDetallesError = createSelector(
  selectTimbradosState,
  (state) => state.errorDetalles
);

// Filtered selectors
export const selectActiveTimbrados = createSelector(
  selectAllTimbrados,
  (timbrados) => timbrados.filter(t => t.activo)
);

export const selectInactiveTimbrados = createSelector(
  selectAllTimbrados,
  (timbrados) => timbrados.filter(t => !t.activo)
);

export const selectElectronicTimbrados = createSelector(
  selectAllTimbrados,
  (timbrados) => timbrados.filter(t => t.isElectronico)
);

export const selectManualTimbrados = createSelector(
  selectAllTimbrados,
  (timbrados) => timbrados.filter(t => !t.isElectronico)
);

// Timbrado by ID selector
export const selectTimbradoById = (id: number) => createSelector(
  selectAllTimbrados,
  (timbrados) => timbrados.find(t => t.id === id)
);

// Timbrado Detalles by Timbrado ID selector
export const selectDetallesByTimbradoId = (timbradoId: number) => createSelector(
  selectAllTimbradoDetalles,
  (detalles) => detalles.filter(d => d.timbradoId === timbradoId)
);

// Timbrado Detalles by Empresa ID selector
export const selectDetallesByEmpresaId = (empresaId: number) => createSelector(
  selectAllTimbradoDetalles,
  (detalles) => detalles.filter(d => {
    // Asumiendo que los detalles tienen una referencia a empresaId
    // Esto puede necesitar ajuste basado en la estructura real
    return true; // Placeholder
  })
);
