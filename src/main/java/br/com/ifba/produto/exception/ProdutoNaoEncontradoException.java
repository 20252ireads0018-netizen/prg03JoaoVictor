package br.com.ifba.produto.exception;

/** Lancada quando se tenta buscar, editar ou excluir um produto que nao existe. */
public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(Long id) {
        super("Produto com ID " + id + " não foi encontrado. Ele pode ter sido excluído; atualize a lista.");
    }
}
