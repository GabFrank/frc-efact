import { bootstrapApplication } from '@angular/platform-browser';
import { registerLocaleData } from '@angular/common';
import localeEsPy from '@angular/common/locales/es-PY';
import localeEsPyExtra from '@angular/common/locales/extra/es-PY';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

registerLocaleData(localeEsPy, 'es-PY', localeEsPyExtra);

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
