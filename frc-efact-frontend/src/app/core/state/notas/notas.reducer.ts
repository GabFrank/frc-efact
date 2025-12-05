import { createReducer, on } from '@ngrx/store';
import { EntityState, EntityAdapter, createEntityAdapter } from '@ngrx/entity';
import { NotaCredito, NotaDebito, NotaRemision } from '../../../models/nota.model';
import * as NotasActions from './notas.actions';

export interface NotasState extends EntityState<NotaCredito | NotaDebito | NotaRemision> {
  loading: boolean;
  error: string | null;
  totalNotasCredito: number;
  totalNotasDebito: number;
  totalNotasRemision: number;
}

export const adapter: EntityAdapter<NotaCredito | NotaDebito | NotaRemision> = createEntityAdapter<NotaCredito | NotaDebito | NotaRemision>({
  selectId: (nota: NotaCredito | NotaDebito | NotaRemision) => nota.id || 0
});

export const initialState: NotasState = adapter.getInitialState({
  loading: false,
  error: null,
  totalNotasCredito: 0,
  totalNotasDebito: 0,
  totalNotasRemision: 0
});

export const notasReducer = createReducer(
  initialState,
  
  // Nota Credito
  on(NotasActions.loadNotasCredito, (state) => ({ ...state, loading: true, error: null })),
  on(NotasActions.loadNotasCreditoSuccess, (state, { notas, total }) => 
    adapter.upsertMany(notas, { ...state, loading: false, totalNotasCredito: total })),
  on(NotasActions.loadNotasCreditoFailure, (state, { error }) => 
    ({ ...state, loading: false, error: error.message || 'Error al cargar notas de crédito' })),
  
  on(NotasActions.createNotaCreditoSuccess, (state, { nota }) => 
    adapter.addOne(nota, { ...state, loading: false })),
  on(NotasActions.createNotaCreditoFailure, (state, { error }) => 
    ({ ...state, loading: false, error: error.message || 'Error al crear nota de crédito' })),
  
  on(NotasActions.deleteNotaCreditoSuccess, (state, { id }) => 
    adapter.removeOne(id, state)),
  
  // Nota Debito
  on(NotasActions.loadNotasDebito, (state) => ({ ...state, loading: true, error: null })),
  on(NotasActions.loadNotasDebitoSuccess, (state, { notas, total }) => 
    adapter.upsertMany(notas, { ...state, loading: false, totalNotasDebito: total })),
  on(NotasActions.loadNotasDebitoFailure, (state, { error }) => 
    ({ ...state, loading: false, error: error.message || 'Error al cargar notas de débito' })),
  
  on(NotasActions.createNotaDebitoSuccess, (state, { nota }) => 
    adapter.addOne(nota, { ...state, loading: false })),
  on(NotasActions.createNotaDebitoFailure, (state, { error }) => 
    ({ ...state, loading: false, error: error.message || 'Error al crear nota de débito' })),
  
  on(NotasActions.deleteNotaDebitoSuccess, (state, { id }) => 
    adapter.removeOne(id, state)),
  
  // Nota Remision
  on(NotasActions.loadNotasRemision, (state) => ({ ...state, loading: true, error: null })),
  on(NotasActions.loadNotasRemisionSuccess, (state, { notas, total }) => 
    adapter.upsertMany(notas, { ...state, loading: false, totalNotasRemision: total })),
  on(NotasActions.loadNotasRemisionFailure, (state, { error }) => 
    ({ ...state, loading: false, error: error.message || 'Error al cargar notas de remisión' })),
  
  on(NotasActions.createNotaRemisionSuccess, (state, { nota }) => 
    adapter.addOne(nota, { ...state, loading: false })),
  on(NotasActions.createNotaRemisionFailure, (state, { error }) => 
    ({ ...state, loading: false, error: error.message || 'Error al crear nota de remisión' })),
  
  on(NotasActions.deleteNotaRemisionSuccess, (state, { id }) => 
    adapter.removeOne(id, state))
);

export const { selectAll, selectEntities, selectIds, selectTotal } = adapter.getSelectors();

