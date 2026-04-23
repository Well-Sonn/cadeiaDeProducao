package loja;

public class EsteiraLoja {
    private final Vehicle[] buffer;
    private int head = 0, tail = 0, count = 0;
    private final int capacity;

    public EsteiraLoja(int capacity) {
        this.capacity = capacity;
        this.buffer = new Vehicle[capacity];
    }

    public synchronized void put(Vehicle v) throws InterruptedException {
        while (count == capacity) {
            wait();
        }
        buffer[tail] = v;
        tail = (tail + 1) % capacity;
        count++;
        notifyAll();
    }

    public synchronized Vehicle take() throws InterruptedException {
        while (count == 0) {
            wait();
        }
        Vehicle v = buffer[head];
        buffer[head] = null;
        head = (head + 1) % capacity;
        count--;
        notifyAll();
        return v;
    }

    public synchronized int size() { return count; }
    public synchronized int capacity() { return capacity; }

    @Override
    public synchronized String toString() {
        return "EsteiraLoja{size=" + count + ",capacity=" + capacity + "}";
    }
}
