import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthState } from './auth.reducer';

export const selectAuthState = createFeatureSelector<AuthState>('auth');

export const selectUser = createSelector(
  selectAuthState,
  (state) => state.user
);

export const selectToken = createSelector(
  selectAuthState,
  (state) => state.token
);

export const selectIsAuthenticated = createSelector(
  selectAuthState,
  (state) => state.isAuthenticated
);

export const selectAuthLoading = createSelector(
  selectAuthState,
  (state) => state.loading
);

export const selectAuthError = createSelector(
  selectAuthState,
  (state) => state.error
);

export const selectCurrentUser = createSelector(
  selectAuthState,
  (state) => state.user
);

export const selectUserRoles = createSelector(
  selectAuthState,
  (state) => {
    if (!state.user?.roles) return [];
    
    return state.user.roles.map(role => {
      // Si el rol es un objeto con propiedad 'nombre', devolver el nombre
      if (typeof role === 'object' && 'nombre' in role) {
        return (role as any).nombre;
      }
      // Si es un string, devolverlo directamente
      return role;
    });
  }
);

export const selectUserRole = createSelector(
  selectUserRoles,
  (roles) => {
    // Jerarquía de roles (del más privilegiado al menos privilegiado)
    const roleHierarchy = ['ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR'];
    
    // Encontrar el rol más privilegiado que tiene el usuario
    for (const role of roleHierarchy) {
      if (roles.includes(role)) {
        return role;
      }
    }
    
    // Si no tiene ningún rol de la jerarquía, devolver el primero
    return roles[0] || null;
  }
);

export const selectHasRole = (roleName: string) => createSelector(
  selectUserRoles,
  (roles) => roles.includes(roleName)
);
