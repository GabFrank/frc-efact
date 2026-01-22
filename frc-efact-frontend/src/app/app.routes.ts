import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { noAuthGuard } from './guards/no-auth.guard';
import { empresaSelectedGuard } from './guards/empresa-selected.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    canActivate: [noAuthGuard],
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'empresas',
        loadChildren: () => import('./features/empresas/empresas.routes').then(m => m.EMPRESAS_ROUTES)
      },
      {
        path: 'timbrados',
        loadChildren: () => import('./features/timbrados/timbrados.routes').then(m => m.TIMBRADOS_ROUTES)
      },
      {
        path: 'clientes',
        canActivate: [empresaSelectedGuard],
        loadComponent: () => import('./features/clientes/clientes-list.component').then(m => m.ClientesListComponent)
      },
      {
        path: 'facturacion',
        canActivate: [empresaSelectedGuard],
        loadChildren: () => import('./features/facturacion/facturacion.routes').then(m => m.FACTURACION_ROUTES)
      },
      {
        path: 'documentos',
        canActivate: [empresaSelectedGuard],
        loadChildren: () => import('./features/documentos/documentos.routes').then(m => m.DOCUMENTOS_ROUTES)
      },
      {
        path: 'reportes',
        canActivate: [empresaSelectedGuard],
        loadComponent: () => import('./features/test-page.component').then(m => m.TestPageComponent)
      },
      {
        path: 'usuarios',
        loadChildren: () => import('./features/usuarios/usuarios.routes').then(m => m.USUARIOS_ROUTES)
      },
      {
        path: 'perfil',
        loadComponent: () => import('./features/usuarios/user-profile.component').then(m => m.UserProfileComponent)
      },
      {
        path: 'auditoria',
        canActivate: [empresaSelectedGuard],
        loadComponent: () => import('./features/test-page.component').then(m => m.TestPageComponent)
      },
      {
        path: 'test',
        loadComponent: () => import('./features/test-page.component').then(m => m.TestPageComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
