export interface Empresa {
  id: number;
  razonSocial: string;
  ruc: string;
  nombreFantasia?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
  tipoSociedad?: string;
  domicilioFiscal: DomicilioFiscal;
  actividadEconomica: ActividadEconomica;
  certificado?: CertificadoInfo;
  activo: boolean;
  creadoEn: string;
}

export interface DomicilioFiscal {
  departamento: string;
  ciudad: string;
  codigoCiudad: string;
  localidad: string;
  barrio: string;
  direccion: string;
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
