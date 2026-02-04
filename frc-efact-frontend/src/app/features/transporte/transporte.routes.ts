import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';

export const TRANSPORTE_ROUTES: Routes = [
  {
    path: 'vehiculos/empresa/:empresaId',
    canActivate: [authGuard],
    loadComponent: () => import('./vehiculos-list.component').then(m => m.VehiculosListComponent),
    data: { title: 'Gestión de Vehículos' }
  },
  {
    path: 'choferes/empresa/:empresaId',
    canActivate: [authGuard],
    loadComponent: () => import('./choferes-list.component').then(m => m.ChoferesListComponent),
    data: { title: 'Gestión de Choferes' }
  }
];
