package loja.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Veiculo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String modelo;
    private final List<String> cadeiaProducao;
    private final long momento;

    public Veiculo(String modelo, List<String> cadeiaProducao) {
        this.id = UUID.randomUUID().toString();
        this.modelo = modelo;
        this.cadeiaProducao = cadeiaProducao;
        this.momento = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getModelo() { return modelo; }
    public List<String> getCadeiaProducao() { return cadeiaProducao; }
    public long getMomento() { return momento; }

    @Override
    public String toString() {
        return "Veiculo{id=" + id + ", modelo=" + modelo + "}";
    }
}
