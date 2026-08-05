# Reglas duras — Frontend (Angular 17)

Verificado 2026-08-05. Fuente: CLAUDE.md, `frc-efact-frontend/SECURITY.md`, [../reference/ngrx-state-index.md](../reference/ngrx-state-index.md).

## 1. URLs API incluyen `/api/`
Al revés del backend: acá el path **sí** lleva `/api/` porque viene en `environment.apiUrl`.

```ts
private readonly baseUrl = `${environment.apiUrl}/clientes`; // ✅
```
`apiUrl` = `http://localhost:8080/api` (dev) / `https://efact.frc-ecommerce.com/api` (prod, VM Hetzner).

⚠️ Los 3 endpoints con doble prefijo (`geografia`, `auditoria`, `reportes`) se consumen hoy con `/api/api/...` desde su api-service — es deuda conocida, no “arreglar” un lado solo. Ver [backend-rules.md](backend-rules.md).

## 2. Nomenclatura
- Archivos **kebab-case**: `cliente-list.component.ts`, `auth.guard.ts`.
- Clases **PascalCase**: `ClienteListComponent`.
- Sufijos obligatorios: servicios `Service`, guards `Guard`.

## 3. NgRx — una rama por entidad principal, pero decidir explícitamente
La convención dice “un store por entidad principal”, pero **no se cumple**: solo **8** entidades tienen store en `core/state/`:
`auth`, `empresas`, `facturacion`, `documentos`, `usuarios`, `timbrados`, `timbrado-detalles`, `notas`.

SIN store (usan el api-service directo): `clientes`, `productos`, `reportes`, `dashboard`, `auditoria`, `transporte`.

→ Al agregar una entidad nueva **decidí a propósito** si lleva store o no. No asumas que todas lo tienen. Cada rama NgRx = `actions / effects / reducer / selectors`.

## 4. Separar list y form
Componentes divididos: `*-list.component.ts` (listado) y `*-form.component.ts` (alta/edición). Diálogos con sufijo `-dialog`.

## 5. JWT en memoria — nunca `localStorage`
El token vive en memoria (NgRx/servicio), no en `localStorage` ni `sessionStorage`.

## 6. Angular Material — NO Tailwind
UI con **Angular Material 17 + SCSS**. No introducir Tailwind ni otro framework de estilos. Charts con Chart.js.

## 7. Idioma
UI y dominio en **español**.

## 8. Compilar antes de commit
`cd frc-efact-frontend && npm run build:dev`. Si falla, **no** commitear. (⚠️ `npm run lint` **no funciona**: falta el target `lint` en `angular.json`.) (El deploy a prod es manual por SSH a la VM Hetzner; ver [../workflows/deploy-hetzner.md](../workflows/deploy-hetzner.md).)
