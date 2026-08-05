# Checklist de Deployment a Producción

Use este checklist para asegurar que todos los pasos del deployment se completen correctamente.

## Pre-Deployment

- [ ] Código en la rama correcta (main/master)
- [ ] Todos los tests pasan localmente
- [ ] Build del frontend funciona: `cd frc-efact-frontend && npm run build:prod`
- [ ] `render.yaml` está actualizado y correcto
- [ ] `application-prod.yml` tiene todas las configuraciones necesarias
- [ ] `Dockerfile` del backend está funcional
- [ ] Todas las migraciones de Flyway están presentes (35 archivos)
- [ ] Flyway está habilitado en producción (`spring.flyway.enabled=true`)

## Deployment en Render

- [ ] Cuenta de Render creada y configurada
- [ ] Repositorio de GitHub conectado a Render
- [ ] Blueprint aplicado desde `render.yaml`
- [ ] Base de datos `frc-efact-db` creada y "Live"
- [ ] Backend `frc-efact-backend` creado y "Live"
- [ ] Frontend `frc-efact-frontend` creado y "Live"
- [ ] Variables de entorno configuradas correctamente:
  - [ ] `DATABASE_URL` conectado automáticamente
  - [ ] `JWT_SECRET` generado automáticamente
  - [ ] `SPRING_PROFILES_ACTIVE=prod`
  - [ ] `GITHUB_USERNAME` / `GITHUB_TOKEN` — **obligatorias** (Dashboard): sin ellas el build de Docker falla al bajar `jsifenlib`
  - [ ] `MAIL_PASSWORD` — sin valor por defecto; si falta, el arranque puede fallar
  - [ ] `ENCRYPTION_KEY` — 32 chars; si falta se usa un default inseguro
  - [ ] (`JWT_EXPIRATION` y `LOG_LEVEL` figuran en `render.yaml` pero son **inertes** — el código no las lee)

## Migración de Datos

- [ ] Datos exportados de desarrollo usando `export-dev-data.sh`
- [ ] Archivos de exportación revisados y verificados
- [ ] Backend en producción iniciado completamente (Flyway aplicó migraciones)
- [ ] Datos importados a producción usando `import-prod-data.sh`
- [ ] Secuencias restauradas correctamente
- [ ] Reporte de datos verificado (coincide con desarrollo)

## Configuración Post-Deployment

- [ ] URL del backend obtenida de Render
- [ ] `environment.prod.ts` actualizado con URL del backend
- [ ] CORS configurado en `application-prod.yml` con URL del frontend
- [ ] Commit y push de cambios al repositorio
- [ ] Frontend redeployado con nueva configuración

## Verificación

- [ ] Backend responde en `/api/actuator/health` → `{"status":"UP"}` (context-path `/api`)
- [ ] Frontend carga correctamente en la URL de producción
- [ ] Login funciona con usuarios existentes
- [ ] Dashboard carga datos correctamente
- [ ] Navegación funciona en todas las rutas
- [ ] CRUD de productos funciona
- [ ] CRUD de clientes funciona
- [ ] Crear factura funciona
- [ ] Empresas se muestran correctamente
- [ ] Timbrados se muestran correctamente

## Seguridad

- [ ] JWT_SECRET es único y seguro (generado automáticamente)
- [ ] HTTPS habilitado (automático en Render)
- [ ] CORS configurado solo para el frontend de producción
- [ ] Credenciales no expuestas en el código
- [ ] Variables de entorno sensibles configuradas en Render

## Documentación

- [ ] URLs de producción documentadas
- [ ] Credenciales de acceso documentadas (guardadas de forma segura)
- [ ] Proceso de backup documentado
- [ ] Proceso de restauración documentado
- [ ] Troubleshooting documentado

## Backup

- [ ] Backup inicial de base de datos de producción realizado
- [ ] Proceso de backup automatizado configurado (si aplica)
- [ ] Ubicación de backups documentada

## Monitoreo

- [ ] Logs del backend accesibles y revisados
- [ ] Logs del frontend accesibles y revisados
- [ ] Health checks configurados
- [ ] Alertas configuradas (si aplica)

## Notas Finales

- [ ] Equipo notificado del deployment
- [ ] Usuarios notificados si es necesario
- [ ] Plan de rollback documentado y probado

---

**Fecha de Deployment:** _______________  
**Realizado por:** _______________  
**Versión desplegada:** _______________
