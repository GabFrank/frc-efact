import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, exhaustMap, tap } from 'rxjs/operators';
import { AuthService } from '../../../services/auth.service';
import { User } from '../../../models/user.model';
import * as AuthActions from './auth.actions';

@Injectable()
export class AuthEffects {
  private readonly actions$ = inject(Actions);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  private mapBackendUserToFrontend(backendUser: any): User {
    return {
      id: backendUser.id,
      username: backendUser.username,
      email: backendUser.email,
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
        tap(() => {
          this.router.navigate(['/dashboard']);
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
          this.router.navigate(['/login']);
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
        const token = this.authService.getToken();
        const refreshToken = this.authService.getRefreshToken();
        const user = this.authService.getCurrentUser();

        if (token && refreshToken && user) {
          console.log('Inicializando auth desde localStorage:', { user, token: !!token, refreshToken: !!refreshToken });
          return AuthActions.initializeAuthSuccess({
            user,
            token,
            refreshToken
          });
        } else {
          console.log('No se encontró información de auth en localStorage');
          return AuthActions.initializeAuthFailure();
        }
      })
    )
  );
}
