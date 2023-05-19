
package digitalwerehouse;

import digitalwerehouse.DB.Modificar;


public class Fornecedores {
     private int codigo;
    private String nome;
    private String categoria;
    private String localizacao;
    private String email;
    private String telefone;


    public Fornecedores(int codigo, String nome, String categoria, String localizacao, String email, String telefone) {
        this.codigo = codigo;
        this.nome = nome;
        this.categoria = categoria;
        this.localizacao = localizacao;
        this.email = email;
        this.telefone = telefone;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }
}
