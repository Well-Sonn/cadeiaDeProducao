package loja.socket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import loja.model.Veiculo;
import loja.util.FactoryRequest;

public class FactoryTeste implements Runnable {
    private final int porta;
    private volatile boolean rodando = true;

    public FactoryTeste(int porta) { this.porta = porta; }

    @Override
    public void run() {
        try (ServerSocket s = new ServerSocket(porta)) {
            System.out.println("MockFactory ouvindo na porta " + porta);
            while (rodando) {
                Socket conexao = s.accept();
                new Thread(() -> handleStoreConnection(conexao)).start();
            }
        } catch (Exception e) {
            System.err.println("MockFactory erro: " + e.getMessage());
        }
    }

    private void handleStoreConnection(Socket socket) {
        try (Socket conexao = socket;
             ObjectOutputStream saida = new ObjectOutputStream(conexao.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())) {
            saida.flush();
            Object req = entrada.readObject();
            if (req instanceof FactoryRequest) {
                FactoryRequest fr = (FactoryRequest) req;
                int quantidade = fr.getQuantidade();
                System.out.println("Fábrica recebeu requisição da loja=" + fr.getIdLoja() + " qtd=" + quantidade);
                for (int i = 0; i < quantidade; i++) {
                    List<String> cadeia = Arrays.asList("Montagem", "Pintura", "Inspecao");
                    Veiculo veiculo = new Veiculo("ModeloX", cadeia);
                    saida.writeObject(veiculo);
                    saida.flush();
                }
                saida.writeObject("END");
                saida.flush();
            } else {
                saida.writeObject("NONE");
                saida.flush();
            }
        } catch (Exception e) {
            System.err.println("Erro de conecao FactoryTeste: " + e.getMessage());
        }
    }

    public void shutdown() { rodando = false; }

    public static void main(String[] args) throws Exception {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 9000;
        FactoryTeste server = new FactoryTeste(porta);
        new Thread(server).start();
    }
}
