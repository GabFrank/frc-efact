> # ⚠️ OBSOLETO (resuelto) — ver [ESTADO_VALIDACION_RUC.md](./ESTADO_VALIDACION_RUC.md)
>
> El backend **ya implementa** este validador correcto (`CalcularVerificadorRuc` +
> `RucValidator`, módulo 11 con cadena invertida). Este documento describía la solución
> como pendiente de aplicar; **ya fue aplicada**. Sirve solo como referencia histórica del
> algoritmo. La deuda actual está en el **frontend** (validación apagada + mock
> interceptor) — ver el documento de estado.

<details>
<summary>Contenido histórico (obsoleto)</summary>

# Validador RUC Correcto para Backend

## 🚨 Problema Identificado

El backend actual rechaza RUCs válidos porque usa un algoritmo de validación incorrecto.

**Ejemplo del error:**
```
Field error: rejected value [80127721-3]
message: Dígito verificador del RUC es incorrecto
```

Pero `80127721-3` **ES VÁLIDO** según el algoritmo oficial paraguayo.

## ✅ Solución: Código Java Correcto

### Implementación Completa

```java
public class CalcularVerificadorRuc {
    
    /**
     * Calcula el dígito verificador de un RUC paraguayo
     * @param ruc Número de RUC sin el dígito verificador
     * @return Dígito verificador calculado
     */
    public static int getDigitoVerificador(String ruc) {
        return getDigitoVerificador(ruc, 11);
    }
    
    /**
     * Obtiene el dígito verificador como string con guión
     * @param ruc Número de RUC sin el dígito verificador
     * @return String en formato "-X" donde X es el dígito verificador
     */
    public static String getDigitoVerificadorString(String ruc) {
        Integer digito = getDigitoVerificador(ruc, 11);
        if (digito != null) {
            return "-" + digito;
        }
        return "";
    }
    
    /**
     * Calcula el dígito verificador usando módulo especificado
     * @param ruc Número de RUC sin el dígito verificador
     * @param base Base para el cálculo del módulo (normalmente 11)
     * @return Dígito verificador calculado
     */
    public static Integer getDigitoVerificador(String ruc, int base) {
        if (ruc.length() < 4) return null;
        
        int k = 2;
        int total = 0;
        String alRevez = invertirCadena(eliminarNoDigitos(ruc));
        
        for (char numero : alRevez.toCharArray()) {
            total += (numero - '0') * k++;
            if (k > base) {
                k = 2;
            }
        }
        
        int resto = total % base;
        return resto > 1 ? base - resto : 0;
    }
    
    /**
     * Invierte una cadena de caracteres
     * @param ruc Cadena a invertir
     * @return Cadena invertida
     */
    protected static String invertirCadena(String ruc) {
        // Si se dispone de Apache Commons se puede usar:
        // StringUtils.reverse(ruc);
        return new StringBuilder(ruc).reverse().toString();
    }
    
    /**
     * Elimina todos los caracteres no numéricos de la cadena
     * @param ruc RUC con números, símbolos y letras
     * @return Versión del RUC consistente de solo dígitos
     */
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

### Validador Spring Boot

```java
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RucValidator implements ConstraintValidator<ValidRuc, String> {
    
    @Override
    public void initialize(ValidRuc constraintAnnotation) {
        // Inicialización si es necesaria
    }
    
    @Override
    public boolean isValid(String ruc, ConstraintValidatorContext context) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return true; // Dejar que @NotNull maneje los valores nulos
        }
        
        // Validar formato básico
        if (!ruc.matches("\\d{6,8}-\\d")) {
            return false;
        }
        
        // Separar número y dígito verificador
        String[] parts = ruc.split("-");
        if (parts.length != 2) {
            return false;
        }
        
        String numero = parts[0];
        int digitoProporcionado;
        
        try {
            digitoProporcionado = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        
        // Calcular dígito verificador correcto
        Integer digitoCalculado = CalcularVerificadorRuc.getDigitoVerificador(numero);
        
        if (digitoCalculado == null) {
            return false;
        }
        
        // Comparar dígitos
        return digitoCalculado.equals(digitoProporcionado);
    }
}
```

### Anotación de Validación

```java
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RucValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRuc {
    String message() default "Formato de RUC inválido o dígito verificador incorrecto";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## 🧪 Casos de Prueba

### RUCs Válidos (Deben Pasar)
```java
@Test
public void testRucsValidos() {
    assertTrue(isValidRuc("4043581-4"));     // Tu RUC
    assertTrue(isValidRuc("80127721-3"));    // El que falló antes
    assertTrue(isValidRuc("80012345-3"));    // RUC de prueba
    assertTrue(isValidRuc("80098765-2"));    // RUC de prueba
}
```

### RUCs Inválidos (Deben Fallar)
```java
@Test
public void testRucsInvalidos() {
    assertFalse(isValidRuc("80127721-4"));   // DV incorrecto
    assertFalse(isValidRuc("4043581-5"));    // DV incorrecto
    assertFalse(isValidRuc("123-4"));        // Muy corto
    assertFalse(isValidRuc("12345678901-1")); // Muy largo
    assertFalse(isValidRuc("1234567-"));     // Sin DV
    assertFalse(isValidRuc("1234567-ab"));   // DV no numérico
}
```

## 📋 Pasos para Implementar

### 1. Reemplazar Validador Actual
```java
// Buscar en el proyecto backend:
// - @ValidRuc annotation
// - RucValidator class
// - Cualquier lógica de validación RUC
```

### 2. Agregar Nuevas Clases
```
src/main/java/com/frcefact/
├── validation/
│   ├── ValidRuc.java           (anotación)
│   ├── RucValidator.java       (validador Spring)
│   └── CalcularVerificadorRuc.java (algoritmo)
└── test/
    └── validation/
        └── RucValidatorTest.java (tests)
```

### 3. Actualizar DTOs
```java
public class EmpresaDto {
    @NotBlank(message = "El RUC es requerido")
    @ValidRuc(message = "Formato de RUC inválido o dígito verificador incorrecto")
    private String ruc;
    
    // ... otros campos
}
```

### 4. Probar Casos Reales
```bash
# Probar con curl o Postman
curl -X PUT /api/empresas/1 \
  -H "Content-Type: application/json" \
  -d '{"ruc": "80127721-3", "razonSocial": "TEST"}' # Debe funcionar

curl -X PUT /api/empresas/1 \
  -H "Content-Type: application/json" \
  -d '{"ruc": "80127721-4", "razonSocial": "TEST"}' # Debe fallar
```

## ✅ Resultado Esperado

Después de implementar:
- ✅ `80127721-3` será aceptado (antes rechazado)
- ✅ `4043581-4` seguirá siendo aceptado
- ✅ RUCs inválidos seguirán siendo rechazados
- ✅ Frontend y backend estarán sincronizados

## 🔄 Sincronización Frontend/Backend

Una vez implementado en backend:
- ✅ Frontend ya usa el algoritmo correcto
- ✅ Validación en tiempo real funcionará perfectamente
- ✅ No habrá discrepancias entre validaciones

## 📞 Verificación

Para verificar que funciona:
1. **Implementar** el código en backend
2. **Probar** con `80127721-3` (debe aceptar)
3. **Probar** con `80127721-4` (debe rechazar)
4. **Confirmar** que frontend y backend coinciden

---

**Prioridad**: 🔥 Alta - Bloquea actualizaciones de RUC
**Esfuerzo**: ⏱️ 30 minutos - Solo reemplazar validador existente
**Impacto**: 🎯 Alto - Resuelve problema crítico de validación

</details>