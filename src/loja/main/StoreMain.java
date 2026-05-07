package loja.main;

import loja.GerenciadorPedidos;
import loja.buffer.EsteiraLoja;
import loja.logs.LoggerUtil;
import loja.socket.ClientSocketFactory;
import loja.socket.ServerSocketClientes;

public class StoreMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: java loja.StoreMain <storeId> <clientPort> <factoryHost> <factoryPort> [bufferCapacity]");
            return;
        }
        String idLoja = args[0];
        int portaClientes = Integer.parseInt(args[1]);
        String fabricaEndereco = args[2];
        int fabricaPorta = Integer.parseInt(args[3]);
        int capacidadeBuffer = args.length >= 5 ? Integer.parseInt(args[4]) : 5;
        int tamanhoLote = Math.max(1, capacidadeBuffer);
        EsteiraLoja esteira = new EsteiraLoja(capacidadeBuffer);
        LoggerUtil logger = new LoggerUtil(idLoja);
        ClientSocketFactory clienteFabrica = new ClientSocketFactory(fabricaEndereco, fabricaPorta, esteira, logger, idLoja, tamanhoLote);
        GerenciadorPedidos gerenciador = new GerenciadorPedidos(esteira, clienteFabrica, logger, idLoja);
        ServerSocketClientes serverClientes = new ServerSocketClientes(portaClientes, gerenciador);
        Thread tFactory = new Thread(clienteFabrica, "ClienteFabrica-" + idLoja);
        Thread tServer = new Thread(serverClientes, "ServerClientes-" + idLoja);
        tFactory.start();
        tServer.start();
        System.out.println("Loja " + idLoja + " iniciada. PortaClientes=" + portaClientes + " Fabrica=" + fabricaEndereco + ":" + fabricaPorta + " Buffer=" + capacidadeBuffer);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Finalizando loja " + idLoja);
            serverClientes.shutdown();
            clienteFabrica.shutdown();
            logger.close();
        }));
        tServer.join();
        tFactory.join();
    }
}
