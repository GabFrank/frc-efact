import { createAction, props } from '@ngrx/store';
import { User, CreateUserRequest, UpdateUserRequest, ResetPasswordRequest, UserSearchRequest } from '../../../models/user.model';

// Load actions
export const loadUsers = createAction(
  '[Users] Load Users',
  props<{ searchRequest?: UserSearchRequest }>()
);

export const loadUsersSuccess = createAction(
  '[Users] Load Users Success',
  props<{ users: User[]; total?: number }>()
);

export const loadUsersFailure = createAction(
  '[Users] Load Users Failure',
  props<{ error: string }>()
);

// Load single user
export const loadUser = createAction(
  '[Users] Load User',
  props<{ id: number }>()
);

export const loadUserById = createAction(
  '[Users] Load User By Id',
  props<{ id: number }>()
);

export const loadUserSuccess = createAction(
  '[Users] Load User Success',
  props<{ user: User }>()
);

export const loadUserFailure = createAction(
  '[Users] Load User Failure',
  props<{ error: string }>()
);

// Create user actions
export const createUser = createAction(
  '[Users] Create User',
  props<{ user: CreateUserRequest }>()
);

export const createUserSuccess = createAction(
  '[Users] Create User Success',
  props<{ user: User }>()
);

export const createUserFailure = createAction(
  '[Users] Create User Failure',
  props<{ error: string }>()
);

// Update user actions
export const updateUser = createAction(
  '[Users] Update User',
  props<{ id: number; user: UpdateUserRequest }>()
);

export const updateUserSuccess = createAction(
  '[Users] Update User Success',
  props<{ user: User }>()
);

export const updateUserFailure = createAction(
  '[Users] Update User Failure',
  props<{ error: string }>()
);

// Delete user actions
export const deleteUser = createAction(
  '[Users] Delete User',
  props<{ id: number }>()
);

export const deleteUserSuccess = createAction(
  '[Users] Delete User Success',
  props<{ id: number }>()
);

export const deleteUserFailure = createAction(
  '[Users] Delete User Failure',
  props<{ error: string }>()
);

// Password management actions
export const resetPassword = createAction(
  '[Users] Reset Password',
  props<{ request: ResetPasswordRequest }>()
);

export const resetUserPassword = createAction(
  '[Users] Reset User Password',
  props<{ userId: number; newPassword: string; forcePasswordChange?: boolean }>()
);

export const resetPasswordSuccess = createAction(
  '[Users] Reset Password Success',
  props<{ userId: number }>()
);

export const resetPasswordFailure = createAction(
  '[Users] Reset Password Failure',
  props<{ error: string }>()
);

// Account management actions
export const toggleUserStatus = createAction(
  '[Users] Toggle User Status',
  props<{ id: number }>()
);

export const toggleUserStatusSuccess = createAction(
  '[Users] Toggle User Status Success',
  props<{ user: User }>()
);

export const toggleUserStatusFailure = createAction(
  '[Users] Toggle User Status Failure',
  props<{ error: string }>()
);

export const unlockUser = createAction(
  '[Users] Unlock User',
  props<{ id: number }>()
);

export const unlockUserSuccess = createAction(
  '[Users] Unlock User Success',
  props<{ user: User }>()
);

export const unlockUserFailure = createAction(
  '[Users] Unlock User Failure',
  props<{ error: string }>()
);

// UI state actions
export const setSearchTerm = createAction(
  '[Users] Set Search Term',
  props<{ searchTerm: string }>()
);

export const setSelectedUser = createAction(
  '[Users] Set Selected User',
  props<{ user: User | null }>()
);

export const clearUsersError = createAction(
  '[Users] Clear Error'
);

export const clearUsersState = createAction(
  '[Users] Clear State'
);