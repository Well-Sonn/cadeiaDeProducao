package loja;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSocketFactory implements Runnable {
    private final String host;
    private final int port;
    private final EsteiraLoja esteira;
    private final LoggerUtil logger;
    private final String storeId;
    private final int batchSize;
    private volatile boolean running = true;

    public ClientSocketFactory(String host, int port, EsteiraLoja esteira, LoggerUtil logger, String storeId, int batchSize) {
        this.host = host;
        this.port = port;
        this.esteira = esteira;
        this.logger = logger;
        this.storeId = storeId;
        this.batchSize = Math.max(1, batchSize);
    }

    public void shutdown() { running = false; }

    public int requestVehicles(int quantity) {
        int received = 0;
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(15000);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            FactoryRequest req = new FactoryRequest(storeId, quantity);
            out.writeObject(req);
            out.flush();
            for (int i = 0; i < quantity; i++) {
                Object obj = in.readObject();
                if (obj == null) break;
                if (obj instanceof Vehicle) {
                    Vehicle v = (Vehicle) obj;
                    try {
                        esteira.put(v);
                        logger.logRecebimento(v, host + ":" + port);
                        received++;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else if (obj instanceof String) {
                    String s = (String) obj;
                    if ("NONE".equals(s) || "END".equals(s)) {
                        break;
                    }
                } else {
                    // ignore unknown
                }
            }
        } catch (Exception e) {
            System.err.println("ClientSocketFactory: error connecting to factory: " + e.getMessage());
        }
        return received;
    }

    @Override
    public void run() {
        while (running) {
            try {
                int current = esteira.size();
                if (current < batchSize) {
                    int needed = batchSize - current;
                    int got = requestVehicles(needed);
                    if (got == 0) {
                        Thread.sleep(5000);
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
