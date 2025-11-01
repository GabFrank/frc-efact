import { createReducer, on } from '@ngrx/store';
import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { DocumentoElectronico } from '../../../models/documento-electronico.model';
import * as DocumentosActions from './documentos.actions';

export interface DocumentosState extends EntityState<DocumentoElectronico> {
  loading: boolean;
  error: string | null;
}

export const adapter: EntityAdapter<DocumentoElectronico> = createEntityAdapter<DocumentoElectronico>();

export const initialState: DocumentosState = adapter.getInitialState({
  loading: false,
  error: null
});

export const documentosReducer = createReducer(
  initialState,
  
  // Load documentos
  on(DocumentosActions.loadDocumentos, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  
  on(DocumentosActions.loadDocumentosSuccess, (state, { documentos }) =>
    adapter.setAll(documentos, {
      ...state,
      loading: false,
      error: null
    })
  ),
  
  on(DocumentosActions.loadDocumentosFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),
  
  // Consultar estado
  on(DocumentosActions.consultarEstado, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  
  on(DocumentosActions.consultarEstadoSuccess, (state, { documento }) =>
    adapter.updateOne(
      { id: documento.id, changes: documento },
      {
        ...state,
        loading: false,
        error: null
      }
    )
  ),
  
  on(DocumentosActions.consultarEstadoFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),
  
  // Cancelar documento
  on(DocumentosActions.cancelarDocumento, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  
  on(DocumentosActions.cancelarDocumentoSuccess, (state, { id }) => ({
    ...state,
    loading: false,
    error: null
  })),
  
  on(DocumentosActions.cancelarDocumentoFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  }))
);

export const { selectAll, selectEntities, selectIds, selectTotal } = adapter.getSelectors();
