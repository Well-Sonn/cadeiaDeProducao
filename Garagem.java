package cliente;

import loja.model.Veiculo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Garagem {

    private final Veiculo[] buffer;
    private final int       capacity;
    private int             head  = 0;  
    private int             tail  = 0;  
    private int             count = 0;  

    private final Semaphore mutex;     
    private final Semaphore espacos;  
    private final Semaphore veiculos; 


    public Garagem(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacidade deve ser > 0");
        this.capacity = capacity;
        this.buffer   = new Veiculo[capacity];
        this.mutex    = new Semaphore(1, true);        
        this.espacos  = new Semaphore(capacity, true);  
        this.veiculos = new Semaphore(0, true);        
    }

    public void adicionar(Veiculo v) throws InterruptedException {
        if (v == null) throw new IllegalArgumentException("Veiculo nao pode ser null");

        espacos.acquire();
        mutex.acquire();

        try {
            buffer[tail] = v;
            tail = (tail + 1) % capacity;
            count++;
        } finally {
            mutex.release();
        }
        veiculos.release();
    }


    public Veiculo retirar() throws InterruptedException {
        veiculos.acquire();
        mutex.acquire();

        Veiculo v;
        try {
            v          = buffer[head];
            buffer[head] = null;   
            head       = (head + 1) % capacity;
            count--;
        } finally {
            mutex.release();
        }
        espacos.release();
        return v;
    }

    public int tamanho() {
        mutex.acquireUninterruptibly();
        try {
            return count;
        } finally {
            mutex.release();
        }
    }

    public boolean isEmpty() {
        return tamanho() == 0;
    }

    public boolean isFull() {
        return tamanho() == capacity;
    }

    public List<Veiculo> listar() {
        mutex.acquireUninterruptibly();
        try {
            List<Veiculo> lista = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Veiculo v = buffer[(head + i) % capacity];
                if (v != null) lista.add(v);
            }
            return Collections.unmodifiableList(lista);
        } finally {
            mutex.release();
        }
    }

    public int getCapacity() {
        return capacity;
    }
}
