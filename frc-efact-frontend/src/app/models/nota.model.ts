export interface NotaCredito {
  id?: number;
  empresaId: number;
  timbradoDetalleId: number;
  clienteId?: number;
  facturaLegalId?: number; // Factura asociada
  numeroNotaCredito?: number;
  fecha: string;

  // Datos cliente
  nombre?: string;
  ruc?: string;
  direccion?: string;

  motivoEmision: string;
  descripcionMotivo: string;

  // Items
  items: NotaItem[];

  // Totales
  ivaParcial0: number;
  ivaParcial5: number;
  ivaParcial10: number;
  totalParcial0: number;
  totalParcial5: number;
  totalParcial10: number;
  descuentoFinal?: number;
  totalParcial?: number;
  totalFinal: number;

  // Moneda extranjera
  monedaExtranjera?: string;
  cambio?: number;

  activo?: boolean;

  // Info adicional
  numeroFormateado?: string;
  nombreEmpresa?: string;
  nombreCliente?: string;
  emailCliente?: string;
  numeroFacturaAsociada?: string;

  // Información de documento electrónico asociada
  documentoElectronicoId?: number;
  estadoDocumentoElectronico?: string;
  cdcDocumentoElectronico?: string;
  urlQrDocumentoElectronico?: string;
  loteDeId?: number;
}

export interface NotaDebito {
  id?: number;
  empresaId: number;
  timbradoDetalleId: number;
  clienteId?: number;
  facturaLegalId?: number;
  numeroNotaDebito?: number;
  fecha: string;

  nombre: string;
  ruc?: string;
  direccion?: string;

  motivoEmision: string;
  descripcionMotivo: string;

  items: NotaItem[];

  ivaParcial0: number;
  ivaParcial5: number;
  ivaParcial10: number;
  totalParcial0: number;
  totalParcial5: number;
  totalParcial10: number;
  totalFinal: number;

  monedaExtranjera?: string;
  cambio?: number;

  activo?: boolean;

  numeroFormateado?: string;
  nombreEmpresa?: string;
  nombreCliente?: string;
  emailCliente?: string;
  numeroFacturaAsociada?: string;

  documentoElectronicoId?: number;
  estadoDocumentoElectronico?: string;
  cdcDocumentoElectronico?: string;
  urlQrDocumentoElectronico?: string;
  loteDeId?: number;
}

export interface NotaRemision {
  id?: number;
  empresaId: number;
  timbradoDetalleId: number;
  clienteId?: number; // Destinatario principal
  facturaLegalId?: number;
  numeroNotaRemision?: number;
  fecha: string;

  // Salida
  direccionPartida: string;
  ciudadPartida: string;
  departamentoPartida: string;
  ciudadPartidaId?: number;
  distritoPartidaId?: number;
  departamentoPartidaId?: number;

  // Llegada
  nombreDestinatario: string;
  rucDestinatario?: string;
  direccionDestinatario: string;
  ciudadDestinatario: string;
  departamentoDestinatario: string;
  ciudadDestinatarioId?: number;
  distritoDestinatarioId?: number;
  departamentoDestinatarioId?: number;

  motivoEmision: string;
  fechaInicioTraslado: string;
  fechaFinTraslado: string;
  kmEstimado: number;

  tipoTransporte: string;
  modalidadTransporte: string;

  vehiculoMarca?: string;
  vehiculoMatricula?: string;

  transportistaNombre?: string;
  transportistaRuc?: string;
  transportistaDireccion?: string;

  conductorNombre?: string;
  conductorDoc?: string;
  conductorDireccion?: string;

  fechaEstimadaFactura?: string;

  items: NotaRemisionItem[];

  activo?: boolean;

  numeroFormateado?: string;
  nombreEmpresa?: string;
  nombreCliente?: string;
  emailCliente?: string;
  numeroFacturaAsociada?: string;

  documentoElectronicoId?: number;
  estadoDocumentoElectronico?: string;
  cdcDocumentoElectronico?: string;
  urlQrDocumentoElectronico?: string;
  loteDeId?: number;
}

export interface NotaItem {
  id?: number;
  productoId?: number;
  cantidad: number;
  descripcion: string;
  precioUnitario: number;
  descuento?: number;
  total: number;
  iva: number; // 0, 5, 10
  codigo?: string;
}

export interface NotaRemisionItem {
  id?: number;
  productoId?: number;
  cantidad: number;
  descripcion: string;
  unidadMedida: string;
  codigo?: string;
}

