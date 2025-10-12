import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';

export const DOCUMENTOS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => 
          import('./documento-electronico-list.component').then(m => m.DocumentoElectronicoListComponent),
        title: 'Documentos Electrónicos'
      },
      {
        path: 'lotes',
        loadComponent: () => 
          import('./lote-list.component').then(m => m.LoteListComponent),
        title: 'Lotes de Documentos'
      },
      {
        path: 'lotes/nuevo',
        loadComponent: () => 
          import('./lote-form.component').then(m => m.LoteFormComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Crear Lote',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']
        }
      },
      {
        path: ':id',
        loadComponent: () => 
          import('./documento-electronico-view.component').then(m => m.DocumentoElectronicoViewComponent),
        title: 'Ver Documento Electrónico'
      }
    ]
  }
];
