import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';
import { empresaSelectedGuard } from '../../guards/empresa-selected.guard';

export const TIMBRADOS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard, empresaSelectedGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./timbrado-list.component').then(m => m.TimbradoListComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Timbrados',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
        }
      },
      {
        path: 'nuevo',
        loadComponent: () => import('./timbrado-form.component').then(m => m.TimbradoFormComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Nuevo Timbrado',
          roles: ['ADMIN', 'EMPRESA_ADMIN']
        }
      },
      {
        path: ':id/editar',
        loadComponent: () => import('./timbrado-form.component').then(m => m.TimbradoFormComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Editar Timbrado',
          roles: ['ADMIN', 'EMPRESA_ADMIN']
        }
      },
      {
        path: ':id',
        loadComponent: () => import('./timbrado-form.component').then(m => m.TimbradoFormComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Ver Timbrado',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
        }
      }
    ]
  }
];