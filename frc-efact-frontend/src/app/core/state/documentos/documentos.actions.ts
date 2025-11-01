import { createAction, props } from '@ngrx/store';
import { DocumentoElectronico, EstadoDE } from '../../../models/documento-electronico.model';

// Load documentos
export const loadDocumentos = createAction(
  '[Documentos] Load Documentos',
  props<{ estado?: EstadoDE }>()
);

export const loadDocumentosSuccess = createAction(
  '[Documentos] Load Documentos Success',
  props<{ documentos: DocumentoElectronico[] }>()
);

export const loadDocumentosFailure = createAction(
  '[Documentos] Load Documentos Failure',
  props<{ error: string }>()
);

// Consultar estado
export const consultarEstado = createAction(
  '[Documentos] Consultar Estado',
  props<{ id: number }>()
);

export const consultarEstadoSuccess = createAction(
  '[Documentos] Consultar Estado Success',
  props<{ documento: DocumentoElectronico }>()
);

export const consultarEstadoFailure = createAction(
  '[Documentos] Consultar Estado Failure',
  props<{ error: string }>()
);

// Cancelar documento
export const cancelarDocumento = createAction(
  '[Documentos] Cancelar Documento',
  props<{ id: number; motivo: string }>()
);

export const cancelarDocumentoSuccess = createAction(
  '[Documentos] Cancelar Documento Success',
  props<{ id: number }>()
);

export const cancelarDocumentoFailure = createAction(
  '[Documentos] Cancelar Documento Failure',
  props<{ error: string }>()
);
