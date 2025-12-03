# Tareas Pendientes y Errores Conocidos

Este documento contiene un registro de todas las funcionalidades que aún faltan implementar o que tienen errores conocidos en el sistema FRC-eFact.

---

## Funcionalidades con Errores o Implementación Incompleta

### Inutilización de Números

**Estado:** ⚠️ Funciona parcialmente  
**Prioridad:** Media/Baja  
**Descripción:** La funcionalidad de inutilización de números funciona en su versión desvinculada con una factura legal, es decir, funciona a medias.

**TODO:**
- Revisar la implementación completa de inutilización de números
- Verificar la vinculación con facturas legales
- Completar la funcionalidad para que funcione correctamente en todos los casos

**Archivos relacionados:**
- `frc-efact-frontend/src/app/features/facturacion/inutilizar-numeros-dialog.component.ts`
- Backend: Servicios relacionados con inutilización de números

**Notas:**
- Funciona cuando no está vinculado a una factura legal
- Requiere revisión para casos donde está vinculado a facturas legales

---

## Funcionalidades Pendientes de Implementar

_(Añadir nuevas funcionalidades pendientes aquí)_

---

## Mejoras Sugeridas

_(Añadir mejoras sugeridas aquí)_

---

## Notas

- Las prioridades se clasifican como: **Alta**, **Media**, **Baja**
- Los estados pueden ser: ✅ **Completo**, ⚠️ **Parcial**, ❌ **No implementado**, 🐛 **Con errores**

