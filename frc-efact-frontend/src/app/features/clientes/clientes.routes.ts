import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';
import { empresaSelectedGuard } from '../../guards/empresa-selected.guard';

export const CLIENTES_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard, empresaSelectedGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./clientes-list.component').then(m => m.ClientesListComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Clientes',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
        }
      }
    ]
  }
];