package com.frcefact.service.sifen;

import com.frcefact.model.*;
import com.frcefact.repository.DocumentoElectronicoRepository;
import com.frcefact.repository.FacturaLegalRepository;
import com.frcefact.service.DocumentoElectronicoService;
import com.frcefact.service.EventoCancelacionDEService;
import com.frcefact.service.EventoInutilizacionDEService;
import com.frcefact.service.EventoNominacionDEService;
import com.frcefact.service.FacturaLegalService;
import com.frcefact.sifen.config.SifenConfigFactory;
import com.frcefact.util.CalcularVerificadorRuc;
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.beans.EventosDE;
import com.roshka.sifen.core.beans.response.RespuestaRecepcionEvento;
import com.roshka.sifen.core.exceptions.SifenException;
import com.roshka.sifen.core.fields.request.event.TgGroupTiEvt;
import com.roshka.sifen.core.fields.request.event.TrGeVeCan;
import com.roshka.sifen.core.fields.request.event.TrGeVeInu;
import com.roshka.sifen.core.fields.request.event.TrGeVeNotRec;
import com.roshka.sifen.core.fields.request.event.TrGesEve;
import com.roshka.sifen.core.types.TiNatRec;
import com.roshka.sifen.core.types.TTiDE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Servicio para gestión de eventos SIFEN (cancelación, nominación, inutilización).
 * Adaptado del proyecto de referencia franco-system-backend-servidor.
 */
@Service
@Transactional
public class SifenEventoService {

    private static final Logger log = LoggerFactory.getLogger(SifenEventoService.class);

    private final DocumentoElectronicoRepository documentoElectronicoRepository;
    private final DocumentoElectronicoService documentoElectronicoService;
    private final EventoCancelacionDEService eventoCancelacionDEService;
    private final EventoInutilizacionDEService eventoInutilizacionDEService;
    private final EventoNominacionDEService eventoNominacionDEService;
    private final FacturaLegalService facturaLegalService;
    private final FacturaLegalRepository facturaLegalRepository;
    private final SifenConfigFactory sifenConfigFactory;

    public SifenEventoService(
            DocumentoElectronicoRepository documentoElectronicoRepository,
            DocumentoElectronicoService documentoElectronicoService,
            EventoCancelacionDEService eventoCancelacionDEService,
            EventoInutilizacionDEService eventoInutilizacionDEService,
            EventoNominacionDEService eventoNominacionDEService,
            FacturaLegalService facturaLegalService,
            FacturaLegalRepository facturaLegalRepository,
            SifenConfigFactory sifenConfigFactory) {
        this.documentoElectronicoRepository = documentoElectronicoRepository;
        this.documentoElectronicoService = documentoElectronicoService;
        this.eventoCancelacionDEService = eventoCancelacionDEService;
        this.eventoInutilizacionDEService = eventoInutilizacionDEService;
        this.eventoNominacionDEService = eventoNominacionDEService;
        this.facturaLegalService = facturaLegalService;
        this.facturaLegalRepository = facturaLegalRepository;
        this.sifenConfigFactory = sifenConfigFactory;
    }

    /**
     * Cancela un documento electrónico enviando un evento de cancelación a SIFEN.
     * Wrapper para compatibilidad con el controlador.
     */
    public EventoCancelacionDE cancelarDocumento(String cdc, String motivo) throws SifenException {
        cancelarDE(cdc, motivo);
        return eventoCancelacionDEService.findActivosByCdcDocumento(cdc).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró el evento de cancelación creado"));
    }

