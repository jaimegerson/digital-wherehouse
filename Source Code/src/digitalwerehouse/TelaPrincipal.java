package digitalwerehouse;

import com.sun.org.apache.bcel.internal.generic.Select;
import digitalwerehouse.DB.Conexao;
import digitalwerehouse.DB.Modificar;
import java.awt.Color;
import java.awt.Image;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

public class TelaPrincipal extends javax.swing.JFrame {

    private JPanel[] btnTelaActual = new JPanel[2];

    private int contador = 0;
    private double precoTotalGlobalEntradas = 0;
    private double precoTotalGlobalSaidas = 0;

    public TelaPrincipal() {
        initComponents();

        btnTelaActual = new JPanel[]{btnComponentes, indComponentes};
        pnlInicial.setVisible(true);
        pnlNavegacaoSuperior.setVisible(false);

        pnlCategorias.setVisible(false);
        pnlMovimentos.setVisible(false);
        pnlRelatorio.setVisible(false);
        pnlFuncionarios.setVisible(false);
        pnlListaFuncionarios.setVisible(false);
        pnlFornecedores.setVisible(false);
        pnlComponentes.setVisible(false);
        pnlCopyright.setVisible(false);

        if (!Modificar.getFuncao().equalsIgnoreCase("Gerente")) {
            pnlPaginaInicialFuncionario.setVisible(true);
            btnFuncionarios.setVisible(false);
            btnGerente.setVisible(false);
            pnlPaginaInicialGerente.setVisible(false);
        } else {
            pnlPaginaInicialFuncionario.setVisible(false);
            pnlPaginaInicialGerente.setVisible(true);
        }

        adicionarItems(boxCodigoFornecedorComponente, "select * from GRUPO4.TABELA_FORNECEDORES", "CÓDIGO");
        adicionarItems(boxCategoriaFornecedor, "select * from GRUPO4.TABELA_CATEGORIAS", "NOME");
        adicionarItems(boxCategoriaComponente, "select * from GRUPO4.TABELA_CATEGORIAS", "NOME");
    }

