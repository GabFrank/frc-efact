export interface Empresa {
  id: number;
  razonSocial: string;
  ruc: string;
  tipoContribuyente: 'PF' | 'PJ'; // PF = Persona Física, PJ = Persona Jurídica
  nombreFantasia?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
  // Domicilio fiscal - IDs directos
  ciudadId: number;
  barrioId?: number;
  domicilioFiscalDireccion: string;
  actividadEconomica: ActividadEconomica;
  certificadoPath?: string;
  certificadoFechaExpiracion?: string;
  certificado?: CertificadoInfo; // Mantener por compatibilidad
  sifenAmbiente?: 'DEV' | 'PROD'; // Ambiente SIFEN (DEV = Desarrollo, PROD = Producción)
  activo: boolean;
  creadoEn: string;
}

export interface ActividadEconomica {
  codigoPrincipal: string;
  descripcionPrincipal: string;
  codigosSecundarios?: string[];
  descripcionesSecundarias?: string[];
}

export interface CertificadoInfo {
  path: string;
  fechaExpiracion: string;
}
