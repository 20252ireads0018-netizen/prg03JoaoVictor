package br.com.ifba.infrastructure.dao;

import br.com.ifba.infrastructure.entity.PersistenceEntity;
import java.util.List;

/**
 * Contrato da camada de acesso a dados. Toda entidade do projeto tem um DAO
 * que oferece, no minimo, estas cinco operacoes.
 *
 * @param <Entity> tipo da entidade persistente
 */
public interface GenericIDao<Entity extends PersistenceEntity> {

    /** Persiste uma entidade nova e devolve a instancia com o id preenchido. */
    Entity save(Entity entity);

    /** Grava as alteracoes de uma entidade ja existente. */
    Entity update(Entity entity);

    /** Remove a entidade do banco. */
    void delete(Entity entity);

    /** Busca pelo identificador. */
    Entity findById(Long id);

    /** Lista todas as entidades do tipo. */
    List<Entity> findAll();
}
