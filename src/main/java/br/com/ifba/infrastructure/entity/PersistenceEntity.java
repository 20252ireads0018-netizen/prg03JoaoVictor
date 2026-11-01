package br.com.ifba.infrastructure.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Superclasse de todas as entidades persistentes. Concentra o identificador,
 * para que nenhuma entidade precise declarar o proprio id e para que o DAO
 * generico consiga trabalhar com qualquer entidade do projeto.
 */
@MappedSuperclass
public abstract class PersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
