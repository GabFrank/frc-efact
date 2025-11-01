export interface Producto {
  id: number;
  empresaId: number;
  codigo?: string;
  descripcion: string;
  precio: number;
  iva: number;
  balanza: boolean;
  activo: boolean;
}
