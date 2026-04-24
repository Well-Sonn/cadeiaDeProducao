package cliente;

import loja.Vehicle;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Logger thread-safe do Cliente.
 *
 * Gera um arquivo de log individual por cliente:
 *   log_cliente-N.txt
 *
 * Tipos de entrada:
 *  - COMPRA       : veículo comprado com sucesso (inclui dados da cadeia produtiva)
 *  - ESPERA       : cliente aguardou por falta de estoque
 *  - RESUMO_FINAL : lista completa da garagem ao encerrar
 *
 * Sincronização: Semáforo de exclusão mútua (mutex) protege a escrita em disco.
 * Cada instância possui seu próprio semáforo (arquivo por cliente = sem disputa
 * entre clientes distintos).
 */
public class ClientLogger {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private final String    clienteId;
    private final String    logFile;
    private final Semaphore mutex;   // exclusão mútua para escrita no arquivo

    // -----------------------------------------------------------------------
    // Construtor
    // -----------------------------------------------------------------------
    public ClientLogger(String clienteId) {
        this.clienteId = clienteId;
        this.logFile   = "log_" + clienteId + ".txt";
        this.mutex     = new Semaphore(1, true);

        // Cria/limpa o arquivo de log no início da execução
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, false))) {
            pw.println("=== LOG DO CLIENTE: " + clienteId + " ===");
            pw.println("Inicio: " + SDF.format(new Date()));
            pw.println("=========================================");
        } catch (IOException e) {
            System.err.println("[ClientLogger][" + clienteId + "] Nao foi possivel criar arquivo de log: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Log de compra bem-sucedida
    //
    // Campos registrados:
    //  - Timestamp
    //  - ID do cliente
    //  - ID do veículo
    //  - Modelo
    //  - ID da loja vendedora
    //  - Cadeia produtiva completa (recebida da fábrica via loja)
    //  - Tamanho atual da garagem
    // -----------------------------------------------------------------------
    public void logCompra(Vehicle vehicle, String lojaId, int tamanhoGaragem) {
        String cadeia = vehicle.getProductionChain() != null
                ? String.join(" -> ", vehicle.getProductionChain())
                : "N/A";

        String entrada = String.format(
                "[%s] COMPRA | cliente=%s | veiculoId=%s | modelo=%s | loja=%s | cadeia=[%s] | garagem=%d",
                timestamp(), clienteId,
                vehicle.getId(), vehicle.getModel(),
                lojaId, cadeia, tamanhoGaragem
        );

        escrever(entrada);
    }

    // -----------------------------------------------------------------------
    // Log de estado de espera (loja sem estoque)
    // -----------------------------------------------------------------------
    public void logEspera(String lojaId, String modelo) {
        String entrada = String.format(
                "[%s] ESPERA  | cliente=%s | loja=%s | modelo=%s | motivo=sem_estoque",
                timestamp(), clienteId, lojaId, modelo
        );

        escrever(entrada);
    }

    // -----------------------------------------------------------------------
    // Log de resumo final — lista todos os veículos na garagem
    // -----------------------------------------------------------------------
    public void logResumo(List<Vehicle> veiculos) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] RESUMO_FINAL | cliente=%s | total_veiculos=%d%n",
                timestamp(), clienteId, veiculos.size()));

        for (int i = 0; i < veiculos.size(); i++) {
            Vehicle v = veiculos.get(i);
            String cadeia = v.getProductionChain() != null
                    ? String.join(" -> ", v.getProductionChain())
                    : "N/A";
            sb.append(String.format("  [%02d] id=%s | modelo=%s | cadeia=[%s]%n",
                    i + 1, v.getId(), v.getModel(), cadeia));
        }

        escrever(sb.toString().trim());
    }

    // -----------------------------------------------------------------------
    // Escrita thread-safe no arquivo via Semáforo
    // -----------------------------------------------------------------------
    private void escrever(String entrada) {
        // Imprime no console também para acompanhamento em tempo real
        System.out.println(entrada);

        mutex.acquireUninterruptibly();
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
            pw.println(entrada);
        } catch (IOException e) {
            System.err.println("[ClientLogger][" + clienteId + "] Erro ao escrever log: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }

    // -----------------------------------------------------------------------
    // Helper: timestamp formatado
    // -----------------------------------------------------------------------
    private static String timestamp() {
        return SDF.format(new Date());
    }
}
