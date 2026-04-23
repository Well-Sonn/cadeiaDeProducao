package loja;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class MockFactory implements Runnable {
    private final int port;
    private volatile boolean running = true;

    public MockFactory(int port) { this.port = port; }

    @Override
    public void run() {
        try (ServerSocket s = new ServerSocket(port)) {
            System.out.println("MockFactory listening on " + port);
            while (running) {
                Socket client = s.accept();
                new Thread(() -> handleStoreConnection(client)).start();
            }
        } catch (Exception e) {
            System.err.println("MockFactory error: " + e.getMessage());
        }
    }

    private void handleStoreConnection(Socket socket) {
        try (Socket s = socket;
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            out.flush();
            Object req = in.readObject();
            if (req instanceof FactoryRequest) {
                FactoryRequest fr = (FactoryRequest) req;
                int qty = fr.getQuantity();
                System.out.println("Factory received request from store=" + fr.getStoreId() + " qty=" + qty);
                for (int i = 0; i < qty; i++) {
                    List<String> chain = Arrays.asList("Montagem", "Pintura", "Inspecao");
                    Vehicle v = new Vehicle("ModeloX", chain);
                    out.writeObject(v);
                    out.flush();
                }
                out.writeObject("END");
                out.flush();
            } else {
                out.writeObject("NONE");
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("MockFactory connection error: " + e.getMessage());
        }
    }

    public void shutdown() { running = false; }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9000;
        MockFactory server = new MockFactory(port);
        new Thread(server).start();
    }
}
