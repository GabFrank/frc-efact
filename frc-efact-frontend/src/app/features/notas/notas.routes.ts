import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';

export const NOTAS_ROUTES: Routes = [
  {
    path: 'notas-credito',
    canActivate: [authGuard],
    loadComponent: () => import('./nota-credito-list.component').then(m => m.NotaCreditoListComponent),
    data: { title: 'Notas de Crédito' }
  },
  {
    path: 'notas-debito',
    canActivate: [authGuard],
    loadComponent: () => import('./nota-debito-list.component').then(m => m.NotaDebitoListComponent),
    data: { title: 'Notas de Débito' }
  },
  {
    path: 'notas-remision',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./nota-remision-list.component').then(m => m.NotaRemisionListComponent),
        data: { title: 'Notas de Remisión' }
      },
      {
        path: 'nueva',
        loadComponent: () => import('./nota-remision-form.component').then(m => m.NotaRemisionFormComponent),
        data: { title: 'Nueva Nota de Remisión' }
      },
      {
        path: ':id',
        loadComponent: () => import('./nota-remision-form.component').then(m => m.NotaRemisionFormComponent),
        data: { title: 'Editar Nota de Remisión' }
      }
    ]
  }
];

