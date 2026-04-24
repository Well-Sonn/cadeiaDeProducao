package loja;

public class GerenciadorPedidos {
    private final EsteiraLoja esteira;
    private final ClientSocketFactory clienteFabrica;
    private final LoggerUtil logger;
    private final String idLoja;

    public GerenciadorPedidos(EsteiraLoja esteira, ClientSocketFactory clienteFabrica, LoggerUtil logger, String idLoja) {
        this.esteira = esteira;
        this.clienteFabrica = clienteFabrica;
        this.logger = logger;
        this.idLoja = idLoja;
    }

    public Veiculo processarPedido(Pedido pedido) {
        try {
            if (esteira.size() == 0) {
                clienteFabrica.solicitarVeiculos(1);
            }
            Veiculo veiculo = esteira.retirar();
            logger.logVenda(veiculo, pedido);
            return veiculo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
