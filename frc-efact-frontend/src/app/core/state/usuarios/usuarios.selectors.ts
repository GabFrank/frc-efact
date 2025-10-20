import { createFeatureSelector, createSelector } from '@ngrx/store';
import { UsersState } from './usuarios.reducer';

// Feature selector
export const selectUsersState = createFeatureSelector<UsersState>('usuarios');

// Basic selectors
export const selectAllUsers = createSelector(
  selectUsersState,
  (state: UsersState) => state.users
);

export const selectUsersLoading = createSelector(
  selectUsersState,
  (state: UsersState) => state.loading
);

export const selectUsersOperationLoading = createSelector(
  selectUsersState,
  (state: UsersState) => state.operationLoading
);

export const selectUsersError = createSelector(
  selectUsersState,
  (state: UsersState) => state.error
);

export const selectSelectedUser = createSelector(
  selectUsersState,
  (state: UsersState) => state.selectedUser
);

export const selectSearchTerm = createSelector(
  selectUsersState,
  (state: UsersState) => state.searchTerm
);

export const selectUsersPagination = createSelector(
  selectUsersState,
  (state: UsersState) => state.pagination
);

// User by ID selector factory
export const selectUserById = (id: number) => createSelector(
  selectAllUsers,
  (users) => users.find(user => user.id === id)
);

// Filtered selectors
export const selectActiveUsers = createSelector(
  selectAllUsers,
  (users) => users.filter(user => user.isActive)
);

export const selectInactiveUsers = createSelector(
  selectAllUsers,
  (users) => users.filter(user => !user.isActive)
);

export const selectLockedUsers = createSelector(
  selectAllUsers,
  (users) => users.filter(user => {
    if (!user.lockedUntil) return false;
    return new Date(user.lockedUntil) > new Date();
  })
);

export const selectUnlockedUsers = createSelector(
  selectAllUsers,
  (users) => users.filter(user => {
    if (!user.lockedUntil) return true;
    return new Date(user.lockedUntil) <= new Date();
  })
);

// Helper function to check if user has role
const userHasRole = (user: any, roleName: string): boolean => {
  if (!user.roles) return false;
  
  return user.roles.some((role: any) => {
    // Si el rol es un objeto con propiedad 'nombre', comparar el nombre
    if (typeof role === 'object' && role && 'nombre' in role) {
      return role.nombre === roleName;
    }
    // Si es un string, comparar directamente
    return role === roleName;
  });
};

// Users by role selectors
export const selectUsersByRole = (role: string) => createSelector(
  selectAllUsers,
  (users) => users.filter(user => userHasRole(user, role))
);

export const selectAdminUsers = createSelector(
  selectAllUsers,
  (users) => users.filter(user => userHasRole(user, 'ADMIN'))
);

export const selectRegularUsers = createSelector(
  selectAllUsers,
  (users) => users.filter(user => userHasRole(user, 'USER') && !userHasRole(user, 'ADMIN'))
);

// Search filtered users
export const selectFilteredUsers = createSelector(
  selectAllUsers,
  selectSearchTerm,
  (users, searchTerm) => {
    if (!searchTerm.trim()) {
      return users;
    }
    
    const term = searchTerm.toLowerCase().trim();
    return users.filter(user => 
      user.username.toLowerCase().includes(term) ||
      user.email.toLowerCase().includes(term)
    );
  }
);

// Statistics selectors
export const selectUsersStats = createSelector(
  selectAllUsers,
  (users) => {
    const total = users.length;
    const active = users.filter(u => u.isActive).length;
    const inactive = users.filter(u => !u.isActive).length;
    const locked = users.filter(u => {
      if (!u.lockedUntil) return false;
      return new Date(u.lockedUntil) > new Date();
    }).length;
    const admins = users.filter(u => userHasRole(u, 'ADMIN')).length;
    
    return {
      total,
      active,
      inactive,
      locked,
      admins,
      regularUsers: total - admins
    };
  }
);

// Combined loading state
export const selectAnyLoading = createSelector(
  selectUsersLoading,
  selectUsersOperationLoading,
  (loading, operationLoading) => loading || operationLoading
);

// User with companies selector
export const selectUserWithCompanies = (userId: number) => createSelector(
  selectUserById(userId),
  (user) => {
    if (!user) return null;
    return {
      ...user,
      companiesCount: user.empresas?.length || 0,
      activeCompanies: user.empresas?.filter(e => e.activo).length || 0
    };
  }
);