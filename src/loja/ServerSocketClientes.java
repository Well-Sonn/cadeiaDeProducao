package loja;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketClientes implements Runnable {
    private final int port;
    private final GerenciadorPedidos gerenciador;
    private volatile boolean running = true;
    private ServerSocket server;

    public ServerSocketClientes(int port, GerenciadorPedidos gerenciador) {
        this.port = port;
        this.gerenciador = gerenciador;
    }

    public void shutdown() {
        running = false;
        try {
            if (server != null && !server.isClosed()) server.close();
        } catch (Exception e) { /* ignore */ }
    }

    @Override
    public void run() {
        try (ServerSocket s = new ServerSocket(port)) {
            this.server = s;
            System.out.println("ServerSocketClientes: listening on port " + port);
            while (running) {
                Socket client = s.accept();
                new Thread(() -> handleClient(client)).start();
            }
        } catch (Exception e) {
            if (running) System.err.println("ServerSocketClientes: error: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (Socket s = socket;
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            out.flush();
            Object obj = in.readObject();
            if (obj instanceof Pedido) {
                Pedido pedido = (Pedido) obj;
                System.out.println("Received pedido: " + pedido);
                Vehicle vehicle = gerenciador.handlePedido(pedido);
                out.writeObject(vehicle);
                out.flush();
            } else {
                System.err.println("Unknown request from client: " + obj);
                out.writeObject(null);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}
