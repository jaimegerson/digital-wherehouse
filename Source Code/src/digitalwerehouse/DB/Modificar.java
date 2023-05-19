package digitalwerehouse.DB;

import digitalwerehouse.Categorias;
import digitalwerehouse.Componentes;
import digitalwerehouse.Fornecedores;
import digitalwerehouse.Funcionarios;
import digitalwerehouse.Login;
import digitalwerehouse.Movimentos;
import java.awt.Image;  
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Modificar {
    // Méto para actualizar um registro na base de dados
    public static void actualizarLinha(Componentes componente) {
        try {
            String sql = "Update GRUPO4.TABELA_COMPONENTES set ID_FORNECEDOR=" + componente.getCsFornecedor() + "" 
                    + ",STOCK_EXISTENTE=" + componente.getStockDisponivel() + "" + ",PREÇO=" + componente.getPrecoUnitario()
                    + "" + ",DESCRIÇÃO='" + componente.getDescrisao() + "'" + ",CATEGORIA='" + componente.getCategoria() + "'" 
                    + "where CÓDIGO=" + componente.getCpStock();
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            stm.executeUpdate(sql);
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    // Méto para inserir um registro na base
    public static void adicionarLinha(Componentes componente) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stm = conn.prepareStatement("insert into TABELA_COMPONENTES values(?,?,?,?,?,?)");
            stm.setInt(1, componente.getCpStock());
            stm.setInt(2, componente.getCsFornecedor());
            stm.setInt(3, componente.getStockDisponivel());
            stm.setDouble(4, componente.getPrecoUnitario());
            stm.setString(5, componente.getCategoria());
            stm.setString(6, componente.getDescrisao());

            stm.executeUpdate();
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    

    public static void adicionarLinha(Funcionarios funcionario) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stm = conn.prepareStatement("insert into TABELA_FUNCIONARIOS values(?,?,?,?,?,?,?,?)");
            stm.setInt(1, funcionario.getCodigo());
            stm.setString(2, funcionario.getNome());
            stm.setString(3, funcionario.getGenero());
            stm.setString(4, funcionario.getBi());
            stm.setString(5, funcionario.getSenha());
            stm.setString(6, "+258" + funcionario.getContacto());
            stm.setString(7, funcionario.getFuncao());
            stm.setBinaryStream(8, funcionario.getFotoPessoal());
            stm.executeUpdate();
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void adicionarLinha(Categorias categoria) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stm = conn.prepareStatement("insert into TABELA_CATEGORIAS values(?,?,?)");
            stm.setInt(1, categoria.getCodigo());
            stm.setString(2, categoria.getNome());
            stm.setString(3, categoria.getDescricao());
            stm.executeUpdate();
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //int codigo, String nome, String categoria, String localizacao, String email, String telefone
    public static void adicionarLinha(Fornecedores fornecedor) {
        try {
            Connection conn = Conexao.conectar();
            PreparedStatement stm = conn.prepareStatement("insert into TABELA_FORNECEDORES values(?,?,?,?,?,?)");
            stm.setInt(1, fornecedor.getCodigo());
            stm.setString(2, fornecedor.getNome());
            stm.setString(3, fornecedor.getCategoria());
            stm.setString(4, fornecedor.getLocalizacao());
            stm.setString(5, fornecedor.getEmail());
            stm.setString(6, fornecedor.getTelefone());
            stm.executeUpdate();
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public static void adicionarLinha(Movimentos movimento) {
        if (movimento.getTipoMovimento().equalsIgnoreCase("Entrada")) {
            return;
        }
        try {
            String[] data = movimento.getData();
            Connection conn = Conexao.conectar();
            PreparedStatement stm = conn.prepareStatement("insert into TABELA_MOVIMENTOS values(?,?,?,?,?,?,?,?)");
            stm.setInt(1, movimento.getCodigoStock());
            stm.setInt(2, movimento.getQuantidade());
            stm.setDouble(3, movimento.getPrecoTotal());
            stm.setString(4, movimento.getTipoMovimento());
            stm.setString(5, data[0]);
            stm.setString(6, data[1]);
            stm.setString(7, data[2]);
            stm.setInt(8, 1);
            stm.executeUpdate();
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            actualizarLinha(movimento);
        }
    }

    public static void actualizarLinha(Movimentos movimento) {
        // Soma o preco total antigo com preco total actual e actualiza na TABELA_MOVIMENTOS
        try {
            String sql = "Update GRUPO4.TABELA_MOVIMENTOS set PRECO_TOTAL=" + (precoTotalAntigo(movimento.getCodigoStock()) + movimento.getPrecoTotal()) + ",CONTADOR=" + (contagemAntiga(movimento.getCodigoStock()) + 1) + "where CODIGO_STOCK=" + movimento.getCodigoStock();
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            stm.executeUpdate(sql);
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static double precoTotalAntigo(int CodigoStock) {
        double precoTotalAntigo = 0;
        //Busca o preco total antigo na TABELA_MOVIMENTOS
        try {
            String sql = "select * from GRUPO4.TABELA_MOVIMENTOS where CODIGO_STOCK=" + CodigoStock;
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            if (rs.next()) {
                precoTotalAntigo = rs.getDouble("PRECO_TOTAL");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return precoTotalAntigo;
    }

    public static int contagemAntiga(int CodigoStock) {
        int contagemAntiga = 0;
        //Busca o preco total antigo na TABELA_MOVIMENTOS
        try {
            String sql = "select * from GRUPO4.TABELA_MOVIMENTOS where CODIGO_STOCK=" + CodigoStock;
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            if (rs.next()) {
                contagemAntiga = rs.getInt("CONTADOR");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return contagemAntiga;
    }

    public static String getNomeFornecedor(int CodigoFornecedor) {
        String nome = "";
        try {
            String sql = "select * from GRUPO4.TABELA_FORNECEDORES where CÓDIGO=" + CodigoFornecedor;
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            if (rs.next()) {
                nome = rs.getString("NOME");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return nome;
    }

    public static String getCategoria(int CodigoFornecedor) {
        String nome = "";
        try {
            String sql = "select * from GRUPO4.TABELA_FORNECEDORES where CÓDIGO=" + CodigoFornecedor;
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            if (rs.next()) {
                nome = rs.getString("CATEGORIA");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return nome;
    }

    public static void actualizarLinha(Funcionarios funcionario) {
        try {
            String sql = "Update GRUPO4.TABELA_FUNCIONARIOS set NOME='" + funcionario.getNome() + "'" + ",GÉNERO='" + funcionario.getGenero() + "'" + ",BI='" + funcionario.getBi() + "'" + ",SENHA='" + funcionario.getSenha() + "'" + ",CONTACTO='" + funcionario.getContacto() + "'" + ",FUNÇÃO='" + funcionario.getFuncao() + "'"+ ",FOTO='" + funcionario.getFotoPessoal()+ "'"  + "where CÓDIGO=" + funcionario.getCodigo();
            Connection conn = Conexao.conectar();
            PreparedStatement pstm = conn.prepareStatement(sql);
            
            Statement stm = conn.createStatement();
            stm.executeUpdate(sql);
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void actualizarLinha(Categorias categoria) {
        try {
            String sql = "Update GRUPO4.TABELA_CATEGORIAS set NOME='" + categoria.getNome() + "'" + ",DESCRIÇÃO='" + categoria.getDescricao() + "'" + "where CÓDIGO=" + categoria.getCodigo();
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            stm.executeUpdate(sql);
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    

    public static void actualizarLinha(int movimento, int codigoMovimento) {
        try {
            String sql = "Update GRUPO4.TABELA_COMPONENTES set STOCK_EXISTENTE=" + movimento + "where CÓDIGO=" + codigoMovimento;
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            stm.executeUpdate(sql);
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void actualizarLinha(Fornecedores fornecedor) {
        try {
            String sql = "Update GRUPO4.TABELA_FORNECEDORES set NOME='" + fornecedor.getNome() + "'" + ",CATEGORIA='" + fornecedor.getCategoria() + "'" + ",LOCALIZAÇÃO='" + fornecedor.getLocalizacao() + "'" + ",EMAIL='" + fornecedor.getEmail() + "'" + ",TELEFONE='" + fornecedor.getTelefone() + "'" + "where CÓDIGO=" + fornecedor.getCodigo();
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            stm.executeUpdate(sql);
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void actualizarLinha(Login login) {
        try {
            String sql = "Update GRUPO4.TABELA_HISTORICO_LOGIN set NOME='" + login.getUsuario() + "'" + ",SENHA='" + login.getSenhha() + "'" + ",FUNCAO='" + login.getFuncao() + "'where ID=" + 1;
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            stm.executeUpdate(sql);
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    //Método para eliminar um registro da base de dados
    public static void eliminarLinha(String codigo, String tabela) {
        try {

            String sql = "Delete from " + tabela + " where CÓDIGO=" + codigo;
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            stm.executeUpdate(sql);
            Conexao.desconectar(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ImageIcon getFoto(int codigo, JLabel label) {
        String sql = "select * from GRUPO4.TABELA_FUNCIONARIOS where CÓDIGO =" + codigo;
        ImageIcon fotoSmall = null;
        try {
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            if (rs.next()) {
                byte[] fotoBinaria = rs.getBytes("FOTO");
                ImageIcon fotoIcon = new ImageIcon(fotoBinaria);
                Image fotoLarge = fotoIcon.getImage();
                Image foto = fotoLarge.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
                fotoSmall = new ImageIcon(foto);
            }
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return fotoSmall;
    }

    public static String getFuncao() {
        String sql = "select * from GRUPO4.TABELA_HISTORICO_LOGIN";
        String funcao = "";

        try {
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            if (rs.next()) {
                funcao = rs.getString("FUNCAO");
            }
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return funcao;
    }

    public static String[][] getComponente() {
        String sqlComponente = "select * from GRUPO4.TABELA_COMPONENTES";
        int linhas = 0;
        try {
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sqlComponente);
            while (rs.next()) {
                linhas++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String[][] componente = new String[linhas][8];
        try {
            Connection conn = Conexao.conectar();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sqlComponente);
            int i = 0;
            while (rs.next()) {
                componente[i][0] = rs.getString("CÓDIGO");
                componente[i][1] = rs.getString("STOCK_EXISTENTE");
                componente[i][2] = rs.getString("ID_FORNECEDOR");
                componente[i][5] = rs.getString("CATEGORIA");
                componente[i][6] = rs.getString("DESCRIÇÃO");

                int codigoStock = Integer.parseInt(componente[i][0]);
                int CodigoFornecedor = Integer.parseInt(componente[i][2]);

                componente[i][3] = Double.toString(precoTotalAntigo(codigoStock));
                componente[i][4] = Integer.toString(contagemAntiga(codigoStock));
                componente[i][7] = getNomeFornecedor(CodigoFornecedor);
                i++;
            }
            Conexao.desconectar(conn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return componente;
    }

}
