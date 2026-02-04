ALTER TABLE financiero.nota_remision
    ADD COLUMN departamento_partida_id BIGINT,
    ADD COLUMN ciudad_partida_id BIGINT,
    ADD COLUMN departamento_destinatario_id BIGINT,
    ADD COLUMN ciudad_destinatario_id BIGINT;

ALTER TABLE financiero.nota_remision
    ADD CONSTRAINT fk_nr_departamento_partida FOREIGN KEY (departamento_partida_id) REFERENCES geografia.departamento(id),
    ADD CONSTRAINT fk_nr_ciudad_partida FOREIGN KEY (ciudad_partida_id) REFERENCES geografia.ciudad(id),
    ADD CONSTRAINT fk_nr_departamento_destinatario FOREIGN KEY (departamento_destinatario_id) REFERENCES geografia.departamento(id),
    ADD CONSTRAINT fk_nr_ciudad_destinatario FOREIGN KEY (ciudad_destinatario_id) REFERENCES geografia.ciudad(id);

COMMENT ON COLUMN financiero.nota_remision.ciudad_partida_id IS 'ID de la ciudad de salida (geografia.ciudad)';
COMMENT ON COLUMN financiero.nota_remision.departamento_partida_id IS 'ID del departamento de salida (geografia.departamento)';
COMMENT ON COLUMN financiero.nota_remision.ciudad_destinatario_id IS 'ID de la ciudad de llegada (geografia.ciudad)';
COMMENT ON COLUMN financiero.nota_remision.departamento_destinatario_id IS 'ID del departamento de llegada (geografia.departamento)';
