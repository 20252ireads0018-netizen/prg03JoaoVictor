package br.com.ifba.infrastructure.exception;

/** Lancada quando se busca, atualiza ou exclui um registro que nao existe mais. */
public class EntidadeNaoEncontradaException extends PersistenciaException {

    public EntidadeNaoEncontradaException(String entidade, Long id) {
        super(entidade + " com ID " + id + " não foi encontrado(a). Pode ter sido excluído(a); atualize a lista.");
    }
}
