package logs;

import java.io.IOException;

public class logEnvio {

    private int idVeiculo;
    private String cor;
    private String tipo;
    private String destino;
    private long timestamp;

    public logEnvio(int idVeiculo, String cor, String tipo, String destino) {
        this.idVeiculo = idVeiculo;
        this.cor = cor;
        this.tipo = tipo;
        this.destino = destino;
        this.timestamp = System.currentTimeMillis();
    }

    public String toJson() {
        return "{" +
                "\"idVeiculo\":" + idVeiculo + "," +
                "\"cor\":\"" + logWriter.escapeJson(cor) + "\"," +
                "\"tipo\":\"" + logWriter.escapeJson(tipo) + "\"," +
                "\"destino\":\"" + logWriter.escapeJson(destino) + "\"," +
                "\"timestamp\":" + timestamp +
                "}";
    }

    public void salvarJson() {
        try {
            logWriter.appendJsonObject("logs/shipping_log.json", toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}