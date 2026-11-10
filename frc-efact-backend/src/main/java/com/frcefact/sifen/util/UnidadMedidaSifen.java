package com.frcefact.sifen.util;

import com.roshka.sifen.core.types.TcUniMed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Resuelve el {@code cUniMed} de SIFEN a partir de la unidad de medida del producto.
 *
 * <p><b>Por qué existe.</b> La unidad de medida estaba desconectada del catálogo de la SET en
 * cuatro puntos que se reforzaban entre sí:
 *
 * <ol>
 *   <li>El selector del formulario de producto ofrecía 14 opciones inventadas, de las cuales solo
 *       cuatro existían en {@link TcUniMed}: {@code UNI}, {@code ML}, {@code M2} y {@code M3}. Las
 *       otras diez ({@code KG}, {@code G}, {@code L}, {@code M}, {@code H}, {@code SERV},
 *       {@code PAR}, {@code CAJ}, {@code BOL}, {@code TUB}) no están en el catálogo.</li>
 *   <li>El formulario forzaba mayúsculas. Como {@code kg}, {@code g}, {@code m}, {@code ml},
 *       {@code ha}, {@code racion}, {@code pm}, {@code Hs}, {@code Km}, {@code Mi}, {@code Ya},
 *       {@code Se} y {@code Di} son minúsculas o mixtas, esos códigos eran
 *       <b>estructuralmente inalcanzables</b>.</li>
 *   <li>Los caminos de factura y nota de crédito ignoraban {@code unidadMedida} por completo y
 *       decidían con el booleano {@code balanza}, que colapsa los 34 códigos a kg-o-unidad.</li>
 *   <li>El camino de nota de remisión sí lo leía, pero con {@code valueOf(x.toUpperCase())}, que
 *       cae en la trampa 2.</li>
 * </ol>
 *
 * <p><b>El case importa y no es cosmético.</b> {@code ML} (88) es <i>Mililitros</i> y {@code ml}
 * (660) es <i>Metro lineal</i>: dos unidades distintas que solo difieren en mayúsculas. Por eso acá
 * <b>no hay matcheo case-insensitive</b> — convertiría metros lineales en mililitros sin que nadie
 * se entere. Lo único que se acepta fuera del match exacto es la tabla explícita de alias legacy.
 *
 * <p><b>Ojo con el nombre de la constante vs. el código.</b> Para casi todas coinciden, pero
 * {@code kg_m2} vale {@code "kg/m2"} y {@code racion} vale {@code "ración"}. Se acepta cualquiera
 * de las dos formas.
 */
public final class UnidadMedidaSifen {

    private static final Logger log = LoggerFactory.getLogger(UnidadMedidaSifen.class);

    /**
     * Valores que el formulario viejo permitía guardar y que no existen en el catálogo de la SET.
     *
     * <p>Los cinco últimos ({@code SERV}, {@code PAR}, {@code CAJ}, {@code BOL}, {@code TUB}) no
     * tienen ningún equivalente en SIFEN, así que van a {@code UNI} — que es exactamente lo que ya
     * se venía emitiendo para ellos al caer en el {@code catch}. El mapeo no cambia ningún
     * documento respecto del comportamiento actual: solo lo hace explícito.
     *
     * <p>La migración V38 normaliza los datos guardados, pero esta tabla se mantiene igual porque
     * la importación de productos por Excel puede volver a introducir estos valores.
     */
    private static final Map<String, TcUniMed> ALIAS_LEGACY = new HashMap<>();

    static {
        ALIAS_LEGACY.put("KG", TcUniMed.kg);      // el código real es "kg", minúscula
        ALIAS_LEGACY.put("G", TcUniMed.g);        // el código real es "g"
        ALIAS_LEGACY.put("L", TcUniMed.LT);       // el código real es "LT"
        ALIAS_LEGACY.put("M", TcUniMed.m);        // el código real es "m"
        ALIAS_LEGACY.put("H", TcUniMed.Hs);       // el código real es "Hs"
        ALIAS_LEGACY.put("SERV", TcUniMed.UNI);   // sin equivalente en SIFEN
        ALIAS_LEGACY.put("PAR", TcUniMed.UNI);    // sin equivalente en SIFEN
        ALIAS_LEGACY.put("CAJ", TcUniMed.UNI);    // sin equivalente en SIFEN
        ALIAS_LEGACY.put("BOL", TcUniMed.UNI);    // sin equivalente en SIFEN
        ALIAS_LEGACY.put("TUB", TcUniMed.UNI);    // sin equivalente en SIFEN
    }

    private UnidadMedidaSifen() {
    }

    /**
     * Resuelve la unidad de medida a emitir.
     *
     * <p>Orden de resolución, del dato más confiable al menos:
     * <ol>
     *   <li>Coincidencia exacta con el nombre de la constante ({@code TN}, {@code kg}, {@code UNI})</li>
     *   <li>Coincidencia exacta con el código SIFEN ({@code "kg/m2"}, {@code "ración"})</li>
     *   <li>Alias legacy del formulario viejo</li>
     *   <li>{@code balanza == true} ⇒ {@code kg}. Fallback histórico, solo para no cambiarle la
     *       unidad a los productos que hoy se emiten en kilos sin tener {@code unidadMedida}
     *       cargada</li>
     *   <li>{@code UNI}, dejando aviso en el log</li>
     * </ol>
     *
     * @param unidadMedida el valor guardado en {@code productos.producto.unidad_medida}
     * @param balanza      el flag legacy del producto; puede ser null
     */
    public static TcUniMed resolver(String unidadMedida, Boolean balanza) {
        if (unidadMedida != null && !unidadMedida.isBlank()) {
            String limpio = unidadMedida.trim();

            for (TcUniMed candidato : TcUniMed.values()) {
                if (candidato.name().equals(limpio) || candidato.getAbreviatura().equals(limpio)) {
                    return candidato;
                }
            }

            TcUniMed alias = ALIAS_LEGACY.get(limpio);
            if (alias != null) {
                log.debug("Unidad de medida legacy '{}' mapeada a {} ({})",
                        limpio, alias.name(), alias.getVal());
                return alias;
            }

            log.warn("⚠️ Unidad de medida '{}' no existe en el catálogo de SIFEN ni en los alias "
                    + "conocidos. Se emite UNI. Revisar el producto.", limpio);
            return TcUniMed.UNI;
        }

        if (Boolean.TRUE.equals(balanza)) {
            return TcUniMed.kg;
        }

        return TcUniMed.UNI;
    }
}
