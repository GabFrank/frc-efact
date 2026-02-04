import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { Store } from '@ngrx/store';
import { MatSnackBar } from '@angular/material/snack-bar';
import { map, take } from 'rxjs/operators';
import { selectUser } from '../core/state/auth/auth.selectors';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const store = inject(Store);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);
  
  const requiredRoles = route.data['roles'] as string[];
  
  if (!requiredRoles || requiredRoles.length === 0) {
    return true;
  }
  
  return store.select(selectUser).pipe(
    take(1),
    map(user => {
      if (!user) {
        // User not authenticated - redirect to login
        snackBar.open('Debe iniciar sesión para acceder a esta página', 'Cerrar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        router.navigate(['/login']);
        return false;
      }
      
      // Check if user has any of the required roles
      const userRoles = user.roles || [];
      const hasRole = requiredRoles.some(requiredRole => {
        return userRoles.some(userRole => {
          // Si el rol es un objeto con propiedad 'nombre', comparar el nombre
          if (typeof userRole === 'object' && userRole && 'nombre' in userRole) {
            return (userRole as any).nombre === requiredRole;
          }
          // Si es un string, comparar directamente
          return userRole === requiredRole;
        });
      });
      
      console.log('Guard - Verificando roles:', {
        requiredRoles,
        userRoles,
        hasRole,
        userRoleNames: userRoles.map(role => 
          typeof role === 'object' && role && 'nombre' in role 
            ? (role as any).nombre 
            : role
        )
      });
      
      if (!hasRole) {
        // User doesn't have required role - show error and redirect
        const isUserAdminRoute = route.url.some(segment => segment.path === 'usuarios');
        
        if (isUserAdminRoute) {
          snackBar.open('No tiene permisos para acceder a la administración de usuarios. Solo los administradores del sistema o de empresa pueden gestionar usuarios.', 'Cerrar', {
            duration: 7000,
            panelClass: ['error-snackbar']
          });
        } else {
          snackBar.open('No tiene permisos para acceder a esta página', 'Cerrar', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
        
        router.navigate(['/dashboard']);
        return false;
      }
      
      return true;
    })
  );
};
