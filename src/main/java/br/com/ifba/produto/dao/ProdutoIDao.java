package br.com.ifba.produto.dao;

import br.com.ifba.infrastructure.dao.GenericIDao;
import br.com.ifba.produto.entity.Produto;
import java.util.List;

/** Acesso a dados de Produto: o CRUD generico mais as buscas proprias do dominio. */
public interface ProdutoIDao extends GenericIDao<Produto> {

    /** Produtos cujo nome contem o termo, sem diferenciar maiusculas de minusculas. */
    List<Produto> findByNome(String termo);

    /** Produtos com estoque abaixo do limite informado (reposicao). */
    List<Produto> findComEstoqueAbaixoDe(int limite);
}
