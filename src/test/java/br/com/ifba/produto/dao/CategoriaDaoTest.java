package br.com.ifba.produto.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.ifba.infrastructure.exception.PersistenciaException;
import br.com.ifba.infrastructure.util.JPAUtil;
import br.com.ifba.infrastructure.util.MigracaoBanco;
import br.com.ifba.produto.entity.Categoria;
import br.com.ifba.produto.entity.Produto;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** DAO de Categoria: finder proprio e as restricoes da migration V3 viram excecao de dominio. */
class CategoriaDaoTest {

    private final CategoriaIDao categorias = new CategoriaDao();
    private final ProdutoIDao produtos = new ProdutoDao();

    @BeforeAll
    static void migrarBanco() {
        MigracaoBanco.migrar();
    }

    @BeforeEach
    void limparTabelas() {
        produtos.findAll().forEach(produtos::delete);
        categorias.findAll().forEach(categorias::delete);
    }

    @AfterAll
    static void fechar() {
        JPAUtil.close();
    }

    @Test
    void findByNomeEncontraSemDiferenciarCaixa() {
        categorias.save(new Categoria("Periféricos"));
        assertTrue(categorias.findByNome("periféricos").isPresent());
        assertFalse(categorias.findByNome("Monitores").isPresent());
        assertFalse(categorias.findByNome(null).isPresent());
    }

    @Test
    void nomeDuplicadoVioleAUniqueDaV3ELancaExcecaoDeDominio() {
        categorias.save(new Categoria("Monitores"));
        assertThrows(PersistenciaException.class, () -> categorias.save(new Categoria("Monitores")));
        assertEquals(1, categorias.findAll().size(), "o rollback nao pode deixar meia categoria");
    }

    @Test
    void excluirCategoriaEmUsoVioleAForeignKeyDaV3ELancaExcecaoDeDominio() {
        Categoria monitores = categorias.save(new Categoria("Monitores"));
        Produto produto = new Produto("Monitor 24", "Fornecedor X", new BigDecimal("899.90"), 2);
        produto.setCategoria(monitores);
        produtos.save(produto);

        assertThrows(PersistenciaException.class, () -> categorias.delete(monitores));
        assertEquals(1, categorias.findAll().size());
    }
}
