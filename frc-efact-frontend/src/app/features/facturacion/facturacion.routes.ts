import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';

export const FACTURACION_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./factura-list.component').then(m => m.FacturaListComponent),
        data: { title: 'Facturas' }
      },
      {
        path: 'nueva',
        loadComponent: () => import('./factura-form.component').then(m => m.FacturaFormComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Nueva Factura',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']
        }
      },
      {
        path: ':id',
        loadComponent: () => import('./factura-view.component').then(m => m.FacturaViewComponent),
        data: { title: 'Ver Factura' }
      },
      {
        path: ':id/editar',
        loadComponent: () => import('./factura-form.component').then(m => m.FacturaFormComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Editar Factura',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR']
        }
      }
    ]
  }
];
