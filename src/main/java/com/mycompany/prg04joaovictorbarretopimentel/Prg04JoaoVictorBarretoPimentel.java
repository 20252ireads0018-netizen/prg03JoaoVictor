package com.mycompany.prg04joaovictorbarretopimentel;

import br.com.ifba.infrastructure.util.JPAUtil;
import br.com.ifba.infrastructure.util.MigracaoBanco;
import br.com.ifba.produto.view.TelaProdutos;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do PGCD. Antes de abrir qualquer tela, aplica as migrations
 * do Flyway; so entao o Hibernate (hbm2ddl.auto=validate) confere o schema.
 */
public class Prg04JoaoVictorBarretoPimentel {

    public static void main(String[] args) {
        try {
            var resultado = MigracaoBanco.migrar();
            System.out.println("Flyway: " + resultado.migrationsExecuted
                    + " migration(s) aplicada(s); versao atual do banco: " + resultado.targetSchemaVersion);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Nao foi possivel preparar o banco de dados:\n" + e.getMessage(),
                    "Erro ao iniciar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(JPAUtil::close));
        SwingUtilities.invokeLater(() -> new TelaProdutos().setVisible(true));
    }
}
