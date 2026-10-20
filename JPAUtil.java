package br.com.ifba.infrastructure.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Ponto unico de acesso ao JPA: cria o EntityManagerFactory uma unica vez e
 * fornece um EntityManager novo para quem precisar. Quem pede o EntityManager
 * e responsavel por fecha-lo (finally { em.close(); }).
 */
public final class JPAUtil {

    private static final String UNIDADE_PADRAO = "pgcdPU";
    private static final String PROPRIEDADE_UNIDADE = "pgcd.persistence.unit";

    private static EntityManagerFactory factory;

    private JPAUtil() {
    }

    private static synchronized EntityManagerFactory getFactory() {
        if (factory == null) {
            String unidade = System.getProperty(PROPRIEDADE_UNIDADE, UNIDADE_PADRAO);
            factory = Persistence.createEntityManagerFactory(unidade);
        }
        return factory;
    }

    public static EntityManager getEntityManager() {
        return getFactory().createEntityManager();
    }

    /** Encerra a fabrica (chamar ao fechar a aplicacao). */
    public static synchronized void close() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
        factory = null;
    }
}
