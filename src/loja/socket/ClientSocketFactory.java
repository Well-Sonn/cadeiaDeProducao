package loja.socket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import loja.FactoryRequest;
import loja.buffer.EsteiraLoja;
import loja.logs.LoggerUtil;
import loja.model.Veiculo;

public class ClientSocketFactory implements Runnable {
    private final String endereco;
    private final int porta;
    private final EsteiraLoja esteira;
    private final LoggerUtil logger;
    private final String idLoja;
    private final int tamanhoLote;
    private volatile boolean rodando = true;

    public ClientSocketFactory(String endereco, int porta, EsteiraLoja esteira, LoggerUtil logger, String idLoja, int tamanhoLote) {
        this.endereco = endereco;
        this.porta = porta;
        this.esteira = esteira;
        this.logger = logger;
        this.idLoja = idLoja;
        this.tamanhoLote = Math.max(1, tamanhoLote);
    }

    public void shutdown() { rodando = false; }

    public int solicitarVeiculos(int quantidade) {
        int recebidos = 0;
        try (Socket conexao = new Socket(endereco, porta)) {
            conexao.setSoTimeout(15000);
            ObjectOutputStream saida = new ObjectOutputStream(conexao.getOutputStream());
            saida.flush();
            ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream());
            FactoryRequest requisicao = new FactoryRequest(idLoja, quantidade);
            saida.writeObject(requisicao);
            saida.flush();
            for (int i = 0; i < quantidade; i++) {
                Object obj = entrada.readObject();
                if (obj == null) break;
                if (obj instanceof Veiculo) {
                    Veiculo veiculo = (Veiculo) obj;
                    try {
                        esteira.colocar(veiculo);
                        logger.logRecebimento(veiculo, endereco + ":" + porta);
                        recebidos++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else if (obj instanceof String) {
                    String s = (String) obj;
                    if ("NONE".equals(s) || "END".equals(s)) {
                        break;
                    }
                } else {
                    // ignorar desconhecido
                }
            }
        } catch (Exception e) {
            System.err.println("ClientSocketFactory: erro ao conectar na fábrica: " + e.getMessage());
        }
        return recebidos;
    }

    @Override
    public void run() {
        while (rodando) {
            try {
                int atual = esteira.size();
                if (atual < tamanhoLote) {
                    int necessarios = tamanhoLote - atual;
                    int obtive = solicitarVeiculos(necessarios);
                    if (obtive == 0) {
                        Thread.sleep(5000);
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
