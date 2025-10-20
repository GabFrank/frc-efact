import { createReducer, on } from '@ngrx/store';
import { User } from '../../../models/user.model';
import * as UsuariosActions from './usuarios.actions';

export interface UsersState {
  users: User[];
  selectedUser: User | null;
  loading: boolean;
  error: string | null;
  searchTerm: string;
  pagination: {
    page: number;
    size: number;
    total: number;
  };
  operationLoading: boolean; // For individual operations like create, update, delete
}

export const initialState: UsersState = {
  users: [],
  selectedUser: null,
  loading: false,
  error: null,
  searchTerm: '',
  pagination: {
    page: 0,
    size: 10,
    total: 0
  },
  operationLoading: false
};

export const usuariosReducer = createReducer(
  initialState,

  // Load users
  on(UsuariosActions.loadUsers, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(UsuariosActions.loadUsersSuccess, (state, { users, total }) => ({
    ...state,
    users,
    loading: false,
    error: null,
    pagination: {
      ...state.pagination,
      total: total || users.length
    }
  })),

  on(UsuariosActions.loadUsersFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Load single user
  on(UsuariosActions.loadUser, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(UsuariosActions.loadUserById, (state) => ({
    ...state,
    loading: true,
    error: null
  })),

  on(UsuariosActions.loadUserSuccess, (state, { user }) => ({
    ...state,
    selectedUser: user,
    loading: false,
    error: null,
    // Also update the user in the users array if it exists
    users: state.users.map(u => u.id === user.id ? user : u)
  })),

  on(UsuariosActions.loadUserFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error
  })),

  // Create user
  on(UsuariosActions.createUser, (state) => ({
    ...state,
    operationLoading: true,
    error: null
  })),

  on(UsuariosActions.createUserSuccess, (state, { user }) => ({
    ...state,
    users: [...state.users, user],
    operationLoading: false,
    error: null,
    pagination: {
      ...state.pagination,
      total: state.pagination.total + 1
    }
  })),

  on(UsuariosActions.createUserFailure, (state, { error }) => ({
    ...state,
    operationLoading: false,
    error
  })),

  // Update user
  on(UsuariosActions.updateUser, (state) => ({
    ...state,
    operationLoading: true,
    error: null
  })),

  on(UsuariosActions.updateUserSuccess, (state, { user }) => ({
    ...state,
    users: state.users.map(u => u.id === user.id ? user : u),
    selectedUser: state.selectedUser?.id === user.id ? user : state.selectedUser,
    operationLoading: false,
    error: null
  })),

  on(UsuariosActions.updateUserFailure, (state, { error }) => ({
    ...state,
    operationLoading: false,
    error
  })),

  // Delete user
  on(UsuariosActions.deleteUser, (state) => ({
    ...state,
    operationLoading: true,
    error: null
  })),

  on(UsuariosActions.deleteUserSuccess, (state, { id }) => ({
    ...state,
    users: state.users.filter(u => u.id !== id),
    selectedUser: state.selectedUser?.id === id ? null : state.selectedUser,
    operationLoading: false,
    error: null,
    pagination: {
      ...state.pagination,
      total: Math.max(0, state.pagination.total - 1)
    }
  })),

  on(UsuariosActions.deleteUserFailure, (state, { error }) => ({
    ...state,
    operationLoading: false,
    error
  })),

  // Reset password
  on(UsuariosActions.resetPassword, (state) => ({
    ...state,
    operationLoading: true,
    error: null
  })),

  on(UsuariosActions.resetUserPassword, (state) => ({
    ...state,
    operationLoading: true,
    error: null
  })),

  on(UsuariosActions.resetPasswordSuccess, (state, { userId }) => ({
    ...state,
    operationLoading: false,
    error: null
  })),

  on(UsuariosActions.resetPasswordFailure, (state, { error }) => ({
    ...state,
    operationLoading: false,
    error
  })),

  // Toggle user status
  on(UsuariosActions.toggleUserStatus, (state) => ({
    ...state,
    operationLoading: true,
    error: null
  })),

  on(UsuariosActions.toggleUserStatusSuccess, (state, { user }) => ({
    ...state,
    users: state.users.map(u => u.id === user.id ? user : u),
    selectedUser: state.selectedUser?.id === user.id ? user : state.selectedUser,
    operationLoading: false,
    error: null
  })),

  on(UsuariosActions.toggleUserStatusFailure, (state, { error }) => ({
    ...state,
    operationLoading: false,
    error
  })),

  // Unlock user
  on(UsuariosActions.unlockUser, (state) => ({
    ...state,
    operationLoading: true,
    error: null
  })),

  on(UsuariosActions.unlockUserSuccess, (state, { user }) => ({
    ...state,
    users: state.users.map(u => u.id === user.id ? user : u),
    selectedUser: state.selectedUser?.id === user.id ? user : state.selectedUser,
    operationLoading: false,
    error: null
  })),

  on(UsuariosActions.unlockUserFailure, (state, { error }) => ({
    ...state,
    operationLoading: false,
    error
  })),

  // UI state actions
  on(UsuariosActions.setSearchTerm, (state, { searchTerm }) => ({
    ...state,
    searchTerm
  })),

  on(UsuariosActions.setSelectedUser, (state, { user }) => ({
    ...state,
    selectedUser: user
  })),

  on(UsuariosActions.clearUsersError, (state) => ({
    ...state,
    error: null
  })),

  on(UsuariosActions.clearUsersState, () => initialState)
);