import { createReducer, on } from '@ngrx/store';
import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { Empresa } from '../../../models/empresa.model';
import * as EmpresasActions from './empresas.actions';

export interface EmpresasState extends EntityState<Empresa> {
  selectedEmpresaId: number | null;
  loading: boolean;
  error: string | null;
}

export const adapter: EntityAdapter<Empresa> = createEntityAdapter<Empresa>();

export const initialState: EmpresasState = adapter.getInitialState({
  selectedEmpresaId: null,
  loading: false,
  error: null
});

export const empresasReducer = createReducer(
  initialState,
  
  // Load empresas
  on(EmpresasActions.loadEmpresas, EmpresasActions.loadMisEmpresas, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  
  on(EmpresasActions.loadEmpresasSuccess, EmpresasActions.loadMisEmpresasSuccess, (state, { empresas }) =>
    adapter.setAll(empresas, {
      ...state,
      loading: false,
      error: null
    })
  ),
  
  on(EmpresasActions.loadEmpresasFailure, EmpresasActions.loadMisEmpresasFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),
  
  // Select empresa
  on(EmpresasActions.selectEmpresa, (state, { empresaId }) => ({
    ...state,
    selectedEmpresaId: empresaId
  })),
  
  // Create empresa
  on(EmpresasActions.createEmpresa, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  
  on(EmpresasActions.createEmpresaSuccess, (state, { empresa }) =>
    adapter.addOne(empresa, {
      ...state,
      loading: false,
      error: null
    })
  ),
  
  on(EmpresasActions.createEmpresaFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),
  
  // Update empresa
  on(EmpresasActions.updateEmpresa, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  
  on(EmpresasActions.updateEmpresaSuccess, (state, { empresa }) =>
    adapter.updateOne(
      { id: empresa.id, changes: empresa },
      {
        ...state,
        loading: false,
        error: null
      }
    )
  ),
  
  on(EmpresasActions.updateEmpresaFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),
  
  // Delete empresa
  on(EmpresasActions.deleteEmpresa, (state) => ({
    ...state,
    loading: true,
    error: null
  })),
  
  on(EmpresasActions.deleteEmpresaSuccess, (state, { id }) =>
    adapter.removeOne(id, {
      ...state,
      loading: false,
      error: null
    })
  ),
  
  on(EmpresasActions.deleteEmpresaFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  }))
);

export const { selectAll, selectEntities, selectIds, selectTotal } = adapter.getSelectors();
