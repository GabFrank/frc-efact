import { createFeatureSelector, createSelector } from '@ngrx/store';
import { TimbradoDetallesState, adapter } from './timbrado-detalles.reducer';

export const selectTimbradoDetallesState = createFeatureSelector<TimbradoDetallesState>('timbradoDetalles');

export const {
  selectAll: selectAllDetalles,
  selectEntities: selectDetallesEntities,
  selectIds: selectDetallesIds,
  selectTotal: selectDetallesTotal
} = adapter.getSelectors(selectTimbradoDetallesState);

export const selectDetallesLoading = createSelector(
  selectTimbradoDetallesState,
  (state) => state.loading
);

export const selectDetallesError = createSelector(
  selectTimbradoDetallesState,
  (state) => state.error
);

export const selectDetalleById = (id: number) => createSelector(
  selectDetallesEntities,
  (entities) => entities[id] || null
);

export const selectDetallesActivos = createSelector(
  selectAllDetalles,
  (detalles) => detalles.filter(detalle => detalle.activo)
);

export const selectDetallesPorAgotarse = (umbralPorcentaje: number = 80) => createSelector(
  selectAllDetalles,
  (detalles) => detalles.filter(detalle =>
    detalle.activo &&
    detalle.porcentajeUtilizado !== undefined &&
    detalle.porcentajeUtilizado >= umbralPorcentaje
  )
);

export const selectDetallesByTimbrado = (timbradoId: number) => createSelector(
  selectAllDetalles,
  (detalles) => detalles.filter(detalle => detalle.timbradoId === timbradoId)
);

export const selectDetallesActivosByTimbrado = (timbradoId: number) => createSelector(
  selectAllDetalles,
  (detalles) => detalles.filter(detalle =>
    detalle.timbradoId === timbradoId && detalle.activo
  )
);










