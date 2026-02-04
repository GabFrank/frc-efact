import { createAction, props } from '@ngrx/store';
import { Empresa } from '../../../models/empresa.model';

// Load empresas
export const loadEmpresas = createAction('[Empresas] Load Empresas');

export const loadEmpresasSuccess = createAction(
  '[Empresas] Load Empresas Success',
  props<{ empresas: Empresa[] }>()
);

export const loadEmpresasFailure = createAction(
  '[Empresas] Load Empresas Failure',
  props<{ error: string }>()
);

// Load mis empresas
export const loadMisEmpresas = createAction('[Empresas] Load Mis Empresas');

export const loadMisEmpresasSuccess = createAction(
  '[Empresas] Load Mis Empresas Success',
  props<{ empresas: Empresa[] }>()
);

export const loadMisEmpresasFailure = createAction(
  '[Empresas] Load Mis Empresas Failure',
  props<{ error: string }>()
);

// Select empresa
export const selectEmpresa = createAction(
  '[Empresas] Select Empresa',
  props<{ empresaId: number }>()
);

// Create empresa
export const createEmpresa = createAction(
  '[Empresas] Create Empresa',
  props<{ empresa: Partial<Empresa>; certificadoFile?: File; certificadoPassword?: string }>()
);

export const createEmpresaSuccess = createAction(
  '[Empresas] Create Empresa Success',
  props<{ empresa: Empresa }>()
);

export const createEmpresaFailure = createAction(
  '[Empresas] Create Empresa Failure',
  props<{ error: string }>()
);

// Update empresa
export const updateEmpresa = createAction(
  '[Empresas] Update Empresa',
  props<{ id: number; empresa: Partial<Empresa>; certificadoFile?: File; certificadoPassword?: string }>()
);

export const updateEmpresaSuccess = createAction(
  '[Empresas] Update Empresa Success',
  props<{ empresa: Empresa }>()
);

export const updateEmpresaFailure = createAction(
  '[Empresas] Update Empresa Failure',
  props<{ error: string }>()
);

// Delete empresa
export const deleteEmpresa = createAction(
  '[Empresas] Delete Empresa',
  props<{ id: number }>()
);

export const deleteEmpresaSuccess = createAction(
  '[Empresas] Delete Empresa Success',
  props<{ id: number }>()
);

export const deleteEmpresaFailure = createAction(
  '[Empresas] Delete Empresa Failure',
  props<{ error: string }>()
);

// Export all actions as EmpresasActions
export const EmpresasActions = {
  loadEmpresas,
  loadEmpresasSuccess,
  loadEmpresasFailure,
  loadMisEmpresas,
  loadMisEmpresasSuccess,
  loadMisEmpresasFailure,
  selectEmpresa,
  createEmpresa,
  createEmpresaSuccess,
  createEmpresaFailure,
  updateEmpresa,
  updateEmpresaSuccess,
  updateEmpresaFailure,
  deleteEmpresa,
  deleteEmpresaSuccess,
  deleteEmpresaFailure
};
