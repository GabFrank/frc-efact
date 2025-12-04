export interface User {
  id: number;
  username: string;
  email: string;
  auth0Id?: string;
  isActive: boolean;
  roles: Role[] | string[]; // Puede ser array de objetos Role o array de strings
  ultimoLogin?: string; // Cambiado de lastLogin
  creadoEn: string; // Cambiado de createdAt
  actualizadoEn: string; // Cambiado de updatedAt
  failedLoginAttempts?: number;
  lockedUntil?: string;
  empresas?: UsuarioEmpresa[];
  createdBy?: number;
  updatedBy?: number;
}

export interface UsuarioEmpresa {
  id: number;
  usuarioId: number;
  empresaId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'FACTURADOR' | 'LECTOR';
  activo: boolean;
  usuario?: User;
  creadoEn: string;
  actualizadoEn: string;
}

export interface AsignarUsuarioEmpresaRequest {
  usuarioId: number;
  empresaId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'FACTURADOR' | 'LECTOR';
}
export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  roles: string[];
  isActive: boolean;
}

export interface UpdateUserRequest {
  username?: string;
  email?: string;
  roles?: string[];
  isActive?: boolean;
}

export interface UpdateProfileRequest {
  username?: string;
  email?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ResetPasswordRequest {
  userId: number;
  newPassword: string;
  forcePasswordChange?: boolean;
}

export interface Role {
  id: number;
  nombre: string;
  descripcion: string;
  creadoEn: string;
}

export interface UserRole {
  userId: number;
  roleId: number;
  assignedAt: string;
  assignedBy: number;
}

export interface UserSearchRequest {
  searchTerm?: string;
  role?: string;
  isActive?: boolean;
  page?: number;
  size?: number;
}
