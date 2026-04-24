package model;

public class veiculo {
    private static int contador = 0;

    private int id;
    private String cor;
    private String tipo;
    private int idEstacao;
    private int idFuncionario;
    private int posicaoEsteiraProducao;

    public veiculo(int idEstacao, int idFuncionario) {
        this.id = ++contador;

        this.cor = (id % 3 == 0) ? "VERMELHO" : (id % 3 == 1) ? "VERDE" : "AZUL";
        this.tipo = (id % 2 == 0) ? "SUV" : "SEDAN";

        this.idEstacao = idEstacao;
        this.idFuncionario = idFuncionario;
    }

    public int getId() {
        return id;
    }

    public String getCor() {
        return cor;
    }

    public String getTipo() {
        return tipo;
    }

    public int getIdEstacao() {
        return idEstacao;
    }

    public int getIdFuncionario() {
        return idFuncionario;
    }

    public int getPosicaoEsteiraProducao() {
        return posicaoEsteiraProducao;
    }

    public void setPosicaoEsteiraProducao(int posicaoEsteiraProducao) {
        this.posicaoEsteiraProducao = posicaoEsteiraProducao;
    }

    @Override
    public String toString() {
        return "Veiculo{id=" + id + ", cor=" + cor + ", tipo=" + tipo +
                ", estacao=" + idEstacao + ", func=" + idFuncionario + "}";
    }
}