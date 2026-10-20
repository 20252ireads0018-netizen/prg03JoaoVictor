package br.com.ifba.produto.repository;

import br.com.ifba.infrastructure.util.JPAUtil;
import br.com.ifba.produto.entity.Produto;
import br.com.ifba.produto.exception.ProdutoNaoEncontradoException;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Acesso a dados de Produto. Toda operacao abre seu EntityManager, fecha no
 * finally e, nas escritas, faz rollback se a transacao ficou aberta.
 */
public class ProdutoRepository {

    public Produto salvar(Produto produto) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(produto);
            em.getTransaction().commit();
            return produto;
        } catch (RuntimeException e) {
            desfazer(em);
            throw e;
        } finally {
            em.close();
        }
    }

    /** @throws ProdutoNaoEncontradoException se o produto nao existir mais. */
    public Produto atualizar(Produto produto) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (produto.getId() == null || em.find(Produto.class, produto.getId()) == null) {
                throw new ProdutoNaoEncontradoException(produto.getId());
            }
            Produto atualizado = em.merge(produto);
            em.getTransaction().commit();
            return atualizado;
        } catch (RuntimeException e) {
            desfazer(em);
            throw e;
        } finally {
            em.close();
        }
    }

    /** @throws ProdutoNaoEncontradoException se o produto nao existir mais. */
    public void excluir(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Produto produto = em.find(Produto.class, id);
            if (produto == null) {
                throw new ProdutoNaoEncontradoException(id);
            }
            em.remove(produto);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            desfazer(em);
            throw e;
        } finally {
            em.close();
        }
    }

    /** @throws ProdutoNaoEncontradoException em vez de devolver null. */
    public Produto buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Produto produto = em.find(Produto.class, id);
            if (produto == null) {
                throw new ProdutoNaoEncontradoException(id);
            }
            return produto;
        } finally {
            em.close();
        }
    }

    public List<Produto> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select p from Produto p order by p.nome", Produto.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Busca por parte do nome, sem diferenciar maiusculas de minusculas. */
    public List<Produto> buscarPorNome(String termo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "select p from Produto p where lower(p.nome) like lower(:termo) order by p.nome",
                    Produto.class)
                    .setParameter("termo", "%" + termo.trim() + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    private void desfazer(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
