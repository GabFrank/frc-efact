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
        loadComponent: () => import('./empresa-form.component').then(m => m.EmpresaFormComponent)
      },
      {
        path: ':id/edit',
        loadComponent: () => import('./empresa-form.component').then(m => m.EmpresaFormComponent)
      },
      {
        path: ':id/usuarios',
        loadComponent: () => import('./usuario-empresa.component').then(m => m.UsuarioEmpresaComponent)
      }
    ]
  }
];
