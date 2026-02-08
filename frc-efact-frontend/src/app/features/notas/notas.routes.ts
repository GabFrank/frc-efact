import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';

export const NOTAS_ROUTES: Routes = [
  {
    path: 'notas-credito',
    canActivate: [authGuard, roleGuard],
    loadComponent: () => import('./nota-credito-list.component').then(m => m.NotaCreditoListComponent),
    data: { 
      title: 'Notas de Crédito',
      roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
    }
  },
  {
    path: 'notas-debito',
    canActivate: [authGuard, roleGuard],
    loadComponent: () => import('./nota-debito-list.component').then(m => m.NotaDebitoListComponent),
    data: { 
      title: 'Notas de Débito',
      roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
    }
  },
  {
    path: 'notas-remision',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./nota-remision-list.component').then(m => m.NotaRemisionListComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Notas de Remisión',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
        }
      },
      {
        path: 'nueva',
        loadComponent: () => import('./nota-remision-form.component').then(m => m.NotaRemisionFormComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Nueva Nota de Remisión',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']
        }
      },
      {
        path: ':id',
        loadComponent: () => import('./nota-remision-form.component').then(m => m.NotaRemisionFormComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Editar Nota de Remisión',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']
        }
      }
    ]
  }
];

