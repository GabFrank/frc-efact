import { ApplicationConfig, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { authInterceptor } from './interceptors/auth.interceptor';
import { errorInterceptor } from './interceptors/error.interceptor';
import { httpsInterceptor } from './interceptors/https.interceptor';

// Reducers
import { authReducer } from './core/state/auth/auth.reducer';
import { empresasReducer } from './core/state/empresas/empresas.reducer';
import { facturacionReducer } from './core/state/facturacion/facturacion.reducer';
import { documentosReducer } from './core/state/documentos/documentos.reducer';

// Effects
import { AuthEffects } from './core/state/auth/auth.effects';
import { EmpresasEffects } from './core/state/empresas/empresas.effects';
import { FacturacionEffects } from './core/state/facturacion/facturacion.effects';
import { DocumentosEffects } from './core/state/documentos/documentos.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(
      withInterceptors([httpsInterceptor, authInterceptor, errorInterceptor])
    ),
    provideStore({
      auth: authReducer,
      empresas: empresasReducer,
      facturacion: facturacionReducer,
      documentos: documentosReducer
    }),
    provideEffects([
      AuthEffects,
      EmpresasEffects,
      FacturacionEffects,
      DocumentosEffects
    ]),
    provideStoreDevtools({
      maxAge: 25,
      logOnly: !isDevMode(),
      autoPause: true,
      trace: false,
      traceLimit: 75
    })
  ]
};
