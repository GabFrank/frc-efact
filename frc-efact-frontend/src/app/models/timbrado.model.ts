export interface Timbrado {
  id: number;
  empresaId: number;
  numero: string;
  isElectronico: boolean;
  csc?: string;
  fechaInicio: string;
  fechaFin: string;
  activo: boolean;

  // Campos calculados/derivados (solo lectura)
  vigente?: boolean;
  diasRestantes?: number;

  // Información de empresa para mostrar (solo lectura)
  razonSocial?: string;
  ruc?: string;
}

export interface TimbradoDetalle {
  id: number;
  timbradoId: number;
  puntoExpedicion: string;
  codigoEstablecimientoFactura: string;
  cantidad?: number | null;
  rangoDesde?: number | null;
  rangoHasta?: number | null;
  numeroActual?: number | null;
  ciudadId: number;
  barrioId?: number;
  direccion?: string;
  telefono?: string;
  activo: boolean;

  // Campos calculados (solo lectura)
  numerosDisponibles?: number;
  porcentajeUtilizado?: number;

  // Información del timbrado para mostrar (solo lectura)
  timbradoNumero?: string;
  timbradoIsElectronico?: boolean;
}
