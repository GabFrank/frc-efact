package com.frcefact.util;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * RUC paraguayo parseado: número base y dígito verificador.
 *
 * <p><b>Por qué existe esta clase.</b> Antes había once {@code ruc.split("-")} repartidos por
 * {@code SifenService}, {@code SifenEventoService}, {@code EmpresaService}, {@code RucValidator} y
 * {@code Cliente}, con tres niveles distintos de defensa: algunos verificaban
 * {@code length > 1}, otros hacían {@code Short.parseShort} sin try/catch, y otros emitían
 * {@code dDVEmi = ""} cuando el RUC no traía guion. Ese último es el peor: produce un XML que
 * SIFEN rechaza sin explicar por qué, que es la misma forma de bug que costó el {@code 1262} y el
 * problema del separador de actividades económicas.
 *
 * <p>Además el patrón {@code ^\d{6,8}-\d$} estaba duplicado en dos clases. Acá vive una sola vez.
 *
 * <h2>Los dos formatos de RUC del sistema</h2>
 * <ul>
 *   <li><b>Contribuyente:</b> {@code numero-DV} — 6 a 8 dígitos, guion, dígito verificador.
 *       Obligatorio para {@code PERSONA_FISICA}, {@code PERSONA_JURIDICA} y {@code GUBERNAMENTAL}.</li>
 *   <li><b>No contribuyente:</b> solo dígitos, sin guion ni DV. Es el número de documento de un
 *       {@code NO_CONTRIBUYENTE} o {@code EXTRANJERO}.</li>
 * </ul>
 *
 * <p>Quién exige cuál es {@code ClienteRucValidator}. Esta clase solo parsea y decide, por el
 * formato, de cuál de los dos se trata — que es como el dato está efectivamente guardado.
 */
public final class RucParaguayo {

    /** Patrón del RUC de contribuyente. Única definición en el proyecto. */
    public static final String PATRON_CONTRIBUYENTE = "^\\d{6,8}-\\d$";

    private static final Pattern PATTERN = Pattern.compile(PATRON_CONTRIBUYENTE);

    private final String base;
    private final int dv;

    private RucParaguayo(String base, int dv) {
        this.base = base;
        this.dv = dv;
    }

    /**
     * ¿Tiene forma de RUC de contribuyente ({@code numero-DV})?
     *
     * <p>Usar esto para ramificar entre contribuyente y no contribuyente. <b>No</b> inferirlo del
     * largo del string: {@code SifenService} hacía {@code ruc.length() >= 6}, y con eso una cédula
     * de 7 dígitos de un no contribuyente quedaba clasificada como contribuyente y se emitía un
     * receptor con {@code dRucRec} pero sin {@code dDVRec}.
     */
    public static boolean tieneFormatoContribuyente(String ruc) {
        return ruc != null && PATTERN.matcher(ruc.trim()).matches();
    }

    /**
     * Parsea un RUC de contribuyente, verificando el dígito verificador.
     *
     * @throws IllegalArgumentException si el formato no sirve o el DV no corresponde. El mensaje
     *         incluye el DV correcto cuando el formato es válido, para que el error sea accionable.
     */
    public static RucParaguayo parse(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            throw new IllegalArgumentException("El RUC es requerido");
        }
        String limpio = ruc.trim();
        if (!PATTERN.matcher(limpio).matches()) {
            throw new IllegalArgumentException(
                    "Formato de RUC inválido: '" + limpio + "'. Se espera 6-8 dígitos, guion y "
                            + "dígito verificador (ej: 4043581-4, 80016875-5)");
        }
        String[] partes = limpio.split("-");
        String base = partes[0];
        int dvDeclarado = Integer.parseInt(partes[1]);
        Integer dvCorrecto = calcularDv(base);
        if (dvCorrecto == null) {
            throw new IllegalArgumentException("No se pudo calcular el dígito verificador de " + base);
        }
        if (dvCorrecto != dvDeclarado) {
            throw new IllegalArgumentException(
                    "Dígito verificador incorrecto en '" + limpio + "'. El correcto para " + base
                            + " es " + dvCorrecto);
        }
        return new RucParaguayo(base, dvDeclarado);
    }

    /**
     * Igual que {@link #parse(String)} pero devuelve vacío en vez de lanzar. Para los casos donde
     * el RUC es opcional — por ejemplo el transportista de una nota de remisión.
     */
    public static Optional<RucParaguayo> tryParse(String ruc) {
        try {
            return Optional.of(parse(ruc));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Calcula el dígito verificador de un número base por módulo 11.
     *
     * @return el DV, o {@code null} si la entrada es demasiado corta para calcularlo. A diferencia
     *         de {@code CalcularVerificadorRuc.getDigitoVerificador(String)}, no colapsa la
     *         entrada inválida en {@code 0} — que es un DV legítimo y hacía indistinguibles los
     *         dos casos.
     */
    public static Integer calcularDv(String base) {
        return CalcularVerificadorRuc.getDigitoVerificador(base, 11);
    }

    /** Número base, sin el dígito verificador. */
    public String getBase() {
        return base;
    }

    public int getDv() {
        return dv;
    }

    /** El DV como {@code short}, que es lo que piden los setters de jsifenlib. */
    public short getDvComoShort() {
        return (short) dv;
    }

    public String getDvComoString() {
        return String.valueOf(dv);
    }

    @Override
    public String toString() {
        return base + "-" + dv;
    }
}