    /**
     * Cancela un documento electrónico enviando un evento de cancelación a SIFEN.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {SifenException.class, IllegalStateException.class, IllegalArgumentException.class})
    public RespuestaRecepcionEvento cancelarDE(String cdc, String motivo) throws SifenException {
        log.info("🚫 [INICIO] Cancelando DE con CDC: {}", cdc);
        log.info("   Motivo: {}", motivo);

        try {
            log.info("   [PASO 1] Buscando documento electrónico en BD...");
            DocumentoElectronico de = documentoElectronicoRepository.findByCdc(cdc)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró DE con CDC: " + cdc));
            log.info("   ✅ DE encontrado - ID: {}, Estado: {}", de.getId(), de.getEstado());

            log.info("   [PASO 2] Verificando si ya tiene cancelación aprobada...");
            if (eventoCancelacionDEService.tieneCancelacionAprobada(de.getId())) {
                log.warn("⚠️ El DE ya tiene un evento de cancelación APROBADO");
                log.warn("   Estado del DE: {}", de.getEstado());
                throw new IllegalStateException("El DE ya fue cancelado exitosamente. No se puede cancelar nuevamente.");
            }
            log.info("   ✅ No tiene cancelación aprobada previa");

            log.info("   [PASO 3] Buscando eventos activos previos...");
            List<EventoCancelacionDE> eventosActivos = eventoCancelacionDEService.findActivosByCdcDocumento(cdc);
            if (!eventosActivos.isEmpty()) {
                log.info("   🔄 Se encontraron {} evento(s) previo(s) - realizando reintento automático", eventosActivos.size());
                for (EventoCancelacionDE eventoAnterior : eventosActivos) {
                    eventoAnterior.setActivo(false);
                    eventoCancelacionDEService.save(eventoAnterior);
                    log.info("      📝 Evento anterior ID {} marcado como inactivo (estado: {})", eventoAnterior.getId(), eventoAnterior.getEstado());
                }
            } else {
                log.info("   ✅ No hay eventos activos previos");
            }

            log.info("   [PASO 4] Construyendo XML del evento de cancelación...");
            TrGeVeCan cancelacion = new TrGeVeCan();
            cancelacion.setId(cdc);
            cancelacion.setmOtEve(motivo);

            TgGroupTiEvt tipoEvento = new TgGroupTiEvt();
            tipoEvento.setrGeVeCan(cancelacion);

            TrGesEve gestionEvento = new TrGesEve();
            int numeroRandom = new Random().nextInt(99999999) + 1;
            String eventoId = String.valueOf(numeroRandom);
            LocalDateTime fechaFirma = LocalDateTime.now();
            gestionEvento.setId(eventoId);
            gestionEvento.setdFecFirma(fechaFirma);
            gestionEvento.setgGroupTiEvt(tipoEvento);

            List<TrGesEve> listaEventos = new ArrayList<>();
            listaEventos.add(gestionEvento);

            EventosDE eventosDE = new EventosDE();
            eventosDE.setrGesEveList(listaEventos);
            log.info("   ✅ XML construido - Evento ID: {}", eventoId);

            log.info("   [PASO 5] Creando registro de EventoCancelacionDE...");
            EventoCancelacionDE eventoCancelacion = new EventoCancelacionDE();
            eventoCancelacion.setDocumentoElectronico(de);
            eventoCancelacion.setEventoId(eventoId);
            eventoCancelacion.setFechaFirma(fechaFirma);
            eventoCancelacion.setCdcDocumento(cdc);
            eventoCancelacion.setMotivoCancelacion(motivo);
            eventoCancelacion.setEstado(EstadoEvento.PENDIENTE);
            eventoCancelacion.setActivo(true);
            log.info("   ✅ Registro de evento creado (aún no guardado en BD)");

            log.info("   [PASO 6] Obteniendo configuración SIFEN...");
            Timbrado timbrado = de.getFacturaLegal().getTimbradoDetalle().getTimbrado();
            SifenConfig config = sifenConfigFactory.buildForTimbrado(timbrado.getId());

            log.info("   [PASO 7] 📤 Enviando evento de cancelación a SIFEN...");
            RespuestaRecepcionEvento respuesta = null;
            try {
                respuesta = sifenConfigFactory.executeWithConfig(config, () -> {
                    return Sifen.recepcionEvento(eventosDE);
                });
                log.info("   ✅ Respuesta recibida de SIFEN");
            } catch (Exception e) {
                log.error("   ❌ ERROR al enviar a SIFEN: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
                throw e;
            }

            log.info("   [PASO 8] Procesando respuesta de SIFEN...");
            procesarRespuestaCancelacion(eventoCancelacion, de, respuesta);

            log.info("   [PASO 9] Guardando evento en BD...");
            try {
                eventoCancelacionDEService.save(eventoCancelacion);
                log.info("   ✅ 💾 Evento guardado en BD - ID: {}, Estado: {}", eventoCancelacion.getId(), eventoCancelacion.getEstado());
            } catch (Exception e) {
                log.error("   ❌ ERROR CRÍTICO al guardar EventoCancelacionDE: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error al guardar evento de cancelación en BD", e);
            }

            log.info("   ✅ [FIN] Proceso de cancelación completado exitosamente");
            return respuesta;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("❌ [ERROR CONTROLADO] {}: {}", e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            if (e instanceof SifenException) {
                log.error("❌ [ERROR SIFEN] {}: {}", e.getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
                throw (SifenException) e;
            }
            log.error("❌ [ERROR NO ESPERADO] {}: {}", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error inesperado en cancelación de DE", e);
        }
    }

    /**
     * Inutiliza un rango de números de documentos electrónicos.
     * Retorna la entidad del evento creado.
     */
    public EventoInutilizacionDE inutilizarNumerosDocumento(
            Timbrado timbrado,
            String establecimiento,
            String puntoExpedicion,
            int numeroInicio,
            int numeroFin,
            TTiDE tipoDE,
            String motivo,
            Long timbradoDetalleId) throws SifenException {
        inutilizarNumeros(timbrado, establecimiento, puntoExpedicion, numeroInicio, numeroFin, tipoDE, motivo, timbradoDetalleId);
        // Buscar el evento más reciente para este timbrado
        return eventoInutilizacionDEService.findByTimbradoIdAndActivoTrue(timbrado.getId()).stream()
                .filter(e -> e.getEstablecimiento().equals(establecimiento) 
                        && e.getPuntoExpedicion().equals(puntoExpedicion)
                        && e.getNumeroInicio().equals(numeroInicio)
                        && e.getNumeroFin().equals(numeroFin))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró el evento de inutilización creado"));
    }

