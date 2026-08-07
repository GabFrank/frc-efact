package com.frcefact.validation;

import com.frcefact.model.TipoClienteSifen;
import com.frcefact.util.RucParaguayo;

/**
 * La regla de negocio del RUC según el tipo de cliente, en un solo lugar.
 *
 * <p><b>Contribuyente ⇒ {@code numero-DV} obligatorio. No contribuyente ⇒ solo dígitos, sin
 * guion.</b>
 *
 * <p>Antes esta regla vivía duplicada y con dos niveles de rigor distintos:
 * <ul>
 *   <li>{@code ClienteRucValidator} (sobre el DTO) exigía el guion — pero solo corre cuando algo
 *       valida el DTO con {@code @Valid}, o sea únicamente en el borde HTTP.</li>
 *   <li>{@code ClienteService.validarRucSegunTipoCliente} (sobre la entidad) chequeaba
 *       <b>solo presencia</b>. Cualquier camino que creara un {@code Cliente} sin pasar por el
 *       controller —un import, otro servicio, un seed— podía grabar un contribuyente sin DV.</li>
 * </ul>
 *
 * <p>Ahora los dos delegan acá, así el invariante se cumple sin importar el punto de entrada.
 */
public final class ReglaRucCliente {

    private ReglaRucCliente() {
    }

    /** Resultado de evaluar la regla. {@code mensaje} es null cuando es válido. */
    public record Resultado(boolean valido, String mensaje) {
        static Resultado ok() {
            return new Resultado(true, null);
        }

        static Resultado error(String mensaje) {
            return new Resultado(false, mensaje);
        }
    }

    /**
     * ¿Este tipo de cliente exige RUC con dígito verificador?
     *
     * @param tipoClienteSifen nombre del enum {@link TipoClienteSifen}; puede ser null o inválido
     * @param tributa          campo legacy, usado como fallback cuando el tipo no sirve
     */
    public static boolean requiereRuc(String tipoClienteSifen, Boolean tributa) {
        if (tipoClienteSifen != null && !tipoClienteSifen.isEmpty()) {
            try {
                return TipoClienteSifen.valueOf(tipoClienteSifen).requiereRuc();
            } catch (IllegalArgumentException e) {
                // Valor de enum desconocido: se cae al campo legacy
            }
        }
        return Boolean.TRUE.equals(tributa);
    }

    /**
     * Igual que {@link #requiereRuc(String, Boolean)} pero sobre el enum ya resuelto, que es como
     * lo tiene la entidad {@code Cliente}. Evita el {@code valueOf} redundante en el servicio.
     */
    public static boolean requiereRuc(TipoClienteSifen tipoClienteSifen, Boolean tributa) {
        if (tipoClienteSifen != null) {
            return tipoClienteSifen.requiereRuc();
        }
        return Boolean.TRUE.equals(tributa);
    }

    /**
     * Evalúa la regla completa.
     *
     * @param requiereRuc si el tipo de cliente es contribuyente
     * @param ruc         el RUC tal como viene, puede ser null o vacío
     */
    public static Resultado validar(boolean requiereRuc, String ruc) {
        boolean vacio = ruc == null || ruc.trim().isEmpty();

        if (requiereRuc) {
            if (vacio) {
                return Resultado.error("El RUC es requerido para este tipo de cliente");
            }
            if (!RucParaguayo.tieneFormatoContribuyente(ruc)) {
                return Resultado.error(
                        "Los contribuyentes deben declarar el RUC como numero-DV, con dígito "
                                + "verificador (ej: 80016875-5)");
            }
            // El formato ya está garantizado; parse verifica además que el DV sea el correcto.
            // Se atrapa la excepción a propósito: esta clase también corre dentro de un
            // ConstraintValidator, donde una excepción escapando se convierte en un 500 en vez
            // del 400 con el mensaje de validación. El mensaje de parse ya dice cuál es el DV
            // correcto, así que se reusa tal cual.
            try {
                RucParaguayo.parse(ruc);
            } catch (IllegalArgumentException e) {
                return Resultado.error(e.getMessage());
            }
            return Resultado.ok();
        }

        // No contribuyente: el RUC es opcional, pero si viene NO debe traer DV.
        if (!vacio && ruc.contains("-")) {
            return Resultado.error(
                    "Los no contribuyentes no deben declarar dígito verificador. Use solo números "
                            + "o deje el campo vacío");
        }
        return Resultado.ok();
    }
}
