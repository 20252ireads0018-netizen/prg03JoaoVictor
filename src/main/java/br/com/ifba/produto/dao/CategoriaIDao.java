package br.com.ifba.produto.dao;

import br.com.ifba.infrastructure.dao.GenericIDao;
import br.com.ifba.produto.entity.Categoria;
import java.util.Optional;

/** Acesso a dados de Categoria: o CRUD generico mais a busca pelo nome, que e unico. */
public interface CategoriaIDao extends GenericIDao<Categoria> {

    /** Categoria com exatamente este nome (sem diferenciar maiusculas), se existir. */
    Optional<Categoria> findByNome(String nome);
}
