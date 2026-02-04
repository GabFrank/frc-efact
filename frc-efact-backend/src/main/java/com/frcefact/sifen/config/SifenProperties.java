package com.frcefact.sifen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades globales para la integración con SIFEN.
 * <p>
 * Estas propiedades definen parámetros generales que aplican a todas las empresas,
 * tales como el ambiente por defecto, habilitación de la Nota Técnica 13 y
 * configuración del scheduler y lotes.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "sifen")
public class SifenProperties {

    /**
     * Indica si la integración con SIFEN está habilitada.
     * Se utiliza para activar/desactivar funcionalidades desde configuración.
     */
    private boolean enabled = false;

    /**
     * Ambiente global por defecto (TEST o PROD).
     * Se usa como fallback si la empresa no tiene un ambiente configurado.
     */
    private String ambiente = "TEST";

    /**
     * Indica si se debe habilitar la Nota Técnica 13 (CDC alfanumérico, DV, etc.).
     */
    private boolean habilitarNotaTecnica13 = true;

    /**
     * Tipo de contribuyente del emisor por defecto.
     * Valores según SIFEN: 1 = Persona Física, 2 = Persona Jurídica.
     */
    private int tipoContribuyenteEmisor = 2;

    private final Scheduler scheduler = new Scheduler();
    private final Lote lote = new Lote();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public boolean isHabilitarNotaTecnica13() {
        return habilitarNotaTecnica13;
    }

    public void setHabilitarNotaTecnica13(boolean habilitarNotaTecnica13) {
        this.habilitarNotaTecnica13 = habilitarNotaTecnica13;
    }

    public int getTipoContribuyenteEmisor() {
        return tipoContribuyenteEmisor;
    }

    public void setTipoContribuyenteEmisor(int tipoContribuyenteEmisor) {
        this.tipoContribuyenteEmisor = tipoContribuyenteEmisor;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Lote getLote() {
        return lote;
    }

    public static class Scheduler {
        /**
         * Permite activar/desactivar el scheduler globalmente.
         */
        private boolean enabled = false;

        /**
         * Delay por defecto entre ejecuciones (ms).
         */
        private long fixedDelay = 300_000L;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getFixedDelay() {
            return fixedDelay;
        }

        public void setFixedDelay(long fixedDelay) {
            this.fixedDelay = fixedDelay;
        }
    }

    public static class Lote {
        private int maxSize = 50;
        private int maxRetries = 5;

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
    }
}

