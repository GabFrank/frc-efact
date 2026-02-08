import { Routes } from '@angular/router';
import { authGuard } from '../../../guards/auth.guard';
import { roleGuard } from '../../../guards/role.guard';
import { empresaSelectedGuard } from '../../../guards/empresa-selected.guard';

export const VEHICULOS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard, empresaSelectedGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('../vehiculos-list.component').then(m => m.VehiculosListComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Vehículos',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
        }
      }
    ]
  }
];