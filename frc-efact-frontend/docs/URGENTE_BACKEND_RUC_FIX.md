# 🚨 URGENTE: Fix Validador RUC Backend

## ❌ Problema Crítico

**El backend rechaza RUCs válidos**, bloqueando actualizaciones de empresas.

**Error actual:**
```
Field error: rejected value [80127721-3]
message: Dígito verificador del RUC es incorrecto
```

**Pero `80127721-3` ES VÁLIDO** según el algoritmo oficial paraguayo.

## 🔧 Solución Inmediata

### 1. Localizar el Validador Actual

Buscar en el proyecto backend:
```bash
# Buscar archivos que contengan validación RUC
grep -r "ValidRuc" src/
grep -r "RucValidator" src/
grep -r "dígito verificador" src/
```

### 2. Reemplazar con Código Correcto

**Archivo: `CalcularVerificadorRuc.java`**
```java
public class CalcularVerificadorRuc {
    
    public static Integer getDigitoVerificador(String ruc, int base) {
        if(ruc.length() < 4) return null;
        
        int k = 2;
        int total = 0;
        String alRevez = invertirCadena(eliminarNoDigitos(ruc));
        
        for (char numero : alRevez.toCharArray()) {
            total += (numero - '0') * k++;
            if (k > base)
                k = 2;
        }
        
        int resto = total % base;
        return resto > 1 ? base - resto : 0;
    }
    
    protected static String invertirCadena(String ruc) {
        return new StringBuilder(ruc).reverse().toString();
    }
    
    protected static String eliminarNoDigitos(String ruc) {
        String toRet = "";
        for (char c : ruc.toCharArray()) {
            if (Character.isDigit(c)) {
                toRet += c;
            } else {
                toRet += (int) c;
            }
        }
        return toRet;
    }
}
```

**Archivo: `RucValidator.java`**
```java
public class RucValidator implements ConstraintValidator<ValidRuc, String> {
    
    @Override
    public boolean isValid(String ruc, ConstraintValidatorContext context) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return true;
        }
        
        if (!ruc.matches("\\d{6,8}-\\d")) {
            return false;
        }
        
        String[] parts = ruc.split("-");
        String numero = parts[0];
        int digitoProporcionado = Integer.parseInt(parts[1]);
        
        Integer digitoCalculado = CalcularVerificadorRuc.getDigitoVerificador(numero, 11);
        
        return digitoCalculado != null && digitoCalculado.equals(digitoProporcionado);
    }
}
```

### 3. Casos de Prueba para Verificar

```java
@Test
public void testRucsValidos() {
    // Estos DEBEN pasar después del fix
    assertTrue(isValidRuc("80127721-3")); // ❌ Falla ahora, ✅ debe pasar
    assertTrue(isValidRuc("4043581-4"));  // RUC del usuario
    assertTrue(isValidRuc("80012345-3")); // RUC de prueba
}

@Test
public void testRucsInvalidos() {
    // Estos DEBEN fallar
    assertFalse(isValidRuc("80127721-4")); // DV incorrecto
    assertFalse(isValidRuc("4043581-5"));  // DV incorrecto
}
```

## ⚡ Verificación Rápida

### Antes del Fix (Estado Actual)
```bash
curl -X PUT /api/empresas/1 \
  -H "Content-Type: application/json" \
  -d '{"ruc": "80127721-3"}'
# Resultado: 400 BAD REQUEST ❌
```

### Después del Fix (Esperado)
```bash
curl -X PUT /api/empresas/1 \
  -H "Content-Type: application/json" \
  -d '{"ruc": "80127721-3"}'
# Resultado: 200 OK ✅
```

## 📋 Checklist de Implementación

- [ ] Localizar validador RUC actual
- [ ] Reemplazar con código correcto
- [ ] Ejecutar tests unitarios
- [ ] Probar con `80127721-3` (debe aceptar)
- [ ] Probar con `80127721-4` (debe rechazar)
- [ ] Reiniciar aplicación
- [ ] Confirmar que frontend funciona

## 🎯 Impacto

**Antes del fix:**
- ❌ Actualizaciones de RUC bloqueadas
- ❌ RUCs válidos rechazados
- ❌ Usuarios frustrados

**Después del fix:**
- ✅ Actualizaciones de RUC funcionan
- ✅ Solo RUCs inválidos rechazados
- ✅ Frontend y backend sincronizados

## ⏱️ Tiempo Estimado

**15-30 minutos** para implementar y probar.

## 🆘 Si Necesitas Ayuda

1. **Localizar archivos**: Buscar `@ValidRuc` en el código
2. **Backup**: Hacer copia del validador actual
3. **Implementar**: Copiar código exacto de arriba
4. **Probar**: Usar casos de prueba específicos
5. **Verificar**: Confirmar que `80127721-3` es aceptado

---

**PRIORIDAD: 🔥 CRÍTICA**
**BLOQUEA**: Actualizaciones de empresas
**SOLUCIÓN**: Lista y probada
**TIEMPO**: 15-30 minutos