    // Méto para actualizar os dados num painel (refresh)
    public void actualizarTabela(JTable tabela, String sql) {
        try {
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);

            tabela.setModel(DbUtils.resultSetToTableModel(rs));
            Conexao.desconectar(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double precoUnitario(int codigo) {
        DefaultTableModel modelo = (DefaultTableModel) tblComponentes.getModel();
        int linhas = tblComponentes.getRowCount();
        double precoUnitario = 0;
        for (int i = 0; i < linhas; i++) {
            if (modelo.getValueAt(i, 0).toString().equalsIgnoreCase(Integer.toString(codigo))) {
                return Double.parseDouble(modelo.getValueAt(i, 3).toString());
            }
        }
        return precoUnitario;
    }

    private void facturar(Movimentos movimento) {
        if (movimento.tipoMovimento.equalsIgnoreCase("Entrada")) {
            txtFacturaEntrada.setText(
                    txtFacturaEntrada.getText()
                    + "\n       * * * * * DIGITAL WEREHOUSE * * * * *"
                    + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                    + "\n   Data:     " + movimento.data[0] + ",  " + movimento.data[1] + ",  " + movimento.data[2] + "        "
                    + "\n   Tipo do movimennto: ENTRADA "
                    + "\n   N° do movimennto: " + movimento.contador
                    + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                    + "\n      * Código do Stock \t" + movimento.codigoStock
                    + "\n      * Quantidade \t+ " + movimento.quantidade
                    + "\n      * Preço por unidade \t" + precoUnitario(movimento.codigoStock) + "0 MZN"
                    + "\n      * Preço total      \t" + movimento.precoTotal + "0 MZN"
                    + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
            );
            precoTotalGlobalEntradas += movimento.precoTotal;
            return;
        }
        txtFacturaSaida.setText(txtFacturaSaida.getText()
                + "\n       * * * * * DIGITAL WEREHOUSE * * * * *"
                + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                + "\n   Data:     " + movimento.data[0] + ",  " + movimento.data[1] + ",  " + movimento.data[2] + "        "
                + "\n   Tipo do movimennto: SAÍDA "
                + "\n   N° do movimennto: " + movimento.contador
                + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                + "\n      * Código do Stock \t" + movimento.codigoStock
                + "\n      * Quantidade \t- " + movimento.quantidade
                + "\n      * Preço por unidade \t" + precoUnitario(movimento.codigoStock) + "0 MZN"
                + "\n      * Preço total      \t" + movimento.precoTotal + "0 MZN"
                + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n");
        precoTotalGlobalSaidas += movimento.precoTotal;
    }

    public int acumulado(Movimentos movimento) { //Soma o stock existente ao stock disponivel
        DefaultTableModel modelo = (DefaultTableModel) tblComponentes.getModel();
        int linhas = tblComponentes.getRowCount();
        for (int i = 0; i < linhas; i++) {
            if (modelo.getValueAt(i, 0).toString().equalsIgnoreCase(boxCodigoStock.getSelectedItem().toString())) {
                if (movimento.tipoMovimento.equalsIgnoreCase("Entrada")) {
                    return Integer.parseInt(modelo.getValueAt(i, 2).toString()) + Integer.parseInt(txtQuantidade.getText());
                }
                if (Integer.parseInt(txtQuantidade.getText()) > Integer.parseInt(modelo.getValueAt(i, 2).toString())) {
                    JOptionPane.showMessageDialog(this, "Stock insuficiente");
                    return 12345678;
                }
                return Integer.parseInt(modelo.getValueAt(i, 2).toString()) - Integer.parseInt(txtQuantidade.getText());
            }

        }
        return 12345678;
    }

    // Método para pegar dados da base(nume coluna especifica) e adicionar a uma JComboBox
    public static void adicionarItems(JComboBox comboBox, String sql, String coluna) {
        comboBox.removeAllItems();
        Statement stm;
        try {
            Connection conn = Conexao.conectar();
            stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                comboBox.addItem(rs.getString(coluna));
            }
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setColor(JPanel painel, JPanel indicadores) {
        painel.setBackground(new Color(41, 57, 80));
        indicadores.setOpaque(true);
    }

    public void resetColor(JPanel[] painel, JPanel[] indicadores) {
        for (int i = 0; i < painel.length; i++) {
            painel[i].setBackground(new Color(5, 29, 57));
        }

        for (int i = 0; i < painel.length; i++) {
            indicadores[i].setOpaque(false);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        plncabecalho = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        pnlContainer = new javax.swing.JPanel();
        ContainerSuperior = new javax.swing.JLayeredPane();
        pnlNavegacaoSuperior = new javax.swing.JPanel();
        btnMovimentos = new javax.swing.JPanel();
        indMovimentos = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        btnCategorias = new javax.swing.JPanel();
        indCategorias = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        btnGerente = new javax.swing.JPanel();
        indGerente = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        btnFuncionarios = new javax.swing.JPanel();
        indFuncionarios = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        btnForncedores = new javax.swing.JPanel();
        indForncedores = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        btnComponentes = new javax.swing.JPanel();
        indComponentes = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        inicio = new javax.swing.JLabel();
        pnlInicial = new javax.swing.JPanel();
        jPanel28 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        ConteinerRodape = new javax.swing.JPanel();
        btnLogin = new javax.swing.JPanel();
        indLogin = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        btnCopyright = new javax.swing.JPanel();
        indCopyright = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        pnlTelas = new javax.swing.JPanel();
        ContainerTelas = new javax.swing.JLayeredPane();
        pnlCategorias = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        btnEliminarCategoria = new javax.swing.JButton();
        btnAdicionarCategoria = new javax.swing.JButton();
        btnLimparCategoria = new javax.swing.JButton();
        btnActualizarCategoria = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        txtCodigoCategoria = new javax.swing.JTextField();
        txtNomeCategoria = new javax.swing.JTextField();
        txtDescricaoCategoria = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblCategorias = new javax.swing.JTable();
        pnlFornecedores = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblFornecedores = new javax.swing.JTable();
        jPanel22 = new javax.swing.JPanel();
        btnActualizarFornecedor = new javax.swing.JButton();
        btnAdicionarFornecedor = new javax.swing.JButton();
        btnEliminarFornecedor = new javax.swing.JButton();
        btnLimparFornecedor = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        txtNomeFornecedor = new javax.swing.JTextField();
        txtTelefoneFornecedor = new javax.swing.JTextField();
        txtEmailFornecedor = new javax.swing.JTextField();
        jPanel25 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jPanel26 = new javax.swing.JPanel();
        boxCodigoFornecedor = new javax.swing.JComboBox();
        boxCategoriaFornecedor = new javax.swing.JComboBox();
        txtLocalizacaoFornecedor = new javax.swing.JTextField();
        jPanel15 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        pnlComponentes = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblComponentes1 = new javax.swing.JTable();
        btnRelatorioComponente = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        txtStockDisponivelComponente = new javax.swing.JTextField();
        txtPrecoComponente = new javax.swing.JTextField();
        txtDescricaoComponente = new javax.swing.JTextField();
        jPanel17 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        boxCodigoStockComponente = new javax.swing.JComboBox();
        boxCodigoFornecedorComponente = new javax.swing.JComboBox();
        boxCategoriaComponente = new javax.swing.JComboBox();
        jPanel19 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        btnEliminarComponente = new javax.swing.JButton();
        btnActualizarComponente = new javax.swing.JButton();
        btnAdicionarComponente = new javax.swing.JButton();
        btnLimparComponente = new javax.swing.JButton();
        ContainerFuncionario = new javax.swing.JLayeredPane();
        pnlFuncionarios = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        boxFuncao = new javax.swing.JComboBox();
        boxEstadcivilFuncionario = new javax.swing.JComboBox();
        txtNomeFuncionario = new javax.swing.JTextField();
        boxGeneroFuncionario = new javax.swing.JComboBox();
        jPanel13 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        txtSenhaFuncionario = new javax.swing.JTextField();
        txtTelefoneFuncionario = new javax.swing.JTextField();
        txtCodigoFuncionario = new javax.swing.JTextField();
        txtNumeroBIFuncionario = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        AdicionarFoto = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        btnActualizarFuncionario = new javax.swing.JButton();
        btnAdicionarFuncionario = new javax.swing.JButton();
        btnEliminarFuncionario = new javax.swing.JButton();
        btnLimparFuncionario = new javax.swing.JButton();
        jLabel65 = new javax.swing.JLabel();
        pnlListaFuncionarios = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblFuncionarios = new javax.swing.JTable();
        btnEliminarFuncionario1 = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        AdicionarFotoLista = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        pnlPaginaInicialFuncionario = new javax.swing.JPanel();
        btnComponentesFuncionarios = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        btnForncedoresFuncionarios = new javax.swing.JPanel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        btnCategoriasFuncionarios = new javax.swing.JPanel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        btnMovimentosFuncionarios = new javax.swing.JPanel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        pnlPaginaInicialGerente = new javax.swing.JPanel();
        btnComponentesGerente = new javax.swing.JPanel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        btnForncedoresGerente = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        btnFuncionariosGerente = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        btnGerenteGerente = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        btnCategoriasGerente = new javax.swing.JPanel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        btnMovimentosGerente = new javax.swing.JPanel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        pnlMovimentos = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblComponentes = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtFacturaSaida = new javax.swing.JTextArea();
        jLabel13 = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        txtQuantidade = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        boxDia = new javax.swing.JComboBox();
        boxMes = new javax.swing.JComboBox();
        boxAno = new javax.swing.JComboBox();
        boxTipoMovimento = new javax.swing.JComboBox();
        boxCodigoStock = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel30 = new javax.swing.JPanel();
        btnTerminar2 = new javax.swing.JButton();
        btnImprimir2 = new javax.swing.JButton();
        jPanel31 = new javax.swing.JPanel();
        btnrelatorio = new javax.swing.JButton();
        btnFactura = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtFacturaEntrada = new javax.swing.JTextArea();
        pnlRelatorio = new javax.swing.JPanel();
        jScrollPane = new javax.swing.JScrollPane();
        txtRelatorio = new javax.swing.JTextArea();
        btnPrint = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();
        pnlCopyright = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        plncabecalho.setBackground(new java.awt.Color(255, 255, 255));
        plncabecalho.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(plncabecalho, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 0, 970, 100));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlContainer.setBackground(new java.awt.Color(5, 29, 57));
        pnlContainer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlContainerMouseExited(evt);
            }
        });
        pnlContainer.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ContainerSuperior.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlNavegacaoSuperior.setBackground(new java.awt.Color(5, 29, 57));
        pnlNavegacaoSuperior.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlNavegacaoSuperiorMouseExited(evt);
            }
        });
        pnlNavegacaoSuperior.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnMovimentos.setBackground(new java.awt.Color(5, 29, 57));
        btnMovimentos.setForeground(new java.awt.Color(41, 57, 80));
        btnMovimentos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMovimentosMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnMovimentosMousePressed(evt);
            }
        });
        btnMovimentos.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        indMovimentos.setOpaque(false);
        indMovimentos.setPreferredSize(new java.awt.Dimension(5, 52));

        javax.swing.GroupLayout indMovimentosLayout = new javax.swing.GroupLayout(indMovimentos);
        indMovimentos.setLayout(indMovimentosLayout);
        indMovimentosLayout.setHorizontalGroup(
            indMovimentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        indMovimentosLayout.setVerticalGroup(
            indMovimentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnMovimentos.add(indMovimentos, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 51));

        jLabel16.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Movimentos");
        btnMovimentos.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 11, 132, 30));

        pnlNavegacaoSuperior.add(btnMovimentos, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 270, 230, 50));

        btnCategorias.setBackground(new java.awt.Color(5, 29, 57));
        btnCategorias.setForeground(new java.awt.Color(41, 57, 80));
        btnCategorias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCategoriasMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnCategoriasMousePressed(evt);
            }
        });
        btnCategorias.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        indCategorias.setOpaque(false);
        indCategorias.setPreferredSize(new java.awt.Dimension(5, 52));

        javax.swing.GroupLayout indCategoriasLayout = new javax.swing.GroupLayout(indCategorias);
        indCategorias.setLayout(indCategoriasLayout);
        indCategoriasLayout.setHorizontalGroup(
            indCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        indCategoriasLayout.setVerticalGroup(
            indCategoriasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnCategorias.add(indCategorias, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 51));

        jLabel17.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Categorias");
        btnCategorias.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 11, 133, 29));

        pnlNavegacaoSuperior.add(btnCategorias, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 220, 230, -1));

        btnGerente.setBackground(new java.awt.Color(5, 29, 57));
        btnGerente.setForeground(new java.awt.Color(41, 57, 80));
        btnGerente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnGerenteMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnGerenteMousePressed(evt);
            }
        });
        btnGerente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        indGerente.setOpaque(false);
        indGerente.setPreferredSize(new java.awt.Dimension(5, 52));

        javax.swing.GroupLayout indGerenteLayout = new javax.swing.GroupLayout(indGerente);
        indGerente.setLayout(indGerenteLayout);
        indGerenteLayout.setHorizontalGroup(
            indGerenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        indGerenteLayout.setVerticalGroup(
            indGerenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnGerente.add(indGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 51));

        jLabel18.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("Gerente");
        btnGerente.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 11, 125, 29));

        pnlNavegacaoSuperior.add(btnGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 370, 230, -1));

        btnFuncionarios.setBackground(new java.awt.Color(5, 29, 57));
        btnFuncionarios.setForeground(new java.awt.Color(41, 57, 80));
        btnFuncionarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnFuncionariosMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnFuncionariosMousePressed(evt);
            }
        });
        btnFuncionarios.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        indFuncionarios.setOpaque(false);
        indFuncionarios.setPreferredSize(new java.awt.Dimension(5, 52));

        javax.swing.GroupLayout indFuncionariosLayout = new javax.swing.GroupLayout(indFuncionarios);
        indFuncionarios.setLayout(indFuncionariosLayout);
        indFuncionariosLayout.setHorizontalGroup(
            indFuncionariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        indFuncionariosLayout.setVerticalGroup(
            indFuncionariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnFuncionarios.add(indFuncionarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 51));

        jLabel22.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Cadastro");
        btnFuncionarios.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 11, 134, 29));

        pnlNavegacaoSuperior.add(btnFuncionarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 320, 230, -1));

        btnForncedores.setBackground(new java.awt.Color(5, 29, 57));
        btnForncedores.setForeground(new java.awt.Color(41, 57, 80));
        btnForncedores.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnForncedoresMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnForncedoresMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnForncedoresMousePressed(evt);
            }
        });
        btnForncedores.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        indForncedores.setOpaque(false);
        indForncedores.setPreferredSize(new java.awt.Dimension(5, 52));

        javax.swing.GroupLayout indForncedoresLayout = new javax.swing.GroupLayout(indForncedores);
        indForncedores.setLayout(indForncedoresLayout);
        indForncedoresLayout.setHorizontalGroup(
            indForncedoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        indForncedoresLayout.setVerticalGroup(
            indForncedoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnForncedores.add(indForncedores, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 51));

        jLabel23.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Forncedores");
        btnForncedores.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 11, 120, 29));

        pnlNavegacaoSuperior.add(btnForncedores, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 170, 230, -1));

        btnComponentes.setBackground(new java.awt.Color(5, 29, 57));
        btnComponentes.setForeground(new java.awt.Color(5, 29, 57));
        btnComponentes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnComponentesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnComponentesMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnComponentesMousePressed(evt);
            }
        });
        btnComponentes.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        indComponentes.setOpaque(false);
        indComponentes.setPreferredSize(new java.awt.Dimension(5, 52));

        javax.swing.GroupLayout indComponentesLayout = new javax.swing.GroupLayout(indComponentes);
        indComponentes.setLayout(indComponentesLayout);
        indComponentesLayout.setHorizontalGroup(
            indComponentesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        indComponentesLayout.setVerticalGroup(
            indComponentesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnComponentes.add(indComponentes, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 51));

        jLabel24.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Componentes");
        btnComponentes.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 11, 149, 29));

        pnlNavegacaoSuperior.add(btnComponentes, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 120, 230, -1));

        inicio.setFont(new java.awt.Font("Century Gothic", 1, 24)); // NOI18N
        inicio.setForeground(new java.awt.Color(255, 255, 255));
        inicio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/PadinaInicial.png"))); // NOI18N
        inicio.setText("Início");
        inicio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                inicioMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                inicioMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                inicioMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                inicioMousePressed(evt);
            }
        });
        pnlNavegacaoSuperior.add(inicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 160, 80));

        ContainerSuperior.add(pnlNavegacaoSuperior, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 440));

        pnlInicial.setBackground(new java.awt.Color(5, 29, 57));
        pnlInicial.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel28.setBackground(new java.awt.Color(5, 29, 57));

        jLabel41.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(255, 255, 255));
        jLabel41.setText("Digital werehouse");

        jLabel42.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setText("a melhor soluçao ");

        jLabel43.setFont(new java.awt.Font("Century Gothic", 1, 16)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setText("para o seu armazém!");

        jLabel44.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Untitled-3.png"))); // NOI18N

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel41)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel28Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel44, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel43, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel42, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel44)
                .addGap(18, 18, 18)
                .addComponent(jLabel41)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel42)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel43))
        );

        pnlInicial.add(jPanel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 180, -1));

        ContainerSuperior.add(pnlInicial, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 460));

        pnlContainer.add(ContainerSuperior, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 430));

        ConteinerRodape.setBackground(new java.awt.Color(5, 29, 57));
        ConteinerRodape.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ConteinerRodapeMouseExited(evt);
            }
        });
        ConteinerRodape.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnLogin.setBackground(new java.awt.Color(5, 29, 57));
        btnLogin.setForeground(new java.awt.Color(41, 57, 80));
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLoginMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnLoginMousePressed(evt);
            }
        });
        btnLogin.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        indLogin.setOpaque(false);
        indLogin.setPreferredSize(new java.awt.Dimension(5, 52));

        javax.swing.GroupLayout indLoginLayout = new javax.swing.GroupLayout(indLogin);
        indLogin.setLayout(indLoginLayout);
        indLoginLayout.setHorizontalGroup(
            indLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        indLoginLayout.setVerticalGroup(
            indLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        btnLogin.add(indLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 50));

        jLabel25.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Logout.png"))); // NOI18N
        jLabel25.setText("Logout");
        btnLogin.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 0, 131, 50));

        ConteinerRodape.add(btnLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 230, -1));

        btnCopyright.setBackground(new java.awt.Color(5, 29, 57));
        btnCopyright.setForeground(new java.awt.Color(41, 57, 80));
        btnCopyright.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCopyrightMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnCopyrightMousePressed(evt);
            }
        });
        btnCopyright.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        indCopyright.setOpaque(false);
        indCopyright.setPreferredSize(new java.awt.Dimension(5, 70));

        javax.swing.GroupLayout indCopyrightLayout = new javax.swing.GroupLayout(indCopyright);
        indCopyright.setLayout(indCopyrightLayout);
        indCopyrightLayout.setHorizontalGroup(
            indCopyrightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 5, Short.MAX_VALUE)
        );
        indCopyrightLayout.setVerticalGroup(
            indCopyrightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        btnCopyright.add(indCopyright, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 50));

        jLabel26.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("Copyright ©");
        btnCopyright.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 137, 20));

        ConteinerRodape.add(btnCopyright, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 230, -1));

        pnlContainer.add(ConteinerRodape, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, 230, 120));

        jPanel2.add(pnlContainer, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 750));

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pnlTelas.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ContainerTelas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ContainerTelasMouseEntered(evt);
            }
        });
        ContainerTelas.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlCategorias.setPreferredSize(new java.awt.Dimension(970, 610));
        pnlCategorias.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnEliminarCategoria.setBackground(new java.awt.Color(5, 29, 57));
        btnEliminarCategoria.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnEliminarCategoria.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarCategoria.setText("Eliminar");
        btnEliminarCategoria.setBorder(null);
        btnEliminarCategoria.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEliminarCategoriaMouseClicked(evt);
            }
        });
        btnEliminarCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarCategoriaActionPerformed(evt);
            }
        });
        jPanel7.add(btnEliminarCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 250, 50));

        btnAdicionarCategoria.setBackground(new java.awt.Color(5, 29, 57));
        btnAdicionarCategoria.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnAdicionarCategoria.setForeground(new java.awt.Color(255, 255, 255));
        btnAdicionarCategoria.setText("Adicionar");
        btnAdicionarCategoria.setBorder(null);
        btnAdicionarCategoria.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAdicionarCategoriaMouseClicked(evt);
            }
        });
        btnAdicionarCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarCategoriaActionPerformed(evt);
            }
        });
        jPanel7.add(btnAdicionarCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 11, 250, 50));

        btnLimparCategoria.setBackground(new java.awt.Color(5, 29, 57));
        btnLimparCategoria.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnLimparCategoria.setForeground(new java.awt.Color(255, 255, 255));
        btnLimparCategoria.setText("Limpar");
        btnLimparCategoria.setBorder(null);
        btnLimparCategoria.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLimparCategoriaMouseClicked(evt);
            }
        });
        btnLimparCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparCategoriaActionPerformed(evt);
            }
        });
        jPanel7.add(btnLimparCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 220, 250, 50));

        btnActualizarCategoria.setBackground(new java.awt.Color(5, 29, 57));
        btnActualizarCategoria.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnActualizarCategoria.setForeground(new java.awt.Color(255, 255, 255));
        btnActualizarCategoria.setText("Actualizar");
        btnActualizarCategoria.setBorder(null);
        btnActualizarCategoria.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnActualizarCategoriaMouseClicked(evt);
            }
        });
        btnActualizarCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarCategoriaActionPerformed(evt);
            }
        });
        jPanel7.add(btnActualizarCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 250, 50));

        pnlCategorias.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 340, 250, 290));

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtCodigoCategoria.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtCodigoCategoria.setForeground(new java.awt.Color(5, 29, 57));
        txtCodigoCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoCategoriaActionPerformed(evt);
            }
        });
        jPanel3.add(txtCodigoCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 256, 45));

        txtNomeCategoria.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtNomeCategoria.setForeground(new java.awt.Color(5, 29, 57));
        txtNomeCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeCategoriaActionPerformed(evt);
            }
        });
        jPanel3.add(txtNomeCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 120, 256, 50));

        txtDescricaoCategoria.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtDescricaoCategoria.setForeground(new java.awt.Color(5, 29, 57));
        txtDescricaoCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescricaoCategoriaActionPerformed(evt);
            }
        });
        jPanel3.add(txtDescricaoCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 210, 256, 50));

        jLabel7.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(5, 29, 57));
        jLabel7.setText("Descrição ");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 180, -1, 31));

        jLabel8.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(5, 29, 57));
        jLabel8.setText("Nome");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, -1, 33));

        jLabel5.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(5, 29, 57));
        jLabel5.setText("Código");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 30));

        pnlCategorias.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, 260));

        tblCategorias.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tblCategorias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Código", "Nome", "Descricao"
            }
        ));
        tblCategorias.setGridColor(new java.awt.Color(255, 255, 255));
        tblCategorias.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tblCategorias.setRowHeight(25);
        tblCategorias.setSelectionBackground(new java.awt.Color(204, 204, 204));
        tblCategorias.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCategoriasMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tblCategorias);

        pnlCategorias.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 70, 640, 560));

        ContainerTelas.add(pnlCategorias, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlFornecedores.setPreferredSize(new java.awt.Dimension(970, 610));
        pnlFornecedores.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblFornecedores.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tblFornecedores.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Codigo", "Nome", "Categoria", "Localizacao", "Email", "Telefone"
            }
        ));
        tblFornecedores.setGridColor(new java.awt.Color(255, 255, 255));
        tblFornecedores.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tblFornecedores.setRowHeight(25);
        tblFornecedores.setSelectionBackground(new java.awt.Color(240, 240, 240));
        tblFornecedores.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFornecedoresMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tblFornecedores);

        pnlFornecedores.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 340, 890, 280));

        jPanel22.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnActualizarFornecedor.setBackground(new java.awt.Color(5, 29, 57));
        btnActualizarFornecedor.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnActualizarFornecedor.setForeground(new java.awt.Color(255, 255, 255));
        btnActualizarFornecedor.setText("Actualizar");
        btnActualizarFornecedor.setBorder(null);
        btnActualizarFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnActualizarFornecedorMouseClicked(evt);
            }
        });
        btnActualizarFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarFornecedorActionPerformed(evt);
            }
        });
        jPanel22.add(btnActualizarFornecedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 192, 50));

        btnAdicionarFornecedor.setBackground(new java.awt.Color(5, 29, 57));
        btnAdicionarFornecedor.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnAdicionarFornecedor.setForeground(new java.awt.Color(255, 255, 255));
        btnAdicionarFornecedor.setText("Adicionar");
        btnAdicionarFornecedor.setBorder(null);
        btnAdicionarFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAdicionarFornecedorMouseClicked(evt);
            }
        });
        btnAdicionarFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarFornecedorActionPerformed(evt);
            }
        });
        jPanel22.add(btnAdicionarFornecedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 192, 50));

        btnEliminarFornecedor.setBackground(new java.awt.Color(5, 29, 57));
        btnEliminarFornecedor.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnEliminarFornecedor.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarFornecedor.setText("Eliminar");
        btnEliminarFornecedor.setBorder(null);
        btnEliminarFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEliminarFornecedorMouseClicked(evt);
            }
        });
        btnEliminarFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarFornecedorActionPerformed(evt);
            }
        });
        jPanel22.add(btnEliminarFornecedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 210, 192, 50));

        btnLimparFornecedor.setBackground(new java.awt.Color(5, 29, 57));
        btnLimparFornecedor.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnLimparFornecedor.setForeground(new java.awt.Color(255, 255, 255));
        btnLimparFornecedor.setText("Limpar");
        btnLimparFornecedor.setBorder(null);
        btnLimparFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLimparFornecedorMouseClicked(evt);
            }
        });
        btnLimparFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparFornecedorActionPerformed(evt);
            }
        });
        jPanel22.add(btnLimparFornecedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 140, 192, 50));

        pnlFornecedores.add(jPanel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 50, -1, 260));

        jPanel27.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtNomeFornecedor.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtNomeFornecedor.setForeground(new java.awt.Color(5, 29, 57));
        txtNomeFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeFornecedorActionPerformed(evt);
            }
        });

        txtTelefoneFornecedor.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtTelefoneFornecedor.setForeground(new java.awt.Color(5, 29, 57));
        txtTelefoneFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTelefoneFornecedortxtNumeroBIActionPerformed(evt);
            }
        });

        txtEmailFornecedor.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtEmailFornecedor.setForeground(new java.awt.Color(5, 29, 57));
        txtEmailFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmailFornecedorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNomeFornecedor, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                    .addComponent(txtTelefoneFornecedor, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtEmailFornecedor))
                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                .addComponent(txtNomeFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(txtTelefoneFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(txtEmailFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel27.add(jPanel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(88, 12, -1, -1));

        jLabel28.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(5, 29, 57));
        jLabel28.setText("Empresa");

        jLabel29.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(5, 29, 57));
        jLabel29.setText("Telefone");

        jLabel30.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(5, 29, 57));
        jLabel30.setText("E-mail");

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel25Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel30, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel25Layout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel27.add(jPanel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 12, -1, 186));

        boxCodigoFornecedor.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxCodigoFornecedor.setForeground(new java.awt.Color(5, 29, 57));
        boxCodigoFornecedor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "4001", "4002", "4003", "4004", "4005", "4006", "4007", "4008", "4009", "5000", "5001", "5002", " ", " " }));

        boxCategoriaFornecedor.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxCategoriaFornecedor.setForeground(new java.awt.Color(5, 29, 57));
        boxCategoriaFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxCategoriaFornecedorActionPerformed(evt);
            }
        });

        txtLocalizacaoFornecedor.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtLocalizacaoFornecedor.setForeground(new java.awt.Color(5, 29, 57));
        txtLocalizacaoFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLocalizacaoFornecedorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(boxCodigoFornecedor, 0, 201, Short.MAX_VALUE)
            .addComponent(boxCategoriaFornecedor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(txtLocalizacaoFornecedor)
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtLocalizacaoFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(boxCategoriaFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(boxCodigoFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel27.add(jPanel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(412, 11, -1, 187));

        jLabel31.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(5, 29, 57));
        jLabel31.setText("Codigo");

        jLabel32.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(5, 29, 57));
        jLabel32.setText("Categoria");

        jLabel33.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(5, 29, 57));
        jLabel33.setText(" Localização");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel32, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel33, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel27.add(jPanel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 12, -1, 186));

        pnlFornecedores.add(jPanel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 50, 640, 209));

        jLabel34.setFont(new java.awt.Font("Century Gothic", 1, 15)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(5, 29, 57));
        jLabel34.setText("Lista dos fornecedores");
        pnlFornecedores.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 300, 190, 27));

        ContainerTelas.add(pnlFornecedores, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlComponentes.setPreferredSize(new java.awt.Dimension(970, 610));
        pnlComponentes.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblComponentes1.setFont(new java.awt.Font("Century Gothic", 0, 15)); // NOI18N
        tblComponentes1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "CP Stock  ", "CS   Fornecedor", "Stock disponivel", "Preçco unitário", "Descriçao", "Categoria"
            }
        ));
        tblComponentes1.setGridColor(new java.awt.Color(255, 255, 255));
        tblComponentes1.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tblComponentes1.setRowHeight(30);
        tblComponentes1.setSelectionBackground(new java.awt.Color(204, 204, 204));
        tblComponentes1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblComponentes1MouseClicked(evt);
            }
        });
        jScrollPane7.setViewportView(tblComponentes1);

        pnlComponentes.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 310, 880, 276));

        btnRelatorioComponente.setBackground(new java.awt.Color(5, 29, 57));
        btnRelatorioComponente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnRelatorioComponente.setForeground(new java.awt.Color(255, 255, 255));
        btnRelatorioComponente.setText(" Relatório");
        btnRelatorioComponente.setBorder(null);
        btnRelatorioComponente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRelatorioComponenteMouseClicked(evt);
            }
        });
        btnRelatorioComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRelatorioComponenteActionPerformed(evt);
            }
        });
        pnlComponentes.add(btnRelatorioComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 600, 172, 35));

        jPanel16.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtStockDisponivelComponente.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtStockDisponivelComponente.setForeground(new java.awt.Color(5, 29, 57));
        txtStockDisponivelComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStockDisponivelComponenteActionPerformed(evt);
            }
        });
        jPanel16.add(txtStockDisponivelComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 20, 174, 50));

        txtPrecoComponente.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtPrecoComponente.setForeground(new java.awt.Color(5, 29, 57));
        txtPrecoComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrecoComponenteActionPerformed(evt);
            }
        });
        jPanel16.add(txtPrecoComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 190, 174, 50));

        txtDescricaoComponente.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtDescricaoComponente.setForeground(new java.awt.Color(5, 29, 57));
        txtDescricaoComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescricaoComponenteActionPerformed(evt);
            }
        });
        jPanel16.add(txtDescricaoComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 174, 50));

        pnlComponentes.add(jPanel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 30, -1, 240));

        jPanel17.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel35.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(5, 29, 57));
        jLabel35.setText("Stock disponivel");
        jPanel17.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        jLabel36.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(5, 29, 57));
        jLabel36.setText("Descrição");
        jPanel17.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 100, -1, -1));

        jLabel37.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(5, 29, 57));
        jLabel37.setText("Preçco unitário");
        jPanel17.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        pnlComponentes.add(jPanel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 50, -1, 210));

        jPanel18.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        boxCodigoStockComponente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxCodigoStockComponente.setForeground(new java.awt.Color(5, 29, 57));
        boxCodigoStockComponente.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "3000", "3001", "3002", "3003", "3004", "3005", "3006", "3007", "3008", "3009", "4000" }));
        jPanel18.add(boxCodigoStockComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 201, 50));

        boxCodigoFornecedorComponente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxCodigoFornecedorComponente.setForeground(new java.awt.Color(5, 29, 57));
        boxCodigoFornecedorComponente.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0000" }));
        boxCodigoFornecedorComponente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                boxCodigoFornecedorComponenteMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                boxCodigoFornecedorComponenteMousePressed(evt);
            }
        });
        boxCodigoFornecedorComponente.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                boxCodigoFornecedorComponentePopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        boxCodigoFornecedorComponente.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                boxCodigoFornecedorComponenteComponentResized(evt);
            }
        });
        boxCodigoFornecedorComponente.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                boxCodigoFornecedorComponenteItemStateChanged(evt);
            }
        });
        boxCodigoFornecedorComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxCodigoFornecedorComponenteActionPerformed(evt);
            }
        });
        boxCodigoFornecedorComponente.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                boxCodigoFornecedorComponenteHierarchyChanged(evt);
            }
        });
        boxCodigoFornecedorComponente.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                boxCodigoFornecedorComponenteCaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                boxCodigoFornecedorComponenteInputMethodTextChanged(evt);
            }
        });
        boxCodigoFornecedorComponente.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                boxCodigoFornecedorComponentePropertyChange(evt);
            }
        });
        boxCodigoFornecedorComponente.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
                boxCodigoFornecedorComponenteAncestorMoved(evt);
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        boxCodigoFornecedorComponente.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                boxCodigoFornecedorComponenteVetoableChange(evt);
            }
        });
        jPanel18.add(boxCodigoFornecedorComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 201, 50));

        boxCategoriaComponente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxCategoriaComponente.setForeground(new java.awt.Color(5, 29, 57));
        boxCategoriaComponente.setToolTipText("");
        boxCategoriaComponente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                boxCategoriaComponenteMouseClicked(evt);
            }
        });
        boxCategoriaComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxCategoriaComponenteActionPerformed(evt);
            }
        });
        jPanel18.add(boxCategoriaComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 201, 50));

        pnlComponentes.add(jPanel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 60, -1, 210));

        jPanel19.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel38.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(5, 29, 57));
        jLabel38.setText("Código do forncedor");
        jPanel19.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, -1, -1));

        jLabel39.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(5, 29, 57));
        jLabel39.setText("Código do stock");
        jPanel19.add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        jLabel40.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(5, 29, 57));
        jLabel40.setText("Categoria");
        jPanel19.add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 180, -1, -1));

        pnlComponentes.add(jPanel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 50, -1, 210));

        jPanel20.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnEliminarComponente.setBackground(new java.awt.Color(5, 29, 57));
        btnEliminarComponente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnEliminarComponente.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarComponente.setText("Eliminar");
        btnEliminarComponente.setBorder(null);
        btnEliminarComponente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEliminarComponenteMouseClicked(evt);
            }
        });
        btnEliminarComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarComponenteActionPerformed(evt);
            }
        });
        jPanel20.add(btnEliminarComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 182, 160, 43));

        btnActualizarComponente.setBackground(new java.awt.Color(5, 29, 57));
        btnActualizarComponente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnActualizarComponente.setForeground(new java.awt.Color(255, 255, 255));
        btnActualizarComponente.setText("Actualizar");
        btnActualizarComponente.setBorder(null);
        btnActualizarComponente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnActualizarComponenteMouseClicked(evt);
            }
        });
        btnActualizarComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarComponenteActionPerformed(evt);
            }
        });
        jPanel20.add(btnActualizarComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 61, 160, 42));

        btnAdicionarComponente.setBackground(new java.awt.Color(5, 29, 57));
        btnAdicionarComponente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnAdicionarComponente.setForeground(new java.awt.Color(255, 255, 255));
        btnAdicionarComponente.setText("Adicionar");
        btnAdicionarComponente.setBorder(null);
        btnAdicionarComponente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAdicionarComponenteMouseClicked(evt);
            }
        });
        btnAdicionarComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarComponenteActionPerformed(evt);
            }
        });
        jPanel20.add(btnAdicionarComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 160, 43));

        btnLimparComponente.setBackground(new java.awt.Color(5, 29, 57));
        btnLimparComponente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnLimparComponente.setForeground(new java.awt.Color(255, 255, 255));
        btnLimparComponente.setText("Limpar");
        btnLimparComponente.setBorder(null);
        btnLimparComponente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLimparComponenteMouseClicked(evt);
            }
        });
        btnLimparComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparComponenteActionPerformed(evt);
            }
        });
        jPanel20.add(btnLimparComponente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 121, 160, 43));

        pnlComponentes.add(jPanel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 40, -1, -1));

        ContainerTelas.add(pnlComponentes, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        ContainerFuncionario.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlFuncionarios.setPreferredSize(new java.awt.Dimension(970, 610));
        pnlFuncionarios.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        boxFuncao.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxFuncao.setForeground(new java.awt.Color(5, 29, 57));
        boxFuncao.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Gerente", "Vendedor", " " }));
        jPanel9.add(boxFuncao, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 180, 250, 49));

        boxEstadcivilFuncionario.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxEstadcivilFuncionario.setForeground(new java.awt.Color(5, 29, 57));
        boxEstadcivilFuncionario.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Casado", "Solteiro" }));
        jPanel9.add(boxEstadcivilFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 256, 250, 49));

        txtNomeFuncionario.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtNomeFuncionario.setForeground(new java.awt.Color(5, 29, 57));
        txtNomeFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeFuncionarioActionPerformed(evt);
            }
        });
        jPanel9.add(txtNomeFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 11, 250, 53));

        boxGeneroFuncionario.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxGeneroFuncionario.setForeground(new java.awt.Color(5, 29, 57));
        boxGeneroFuncionario.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Masculino", "Femenino", "Indeterminado", " " }));
        jPanel9.add(boxGeneroFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 97, 250, 49));

        jPanel12.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(326, 212, 250, 305));

        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(5, 29, 57));
        jLabel9.setText("Funcao");
        jPanel13.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, -1, 29));

        jLabel14.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(5, 29, 57));
        jLabel14.setText("Estado civil");
        jPanel13.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, -1, 31));

        jLabel15.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(5, 29, 57));
        jLabel15.setText("Genero");
        jPanel13.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 110, -1, 29));

        jLabel10.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(5, 29, 57));
        jLabel10.setText("Nome");
        jPanel13.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 30, -1, -1));

        jPanel12.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 210, -1, 305));

        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtSenhaFuncionario.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtSenhaFuncionario.setForeground(new java.awt.Color(5, 29, 57));
        txtSenhaFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSenhaFuncionarioActionPerformed(evt);
            }
        });
        jPanel10.add(txtSenhaFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 243, 240, 51));

        txtTelefoneFuncionario.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtTelefoneFuncionario.setForeground(new java.awt.Color(5, 29, 57));
        txtTelefoneFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTelefoneFuncionarioActionPerformed(evt);
            }
        });
        jPanel10.add(txtTelefoneFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, 240, 52));

        txtCodigoFuncionario.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtCodigoFuncionario.setForeground(new java.awt.Color(5, 29, 57));
        txtCodigoFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoFuncionarioActionPerformed(evt);
            }
        });
        jPanel10.add(txtCodigoFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 160, 240, 54));

        txtNumeroBIFuncionario.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtNumeroBIFuncionario.setForeground(new java.awt.Color(5, 29, 57));
        txtNumeroBIFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNumeroBIFuncionarioActionPerformed(evt);
            }
        });
        jPanel10.add(txtNumeroBIFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 240, 54));

        jPanel12.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 230, 240, -1));

        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel19.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(5, 29, 57));
        jLabel19.setText("Telefone");
        jPanel11.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, 29));

        jLabel20.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(5, 29, 57));
        jLabel20.setText("Codigo");
        jPanel11.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, -1, 35));

        jLabel21.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(5, 29, 57));
        jLabel21.setText("Senha");
        jPanel11.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 260, -1, 27));

        jLabel27.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(5, 29, 57));
        jLabel27.setText("Numero BI");
        jPanel11.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, 29));

        jPanel12.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 220, 89, 300));

        AdicionarFoto.setBackground(new java.awt.Color(255, 0, 51));
        AdicionarFoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AdicionarFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/LogoDark.png"))); // NOI18N
        AdicionarFoto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AdicionarFotoMousePressed(evt);
            }
        });
        jPanel12.add(AdicionarFoto, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 20, 158, 160));

        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnActualizarFuncionario.setBackground(new java.awt.Color(5, 29, 57));
        btnActualizarFuncionario.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnActualizarFuncionario.setForeground(new java.awt.Color(255, 255, 255));
        btnActualizarFuncionario.setText("Actualizar");
        btnActualizarFuncionario.setBorder(null);
        btnActualizarFuncionario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnActualizarFuncionarioMouseClicked(evt);
            }
        });
        btnActualizarFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarFuncionarioActionPerformed(evt);
            }
        });
        jPanel14.add(btnActualizarFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 92, 182, 55));

        btnAdicionarFuncionario.setBackground(new java.awt.Color(5, 29, 57));
        btnAdicionarFuncionario.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnAdicionarFuncionario.setForeground(new java.awt.Color(255, 255, 255));
        btnAdicionarFuncionario.setText("Adicionar");
        btnAdicionarFuncionario.setBorder(null);
        btnAdicionarFuncionario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAdicionarFuncionarioMouseClicked(evt);
            }
        });
        btnAdicionarFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarFuncionarioActionPerformed(evt);
            }
        });
        jPanel14.add(btnAdicionarFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 11, 182, 51));

        btnEliminarFuncionario.setBackground(new java.awt.Color(5, 29, 57));
        btnEliminarFuncionario.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnEliminarFuncionario.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarFuncionario.setText("Listar");
        btnEliminarFuncionario.setBorder(null);
        btnEliminarFuncionario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEliminarFuncionarioMouseClicked(evt);
            }
        });
        btnEliminarFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarFuncionarioActionPerformed(evt);
            }
        });
        jPanel14.add(btnEliminarFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 255, 182, 51));

        btnLimparFuncionario.setBackground(new java.awt.Color(5, 29, 57));
        btnLimparFuncionario.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnLimparFuncionario.setForeground(new java.awt.Color(255, 255, 255));
        btnLimparFuncionario.setText("Limpar");
        btnLimparFuncionario.setBorder(null);
        btnLimparFuncionario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLimparFuncionarioMouseClicked(evt);
            }
        });
        btnLimparFuncionario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparFuncionarioActionPerformed(evt);
            }
        });
        jPanel14.add(btnLimparFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 177, 182, 51));

        jPanel12.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, -1, -1));

        pnlFuncionarios.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 940, 540));

        jLabel65.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel65.setForeground(new java.awt.Color(5, 29, 57));
        jLabel65.setText("Cadastro ");
        pnlFuncionarios.add(jLabel65, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 40, -1, -1));

        ContainerFuncionario.add(pnlFuncionarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlListaFuncionarios.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblFuncionarios.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tblFuncionarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Codigo", "Nome", "Numero do BI", "Genero", "Senha", "Telefone"
            }
        ));
        tblFuncionarios.setGridColor(new java.awt.Color(255, 255, 255));
        tblFuncionarios.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tblFuncionarios.setRowHeight(25);
        tblFuncionarios.setSelectionBackground(new java.awt.Color(240, 240, 240));
        tblFuncionarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFuncionariosMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblFuncionarios);

        pnlListaFuncionarios.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 260, 900, 340));

        btnEliminarFuncionario1.setBackground(new java.awt.Color(5, 29, 57));
        btnEliminarFuncionario1.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnEliminarFuncionario1.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminarFuncionario1.setText("Eliminar");
        btnEliminarFuncionario1.setBorder(null);
        btnEliminarFuncionario1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEliminarFuncionario1MouseClicked(evt);
            }
        });
        btnEliminarFuncionario1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarFuncionario1ActionPerformed(evt);
            }
        });
        pnlListaFuncionarios.add(btnEliminarFuncionario1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 190, 40));

        btnVoltar.setBackground(new java.awt.Color(5, 29, 57));
        btnVoltar.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnVoltar.setForeground(new java.awt.Color(255, 255, 255));
        btnVoltar.setText("Fechar");
        btnVoltar.setBorder(null);
        btnVoltar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnVoltarMouseClicked(evt);
            }
        });
        btnVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoltarActionPerformed(evt);
            }
        });
        pnlListaFuncionarios.add(btnVoltar, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 150, 190, 40));

        btnEditar.setBackground(new java.awt.Color(5, 29, 57));
        btnEditar.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnEditar.setForeground(new java.awt.Color(255, 255, 255));
        btnEditar.setText("Editar");
        btnEditar.setBorder(null);
        btnEditar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEditarMouseClicked(evt);
            }
        });
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });
        pnlListaFuncionarios.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 90, 190, 40));

        AdicionarFotoLista.setBackground(new java.awt.Color(255, 0, 51));
        AdicionarFotoLista.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AdicionarFotoLista.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/LogoDark.png"))); // NOI18N
        AdicionarFotoLista.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                AdicionarFotoListaMousePressed(evt);
            }
        });
        pnlListaFuncionarios.add(AdicionarFotoLista, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, 158, 160));

        jLabel69.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel69.setForeground(new java.awt.Color(5, 29, 57));
        jLabel69.setText("Lista dos funcionários");
        pnlListaFuncionarios.add(jLabel69, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 220, -1, -1));

        ContainerFuncionario.add(pnlListaFuncionarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        ContainerTelas.add(ContainerFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlPaginaInicialFuncionario.setPreferredSize(new java.awt.Dimension(970, 610));
        pnlPaginaInicialFuncionario.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnComponentesFuncionarios.setBackground(new java.awt.Color(5, 29, 57));
        btnComponentesFuncionarios.setBorder(new javax.swing.border.MatteBorder(null));
        btnComponentesFuncionarios.setForeground(new java.awt.Color(5, 29, 57));
        btnComponentesFuncionarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnComponentesFuncionariosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnComponentesFuncionariosMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnComponentesFuncionariosMousePressed(evt);
            }
        });
        btnComponentesFuncionarios.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                btnComponentesFuncionariosMouseDragged(evt);
            }
        });
        btnComponentesFuncionarios.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnComponentesFuncionariosFocusGained(evt);
            }
        });
        btnComponentesFuncionarios.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel57.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(214, 217, 223));
        jLabel57.setText("Componentes");
        btnComponentesFuncionarios.add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 70, 130, 29));

        jLabel58.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Componentos.png"))); // NOI18N
        btnComponentesFuncionarios.add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 150, 150));

        pnlPaginaInicialFuncionario.add(btnComponentesFuncionarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, 360, -1));

        btnForncedoresFuncionarios.setBackground(new java.awt.Color(5, 29, 57));
        btnForncedoresFuncionarios.setBorder(new javax.swing.border.MatteBorder(null));
        btnForncedoresFuncionarios.setForeground(new java.awt.Color(41, 57, 80));
        btnForncedoresFuncionarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnForncedoresFuncionariosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnForncedoresFuncionariosMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnForncedoresFuncionariosMousePressed(evt);
            }
        });
        btnForncedoresFuncionarios.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel59.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel59.setForeground(new java.awt.Color(214, 217, 223));
        jLabel59.setText("Forncedores");
        btnForncedoresFuncionarios.add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 70, 120, 29));

        jLabel60.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Fornecedoores.png"))); // NOI18N
        btnForncedoresFuncionarios.add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, 150, 140));

        pnlPaginaInicialFuncionario.add(btnForncedoresFuncionarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 100, 360, 170));

        btnCategoriasFuncionarios.setBackground(new java.awt.Color(5, 29, 57));
        btnCategoriasFuncionarios.setBorder(new javax.swing.border.MatteBorder(null));
        btnCategoriasFuncionarios.setForeground(new java.awt.Color(41, 57, 80));
        btnCategoriasFuncionarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCategoriasFuncionariosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCategoriasFuncionariosMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnCategoriasFuncionariosMousePressed(evt);
            }
        });
        btnCategoriasFuncionarios.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel61.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel61.setForeground(new java.awt.Color(214, 217, 223));
        jLabel61.setText("Categorias");
        btnCategoriasFuncionarios.add(jLabel61, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 70, 110, 29));

        jLabel62.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Categorias.png"))); // NOI18N
        btnCategoriasFuncionarios.add(jLabel62, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 160, 150));

        pnlPaginaInicialFuncionario.add(btnCategoriasFuncionarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 380, 360, 170));

        btnMovimentosFuncionarios.setBackground(new java.awt.Color(5, 29, 57));
        btnMovimentosFuncionarios.setBorder(new javax.swing.border.MatteBorder(null));
        btnMovimentosFuncionarios.setForeground(new java.awt.Color(41, 57, 80));
        btnMovimentosFuncionarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMovimentosFuncionariosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMovimentosFuncionariosMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnMovimentosFuncionariosMousePressed(evt);
            }
        });
        btnMovimentosFuncionarios.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel63.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel63.setForeground(new java.awt.Color(214, 217, 223));
        jLabel63.setText("Movimentos");
        btnMovimentosFuncionarios.add(jLabel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 60, 120, 40));

        jLabel64.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Movimentos.png"))); // NOI18N
        btnMovimentosFuncionarios.add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, 150, 130));

        pnlPaginaInicialFuncionario.add(btnMovimentosFuncionarios, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 380, 360, 170));

        ContainerTelas.add(pnlPaginaInicialFuncionario, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlPaginaInicialGerente.setPreferredSize(new java.awt.Dimension(970, 610));
        pnlPaginaInicialGerente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnComponentesGerente.setBackground(new java.awt.Color(5, 29, 57));
        btnComponentesGerente.setBorder(new javax.swing.border.MatteBorder(null));
        btnComponentesGerente.setForeground(new java.awt.Color(5, 29, 57));
        btnComponentesGerente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnComponentesGerenteMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnComponentesGerenteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnComponentesGerenteMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnComponentesGerenteMousePressed(evt);
            }
        });
        btnComponentesGerente.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                btnComponentesGerenteMouseDragged(evt);
            }
        });
        btnComponentesGerente.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                btnComponentesGerenteFocusGained(evt);
            }
        });
        btnComponentesGerente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel45.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(214, 217, 223));
        jLabel45.setText("Componentes");
        btnComponentesGerente.add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, 130, 29));

        jLabel46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Componentos.png"))); // NOI18N
        btnComponentesGerente.add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 150, 150));

        pnlPaginaInicialGerente.add(btnComponentesGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 80, 200, -1));

        btnForncedoresGerente.setBackground(new java.awt.Color(5, 29, 57));
        btnForncedoresGerente.setBorder(new javax.swing.border.MatteBorder(null));
        btnForncedoresGerente.setForeground(new java.awt.Color(41, 57, 80));
        btnForncedoresGerente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnForncedoresGerenteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnForncedoresGerenteMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnForncedoresGerenteMousePressed(evt);
            }
        });
        btnForncedoresGerente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel47.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(214, 217, 223));
        jLabel47.setText("Forncedores");
        btnForncedoresGerente.add(jLabel47, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, 120, 29));

        jLabel48.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Fornecedoores.png"))); // NOI18N
        btnForncedoresGerente.add(jLabel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 150, 140));

        pnlPaginaInicialGerente.add(btnForncedoresGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 80, 200, -1));

        btnFuncionariosGerente.setBackground(new java.awt.Color(5, 29, 57));
        btnFuncionariosGerente.setBorder(new javax.swing.border.MatteBorder(null));
        btnFuncionariosGerente.setForeground(new java.awt.Color(41, 57, 80));
        btnFuncionariosGerente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnFuncionariosGerenteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnFuncionariosGerenteMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnFuncionariosGerenteMousePressed(evt);
            }
        });
        btnFuncionariosGerente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel49.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(214, 217, 223));
        jLabel49.setText("Cadastro");
        btnFuncionariosGerente.add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 100, 29));

        jLabel50.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Funcionarios.png"))); // NOI18N
        btnFuncionariosGerente.add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 160, 150));

        pnlPaginaInicialGerente.add(btnFuncionariosGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 80, 200, -1));

        btnGerenteGerente.setBackground(new java.awt.Color(5, 29, 57));
        btnGerenteGerente.setBorder(new javax.swing.border.MatteBorder(null));
        btnGerenteGerente.setForeground(new java.awt.Color(41, 57, 80));
        btnGerenteGerente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnGerenteGerenteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnGerenteGerenteMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnGerenteGerenteMousePressed(evt);
            }
        });
        btnGerenteGerente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel51.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(214, 217, 223));
        jLabel51.setText("Gerente");
        btnGerenteGerente.add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 10, 90, 29));

        jLabel52.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Gerente.png"))); // NOI18N
        btnGerenteGerente.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, 150, 130));

        pnlPaginaInicialGerente.add(btnGerenteGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 380, 200, 200));

        btnCategoriasGerente.setBackground(new java.awt.Color(5, 29, 57));
        btnCategoriasGerente.setBorder(new javax.swing.border.MatteBorder(null));
        btnCategoriasGerente.setForeground(new java.awt.Color(41, 57, 80));
        btnCategoriasGerente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCategoriasGerenteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCategoriasGerenteMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnCategoriasGerenteMousePressed(evt);
            }
        });
        btnCategoriasGerente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel53.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(214, 217, 223));
        jLabel53.setText("Categorias");
        btnCategoriasGerente.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(43, 14, 133, 29));

        jLabel54.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Categorias.png"))); // NOI18N
        btnCategoriasGerente.add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 160, 150));

        pnlPaginaInicialGerente.add(btnCategoriasGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 380, 200, 200));

        btnMovimentosGerente.setBackground(new java.awt.Color(5, 29, 57));
        btnMovimentosGerente.setBorder(new javax.swing.border.MatteBorder(null));
        btnMovimentosGerente.setForeground(new java.awt.Color(41, 57, 80));
        btnMovimentosGerente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMovimentosGerenteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMovimentosGerenteMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btnMovimentosGerenteMousePressed(evt);
            }
        });
        btnMovimentosGerente.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel55.setFont(new java.awt.Font("Century Gothic", 1, 18)); // NOI18N
        jLabel55.setForeground(new java.awt.Color(214, 217, 223));
        jLabel55.setText("Movimentos");
        btnMovimentosGerente.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 120, 40));

        jLabel56.setIcon(new javax.swing.ImageIcon(getClass().getResource("/digitalwerehouse/PNG/small/Movimentos.png"))); // NOI18N
        btnMovimentosGerente.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, 150, 130));

        pnlPaginaInicialGerente.add(btnMovimentosGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 380, 200, 203));

        ContainerTelas.add(pnlPaginaInicialGerente, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlMovimentos.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblComponentes.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        tblComponentes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CP Stock  ", "CS   Fornecedor", "Stock disponivel", "Preçco unitário", "Descriçao", "Categoria"
            }
        ));
        tblComponentes.setGridColor(new java.awt.Color(255, 255, 255));
        tblComponentes.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tblComponentes.setRowHeight(25);
        tblComponentes.setSelectionBackground(new java.awt.Color(204, 204, 204));
        tblComponentes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblComponentesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblComponentes);

        pnlMovimentos.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 70, 610, 250));

        jLabel1.setFont(new java.awt.Font("Century Gothic", 1, 15)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(5, 29, 57));
        jLabel1.setText("Selecione um componente na tabela");
        pnlMovimentos.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 30, -1, 27));

        jLabel12.setFont(new java.awt.Font("Century Gothic", 1, 15)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(5, 29, 57));
        jLabel12.setText("Saídas");
        pnlMovimentos.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 330, -1, 29));

        txtFacturaSaida.setColumns(20);
        txtFacturaSaida.setFont(new java.awt.Font("Century Gothic", 0, 13)); // NOI18N
        txtFacturaSaida.setRows(5);
        jScrollPane3.setViewportView(txtFacturaSaida);

        pnlMovimentos.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 370, 300, 250));

        jLabel13.setFont(new java.awt.Font("Century Gothic", 1, 15)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(5, 29, 57));
        jLabel13.setText("Entradas");
        pnlMovimentos.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 330, -1, 29));

        jPanel23.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtQuantidade.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtQuantidade.setForeground(new java.awt.Color(5, 29, 57));
        txtQuantidade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantidadetxtNumeroBIActionPerformed(evt);
            }
        });
        jPanel23.add(txtQuantidade, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 210, 280, 50));

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        boxDia.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxDia.setForeground(new java.awt.Color(5, 29, 57));
        boxDia.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        jPanel4.add(boxDia, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        boxMes.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxMes.setForeground(new java.awt.Color(5, 29, 57));
        boxMes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Dezembro", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro" }));
        jPanel4.add(boxMes, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 0, 104, -1));

        boxAno.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxAno.setForeground(new java.awt.Color(5, 29, 57));
        boxAno.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030", "2031", "2032", "2033", "2034", "2035", "2036", "2037", "2038", "2039", "2040" }));
        jPanel4.add(boxAno, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, -1, -1));

        jPanel23.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 300, -1, -1));

        boxTipoMovimento.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxTipoMovimento.setForeground(new java.awt.Color(5, 29, 57));
        boxTipoMovimento.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Entrada", "Saída" }));
        jPanel23.add(boxTipoMovimento, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 280, 50));

        boxCodigoStock.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        boxCodigoStock.setForeground(new java.awt.Color(5, 29, 57));
        jPanel23.add(boxCodigoStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 120, 280, 50));

        jLabel11.setFont(new java.awt.Font("Century Gothic", 1, 15)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(5, 29, 57));
        jLabel11.setText("Movimento");
        jPanel23.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 29));

        jLabel3.setFont(new java.awt.Font("Century Gothic", 1, 15)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(5, 29, 57));
        jLabel3.setText("Código Stock");
        jPanel23.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, -1, -1));

        jLabel6.setFont(new java.awt.Font("Century Gothic", 1, 15)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(5, 29, 57));
        jLabel6.setText("Quantidade");
        jPanel23.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 180, 100, 29));

        jLabel4.setFont(new java.awt.Font("Century Gothic", 1, 15)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(5, 29, 57));
        jLabel4.setText("Data");
        jPanel23.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 270, -1, 29));

        pnlMovimentos.add(jPanel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 280, 350));

        jPanel30.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnTerminar2.setBackground(new java.awt.Color(5, 29, 57));
        btnTerminar2.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnTerminar2.setForeground(new java.awt.Color(255, 255, 255));
        btnTerminar2.setText("Terminar");
        btnTerminar2.setBorder(null);
        btnTerminar2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnTerminar2btnTerminarbtnLimparMouseClicked(evt);
            }
        });
        btnTerminar2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTerminar2btnTerminarbtnLimparActionPerformed(evt);
            }
        });
        jPanel30.add(btnTerminar2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 83, 140, 47));

        btnImprimir2.setBackground(new java.awt.Color(5, 29, 57));
        btnImprimir2.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnImprimir2.setForeground(new java.awt.Color(255, 255, 255));
        btnImprimir2.setText("Imprimir");
        btnImprimir2.setBorder(null);
        btnImprimir2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnImprimir2btnImprimirbtnEliminarMouseClicked(evt);
            }
        });
        btnImprimir2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimir2btnImprimirbtnEliminarActionPerformed(evt);
            }
        });
        jPanel30.add(btnImprimir2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 140, 46));

        pnlMovimentos.add(jPanel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 450, 140, 130));

        jPanel31.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnrelatorio.setBackground(new java.awt.Color(5, 29, 57));
        btnrelatorio.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnrelatorio.setForeground(new java.awt.Color(255, 255, 255));
        btnrelatorio.setText(" Relatório");
        btnrelatorio.setBorder(null);
        btnrelatorio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnrelatoriobtnActualizarMouseClicked(evt);
            }
        });
        btnrelatorio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnrelatoriobtnActualizarActionPerformed(evt);
            }
        });
        jPanel31.add(btnrelatorio, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 82, 140, 48));

        btnFactura.setBackground(new java.awt.Color(5, 29, 57));
        btnFactura.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnFactura.setForeground(new java.awt.Color(255, 255, 255));
        btnFactura.setText("Adicionar");
        btnFactura.setBorder(null);
        btnFactura.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnFacturabtnAdicionarMouseClicked(evt);
            }
        });
        btnFactura.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                btnFacturaStateChanged(evt);
            }
        });
        btnFactura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFacturabtnAdicionarActionPerformed(evt);
            }
        });
        jPanel31.add(btnFactura, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 140, 46));

        pnlMovimentos.add(jPanel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 450, 140, 130));

        txtFacturaEntrada.setColumns(20);
        txtFacturaEntrada.setFont(new java.awt.Font("Century Gothic", 0, 13)); // NOI18N
        txtFacturaEntrada.setRows(5);
        jScrollPane2.setViewportView(txtFacturaEntrada);

        pnlMovimentos.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 370, 300, 250));

        ContainerTelas.add(pnlMovimentos, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlRelatorio.setPreferredSize(new java.awt.Dimension(970, 610));
        pnlRelatorio.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtRelatorio.setColumns(20);
        txtRelatorio.setFont(new java.awt.Font("Century Gothic", 0, 14)); // NOI18N
        txtRelatorio.setRows(5);
        jScrollPane.setViewportView(txtRelatorio);

        pnlRelatorio.add(jScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, 821, 502));

        btnPrint.setBackground(new java.awt.Color(5, 29, 57));
        btnPrint.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnPrint.setForeground(new java.awt.Color(255, 255, 255));
        btnPrint.setText("Imprimir");
        btnPrint.setBorder(null);
        btnPrint.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPrintMouseClicked(evt);
            }
        });
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });
        pnlRelatorio.add(btnPrint, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 570, 181, 50));

        btnFechar.setBackground(new java.awt.Color(5, 29, 57));
        btnFechar.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnFechar.setForeground(new java.awt.Color(255, 255, 255));
        btnFechar.setText("Fechar");
        btnFechar.setBorder(null);
        btnFechar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnFecharMouseClicked(evt);
            }
        });
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharActionPerformed(evt);
            }
        });
        pnlRelatorio.add(btnFechar, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 570, 200, 50));

        ContainerTelas.add(pnlRelatorio, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlCopyright.setPreferredSize(new java.awt.Dimension(970, 610));
        pnlCopyright.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlCopyrightMouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pnlCopyrightMousePressed(evt);
            }
        });
        pnlCopyright.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        ContainerTelas.add(pnlCopyright, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        pnlTelas.add(ContainerTelas, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 970, 650));

        getContentPane().add(pnlTelas, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 100, 970, 650));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    //*****************************************************************************************************************************************
    public boolean isMovimentoPeenchido() {
        if (txtQuantidade.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informação em falta");
            return false;
        }
        return true;
    }


    private void btnComponentesMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesMousePressed
        setColor(btnComponentes, indComponentes);
        resetColor(new JPanel[]{btnForncedores, btnFuncionarios, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});

        actualizarTabela(tblComponentes1, "select * from GRUPO4.TABELA_COMPONENTES");
        pnlCategorias.setVisible(false);
        pnlMovimentos.setVisible(false);
        pnlRelatorio.setVisible(false);
        pnlFuncionarios.setVisible(false);
        pnlListaFuncionarios.setVisible(false);
        pnlFornecedores.setVisible(false);
        pnlComponentes.setVisible(true);
        pnlCopyright.setVisible(false);
        btnTelaActual = new JPanel[]{btnComponentes, indComponentes};
    }//GEN-LAST:event_btnComponentesMousePressed

    private void btnForncedoresMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresMousePressed
        setColor(btnForncedores, indForncedores);
        resetColor(new JPanel[]{btnComponentes, btnFuncionarios, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indComponentes, indFuncionarios, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});

        actualizarTabela(tblFornecedores, "select * from GRUPO4.TABELA_FORNECEDORES");

        if (!Modificar.getFuncao().equalsIgnoreCase("Gerente")) {
            btnFuncionarios.setVisible(false);
            btnGerente.setVisible(false);
        }

        pnlCategorias.setVisible(false);
        pnlMovimentos.setVisible(false);
        pnlRelatorio.setVisible(false);
        pnlFuncionarios.setVisible(false);
        pnlListaFuncionarios.setVisible(false);
        pnlFornecedores.setVisible(true);
        pnlComponentes.setVisible(false);
        pnlCopyright.setVisible(false);
        btnTelaActual = new JPanel[]{btnForncedores, indForncedores};
    }//GEN-LAST:event_btnForncedoresMousePressed

    private void btnFuncionariosMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFuncionariosMousePressed
        setColor(btnFuncionarios, indFuncionarios);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indComponentes, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});

        actualizarTabela(tblFuncionarios, "select * from GRUPO4.TABELA_FUNCIONARIOS");

        if (!Modificar.getFuncao().equalsIgnoreCase("Gerente")) {
            btnFuncionarios.setVisible(false);
            btnGerente.setVisible(false);
        }

        pnlCategorias.setVisible(false);
        pnlMovimentos.setVisible(false);
        pnlRelatorio.setVisible(false);
        pnlFuncionarios.setVisible(true);
        pnlListaFuncionarios.setVisible(false);
        pnlFornecedores.setVisible(false);
        pnlComponentes.setVisible(false);
        pnlCopyright.setVisible(false);
        btnTelaActual = new JPanel[]{btnFuncionarios, indFuncionarios};
    }//GEN-LAST:event_btnFuncionariosMousePressed

    private void btnGerenteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGerenteMousePressed
        setColor(btnGerente, indGerente);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnMovimentos, btnCategorias, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indMovimentos, indCategorias, indLogin, indCopyright});
        new Gerente().setVisible(true);
        dispose();
    }//GEN-LAST:event_btnGerenteMousePressed

    private void btnCategoriasMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategoriasMousePressed
        setColor(btnCategorias, indCategorias);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnMovimentos, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indMovimentos, indGerente, indLogin, indCopyright});

        actualizarTabela(tblCategorias, "select * from GRUPO4.TABELA_CATEGORIAS");

        if (!Modificar.getFuncao().equalsIgnoreCase("Gerente")) {
            btnFuncionarios.setVisible(false);
            btnGerente.setVisible(false);
        }

        pnlCategorias.setVisible(true);
        pnlMovimentos.setVisible(false);
        pnlRelatorio.setVisible(false);
        pnlFuncionarios.setVisible(false);
        pnlListaFuncionarios.setVisible(false);
        pnlFornecedores.setVisible(false);
        pnlComponentes.setVisible(false);
        pnlCopyright.setVisible(false);
        btnTelaActual = new JPanel[]{btnCategorias, indCategorias};
    }//GEN-LAST:event_btnCategoriasMousePressed

    private void btnMovimentosMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovimentosMousePressed
        setColor(btnMovimentos, indMovimentos);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indLogin, indCopyright});
        actualizarTabela(tblComponentes, "select * from GRUPO4.TABELA_COMPONENTES");
        adicionarItems(boxCodigoStock, "select * from GRUPO4.TABELA_COMPONENTES", "CÓDIGO");

        if (!Modificar.getFuncao().equalsIgnoreCase("Gerente")) {
            btnFuncionarios.setVisible(false);
            btnGerente.setVisible(false);
        }

        pnlCategorias.setVisible(false);
        pnlMovimentos.setVisible(true);
        pnlRelatorio.setVisible(false);
        pnlFuncionarios.setVisible(false);
        pnlListaFuncionarios.setVisible(false);
        pnlFornecedores.setVisible(false);
        pnlComponentes.setVisible(false);
        pnlCopyright.setVisible(false);
        btnTelaActual = new JPanel[]{btnMovimentos, indMovimentos};
    }//GEN-LAST:event_btnMovimentosMousePressed

    private void inicioMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inicioMouseEntered
        inicio.setForeground(new Color(41, 57, 80));
    }//GEN-LAST:event_inicioMouseEntered

    private void inicioMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inicioMouseExited
        inicio.setForeground(Color.WHITE);
    }//GEN-LAST:event_inicioMouseExited

    private void inicioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inicioMouseClicked

    }//GEN-LAST:event_inicioMouseClicked

    private void btnCopyrightMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCopyrightMousePressed
        setColor(btnCopyright, indCopyright);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnLogin, btnMovimentos},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indLogin, indMovimentos});

        pnlCategorias.setVisible(false);
        pnlMovimentos.setVisible(false);
        pnlRelatorio.setVisible(false);
        pnlFuncionarios.setVisible(false);
        pnlListaFuncionarios.setVisible(false);
        pnlFornecedores.setVisible(false);
        pnlComponentes.setVisible(false);
        pnlCopyright.setVisible(true);
        btnTelaActual = new JPanel[]{btnCopyright, indCopyright};
    }//GEN-LAST:event_btnCopyrightMousePressed

