package loja.socket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import loja.GerenciadorPedidos;
import loja.Pedido;
import loja.model.Veiculo;

public class ServerSocketClientes implements Runnable {
    private final int porta;
    private final GerenciadorPedidos gerenciador;
    private volatile boolean rodando = true;
    private ServerSocket servidor;

    public ServerSocketClientes(int porta, GerenciadorPedidos gerenciador) {
        this.porta = porta;
        this.gerenciador = gerenciador;
    }

    public void shutdown() {
        rodando = false;
        try {
            if (servidor != null && !servidor.isClosed()) servidor.close();
        } catch (Exception e) { /* ignorar */ }
    }

    @Override
    public void run() {
        try (ServerSocket s = new ServerSocket(porta)) {
            this.servidor = s;
            System.out.println("ServerSocketClientes: ouvindo na porta " + porta);
            while (rodando) {
                Socket clienteSocket = s.accept();
                new Thread(() -> handleClient(clienteSocket)).start();
            }
        } catch (Exception e) {
            if (rodando) System.err.println("ServerSocketClientes: erro: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (Socket conexao = socket;
             ObjectOutputStream saida = new ObjectOutputStream(conexao.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())) {
            saida.flush();
            Object obj = entrada.readObject();
            if (obj instanceof Pedido) {
                Pedido pedido = (Pedido) obj;
                System.out.println("Pedido recebido: " + pedido);
                Veiculo vehicle = gerenciador.processarPedido(pedido);
                saida.writeObject(vehicle);
                saida.flush();
            } else {
                System.err.println("Requisição desconhecida do cliente: " + obj);
                saida.writeObject(null);
                saida.flush();
            }
        } catch (Exception e) {
            System.err.println("Erro ao atender cliente: " + e.getMessage());
        }
    }
}
