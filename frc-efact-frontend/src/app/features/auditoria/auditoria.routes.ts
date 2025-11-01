import { Routes } from '@angular/router';
import { roleGuard } from '../../guards/role.guard';

/**
 * Rutas del módulo de auditoría
 * Restringido solo a usuarios con rol ADMIN
 */
export const AUDITORIA_ROUTES: Routes = [
  {
    path: '',
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] },
    children: [
      {
        path: '',
        loadComponent: () => import('./auditoria-list.component').then(m => m.AuditoriaListComponent)
      },
      {
        path: 'entidad/:tipo/:id',
        loadComponent: () => import('./auditoria-entidad.component').then(m => m.AuditoriaEntidadComponent)
      }
    ]
  }
];
