import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';

export const CLIENTES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./clientes-list.component').then(m => m.ClientesListComponent),
    canActivate: [authGuard]
  }
];
