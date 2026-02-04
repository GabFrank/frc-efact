package com.frcefact.service.sifen;

import com.frcefact.model.DocumentoElectronico;
import com.frcefact.model.EventoCancelacionDE;
import com.frcefact.model.LoteDE;
import com.frcefact.repository.DocumentoElectronicoRepository;
import com.frcefact.repository.EventoCancelacionDERepository;
import com.frcefact.repository.LoteDERepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduler centralizado para operaciones periódicas con SIFEN.
 */
@Component
@Transactional
public class SifenSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SifenSchedulerService.class);

    private final LoteDERepository loteDERepository;
    private final DocumentoElectronicoRepository documentoElectronicoRepository;
    private final EventoCancelacionDERepository eventoCancelacionDERepository;
    private final SifenService sifenService;

    public SifenSchedulerService(LoteDERepository loteDERepository,
                                 DocumentoElectronicoRepository documentoElectronicoRepository,
                                 EventoCancelacionDERepository eventoCancelacionDERepository,
                                 SifenService sifenService) {
        this.loteDERepository = loteDERepository;
        this.documentoElectronicoRepository = documentoElectronicoRepository;
        this.eventoCancelacionDERepository = eventoCancelacionDERepository;
        this.sifenService = sifenService;
    }

    /**
     * Consulta periódicamente los lotes que están en proceso.
     */
    @Scheduled(fixedDelayString = "${sifen.scheduler.consulta-lotes.delay:300000}")
    public void consultarLotesEnProceso() {
        List<LoteDE> lotes = loteDERepository.findLotesEnProceso();
        if (lotes.isEmpty()) {
            return;
        }

        log.debug("⏱️ Consultando estado de {} lotes en proceso", lotes.size());
        for (LoteDE lote : lotes) {
            try {
                sifenService.consultarLote(lote.getId());
            } catch (Exception e) {
                log.error("❌ Error al consultar lote {}: {}", lote.getId(), e.getMessage());
            }
        }
    }

    /**
     * Consulta documentos electrónicos pendientes de respuesta.
     */
    @Scheduled(fixedDelayString = "${sifen.scheduler.consulta-documentos.delay:600000}")
    public void consultarDocumentosPendientes() {
        List<DocumentoElectronico> pendientes = documentoElectronicoRepository.findPendientesConCdc();
        if (pendientes.isEmpty()) {
            return;
        }

        log.debug("⏱️ Consultando estado de {} documentos pendientes", pendientes.size());
        for (DocumentoElectronico documento : pendientes) {
            try {
                sifenService.consultarDocumento(documento.getCdc());
            } catch (Exception e) {
                log.error("❌ Error al consultar documento {}: {}", documento.getCdc(), e.getMessage());
            }
        }
    }

    /**
     * Procesa eventos de cancelación pendientes (placeholder hasta integración completa).
     */
    @Scheduled(fixedDelayString = "${sifen.scheduler.eventos.delay:900000}")
    public void procesarEventosPendientes() {
        List<EventoCancelacionDE> eventos = eventoCancelacionDERepository.findEventosPendientes();
        if (eventos.isEmpty()) {
            return;
        }

        log.debug("⏱️ Encontrados {} eventos de cancelación pendientes (envío a SIFEN pendiente de implementación)", eventos.size());
    }
}








