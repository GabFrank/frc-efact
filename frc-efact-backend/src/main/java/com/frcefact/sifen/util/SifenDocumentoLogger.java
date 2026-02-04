package com.frcefact.sifen.util;

import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.beans.DocumentoElectronico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidad para inspeccionar y loguear el estado interno de DocumentoElectronico al generar CDC/NT13.
 */
public final class SifenDocumentoLogger {

    private static final Logger log = LoggerFactory.getLogger(SifenDocumentoLogger.class);

    private SifenDocumentoLogger() {
    }

    public static void logDatosCdc(DocumentoElectronico de, SifenConfig config) {
        try {
            log.info("═══════════════════════════════════════════════════════════════");
            log.info("🔍 Debug CDC / NT13 en DocumentoElectronico");
            log.info("   - dDVId: {}", de.getdDVId());
            log.info("   - CDC generado: {}", de.obtenerCDC());
            log.info("   - habilitarNotaTecnica13 (config): {}", config != null ? config.isHabilitarNotaTecnica13() : "null");
            log.info("   - getId(): {}", de.getId());
        } catch (Exception e) {
            log.warn("⚠️ No se pudo loguear detalles de DocumentoElectronico: {}", e.getMessage());
        } finally {
            log.info("═══════════════════════════════════════════════════════════════");
        }
    }
}
