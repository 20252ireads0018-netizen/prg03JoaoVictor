package br.com.ifba.produto.dao;

import br.com.ifba.infrastructure.dao.GenericDao;
import br.com.ifba.produto.entity.Categoria;
import java.util.Optional;

/** DAO de Categoria. O CRUD vem de GenericDao; aqui fica a busca pelo nome. */
public class CategoriaDao extends GenericDao<Categoria> implements CategoriaIDao {

    public CategoriaDao() {
        super(Categoria.class);
    }

    @Override
    public Optional<Categoria> findByNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return Optional.empty();
        }
        return consultar(em -> em
                .createQuery("select c from Categoria c where lower(c.nome) = lower(:nome)", Categoria.class)
                .setParameter("nome", nome.trim())
                .getResultStream()
                .findFirst());
    }
}
