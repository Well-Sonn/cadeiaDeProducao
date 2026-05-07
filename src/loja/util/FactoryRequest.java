package loja;

import java.io.Serializable;

public class FactoryRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String idLoja;
    private final int quantidade;

    public FactoryRequest(String idLoja, int quantidade) {
        this.idLoja = idLoja;
        this.quantidade = quantidade;
    }
    public String getIdLoja() { return idLoja; }
    public int getQuantidade() { return quantidade; }
    @Override
    public String toString() { return "FactoryRequest{idLoja="+idLoja+",quantidade="+quantidade+"}"; }
}
