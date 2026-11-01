package br.com.ifba.produto.dao;

import br.com.ifba.infrastructure.dao.GenericDao;
import br.com.ifba.produto.entity.Produto;
import java.util.List;

/** DAO de Produto. O CRUD vem de GenericDao; aqui ficam apenas as consultas do dominio. */
public class ProdutoDao extends GenericDao<Produto> implements ProdutoIDao {

    public ProdutoDao() {
        super(Produto.class);
    }

    @Override
    public List<Produto> findAll() {
        return consultar(em -> em
                .createQuery("select p from Produto p order by p.nome", Produto.class)
                .getResultList());
    }

    @Override
    public List<Produto> findByNome(String termo) {
        String filtro = "%" + (termo == null ? "" : termo.trim()) + "%";
        return consultar(em -> em
                .createQuery("select p from Produto p where lower(p.nome) like lower(:termo) order by p.nome",
                        Produto.class)
                .setParameter("termo", filtro)
                .getResultList());
    }

    @Override
    public List<Produto> findComEstoqueAbaixoDe(int limite) {
        return consultar(em -> em
                .createQuery("select p from Produto p where p.estoque < :limite order by p.estoque, p.nome",
                        Produto.class)
                .setParameter("limite", limite)
                .getResultList());
    }
}
