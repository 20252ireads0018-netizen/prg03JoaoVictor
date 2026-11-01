package br.com.ifba.infrastructure.dao;

import br.com.ifba.infrastructure.entity.PersistenceEntity;
import br.com.ifba.infrastructure.exception.EntidadeNaoEncontradaException;
import br.com.ifba.infrastructure.exception.PersistenciaException;
import br.com.ifba.infrastructure.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Implementacao generica do acesso a dados com JPA. O tipo concreto da
 * entidade chega pelo construtor, e e ele que o EntityManager usa em find() e
 * nas consultas JPQL.
 *
 * Regra das operacoes de escrita: try/catch/finally, com rollback() no catch,
 * close() no finally e a excecao de dominio (PersistenciaException) para quem
 * chamou. Nenhuma tela precisa conhecer EntityManager ou transacao.
 *
 * @param <Entity> tipo da entidade persistente
 */
public class GenericDao<Entity extends PersistenceEntity> implements GenericIDao<Entity> {

    private final Class<Entity> tipo;

    public GenericDao(Class<Entity> tipo) {
        this.tipo = tipo;
    }

    protected Class<Entity> getTipo() {
        return tipo;
    }

    protected String getNomeEntidade() {
        return tipo.getSimpleName();
    }

    @Override
    public Entity save(Entity entity) {
        exigirEntidade(entity);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
            return entity;
        } catch (RuntimeException e) {
            rollback(em);
            throw new PersistenciaException("Não foi possível salvar " + getNomeEntidade() + ".", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Entity update(Entity entity) {
        exigirEntidade(entity);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (entity.getId() == null || em.find(tipo, entity.getId()) == null) {
                throw new EntidadeNaoEncontradaException(getNomeEntidade(), entity.getId());
            }
            Entity atualizada = em.merge(entity);
            em.getTransaction().commit();
            return atualizada;
        } catch (PersistenciaException e) {
            rollback(em);
            throw e;
        } catch (RuntimeException e) {
            rollback(em);
            throw new PersistenciaException("Não foi possível atualizar " + getNomeEntidade() + ".", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Entity entity) {
        exigirEntidade(entity);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Entity gerenciada = entity.getId() == null ? null : em.find(tipo, entity.getId());
            if (gerenciada == null) {
                throw new EntidadeNaoEncontradaException(getNomeEntidade(), entity.getId());
            }
            em.remove(gerenciada);
            em.getTransaction().commit();
        } catch (PersistenciaException e) {
            rollback(em);
            throw e;
        } catch (RuntimeException e) {
            rollback(em);
            throw new PersistenciaException("Não foi possível excluir " + getNomeEntidade() + ".", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Entity findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Entity entidade = id == null ? null : em.find(tipo, id);
            if (entidade == null) {
                throw new EntidadeNaoEncontradaException(getNomeEntidade(), id);
            }
            return entidade;
        } catch (PersistenciaException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new PersistenciaException("Não foi possível buscar " + getNomeEntidade() + ".", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Entity> findAll() {
        return consultar(em -> em.createQuery("select e from " + getNomeEntidade() + " e", tipo).getResultList());
    }

    /**
     * Executa uma consulta de leitura. O EntityManager e aberto e fechado aqui,
     * e qualquer falha vira PersistenciaException. Os DAOs especificos usam este
     * metodo para os seus finders (findByNome, findByLogin...).
     */
    protected <R> R consultar(Consulta<R> consulta) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return consulta.executar(em);
        } catch (RuntimeException e) {
            throw new PersistenciaException("Não foi possível consultar " + getNomeEntidade() + ".", e);
        } finally {
            em.close();
        }
    }

    @FunctionalInterface
    protected interface Consulta<R> {
        R executar(EntityManager em);
    }

    private void exigirEntidade(Entity entity) {
        if (entity == null) {
            throw new PersistenciaException("Nenhum(a) " + getNomeEntidade() + " foi informado(a).");
        }
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
