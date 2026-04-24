package buffer;

import model.veiculo;
import java.util.concurrent.Semaphore;

public class bufferCircular {
    private veiculo[] buffer;
    private int tamanho;
    private int entrada, saida;

    private Semaphore mutex;
    private Semaphore vazio;
    private Semaphore cheio;

    public bufferCircular(int tamanho) {
        this.tamanho = tamanho;
        buffer = new veiculo[tamanho];
        entrada = 0;
        saida = 0;
        mutex = new Semaphore(1);
        vazio = new Semaphore(tamanho);
        cheio = new Semaphore(0);
    }

    public int produzir(veiculo v) throws InterruptedException {
        vazio.acquire();
        mutex.acquire();

        int posicao = entrada;
        buffer[entrada] = v;
        entrada = (entrada + 1) % tamanho;

        System.out.println("Veiculo Produzido: " + v + " | posição esteira " + posicao);

        mutex.release();
        cheio.release();

        return posicao;
    }

    public veiculo consumir() throws InterruptedException {
        cheio.acquire();
        mutex.acquire();

        veiculo v = buffer[saida];
        saida = (saida + 1) % tamanho;

        mutex.release();
        vazio.release();

        return v;
    }
}