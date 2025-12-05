import { ErrorHandler, Injectable, Provider } from '@angular/core';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: unknown): void {
    const errObj = error as any;
    const msg = errObj?.message ?? '';
    // Silenciar NG0100 en dev para no bloquear navegación
    if (typeof msg === 'string' && msg.includes('NG0100')) {
      return;
    }
    console.error('GLOBAL_ERROR_HANDLER', errObj);
    console.error('GLOBAL_ERROR_HANDLER_META', {
      message: msg,
      stack: errObj?.stack ?? null,
      debugContext: errObj?.ngErrorDebugContext ?? errObj?.context ?? null,
      cause: errObj?.cause ?? null,
      keys: errObj ? Object.keys(errObj) : []
    });
    console.error('GLOBAL_ERROR_HANDLER_URL', window.location.href);
    // No rethrow para evitar loops; Angular ya lo registra.
  }
}

export const GLOBAL_ERROR_HANDLER_PROVIDER: Provider = {
  provide: ErrorHandler,
  useClass: GlobalErrorHandler
};

