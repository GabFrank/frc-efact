/**
 * Contiene las entidades JPA del sistema de facturación electrónica.
 * 
 * <p>Las entidades están organizadas por dominio:</p>
 * <ul>
 *   <li>Persona: Usuario, Rol, UsuarioRol</li>
 *   <li>Empresa: Empresa, UsuarioEmpresa</li>
 *   <li>Financiero: Timbrado, TimbradoDetalle, FacturaLegal, FacturaLegalItem, 
 *       DocumentoElectronico, LoteDE, EventoCancelacionDE</li>
 *   <li>Productos: Producto</li>
 *   <li>Clientes: Cliente</li>
 * </ul>
 * 
 * <p>Todas las entidades principales extienden de {@link com.frcefact.model.base.AuditableEntity}
 * para incluir campos de auditoría automáticos.</p>
 */
package com.frcefact.model;
