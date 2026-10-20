package br.com.ifba.produto.view;

import br.com.ifba.infrastructure.util.JPAUtil;
import br.com.ifba.produto.entity.Produto;
import br.com.ifba.produto.exception.ProdutoNaoEncontradoException;
import br.com.ifba.produto.repository.ProdutoRepository;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/** Tela de CRUD de produtos (cadastrar, listar, buscar, editar, excluir). */
public class TelaProdutos extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(TelaProdutos.class.getName());

    private final ProdutoRepository repository = new ProdutoRepository();

    private final JTextField txtBusca = new JTextField(20);
    private final JTextField txtNome = new JTextField();
    private final JTextField txtFornecedor = new JTextField();
    private final JTextField txtPreco = new JTextField();
    private final JTextField txtEstoque = new JTextField();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Fornecedor", "Preço", "Estoque"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);

    /** ID do produto selecionado na tabela; null quando e um cadastro novo. */
    private Long idSelecionado;

    public TelaProdutos() {
        super("Produtos - PGCD");
        montarTela();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JPAUtil.close();
                dispose();
                System.exit(0);
            }
        });
        carregarTabela(() -> repository.listar());
    }

    private void montarTela() {
        JPanel busca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar por nome");
        JButton btnTodos = new JButton("Listar todos");
        busca.add(new JLabel("Nome:"));
        busca.add(txtBusca);
        busca.add(btnBuscar);
        busca.add(btnTodos);

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Nome:"));
        form.add(txtNome);
        form.add(new JLabel("Fornecedor:"));
        form.add(txtFornecedor);
        form.add(new JLabel("Preço (R$):"));
        form.add(txtPreco);
        form.add(new JLabel("Estoque:"));
        form.add(txtEstoque);

        JButton btnCadastrar = new JButton("Cadastrar");
        JButton btnEditar = new JButton("Salvar edição");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botoes.add(btnCadastrar);
        botoes.add(btnEditar);
        botoes.add(btnExcluir);
        botoes.add(btnLimpar);

        JPanel baixo = new JPanel(new BorderLayout(6, 6));
        baixo.add(form, BorderLayout.CENTER);
        baixo.add(botoes, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(busca, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(tabela), BorderLayout.CENTER);
        getContentPane().add(baixo, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscar());
        btnTodos.addActionListener(e -> {
            txtBusca.setText("");
            carregarTabela(() -> repository.listar());
        });
        btnCadastrar.addActionListener(e -> cadastrar());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limparFormulario());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioComSelecao();
            }
        });

        setSize(720, 520);
        setLocationRelativeTo(null);
    }

    // ---------- operacoes ----------

    private void cadastrar() {
        try {
            Produto produto = lerFormulario();
            repository.salvar(produto);
            JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso!");
            limparFormulario();
            carregarTabela(() -> repository.listar());
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (RuntimeException e) {
            erroDeBanco("cadastrar o produto", e);
        }
    }

    private void editar() {
        if (idSelecionado == null) {
            avisar("Selecione um produto na tabela para editar.");
            return;
        }
        try {
            Produto produto = lerFormulario();
            produto.setId(idSelecionado);
            repository.atualizar(produto);
            JOptionPane.showMessageDialog(this, "Produto atualizado com sucesso!");
            limparFormulario();
            carregarTabela(() -> repository.listar());
        } catch (ProdutoNaoEncontradoException e) {
            avisar(e.getMessage());
            limparFormulario();
            carregarTabela(() -> repository.listar());
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (RuntimeException e) {
            erroDeBanco("atualizar o produto", e);
        }
    }

    private void excluir() {
        if (idSelecionado == null) {
            avisar("Selecione um produto na tabela para excluir.");
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(this,
                "Excluir o produto selecionado?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            repository.excluir(idSelecionado);
            JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!");
        } catch (ProdutoNaoEncontradoException e) {
            avisar(e.getMessage());
        } catch (RuntimeException e) {
            erroDeBanco("excluir o produto", e);
            return;
        }
        limparFormulario();
        carregarTabela(() -> repository.listar());
    }

    private void buscar() {
        String termo = txtBusca.getText();
        if (termo.isBlank()) {
            avisar("Digite parte do nome para buscar.");
            return;
        }
        carregarTabela(() -> repository.buscarPorNome(termo));
    }

    // ---------- apoio ----------

    private void carregarTabela(java.util.function.Supplier<List<Produto>> consulta) {
        try {
            List<Produto> produtos = consulta.get();
            modelo.setRowCount(0);
            for (Produto p : produtos) {
                modelo.addRow(new Object[]{p.getId(), p.getNome(), p.getFornecedor(), p.getPreco(), p.getEstoque()});
            }
        } catch (RuntimeException e) {
            erroDeBanco("carregar os produtos", e);
        }
    }

    private Produto lerFormulario() {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do produto.");
        }
        BigDecimal preco;
        int estoque;
        try {
            preco = new BigDecimal(txtPreco.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preço inválido. Use apenas números, ex.: 19,90.");
        }
        try {
            estoque = Integer.parseInt(txtEstoque.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Estoque inválido. Use um número inteiro.");
        }
        if (preco.signum() < 0 || estoque < 0) {
            throw new IllegalArgumentException("Preço e estoque não podem ser negativos.");
        }
        return new Produto(nome, txtFornecedor.getText().trim(), preco, estoque);
    }

    private void preencherFormularioComSelecao() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return;
        }
        idSelecionado = (Long) modelo.getValueAt(linha, 0);
        txtNome.setText(String.valueOf(modelo.getValueAt(linha, 1)));
        Object fornecedor = modelo.getValueAt(linha, 2);
        txtFornecedor.setText(fornecedor == null ? "" : fornecedor.toString());
        txtPreco.setText(String.valueOf(modelo.getValueAt(linha, 3)));
        txtEstoque.setText(String.valueOf(modelo.getValueAt(linha, 4)));
    }

    private void limparFormulario() {
        idSelecionado = null;
        txtNome.setText("");
        txtFornecedor.setText("");
        txtPreco.setText("");
        txtEstoque.setText("");
        tabela.clearSelection();
    }

    private void avisar(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void erroDeBanco(String acao, RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Falha ao " + acao, e);
        JOptionPane.showMessageDialog(this,
                "Não foi possível " + acao + ". Verifique a conexão com o banco e tente novamente.",
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaProdutos().setVisible(true));
    }
}
