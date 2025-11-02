import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';
import { roleGuard } from '../../guards/role.guard';

export const EMPRESAS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./empresas-list.component').then(m => m.EmpresasListComponent)
      },
      {
        path: 'new',
        loadComponent: () => import('./empresa-form.component').then(m => m.EmpresaFormComponent)
      },
      {
        path: ':id',
        loadComponent: () => import('./empresa-usuarios.component').then(m => m.EmpresaUsuariosComponent)
      },
      {
        path: ':id/edit',
        loadComponent: () => import('./empresa-form.component').then(m => m.EmpresaFormComponent)
      },
      {
        path: ':id/usuarios',
        loadComponent: () => import('./empresa-usuarios.component').then(m => m.EmpresaUsuariosComponent)
      },
      {
        path: ':id/timbrados',
        loadComponent: () => import('./empresa-timbrados.component').then(m => m.EmpresaTimbradosComponent)
      },
      {
        path: ':id/productos',
        loadComponent: () => import('../productos/productos-list.component').then(m => m.ProductosListComponent)
      }
    ]
  }
];
