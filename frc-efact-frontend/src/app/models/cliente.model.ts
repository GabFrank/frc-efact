/**
 * Modelo de Cliente según Manual Técnico SIFEN v1.50.
 * Incluye campos legacy (tributa, tipoContribuyente) para compatibilidad.
 */
export interface Cliente {
  id: number;
  empresaId: number;
  nombre: string;
  razonSocial?: string;
  ruc?: string;
  direccion?: string;
  /**
   * Número de casa (B411 dNumCasRec).
   */
  numeroCasa?: string;
  telefono?: string;
  /**
   * Celular (B413 dCelRec).
   */
  celular?: string;
  email?: string;
  /**
   * Tipo de cliente según SIFEN v1.50.
   * Valores: PERSONA_FISICA, PERSONA_JURIDICA, NO_CONTRIBUYENTE, EXTRANJERO, GUBERNAMENTAL
   */
  tipoClienteSifen?: 'PERSONA_FISICA' | 'PERSONA_JURIDICA' | 'NO_CONTRIBUYENTE' | 'EXTRANJERO' | 'GUBERNAMENTAL';
  /**
   * Campo legacy para compatibilidad.
   * @deprecated Usar tipoClienteSifen en su lugar
   */
  tributa: boolean;
  /**
   * Campo legacy para compatibilidad.
   * @deprecated Usar tipoClienteSifen en su lugar
   */
  tipoContribuyente?: 'PF' | 'PJ' | 'EG';
  /**
   * ID del país (B415 cPaisRec).
   */
  paisId?: number;
  /**
   * ID de la ciudad (B410 cCiuRec).
   */
  ciudadId?: number;
  activo: boolean;
}