//*********************************************************************************************************************************************
    public void limparCaixasDeTextoCategoria() {
        txtCodigoCategoria.setText("");
        txtNomeCategoria.setText("");
        txtDescricaoCategoria.setText("");
    }

    public boolean isExisteCategoria() { // Verifica se já existe um funcionrio com mesmo código
        DefaultTableModel modelo = (DefaultTableModel) tblCategorias.getModel();
        int linhas = tblCategorias.getRowCount();
        for (int i = 0; i < linhas; i++) {
            if (modelo.getValueAt(i, 0).toString().equalsIgnoreCase(txtCodigoCategoria.getText())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPeenchidoCategoria() {
        if (((txtCodigoCategoria.getText().isEmpty() || txtNomeCategoria.getText().isEmpty()) || txtDescricaoCategoria.getText().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Informação em falta");
            return false;
        }
        return true;
    }

    //****************************************************************************************************************************************
    public boolean isFuncionarioPeenchido() {
        if ((((txtCodigoFuncionario.getText().isEmpty() || txtNomeFuncionario.getText().isEmpty()) || txtNumeroBIFuncionario.getText().isEmpty()) || txtSenhaFuncionario.getText().isEmpty() || txtTelefoneFuncionario.getText().isEmpty()) && fotoPessoal != null) {
            JOptionPane.showMessageDialog(this, "Informação em falta");
            return false;
        }
        return true;
    }

    public void limparCaixasDeTextoFuncionario() {
        txtCodigoFuncionario.setText("");
        txtNomeFuncionario.setText("");
        txtNumeroBIFuncionario.setText("");
        txtSenhaFuncionario.setText("");
        txtTelefoneFuncionario.setText("");
        ImageIcon icon = new ImageIcon("C:\\Users\\James_Xfile\\Documents\\NetBeansProjects\\DigitalWerehouse\\src\\digitalwerehouse\\PNG\\small\\LogoDark.png");
        AdicionarFotoLista.setIcon(icon);
    }

    public boolean isFuncionarioExiste() { // Verifica se já existe um funcionrio com mesmo código
        DefaultTableModel modelo = (DefaultTableModel) tblFuncionarios.getModel();
        int linhas = tblFuncionarios.getRowCount();
        for (int i = 0; i < linhas; i++) {
            if (modelo.getValueAt(i, 0).toString().equalsIgnoreCase(txtCodigoFuncionario.getText())) {
                return true;
            }
        }
        return false;
    }

    //*****************************************************************************************************************************************
    public boolean isFornecedorPeenchido() {
        if ((txtNomeFornecedor.getText().isEmpty() || txtTelefoneFornecedor.getText().isEmpty()) || txtEmailFornecedor.getText().isEmpty() || txtLocalizacaoFornecedor.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informação em falta");
            return false;
        }
        return true;
    }

    public boolean isFornecedorExiste() {
        DefaultTableModel modelo = (DefaultTableModel) tblFornecedores.getModel();
        int linhas = tblFornecedores.getRowCount();
        for (int i = 0; i < linhas; i++) {
            if (modelo.getValueAt(i, 0).toString().equalsIgnoreCase(boxCodigoFornecedor.getSelectedItem().toString())) {
                return true;
            }
        }
        return false;
    }

    public void limparCaixasDeTextoFornecedor() {
        txtNomeFornecedor.setText("");
        txtTelefoneFornecedor.setText("");
        txtEmailFornecedor.setText("");
        txtLocalizacaoFornecedor.setText("");
    }

    //****************************************************************************************************************************************
    public void limparCaixasDeTextoComponentes() {
        txtStockDisponivelComponente.setText("");
        txtDescricaoComponente.setText("");
        txtPrecoComponente.setText("");
    }

    // Verifica setodos os campos estão preenchidos
    public boolean isComponentePeenchido() {
        if (((txtStockDisponivelComponente.getText().isEmpty()
                || txtDescricaoComponente.getText().isEmpty())
                || txtPrecoComponente.getText().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Informação em falta");
            return false;
        }
        return true;
    }

    public int acumulado() { //Soma o stock existente ao stock disponivel
        DefaultTableModel modelo = (DefaultTableModel) tblComponentes1.getModel();
        int linhas = tblComponentes1.getRowCount();
        int acumulado = 0;
        for (int i = 0; i < linhas; i++) {
            if (modelo.getValueAt(i, 0).toString().equalsIgnoreCase(boxCodigoStockComponente.getSelectedItem().toString())) {
                acumulado = Integer.parseInt(modelo.getValueAt(i, 2).toString()) + Integer.parseInt(txtStockDisponivelComponente.getText());
            }
        }
        return acumulado;
    }

    // Método que verifica se existe um registro na base com mesmo código
    public boolean isComponenteExiste() {
        DefaultTableModel modelo = (DefaultTableModel) tblComponentes1.getModel();
        int linhas = tblComponentes1.getRowCount();
        for (int i = 0; i < linhas; i++) {
            if (modelo.getValueAt(i, 0).toString().equalsIgnoreCase(boxCodigoStockComponente.getSelectedItem().toString())) {
                return true;
            }
        }
        return false;
    }

//*********************************************************************************************************************************************
    public void setColor(JPanel painel) {
        painel.setBackground(new Color(41, 57, 80));

    }

    public void mouseExit(JPanel painel) {
        painel.setBackground(new Color(5, 29, 57));

    }

    public void resetColor(JPanel[] painel) {
        for (int i = 0; i < painel.length; i++) {
            painel[i].setBackground(new Color(5, 29, 57));
        }

    }

    private void setPainel(JPanel painel) {
        JPanel[] paineis = new JPanel[]{pnlPaginaInicialFuncionario, pnlPaginaInicialGerente, pnlInicial, pnlNavegacaoSuperior,
            pnlCategorias, pnlMovimentos, pnlRelatorio, pnlFuncionarios, pnlFornecedores, pnlComponentes, pnlCopyright};
        for (JPanel aux : paineis) {
            if (aux == painel || aux == pnlNavegacaoSuperior) {
                aux.setVisible(true);
                continue;
            }
            aux.setVisible(false);
        }
    }

    private void inicioMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inicioMousePressed
        pnlInicial.setVisible(true);
        pnlNavegacaoSuperior.setVisible(false);

        pnlCategorias.setVisible(false);
        pnlMovimentos.setVisible(false);
        pnlRelatorio.setVisible(false);
        pnlFuncionarios.setVisible(false);
        pnlFornecedores.setVisible(false);
        pnlComponentes.setVisible(false);
        pnlCopyright.setVisible(false);
        pnlListaFuncionarios.setVisible(false);

        if (!Modificar.getFuncao().equalsIgnoreCase("Gerente")) {
            pnlPaginaInicialFuncionario.setVisible(true);
            pnlPaginaInicialGerente.setVisible(false);
        } else {
            pnlPaginaInicialFuncionario.setVisible(false);
            pnlPaginaInicialGerente.setVisible(true);
        }
    }//GEN-LAST:event_inicioMousePressed

    private void btnComponentesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesMouseEntered
        setColor(btnComponentes, indComponentes);
        resetColor(new JPanel[]{btnForncedores, btnFuncionarios, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});

    }//GEN-LAST:event_btnComponentesMouseEntered

    private void btnComponentesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesMouseExited

    }//GEN-LAST:event_btnComponentesMouseExited

    private void btnForncedoresMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresMouseEntered
        setColor(btnForncedores, indForncedores);
        resetColor(new JPanel[]{btnComponentes, btnFuncionarios, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indComponentes, indFuncionarios, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});

    }//GEN-LAST:event_btnForncedoresMouseEntered

    private void btnForncedoresMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresMouseExited

    }//GEN-LAST:event_btnForncedoresMouseExited

    private void btnCategoriasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategoriasMouseEntered
        setColor(btnCategorias, indCategorias);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnMovimentos, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indMovimentos, indGerente, indLogin, indCopyright});

    }//GEN-LAST:event_btnCategoriasMouseEntered

    private void btnMovimentosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovimentosMouseEntered
        setColor(btnMovimentos, indMovimentos);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indLogin, indCopyright});

    }//GEN-LAST:event_btnMovimentosMouseEntered

    private void btnFuncionariosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFuncionariosMouseEntered
        setColor(btnFuncionarios, indFuncionarios);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indComponentes, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});

    }//GEN-LAST:event_btnFuncionariosMouseEntered

    private void btnGerenteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGerenteMouseEntered
        setColor(btnGerente, indGerente);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnMovimentos, btnCategorias, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indMovimentos, indCategorias, indLogin, indCopyright});

    }//GEN-LAST:event_btnGerenteMouseEntered

    private void btnCopyrightMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCopyrightMouseEntered
        setColor(btnCopyright, indCopyright);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnLogin, btnMovimentos},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indLogin, indMovimentos});
    }//GEN-LAST:event_btnCopyrightMouseEntered

    private void pnlNavegacaoSuperiorMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlNavegacaoSuperiorMouseExited

        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnMovimentos, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indMovimentos, indCopyright});

        setColor(btnTelaActual[0], btnTelaActual[1]);
    }//GEN-LAST:event_pnlNavegacaoSuperiorMouseExited

    private void pnlContainerMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlContainerMouseExited

    }//GEN-LAST:event_pnlContainerMouseExited

    private void btnLoginMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLoginMousePressed
        new Login().setVisible(true);
        dispose();
    }//GEN-LAST:event_btnLoginMousePressed

    private void btnLoginMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLoginMouseEntered
        setColor(btnLogin, indLogin);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnCopyright, btnMovimentos},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indCopyright, indMovimentos});
    }//GEN-LAST:event_btnLoginMouseEntered

    private void ConteinerRodapeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ConteinerRodapeMouseExited
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnMovimentos, btnCopyright, btnLogin},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indMovimentos, indCopyright, indLogin});

        setColor(btnTelaActual[0], btnTelaActual[1]);
    }//GEN-LAST:event_ConteinerRodapeMouseExited

    private void ContainerTelasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ContainerTelasMouseEntered
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnMovimentos, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indMovimentos, indLogin, indCopyright});

        setColor(btnTelaActual[0], btnTelaActual[1]);
    }//GEN-LAST:event_ContainerTelasMouseEntered

    private void pnlCopyrightMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCopyrightMouseEntered
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnMovimentos, btnCopyright, btnLogin},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indMovimentos, indCopyright, indLogin});

        setColor(btnTelaActual[0], btnTelaActual[1]);
    }//GEN-LAST:event_pnlCopyrightMouseEntered

    private void pnlCopyrightMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlCopyrightMousePressed

    }//GEN-LAST:event_pnlCopyrightMousePressed

    private void tblComponentes1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblComponentes1MouseClicked
        DefaultTableModel modelo = (DefaultTableModel) tblComponentes1.getModel();
        int linhaSelecionada = tblComponentes1.getSelectedRow();
        boxCodigoStockComponente.setSelectedItem(modelo.getValueAt(linhaSelecionada, 0).toString());
        boxCodigoFornecedorComponente.setSelectedItem(modelo.getValueAt(linhaSelecionada, 1).toString());
        boxCategoriaComponente.setSelectedItem(modelo.getValueAt(linhaSelecionada, 4).toString());

        txtStockDisponivelComponente.setText(modelo.getValueAt(linhaSelecionada, 2).toString());
        txtDescricaoComponente.setText(modelo.getValueAt(linhaSelecionada, 5).toString());
        txtPrecoComponente.setText(modelo.getValueAt(linhaSelecionada, 3).toString());
    }//GEN-LAST:event_tblComponentes1MouseClicked

    private void btnRelatorioComponenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRelatorioComponenteMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRelatorioComponenteMouseClicked

    private void btnRelatorioComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRelatorioComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRelatorioComponenteActionPerformed

    private void txtStockDisponivelComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStockDisponivelComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtStockDisponivelComponenteActionPerformed

    private void txtDescricaoComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescricaoComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescricaoComponenteActionPerformed

    private void txtPrecoComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrecoComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrecoComponenteActionPerformed

    private void boxCodigoFornecedorComponenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteMouseClicked

    }//GEN-LAST:event_boxCodigoFornecedorComponenteMouseClicked

    private void boxCodigoFornecedorComponenteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteItemStateChanged

    }//GEN-LAST:event_boxCodigoFornecedorComponenteItemStateChanged

    //
    private void boxCodigoFornecedorComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteActionPerformed

        /*String cat = Modificar.getCategoria(Integer.parseInt(boxCodigoFornecedorComponente.getSelectedItem().toString()));
         boxCategoriaComponente.removeAllItems();
         boxCategoriaComponente.addItem(cat);*/
    }//GEN-LAST:event_boxCodigoFornecedorComponenteActionPerformed

    private void boxCodigoFornecedorComponenteHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteHierarchyChanged

    }//GEN-LAST:event_boxCodigoFornecedorComponenteHierarchyChanged

    private void boxCodigoFornecedorComponenteCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteCaretPositionChanged

    }//GEN-LAST:event_boxCodigoFornecedorComponenteCaretPositionChanged

    private void boxCodigoFornecedorComponentePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponentePropertyChange

    }//GEN-LAST:event_boxCodigoFornecedorComponentePropertyChange

    private void boxCodigoFornecedorComponenteVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_boxCodigoFornecedorComponenteVetoableChange

    }//GEN-LAST:event_boxCodigoFornecedorComponenteVetoableChange

    private void boxCategoriaComponenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_boxCategoriaComponenteMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_boxCategoriaComponenteMouseClicked
