package digitalwerehouse;

public class Movimentos {

    protected int codigoStock;
    protected int quantidade;
    protected double precoTotal;
    protected String tipoMovimento;
    protected String[] data = new String[3];
    protected int contador = 0;
    protected double precoTotalGlobalEntradas = 0;
    protected double precoTotalGlobalSaidas = 0;


    public Movimentos(String tipoMovimento, int codigoStock, int quantidade, int contador, String[] data, double precoTotal) {
        this.tipoMovimento = tipoMovimento;
        this.codigoStock = codigoStock;
        this.contador = contador;
        this.quantidade = quantidade;
        this.data = data;
        this.precoTotal = precoTotal * quantidade;
    }

    public Movimentos(String tipoMovimento, int codigoStock, int quantidade, int contador, String[] data, double precoTotal, double precoTotalGlobalEntradas, double precoTotalGlobalSaidas) {
        this(tipoMovimento, codigoStock, quantidade, contador, data, precoTotal);
        this.precoTotalGlobalEntradas = precoTotalGlobalEntradas;
        this.precoTotalGlobalSaidas = precoTotalGlobalSaidas;
    }

    public String getTipoMovimento() {
        return tipoMovimento;
    }

    public int getCodigoStock() {
        return codigoStock;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String[] getData() {
        return data;
    }

    public double getPrecoTotal() {
        return precoTotal;
    }
    
    

}
