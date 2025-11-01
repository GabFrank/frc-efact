import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { selectAllEmpresas } from '../core/state/empresas/empresas.selectors';
import { selectUser } from '../core/state/auth/auth.selectors';

export const empresaAccessGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const store = inject(Store);
  const router = inject(Router);
  
  const empresaId = route.params['empresaId'] || route.queryParams['empresaId'];
  
  if (!empresaId) {
    // If no empresaId is required, allow access
    return true;
  }
  
  return store.select(selectUser).pipe(
    take(1),
    switchMap(user => {
      if (!user) {
        router.navigate(['/login']);
        return of(false);
      }
      
      return store.select(selectAllEmpresas).pipe(
        take(1),
        map(empresas => {
          // Check if user has access to the empresa
          const hasAccess = empresas.some(empresa => 
            empresa.id === parseInt(empresaId, 10)
          );
          
          if (!hasAccess) {
            router.navigate(['/dashboard']);
            return false;
          }
          
          return true;
        })
      );
    })
  );
};