/*
    private void boxCategoriaComponentePopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {                                                                           
        String cat = Modificar.getCategoria(Integer.parseInt(boxCategoriaComponente.getSelectedItem().toString()));
        boxCategoriaComponente.removeAllItems();
        boxCategoriaComponente.addItem(cat);
    }    
 */
    private void boxCategoriaComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxCategoriaComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_boxCategoriaComponenteActionPerformed

    // Método responsavel por eliminar e actualizar linha de uma tabela
    private void btnEliminarComponenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarComponenteMouseClicked
        int resposta = JOptionPane.showConfirmDialog(null, "Tem certeza?", "Eliminar componente", JOptionPane.YES_NO_OPTION);
        if (resposta != 0) {
            return;
        }
        Modificar.eliminarLinha(boxCodigoStockComponente.getSelectedItem().toString(), "GRUPO4.TABELA_COMPONENTES");
        actualizarTabela(tblComponentes1, "select * from GRUPO4.TABELA_COMPONENTES");
        limparCaixasDeTextoComponentes();
        JOptionPane.showMessageDialog(this, "Componente eliminado com sucesso");
    }//GEN-LAST:event_btnEliminarComponenteMouseClicked

    private void btnEliminarComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEliminarComponenteActionPerformed
// Actaliza os dados de um componente da tabela e na base de dados
    private void btnActualizarComponenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnActualizarComponenteMouseClicked
        if (!isComponentePeenchido()) {
            return;
        }
        Componentes componente = new Componentes(Integer.parseInt(boxCodigoStockComponente.getSelectedItem().toString()), Integer.parseInt(boxCodigoFornecedorComponente.getSelectedItem().toString()), boxCategoriaComponente.getSelectedItem().toString(), acumulado(), txtDescricaoComponente.getText(), Double.parseDouble(txtPrecoComponente.getText()));
        Modificar.actualizarLinha(componente);
        actualizarTabela(tblComponentes1, "select * from GRUPO4.TABELA_COMPONENTES");
        JOptionPane.showMessageDialog(this, "Dados actualizados com sucesso!");
    }//GEN-LAST:event_btnActualizarComponenteMouseClicked

    private void btnActualizarComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnActualizarComponenteActionPerformed
