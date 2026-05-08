package loja.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import loja.model.PedidoCliente;
import loja.model.Veiculo;

public class LoggerUtil {
    private final PrintWriter recebimentoWriter;
    private final PrintWriter vendaWriter;
    private final Object lock = new Object();

    
    public LoggerUtil(String lojaId) throws IOException {

    File r = new File("logs/loja-" + lojaId + "-recebimento.log");
    File v = new File("logs/loja-" + lojaId + "-venda.log");

    recebimentoWriter = new PrintWriter(new FileWriter(r, true), true);
    vendaWriter = new PrintWriter(new FileWriter(v, true), true);
}

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public void logRecebimento(Veiculo veiculo, String factoryInfo) {
        synchronized (lock) {
            String chain = veiculo.getCadeiaProducao() == null ? "" : String.join(" | ", veiculo.getCadeiaProducao());
            recebimentoWriter.printf("%s RECEBIDO id=%s modelo=%s fabrica=%s cadeia=%s%n", now(), veiculo.getId(), veiculo.getModelo(), factoryInfo, chain);
        }
    }

    public void logVenda(String idLoja, Veiculo veiculo, PedidoCliente pedido) {
        synchronized (lock) {
            String chain = veiculo.getCadeiaProducao() == null ? "" : String.join(" | ", veiculo.getCadeiaProducao());
            vendaWriter.printf("%s VENDIDO id=%s modelo=%s cliente=%s cadeia=%s%n", now(), idLoja, veiculo.getId(), veiculo.getModelo(), pedido != null ? pedido.getIdCliente() : "unknown", chain);
        }
    }

    public void close() {
        recebimentoWriter.close();
        vendaWriter.close();
    }
}
