package loja.Esteira;

import loja.model.Veiculo;

public class EsteiraLoja {
    private final Veiculo[] buffer;
    private int inicio = 0, fim = 0, tamanho = 0;
    private final int capacidade;

    public EsteiraLoja(int capacidade) {
        this.capacidade = capacidade;
        this.buffer = new Veiculo[capacidade];
    }

    public synchronized void colocar(Veiculo v) throws InterruptedException {
        while (tamanho == capacidade) {
            wait();
        }
        buffer[fim] = v;
        fim = (fim + 1) % capacidade;
        tamanho++;
        notifyAll();
    }

    public synchronized Veiculo retirar() throws InterruptedException {
        while (tamanho == 0) {
            wait();
        }
        Veiculo v = buffer[inicio];
        buffer[inicio] = null;
        inicio = (inicio + 1) % capacidade;
        tamanho--;
        notifyAll();
        return v;
    }

    public synchronized int size() { return tamanho; }
    public synchronized int capacity() { return capacidade; }

    @Override
    public synchronized String toString() {
        return "EsteiraLoja{size=" + tamanho + ",capacidade=" + capacidade + "}";
    }
}
