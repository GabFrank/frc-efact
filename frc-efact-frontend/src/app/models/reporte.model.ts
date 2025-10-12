export interface FacturaReporte {
  id: number;
  numeroFactura: number;
  fecha: string;
  clienteNombre: string;
  clienteRuc?: string;
  totalFinal: number;
  estado: string;
}

export interface ClienteRanking {
  clienteId: number;
  clienteNombre: string;
  clienteRuc?: string;
  totalFacturado: number;
  cantidadFacturas: number;
}

export interface ProductoReporte {
  productoId: number;
  descripcion: string;
  cantidadVendida: number;
  montoTotal: number;
}

export interface UsuarioReporte {
  usuarioId: number;
  usuarioNombre: string;
  cantidadFacturas: number;
  totalFacturado: number;
}

export interface FacturaFiltro {
  empresaId?: number;
  fechaDesde?: string;
  fechaHasta?: string;
  clienteId?: number;
  estado?: string;
  montoMinimo?: number;
  montoMaximo?: number;
}
