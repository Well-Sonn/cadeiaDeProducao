package cliente;

import loja.Pedido;
import loja.Vehicle;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;


public class ConexaoLoja {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS    = 8000;

    private static final int MAX_CONEXOES_GLOBAIS    = 10;

    private static final Semaphore semConexoesGlobais =
            new Semaphore(MAX_CONEXOES_GLOBAIS, true);

    private ConexaoLoja() {}

    public static Vehicle comprar(String host, int porta, String model, String clienteId) {
        try {
            semConexoesGlobais.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[ConexaoLoja][%s] Interrompido ao aguardar semaforo de conexao.%n", clienteId);
            return null;
        }

        try {
            return executarCompra(host, porta, model, clienteId);
        } finally {
            semConexoesGlobais.release();
        }
    }

    private static Vehicle executarCompra(String host, int porta, String model, String clienteId) {
        try (Socket socket = new Socket()) {

            socket.connect(new java.net.InetSocketAddress(host, porta), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);

            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

                out.flush();

                Pedido pedido = new Pedido(Pedido.Type.BUY, model, clienteId);
                out.writeObject(pedido);
                out.flush();

                Object resposta = in.readObject();

                if (resposta instanceof Vehicle) {
                    Vehicle v = (Vehicle) resposta;
                    System.out.printf("[ConexaoLoja][%s] Veiculo recebido: id=%s modelo=%s de %s:%d%n",
                            clienteId, v.getId(), v.getModel(), host, porta);
                    return v;
                } else {
                    System.out.printf("[ConexaoLoja][%s] Loja %s:%d sem estoque para modelo=%s.%n",
                            clienteId, host, porta, model);
                    return null;
                }
            }

        } catch (ConnectException e) {
            System.err.printf("[ConexaoLoja][%s] Nao foi possivel conectar a %s:%d - Loja offline? (%s)%n",
                    clienteId, host, porta, e.getMessage());
            return null;
        } catch (SocketTimeoutException e) {
            System.err.printf("[ConexaoLoja][%s] Timeout ao comunicar com %s:%d%n", clienteId, host, porta);
            return null;
        } catch (Exception e) {
            System.err.printf("[ConexaoLoja][%s] Erro ao comprar em %s:%d - %s%n",
                    clienteId, host, porta, e.getMessage());
            return null;
        }
    }
}
