package com.frcefact.dto;

/**
 * Respuesta para la generación y envío de un documento electrónico.
 */
public class GenerarDeResponse {

    private DocumentoElectronicoDto documento;
    private LoteDeDto lote;

    public GenerarDeResponse() {
    }

    public GenerarDeResponse(DocumentoElectronicoDto documento, LoteDeDto lote) {
        this.documento = documento;
        this.lote = lote;
    }

    public DocumentoElectronicoDto getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoElectronicoDto documento) {
        this.documento = documento;
    }

    public LoteDeDto getLote() {
        return lote;
    }

    public void setLote(LoteDeDto lote) {
        this.lote = lote;
    }

    public static GenerarDeResponse of(DocumentoElectronicoDto documento, LoteDeDto lote) {
        return new GenerarDeResponse(documento, lote);
    }
}

