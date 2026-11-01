-- V3: restricoes de integridade.
-- Nao podem existir duas categorias com o mesmo nome e todo produto
-- com categoria precisa apontar para uma categoria que existe.
ALTER TABLE categoria
    ADD CONSTRAINT uk_categoria_nome UNIQUE (nome);

ALTER TABLE produto
    ADD CONSTRAINT fk_produto_categoria
    FOREIGN KEY (categoria_id) REFERENCES categoria (id);
