import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';

export const EMPRESAS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./empresas-list.component').then(m => m.EmpresasListComponent)
  },
  {
    path: 'new',
    canActivate: [authGuard],
    loadComponent: () => import('./empresa-form.component').then(m => m.EmpresaFormComponent)
  },
  {
    path: ':id',
    canActivate: [authGuard],
    loadComponent: () => import('./empresa-usuarios.component').then(m => m.EmpresaUsuariosComponent)
  },
  {
    path: ':id/edit',
    canActivate: [authGuard],
    loadComponent: () => import('./empresa-form.component').then(m => m.EmpresaFormComponent)
  },
  {
    path: ':id/usuarios',
    canActivate: [authGuard],
    loadComponent: () => import('./empresa-usuarios.component').then(m => m.EmpresaUsuariosComponent)
  },
  {
    path: ':id/timbrados',
    canActivate: [authGuard],
    loadComponent: () => import('./empresa-timbrados.component').then(m => m.EmpresaTimbradosComponent)
  },
  {
    path: ':id/productos',
    canActivate: [authGuard],
    loadComponent: () => import('../productos/productos-list.component').then(m => m.ProductosListComponent)
  }
];
