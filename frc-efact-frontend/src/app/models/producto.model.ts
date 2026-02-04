/**
 * Tipo de transacción u operación comercial según Manual Técnico SIFEN v1.50.
 * Campo D011 (iTipTra): código del tipo de transacción.
 * Campo D012 (dDesTipTra): descripción del tipo de transacción.
 */
export enum TipoTransaccionProducto {
  VENTA_MERCADERIA = 'VENTA_MERCADERIA',              // Código 1: Venta de mercadería
  PRESTACION_SERVICIOS = 'PRESTACION_SERVICIOS',      // Código 2: Prestación de servicios
  MIXTO = 'MIXTO',                                    // Código 3: Mixto (Venta de mercadería y servicios)
  VENTA_ACTIVO_FIJO = 'VENTA_ACTIVO_FIJO',           // Código 4: Venta de activo fijo
  VENTA_DIVISAS = 'VENTA_DIVISAS',                    // Código 5: Venta de divisas
  COMPRA_DIVISAS = 'COMPRA_DIVISAS',                  // Código 6: Compra de divisas
  PROMOCION_MUESTRAS = 'PROMOCION_MUESTRAS',         // Código 7: Promoción o entrega de muestras
  DONACION = 'DONACION',                              // Código 8: Donación
  ANTICIPO = 'ANTICIPO',                              // Código 9: Anticipo
  COMPRA_PRODUCTOS = 'COMPRA_PRODUCTOS',              // Código 10: Compra de productos
  COMPRA_SERVICIOS = 'COMPRA_SERVICIOS',              // Código 11: Compra de servicios
  VENTA_CREDITO_FISCAL = 'VENTA_CREDITO_FISCAL',      // Código 12: Venta de crédito fiscal
  MUESTRAS_MEDICAS = 'MUESTRAS_MEDICAS'               // Código 13: Muestras médicas
}

/**
 * Mapa de códigos numéricos SIFEN a tipos de transacción
 */
export const TIPO_TRANSACCION_CODIGOS: Record<TipoTransaccionProducto, number> = {
  [TipoTransaccionProducto.VENTA_MERCADERIA]: 1,
  [TipoTransaccionProducto.PRESTACION_SERVICIOS]: 2,
  [TipoTransaccionProducto.MIXTO]: 3,
  [TipoTransaccionProducto.VENTA_ACTIVO_FIJO]: 4,
  [TipoTransaccionProducto.VENTA_DIVISAS]: 5,
  [TipoTransaccionProducto.COMPRA_DIVISAS]: 6,
  [TipoTransaccionProducto.PROMOCION_MUESTRAS]: 7,
  [TipoTransaccionProducto.DONACION]: 8,
  [TipoTransaccionProducto.ANTICIPO]: 9,
  [TipoTransaccionProducto.COMPRA_PRODUCTOS]: 10,
  [TipoTransaccionProducto.COMPRA_SERVICIOS]: 11,
  [TipoTransaccionProducto.VENTA_CREDITO_FISCAL]: 12,
  [TipoTransaccionProducto.MUESTRAS_MEDICAS]: 13
};

/**
 * Descripciones de los tipos de transacción según SIFEN v1.50
 */
export const TIPO_TRANSACCION_DESCRIPCIONES: Record<TipoTransaccionProducto, string> = {
  [TipoTransaccionProducto.VENTA_MERCADERIA]: 'Venta de mercadería',
  [TipoTransaccionProducto.PRESTACION_SERVICIOS]: 'Prestación de servicios',
  [TipoTransaccionProducto.MIXTO]: 'Mixto (Venta de mercadería y servicios)',
  [TipoTransaccionProducto.VENTA_ACTIVO_FIJO]: 'Venta de activo fijo',
  [TipoTransaccionProducto.VENTA_DIVISAS]: 'Venta de divisas',
  [TipoTransaccionProducto.COMPRA_DIVISAS]: 'Compra de divisas',
  [TipoTransaccionProducto.PROMOCION_MUESTRAS]: 'Promoción o entrega de muestras',
  [TipoTransaccionProducto.DONACION]: 'Donación',
  [TipoTransaccionProducto.ANTICIPO]: 'Anticipo',
  [TipoTransaccionProducto.COMPRA_PRODUCTOS]: 'Compra de productos',
  [TipoTransaccionProducto.COMPRA_SERVICIOS]: 'Compra de servicios',
  [TipoTransaccionProducto.VENTA_CREDITO_FISCAL]: 'Venta de crédito fiscal',
  [TipoTransaccionProducto.MUESTRAS_MEDICAS]: 'Muestras médicas'
};

export interface Producto {
  id: number;
  empresaId: number;
  codigo?: string;
  descripcion: string;
  precio: number;
  iva: number;
  balanza: boolean;
  activo: boolean;
  tipoTransaccion: TipoTransaccionProducto;
  unidadMedida: string;
  creadoEn?: string;
  creadoPor?: string;
  actualizadoEn?: string;
  actualizadoPor?: string;
}
