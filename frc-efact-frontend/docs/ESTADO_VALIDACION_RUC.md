# Estado real de la validación de RUC (frontend + backend)

> **Fuente de verdad.** Este documento reemplaza a los antiguos
> `URGENTE_BACKEND_RUC_FIX.md`, `BACKEND_RUC_VALIDATOR_CORRECTO.md` y
> `RUC_VALIDATION_SYNC.md`, que quedaron **obsoletos y engañosos** (presentaban como
> crítico/abierto un bug de backend que ya está resuelto).

Última verificación contra código: **2026**.

---

## Resumen en 2 líneas

- **Backend:** ✅ correcto. Usa `CalcularVerificadorRuc` (módulo 11 con cadena invertida),
  el algoritmo oficial paraguayo. Acepta RUCs válidos y rechaza los inválidos.
- **Frontend:** ⚠️ validación de RUC **deshabilitada a propósito**, y un
  **interceptor mock sigue activo** interceptando `/api/empresas/validate-ruc` — incluso
  en producción. El "workaround temporal" quedó permanente. **Deuda técnica.**

---

## Backend — resuelto (correcto)

El validador del backend calcula el dígito verificador con el algoritmo oficial:
limpiar no-dígitos → invertir la cadena → pesos `2..11` cíclicos → `resto = total % 11`
→ DV = `resto > 1 ? 11 - resto : 0`.

Ejemplo `80127721-3`: invertido `12772108`, total `162`, `162 % 11 = 8`, `11 - 8 = 3` → **válido**.

Los documentos históricos afirmaban que el backend "rechazaba RUCs válidos". **Eso ya
no ocurre**: el validador fue corregido. No hay ninguna acción pendiente del lado del
backend respecto de este algoritmo.

---

## Frontend — deuda técnica (validación real apagada)

Dos piezas mantienen la validación de RUC efectivamente desactivada:

### 1. `src/app/services/ruc-validation.service.ts`

- `validateRucFormat()` valida **solo el formato** (`/^\d{6,8}-\d$/`) y retorna `true`.
  La verificación real del dígito (`validateRucCheckDigit`) está **comentada**.
- `validateRucLive()` retorna `of({ valid: true, exists: false })` sin llamar al backend.
  El bloque real (llamada HTTP a `/empresas/validate-ruc`) está **comentado** con un
  `// TODO: Reactivar cuando backend use algoritmo correcto`.
- Paradójicamente, el propio servicio **ya tiene** el algoritmo correcto implementado en
  `calculateCheckDigit()` (mismo módulo 11 invertido que el backend). Solo hay que
  volver a cablearlo.

### 2. `src/app/interceptors/mock-ruc.interceptor.ts` (REGISTRADO en `app.config.ts`)

- Está registrado en la cadena `withInterceptors([...])` de `app.config.ts`, **sin guarda
  por entorno**: corre en dev y en prod.
- Intercepta cualquier request a `/api/empresas/validate-ruc` y responde con datos
  **mock hardcodeados** (lista `existingRucs` con RUCs ficticios como `80012345-3`
  "EMPRESA DEMO S.A."). La request nunca llega al backend real.

### Consecuencia

La verificación de dígito verificador y la comprobación de duplicados **no se ejercen**
desde la UI. El formulario solo comprueba el patrón `NNNNNNNN-N`. Cualquier respaldo de
"RUC ya registrado" que muestre la UI proviene de datos ficticios del interceptor.

---

## Cómo reactivar la validación real (checklist de deuda)

1. **Quitar el mock interceptor:** eliminar `mockRucInterceptor` del array
   `withInterceptors([...])` en `src/app/app.config.ts` (y, opcionalmente, borrar
   `interceptors/mock-ruc.interceptor.ts`).
2. **Reactivar el servicio** `ruc-validation.service.ts`:
   - En `validateRucFormat()`, descomentar `return this.validateRucCheckDigit(ruc);`.
   - En `validateRucLive()`, quitar el `of({ valid: true, ... })` temprano y descomentar
     el bloque HTTP real contra `${API_BASE_URL}/empresas/validate-ruc`.
3. **Verificar el endpoint** `GET /api/empresas/validate-ruc` en el backend (existencia,
   parámetros `ruc` / `excludeId`, forma de respuesta `{ valid, exists, razonSocial }`).
4. **Probar** con casos reales: `80127721-3` (válido), `80127721-4` (DV inválido),
   y un RUC ya existente (debe reportar duplicado desde el backend, no desde el mock).
5. **Interceptores muertos relacionados:** `interceptors/ruc-workaround.interceptor.ts`
   no está registrado en ningún lado; considerar eliminarlo para evitar confusión.

---

## Archivos relevantes

| Archivo | Rol | Estado |
|---------|-----|--------|
| `src/app/services/ruc-validation.service.ts` | Validación de RUC (formato + DV + live) | ⚠️ DV y live comentados; algoritmo correcto presente pero sin usar |
| `src/app/interceptors/mock-ruc.interceptor.ts` | Mock de `/validate-ruc` | ⚠️ Registrado en `app.config.ts`, activo en prod |
| `src/app/interceptors/ruc-workaround.interceptor.ts` | (workaround viejo) | ☠️ No registrado / muerto |
| `src/app/app.config.ts` | Registra interceptores | Incluye `mockRucInterceptor` |
| Backend `CalcularVerificadorRuc` / `RucValidator` | Validación oficial | ✅ Correcto |
</content>
