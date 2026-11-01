-- V1: entidade principal do PGCD (Portal de Gerenciamento de Comercio Digital).
-- SQL escrito a mao, espelhando a entidade Produto.
CREATE TABLE produto (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    nome       VARCHAR(120)  NOT NULL,
    fornecedor VARCHAR(120),
    preco      DECIMAL(12,2) NOT NULL,
    estoque    INT           NOT NULL,
    PRIMARY KEY (id)
);
