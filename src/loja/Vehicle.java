package loja;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String model;
    private final List<String> productionChain;
    private final long timestamp;

    public Vehicle(String model, List<String> productionChain) {
        this.id = UUID.randomUUID().toString();
        this.model = model;
        this.productionChain = productionChain;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getModel() { return model; }
    public List<String> getProductionChain() { return productionChain; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Vehicle{id=" + id + ", model=" + model + "}";
    }
}
