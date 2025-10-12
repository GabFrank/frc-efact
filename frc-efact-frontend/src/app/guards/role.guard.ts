import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { Store } from '@ngrx/store';
import { map, take } from 'rxjs/operators';
import { selectUser } from '../core/state/auth/auth.selectors';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const store = inject(Store);
  const router = inject(Router);
  
  const requiredRoles = route.data['roles'] as string[];
  
  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }
  
  return store.select(selectUser).pipe(
    take(1),
    map(user => {
      if (!user) {
        router.navigate(['/login']);
        return false;
      }
      
      // Check if user has any of the required roles
      const userRoles = user.roles || [];
      const hasRole = requiredRoles.some(role => 
        userRoles.includes(role)
      );
      
      if (!hasRole) {
        router.navigate(['/dashboard']);
        return false;
      }
      
      return true;
    })
  );
};
