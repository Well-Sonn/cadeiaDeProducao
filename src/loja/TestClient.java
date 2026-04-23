package loja;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java loja.TestClient <storeHost> <storePort>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        try (Socket s = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
            out.flush();
            Pedido pedido = new Pedido(Pedido.Type.BUY, "any", "cliente-1");
            out.writeObject(pedido);
            out.flush();
            Object resp = in.readObject();
            if (resp instanceof Vehicle) {
                Vehicle v = (Vehicle) resp;
                System.out.println("Received vehicle: " + v.getId() + " model=" + v.getModel());
            } else {
                System.out.println("No vehicle received.");
            }
        }
    }
}
