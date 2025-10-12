export interface Timbrado {
  id: number;
  empresaId: number;
  razonSocial: string;
  ruc: string;
  numero: string;
  isElectronico: boolean;
  fechaInicio: string;
  fechaFin: string;
  activo: boolean;
}

export interface TimbradoDetalle {
  id: number;
  timbradoId: number;
  puntoExpedicion: string;
  codigoEstablecimientoFactura: string;
  cantidad: number;
  rangoDesde: number;
  rangoHasta: number;
  numeroActual: number;
  departamento?: string;
  ciudad?: string;
  codigoCiudad?: string;
  localidad?: string;
  barrio?: string;
  direccion?: string;
  telefono?: string;
  activo: boolean;
}
