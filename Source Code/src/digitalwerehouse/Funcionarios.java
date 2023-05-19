package digitalwerehouse;

import java.io.FileInputStream;

public class Funcionarios {

    private int codigo;
    private String nome;
    private String genero;
    private String bi;
    private String senha;
    private String contacto;
    private String funcao;
    FileInputStream fotoPessoal = null;

    public Funcionarios(int codigo, String nome, String genero, String bi, String senha, String contacto, String funcao, FileInputStream fotoPessoal) {
        this.codigo = codigo;
        this.nome = nome;
        this.genero = genero;
        this.bi = bi;
        this.senha = senha;
        this.contacto = contacto;
        this.funcao = funcao;
        this.fotoPessoal = fotoPessoal;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getGenero() {
        return genero;
    }

    public String getBi() {
        return bi;
    }

    public String getSenha() {
        return senha;
    }

    public String getContacto() {
        return contacto;
    }

    public String getFuncao() {
        return funcao;
    }

    public FileInputStream getFotoPessoal() {
        return fotoPessoal;
    }
    
}
