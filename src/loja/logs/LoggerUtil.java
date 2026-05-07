package loja.logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import loja.model.Veiculo;
import loja.util.Pedido;

public class LoggerUtil {
    private final PrintWriter recebimentoWriter;
    private final PrintWriter vendaWriter;
    private final Object lock = new Object();
    public LoggerUtil(String storeId) throws IOException {
        File r = new File("store-" + storeId + "-recebimento.log");
        File v = new File("store-" + storeId + "-venda.log");
        recebimentoWriter = new PrintWriter(new FileWriter(r, true), true);
        vendaWriter = new PrintWriter(new FileWriter(v, true), true);
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public void logRecebimento(Veiculo vehicle, String factoryInfo) {
        synchronized (lock) {
            String chain = vehicle.getCadeiaProducao() == null ? "" : String.join(" | ", vehicle.getCadeiaProducao());
            recebimentoWriter.printf("%s RECEBIDO id=%s modelo=%s fabrica=%s cadeia=%s%n", now(), vehicle.getId(), vehicle.getModelo(), factoryInfo, chain);
        }
    }

    public void logVenda(String idLoja, Veiculo veiculo, Pedido pedido) {
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