// Método responsvel por adicionar uma nova linha na tabela
    private void btnAdicionarComponenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAdicionarComponenteMouseClicked
        if (!isComponentePeenchido()) {
            return;
        }
        if (isComponenteExiste()) {
            JOptionPane.showMessageDialog(this, "O código de stock deve ser único!");
            return;
        }
        Componentes componente = new Componentes(Integer.parseInt(boxCodigoStockComponente.getSelectedItem().toString()),
                Integer.parseInt(boxCodigoFornecedorComponente.getSelectedItem().toString()), boxCategoriaComponente.getSelectedItem().toString(), Integer.parseInt(txtStockDisponivelComponente.getText()), txtDescricaoComponente.getText(), Double.parseDouble(txtPrecoComponente.getText()));
        Modificar.adicionarLinha(componente);
        actualizarTabela(tblComponentes1, "select * from GRUPO4.TABELA_COMPONENTES");
        JOptionPane.showMessageDialog(null, "Componente adicionado com sucesso");
    }//GEN-LAST:event_btnAdicionarComponenteMouseClicked

    private void btnAdicionarComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAdicionarComponenteActionPerformed

    // Metodo para limpar caixas de texto
    private void btnLimparComponenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLimparComponenteMouseClicked
        limparCaixasDeTextoComponentes();
    }//GEN-LAST:event_btnLimparComponenteMouseClicked

    private void btnLimparComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparComponenteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLimparComponenteActionPerformed

