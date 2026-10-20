package br.com.ifba.produto.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ifba.infrastructure.util.JPAUtil;
import br.com.ifba.produto.entity.Produto;
import br.com.ifba.produto.exception.ProdutoNaoEncontradoException;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProdutoRepositoryTest {

    private final ProdutoRepository repository = new ProdutoRepository();

    @BeforeEach
    void limparTabela() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("delete from Produto").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @AfterAll
    static void fechar() {
        JPAUtil.close();
    }

    private Produto novo(String nome) {
        return new Produto(nome, "Fornecedor X", new BigDecimal("10.00"), 5);
    }

    @Test
    void buscarPorIdInexistenteLancaExcecao() {
        ProdutoNaoEncontradoException e = assertThrows(ProdutoNaoEncontradoException.class,
                () -> repository.buscarPorId(999L));
        assertTrue(e.getMessage().contains("999"));
    }

    @Test
    void buscarPorIdExistenteNaoLancaExcecao() {
        Produto salvo = repository.salvar(novo("Mouse"));
        assertDoesNotThrow(() -> repository.buscarPorId(salvo.getId()));
        assertEquals("Mouse", repository.buscarPorId(salvo.getId()).getNome());
    }

    @Test
    void atualizarInexistenteLancaExcecao() {
        Produto fantasma = novo("Fantasma");
        fantasma.setId(999L);
        assertThrows(ProdutoNaoEncontradoException.class, () -> repository.atualizar(fantasma));
    }

    @Test
    void atualizarExistenteGravaAlteracao() {
        Produto salvo = repository.salvar(novo("Teclado"));
        salvo.setNome("Teclado Gamer");
        assertDoesNotThrow(() -> repository.atualizar(salvo));
        assertEquals("Teclado Gamer", repository.buscarPorId(salvo.getId()).getNome());
    }

    @Test
    void excluirInexistenteLancaExcecao() {
        assertThrows(ProdutoNaoEncontradoException.class, () -> repository.excluir(999L));
    }

    @Test
    void excluirExistenteRemoveSemExcecao() {
        Produto salvo = repository.salvar(novo("Monitor"));
        assertDoesNotThrow(() -> repository.excluir(salvo.getId()));
        assertThrows(ProdutoNaoEncontradoException.class, () -> repository.buscarPorId(salvo.getId()));
    }

    @Test
    void buscarPorNomeUsaJpqlSemDiferenciarCaixa() {
        repository.salvar(novo("Mouse Sem Fio"));
        repository.salvar(novo("Teclado"));
        List<Produto> achados = repository.buscarPorNome("MOUSE");
        assertEquals(1, achados.size());
        assertEquals("Mouse Sem Fio", achados.get(0).getNome());
    }
}
