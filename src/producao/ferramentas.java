package producao;


import java.util.concurrent.Semaphore;

public class ferramentas {
    private int id;
    private Semaphore semaforo;

    public ferramentas(int id) {
        this.id = id;
        this.semaforo = new Semaphore(1);
    }

    public int getId() {
        return id;
    }

    public void pegar() throws InterruptedException {
        semaforo.acquire();
    }

    public void soltar() {
        semaforo.release();
    }
}