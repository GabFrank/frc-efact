import { createFeatureSelector, createSelector } from '@ngrx/store';
import { DocumentosState, selectAll } from './documentos.reducer';

export const selectDocumentosState = createFeatureSelector<DocumentosState>('documentos');

export const selectAllDocumentos = createSelector(
  selectDocumentosState,
  selectAll
);

export const selectDocumentosLoading = createSelector(
  selectDocumentosState,
  (state) => state.loading
);

export const selectDocumentosError = createSelector(
  selectDocumentosState,
  (state) => state.error
);
