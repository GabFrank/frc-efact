import { createFeatureSelector, createSelector } from '@ngrx/store';
import { EmpresasState, selectAll, selectEntities } from './empresas.reducer';

export const selectEmpresasState = createFeatureSelector<EmpresasState>('empresas');

export const selectAllEmpresas = createSelector(
  selectEmpresasState,
  selectAll
);

export const selectEmpresasEntities = createSelector(
  selectEmpresasState,
  selectEntities
);

export const selectSelectedEmpresaId = createSelector(
  selectEmpresasState,
  (state) => state.selectedEmpresaId
);

export const selectSelectedEmpresa = createSelector(
  selectEmpresasEntities,
  selectSelectedEmpresaId,
  (entities, selectedId) => (selectedId ? entities[selectedId] : null)
);

export const selectEmpresasLoading = createSelector(
  selectEmpresasState,
  (state) => state.loading
);

export const selectEmpresasError = createSelector(
  selectEmpresasState,
  (state) => state.error
);

export const selectEmpresaById = (id: number) => createSelector(
  selectEmpresasEntities,
  (entities) => entities[id] || null
);
