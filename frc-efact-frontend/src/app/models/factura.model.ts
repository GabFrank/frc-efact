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

  // Moneda extranjera
  monedaExtranjera?: string;
  cambio?: number;

  // Información de documento electrónico asociada
  documentoElectronicoId?: number;
  estadoDocumentoElectronico?: string;
  cdcDocumentoElectronico?: string;
  urlQrDocumentoElectronico?: string;
  loteDeId?: number;
}

export interface FacturaLegalItem {
  id?: number;
  productoId?: number;
  cantidad: number;
  descripcion: string;
  precioUnitario: number;
  total: number;
  /**
   * Tasa de IVA del ítem: 0, 5 o 10. Viene del backend, que la tiene persistida desde que se
   * emitió la factura.
   *
   * NO resolverla contra el catálogo de productos. Se hacía así, y como el formulario solo carga
   * los primeros 20 productos de la empresa, un ítem cuyo producto quedaba fuera de esa página se
   * mostraba con IVA 0 y la factura entera aparecía como exenta — y guardar desde esa pantalla
   * sobreescribía los totales correctos de la cabecera con ceros.
   */
  iva: number;
}
