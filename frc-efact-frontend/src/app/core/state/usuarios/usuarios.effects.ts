import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { map, catchError, switchMap, tap, delay, retryWhen, take } from 'rxjs/operators';
import { UsuarioApiService } from '../../api/usuario-api.service';
import { NotificationService } from '../../services/notification.service';
import { User } from '../../../models/user.model';
import * as UsuariosActions from './usuarios.actions';

@Injectable()
export class UsuariosEffects {
  private readonly actions$ = inject(Actions);
  private readonly usuarioApiService = inject(UsuarioApiService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  // Load users effect
  loadUsers$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.loadUsers),
      switchMap(({ searchRequest }) => {
        if (searchRequest) {
          return this.usuarioApiService.searchUsers(searchRequest).pipe(
            map((response: { content: User[], totalElements: number, totalPages: number }) => 
              UsuariosActions.loadUsersSuccess({ 
                users: response.content, 
                total: response.totalElements 
              })
            ),
            retryWhen(errors => 
              errors.pipe(
                delay(1000),
                take(2) // Retry up to 2 times
              )
            ),
            catchError((error) => {
              const errorMessage = this.getErrorMessage(error, 'Error al cargar usuarios');
              return of(UsuariosActions.loadUsersFailure({ error: errorMessage }));
            })
          );
        } else {
          return this.usuarioApiService.getAll().pipe(
            map((users: User[]) => UsuariosActions.loadUsersSuccess({ users })),
            retryWhen(errors => 
              errors.pipe(
                delay(1000),
                take(2) // Retry up to 2 times
              )
            ),
            catchError((error) => {
              const errorMessage = this.getErrorMessage(error, 'Error al cargar usuarios');
              return of(UsuariosActions.loadUsersFailure({ error: errorMessage }));
            })
          );
        }
      })
    )
  );

  // Load single user effect
  loadUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.loadUser),
      switchMap(({ id }) =>
        this.usuarioApiService.getById(id).pipe(
          map((user) => UsuariosActions.loadUserSuccess({ user })),
          catchError((error) =>
            of(UsuariosActions.loadUserFailure({ 
              error: this.getErrorMessage(error, 'Error al cargar usuario') 
            }))
          )
        )
      )
    )
  );

  // Load user by ID effect
  loadUserById$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.loadUserById),
      switchMap(({ id }) =>
        this.usuarioApiService.getById(id).pipe(
          map((user) => UsuariosActions.loadUserSuccess({ user })),
          catchError((error) =>
            of(UsuariosActions.loadUserFailure({ 
              error: this.getErrorMessage(error, 'Error al cargar usuario') 
            }))
          )
        )
      )
    )
  );

  // Create user effect
  createUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.createUser),
      switchMap(({ user }) =>
        this.usuarioApiService.create(user).pipe(
          map((createdUser) => UsuariosActions.createUserSuccess({ user: createdUser })),
          catchError((error) => {
            const errorMessage = this.getDetailedErrorMessage(error, 'Error al crear usuario');
            return of(UsuariosActions.createUserFailure({ error: errorMessage }));
          })
        )
      )
    )
  );

  // Update user effect
  updateUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.updateUser),
      switchMap(({ id, user }) =>
        this.usuarioApiService.update(id, user).pipe(
          map((updatedUser) => UsuariosActions.updateUserSuccess({ user: updatedUser })),
          catchError((error) => {
            const errorMessage = this.getDetailedErrorMessage(error, 'Error al actualizar usuario');
            return of(UsuariosActions.updateUserFailure({ error: errorMessage }));
          })
        )
      )
    )
  );

  // Delete user effect
  deleteUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.deleteUser),
      switchMap(({ id }) =>
        this.usuarioApiService.delete(id).pipe(
          map(() => UsuariosActions.deleteUserSuccess({ id })),
          catchError((error) =>
            of(UsuariosActions.deleteUserFailure({ 
              error: this.getErrorMessage(error, 'Error al eliminar usuario') 
            }))
          )
        )
      )
    )
  );

  // Reset password effect
  resetPassword$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.resetPassword),
      switchMap(({ request }) =>
        this.usuarioApiService.resetPassword(request).pipe(
          map(() => UsuariosActions.resetPasswordSuccess({ userId: request.userId })),
          catchError((error) =>
            of(UsuariosActions.resetPasswordFailure({ 
              error: this.getErrorMessage(error, 'Error al restablecer contraseña') 
            }))
          )
        )
      )
    )
  );

  // Reset user password effect
  resetUserPassword$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.resetUserPassword),
      switchMap(({ userId, newPassword, forcePasswordChange }) =>
        this.usuarioApiService.resetPassword({
          userId,
          newPassword,
          forcePasswordChange
        }).pipe(
          map(() => UsuariosActions.resetPasswordSuccess({ userId })),
          catchError((error) =>
            of(UsuariosActions.resetPasswordFailure({ 
              error: this.getErrorMessage(error, 'Error al restablecer contraseña') 
            }))
          )
        )
      )
    )
  );

  // Toggle user status effect
  toggleUserStatus$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.toggleUserStatus),
      switchMap(({ id }) =>
        this.usuarioApiService.toggleUserStatus(id).pipe(
          map((user) => UsuariosActions.toggleUserStatusSuccess({ user })),
          catchError((error) =>
            of(UsuariosActions.toggleUserStatusFailure({ 
              error: this.getErrorMessage(error, 'Error al cambiar estado del usuario') 
            }))
          )
        )
      )
    )
  );

  // Unlock user effect
  unlockUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UsuariosActions.unlockUser),
      switchMap(({ id }) =>
        this.usuarioApiService.unlockUser(id).pipe(
          map((user) => UsuariosActions.unlockUserSuccess({ user })),
          catchError((error) =>
            of(UsuariosActions.unlockUserFailure({ 
              error: this.getErrorMessage(error, 'Error al desbloquear usuario') 
            }))
          )
        )
      )
    )
  );

  // Success notifications
  createUserSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.createUserSuccess),
        tap(({ user }) => {
          this.notificationService.showOperationSuccess('create', 'Usuario');
          this.router.navigate(['/usuarios']);
        })
      ),
    { dispatch: false }
  );

  updateUserSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.updateUserSuccess),
        tap(({ user }) => {
          this.notificationService.showOperationSuccess('update', 'Usuario');
          this.router.navigate(['/usuarios']);
        })
      ),
    { dispatch: false }
  );

  deleteUserSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.deleteUserSuccess),
        tap(() => {
          this.notificationService.showOperationSuccess('delete', 'Usuario');
        })
      ),
    { dispatch: false }
  );

  resetPasswordSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.resetPasswordSuccess),
        tap(() => {
          this.notificationService.showOperationSuccess('reset', 'Contraseña');
        })
      ),
    { dispatch: false }
  );

  toggleUserStatusSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.toggleUserStatusSuccess),
        tap(({ user }) => {
          const operation = user.isActive ? 'activate' : 'deactivate';
          this.notificationService.showOperationSuccess(operation, 'Usuario');
        })
      ),
    { dispatch: false }
  );

  unlockUserSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.unlockUserSuccess),
        tap(() => {
          this.notificationService.showOperationSuccess('unlock', 'Usuario');
        })
      ),
    { dispatch: false }
  );

  // Error notifications with retry options
  loadUsersError$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.loadUsersFailure),
        tap(({ error }) => {
          if (this.isNetworkError(error)) {
            this.notificationService.showNetworkError(() => {
              // Retry loading users
              this.actions$.pipe(take(1)).subscribe(() => {
                // Dispatch load users action again
                // Note: In a real implementation, you'd need to store the original search request
              });
            });
          } else {
            this.notificationService.showOperationError('load', 'usuarios');
          }
        })
      ),
    { dispatch: false }
  );

  createUserError$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.createUserFailure),
        tap(({ error }) => {
          if (this.isValidationError(error)) {
            // Validation errors are handled in the form component
            return;
          }
          this.notificationService.showOperationError('create', 'usuario');
        })
      ),
    { dispatch: false }
  );

  updateUserError$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.updateUserFailure),
        tap(({ error }) => {
          if (this.isValidationError(error)) {
            // Validation errors are handled in the form component
            return;
          }
          this.notificationService.showOperationError('update', 'usuario');
        })
      ),
    { dispatch: false }
  );

  deleteUserError$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.deleteUserFailure),
        tap(({ error }) => {
          this.notificationService.showOperationError('delete', 'usuario');
        })
      ),
    { dispatch: false }
  );

  resetPasswordError$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.resetPasswordFailure),
        tap(({ error }) => {
          this.notificationService.showOperationError('reset', 'contraseña');
        })
      ),
    { dispatch: false }
  );

  toggleUserStatusError$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.toggleUserStatusFailure),
        tap(({ error }) => {
          this.notificationService.showError('Error al cambiar el estado del usuario');
        })
      ),
    { dispatch: false }
  );

  unlockUserError$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(UsuariosActions.unlockUserFailure),
        tap(({ error }) => {
          this.notificationService.showOperationError('unlock', 'usuario');
        })
      ),
    { dispatch: false }
  );

  private getErrorMessage(error: any, defaultMessage: string): string {
    if (error?.error?.message) {
      return error.error.message;
    }
    if (error?.message) {
      return error.message;
    }
    if (typeof error === 'string') {
      return error;
    }
    return defaultMessage;
  }

  private getDetailedErrorMessage(error: any, defaultMessage: string): string {
    // Handle HTTP error responses
    if (error?.status) {
      switch (error.status) {
        case 400:
          return error?.error?.message || 'Datos inválidos. Verifique la información ingresada.';
        case 401:
          return 'No tiene permisos para realizar esta operación.';
        case 403:
          return 'Acceso denegado. Contacte al administrador.';
        case 404:
          return 'Usuario no encontrado.';
        case 409:
          return error?.error?.message || 'Conflicto: El usuario ya existe o hay datos duplicados.';
        case 422:
          // Return the full error for validation handling in components
          return JSON.stringify(error.error);
        case 500:
          return 'Error interno del servidor. Inténtelo más tarde.';
        default:
          return error?.error?.message || defaultMessage;
      }
    }

    return this.getErrorMessage(error, defaultMessage);
  }

  private isNetworkError(error: string): boolean {
    return error.toLowerCase().includes('network') || 
           error.toLowerCase().includes('connection') ||
           error.toLowerCase().includes('timeout') ||
           error.toLowerCase().includes('conexión');
  }

  private isValidationError(error: string): boolean {
    try {
      const parsed = JSON.parse(error);
      return parsed.fieldErrors || parsed.validationErrors;
    } catch {
      return error.toLowerCase().includes('validation') ||
             error.toLowerCase().includes('invalid') ||
             error.toLowerCase().includes('required') ||
             error.toLowerCase().includes('already exists');
    }
  }
}