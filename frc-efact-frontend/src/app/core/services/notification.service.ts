import { Injectable, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

export interface NotificationConfig extends MatSnackBarConfig {
  showRetry?: boolean;
  retryAction?: () => void;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  private readonly defaultConfig: MatSnackBarConfig = {
    duration: 5000,
    horizontalPosition: 'end',
    verticalPosition: 'top'
  };

  /**
   * Show a success notification
   */
  showSuccess(message: string, config?: NotificationConfig): void {
    const finalConfig = {
      ...this.defaultConfig,
      ...config,
      panelClass: ['success-snackbar'],
      duration: config?.duration ?? 3000
    };

    this.snackBar.open(message, 'Cerrar', finalConfig);
  }

  /**
   * Show an error notification
   */
  showError(message: string, config?: NotificationConfig): void {
    const finalConfig = {
      ...this.defaultConfig,
      ...config,
      panelClass: ['error-snackbar'],
      duration: config?.duration ?? 5000
    };

    const action = config?.showRetry ? 'Reintentar' : 'Cerrar';
    const snackBarRef = this.snackBar.open(message, action, finalConfig);

    if (config?.showRetry && config?.retryAction) {
      snackBarRef.onAction().subscribe(() => {
        config.retryAction!();
      });
    }
  }

  /**
   * Show a warning notification
   */
  showWarning(message: string, config?: NotificationConfig): void {
    const finalConfig = {
      ...this.defaultConfig,
      ...config,
      panelClass: ['warning-snackbar'],
      duration: config?.duration ?? 4000
    };

    this.snackBar.open(message, 'Cerrar', finalConfig);
  }

  /**
   * Show an info notification
   */
  showInfo(message: string, config?: NotificationConfig): void {
    const finalConfig = {
      ...this.defaultConfig,
      ...config,
      duration: config?.duration ?? 3000
    };

    this.snackBar.open(message, 'Cerrar', finalConfig);
  }

  /**
   * Show a validation error notification
   */
  showValidationError(message: string = 'Por favor, corrija los errores en el formulario'): void {
    this.showError(message, { duration: 4000 });
  }

  /**
   * Show a server error notification with retry option
   */
  showServerError(message: string, retryAction?: () => void): void {
    this.showError(message, {
      showRetry: !!retryAction,
      retryAction,
      duration: 0 // Don't auto-dismiss server errors
    });
  }

  /**
   * Show a network error notification with retry option
   */
  showNetworkError(retryAction?: () => void): void {
    this.showServerError(
      'Error de conexión. Verifique su conexión a internet e inténtelo de nuevo.',
      retryAction
    );
  }

  /**
   * Show an operation success notification
   */
  showOperationSuccess(operation: string, entity: string = 'elemento'): void {
    const messages = {
      create: `${entity} creado exitosamente`,
      update: `${entity} actualizado exitosamente`,
      delete: `${entity} eliminado exitosamente`,
      activate: `${entity} activado exitosamente`,
      deactivate: `${entity} desactivado exitosamente`,
      unlock: `${entity} desbloqueado exitosamente`,
      reset: `Contraseña restablecida exitosamente`
    };

    const message = messages[operation as keyof typeof messages] || `Operación completada exitosamente`;
    this.showSuccess(message);
  }

  /**
   * Show an operation error notification
   */
  showOperationError(operation: string, entity: string = 'elemento', retryAction?: () => void): void {
    const messages = {
      create: `Error al crear ${entity}`,
      update: `Error al actualizar ${entity}`,
      delete: `Error al eliminar ${entity}`,
      load: `Error al cargar ${entity}`,
      activate: `Error al activar ${entity}`,
      deactivate: `Error al desactivar ${entity}`,
      unlock: `Error al desbloquear ${entity}`,
      reset: `Error al restablecer contraseña`
    };

    const message = messages[operation as keyof typeof messages] || `Error en la operación`;
    this.showError(message, {
      showRetry: !!retryAction,
      retryAction
    });
  }

  /**
   * Dismiss all notifications
   */
  dismissAll(): void {
    this.snackBar.dismiss();
  }
}