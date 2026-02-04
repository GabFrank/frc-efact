ALTER TABLE financiero.nota_remision 
    ADD COLUMN distrito_partida_id BIGINT,
    ADD COLUMN distrito_destinatario_id BIGINT;

ALTER TABLE financiero.nota_remision
    ADD CONSTRAINT fk_nr_distrito_partida FOREIGN KEY (distrito_partida_id) REFERENCES geografia.distrito(id),
    ADD CONSTRAINT fk_nr_distrito_destinatario FOREIGN KEY (distrito_destinatario_id) REFERENCES geografia.distrito(id);

COMMENT ON COLUMN financiero.nota_remision.distrito_partida_id IS 'ID del distrito de salida (geografia.distrito)';
COMMENT ON COLUMN financiero.nota_remision.distrito_destinatario_id IS 'ID del distrito de llegada (geografia.distrito)';
