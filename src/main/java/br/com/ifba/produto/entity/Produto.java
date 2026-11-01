package br.com.ifba.produto.entity;

import br.com.ifba.infrastructure.entity.PersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Produto do estoque da loja (PGCD). A tabela e criada pela migration V1
 * (db/migration/V1__cria_tabela_produto.sql); a coluna categoria_id vem da V2
 * e a chave estrangeira da V3.
 */
@Entity
@Table(name = "produto")
public class Produto extends PersistenceEntity {


    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 120)
    private String fornecedor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private int estoque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    public Produto() {
    }

    public Produto(String nome, String fornecedor, BigDecimal preco, int estoque) {
        this.nome = nome;
        this.fornecedor = fornecedor;
        this.preco = preco;
        this.estoque = estoque;
    }


    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}
