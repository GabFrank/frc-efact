export interface Chofer {
  id?: number;
  empresaId: number;
  nombre: string;
  documento?: string;
  direccion?: string;
  activo: boolean;
}
