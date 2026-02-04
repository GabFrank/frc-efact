export interface DocumentoElectronico {
  id: number;
  facturaLegalId: number;
  loteDeId?: number;
  cdc?: string;
  urlQr?: string;
  numeroDocumento?: string;
  estado: EstadoDE;
  codigoRespuestaSifen?: string;
  mensajeRespuestaSifen?: string;
  respuestaSifen?: string;
  protocoloAutorizacion?: string;
  fechaEmision: string;
  fechaRecepcionSifen?: string;
}

export enum EstadoDE {
  PENDIENTE = 'PENDIENTE',
  EN_PROCESO = 'EN_PROCESO',
  APROBADO = 'APROBADO',
  RECHAZADO = 'RECHAZADO',
  CANCELADO = 'CANCELADO',
  ERROR = 'ERROR'
}

export interface LoteDE {
  id: number;
  empresaId: number;
  estado: EstadoLote;
  protocolo?: string;
  respuestaSifen?: string;
  codigoRespuesta?: string;
  mensajeRespuesta?: string;
  fechaProcesado?: string;
  fechaUltimoIntento?: string;
  intentos: number;
}

export enum EstadoLote {
  PENDIENTE = 'PENDIENTE',
  EN_PROCESO = 'EN_PROCESO',
  APROBADO = 'APROBADO',
  RECHAZADO = 'RECHAZADO',
  ERROR = 'ERROR'
}

export interface GenerarDeResponse {
  documento: DocumentoElectronico;
  lote: LoteDE;
}
