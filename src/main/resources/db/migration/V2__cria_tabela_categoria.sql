-- V2: evolucao do dominio - produtos passam a ter categoria.
-- Nova tabela categoria e nova coluna produto.categoria_id (ainda sem restricao).
CREATE TABLE categoria (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE produto ADD COLUMN categoria_id BIGINT;
