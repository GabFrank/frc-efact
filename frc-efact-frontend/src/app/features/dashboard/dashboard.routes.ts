import { Routes } from '@angular/router';
import { DashboardUsuarioComponent } from './dashboard-usuario.component';
import { DashboardEmpresaComponent } from './dashboard-empresa.component';
import { authGuard } from '../../guards/auth.guard';
import { empresaAccessGuard } from '../../guards/empresa-access.guard';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'usuario',
    pathMatch: 'full'
  },
  {
    path: 'usuario',
    component: DashboardUsuarioComponent,
    canActivate: [authGuard],
    title: 'Mi Dashboard'
  },
  {
    path: 'empresa/:id',
    component: DashboardEmpresaComponent,
    canActivate: [authGuard, empresaAccessGuard],
    title: 'Dashboard de Empresa'
  }
];
