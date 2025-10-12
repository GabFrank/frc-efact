export interface Cliente {
  id: number;
  empresaId: number;
  nombre: string;
  razonSocial?: string;
  ruc?: string;
  direccion?: string;
  telefono?: string;
  email?: string;
  tributa: boolean;
  tipoContribuyente?: 'PF' | 'PJ' | 'EG';
  activo: boolean;
}
