package socket;

import buffer.bufferCircular;
import model.veiculo;
import logs.logEnvio;

import java.io.*;
import java.net.Socket;

public class clientHandler extends Thread {

    private Socket socket;
    private bufferCircular buffer;

    public clientHandler(Socket socket, bufferCircular buffer) {
        this.socket = socket;
        this.buffer = buffer;
    }

    @Override
    public void run() {

        System.out.println("Loja conectada de " +
                socket.getInetAddress() + ":" + socket.getPort());

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true
            );

            String request;

            while ((request = in.readLine()) != null) {

                if (request.equals("REQUEST_VEHICLE")) {

                    veiculo v = buffer.consumir();

                    String resposta = v.getId() + ";" +
                                      v.getCor() + ";" +
                                      v.getTipo();

                    System.out.println("Enviando veículo ID=" + v.getId());

                    out.println("OK|" + resposta);

                    logEnvio log = new logEnvio(
                            v.getId(),
                            v.getCor(),
                            v.getTipo(),
                            socket.getInetAddress().toString()
                    );
                    log.salvarJson();

                    System.out.println("Veículo enviado com sucesso");
                } else {
                    out.println("INVALID");
                }
            }

        } catch (Exception e) {
            System.out.println("Loja desconectada");
        }
    }
}