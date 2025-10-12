import { Routes } from '@angular/router';

export const REPORTES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./reportes-list.component').then(m => m.ReportesListComponent)
  },
  {
    path: 'facturas',
    loadComponent: () => import('./reporte-facturas.component').then(m => m.ReporteFacturasComponent)
  },
  {
    path: 'clientes',
    loadComponent: () => import('./reporte-clientes.component').then(m => m.ReporteClientesComponent)
  },
  {
    path: 'productos',
    loadComponent: () => import('./reporte-productos.component').then(m => m.ReporteProductosComponent)
  },
  {
    path: 'usuarios',
    loadComponent: () => import('./reporte-usuarios.component').then(m => m.ReporteUsuariosComponent)
  }
];
