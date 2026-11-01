package br.com.ifba.produto.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ifba.infrastructure.exception.EntidadeNaoEncontradaException;
import br.com.ifba.infrastructure.exception.PersistenciaException;
import br.com.ifba.infrastructure.util.JPAUtil;
import br.com.ifba.infrastructure.util.MigracaoBanco;
import br.com.ifba.produto.entity.Produto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Rede de testes do DAO de Produto: o CRUD herdado de GenericDao, os finders
 * do dominio e, principalmente, a excecao de dominio lancada quando a
 * operacao no banco falha.
 */
class ProdutoDaoTest {

    private final ProdutoIDao dao = new ProdutoDao();

    /** O schema do H2 e criado pelas mesmas migrations da aplicacao (nao pelo Hibernate). */
    @BeforeAll
    static void migrarBanco() {
        MigracaoBanco.migrar();
    }

    @BeforeEach
    void limparTabela() {
        dao.findAll().forEach(dao::delete);
    }

    @AfterAll
    static void fechar() {
        JPAUtil.close();
    }

    private Produto novo(String nome) {
        return new Produto(nome, "Fornecedor X", new BigDecimal("10.00"), 5);
    }

    // ---------- excecoes do DAO (Task 06) ----------

    @Test
    void saveComNomeNuloLancaExcecaoDeDominio() {
        Produto invalido = new Produto(null, "Fornecedor X", new BigDecimal("1.00"), 1);
        PersistenciaException e = assertThrows(PersistenciaException.class, () -> dao.save(invalido));
        assertNotNull(e.getCause(), "a causa original do JPA deve ser preservada");
        assertTrue(dao.findAll().isEmpty(), "o rollback deve desfazer a tentativa");
    }

    @Test
    void saveComPrecoNuloLancaExcecaoDeDominio() {
        Produto invalido = new Produto("Sem preco", "Fornecedor X", null, 1);
        assertThrows(PersistenciaException.class, () -> dao.save(invalido));
    }

    @Test
    void saveNuloLancaExcecaoDeDominio() {
        assertThrows(PersistenciaException.class, () -> dao.save(null));
    }

    @Test
    void findByIdInexistenteLancaExcecao() {
        EntidadeNaoEncontradaException e = assertThrows(EntidadeNaoEncontradaException.class,
                () -> dao.findById(999L));
        assertTrue(e.getMessage().contains("999"));
    }

    @Test
    void updateInexistenteLancaExcecao() {
        Produto fantasma = novo("Fantasma");
        fantasma.setId(999L);
        assertThrows(EntidadeNaoEncontradaException.class, () -> dao.update(fantasma));
    }

    @Test
    void deleteInexistenteLancaExcecao() {
        Produto fantasma = novo("Fantasma");
        fantasma.setId(999L);
        assertThrows(EntidadeNaoEncontradaException.class, () -> dao.delete(fantasma));
    }

    @Test
    void excecaoDeDominioEsconde0Jpa() {
        // A tela so precisa tratar PersistenciaException; EntidadeNaoEncontrada e um caso dela.
        assertTrue(PersistenciaException.class.isAssignableFrom(EntidadeNaoEncontradaException.class));
    }

    // ---------- CRUD ----------

    @Test
    void savePreencheIdEFindByIdEncontra() {
        Produto salvo = dao.save(novo("Mouse"));
        assertNotNull(salvo.getId());
        assertDoesNotThrow(() -> dao.findById(salvo.getId()));
        assertEquals("Mouse", dao.findById(salvo.getId()).getNome());
    }

    @Test
    void updateExistenteGravaAlteracao() {
        Produto salvo = dao.save(novo("Teclado"));
        salvo.setNome("Teclado Gamer");
        assertDoesNotThrow(() -> dao.update(salvo));
        assertEquals("Teclado Gamer", dao.findById(salvo.getId()).getNome());
    }

    @Test
    void deleteExistenteRemove() {
        Produto salvo = dao.save(novo("Monitor"));
        assertDoesNotThrow(() -> dao.delete(salvo));
        assertThrows(EntidadeNaoEncontradaException.class, () -> dao.findById(salvo.getId()));
    }

    @Test
    void findAllListaOrdenadoPorNome() {
        dao.save(novo("Teclado"));
        dao.save(novo("Mouse"));
        List<Produto> todos = dao.findAll();
        assertEquals(2, todos.size());
        assertEquals("Mouse", todos.get(0).getNome());
    }

    // ---------- finders do dominio (Task 04) ----------

    @Test
    void findByNomeUsaJpqlSemDiferenciarCaixa() {
        dao.save(novo("Mouse Sem Fio"));
        dao.save(novo("Teclado"));
        List<Produto> achados = dao.findByNome("MOUSE");
        assertEquals(1, achados.size());
        assertEquals("Mouse Sem Fio", achados.get(0).getNome());
    }

    @Test
    void findComEstoqueAbaixoDeFiltraPeloLimite() {
        Produto acabando = novo("Cabo HDMI");
        acabando.setEstoque(1);
        dao.save(acabando);
        dao.save(novo("Webcam"));
        List<Produto> repor = dao.findComEstoqueAbaixoDe(3);
        assertEquals(1, repor.size());
        assertEquals("Cabo HDMI", repor.get(0).getNome());
    }
}
