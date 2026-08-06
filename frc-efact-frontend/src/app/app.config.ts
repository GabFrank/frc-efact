import { ApplicationConfig, LOCALE_ID, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { provideAuth0 } from '@auth0/auth0-angular';

import { environment } from '../environments/environment';
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
import { usuariosReducer } from './core/state/usuarios/usuarios.reducer';
import { timbradosReducer } from './core/state/timbrados/timbrados.reducer';
import { timbradoDetallesReducer } from './core/state/timbrado-detalles/timbrado-detalles.reducer';
import { notasReducer } from './core/state/notas/notas.reducer';

// Effects
import { AuthEffects } from './core/state/auth/auth.effects';
import { EmpresasEffects } from './core/state/empresas/empresas.effects';
import { FacturacionEffects } from './core/state/facturacion/facturacion.effects';
import { DocumentosEffects } from './core/state/documentos/documentos.effects';
import { UsuariosEffects } from './core/state/usuarios/usuarios.effects';
import { TimbradosEffects } from './core/state/timbrados/timbrados.effects';
import { TimbradoDetallesEffects } from './core/state/timbrado-detalles/timbrado-detalles.effects';
import { NotasEffects } from './core/state/notas/notas.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    provideAuth0({
      domain: environment.auth0.domain,
      clientId: environment.auth0.clientId,
      authorizationParams: {
        redirect_uri: window.location.origin,
        audience: environment.auth0.authorizationParams.audience
      },
      httpInterceptor: {
        allowedList: [
          {
            uri: `${environment.apiUrl}/*`,
            allowAnonymous: true
          }
        ]
      },
      errorPath: '/login'
    }),
    provideHttpClient(
      withInterceptors([httpsInterceptor, authInterceptor, errorInterceptor])
    ),
    provideStore({
      auth: authReducer,
      empresas: empresasReducer,
      facturacion: facturacionReducer,
      documentos: documentosReducer,
      usuarios: usuariosReducer,
      timbrados: timbradosReducer,
      timbradoDetalles: timbradoDetallesReducer,
      notas: notasReducer
    }),
    provideEffects([
      AuthEffects,
      EmpresasEffects,
      FacturacionEffects,
      DocumentosEffects,
      UsuariosEffects,
      TimbradosEffects,
      TimbradoDetallesEffects,
      NotasEffects
    ]),
    provideStoreDevtools({
      maxAge: 25,
      logOnly: !isDevMode(),
      autoPause: true,
      trace: false,
      traceLimit: 75
    }),
    { provide: LOCALE_ID, useValue: 'es-PY' }
  ]
};
