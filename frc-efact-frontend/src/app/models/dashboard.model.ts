export interface DashboardUsuario {
  usuarioId: number;
  nombreCompleto: string;
  cantidadEmpresas: number;
  ultimoAcceso?: string;
  facturasCreadasMesActual: number;
  ultimasActividades: ActividadReciente[];
}

export interface ActividadReciente {
  id: number;
  accion: 'CREATE' | 'UPDATE' | 'DELETE' | 'LOGIN' | 'LOGOUT';
  entidadTipo: string;
  entidadId?: number;
  descripcion: string;
  fechaHora: string;
  empresaNombre?: string;
}

export interface DashboardEmpresa {
  empresaId: number;
  razonSocial: string;
  totalFacturasEmitidas: number;
  totalGuaraniesMesActual: number;
  totalesPorIva: TotalesPorIva;
  top10Clientes: ClienteRanking[];
  top10Productos: ProductoRanking[];
  facturasAprobadas: FacturasAprobadas;
  facturasCanceladas: FacturasCanceladas;
}

export interface ProductoRanking {
  productoId: number;
  codigo?: string;
  descripcion: string;
  cantidadVendida: number;
  montoTotal: number;
}

export interface TotalesPorIva {
  totalIva10: number;
  totalIva5: number;
  totalIva0: number;
  totalGeneral: number;
}

export interface ClienteRanking {
  clienteId: number;
  nombre: string;
  ruc?: string;
  cantidadFacturas: number;
  montoTotal: number;
}

export interface FacturasAprobadas {
  cantidad: number;
  totalGs: number;
  totalIva10: number;
  totalIva5: number;
  totalExentas: number;
}

export interface FacturasCanceladas {
  cantidad: number;
  totalGs: number;
}
