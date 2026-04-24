package logs;

import java.io.IOException;

public class logProducao {

    private int idVeiculo;
    private String cor;
    private String tipo;
    private int idEstacao;
    private int idFuncionario;
    private int posicaoEsteira;

    public logProducao(int idVeiculo, String cor, String tipo, int idEstacao, int idFuncionario, int posicaoEsteira) {
        this.idVeiculo = idVeiculo;
        this.cor = cor;
        this.tipo = tipo;
        this.idEstacao = idEstacao;
        this.idFuncionario = idFuncionario;
        this.posicaoEsteira = posicaoEsteira;
    }

    public int getIdVeiculo() {
        return idVeiculo;
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

    public int getPosicaoEsteira() {
        return posicaoEsteira;
    }

    public String toJson() {
        return "{" +
                "\"idVeiculo\":" + idVeiculo + "," +
                "\"cor\":\"" + logWriter.escapeJson(cor) + "\"," +
                "\"tipo\":\"" + logWriter.escapeJson(tipo) + "\"," +
                "\"idEstacao\":" + idEstacao + "," +
                "\"idFuncionario\":" + idFuncionario + "," +
                "\"posicaoEsteira\":" + posicaoEsteira +
                "}";
    }

    public void salvarJson() {
        try {
            logWriter.appendJsonObject("logs/production_log.json", toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
