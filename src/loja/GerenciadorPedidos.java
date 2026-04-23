package loja;

public class GerenciadorPedidos {
    private final EsteiraLoja esteira;
    private final ClientSocketFactory factoryClient;
    private final LoggerUtil logger;
    private final String storeId;

    public GerenciadorPedidos(EsteiraLoja esteira, ClientSocketFactory factoryClient, LoggerUtil logger, String storeId) {
        this.esteira = esteira;
        this.factoryClient = factoryClient;
        this.logger = logger;
        this.storeId = storeId;
    }

    public Vehicle handlePedido(Pedido pedido) {
        try {
            if (esteira.size() == 0) {
                factoryClient.requestVehicles(1);
            }
            Vehicle v = esteira.take();
            logger.logVenda(v, pedido);
            return v;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