    /**
     * Inutiliza un rango de números de documentos electrónicos.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {SifenException.class, IllegalStateException.class, IllegalArgumentException.class})
    public RespuestaRecepcionEvento inutilizarNumeros(
            Timbrado timbrado,
            String establecimiento,
            String puntoExpedicion,
            int numeroInicio,
            int numeroFin,
            TTiDE tipoDE,
            String motivo,
            Long timbradoDetalleId) throws SifenException {

        log.info("📝 [INICIO] Inutilizando números de documentos");
        log.info("   Timbrado: {}", timbrado.getNumero());
        log.info("   Establecimiento: {}", establecimiento);
        log.info("   Punto Expedición: {}", puntoExpedicion);
        log.info("   Rango: {} - {}", numeroInicio, numeroFin);
        log.info("   Tipo DE: {}", tipoDE);
        log.info("   Motivo: {}", motivo);
        log.info("   Timbrado Detalle ID: {}", timbradoDetalleId);

        try {
            if (numeroInicio > numeroFin) {
                throw new IllegalArgumentException("Número inicial (" + numeroInicio + ") no puede ser mayor que número final (" + numeroFin + ")");
            }

            log.info("   [PASO 1] Construyendo XML del evento de inutilización...");
            TrGeVeInu inutilizacion = new TrGeVeInu();
            inutilizacion.setdNumTim(Integer.parseInt(timbrado.getNumero()));
            inutilizacion.setdEst(establecimiento);
            inutilizacion.setdPunExp(puntoExpedicion);
            inutilizacion.setdNumIn(String.valueOf(numeroInicio));
            inutilizacion.setdNumFin(String.valueOf(numeroFin));
            inutilizacion.setiTiDE(tipoDE);
            inutilizacion.setmOtEve(motivo);

            TgGroupTiEvt tipoEvento = new TgGroupTiEvt();
            tipoEvento.setrGeVeInu(inutilizacion);

            int numeroRandom = new Random().nextInt(99999999) + 1;
            String eventoId = String.valueOf(numeroRandom);
            LocalDateTime fechaFirma = LocalDateTime.now();
            TrGesEve gestionEvento = new TrGesEve();
            gestionEvento.setId(eventoId);
            gestionEvento.setdFecFirma(fechaFirma);
            gestionEvento.setgGroupTiEvt(tipoEvento);

            List<TrGesEve> listaEventos = new ArrayList<>();
            listaEventos.add(gestionEvento);

            EventosDE eventosDE = new EventosDE();
            eventosDE.setrGesEveList(listaEventos);
            log.info("   ✅ XML construido - Evento ID: {}", eventoId);

            log.info("   [PASO 2] Creando registro de EventoInutilizacionDE...");
            EventoInutilizacionDE eventoInutilizacion = new EventoInutilizacionDE();
            eventoInutilizacion.setTimbrado(timbrado);
            if (timbradoDetalleId != null) {
                TimbradoDetalle timbradoDetalle = new TimbradoDetalle();
                timbradoDetalle.setId(timbradoDetalleId);
                eventoInutilizacion.setTimbradoDetalle(timbradoDetalle);
            }
            eventoInutilizacion.setEventoId(eventoId);
            eventoInutilizacion.setFechaFirma(fechaFirma);
            eventoInutilizacion.setEstablecimiento(establecimiento);
            eventoInutilizacion.setPuntoExpedicion(puntoExpedicion);
            eventoInutilizacion.setNumeroInicio(numeroInicio);
            eventoInutilizacion.setNumeroFin(numeroFin);
            eventoInutilizacion.setTipoDE(tipoDE.name());
            eventoInutilizacion.setMotivoInutilizacion(motivo);
            eventoInutilizacion.setEstado(EstadoEvento.PENDIENTE);
            eventoInutilizacion.setActivo(true);
            log.info("   ✅ Registro de evento creado (aún no guardado en BD)");

            log.info("   [PASO 3] Obteniendo configuración SIFEN...");
            SifenConfig config = sifenConfigFactory.buildForTimbrado(timbrado.getId());

            log.info("   [PASO 4] 📤 Enviando evento de inutilización a SIFEN...");
            RespuestaRecepcionEvento respuesta = null;
            try {
                respuesta = sifenConfigFactory.executeWithConfig(config, () -> {
                    return Sifen.recepcionEvento(eventosDE);
                });
                log.info("   ✅ Respuesta recibida de SIFEN");
            } catch (Exception e) {
                log.error("   ❌ ERROR al enviar a SIFEN: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
                throw e;
            }

            log.info("   [PASO 5] Procesando respuesta de SIFEN...");
            procesarRespuestaInutilizacion(eventoInutilizacion, respuesta);

            log.info("   [PASO 6] Guardando evento en BD...");
            try {
                eventoInutilizacionDEService.save(eventoInutilizacion);
                log.info("   ✅ 💾 Evento guardado en BD - ID: {}, Estado: {}", eventoInutilizacion.getId(), eventoInutilizacion.getEstado());
            } catch (Exception e) {
                log.error("   ❌ ERROR CRÍTICO al guardar EventoInutilizacionDE: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error al guardar evento de inutilización en BD", e);
            }

            log.info("   ✅ [FIN] Proceso de inutilización completado exitosamente");
            return respuesta;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("❌ [ERROR CONTROLADO] {}: {}", e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            if (e instanceof SifenException) {
                log.error("❌ [ERROR SIFEN] {}: {}", e.getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
                throw (SifenException) e;
            }
            log.error("❌ [ERROR NO ESPERADO] {}: {}", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error inesperado en inutilización de números", e);
        }
    }

    /**
     * Nomina un receptor para un documento electrónico innominado.
     * Retorna la entidad del evento creado.
     */
    public EventoNominacionDE nominarReceptorDocumento(String cdc, Cliente cliente) throws SifenException {
        nominarReceptor(cdc, cliente);
        return eventoNominacionDEService.findActivosByCdcDocumento(cdc).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró el evento de nominación creado"));
    }

