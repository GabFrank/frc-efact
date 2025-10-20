import { createAction, props } from '@ngrx/store';
import { User } from '../../../models/user.model';

// Login actions
export const login = createAction(
  '[Auth] Login',
  props<{ username: string; password: string }>()
);

export const loginSuccess = createAction(
  '[Auth] Login Success',
  props<{ user: User; token: string; refreshToken: string }>()
);

export const loginFailure = createAction(
  '[Auth] Login Failure',
  props<{ error: string }>()
);

// Logout actions
export const logout = createAction('[Auth] Logout');

export const logoutSuccess = createAction('[Auth] Logout Success');

// Refresh token actions
export const refreshToken = createAction('[Auth] Refresh Token');

export const refreshTokenSuccess = createAction(
  '[Auth] Refresh Token Success',
  props<{ token: string; refreshToken: string }>()
);

export const refreshTokenFailure = createAction(
  '[Auth] Refresh Token Failure',
  props<{ error: string }>()
);

// Load user actions
export const loadUser = createAction('[Auth] Load User');

export const loadUserSuccess = createAction(
  '[Auth] Load User Success',
  props<{ user: User }>()
);

export const loadUserFailure = createAction(
  '[Auth] Load User Failure',
  props<{ error: string }>()
);

// Initialize auth from storage
export const initializeAuth = createAction('[Auth] Initialize Auth');

export const initializeAuthSuccess = createAction(
  '[Auth] Initialize Auth Success',
  props<{ user: User; token: string; refreshToken: string }>()
);

export const initializeAuthFailure = createAction(
  '[Auth] Initialize Auth Failure'
);
