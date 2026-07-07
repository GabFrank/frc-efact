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

### API pública de facturación electrónica (roadmap, anotado 2026-07-07)

**Estado:** ❌ No implementado
**Prioridad:** Media
**Descripción:** Exponer las capacidades SIFEN de frc-efact (emisión de DE, notas
C/D/R, eventos, consulta de estado, KuDE) como API para que otras apps del
ecosistema (Franco Systems central/filial, e-commerce, etc.) facturen sin
reimplementar SIFEN.

Consideraciones de diseño relevadas:
- **Auth M2M**: hoy el API usa JWT de usuario. Para apps consumidoras usar
  OAuth2 client_credentials (Auth0 ya está integrado → apps M2M de Auth0) o
  API keys por app, siempre scopeadas a una `empresa` (el modelo multi-empresa
  ya existe: `usuario_empresa` → algo análogo `app_empresa`).
- **Versionado**: prefijo `/v1` dentro del context-path y contratos DTO estables;
  publicar el OpenAPI (springdoc ya lo genera).
- **Idempotencia**: header `Idempotency-Key` en la emisión — un retry del
  cliente NO debe duplicar una factura/DE.
- **Async**: la aprobación SIFEN es asíncrona (lote + polling). Ofrecer webhook
  de callback (`APROBADO`/`RECHAZADO`) además del polling del cliente.
- **Rate limiting y auditoría por app** (base ya existe: RateLimiting + AuditLog).
- Primer consumidor natural: reemplaza el flujo manual de la skill
  `migrate-de-central-to-frc-efact` (central podría emitir NC/ND directo via API).

### Post-migración Hetzner (anotado 2026-07-07 — ver docs/deployment/hetzner/RUNBOOK_VM.md)

**Estado:** ⚠️ Parcial (migración hecha; quedan tareas de cierre)
**Prioridad:** Alta

- [ ] Revocar el PAT de GitHub filtrado (sigue válido; Render suspendido ya no lo usa)
- [ ] Copia off-site de backups (sección rclone/rsync de `deploy/backup-db.sh`)
- [ ] Monitoreo externo a `https://efact.frc-ecommerce.com/api/actuator/health`
- [ ] Tras 1-2 semanas estables: dar de baja Render (incl. DB), quitar
      `*.onrender.com` del CORS, mergear la rama de migración a `main`,
      actualizar CLAUDE.md y skill (regla "push = deploy a Render" obsoleta),
      y decidir si se arma deploy por GitHub Actions via SSH a la VM
- [ ] Renovar certificado de FRANCO AREVALOS S.A. (vence **2026-08-20**) y
      re-subirlo desde la UI

---

## Mejoras Sugeridas

_(Añadir mejoras sugeridas aquí)_

---

## Notas

- Las prioridades se clasifican como: **Alta**, **Media**, **Baja**
- Los estados pueden ser: ✅ **Completo**, ⚠️ **Parcial**, ❌ **No implementado**, 🐛 **Con errores**

