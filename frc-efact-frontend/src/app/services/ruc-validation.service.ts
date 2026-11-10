import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

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
   * Valida formato Y dígito verificador del RUC paraguayo.
   *
   * El chequeo del DV estuvo deshabilitado con el comentario "Backend usa algoritmo
   * incorrecto". Era falso: el algoritmo del backend (CalcularVerificadorRuc) es correcto y
   * `calculateCheckDigit` de este servicio es idéntico línea por línea. La confusión vino de
   * tests con dígitos verificadores inventados. Verificado el 2026-08-06 contra los 12 RUCs
   * de producción: todos validan.
   */
  validateRucFormat(ruc: string): boolean {
    if (!ruc) return false;

    // Formato: 12345678-9 (6-8 dígitos, guión, 1 dígito verificador)
    const rucPattern = /^\d{6,8}-\d$/;
    if (!rucPattern.test(ruc)) return false;

    return this.validateRucCheckDigit(ruc);
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
   * Valida el RUC: formato y dígito verificador.
   *
   * ⚠️ **No consulta al servidor.** El chequeo de unicidad ("este RUC ya pertenece a la
   * empresa X") requiere el endpoint `GET /empresas/validate-ruc`, que **no existe** en el
   * backend — verificado el 2026-08-06. Hasta que exista, `exists` es siempre `false`.
   *
   * Antes esta función devolvía `valid: true` sin validar nada, y un interceptor
   * (`mock-ruc.interceptor`, ya eliminado) servía respuestas falsas para esa URL, incluso en
   * producción. Ahora al menos el formato y el DV se validan de verdad, del lado del cliente.
   *
   * Pendiente registrado en docs/TAREAS_PENDIENTES.md §4.
   */
  validateRucLive(ruc: string, excludeId?: number): Observable<RucValidationResult> {
    if (!this.validateRucFormat(ruc)) {
      const correcto = this.getCorrectCheckDigit(ruc);
      return of({
        valid: false,
        error: correcto
          ? `Dígito verificador incorrecto. El correcto para ${ruc.split('-')[0]} es ${correcto}`
          : 'Formato de RUC inválido. Use 6-8 dígitos, guión y dígito verificador'
      });
    }

    return of({ valid: true, exists: false, error: undefined });
  }

  /**
   * Devuelve el DV correcto si el formato es válido pero el dígito no coincide; null si el
   * formato tampoco sirve. Se usa para dar un mensaje de error accionable.
   */
  private getCorrectCheckDigit(ruc: string): number | null {
    if (!ruc || !/^\d{6,8}-\d$/.test(ruc)) return null;
    return this.calculateCheckDigit(ruc.split('-')[0]);
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