package digitalwerehouse.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    public static Connection conectar() {
        String dbName = "digitalwerehouse";
        String user = "GRUPO4";
        String password = "1234";
        
        String linkActual = System.getProperty("user.dir");
        //String urlActual  = "jdbc:derby:" + linkActual + "/" + dbName;
        String url = "jdbc:derby://localhost:1527/digitalwerehouse";
        try {
            System.out.println("Base de Dados conectada com sucesso.. .");
            return DriverManager.getConnection(url, user, password);  
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void desconectar(Connection conn) {
        try {
        conn.close();
            System.out.println("Base de Dados desconectada com sucesso.. .");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
