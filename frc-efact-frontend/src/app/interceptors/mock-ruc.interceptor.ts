import { HttpInterceptorFn, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';

// Simulación de RUCs existentes en la base de datos (con dígitos verificadores correctos)
const existingRucs = [
  { ruc: '80012345-3', razonSocial: 'EMPRESA DEMO S.A.' },
  { ruc: '80098765-2', razonSocial: 'COMERCIAL EJEMPLO S.R.L.' },
  { ruc: '80055555-0', razonSocial: 'SERVICIOS MOCK LTDA.' }
];

export const mockRucInterceptor: HttpInterceptorFn = (req, next) => {
  // Solo interceptar requests de validación de RUC
  if (req.url.includes('/api/empresas/validate-ruc')) {
    return handleRucValidation(req);
  }

  return next(req);
};

function handleRucValidation(req: HttpRequest<any>): Observable<HttpResponse<any>> {
    const ruc = req.params.get('ruc');
    const excludeId = req.params.get('excludeId');

    if (!ruc) {
      return of(new HttpResponse({
        status: 400,
        body: { valid: false, error: 'RUC no proporcionado' }
      })).pipe(delay(300));
    }

    // Validar formato básico
    const rucPattern = /^\d{6,8}-\d$/;
    if (!rucPattern.test(ruc)) {
      return of(new HttpResponse({
        status: 200,
        body: { valid: false, error: 'Formato de RUC inválido' }
      })).pipe(delay(300));
    }

    // Buscar si el RUC ya existe
    const existingRuc = existingRucs.find(r => r.ruc === ruc);
    
    if (existingRuc) {
      // Si se proporciona excludeId, simular que es la misma empresa
      if (excludeId && parseInt(excludeId) === 1) {
        return of(new HttpResponse({
          status: 200,
          body: { 
            valid: true, 
            exists: false,
            message: 'RUC válido (empresa actual)' 
          }
        })).pipe(delay(500));
      }

      return of(new HttpResponse({
        status: 200,
        body: { 
          valid: false, 
          exists: true,
          razonSocial: existingRuc.razonSocial,
          error: 'RUC ya registrado'
        }
      })).pipe(delay(500));
    }

    // RUC válido y disponible
    return of(new HttpResponse({
      status: 200,
      body: { 
        valid: true, 
        exists: false,
        message: 'RUC válido y disponible' 
      }
    })).pipe(delay(800)); // Simular latencia de red
}