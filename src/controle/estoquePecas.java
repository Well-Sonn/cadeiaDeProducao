package controle;

import java.util.concurrent.Semaphore;

public class estoquePecas {
    private Semaphore pecas;

    public estoquePecas(int total) {
        this.pecas = new Semaphore(total);
    }

    public void usarPeca() throws InterruptedException {
        pecas.acquire();
    }

    public void adicionarPeca() {
        pecas.release();
    }

    public int getDisponivel() {
        return pecas.availablePermits();
    }
}