    /**
     * Nomina un receptor para un documento electrónico innominado.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {SifenException.class, IllegalStateException.class, IllegalArgumentException.class})
    public RespuestaRecepcionEvento nominarReceptor(String cdc, Cliente cliente) throws SifenException {
        log.info("👤 [INICIO] Nominando receptor para DE con CDC: {}", cdc);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no puede ser null para nominación");
        }
        log.info("   Cliente: {} (ID: {})", cliente.getNombreCompleto(), cliente.getId());

        DocumentoElectronico de = documentoElectronicoRepository.findByCdc(cdc)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró DE con CDC: " + cdc));

        FacturaLegal factura = de.getFacturaLegal();
        if (factura == null) {
            throw new IllegalArgumentException("DE sin factura asociada");
        }

        if (eventoNominacionDEService.tieneNominacionAprobada(de.getId())) {
            log.warn("⚠️ El DE ya tiene un evento de nominación APROBADO");
            throw new IllegalStateException("El DE ya fue nominado exitosamente. No se puede nominar nuevamente.");
        }

        List<EventoNominacionDE> eventosActivos = eventoNominacionDEService.findActivosByCdcDocumento(cdc);
        if (!eventosActivos.isEmpty()) {
            log.info("   🔄 Se encontraron {} evento(s) previo(s) - realizando reintento automático", eventosActivos.size());
            for (EventoNominacionDE eventoAnterior : eventosActivos) {
                eventoAnterior.setActivo(false);
                eventoNominacionDEService.save(eventoAnterior);
                log.info("      📝 Evento anterior ID {} marcado como inactivo (estado: {})", eventoAnterior.getId(), eventoAnterior.getEstado());
            }
        }

        BigDecimal totalFactura = factura.getTotalFinal() != null ? factura.getTotalFinal() : BigDecimal.ZERO;
        LocalDateTime fechaFirma = LocalDateTime.now();
        LocalDateTime fechaRecepcion = LocalDateTime.now();
        log.info("   Total: {} (obtenido desde factura ID: {})", totalFactura, factura.getId());

        // Determinar configuración del receptor
        Double montoTotal = factura.getTotalFinal() != null ? factura.getTotalFinal().doubleValue() : 0.0;
        ReceptorConfig config = determinarConfiguracionReceptor(cliente, montoTotal);
        TrGeVeNotRec nominacion = new TrGeVeNotRec();
        nominacion.setId(cdc);
        nominacion.setdFecEmi(factura.getFecha());
        nominacion.setdFecRecep(fechaRecepcion);
        nominacion.setdTotalGs(totalFactura);
        nominacion.setdNomRec(config.nombreReceptor);

        String tipoReceptor;
        String documentoReceptor;
        if (config.naturalezaReceptor == TiNatRec.CONTRIBUYENTE) {
            nominacion.setiTipRec(TiNatRec.CONTRIBUYENTE);
            nominacion.setdRucRec(config.numeroDocumento);
            nominacion.setdDVRec(String.valueOf(config.digitoVerificador));
            tipoReceptor = "CONTRIBUYENTE";
            documentoReceptor = config.numeroDocumento + "-" + config.digitoVerificador;
            log.info("   Tipo: Contribuyente - RUC: {}-{}", config.numeroDocumento, config.digitoVerificador);
        } else {
            nominacion.setiTipRec(TiNatRec.NO_CONTRIBUYENTE);
            nominacion.setdTipIDRec(config.tipoDocumentoReceptor);
            nominacion.setdNumID(config.numeroDocumento);
            tipoReceptor = "NO_CONTRIBUYENTE";
            documentoReceptor = config.numeroDocumento;
            log.info("   Tipo: No Contribuyente - Doc: {} ({})", config.numeroDocumento, config.tipoDocumentoReceptor);
        }

        TgGroupTiEvt tipoEvento = new TgGroupTiEvt();
        tipoEvento.setrGeVeNotRec(nominacion);

        TrGesEve gestionEvento = new TrGesEve();
        int numeroRandom = new Random().nextInt(99999999) + 1;
        String eventoId = String.valueOf(numeroRandom);
        gestionEvento.setId(eventoId);
        gestionEvento.setdFecFirma(fechaFirma);
        gestionEvento.setgGroupTiEvt(tipoEvento);

        List<TrGesEve> listaEventos = new ArrayList<>();
        listaEventos.add(gestionEvento);

        EventosDE eventosDE = new EventosDE();
        eventosDE.setrGesEveList(listaEventos);

        EventoNominacionDE eventoNominacion = new EventoNominacionDE();
        eventoNominacion.setDocumentoElectronico(de);
        eventoNominacion.setEventoId(eventoId);
        eventoNominacion.setFechaFirma(fechaFirma);
        eventoNominacion.setCdcDocumento(cdc);
        eventoNominacion.setCliente(cliente);
        eventoNominacion.setNombreReceptor(config.nombreReceptor);
        eventoNominacion.setDocumentoReceptor(documentoReceptor);
        eventoNominacion.setTipoReceptor(tipoReceptor);
        eventoNominacion.setTotalFactura(totalFactura);
        eventoNominacion.setFechaEmision(factura.getFecha());
        eventoNominacion.setFechaRecepcion(fechaRecepcion);
        eventoNominacion.setEstado(EstadoEvento.PENDIENTE);
        eventoNominacion.setActivo(true);

        log.info("   [PASO 1] Obteniendo configuración SIFEN...");
        Timbrado timbrado = factura.getTimbradoDetalle().getTimbrado();
        SifenConfig sifenConfig = sifenConfigFactory.buildForTimbrado(timbrado.getId());

        log.info("   [PASO 2] 📤 Enviando evento de nominación a SIFEN...");
        RespuestaRecepcionEvento respuesta = sifenConfigFactory.executeWithConfig(sifenConfig, () -> {
            return Sifen.recepcionEvento(eventosDE);
        });

        log.info("   [PASO 3] Procesando respuesta de SIFEN...");
        procesarRespuestaNominacion(eventoNominacion, de, factura, cliente, respuesta);

        log.info("   [PASO 4] Guardando evento en BD...");
        eventoNominacionDEService.save(eventoNominacion);
        log.info("   ✅ 💾 Evento guardado en BD - ID: {}, Estado: {}", eventoNominacion.getId(), eventoNominacion.getEstado());
        
        return respuesta;
    }

    // Métodos auxiliares para procesar respuestas

    private void procesarRespuestaCancelacion(EventoCancelacionDE evento, DocumentoElectronico de, RespuestaRecepcionEvento respuesta) {
        String xmlRespuesta = respuesta.getRespuestaBruta();
        log.info("   📄 Tamaño respuesta XML: {} bytes", xmlRespuesta != null ? xmlRespuesta.length() : 0);
        
        String codigoRespuesta = extraerValorXML(xmlRespuesta, "<dCodRes>", "</dCodRes>");
        if (codigoRespuesta == null) {
            codigoRespuesta = extraerValorXML(xmlRespuesta, "<ns2:dCodRes>", "</ns2:dCodRes>");
        }
        String mensajeRespuesta = extraerValorXML(xmlRespuesta, "<dMsgRes>", "</dMsgRes>");
        if (mensajeRespuesta == null) {
            mensajeRespuesta = extraerValorXML(xmlRespuesta, "<ns2:dMsgRes>", "</ns2:dMsgRes>");
        }
        log.info("   📥 Respuesta recibida - Código: {}", codigoRespuesta);
        log.info("   📥 Mensaje: {}", mensajeRespuesta);

        evento.setRespuestaBruta(xmlRespuesta);
        evento.setCodigoRespuesta(codigoRespuesta);
        evento.setMensajeRespuesta(mensajeRespuesta);

        String estadoResultado = extraerValorXML(xmlRespuesta, "<dEstRes>", "</dEstRes>");
        if (estadoResultado == null) {
            estadoResultado = extraerValorXML(xmlRespuesta, "<ns2:dEstRes>", "</ns2:dEstRes>");
        }
        log.info("   📊 Estado del evento en SIFEN: {}", estadoResultado);

        String protocolo = extraerValorXML(xmlRespuesta, "<dProtAut>", "</dProtAut>");
        if (protocolo == null) {
            protocolo = extraerValorXML(xmlRespuesta, "<ns2:dProtAut>", "</ns2:dProtAut>");
        }
        if (protocolo != null && !protocolo.isEmpty() && !"0".equals(protocolo)) {
            evento.setProtocoloAutorizacion(protocolo);
            log.info("   📋 Protocolo: {}", protocolo);
        }

        log.info("   [PASO 8] Determinando estado del evento...");
        String errorMessage = null;

        if ("Aprobado".equalsIgnoreCase(estadoResultado)) {
            log.info("   🎯 Estado: APROBADO");
            evento.setEstado(EstadoEvento.APROBADO);
            evento.setFechaProcesamiento(LocalDateTime.now());
            de.setEstado(EstadoDE.CANCELADO);
            de.setCodigoRespuestaSifen(codigoRespuesta);
            de.setMensajeRespuestaSifen(mensajeRespuesta);
            try {
                documentoElectronicoRepository.save(de);
                log.info("   ✅ Evento APROBADO - DE actualizado a estado CANCELADO");
            } catch (Exception e) {
                log.error("   ❌ ERROR al guardar DE con estado CANCELADO: {}", e.getMessage());
                e.printStackTrace();
            }
            log.info("   📋 Código SIFEN: {} - {}", codigoRespuesta, mensajeRespuesta);
        } else if ("Rechazado".equalsIgnoreCase(estadoResultado)) {
            log.info("   🎯 Estado: RECHAZADO");
            evento.setEstado(EstadoEvento.RECHAZADO);
            evento.setFechaProcesamiento(LocalDateTime.now());
            de.setCodigoRespuestaSifen(codigoRespuesta);
            de.setMensajeRespuestaSifen(mensajeRespuesta);
            try {
                documentoElectronicoRepository.save(de);
            } catch (Exception e) {
                log.error("   ❌ ERROR al guardar DE con respuesta de rechazo: {}", e.getMessage());
                e.printStackTrace();
            }
            log.error("   ❌ Evento RECHAZADO por SIFEN");
            log.error("   📋 Código: {} - {}", codigoRespuesta, mensajeRespuesta);
            log.error("   ℹ️ El DE mantiene su estado actual: {}", de.getEstado());
            errorMessage = "SIFEN rechazó la cancelación: " + mensajeRespuesta;
        } else if (estadoResultado == null || estadoResultado.isEmpty()) {
            log.info("   🎯 Estado resultado es null o vacío - evaluando código de respuesta");
            if ("0300".equals(codigoRespuesta)) {
                evento.setEstado(EstadoEvento.PENDIENTE);
                log.info("   ✅ Evento recibido (código 0300) - pendiente de procesamiento");
            } else if ("0600".equals(codigoRespuesta)) {
                if (protocolo != null && !protocolo.isEmpty() && !"0".equals(protocolo)) {
                    evento.setEstado(EstadoEvento.APROBADO);
                    evento.setFechaProcesamiento(LocalDateTime.now());
                    de.setEstado(EstadoDE.CANCELADO);
                    try {
                        documentoElectronicoRepository.save(de);
                        log.info("   ✅ Evento APROBADO (código 0600 + protocolo) - DE actualizado a CANCELADO");
                    } catch (Exception e) {
                        log.error("   ❌ ERROR al guardar DE con estado CANCELADO (0600): {}", e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    evento.setEstado(EstadoEvento.PENDIENTE);
                    log.info("   ✅ Evento registrado (código 0600) - estado pendiente");
                }
            } else {
                evento.setEstado(EstadoEvento.ERROR_ENVIO);
                log.error("   ❌ Error en envío - Código: {} - {}", codigoRespuesta, mensajeRespuesta);
                errorMessage = "Error al enviar evento: " + codigoRespuesta + " - " + mensajeRespuesta;
            }
        } else {
            log.info("   🎯 Estado desconocido: {}", estadoResultado);
            evento.setEstado(EstadoEvento.PENDIENTE);
            log.warn("   ⚠️ Estado desconocido: {} - marcando como PENDIENTE", estadoResultado);
        }

        if (errorMessage != null) {
            log.warn("   ⚠️ Lanzando excepción por error: {}", errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    private void procesarRespuestaInutilizacion(EventoInutilizacionDE evento, RespuestaRecepcionEvento respuesta) {
        String xmlRespuesta = respuesta.getRespuestaBruta();
        log.info("   📄 Tamaño respuesta XML: {} bytes", xmlRespuesta != null ? xmlRespuesta.length() : 0);
        
        String codigoRespuesta = extraerValorXML(xmlRespuesta, "<dCodRes>", "</dCodRes>");
        if (codigoRespuesta == null) {
            codigoRespuesta = extraerValorXML(xmlRespuesta, "<ns2:dCodRes>", "</ns2:dCodRes>");
        }
        String mensajeRespuesta = extraerValorXML(xmlRespuesta, "<dMsgRes>", "</dMsgRes>");
        if (mensajeRespuesta == null) {
            mensajeRespuesta = extraerValorXML(xmlRespuesta, "<ns2:dMsgRes>", "</ns2:dMsgRes>");
        }
        log.info("   📥 Respuesta recibida - Código: {}", codigoRespuesta);
        log.info("   📥 Mensaje: {}", mensajeRespuesta);

        evento.setRespuestaBruta(xmlRespuesta);
        evento.setCodigoRespuesta(codigoRespuesta);
        evento.setMensajeRespuesta(mensajeRespuesta);

        String estadoResultado = extraerValorXML(xmlRespuesta, "<dEstRes>", "</dEstRes>");
        if (estadoResultado == null) {
            estadoResultado = extraerValorXML(xmlRespuesta, "<ns2:dEstRes>", "</ns2:dEstRes>");
        }
        log.info("   📊 Estado del evento en SIFEN: {}", estadoResultado);

        String protocolo = extraerValorXML(xmlRespuesta, "<dProtAut>", "</dProtAut>");
        if (protocolo == null) {
            protocolo = extraerValorXML(xmlRespuesta, "<ns2:dProtAut>", "</ns2:dProtAut>");
        }
        if (protocolo != null && !protocolo.isEmpty() && !"0".equals(protocolo)) {
            evento.setProtocoloAutorizacion(protocolo);
            log.info("   📋 Protocolo: {}", protocolo);
        }

        log.info("   [PASO 5] Determinando estado del evento...");
        String errorMessage = null;

        if ("Aprobado".equalsIgnoreCase(estadoResultado)) {
            log.info("   🎯 Estado: APROBADO");
            evento.setEstado(EstadoEvento.APROBADO);
            evento.setFechaProcesamiento(LocalDateTime.now());
            log.info("   ✅ Evento APROBADO por SIFEN");
            log.info("   📋 Código SIFEN: {} - {}", codigoRespuesta, mensajeRespuesta);
        } else if ("Rechazado".equalsIgnoreCase(estadoResultado)) {
            log.info("   🎯 Estado: RECHAZADO");
            evento.setEstado(EstadoEvento.RECHAZADO);
            evento.setFechaProcesamiento(LocalDateTime.now());
            log.warn("   ⚠️ Evento RECHAZADO por SIFEN (no es un error, es una respuesta válida)");
            log.warn("   📋 Código: {} - {}", codigoRespuesta, mensajeRespuesta);
        } else if (estadoResultado == null || estadoResultado.isEmpty()) {
            log.info("   🎯 Estado resultado es null o vacío - evaluando código de respuesta");
            if ("0300".equals(codigoRespuesta)) {
                evento.setEstado(EstadoEvento.PENDIENTE);
                log.info("   ✅ Evento recibido (código 0300) - pendiente de procesamiento");
            } else if ("0600".equals(codigoRespuesta)) {
                if (protocolo != null && !protocolo.isEmpty() && !"0".equals(protocolo)) {
                    evento.setEstado(EstadoEvento.APROBADO);
                    evento.setFechaProcesamiento(LocalDateTime.now());
                    log.info("   ✅ Evento APROBADO (código 0600 + protocolo)");
                } else {
                    evento.setEstado(EstadoEvento.PENDIENTE);
                    log.info("   ✅ Evento registrado (código 0600) - estado pendiente");
                }
            } else {
                evento.setEstado(EstadoEvento.ERROR_ENVIO);
                log.error("   ❌ Error en envío - Código: {} - {}", codigoRespuesta, mensajeRespuesta);
                errorMessage = "Error al enviar evento: " + codigoRespuesta + " - " + mensajeRespuesta;
            }
        } else {
            log.info("   🎯 Estado desconocido: {}", estadoResultado);
            evento.setEstado(EstadoEvento.PENDIENTE);
            log.warn("   ⚠️ Estado desconocido: {} - marcando como PENDIENTE", estadoResultado);
        }

        if (errorMessage != null) {
            log.warn("   ⚠️ Lanzando excepción por error: {}", errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    private void procesarRespuestaNominacion(EventoNominacionDE evento, DocumentoElectronico de, FacturaLegal factura, Cliente cliente, RespuestaRecepcionEvento respuesta) {
        String xmlRespuesta = respuesta.getRespuestaBruta();
        String codigoRespuesta = extraerValorXML(xmlRespuesta, "<dCodRes>", "</dCodRes>");
        if (codigoRespuesta == null) {
            codigoRespuesta = extraerValorXML(xmlRespuesta, "<ns2:dCodRes>", "</ns2:dCodRes>");
        }
        String mensajeRespuesta = extraerValorXML(xmlRespuesta, "<dMsgRes>", "</dMsgRes>");
        if (mensajeRespuesta == null) {
            mensajeRespuesta = extraerValorXML(xmlRespuesta, "<ns2:dMsgRes>", "</ns2:dMsgRes>");
        }
        log.info("   📥 Respuesta recibida - Código: {}", codigoRespuesta);
        log.info("   📥 Mensaje: {}", mensajeRespuesta);

        evento.setRespuestaBruta(xmlRespuesta);
        evento.setCodigoRespuesta(codigoRespuesta);
        evento.setMensajeRespuesta(mensajeRespuesta);

        String estadoResultado = extraerValorXML(xmlRespuesta, "<dEstRes>", "</dEstRes>");
        if (estadoResultado == null) {
            estadoResultado = extraerValorXML(xmlRespuesta, "<ns2:dEstRes>", "</ns2:dEstRes>");
        }
        log.info("   📊 Estado del evento en SIFEN: {}", estadoResultado);

        String protocolo = extraerValorXML(xmlRespuesta, "<dProtAut>", "</dProtAut>");
        if (protocolo == null) {
            protocolo = extraerValorXML(xmlRespuesta, "<ns2:dProtAut>", "</ns2:dProtAut>");
        }
        if (protocolo != null && !protocolo.isEmpty() && !"0".equals(protocolo)) {
            evento.setProtocoloAutorizacion(protocolo);
            log.info("   📋 Protocolo: {}", protocolo);
        }

        if ("Aprobado".equalsIgnoreCase(estadoResultado)) {
            evento.setEstado(EstadoEvento.APROBADO);
            evento.setFechaProcesamiento(LocalDateTime.now());
            de.setCodigoRespuestaSifen(codigoRespuesta);
            de.setMensajeRespuestaSifen(mensajeRespuesta);
            documentoElectronicoRepository.save(de);
            
            // Actualizar factura con cliente nominado y sus datos
            factura.setCliente(cliente);
            factura.setNombre(cliente.getNombreCompleto());
            factura.setRuc(cliente.getRuc());
            facturaLegalRepository.save(factura);
            log.info("   ✅ Evento APROBADO - Factura actualizada con cliente nominado");
            log.info("   📋 Código SIFEN: {} - {}", codigoRespuesta, mensajeRespuesta);
            log.info("   👤 Factura ID {} ahora tiene cliente ID {} - Nombre: {} - RUC: {}", 
                    factura.getId(), cliente.getId(), factura.getNombre(), factura.getRuc());
        } else if ("Rechazado".equalsIgnoreCase(estadoResultado)) {
            evento.setEstado(EstadoEvento.RECHAZADO);
            evento.setFechaProcesamiento(LocalDateTime.now());
            de.setCodigoRespuestaSifen(codigoRespuesta);
            de.setMensajeRespuestaSifen(mensajeRespuesta);
            documentoElectronicoRepository.save(de);
            log.error("   ❌ Evento RECHAZADO por SIFEN");
            log.error("   📋 Código: {} - {}", codigoRespuesta, mensajeRespuesta);
            log.error("   ℹ️ La factura mantiene cliente NULL (innominada)");
        } else if (estadoResultado == null || estadoResultado.isEmpty()) {
            if ("0300".equals(codigoRespuesta)) {
                evento.setEstado(EstadoEvento.PENDIENTE);
                log.info("   ✅ Evento recibido (código 0300) - pendiente de procesamiento");
            } else if ("0600".equals(codigoRespuesta)) {
                if (protocolo != null && !protocolo.isEmpty() && !"0".equals(protocolo)) {
                    evento.setEstado(EstadoEvento.APROBADO);
                    evento.setFechaProcesamiento(LocalDateTime.now());
                    
                    // Actualizar factura con cliente nominado y sus datos
                    factura.setCliente(cliente);
                    factura.setNombre(cliente.getNombreCompleto());
                    factura.setRuc(cliente.getRuc());
                    facturaLegalRepository.save(factura);
                    log.info("   ✅ Evento APROBADO (código 0600 + protocolo) - Factura actualizada");
                    log.info("   👤 Factura ID {} ahora tiene cliente ID {} - Nombre: {} - RUC: {}", 
                            factura.getId(), cliente.getId(), factura.getNombre(), factura.getRuc());
                } else {
                    evento.setEstado(EstadoEvento.PENDIENTE);
                    log.info("   ✅ Evento registrado (código 0600) - estado pendiente");
                }
            } else {
                evento.setEstado(EstadoEvento.ERROR_ENVIO);
                log.error("   ❌ Error en envío - Código: {} - {}", codigoRespuesta, mensajeRespuesta);
            }
        } else {
            evento.setEstado(EstadoEvento.PENDIENTE);
            log.warn("   ⚠️ Estado desconocido: {} - marcando como PENDIENTE", estadoResultado);
        }
    }

    /**
     * Determina la configuración del receptor para nominación.
     */
    private ReceptorConfig determinarConfiguracionReceptor(Cliente cliente, Double montoTotal) {
        ReceptorConfig config = new ReceptorConfig();
        
        // Validar monto máximo para innominado
        final double MONTO_MAXIMO_INNOMINADO = 7_000_000.0;
        if (montoTotal != null && montoTotal >= MONTO_MAXIMO_INNOMINADO) {
            throw new IllegalArgumentException(
                "Factura innominada no permitida para montos >= 7.000.000 PYG. Monto: " + montoTotal
            );
        }

        // Obtener nombre
        config.nombreReceptor = cliente.getNombreCompleto();
        if (config.nombreReceptor == null || config.nombreReceptor.trim().isEmpty()) {
            config.nombreReceptor = "Sin Nombre";
        }

        // Determinar si es contribuyente
        boolean esContribuyente = cliente.requiereRuc() && cliente.getRuc() != null && !cliente.getRuc().trim().isEmpty();
        
        if (esContribuyente) {
            config.naturalezaReceptor = TiNatRec.CONTRIBUYENTE;
            // Extraer RUC sin DV
            String ruc = cliente.getRuc();
            if (ruc.contains("-")) {
                String[] partes = ruc.split("-");
                config.numeroDocumento = partes[0].trim();
                config.digitoVerificador = Short.parseShort(partes[1].trim());
            } else {
                config.numeroDocumento = ruc.trim();
                config.digitoVerificador = (short) CalcularVerificadorRuc.getDigitoVerificador(config.numeroDocumento);
            }
        } else {
            config.naturalezaReceptor = TiNatRec.NO_CONTRIBUYENTE;
            // Para no contribuyentes, usar CI u otro documento
            // Por ahora, usar un valor por defecto - esto debería mejorarse
            config.numeroDocumento = "0";
            config.tipoDocumentoReceptor = com.roshka.sifen.core.types.TiTipDocRec.INNOMINADO;
        }
        
        return config;
    }

    /**
     * Clase auxiliar para configuración del receptor.
     */
    private static class ReceptorConfig {
        TiNatRec naturalezaReceptor;
        String nombreReceptor;
        String numeroDocumento;
        Short digitoVerificador;
        com.roshka.sifen.core.types.TiTipDocRec tipoDocumentoReceptor;
    }

    /**
     * Extrae un valor de un XML buscando tags con o sin namespace.
     */
    private String extraerValorXML(String xml, String tagInicio, String tagFin) {
        try {
            if (xml == null || xml.isEmpty()) {
                return null;
            }
            int inicio = xml.indexOf(tagInicio);
            if (inicio == -1) return null;
            inicio += tagInicio.length();
            int fin = xml.indexOf(tagFin, inicio);
            if (fin == -1) return null;
            return xml.substring(inicio, fin).trim();
        } catch (Exception e) {
            log.error("Error al extraer valor XML: {}", e.getMessage());
            return null;
        }
    }
}
