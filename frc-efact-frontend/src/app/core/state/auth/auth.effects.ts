import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { of } from 'rxjs';
import { map, catchError, exhaustMap, tap } from 'rxjs/operators';
import { AuthService } from '../../../services/auth.service';
import { User } from '../../../models/user.model';
import * as AuthActions from './auth.actions';
import { loadMisEmpresas } from '../empresas/empresas.actions';

@Injectable()
export class AuthEffects {
  private readonly actions$ = inject(Actions);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly store = inject(Store);

  private mapBackendUserToFrontend(backendUser: any): User {
    return {
      id: backendUser.id,
      username: backendUser.username,
      email: backendUser.email,
      auth0Id: backendUser.auth0Id,
      isActive: backendUser.isActive,
      roles: backendUser.roles || [],
      ultimoLogin: backendUser.ultimoLogin,
      creadoEn: backendUser.creadoEn,
      actualizadoEn: backendUser.actualizadoEn,
      failedLoginAttempts: backendUser.failedLoginAttempts || 0,
      lockedUntil: backendUser.lockedUntil,
      empresas: backendUser.empresas || [],
      createdBy: backendUser.createdBy,
      updatedBy: backendUser.updatedBy
    };
  }

  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      exhaustMap(({ username, password }) =>
        this.authService.login({ username, password }).pipe(
          map((response) =>
            AuthActions.loginSuccess({
              user: this.mapBackendUserToFrontend(response.usuario),
              token: response.token,
              refreshToken: response.refreshToken
            })
          ),
          catchError((error) =>
            of(AuthActions.loginFailure({ error: error.message }))
          )
        )
      )
    )
  );

  loginSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginSuccess),
        tap(({ user, token, refreshToken }) => {
          localStorage.setItem('auth_token', token);
          localStorage.setItem('refresh_token', refreshToken);
          localStorage.setItem('current_user', JSON.stringify(user));

          this.authService.updateCurrentUser(user);

          // Cargar empresas del usuario después del login
          this.store.dispatch(loadMisEmpresas());

          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 100);
        })
      ),
    { dispatch: false }
  );

  logout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.logout),
      exhaustMap(() =>
        this.authService.logout().pipe(
          map(() => AuthActions.logoutSuccess()),
          catchError(() => of(AuthActions.logoutSuccess()))
        )
      )
    )
  );

  logoutSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.logoutSuccess),
        tap(() => {
          // Reiniciar la aplicación para limpiar completamente el estado
          window.location.href = '/login';
        })
      ),
    { dispatch: false }
  );

  refreshToken$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.refreshToken),
      exhaustMap(() =>
        this.authService.refreshToken().pipe(
          map((response) =>
            AuthActions.refreshTokenSuccess({
              token: response.token,
              refreshToken: response.refreshToken
            })
          ),
          catchError((error) =>
            of(AuthActions.refreshTokenFailure({ error: error.message }))
          )
        )
      )
    )
  );

  initializeAuth$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.initializeAuth),
      map(() => {
        const token = localStorage.getItem('auth_token');
        const refreshToken = localStorage.getItem('refresh_token') || '';
        const userJson = localStorage.getItem('current_user');

        // Solo requerir token + user. refreshToken es opcional porque
        // los usuarios Auth0 no lo tienen (Auth0 maneja refresh internamente).
        if (token && userJson) {
          try {
            const user = JSON.parse(userJson);
            return AuthActions.initializeAuthSuccess({
              user,
              token,
              refreshToken
            });
          } catch (error) {
            console.error('Error parsing user from localStorage:', error);
            localStorage.removeItem('current_user');
            return AuthActions.initializeAuthFailure();
          }
        } else {
          return AuthActions.initializeAuthFailure();
        }
      })
    )
  );

  initializeAuthSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.initializeAuthSuccess),
        tap(({ user }) => {
          // Actualizar el AuthService para mantener sincronización
          this.authService.updateCurrentUser(user);
          // Cargar empresas del usuario al inicializar la sesión
          this.store.dispatch(loadMisEmpresas());
        })
      ),
    { dispatch: false }
  );
}
