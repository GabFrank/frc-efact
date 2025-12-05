import { bootstrapApplication } from '@angular/platform-browser';
import { registerLocaleData } from '@angular/common';
import localeEsPy from '@angular/common/locales/es-PY';
import localeEsPyExtra from '@angular/common/locales/extra/es-PY';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

registerLocaleData(localeEsPy, 'es-PY', localeEsPyExtra);

// Log global errors to identificar stack real en runtime
window.addEventListener('error', (event) => {
  console.error('GLOBAL_ERROR', event.error || event.message, {
    filename: event.filename,
    lineno: event.lineno,
    colno: event.colno,
    error: event.error
  });
});

// Log promesas no manejadas
window.addEventListener('unhandledrejection', (event) => {
  console.error('UNHANDLED_PROMISE', event.reason);
});

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
