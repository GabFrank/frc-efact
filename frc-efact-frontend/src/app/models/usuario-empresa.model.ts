export interface UsuarioEmpresa {
  id: number;
  usuarioId: number;
  empresaId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'FACTURADOR' | 'LECTOR';
  activo: boolean;
  usuarioUsername?: string;
  usuarioEmail?: string;
  usuarioRoles?: string[];
  empresaRazonSocial?: string;
  creadoEn: string;
  actualizadoEn: string;
}

export interface AsignarUsuarioEmpresaRequest {
  usuarioId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'FACTURADOR' | 'LECTOR';
}
