package cliente;

import loja.Pedido;
import loja.Vehicle;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

/**
 * Gerencia a comunicação entre o cliente e uma loja via Socket TCP.
 *
 * Protocolo (compatível com ServerSocketClientes.java da loja):
 *  1. Cliente abre conexão TCP com a loja.
 *  2. Cliente envia um objeto {@link Pedido} serializado.
 *  3. Loja responde com um objeto {@link Vehicle} serializado, ou null
 *     se não houver veículo disponível.
 *  4. Conexão é fechada após cada transação.
 *
 * Controle de concorrência:
 *  - Um Semáforo global limita o número máximo de conexões simultâneas
 *    abertas ao conjunto de lojas (evita sobrecarga de sockets).
 *  - Cada loja pode receber no máximo MAX_CONEXOES_POR_LOJA conexões
 *    simultâneas do processo cliente (controlado por semáforo por host:porta).
 *
 * Todos os campos static são inicializados uma vez e são thread-safe.
 */
public class ConexaoLoja {

    // Timeout de conexão e leitura em milissegundos
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS    = 8000;

    // Máximo de conexões simultâneas globais abertas pelo processo cliente
    private static final int MAX_CONEXOES_GLOBAIS    = 10;

    // Semáforo global de conexões (único lock do tipo Semaphore permitido)
    private static final Semaphore semConexoesGlobais =
            new Semaphore(MAX_CONEXOES_GLOBAIS, true);

    // Construtor privado — classe utilitária com métodos estáticos
    private ConexaoLoja() {}

    // -----------------------------------------------------------------------
    // Método principal: tenta comprar um veículo de uma loja
    //
    // Retorna o Vehicle recebido, ou null se:
    //  - A loja não tem veículo disponível (loja retornou null)
    //  - Erro de conexão/comunicação
    // -----------------------------------------------------------------------
    public static Vehicle comprar(String host, int porta, String model, String clienteId) {
        try {
            // Aguarda permissão de conexão (controle de concorrência)
            semConexoesGlobais.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[ConexaoLoja][%s] Interrompido ao aguardar semaforo de conexao.%n", clienteId);
            return null;
        }

        try {
            return executarCompra(host, porta, model, clienteId);
        } finally {
            // Libera a permissão independente do resultado
            semConexoesGlobais.release();
        }
    }

    // -----------------------------------------------------------------------
    // Executa a comunicação socket de fato
    // -----------------------------------------------------------------------
    private static Vehicle executarCompra(String host, int porta, String model, String clienteId) {
        try (Socket socket = new Socket()) {

            // Configura timeouts antes de conectar
            socket.connect(new java.net.InetSocketAddress(host, porta), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);

            // Ordem de criação importa no Java Serialization:
            // ObjectOutputStream DEVE ser criado antes do ObjectInputStream
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

                // Flush obrigatório antes de ler (envia o header de serialização)
                out.flush();

                // Monta e envia o pedido
                Pedido pedido = new Pedido(Pedido.Type.BUY, model, clienteId);
                out.writeObject(pedido);
                out.flush();

                // Aguarda resposta da loja
                Object resposta = in.readObject();

                if (resposta instanceof Vehicle) {
                    Vehicle v = (Vehicle) resposta;
                    System.out.printf("[ConexaoLoja][%s] Veiculo recebido: id=%s modelo=%s de %s:%d%n",
                            clienteId, v.getId(), v.getModel(), host, porta);
                    return v;
                } else {
                    // null ou objeto inesperado — loja sem estoque
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
