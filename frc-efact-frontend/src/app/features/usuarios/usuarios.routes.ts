import { Routes } from '@angular/router';
import { roleGuard } from '../../guards/role.guard';
import { UsuariosListComponent } from './usuarios-list.component';
import { UsuarioFormComponent } from './usuario-form.component';
import { UsuarioDetailComponent } from './usuario-detail.component';

export const USUARIOS_ROUTES: Routes = [
  {
    path: '',
    component: UsuariosListComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'new',
    component: UsuarioFormComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: ':id/edit',
    component: UsuarioFormComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: ':id',
    component: UsuarioDetailComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  }
];