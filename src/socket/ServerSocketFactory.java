package socket;

import java.net.ServerSocket;
import java.net.Socket;

import buffer.bufferCircular;

public class ServerSocketFactory extends Thread {

    private int porta;
    private bufferCircular buffer;

    public ServerSocketFactory(int porta, bufferCircular buffer) {
        this.porta = porta;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(porta)) {

            System.out.println("Fábrica aguardando conexões na porta " + porta);

            while (true) {
                Socket cliente = server.accept();
                System.out.println("Loja conectada!");

                new clientHandler(cliente, buffer).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}