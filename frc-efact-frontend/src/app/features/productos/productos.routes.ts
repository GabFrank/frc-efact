import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';
import { empresaSelectedGuard } from '../../guards/empresa-selected.guard';

export const PRODUCTOS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard, empresaSelectedGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./productos-list.component').then(m => m.ProductosListComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Productos',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
        }
      }
    ]
  }
];