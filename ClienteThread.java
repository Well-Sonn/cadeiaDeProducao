package cliente;

import loja.Pedido;
import loja.Vehicle;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class ClienteThread implements Runnable {

    private static final int MIN_COMPRAS   = 5;   
    private static final int MAX_COMPRAS   = 15;   
    private static final int WAIT_BASE_MS  = 2000; 
    private static final int WAIT_JITTER   = 1000;
    private static final int DELAY_BASE_MS = 300;  
    private static final int DELAY_JITTER  = 700;  

    private static final String[] MODELS = { "SUV", "Sedan" };

    private final int                          clienteId;
    private final String                       clienteStringId;
    private final List<ClientMain.EnderecoLoja> lojas;
    private final Garagem                      garagem;
    private final ClientLogger                 logger;
    private final Random                       random;
    private final int                          totalCompras; 

    private final Semaphore estadoSemaforo;


    public ClienteThread(int clienteId, List<ClientMain.EnderecoLoja> lojas) {
        this.clienteId       = clienteId;
        this.clienteStringId = "cliente-" + clienteId;
        this.lojas           = lojas;
        this.garagem         = new Garagem(100);          
        this.logger          = new ClientLogger(clienteStringId);
        this.random          = new Random(clienteId * 31L);
        this.totalCompras    = MIN_COMPRAS + random.nextInt(MAX_COMPRAS - MIN_COMPRAS + 1);
        this.estadoSemaforo  = new Semaphore(1);
    }

    @Override
    public void run() {
        System.out.printf("[%s] Iniciado. Meta: %d compras.%n", clienteStringId, totalCompras);

        int comprasRealizadas = 0;

        while (comprasRealizadas < totalCompras) {

            ClientMain.EnderecoLoja loja  = escolherLojaAleatoria();
            String                  model = escolherModeloAleatorio();

            try {
                estadoSemaforo.acquire();

                boolean comprado = tentarCompra(loja, model);

                if (comprado) {
                    comprasRealizadas++;
                    System.out.printf("[%s] Compra %d/%d concluida. Garagem: %d veiculos.%n",
                            clienteStringId, comprasRealizadas, totalCompras, garagem.tamanho());

                    estadoSemaforo.release();
                    pausaAleatoria(DELAY_BASE_MS, DELAY_JITTER);

                } else {
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

        System.out.printf("[%s] Finalizado. Total na garagem: %d veiculos.%n",
                clienteStringId, garagem.tamanho());
        logger.logResumo(garagem.listar());
    }

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
