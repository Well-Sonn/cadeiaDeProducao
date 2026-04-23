package loja;

import java.io.Serializable;

public class FactoryRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String storeId;
    private final int quantity;

    public FactoryRequest(String storeId, int quantity) {
        this.storeId = storeId;
        this.quantity = quantity;
    }
    public String getStoreId() { return storeId; }
    public int getQuantity() { return quantity; }
    @Override
    public String toString() { return "FactoryRequest{storeId="+storeId+",quantity="+quantity+"}"; }
}