// Setters e getters dos atributos de uma tabela
    private void tblFornecedoresMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFornecedoresMouseClicked
        DefaultTableModel modelo = (DefaultTableModel) tblFornecedores.getModel();
        int linhaSelecionada = tblFornecedores.getSelectedRow();

        boxCodigoFornecedor.setSelectedItem(modelo.getValueAt(linhaSelecionada, 0).toString());
        txtNomeFornecedor.setText(modelo.getValueAt(linhaSelecionada, 1).toString());
        boxCategoriaFornecedor.setSelectedItem(modelo.getValueAt(linhaSelecionada, 2).toString());
        txtLocalizacaoFornecedor.setText(modelo.getValueAt(linhaSelecionada, 3).toString());
        txtEmailFornecedor.setText(modelo.getValueAt(linhaSelecionada, 4).toString());
        txtTelefoneFornecedor.setText(modelo.getValueAt(linhaSelecionada, 5).toString());
    }//GEN-LAST:event_tblFornecedoresMouseClicked
// Neste metodo os dados do fornecedor são preenchidos e adicionados e actualizados a tabela.
    private void btnActualizarFornecedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnActualizarFornecedorMouseClicked
        if (!isFornecedorPeenchido()) {
            return;
        }
        Fornecedores fornecedor = new Fornecedores(Integer.parseInt(boxCodigoFornecedor.getSelectedItem().toString()), txtNomeFornecedor.getText(), boxCategoriaFornecedor.getSelectedItem().toString(), txtLocalizacaoFornecedor.getText(), txtEmailFornecedor.getText(), txtTelefoneFornecedor.getText());
        Modificar.actualizarLinha(fornecedor);
        actualizarTabela(tblFornecedores, "select * from GRUPO4.TABELA_FORNECEDORES");
        JOptionPane.showMessageDialog(this, "Dados do forneçedor actualizados com sucesso!");
    }//GEN-LAST:event_btnActualizarFornecedorMouseClicked

    private void btnActualizarFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarFornecedorActionPerformed

    }//GEN-LAST:event_btnActualizarFornecedorActionPerformed
// Neste metodo é feito a verificação do código do fornecedor, caso o código esteja correcto é adicionado a tabela principal
    private void btnAdicionarFornecedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAdicionarFornecedorMouseClicked
        if (!isFornecedorPeenchido()) {
            return;
        }
        if (isFornecedorExiste()) {
            JOptionPane.showMessageDialog(this, "O código do forneçedor deve ser único \nDigite um codigo diferente e tente novamente.");
            return;
        }
        Fornecedores fornecedor = new Fornecedores(Integer.parseInt(boxCodigoFornecedor.getSelectedItem().toString()), txtNomeFornecedor.getText(), boxCategoriaFornecedor.getSelectedItem().toString(), txtLocalizacaoFornecedor.getText(), txtEmailFornecedor.getText(), txtTelefoneFornecedor.getText());
        Modificar.adicionarLinha(fornecedor);
        JOptionPane.showMessageDialog(this, "Forneçedor adicionado com sucesso");
        actualizarTabela(tblFornecedores, "select * from GRUPO4.TABELA_FORNECEDORES");

        adicionarItems(boxCodigoFornecedorComponente, "select * from GRUPO4.TABELA_FORNECEDORES", "CÓDIGO");

    }//GEN-LAST:event_btnAdicionarFornecedorMouseClicked

    private void btnAdicionarFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarFornecedorActionPerformed

    }//GEN-LAST:event_btnAdicionarFornecedorActionPerformed
// o metodo serve para  eliminar os dados do fornecedor
    private void btnEliminarFornecedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarFornecedorMouseClicked
        int resposta = JOptionPane.showConfirmDialog(null, "Tem certeza?", "Eliminar Forneçedor", JOptionPane.YES_NO_OPTION);
        if (resposta != 0) {
            return;
        }
        Modificar.eliminarLinha(boxCodigoFornecedor.getSelectedItem().toString(), "GRUPO4.TABELA_FORNECEDORES");
        actualizarTabela(tblFornecedores, "select * from GRUPO4.TABELA_FORNECEDORES");
        limparCaixasDeTextoFornecedor();
        JOptionPane.showMessageDialog(this, "Forneçedor eliminado com sucesso");
        adicionarItems(boxCodigoFornecedorComponente, "select * from GRUPO4.TABELA_FORNECEDORES", "CÓDIGO");
    }//GEN-LAST:event_btnEliminarFornecedorMouseClicked

    private void btnEliminarFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarFornecedorActionPerformed

    }//GEN-LAST:event_btnEliminarFornecedorActionPerformed

    private void btnLimparFornecedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLimparFornecedorMouseClicked
        limparCaixasDeTextoFornecedor();
    }//GEN-LAST:event_btnLimparFornecedorMouseClicked

    private void btnLimparFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparFornecedorActionPerformed

    }//GEN-LAST:event_btnLimparFornecedorActionPerformed

    private void txtNomeFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeFornecedorActionPerformed

    }//GEN-LAST:event_txtNomeFornecedorActionPerformed

    private void txtTelefoneFornecedortxtNumeroBIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTelefoneFornecedortxtNumeroBIActionPerformed

    }//GEN-LAST:event_txtTelefoneFornecedortxtNumeroBIActionPerformed

    private void txtEmailFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEmailFornecedorActionPerformed

    }//GEN-LAST:event_txtEmailFornecedorActionPerformed

    private void txtLocalizacaoFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLocalizacaoFornecedorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLocalizacaoFornecedorActionPerformed
// metodo responsavel pela eliminação de uma categoria
    private void btnEliminarCategoriaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarCategoriaMouseClicked
        int resposta = JOptionPane.showConfirmDialog(null, "Tem certeza?", "Eliminar Funcionario", JOptionPane.YES_NO_OPTION);
        if (resposta != 0) {
            return;
        }
        if (txtCodigoCategoria.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o código da categoria que deseja eliminar");
            return;
        }
        Modificar.eliminarLinha(txtCodigoCategoria.getText(), "GRUPO4.TABELA_CATEGORIAS");
        actualizarTabela(tblCategorias, "select * from GRUPO4.TABELA_CATEGORIAS");
        limparCaixasDeTextoCategoria();
        JOptionPane.showMessageDialog(this, "categoria eliminado com sucesso");
        adicionarItems(boxCategoriaFornecedor, "select * from GRUPO4.TABELA_CATEGORIAS", "NOME");
        adicionarItems(boxCategoriaComponente, "select * from GRUPO4.TABELA_CATEGORIAS", "NOME");
    }//GEN-LAST:event_btnEliminarCategoriaMouseClicked

    private void btnEliminarCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarCategoriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEliminarCategoriaActionPerformed
// Metodo responsavel por adicionar uma categoria
    private void btnAdicionarCategoriaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAdicionarCategoriaMouseClicked
        if (!isPeenchidoCategoria()) {
            return;
        }
        if (isExisteCategoria()) {
            JOptionPane.showMessageDialog(this, "O código da categoria deve ser único");
            return;
        }
        Categorias categoria = new Categorias(Integer.parseInt(txtCodigoCategoria.getText()), txtNomeCategoria.getText(), txtDescricaoCategoria.getText());
        Modificar.adicionarLinha(categoria);
        actualizarTabela(tblCategorias, "select * from GRUPO4.TABELA_CATEGORIAS");
        JOptionPane.showMessageDialog(null, "Categoria adicionada com sucesso!");

        adicionarItems(boxCategoriaFornecedor, "select * from GRUPO4.TABELA_CATEGORIAS", "NOME");
        adicionarItems(boxCategoriaComponente, "select * from GRUPO4.TABELA_CATEGORIAS", "NOME");
    }//GEN-LAST:event_btnAdicionarCategoriaMouseClicked

    private void btnAdicionarCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarCategoriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAdicionarCategoriaActionPerformed
// actualização das categorias
    private void btnActualizarCategoriaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnActualizarCategoriaMouseClicked
        if (!isPeenchidoCategoria()) {
            return;
        }
        Categorias categoria = new Categorias(Integer.parseInt(txtCodigoCategoria.getText()), txtNomeCategoria.getText(), txtDescricaoCategoria.getText());
        Modificar.actualizarLinha(categoria);
        actualizarTabela(tblCategorias, "select * from GRUPO4.TABELA_CATEGORIAS");
        JOptionPane.showMessageDialog(this, "Dados actualizados com sucesso!");
    }//GEN-LAST:event_btnActualizarCategoriaMouseClicked

    private void btnActualizarCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarCategoriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnActualizarCategoriaActionPerformed

    private void btnLimparCategoriaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLimparCategoriaMouseClicked
        limparCaixasDeTextoCategoria();
    }//GEN-LAST:event_btnLimparCategoriaMouseClicked

    private void btnLimparCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparCategoriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLimparCategoriaActionPerformed

    private void txtCodigoCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoCategoriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCodigoCategoriaActionPerformed

    private void txtNomeCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeCategoriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomeCategoriaActionPerformed

    private void txtDescricaoCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescricaoCategoriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescricaoCategoriaActionPerformed

    // Setters e getters dos atributos de uma categoria na tabela
    private void tblCategoriasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCategoriasMouseClicked
        DefaultTableModel modelo = (DefaultTableModel) tblCategorias.getModel();
        int linhaSelecionada = tblCategorias.getSelectedRow();
        txtCodigoCategoria.setText(modelo.getValueAt(linhaSelecionada, 0).toString());
        txtNomeCategoria.setText(modelo.getValueAt(linhaSelecionada, 1).toString());
        txtDescricaoCategoria.setText(modelo.getValueAt(linhaSelecionada, 2).toString());
    }//GEN-LAST:event_tblCategoriasMouseClicked

    private void tblComponentesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblComponentesMouseClicked
        DefaultTableModel modelo = (DefaultTableModel) tblComponentes.getModel();
        int linhaSelecionada = tblComponentes.getSelectedRow();
        boxCodigoStock.setSelectedItem(modelo.getValueAt(linhaSelecionada, 0).toString());
    }//GEN-LAST:event_tblComponentesMouseClicked

    private void txtQuantidadetxtNumeroBIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantidadetxtNumeroBIActionPerformed

    }//GEN-LAST:event_txtQuantidadetxtNumeroBIActionPerformed
// Este metoto faz a contablização de todos os movimentos de entradas e  de saídas, mostrado tabel o preço gloabal de movimentos de entradas e o preço gloabal das saídas. 
    private void btnTerminar2btnTerminarbtnLimparMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnTerminar2btnTerminarbtnLimparMouseClicked
        int codigoStock = Integer.parseInt(boxCodigoStock.getSelectedItem().toString());
        Movimentos movimento = new Movimentos(boxTipoMovimento.getSelectedItem().toString(), codigoStock, Integer.parseInt(txtQuantidade.getText()), contador, new String[]{boxDia.getSelectedItem().toString(), boxMes.getSelectedItem().toString(), boxAno.getSelectedItem().toString()}, precoUnitario(codigoStock), precoTotalGlobalEntradas, precoTotalGlobalSaidas);
        if (boxTipoMovimento.getSelectedItem().toString().equalsIgnoreCase("Entrada")) {
            txtFacturaEntrada.setText(txtFacturaEntrada.getText()
                    + "\n      * Preço total Global      \t" + precoTotalGlobalEntradas + "0 MZN"
                    + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                    + "\n                  * * * OBRIGADO * * *"
                    + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
            );
            return;
        }
        txtFacturaSaida.setText(txtFacturaSaida.getText()
                + "\n      * Preço total Global      \t" + precoTotalGlobalSaidas + "0 MZN"
                + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                + "\n                  * * * OBRIGADO * * *"
                + "\n  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n"
        );
    }//GEN-LAST:event_btnTerminar2btnTerminarbtnLimparMouseClicked

    private void btnTerminar2btnTerminarbtnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTerminar2btnTerminarbtnLimparActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTerminar2btnTerminarbtnLimparActionPerformed

