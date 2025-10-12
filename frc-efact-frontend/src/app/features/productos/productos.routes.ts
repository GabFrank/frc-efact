import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';

export const PRODUCTOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./productos-list.component').then(m => m.ProductosListComponent),
    canActivate: [authGuard]
  }
];
