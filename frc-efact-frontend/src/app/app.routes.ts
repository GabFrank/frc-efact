import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { noAuthGuard } from './guards/no-auth.guard';

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
    path: 'dashboard',
    canActivate: [authGuard],
    loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES)
  },
  {
    path: 'empresas',
    canActivate: [authGuard],
    loadChildren: () => import('./features/empresas/empresas.routes').then(m => m.EMPRESAS_ROUTES)
  },
  {
    path: 'timbrados',
    canActivate: [authGuard],
    loadChildren: () => import('./features/timbrados/timbrados.routes').then(m => m.TIMBRADOS_ROUTES)
  },
  {
    path: 'productos',
    canActivate: [authGuard],
    loadChildren: () => import('./features/productos/productos.routes').then(m => m.PRODUCTOS_ROUTES)
  },
  {
    path: 'clientes',
    canActivate: [authGuard],
    loadChildren: () => import('./features/clientes/clientes.routes').then(m => m.CLIENTES_ROUTES)
  },
  {
    path: 'facturacion',
    canActivate: [authGuard],
    loadChildren: () => import('./features/facturacion/facturacion.routes').then(m => m.FACTURACION_ROUTES)
  },
  {
    path: 'documentos',
    canActivate: [authGuard],
    loadChildren: () => import('./features/documentos/documentos.routes').then(m => m.DOCUMENTOS_ROUTES)
  },
  {
    path: 'reportes',
    canActivate: [authGuard],
    loadChildren: () => import('./features/reportes/reportes.routes').then(m => m.REPORTES_ROUTES)
  },
  {
    path: 'auditoria',
    canActivate: [authGuard],
    loadChildren: () => import('./features/auditoria/auditoria.routes').then(m => m.AUDITORIA_ROUTES)
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
