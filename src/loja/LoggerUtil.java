package loja;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public void logRecebimento(Vehicle vehicle, String factoryInfo) {
        synchronized (lock) {
            String chain = vehicle.getProductionChain() == null ? "" : String.join(" | ", vehicle.getProductionChain());
            recebimentoWriter.printf("%s RECEBIDO id=%s model=%s factory=%s chain=%s%n", now(), vehicle.getId(), vehicle.getModel(), factoryInfo, chain);
        }
    }

    public void logVenda(Vehicle vehicle, Pedido pedido) {
        synchronized (lock) {
            String chain = vehicle.getProductionChain() == null ? "" : String.join(" | ", vehicle.getProductionChain());
            vendaWriter.printf("%s VENDIDO id=%s model=%s client=%s chain=%s%n", now(), vehicle.getId(), vehicle.getModel(), pedido != null ? pedido.getClientId() : "unknown", chain);
        }
    }

    public void close() {
        recebimentoWriter.close();
        vendaWriter.close();
    }
}
