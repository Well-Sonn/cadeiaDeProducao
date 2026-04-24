package cliente;

import java.util.ArrayList;
import java.util.List;

/**
 * Ponto de entrada do módulo cliente.
 *
 * Uso:
 *   java cliente.ClientMain <host1:port1> <host2:port2> <host3:port3>
 *
 * Exemplo (3 lojas em máquinas distintas):
 *   java cliente.ClientMain 192.168.1.10:6001 192.168.1.11:6001 192.168.1.12:6001
 *
 * Exemplo (teste local, 3 lojas na mesma máquina em portas diferentes):
 *   java cliente.ClientMain localhost:6001 localhost:6002 localhost:6003
 *
 * Cria 20 threads de clientes, cada uma com sua própria Garagem.
 * Toda a sincronização é feita exclusivamente com Semáforos.
 */
public class ClientMain {

    public static final int NUM_CLIENTES = 20;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("=================================================");
            System.out.println("  Modulo CLIENTE - Cadeia de Producao de Veiculos");
            System.out.println("=================================================");
            System.out.println("Uso: java cliente.ClientMain <host1:porta1> [host2:porta2] ...");
            System.out.println("Exemplo: java cliente.ClientMain 192.168.1.10:6001 192.168.1.11:6001 192.168.1.12:6001");
            System.exit(1);
        }

        // Parseia endereços das lojas a partir dos argumentos
        List<EnderecoLoja> lojas = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            String[] partes = arg.split(":");
            if (partes.length != 2) {
                System.err.println("[ClientMain] Argumento invalido ignorado: " + arg + " (esperado: host:porta)");
                continue;
            }
            try {
                String host = partes[0].trim();
                int porta   = Integer.parseInt(partes[1].trim());
                lojas.add(new EnderecoLoja(i + 1, host, porta));
                System.out.println("[ClientMain] Loja registrada -> loja-" + (i + 1) + " em " + host + ":" + porta);
            } catch (NumberFormatException e) {
                System.err.println("[ClientMain] Porta invalida para: " + arg);
            }
        }

        if (lojas.isEmpty()) {
            System.err.println("[ClientMain] Nenhum endereco de loja valido fornecido. Encerrando.");
            System.exit(1);
        }

        System.out.println("[ClientMain] Iniciando " + NUM_CLIENTES + " clientes conectando a " + lojas.size() + " loja(s)...");

        // Cria e dispara as 20 threads de clientes
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= NUM_CLIENTES; i++) {
            ClienteThread cliente = new ClienteThread(i, lojas);
            Thread t = new Thread(cliente, "Cliente-" + i);
            t.setDaemon(false);
            threads.add(t);
            t.start();
        }

        // Aguarda todos os clientes terminarem
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("[ClientMain] Todos os clientes finalizaram.");
    }

    // ---------------------------------------------------------------------------
    // Classe auxiliar imutável para encapsular endereço de uma loja
    // ---------------------------------------------------------------------------
    public static class EnderecoLoja {
        public final int    lojaId;
        public final String host;
        public final int    porta;

        public EnderecoLoja(int lojaId, String host, int porta) {
            this.lojaId = lojaId;
            this.host   = host;
            this.porta  = porta;
        }

        @Override
        public String toString() {
            return "loja-" + lojaId + "[" + host + ":" + porta + "]";
        }
    }
}
