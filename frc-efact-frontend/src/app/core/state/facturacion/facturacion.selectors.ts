import { createFeatureSelector, createSelector } from '@ngrx/store';
import { FacturacionState, selectAll } from './facturacion.reducer';

export const selectFacturacionState = createFeatureSelector<FacturacionState>('facturacion');

export const selectAllFacturas = createSelector(
  selectFacturacionState,
  selectAll
);

export const selectFacturacionLoading = createSelector(
  selectFacturacionState,
  (state) => state.loading
);

export const selectFacturacionError = createSelector(
  selectFacturacionState,
  (state) => state.error
);
