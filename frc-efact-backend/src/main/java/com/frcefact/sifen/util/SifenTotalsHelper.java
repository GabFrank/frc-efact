package com.frcefact.sifen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * Utilidades para trabajar con totales de jsifenlib.
 * Aplica fixes conocidos necesarios antes de enviar documentos a SIFEN.
 */
public final class SifenTotalsHelper {

    private static final Logger log = LoggerFactory.getLogger(SifenTotalsHelper.class);

    private SifenTotalsHelper() {
        // Utility
    }

    /**
     * Bug conocido de jsifenlib: los campos dLiqTotIVA no se setean automáticamente.
     * Se asignan utilizando reflexión antes de serializar el XML.
     */
    public static void applyIvaFix(Object totales) {
        if (totales == null) {
            return;
        }
        try {
            BigDecimal iva10 = invokeGetter(totales, "getdIVA10");
            if (iva10 != null && iva10.compareTo(BigDecimal.ZERO) > 0) {
                setFieldValue(totales, "dLiqTotIVA10", iva10);
            }

            BigDecimal iva5 = invokeGetter(totales, "getdIVA5");
            if (iva5 != null && iva5.compareTo(BigDecimal.ZERO) > 0) {
                setFieldValue(totales, "dLiqTotIVA5", iva5);
            }
        } catch (Exception e) {
            log.warn("No se pudo aplicar fix de totales IVA: {}", e.getMessage());
        }
    }

    private static BigDecimal invokeGetter(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        Object value = method.invoke(target);
        return value instanceof BigDecimal ? (BigDecimal) value : null;
    }

    private static void setFieldValue(Object totales, String fieldName, BigDecimal value) throws Exception {
        Field field = totales.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(totales, value);
    }
}

