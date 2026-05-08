package loja.model;

import java.io.Serializable;

public class PedidoCliente implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type { BUY }

    private final Type tipo;
    private final String modelo;
    private final String idCliente;

    public PedidoCliente(Type tipo, String modelo, String idCliente) {
        this.tipo = tipo;
        this.modelo = modelo;
        this.idCliente = idCliente;
    }
    public Type getType() { return tipo; }
    public String getModelo() { return modelo; }
    public String getIdCliente() { return idCliente; }
    @Override
    public String toString() { return "Pedido{" + tipo + "," + modelo + "," + idCliente + "}"; }
}
