export interface FacturaLegal {
  id?: number;
  empresaId: number;
  timbradoDetalleId: number;
  clienteId?: number;
  numeroFactura?: number;
  fecha: string;
  credito: boolean;
  
  // Datos cliente
  nombre: string;
  ruc?: string;
  direccion?: string;
  
  // Items
  items: FacturaLegalItem[];
  
  // Totales
  ivaParcial0: number;
  ivaParcial5: number;
  ivaParcial10: number;
  totalParcial0: number;
  totalParcial5: number;
  totalParcial10: number;
  descuentoFinal: number;
  totalParcial: number;
  totalFinal: number;
}

export interface FacturaLegalItem {
  id?: number;
  productoId?: number;
  cantidad: number;
  descripcion: string;
  precioUnitario: number;
  total: number;
}
