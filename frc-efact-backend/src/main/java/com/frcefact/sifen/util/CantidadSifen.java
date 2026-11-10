package com.frcefact.sifen.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Normaliza la cantidad de un ítem para el campo {@code dCantProSer} del XML de SIFEN.
 *
 * <p><b>El bug que esto corrige.</b> {@code SifenService} decidía la precisión decimal con el
 * booleano {@code balanza} del producto:
 *
 * <pre>
 * if (producto.getBalanza()) {
 *     cantidad = item.getCantidad().setScale(3, HALF_UP);
 * } else {
 *     cantidad = item.getCantidad().setScale(0, HALF_UP);   // ← destruye el dato
 * }
 * </pre>
 *
 * <p>Una factura de <b>575,195 toneladas</b> de maíz a USD 150 se emitió con
 * {@code dCantProSer = 575}. Como jsifenlib deriva {@code dTotBruOpeItem} de
 * {@code dPUniProSer × dCantProSer} ({@code TgValorItem.java:31}), ese único campo truncado
 * arrastró todos los totales: el DE salió por <b>USD 86.250 en vez de 86.279,25</b>, con el IVA y
 * el total en guaraníes recalculados sobre el monto equivocado. El cliente tuvo que cancelarlo.
 *
 * <p><b>No hay ningún caso en que redondear a entero sea correcto.</b> Si se venden 3 unidades, el
 * {@code setScale(0)} no cambia nada; si se venden 575,195, destruye el dato. Es puro daño
 * potencial. El XSD admite cuatro decimales sin condiciones:
 * {@code tdCantProSer} tiene {@code totalDigits=14} y {@code fractionDigits=4}
 * ({@code DE_Types_v150.xsd:1200-1204}).
 *
 * <p><b>Y SIFEN no ataja esto.</b> El DE de arriba salió con {@code dMonTiPag = 86279,25} —el total
 * correcto, que viene de otro camino— sobre una operación declarada de {@code 86250}. La SET
 * <b>aprobó</b> ese XML internamente inconsistente. No se puede delegar en su validación.
 */
public final class CantidadSifen {

    /** Máximo que admite {@code tdCantProSer} en el XSD de SIFEN v150. */
    public static final int MAX_DECIMALES = 4;

    private CantidadSifen() {
    }

    /**
     * Devuelve la cantidad lista para {@code setdCantProSer}, sin perder decimales significativos.
     *
     * <p>Se limita a {@link #MAX_DECIMALES} solo porque es el techo del XSD, y se quitan los ceros
     * a la derecha para no emitir {@code 575,0000} donde alcanza {@code 575}.
     *
     * <p><b>La trampa.</b> {@code new BigDecimal("575.000").stripTrailingZeros()} devuelve
     * {@code 5.75E+2} (escala negativa), y jsifenlib serializa con
     * {@code String.valueOf(this.dCantProSer)} ({@code TgCamItem.java:74}), así que esa notación
     * científica iría <b>tal cual al XML</b> y SIFEN rechazaría el documento. Por eso se fuerza la
     * escala a cero cuando queda negativa.
     */
    public static BigDecimal normalizar(BigDecimal cantidad) {
        if (cantidad == null) {
            return null;
        }

        BigDecimal acotada = cantidad.scale() > MAX_DECIMALES
                ? cantidad.setScale(MAX_DECIMALES, RoundingMode.HALF_UP)
                : cantidad;

        BigDecimal sinCerosSobrantes = acotada.stripTrailingZeros();

        // Escala negativa ⇒ toString() usaría notación científica. Ver el javadoc.
        return sinCerosSobrantes.scale() < 0
                ? sinCerosSobrantes.setScale(0, RoundingMode.UNNECESSARY)
                : sinCerosSobrantes;
    }
}
