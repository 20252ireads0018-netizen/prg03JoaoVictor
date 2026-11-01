package br.com.ifba.infrastructure.exception;

/**
 * Excecao de dominio lancada pela camada de acesso a dados quando uma
 * operacao no banco falha. Esconde das telas os detalhes do JPA/JDBC.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
