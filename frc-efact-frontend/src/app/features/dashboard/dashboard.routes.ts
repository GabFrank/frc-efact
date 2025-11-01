import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { DashboardUsuarioComponent } from './dashboard-usuario.component';
import { DashboardEmpresaComponent } from './dashboard-empresa.component';
import { authGuard } from '../../guards/auth.guard';
import { empresaAccessGuard } from '../../guards/empresa-access.guard';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    component: DashboardComponent,
    canActivate: [authGuard],
    title: 'Dashboard'
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
