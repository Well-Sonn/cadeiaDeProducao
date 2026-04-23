package loja;

public class StoreMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: java loja.StoreMain <storeId> <clientPort> <factoryHost> <factoryPort> [bufferCapacity]");
            return;
        }
        String storeId = args[0];
        int clientPort = Integer.parseInt(args[1]);
        String factoryHost = args[2];
        int factoryPort = Integer.parseInt(args[3]);
        int bufferCapacity = args.length >= 5 ? Integer.parseInt(args[4]) : 5;
        int batchSize = Math.max(1, bufferCapacity);
        EsteiraLoja esteira = new EsteiraLoja(bufferCapacity);
        LoggerUtil logger = new LoggerUtil(storeId);
        ClientSocketFactory factoryClient = new ClientSocketFactory(factoryHost, factoryPort, esteira, logger, storeId, batchSize);
        GerenciadorPedidos gerenciador = new GerenciadorPedidos(esteira, factoryClient, logger, storeId);
        ServerSocketClientes serverClientes = new ServerSocketClientes(clientPort, gerenciador);
        Thread tFactory = new Thread(factoryClient, "FactoryClient-" + storeId);
        Thread tServer = new Thread(serverClientes, "ServerClientes-" + storeId);
        tFactory.start();
        tServer.start();
        System.out.println("Store " + storeId + " started. ClientPort=" + clientPort + " Factory=" + factoryHost + ":" + factoryPort + " Buffer=" + bufferCapacity);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down store " + storeId);
            serverClientes.shutdown();
            factoryClient.shutdown();
            logger.close();
        }));
        tServer.join();
        tFactory.join();
    }
}
