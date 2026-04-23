package loja;

import java.io.Serializable;

public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type { BUY }

    private final Type type;
    private final String model;
    private final String clientId;

    public Pedido(Type type, String model, String clientId) {
        this.type = type;
        this.model = model;
        this.clientId = clientId;
    }
    public Type getType() { return type; }
    public String getModel() { return model; }
    public String getClientId() { return clientId; }
    @Override
    public String toString() { return "Pedido{" + type + "," + model + "," + clientId + "}"; }
}
