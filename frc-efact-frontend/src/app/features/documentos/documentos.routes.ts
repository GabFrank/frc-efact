import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';
import { empresaSelectedGuard } from '../../guards/empresa-selected.guard';

export const DOCUMENTOS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'lista',
    pathMatch: 'full'
  },
  {
    path: 'lista',
    canActivate: [authGuard],
    loadComponent: () => 
      import('./documento-electronico-list.component').then(m => m.DocumentoElectronicoListComponent),
    title: 'Documentos Electrónicos'
  },
  {
    path: 'cancelacion',
    canActivate: [authGuard, empresaSelectedGuard],
    loadComponent: () => 
      import('./evento-cancelacion-list.component').then(m => m.EventoCancelacionListComponent),
    title: 'Eventos de Cancelación'
  },
  {
    path: 'nominacion',
    canActivate: [authGuard, empresaSelectedGuard],
    loadComponent: () => 
      import('./evento-nominacion-list.component').then(m => m.EventoNominacionListComponent),
    title: 'Eventos de Nominación'
  },
  {
    path: 'inutilizacion',
    canActivate: [authGuard, empresaSelectedGuard],
    loadComponent: () => 
      import('./evento-inutilizacion-list.component').then(m => m.EventoInutilizacionListComponent),
    title: 'Eventos de Inutilización'
  },
  {
    path: 'lotes',
    canActivate: [authGuard],
    loadComponent: () => 
      import('./lote-list.component').then(m => m.LoteListComponent),
    title: 'Lotes de Documentos'
  },
  {
    path: 'lotes/nuevo',
    canActivate: [authGuard, roleGuard],
    loadComponent: () => 
      import('./lote-form.component').then(m => m.LoteFormComponent),
    data: { 
      title: 'Crear Lote',
      roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']
    }
  },
  {
    path: ':id',
    canActivate: [authGuard],
    loadComponent: () => 
      import('./documento-electronico-view.component').then(m => m.DocumentoElectronicoViewComponent),
    title: 'Ver Documento Electrónico'
  }
];
