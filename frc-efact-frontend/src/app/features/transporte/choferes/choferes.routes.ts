import { Routes } from '@angular/router';
import { authGuard } from '../../../guards/auth.guard';
import { roleGuard } from '../../../guards/role.guard';
import { empresaSelectedGuard } from '../../../guards/empresa-selected.guard';

export const CHOFERES_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard, empresaSelectedGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('../choferes-list.component').then(m => m.ChoferesListComponent),
        canActivate: [roleGuard],
        data: { 
          title: 'Choferes',
          roles: ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR']
        }
      }
    ]
  }
];