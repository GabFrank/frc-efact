export interface User {
  id: number;
  username: string;
  email: string;
  createdAt: string;
  lastLogin?: string;
  roles?: string[];
}

export interface UsuarioEmpresa {
  id: number;
  usuarioId: number;
  empresaId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'LECTOR';
  activo: boolean;
  usuario?: User;
  creadoEn: string;
  actualizadoEn: string;
}

export interface AsignarUsuarioEmpresaRequest {
  usuarioId: number;
  empresaId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'LECTOR';
}
