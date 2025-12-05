import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs/operators';
import { selectSelectedEmpresa } from '../core/state/empresas/empresas.selectors';

export const empresaSelectedGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const store = inject(Store);
  const router = inject(Router);
  
  // Rutas que no requieren empresa seleccionada
  const publicRoutes = ['dashboard', 'empresas', 'perfil', 'usuarios'];
  const currentPath = route.routeConfig?.path || '';
  
  // Verificar si la ruta actual es pública
  const isPublicRoute = publicRoutes.some(publicPath => 
    currentPath === publicPath || currentPath.startsWith(publicPath + '/')
  );
  
  if (isPublicRoute) {
    return true;
  }
  
  // Para otras rutas, verificar que haya una empresa seleccionada
  return store.select(selectSelectedEmpresa).pipe(
    take(1),
    map((selectedEmpresa): boolean | UrlTree =>
      selectedEmpresa ? true : router.createUrlTree(['/dashboard'])
    )
  );
};

