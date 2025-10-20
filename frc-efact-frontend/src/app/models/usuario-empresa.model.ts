export interface UsuarioEmpresa {
  id: number;
  usuarioId: number;
  empresaId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'LECTOR';
  activo: boolean;
  usuarioUsername?: string;
  empresaRazonSocial?: string;
  creadoEn: string;
  actualizadoEn: string;
}

export interface AsignarUsuarioEmpresaRequest {
  usuarioId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'LECTOR';
}
