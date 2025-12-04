import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';

export const TIMBRADOS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('../empresas/empresa-timbrados.component').then(m => m.EmpresaTimbradosComponent),
        title: 'Timbrados'
      },
      {
        path: 'new',
        loadComponent: () => import('./timbrado-form.component').then(m => m.TimbradoFormComponent),
        title: 'Nuevo Timbrado'
      },
      {
        path: ':id',
        loadComponent: () => import('./timbrado-form.component').then(m => m.TimbradoFormComponent),
        title: 'Ver Timbrado'
      },
      {
        path: ':id/edit',
        loadComponent: () => import('./timbrado-form.component').then(m => m.TimbradoFormComponent),
        title: 'Editar Timbrado'
      },
      {
        path: ':id/detalles',
        loadComponent: () => import('./timbrado-detalle-list.component').then(m => m.TimbradoDetalleListComponent),
        title: 'Puntos de Expedición'
      },
      {
        path: ':id/detalles/new',
        loadComponent: () => import('./timbrado-detalle-form.component').then(m => m.TimbradoDetalleFormComponent),
        title: 'Nuevo Punto de Expedición'
      },
      {
        path: ':id/detalles/:detalleId',
        loadComponent: () => import('./timbrado-detalle-form.component').then(m => m.TimbradoDetalleFormComponent),
        title: 'Ver Punto de Expedición'
      },
      {
        path: ':id/detalles/:detalleId/edit',
        loadComponent: () => import('./timbrado-detalle-form.component').then(m => m.TimbradoDetalleFormComponent),
        title: 'Editar Punto de Expedición'
      }
    ]
  }
];
