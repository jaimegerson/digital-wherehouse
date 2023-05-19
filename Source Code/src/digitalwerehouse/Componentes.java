
package digitalwerehouse;


public class Componentes {
    private int cpStock;
    private int csFornecedor;
    private String categoria;
    private int stockDisponivel;
    private String descrisao;
    private double precoUnitario;


    public Componentes(int cpStock, int csFornecedor, String categoria, int stockDisponivel, String descrisao, double precoUnitario) {
        this.cpStock = cpStock;
        this.csFornecedor = csFornecedor;
        this.categoria = categoria;
        this.stockDisponivel = stockDisponivel;
        this.descrisao = descrisao;
        this.precoUnitario = precoUnitario;
    }

    public int getCpStock() {
        return cpStock;
    }

    public int getCsFornecedor() {
        return csFornecedor;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getStockDisponivel() {
        return stockDisponivel;
    }

    public String getDescrisao() {
        return descrisao;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    
}
