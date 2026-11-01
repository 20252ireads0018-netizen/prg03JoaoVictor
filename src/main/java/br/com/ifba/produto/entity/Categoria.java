package br.com.ifba.produto.entity;

import br.com.ifba.infrastructure.entity.PersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Categoria de produto (ex.: Perifericos, Monitores). Criada na migration V2;
 * a restricao UNIQUE em nome vem da V3.
 */
@Entity
@Table(name = "categoria")
public class Categoria extends PersistenceEntity {


    @Column(nullable = false, length = 80, unique = true)
    private String nome;

    public Categoria() {
    }

    public Categoria(String nome) {
        this.nome = nome;
    }


    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
