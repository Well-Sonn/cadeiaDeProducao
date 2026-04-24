package cliente;

import loja.Pedido;
import loja.Vehicle;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Representa um cliente individual como uma Thread.
 *
 * Comportamento:
 *  - Cada cliente escolhe ALEATORIAMENTE a loja a cada tentativa de compra.
 *  - Escolhe ALEATORIAMENTE o modelo do veículo (SUV ou Sedan).
 *  - Realiza entre MIN_COMPRAS e MAX_COMPRAS compras ao total.
 *  - Caso a loja não tenha veículo disponível (retorna null), o cliente
 *    entra em estado de ESPERA por WAIT_BASE_MS + variação aleatória
 *    e então tenta novamente (mesma compra não é perdida, só adiada).
 *  - Cada veículo comprado é armazenado na Garagem do cliente.
 *
 * Sincronização: APENAS Semáforos (nenhum synchronized/ReentrantLock).
 */
public class ClienteThread implements Runnable {

    // -----------------------------------------------------------------------
    // Constantes de configuração
    // -----------------------------------------------------------------------
    private static final int MIN_COMPRAS   = 5;    // mínimo de compras por cliente
    private static final int MAX_COMPRAS   = 15;   // máximo de compras por cliente
    private static final int WAIT_BASE_MS  = 2000; // espera base quando loja sem estoque
    private static final int WAIT_JITTER   = 1000; // variação aleatória do wait
    private static final int DELAY_BASE_MS = 300;  // pausa entre compras bem-sucedidas
    private static final int DELAY_JITTER  = 700;  // variação aleatória da pausa

    private static final String[] MODELS = { "SUV", "Sedan" };

    // -----------------------------------------------------------------------
    // Atributos de instância
    // -----------------------------------------------------------------------
    private final int                          clienteId;
    private final String                       clienteStringId;
    private final List<ClientMain.EnderecoLoja> lojas;
    private final Garagem                      garagem;
    private final ClientLogger                 logger;
    private final Random                       random;
    private final int                          totalCompras;  // definido aleatoriamente no construtor

    /**
     * Semáforo de estado: garante que o cliente não tente nova compra
     * enquanto ainda está processando/registrando a atual.
     * (1 permissão = cliente pronto para a próxima ação)
     */
    private final Semaphore estadoSemaforo;

    // -----------------------------------------------------------------------
    // Construtor
    // -----------------------------------------------------------------------
    public ClienteThread(int clienteId, List<ClientMain.EnderecoLoja> lojas) {
        this.clienteId       = clienteId;
        this.clienteStringId = "cliente-" + clienteId;
        this.lojas           = lojas;
        this.garagem         = new Garagem(100);          // garagem com capacidade para 100 veículos
        this.logger          = new ClientLogger(clienteStringId);
        this.random          = new Random(clienteId * 31L); // seed por cliente para reprodutibilidade
        this.totalCompras    = MIN_COMPRAS + random.nextInt(MAX_COMPRAS - MIN_COMPRAS + 1);
        this.estadoSemaforo  = new Semaphore(1);
    }

    // -----------------------------------------------------------------------
    // Execução principal da thread
    // -----------------------------------------------------------------------
    @Override
    public void run() {
        System.out.printf("[%s] Iniciado. Meta: %d compras.%n", clienteStringId, totalCompras);

        int comprasRealizadas = 0;

        while (comprasRealizadas < totalCompras) {
            // Escolha aleatória de loja e modelo a cada tentativa
            ClientMain.EnderecoLoja loja  = escolherLojaAleatoria();
            String                  model = escolherModeloAleatorio();

            try {
                // Semáforo garante que processamento de uma compra seja atômico por cliente
                estadoSemaforo.acquire();

                boolean comprado = tentarCompra(loja, model);

                if (comprado) {
                    comprasRealizadas++;
                    System.out.printf("[%s] Compra %d/%d concluida. Garagem: %d veiculos.%n",
                            clienteStringId, comprasRealizadas, totalCompras, garagem.tamanho());

                    // Pequena pausa entre compras bem-sucedidas (simula decisão do cliente)
                    estadoSemaforo.release();
                    pausaAleatoria(DELAY_BASE_MS, DELAY_JITTER);

                } else {
                    // Loja sem estoque: cliente entra em espera antes de tentar novamente
                    System.out.printf("[%s] Sem estoque em %s para modelo %s. Aguardando...%n",
                            clienteStringId, loja, model);
                    logger.logEspera(loja.toString(), model);
                    estadoSemaforo.release();
                    pausaAleatoria(WAIT_BASE_MS, WAIT_JITTER);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.printf("[%s] Thread interrompida.%n", clienteStringId);
                break;
            }
        }

        // Resumo final do cliente
        System.out.printf("[%s] Finalizado. Total na garagem: %d veiculos.%n",
                clienteStringId, garagem.tamanho());
        logger.logResumo(garagem.listar());
    }

    // -----------------------------------------------------------------------
    // Tenta comprar um veículo de uma loja específica
    // Retorna true se compra bem-sucedida, false se loja sem estoque
    // -----------------------------------------------------------------------
    private boolean tentarCompra(ClientMain.EnderecoLoja loja, String model) {
        Vehicle veiculo = ConexaoLoja.comprar(loja.host, loja.porta, model, clienteStringId);

        if (veiculo == null) {
            return false;
        }

        try {
            garagem.adicionar(veiculo);
            logger.logCompra(veiculo, loja.toString(), garagem.tamanho());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private ClientMain.EnderecoLoja escolherLojaAleatoria() {
        return lojas.get(random.nextInt(lojas.size()));
    }

    private String escolherModeloAleatorio() {
        return MODELS[random.nextInt(MODELS.length)];
    }

    private void pausaAleatoria(int baseMs, int jitterMs) throws InterruptedException {
        int pausa = baseMs + random.nextInt(jitterMs + 1);
        Thread.sleep(pausa);
    }
}
