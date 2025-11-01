import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, timer } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';

export interface RucValidationResult {
  valid: boolean;
  exists?: boolean;
  razonSocial?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RucValidationService {
  private readonly API_BASE_URL = '/api'; // Ajustar según tu configuración

  constructor(private http: HttpClient) {}

  /**
   * Valida el formato del RUC paraguayo
   */
  validateRucFormat(ruc: string): boolean {
    if (!ruc) return false;
    
    // Formato: 12345678-9 (6-8 dígitos, guión, 1 dígito verificador)
    const rucPattern = /^\d{6,8}-\d$/;
    if (!rucPattern.test(ruc)) return false;

    // TEMPORALMENTE DESHABILITADO: Backend usa algoritmo incorrecto
    // return this.validateRucCheckDigit(ruc);
    
    // Solo validar formato hasta que backend se corrija
    return true;
  }

  /**
   * Valida el dígito verificador del RUC paraguayo
   */
  private validateRucCheckDigit(ruc: string): boolean {
    const parts = ruc.split('-');
    if (parts.length !== 2) return false;

    const number = parts[0];
    const providedCheckDigit = parseInt(parts[1]);
    const calculatedCheckDigit = this.calculateCheckDigit(number);

    return calculatedCheckDigit === providedCheckDigit;
  }

  /**
   * Valida RUC en tiempo real con el backend
   */
  validateRucLive(ruc: string, excludeId?: number): Observable<RucValidationResult> {
    // Primero validar formato
    if (!this.validateRucFormat(ruc)) {
      return of({
        valid: false,
        error: 'Formato de RUC inválido'
      });
    }

    // TEMPORALMENTE DESHABILITADO: Backend rechaza RUCs válidos
    // Hasta que se corrija el algoritmo del backend, solo validamos formato
    return of({
      valid: true,
      exists: false,
      error: undefined
    });

    // TODO: Reactivar cuando backend use algoritmo correcto
    // return timer(500).pipe(
    //   switchMap(() => {
    //     const params: any = { ruc };
    //     if (excludeId) {
    //       params.excludeId = excludeId;
    //     }

    //     return this.http.get<RucValidationResult>(`${this.API_BASE_URL}/empresas/validate-ruc`, {
    //       params
    //     }).pipe(
    //       map(result => ({
    //         valid: result.valid,
    //         exists: result.exists,
    //         razonSocial: result.razonSocial,
    //         error: result.error
    //       })),
    //       catchError(error => {
    //         console.error('Error validating RUC:', error);
    //         return of({
    //           valid: false,
    //           error: 'Error al validar RUC con el servidor'
    //         });
    //       })
    //     );
    //   })
    // );
  }

  /**
   * Formatea el RUC mientras el usuario escribe
   */
  formatRucInput(value: string): string {
    // Remover caracteres no numéricos excepto el guión
    let cleaned = value.replace(/[^\d-]/g, '');
    
    // Remover guiones múltiples
    cleaned = cleaned.replace(/-+/g, '-');
    
    // Si hay más de un guión, mantener solo el primero
    const parts = cleaned.split('-');
    if (parts.length > 2) {
      cleaned = parts[0] + '-' + parts.slice(1).join('');
    }
    
    // Limitar longitud: máximo 8 dígitos + guión + 1 dígito
    if (cleaned.length > 10) {
      cleaned = cleaned.substring(0, 10);
    }
    
    // Auto-agregar guión si se escriben 6-8 dígitos seguidos
    if (/^\d{6,8}$/.test(cleaned)) {
      // No agregar guión automáticamente, dejar que el usuario lo escriba
    }
    
    return cleaned;
  }

  /**
   * Calcula el dígito verificador correcto para un número de RUC
   * Algoritmo oficial paraguayo basado en módulo 11
   */
  calculateCheckDigit(number: string, base: number = 11): number {
    if (number.length < 4) return 0;

    let k = 2;
    let total = 0;

    // Eliminar caracteres no numéricos y convertir a string
    const cleanNumber = this.eliminarNoDigitos(number);
    
    // Invertir la cadena (clave del algoritmo paraguayo)
    const reversed = cleanNumber.split('').reverse().join('');

    // Procesar cada dígito de la cadena invertida
    for (let i = 0; i < reversed.length; i++) {
      const digit = parseInt(reversed[i]);
      total += digit * k;
      
      k++;
      if (k > base) {
        k = 2;
      }
    }

    const remainder = total % base;
    return remainder > 1 ? base - remainder : 0;
  }

  /**
   * Elimina todos los caracteres no numéricos de la cadena
   */
  private eliminarNoDigitos(ruc: string): string {
    let result = '';
    for (const char of ruc) {
      if (char >= '0' && char <= '9') {
        result += char;
      } else {
        // Convertir caracteres no numéricos a su código ASCII
        result += char.charCodeAt(0).toString();
      }
    }
    return result;
  }

  /**
   * Obtiene sugerencias de formato para el usuario
   */
  getRucFormatHint(value: string): string {
    if (!value) return 'Formato: 12345678-9';
    
    const cleaned = value.replace(/[^\d-]/g, '');
    
    if (cleaned.length === 0) return 'Formato: 12345678-9';
    if (cleaned.length < 6) return 'Mínimo 6 dígitos antes del guión';
    if (cleaned.length >= 6 && !cleaned.includes('-')) {
      // Sugerir el dígito verificador correcto
      const correctDigit = this.calculateCheckDigit(cleaned);
      return `Agregar guión y dígito verificador: ${cleaned}-${correctDigit}`;
    }
    if (cleaned.includes('-') && cleaned.split('-')[1].length === 0) return 'Agregar dígito verificador';
    
    return '';
  }

  /**
   * Método de debug para verificar el cálculo del dígito verificador
   */
  debugCheckDigit(ruc: string): any {
    const parts = ruc.split('-');
    if (parts.length !== 2) return { error: 'Formato inválido' };

    const number = parts[0];
    const providedDigit = parseInt(parts[1]);
    
    let k = 2;
    let total = 0;
    const calculations: any[] = [];

    // Algoritmo oficial: limpiar, invertir y procesar
    const cleanNumber = this.eliminarNoDigitos(number);
    const reversed = cleanNumber.split('').reverse().join('');

    for (let i = 0; i < reversed.length; i++) {
      const digit = parseInt(reversed[i]);
      const product = digit * k;
      total += product;
      
      calculations.push({
        position: i + 1,
        originalPosition: cleanNumber.length - i,
        digit,
        weight: k,
        product
      });
      
      k++;
      if (k > 11) {
        k = 2;
      }
    }

    const remainder = total % 11;
    const calculatedDigit = remainder > 1 ? 11 - remainder : 0;

    return {
      originalNumber: number,
      cleanNumber,
      reversedNumber: reversed,
      providedDigit,
      calculations,
      total,
      remainder,
      calculatedDigit,
      isValid: calculatedDigit === providedDigit
    };
  }
}