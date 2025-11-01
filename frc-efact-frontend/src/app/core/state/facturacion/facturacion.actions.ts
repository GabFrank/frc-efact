import { createAction, props } from '@ngrx/store';
import { FacturaLegal } from '../../../models/factura.model';
import { FacturaFiltro } from '../../../models/reporte.model';

// Load facturas
export const loadFacturas = createAction(
  '[Facturacion] Load Facturas',
  props<{ filtro?: FacturaFiltro }>()
);

export const loadFacturasSuccess = createAction(
  '[Facturacion] Load Facturas Success',
  props<{ facturas: FacturaLegal[] }>()
);

export const loadFacturasFailure = createAction(
  '[Facturacion] Load Facturas Failure',
  props<{ error: string }>()
);

// Create factura
export const createFactura = createAction(
  '[Facturacion] Create Factura',
  props<{ factura: Partial<FacturaLegal> }>()
);

export const createFacturaSuccess = createAction(
  '[Facturacion] Create Factura Success',
  props<{ factura: FacturaLegal }>()
);

export const createFacturaFailure = createAction(
  '[Facturacion] Create Factura Failure',
  props<{ error: string }>()
);

// Update factura
export const updateFactura = createAction(
  '[Facturacion] Update Factura',
  props<{ id: number; factura: Partial<FacturaLegal> }>()
);

export const updateFacturaSuccess = createAction(
  '[Facturacion] Update Factura Success',
  props<{ factura: FacturaLegal }>()
);

export const updateFacturaFailure = createAction(
  '[Facturacion] Update Factura Failure',
  props<{ error: string }>()
);

// Delete factura
export const deleteFactura = createAction(
  '[Facturacion] Delete Factura',
  props<{ id: number }>()
);

export const deleteFacturaSuccess = createAction(
  '[Facturacion] Delete Factura Success',
  props<{ id: number }>()
);

export const deleteFacturaFailure = createAction(
  '[Facturacion] Delete Factura Failure',
  props<{ error: string }>()
);
