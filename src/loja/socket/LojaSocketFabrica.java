package loja.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import loja.Esteira.EsteiraLoja;
import loja.logger.LoggerUtil;
import loja.model.PedidoFabrica;
import loja.model.Veiculo;

public class LojaSocketFabrica implements Runnable {
    private final String endereco;
    private final int porta;
    private final EsteiraLoja esteira;
    private final LoggerUtil logger;
    private final String idLoja;
    private final int tamanhoLote;
    private volatile boolean rodando = true;

    public LojaSocketFabrica(String endereco, int porta, EsteiraLoja esteira, LoggerUtil logger, String idLoja, int tamanhoLote) {
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

        BufferedReader entrada = new BufferedReader(
                new InputStreamReader(conexao.getInputStream()));

        PrintWriter saida = new PrintWriter(
                conexao.getOutputStream(), true);

        for (int i = 0; i < quantidade; i++) {

            saida.println("REQUEST_VEHICLE");

            String resposta = entrada.readLine();

            if (resposta == null) break;

            if (resposta.startsWith("OK|")) {
                String dados = resposta.substring(3);
                String[] partes = dados.split(";");

                List<String> cadeia = new ArrayList<>();
                cadeia.add("Fabrica");

                Veiculo veiculo = new Veiculo(
                        partes[1] + "-" + partes[2],
                        cadeia);

                esteira.colocar(veiculo);
                logger.logRecebimento(veiculo, endereco + ":" + porta);
                recebidos++;
            }
        }

    } catch (Exception e) {
        System.err.println("ClienteSocketFabrica: erro ao conectar na fábrica: " + e.getMessage());
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
