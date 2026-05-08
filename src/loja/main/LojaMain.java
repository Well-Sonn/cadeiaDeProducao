package loja.main;

import loja.Esteira.EsteiraLoja;
import loja.controle.GerenciadorPedidos;
import loja.logger.LoggerUtil;
import loja.socket.LojaSocketFabrica;
import loja.socket.ClienteSocketLoja;

public class LojaMain {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Uso: java loja.main.LojaMain <lojaId> <portaClientes> <enderecoFabrica> <portaFabrica> [capacidadeBuffer]");
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
        LojaSocketFabrica clienteFabrica = new LojaSocketFabrica(fabricaEndereco, fabricaPorta, esteira, logger, idLoja, tamanhoLote);
        GerenciadorPedidos gerenciador = new GerenciadorPedidos(esteira, clienteFabrica, logger, idLoja);
        ClienteSocketLoja serverClientes = new ClienteSocketLoja(portaClientes, gerenciador);
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
