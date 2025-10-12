/**
 * Modelo de auditoría para el frontend
 */

export interface AuditLog {
  id: number;
  usuario: {
    id: number;
    username: string;
    nombreCompleto?: string;
  };
  empresa?: {
    id: number;
    razonSocial: string;
  };
  entidadTipo: string;
  entidadId: number;
  accion: AccionEnum;
  fechaHora: string;
  valoresAnteriores?: Record<string, any>;
  valoresNuevos?: Record<string, any>;
  ipAddress?: string;
  userAgent?: string;
  descripcion?: string;
}

export enum AccionEnum {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  READ = 'READ'
}

export interface AuditLogFilter {
  usuarioId?: number;
  empresaId?: number;
  entidadTipo?: string;
  accion?: AccionEnum;
  fechaDesde?: string;
  fechaHasta?: string;
  page?: number;
  size?: number;
}

export interface AuditLogPage {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
