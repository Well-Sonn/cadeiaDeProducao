package cliente;

import loja.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Garagem do Cliente — Buffer Circular Thread-Safe.
 *
 * Implementação do buffer circular usando EXCLUSIVAMENTE Semáforos,
 * seguindo o padrão produtor-consumidor clássico.
 *
 * Semáforos utilizados:
 *  - mutex    : exclusão mútua para acesso às variáveis internas (1 permissão)
 *  - espacos  : conta posições livres no buffer (inicializado com capacity)
 *  - veiculos : conta veículos disponíveis no buffer (inicializado com 0)
 *
 * A garagem do cliente é exclusivamente produtora (adicionar) durante a
 * simulação, mas expõe retirar() para uso futuro (ex: venda entre clientes).
 */
public class Garagem {

    private final Vehicle[] buffer;
    private final int       capacity;
    private int             head  = 0;  // próximo índice para retirar
    private int             tail  = 0;  // próximo índice para inserir
    private int             count = 0;  // quantidade atual de veículos

    // Semáforos — única forma de sincronização permitida
    private final Semaphore mutex;      // exclusão mútua (seção crítica)
    private final Semaphore espacos;    // posições livres no buffer
    private final Semaphore veiculos;   // veículos disponíveis no buffer

    // -----------------------------------------------------------------------
    // Construtor
    // -----------------------------------------------------------------------
    public Garagem(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacidade deve ser > 0");
        this.capacity = capacity;
        this.buffer   = new Vehicle[capacity];
        this.mutex    = new Semaphore(1, true);         // fair = FIFO
        this.espacos  = new Semaphore(capacity, true);  // começa com todas as posições livres
        this.veiculos = new Semaphore(0, true);         // começa vazio
    }

    // -----------------------------------------------------------------------
    // Adiciona veículo à garagem (bloqueia se lotada)
    // -----------------------------------------------------------------------
    public void adicionar(Vehicle v) throws InterruptedException {
        if (v == null) throw new IllegalArgumentException("Veiculo nao pode ser null");

        // 1. Aguarda ter espaço livre
        espacos.acquire();
        // 2. Entra na seção crítica
        mutex.acquire();
        try {
            buffer[tail] = v;
            tail = (tail + 1) % capacity;
            count++;
        } finally {
            // 3. Sai da seção crítica
            mutex.release();
        }
        // 4. Sinaliza que há mais um veículo disponível
        veiculos.release();
    }

    // -----------------------------------------------------------------------
    // Retira veículo da garagem (bloqueia se vazia)
    // -----------------------------------------------------------------------
    public Vehicle retirar() throws InterruptedException {
        // 1. Aguarda ter veículo disponível
        veiculos.acquire();
        // 2. Entra na seção crítica
        mutex.acquire();
        Vehicle v;
        try {
            v          = buffer[head];
            buffer[head] = null;   // ajuda o GC
            head       = (head + 1) % capacity;
            count--;
        } finally {
            // 3. Sai da seção crítica
            mutex.release();
        }
        // 4. Sinaliza que há mais um espaço livre
        espacos.release();
        return v;
    }

    // -----------------------------------------------------------------------
    // Retorna quantidade atual de veículos (snapshot, não-bloqueante)
    // -----------------------------------------------------------------------
    public int tamanho() {
        mutex.acquireUninterruptibly();
        try {
            return count;
        } finally {
            mutex.release();
        }
    }

    // -----------------------------------------------------------------------
    // Retorna true se garagem vazia (snapshot)
    // -----------------------------------------------------------------------
    public boolean isEmpty() {
        return tamanho() == 0;
    }

    // -----------------------------------------------------------------------
    // Retorna true se garagem cheia (snapshot)
    // -----------------------------------------------------------------------
    public boolean isFull() {
        return tamanho() == capacity;
    }

    // -----------------------------------------------------------------------
    // Retorna cópia imutável dos veículos presentes na garagem.
    // Usado para logging — não remove os veículos.
    // -----------------------------------------------------------------------
    public List<Vehicle> listar() {
        mutex.acquireUninterruptibly();
        try {
            List<Vehicle> lista = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Vehicle v = buffer[(head + i) % capacity];
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