// Método responsavel pela impressão das facturas
    private void btnImprimir2btnImprimirbtnEliminarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimir2btnImprimirbtnEliminarMouseClicked
        if (!"".equals(txtFacturaEntrada.getText())
                && boxTipoMovimento.getSelectedItem().toString().equalsIgnoreCase("Entrada")) {
            try {
                txtFacturaEntrada.print();         // Imprime a factura das saídas
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (!"".equals(txtFacturaSaida.getText())
                && boxTipoMovimento.getSelectedItem().toString().equalsIgnoreCase("Saída")) {
            try {
                txtFacturaSaida.print();         // Imprime a factura das entradas
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        JOptionPane.showMessageDialog(this, "Nenhum movimento registado");
    }//GEN-LAST:event_btnImprimir2btnImprimirbtnEliminarMouseClicked

    private void btnImprimir2btnImprimirbtnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimir2btnImprimirbtnEliminarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnImprimir2btnImprimirbtnEliminarActionPerformed

    /* Metodo responsavel pela impresssão e actualização do relatório, que tem com os seguintes dados:
     Do código de Stock;
     Quantidades em stock;
     Saídas;
     O preço total
    
     */

    private void btnrelatoriobtnActualizarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnrelatoriobtnActualizarMouseClicked

        pnlCategorias.setVisible(false);
        pnlMovimentos.setVisible(false);
        pnlRelatorio.setVisible(true);
        pnlFuncionarios.setVisible(false);

        txtRelatorio.setText(
                "\n\n\t\t * * * * * * * * * *  DIGITAL WEREHOUSE  * * * * *  * * * * *"
                + "\n      - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - "
                + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                + "\n\tCódigo de stock \tQuantidade em stock \tSaídas \tMontante total [MZN]"
                + "\n      - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - "
                + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
        );
        String[][] componentes = Modificar.getComponente();
        double precoTotalGlobalSaidas = 0;
        for (String[] componente : componentes) {
            txtRelatorio.setText(txtRelatorio.getText()
                    + "\n\t          " + componente[0] + "\t\t              " + componente[1] + "\t\t    " + componente[4] + "\t   " + componente[3] + "\n"
            );
            precoTotalGlobalSaidas += Double.parseDouble(componente[3]);
        }
        int indexProdutoMaisSaidas = 0, maior = 0;
        boolean algumaSaida = false;
        for (int i = 0; i < componentes.length; i++) {
            if (maior < Integer.parseInt(componentes[i][4])) {
                maior = Integer.parseInt(componentes[i][4]);
                indexProdutoMaisSaidas = i;
                algumaSaida = true;
            }
        }
        // O formato do relatório 
        if (!algumaSaida) {

            txtRelatorio.setText(txtRelatorio.getText()
                    + "      - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - "
                    + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                    + "\n\t"
                    + "\n\tMontante total global das saídas: 00 MZN"
                    + "\n\tProduto com mais saídas: null : null"
                    + "\n\tNome do fornecedor: null"
            );
        } else {
            // Aqui são mostrados os produtos com mais saídas, o nome do fornecedor 
            // e o  montante total  das saidas. NB: Todos estes dados são impressos em formato de texto.
            txtRelatorio.setText(txtRelatorio.getText()
                    + "      - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - "
                    + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
                    + "\n\t"
                    + "\n\tMontante total global das saídas: " + precoTotalGlobalSaidas + "0 MZN"
                    + "\n\tProduto com mais saídas: " + componentes[indexProdutoMaisSaidas][5] + " : " + componentes[indexProdutoMaisSaidas][6]
                    + "\n\tNome do fornecedor: " + componentes[indexProdutoMaisSaidas][7]
            );
        }
    }//GEN-LAST:event_btnrelatoriobtnActualizarMouseClicked

    private void btnrelatoriobtnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnrelatoriobtnActualizarActionPerformed

    }//GEN-LAST:event_btnrelatoriobtnActualizarActionPerformed
// Metodo de finalização dos movimentos
    private void btnFacturabtnAdicionarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFacturabtnAdicionarMouseClicked
        if (!isMovimentoPeenchido()) {
            return;
        }
        contador++;
        int codigoStock = Integer.parseInt(boxCodigoStock.getSelectedItem().toString());
        Movimentos movimento = new Movimentos(boxTipoMovimento.getSelectedItem().toString(), codigoStock, Integer.parseInt(txtQuantidade.getText()), contador, new String[]{boxDia.getSelectedItem().toString(), boxMes.getSelectedItem().toString(), boxAno.getSelectedItem().toString()}, precoUnitario(codigoStock));
        int acumulado = acumulado(movimento);
        if (acumulado != 12345678) {
            Modificar.actualizarLinha(acumulado, movimento.codigoStock);
            Modificar.adicionarLinha(movimento);
            actualizarTabela(tblComponentes, "select * from GRUPO4.TABELA_COMPONENTES");
            facturar(movimento);
            JOptionPane.showMessageDialog(this, "Movimento efetuado com sucesso");

        }
    }//GEN-LAST:event_btnFacturabtnAdicionarMouseClicked

    private void btnFacturaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_btnFacturaStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFacturaStateChanged

    private void btnFacturabtnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFacturabtnAdicionarActionPerformed

    }//GEN-LAST:event_btnFacturabtnAdicionarActionPerformed

    private void btnPrintMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPrintMouseClicked
        try {
            txtRelatorio.print();
        } catch (PrinterException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnPrintMouseClicked

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnFecharMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFecharMouseClicked
        pnlMovimentos.setVisible(true);
        pnlRelatorio.setVisible(false);
    }//GEN-LAST:event_btnFecharMouseClicked

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnFecharActionPerformed

    private void btnComponentesFuncionariosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesFuncionariosMouseEntered
        setColor(btnComponentesFuncionarios);
    }//GEN-LAST:event_btnComponentesFuncionariosMouseEntered

    private void btnComponentesFuncionariosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesFuncionariosMouseExited
        mouseExit(btnComponentesFuncionarios);
    }//GEN-LAST:event_btnComponentesFuncionariosMouseExited

    private void btnComponentesFuncionariosMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesFuncionariosMousePressed
        btnTelaActual = new JPanel[]{btnComponentes, indComponentes};
        setColor(btnComponentes, indComponentes);
        resetColor(new JPanel[]{btnForncedores, btnFuncionarios, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});
        setPainel(pnlComponentes);
        actualizarTabela(tblComponentes1, "select * from GRUPO4.TABELA_COMPONENTES");
        adicionarItems(boxCodigoFornecedorComponente, "select * from GRUPO4.TABELA_FORNECEDORES", "CÓDIGO");
    }//GEN-LAST:event_btnComponentesFuncionariosMousePressed

    private void btnComponentesFuncionariosMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesFuncionariosMouseDragged

    }//GEN-LAST:event_btnComponentesFuncionariosMouseDragged

    private void btnComponentesFuncionariosFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_btnComponentesFuncionariosFocusGained

    }//GEN-LAST:event_btnComponentesFuncionariosFocusGained

    private void btnForncedoresFuncionariosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresFuncionariosMouseEntered
        setColor(btnForncedoresFuncionarios);
    }//GEN-LAST:event_btnForncedoresFuncionariosMouseEntered

    private void btnForncedoresFuncionariosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresFuncionariosMouseExited
        mouseExit(btnForncedoresFuncionarios);
    }//GEN-LAST:event_btnForncedoresFuncionariosMouseExited

    private void btnForncedoresFuncionariosMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresFuncionariosMousePressed
        btnTelaActual = new JPanel[]{btnForncedores, indForncedores};
        setColor(btnForncedores, indForncedores);
        resetColor(new JPanel[]{btnComponentes, btnFuncionarios, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indComponentes, indFuncionarios, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});
        setPainel(pnlFornecedores);
        actualizarTabela(tblFornecedores, "select * from GRUPO4.TABELA_FORNECEDORES");
    }//GEN-LAST:event_btnForncedoresFuncionariosMousePressed

    private void btnCategoriasFuncionariosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategoriasFuncionariosMouseEntered
        setColor(btnCategoriasFuncionarios);
    }//GEN-LAST:event_btnCategoriasFuncionariosMouseEntered

    private void btnCategoriasFuncionariosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategoriasFuncionariosMouseExited
        mouseExit(btnCategoriasFuncionarios);
    }//GEN-LAST:event_btnCategoriasFuncionariosMouseExited

    private void btnCategoriasFuncionariosMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategoriasFuncionariosMousePressed
        btnTelaActual = new JPanel[]{btnCategorias, indCategorias};
        setColor(btnCategorias, indCategorias);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnMovimentos, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indMovimentos, indGerente, indLogin, indCopyright});
        setPainel(pnlCategorias);
        actualizarTabela(tblCategorias, "select * from GRUPO4.TABELA_CATEGORIAS");
    }//GEN-LAST:event_btnCategoriasFuncionariosMousePressed

    private void btnMovimentosFuncionariosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovimentosFuncionariosMouseEntered
        setColor(btnMovimentosFuncionarios);
    }//GEN-LAST:event_btnMovimentosFuncionariosMouseEntered

    private void btnMovimentosFuncionariosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovimentosFuncionariosMouseExited
        mouseExit(btnMovimentosFuncionarios);
    }//GEN-LAST:event_btnMovimentosFuncionariosMouseExited

    private void btnMovimentosFuncionariosMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovimentosFuncionariosMousePressed
        btnTelaActual = new JPanel[]{btnMovimentos, indMovimentos};
        setColor(btnMovimentos, indMovimentos);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indLogin, indCopyright});
        setPainel(pnlMovimentos);
        actualizarTabela(tblComponentes, "select * from GRUPO4.TABELA_COMPONENTES");
        adicionarItems(boxCodigoStock, "select * from GRUPO4.TABELA_COMPONENTES", "CÓDIGO");
    }//GEN-LAST:event_btnMovimentosFuncionariosMousePressed

    private void btnComponentesGerenteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesGerenteMouseClicked

    }//GEN-LAST:event_btnComponentesGerenteMouseClicked

    private void btnComponentesGerenteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesGerenteMouseEntered
        setColor(btnComponentesGerente);
    }//GEN-LAST:event_btnComponentesGerenteMouseEntered

    private void btnComponentesGerenteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesGerenteMouseExited
        mouseExit(btnComponentesGerente);
    }//GEN-LAST:event_btnComponentesGerenteMouseExited

    private void btnComponentesGerenteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesGerenteMousePressed
        setPainel(pnlComponentes); //   Activa a visibilidade do painel Componentes
        actualizarTabela(tblComponentes1, "select * from GRUPO4.TABELA_COMPONENTES");
        adicionarItems(boxCodigoFornecedorComponente, "select * from GRUPO4.TABELA_FORNECEDORES", "CÓDIGO");

        btnTelaActual = new JPanel[]{btnComponentes, indComponentes};
        setColor(btnComponentes, indComponentes);
        resetColor(new JPanel[]{btnForncedores, btnFuncionarios, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});

    }//GEN-LAST:event_btnComponentesGerenteMousePressed

    private void btnComponentesGerenteMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnComponentesGerenteMouseDragged

    }//GEN-LAST:event_btnComponentesGerenteMouseDragged

    private void btnComponentesGerenteFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_btnComponentesGerenteFocusGained

    }//GEN-LAST:event_btnComponentesGerenteFocusGained

    private void btnFuncionariosGerenteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFuncionariosGerenteMouseEntered
        setColor(btnFuncionariosGerente);
    }//GEN-LAST:event_btnFuncionariosGerenteMouseEntered

    private void btnFuncionariosGerenteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFuncionariosGerenteMouseExited
        mouseExit(btnFuncionariosGerente);
    }//GEN-LAST:event_btnFuncionariosGerenteMouseExited

    private void btnFuncionariosGerenteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFuncionariosGerenteMousePressed
        btnTelaActual = new JPanel[]{btnFuncionarios, indFuncionarios};
        setColor(btnFuncionarios, indFuncionarios);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indComponentes, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});
        setPainel(pnlFuncionarios);
        actualizarTabela(tblFuncionarios, "select * from GRUPO4.TABELA_FUNCIONARIOS");
    }//GEN-LAST:event_btnFuncionariosGerenteMousePressed

    private void btnGerenteGerenteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGerenteGerenteMouseEntered
        setColor(btnGerenteGerente);
    }//GEN-LAST:event_btnGerenteGerenteMouseEntered

    private void btnGerenteGerenteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGerenteGerenteMouseExited
        mouseExit(btnGerenteGerente);
    }//GEN-LAST:event_btnGerenteGerenteMouseExited

    private void btnGerenteGerenteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGerenteGerenteMousePressed
        btnTelaActual = new JPanel[]{btnGerente, indGerente};
        setColor(btnGerente, indGerente);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnMovimentos, btnCategorias, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indMovimentos, indCategorias, indLogin, indCopyright});
        new Gerente().setVisible(true);
        dispose();
    }//GEN-LAST:event_btnGerenteGerenteMousePressed

    private void btnCategoriasGerenteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategoriasGerenteMouseEntered
        setColor(btnCategoriasGerente);
    }//GEN-LAST:event_btnCategoriasGerenteMouseEntered

    private void btnCategoriasGerenteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategoriasGerenteMouseExited
        mouseExit(btnCategoriasGerente);
    }//GEN-LAST:event_btnCategoriasGerenteMouseExited

    private void btnCategoriasGerenteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCategoriasGerenteMousePressed
        btnTelaActual = new JPanel[]{btnCategorias, indCategorias};
        setColor(btnCategorias, indCategorias);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnMovimentos, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indMovimentos, indGerente, indLogin, indCopyright});
        setPainel(pnlCategorias);
        actualizarTabela(tblCategorias, "select * from GRUPO4.TABELA_CATEGORIAS");
    }//GEN-LAST:event_btnCategoriasGerenteMousePressed

    private void btnMovimentosGerenteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovimentosGerenteMouseEntered
        setColor(btnMovimentosGerente);
    }//GEN-LAST:event_btnMovimentosGerenteMouseEntered

    private void btnMovimentosGerenteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovimentosGerenteMouseExited
        mouseExit(btnMovimentosGerente);
    }//GEN-LAST:event_btnMovimentosGerenteMouseExited

    private void btnMovimentosGerenteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMovimentosGerenteMousePressed
        btnTelaActual = new JPanel[]{btnMovimentos, indMovimentos};
        setColor(btnMovimentos, indMovimentos);
        resetColor(new JPanel[]{btnComponentes, btnForncedores, btnFuncionarios, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indForncedores, indFuncionarios, indComponentes, indCategorias, indGerente, indLogin, indCopyright});
        setPainel(pnlMovimentos);
        actualizarTabela(tblComponentes, "select * from GRUPO4.TABELA_COMPONENTES");
        adicionarItems(boxCodigoStock, "select * from GRUPO4.TABELA_COMPONENTES", "CÓDIGO");
    }//GEN-LAST:event_btnMovimentosGerenteMousePressed

    private void btnForncedoresGerenteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresGerenteMousePressed
        btnTelaActual = new JPanel[]{btnForncedores, indForncedores};
        setColor(btnForncedores, indForncedores);
        resetColor(new JPanel[]{btnComponentes, btnFuncionarios, btnMovimentos, btnCategorias, btnGerente, btnLogin, btnCopyright},
                new JPanel[]{indComponentes, indFuncionarios, indMovimentos, indCategorias, indGerente, indLogin, indCopyright});
        setPainel(pnlFornecedores);
        actualizarTabela(tblFornecedores, "select * from GRUPO4.TABELA_FORNECEDORES");
    }//GEN-LAST:event_btnForncedoresGerenteMousePressed

    private void btnForncedoresGerenteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresGerenteMouseExited
        mouseExit(btnForncedoresGerente);
    }//GEN-LAST:event_btnForncedoresGerenteMouseExited

    private void btnForncedoresGerenteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnForncedoresGerenteMouseEntered
        setColor(btnForncedoresGerente);
    }//GEN-LAST:event_btnForncedoresGerenteMouseEntered

    private void tblFuncionariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFuncionariosMouseClicked
        DefaultTableModel modelo = (DefaultTableModel) tblFuncionarios.getModel();
        int linhaSelecionada = tblFuncionarios.getSelectedRow();
        int codigo = Integer.parseInt(modelo.getValueAt(linhaSelecionada, 0).toString());
        txtCodigoFuncionario.setText(Integer.toString(codigo));
        txtNomeFuncionario.setText(modelo.getValueAt(linhaSelecionada, 1).toString());
        boxFuncao.setSelectedItem(modelo.getValueAt(linhaSelecionada, 2).toString());
        txtNumeroBIFuncionario.setText(modelo.getValueAt(linhaSelecionada, 3).toString());
        txtSenhaFuncionario.setText(modelo.getValueAt(linhaSelecionada, 4).toString());
        txtTelefoneFuncionario.setText(modelo.getValueAt(linhaSelecionada, 5).toString());

        AdicionarFotoLista.setIcon(Modificar.getFoto(codigo, AdicionarFotoLista));
        AdicionarFoto.setIcon(Modificar.getFoto(codigo, AdicionarFoto));
    }//GEN-LAST:event_tblFuncionariosMouseClicked

    private void btnEliminarFuncionario1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarFuncionario1MouseClicked
        int resposta = JOptionPane.showConfirmDialog(null, "Tem certeza?", "Eliminar Funcionario", JOptionPane.YES_NO_OPTION);
        if (resposta != 0) {
            return;
        }
        if (txtCodigoFuncionario.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o código do funcionario que deseja eliminar");
            return;
        }
        Modificar.eliminarLinha(txtCodigoFuncionario.getText(), "GRUPO4.TABELA_FUNCIONARIOS");
        actualizarTabela(tblFuncionarios, "select * from GRUPO4.TABELA_FUNCIONARIOS");
        limparCaixasDeTextoFuncionario();
        JOptionPane.showMessageDialog(this, "Funcionario eliminado com sucesso");
    }//GEN-LAST:event_btnEliminarFuncionario1MouseClicked

    private void btnEliminarFuncionario1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarFuncionario1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEliminarFuncionario1ActionPerformed

    private void btnVoltarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVoltarMouseClicked
        limparCaixasDeTextoFuncionario();
        pnlFuncionarios.setVisible(true);
        pnlListaFuncionarios.setVisible(false);

    }//GEN-LAST:event_btnVoltarMouseClicked

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void txtNomeFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeFuncionarioActionPerformed

    }//GEN-LAST:event_txtNomeFuncionarioActionPerformed

    private void txtSenhaFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSenhaFuncionarioActionPerformed

    }//GEN-LAST:event_txtSenhaFuncionarioActionPerformed

    private void txtTelefoneFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTelefoneFuncionarioActionPerformed

    }//GEN-LAST:event_txtTelefoneFuncionarioActionPerformed

    private void txtCodigoFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoFuncionarioActionPerformed

    }//GEN-LAST:event_txtCodigoFuncionarioActionPerformed

    private void txtNumeroBIFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNumeroBIFuncionarioActionPerformed

    }//GEN-LAST:event_txtNumeroBIFuncionarioActionPerformed

    private void btnActualizarFuncionarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnActualizarFuncionarioMouseClicked
        /*  if (!isFuncionarioPeenchido()) {
         return;
         }
         Funcionarios funcionario = new Funcionarios(Integer.parseInt(txtCodigoFuncionario.getText()), txtNomeFuncionario.getText(), boxGeneroFuncionario.getSelectedItem().toString(), txtNumeroBIFuncionario.getText(), txtSenhaFuncionario.getText(), txtTelefoneFuncionario.getText(), boxFuncao.getSelectedItem().toString(), this.fotoPessoal);
         Modificar.actualizarLinha(funcionario);
         actualizarTabela(tblFuncionarios, "select * from GRUPO4.TABELA_FUNCIONARIOS");
         JOptionPane.showMessageDialog(this, "Dados do funcionario actualizados com sucesso!");*/

        if (!isFuncionarioPeenchido()) {
            return;
        }
        Modificar.eliminarLinha(txtCodigoFuncionario.getText(), "GRUPO4.TABELA_FUNCIONARIOS");
        actualizarTabela(tblFuncionarios, "select * from GRUPO4.TABELA_FUNCIONARIOS");
        Funcionarios funcionario = new Funcionarios(Integer.parseInt(txtCodigoFuncionario.getText()), txtNomeFuncionario.getText(), boxGeneroFuncionario.getSelectedItem().toString(), txtNumeroBIFuncionario.getText(), txtSenhaFuncionario.getText(), txtTelefoneFuncionario.getText(), boxFuncao.getSelectedItem().toString(), this.fotoPessoal);
        Modificar.adicionarLinha(funcionario);
        actualizarTabela(tblFuncionarios, "select * from GRUPO4.TABELA_FUNCIONARIOS");
        JOptionPane.showMessageDialog(this, "Funcionario actualizado com sucesso");
        AdicionarFotoLista.setIcon(Modificar.getFoto(Integer.parseInt(txtCodigoFuncionario.getText()), AdicionarFotoLista));
        AdicionarFoto.setIcon(Modificar.getFoto(Integer.parseInt(txtCodigoFuncionario.getText()), AdicionarFoto));


    }//GEN-LAST:event_btnActualizarFuncionarioMouseClicked

    private void btnActualizarFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarFuncionarioActionPerformed

    }//GEN-LAST:event_btnActualizarFuncionarioActionPerformed

    private void btnAdicionarFuncionarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAdicionarFuncionarioMouseClicked
        if (!isFuncionarioPeenchido()) {
            return;
        }
        if (isFuncionarioExiste()) {
            JOptionPane.showMessageDialog(this, "O código do funcionario deve ser único \nDigite um codigo diferente e tente novamente.");
            return;
        }
        Funcionarios funcionario = new Funcionarios(Integer.parseInt(txtCodigoFuncionario.getText()), txtNomeFuncionario.getText(), boxGeneroFuncionario.getSelectedItem().toString(), txtNumeroBIFuncionario.getText(), txtSenhaFuncionario.getText(), txtTelefoneFuncionario.getText(), boxFuncao.getSelectedItem().toString(), this.fotoPessoal);
        Modificar.adicionarLinha(funcionario);
        actualizarTabela(tblFuncionarios, "select * from GRUPO4.TABELA_FUNCIONARIOS");
        JOptionPane.showMessageDialog(this, "Funcionario adicionado com sucesso");


    }//GEN-LAST:event_btnAdicionarFuncionarioMouseClicked

    private void btnAdicionarFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarFuncionarioActionPerformed

    }//GEN-LAST:event_btnAdicionarFuncionarioActionPerformed

    private void btnEliminarFuncionarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarFuncionarioMouseClicked
        pnlListaFuncionarios.setVisible(true);
        pnlFuncionarios.setVisible(false);
    }//GEN-LAST:event_btnEliminarFuncionarioMouseClicked

    private void btnEliminarFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarFuncionarioActionPerformed

    }//GEN-LAST:event_btnEliminarFuncionarioActionPerformed

    private void btnLimparFuncionarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLimparFuncionarioMouseClicked
        limparCaixasDeTextoFuncionario();
    }//GEN-LAST:event_btnLimparFuncionarioMouseClicked

    private void btnLimparFuncionarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparFuncionarioActionPerformed

    }//GEN-LAST:event_btnLimparFuncionarioActionPerformed

    private void btnEditarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditarMouseClicked
        if (!isFuncionarioPeenchido()) {
            JOptionPane.showMessageDialog(this, "Por favor selecione um funcionario na lista!");
            return;
        }
        pnlFuncionarios.setVisible(true);
        pnlListaFuncionarios.setVisible(false);
    }//GEN-LAST:event_btnEditarMouseClicked

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEditarActionPerformed
    FileInputStream fotoPessoal = null;
    String fotoPath = null;
    private void AdicionarFotoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AdicionarFotoMousePressed
        JFileChooser fotoChooser = new JFileChooser();
        fotoChooser.setDialogTitle("Selecione a imagem");
        fotoChooser.showOpenDialog(null);
        File fotoFile = fotoChooser.getSelectedFile();
        if (fotoFile == null) {
            return;
        }
        fotoPath = fotoFile.getAbsolutePath();
        ImageIcon fotoIcon = new ImageIcon(new ImageIcon(fotoPath).getImage().getScaledInstance(AdicionarFoto.getWidth(), AdicionarFoto.getHeight(), Image.SCALE_SMOOTH));
        AdicionarFoto.setIcon(fotoIcon);
        try {
            fotoPessoal = new FileInputStream(fotoFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, fotoPath);
    }//GEN-LAST:event_AdicionarFotoMousePressed

    private void AdicionarFotoListaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AdicionarFotoListaMousePressed
        // TODO add your handling code here:
    }//GEN-LAST:event_AdicionarFotoListaMousePressed

    private void boxCodigoFornecedorComponenteInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteInputMethodTextChanged

    }//GEN-LAST:event_boxCodigoFornecedorComponenteInputMethodTextChanged

    private void boxCodigoFornecedorComponenteMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteMousePressed

    }//GEN-LAST:event_boxCodigoFornecedorComponenteMousePressed

    private void boxCodigoFornecedorComponenteComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteComponentResized

    }//GEN-LAST:event_boxCodigoFornecedorComponenteComponentResized

    private void boxCodigoFornecedorComponenteAncestorMoved(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponenteAncestorMoved

    }//GEN-LAST:event_boxCodigoFornecedorComponenteAncestorMoved

    private void boxCodigoFornecedorComponentePopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_boxCodigoFornecedorComponentePopupMenuWillBecomeInvisible
        String cat = Modificar.getCategoria(Integer.parseInt(boxCodigoFornecedorComponente.getSelectedItem().toString()));
        boxCategoriaComponente.removeAllItems();
        boxCategoriaComponente.addItem(cat);
    }//GEN-LAST:event_boxCodigoFornecedorComponentePopupMenuWillBecomeInvisible

    private void boxCategoriaFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxCategoriaFornecedorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_boxCategoriaFornecedorActionPerformed

    /* public static void main(String args[]) {
     java.awt.EventQueue.invokeLater(new Runnable() {
     public void run() {
     new TelaPrincipal().setVisible(true);
     }
     });
     }*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AdicionarFoto;
    private javax.swing.JLabel AdicionarFotoLista;
    private javax.swing.JLayeredPane ContainerFuncionario;
    private javax.swing.JLayeredPane ContainerSuperior;
    private javax.swing.JLayeredPane ContainerTelas;
    private javax.swing.JPanel ConteinerRodape;
    private javax.swing.JComboBox boxAno;
    private javax.swing.JComboBox boxCategoriaComponente;
    private javax.swing.JComboBox boxCategoriaFornecedor;
    private javax.swing.JComboBox boxCodigoFornecedor;
    private static javax.swing.JComboBox boxCodigoFornecedorComponente;
    private javax.swing.JComboBox boxCodigoStock;
    private javax.swing.JComboBox boxCodigoStockComponente;
    private javax.swing.JComboBox boxDia;
    private javax.swing.JComboBox boxEstadcivilFuncionario;
    private javax.swing.JComboBox boxFuncao;
    private javax.swing.JComboBox boxGeneroFuncionario;
    private javax.swing.JComboBox boxMes;
    private javax.swing.JComboBox boxTipoMovimento;
    private javax.swing.JButton btnActualizarCategoria;
    private javax.swing.JButton btnActualizarComponente;
    private javax.swing.JButton btnActualizarFornecedor;
    private javax.swing.JButton btnActualizarFuncionario;
    private javax.swing.JButton btnAdicionarCategoria;
    private javax.swing.JButton btnAdicionarComponente;
    private javax.swing.JButton btnAdicionarFornecedor;
    private javax.swing.JButton btnAdicionarFuncionario;
    private javax.swing.JPanel btnCategorias;
    private javax.swing.JPanel btnCategoriasFuncionarios;
    private javax.swing.JPanel btnCategoriasGerente;
    private javax.swing.JPanel btnComponentes;
    private javax.swing.JPanel btnComponentesFuncionarios;
    private javax.swing.JPanel btnComponentesGerente;
    private javax.swing.JPanel btnCopyright;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminarCategoria;
    private javax.swing.JButton btnEliminarComponente;
    private javax.swing.JButton btnEliminarFornecedor;
    private javax.swing.JButton btnEliminarFuncionario;
    private javax.swing.JButton btnEliminarFuncionario1;
    private javax.swing.JButton btnFactura;
    private javax.swing.JButton btnFechar;
    private javax.swing.JPanel btnForncedores;
    private javax.swing.JPanel btnForncedoresFuncionarios;
    private javax.swing.JPanel btnForncedoresGerente;
    private javax.swing.JPanel btnFuncionarios;
    private javax.swing.JPanel btnFuncionariosGerente;
    private javax.swing.JPanel btnGerente;
    private javax.swing.JPanel btnGerenteGerente;
    private javax.swing.JButton btnImprimir2;
    private javax.swing.JButton btnLimparCategoria;
    private javax.swing.JButton btnLimparComponente;
    private javax.swing.JButton btnLimparFornecedor;
    private javax.swing.JButton btnLimparFuncionario;
    private javax.swing.JPanel btnLogin;
    private javax.swing.JPanel btnMovimentos;
    private javax.swing.JPanel btnMovimentosFuncionarios;
    private javax.swing.JPanel btnMovimentosGerente;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnRelatorioComponente;
    private javax.swing.JButton btnTerminar2;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JButton btnrelatorio;
    private javax.swing.JPanel indCategorias;
    private javax.swing.JPanel indComponentes;
    private javax.swing.JPanel indCopyright;
    private javax.swing.JPanel indForncedores;
    private javax.swing.JPanel indFuncionarios;
    private javax.swing.JPanel indGerente;
    private javax.swing.JPanel indLogin;
    private javax.swing.JPanel indMovimentos;
    private javax.swing.JLabel inicio;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JPanel plncabecalho;
    private javax.swing.JPanel pnlCategorias;
    private javax.swing.JPanel pnlComponentes;
    private javax.swing.JPanel pnlContainer;
    private javax.swing.JPanel pnlCopyright;
    private javax.swing.JPanel pnlFornecedores;
    private javax.swing.JPanel pnlFuncionarios;
    private javax.swing.JPanel pnlInicial;
    private javax.swing.JPanel pnlListaFuncionarios;
    private javax.swing.JPanel pnlMovimentos;
    private javax.swing.JPanel pnlNavegacaoSuperior;
    private javax.swing.JPanel pnlPaginaInicialFuncionario;
    private javax.swing.JPanel pnlPaginaInicialGerente;
    private javax.swing.JPanel pnlRelatorio;
    private javax.swing.JPanel pnlTelas;
    private javax.swing.JTable tblCategorias;
    private javax.swing.JTable tblComponentes;
    private javax.swing.JTable tblComponentes1;
    private javax.swing.JTable tblFornecedores;
    private javax.swing.JTable tblFuncionarios;
    private javax.swing.JTextField txtCodigoCategoria;
    private javax.swing.JTextField txtCodigoFuncionario;
    private javax.swing.JTextField txtDescricaoCategoria;
    private javax.swing.JTextField txtDescricaoComponente;
    private javax.swing.JTextField txtEmailFornecedor;
    private javax.swing.JTextArea txtFacturaEntrada;
    private javax.swing.JTextArea txtFacturaSaida;
    private javax.swing.JTextField txtLocalizacaoFornecedor;
    private javax.swing.JTextField txtNomeCategoria;
    private javax.swing.JTextField txtNomeFornecedor;
    private javax.swing.JTextField txtNomeFuncionario;
    private javax.swing.JTextField txtNumeroBIFuncionario;
    private javax.swing.JTextField txtPrecoComponente;
    private javax.swing.JTextField txtQuantidade;
    private javax.swing.JTextArea txtRelatorio;
    private javax.swing.JTextField txtSenhaFuncionario;
    private javax.swing.JTextField txtStockDisponivelComponente;
    private javax.swing.JTextField txtTelefoneFornecedor;
    private javax.swing.JTextField txtTelefoneFuncionario;
    // End of variables declaration//GEN-END:variables
}
