import { HttpInterceptorFn } from '@angular/common/http';
import { timeout } from 'rxjs/operators';

import { environment } from '../../environments/environment';

/**
 * Aplica un timeout a todas las peticiones HTTP.
 *
 * <p><b>Por qué existe.</b> `environment.apiTimeout` estaba declarado en los dos archivos de
 * environment desde el principio y **nunca se aplicaba en ningún lado** — ni en interceptors ni
 * en los servicios de `core/api`. Sin timeout, una petición contra un backend caído o una red
 * colgada queda pendiente indefinidamente.
 *
 * <p>Eso no era solo una espera larga: atascaba de forma permanente los effects de NgRx que usan
 * `exhaustMap`, porque ese operador descarta toda emisión nueva mientras la anterior siga activa.
 * En dev, un intento de login hecho con el backend apagado dejó el formulario con `loading` en
 * `true` y **todos los clics siguientes ignorados en silencio** hasta recargar la página.
 *
 * <p>Con el timeout, el observable interno siempre termina —con éxito o con error— así que el
 * slot se libera y el `exhaustMap` de `refreshToken$` (donde la deduplicación de refreshes
 * concurrentes sí es el comportamiento deseado) deja de ser un riesgo.
 *
 * <p>El error que emite es `TimeoutError`, que llega al `errorInterceptor` como una excepción sin
 * `status`, y ahí se trata igual que una caída de conexión.
 */
export const timeoutInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(timeout(environment.apiTimeout));
