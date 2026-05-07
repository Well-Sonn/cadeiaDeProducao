package loja;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import loja.model.Veiculo;

public class TestClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java loja.TestClient <storeHost> <storePort>");
            return;
        }
        String endereco = args[0];
        int porta = Integer.parseInt(args[1]);
        try (Socket conexao = new Socket(endereco, porta);
             ObjectOutputStream saida = new ObjectOutputStream(conexao.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())) {
            saida.flush();
            Pedido pedido = new Pedido(Pedido.Type.BUY, "any", "cliente-1");
            saida.writeObject(pedido);
            saida.flush();
            Object resp = entrada.readObject();
            if (resp instanceof Veiculo) {
                Veiculo v = (Veiculo) resp;
                System.out.println("Veículo recebido: " + v.getId() + " modelo=" + v.getModelo());
            } else {
                System.out.println("Nenhum veículo recebido.");
            }
        }
    }
}
