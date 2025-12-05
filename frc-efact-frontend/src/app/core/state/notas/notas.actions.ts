import { createAction, props } from '@ngrx/store';
import { NotaCredito, NotaDebito, NotaRemision } from '../../../models/nota.model';

// Nota Credito
export const loadNotasCredito = createAction(
  '[Notas] Load Notas Credito',
  props<{ empresaId: number, page?: number, size?: number }>()
);

export const loadNotasCreditoSuccess = createAction(
  '[Notas] Load Notas Credito Success',
  props<{ notas: NotaCredito[], total: number }>()
);

export const loadNotasCreditoFailure = createAction(
  '[Notas] Load Notas Credito Failure',
  props<{ error: any }>()
);

export const createNotaCredito = createAction(
  '[Notas] Create Nota Credito',
  props<{ nota: NotaCredito }>()
);

export const createNotaCreditoSuccess = createAction(
  '[Notas] Create Nota Credito Success',
  props<{ nota: NotaCredito }>()
);

export const createNotaCreditoFailure = createAction(
  '[Notas] Create Nota Credito Failure',
  props<{ error: any }>()
);

export const deleteNotaCredito = createAction(
  '[Notas] Delete Nota Credito',
  props<{ id: number }>()
);

export const deleteNotaCreditoSuccess = createAction(
  '[Notas] Delete Nota Credito Success',
  props<{ id: number }>()
);

// Nota Debito
export const loadNotasDebito = createAction(
  '[Notas] Load Notas Debito',
  props<{ empresaId: number, page?: number, size?: number }>()
);

export const loadNotasDebitoSuccess = createAction(
  '[Notas] Load Notas Debito Success',
  props<{ notas: NotaDebito[], total: number }>()
);

export const loadNotasDebitoFailure = createAction(
  '[Notas] Load Notas Debito Failure',
  props<{ error: any }>()
);

export const createNotaDebito = createAction(
  '[Notas] Create Nota Debito',
  props<{ nota: NotaDebito }>()
);

export const createNotaDebitoSuccess = createAction(
  '[Notas] Create Nota Debito Success',
  props<{ nota: NotaDebito }>()
);

export const createNotaDebitoFailure = createAction(
  '[Notas] Create Nota Debito Failure',
  props<{ error: any }>()
);

export const deleteNotaDebito = createAction(
  '[Notas] Delete Nota Debito',
  props<{ id: number }>()
);

export const deleteNotaDebitoSuccess = createAction(
  '[Notas] Delete Nota Debito Success',
  props<{ id: number }>()
);

// Nota Remision
export const loadNotasRemision = createAction(
  '[Notas] Load Notas Remision',
  props<{ empresaId: number, page?: number, size?: number }>()
);

export const loadNotasRemisionSuccess = createAction(
  '[Notas] Load Notas Remision Success',
  props<{ notas: NotaRemision[], total: number }>()
);

export const loadNotasRemisionFailure = createAction(
  '[Notas] Load Notas Remision Failure',
  props<{ error: any }>()
);

export const createNotaRemision = createAction(
  '[Notas] Create Nota Remision',
  props<{ nota: NotaRemision }>()
);

export const createNotaRemisionSuccess = createAction(
  '[Notas] Create Nota Remision Success',
  props<{ nota: NotaRemision }>()
);

export const createNotaRemisionFailure = createAction(
  '[Notas] Create Nota Remision Failure',
  props<{ error: any }>()
);

export const deleteNotaRemision = createAction(
  '[Notas] Delete Nota Remision',
  props<{ id: number }>()
);

export const deleteNotaRemisionSuccess = createAction(
  '[Notas] Delete Nota Remision Success',
  props<{ id: number }>()
);

