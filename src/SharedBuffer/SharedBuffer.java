package Buffer;

import java.util.Queue;

public class SharedBuffer<T> {
    private final Queue<T> queue;
    private final int capacity;

    public SharedBuffer(Queue<T> queue, int capacity) {
        this.queue = queue;
        this.capacity = capacity;
    }

    public synchronized void add(T item) {
        try {
            while (queue.size() == capacity) {
                wait();
            }
            queue.add(item);
            notifyAll();
        } catch (InterruptedException e) {
            System.out.println("Add operation interrupted");
            Thread.currentThread().interrupt();
        }
    }

    public T remove() {
        try {
            T item;
            while (queue.isEmpty()) {
                wait();
            }
            item = queue.poll();
            notifyAll();
            return item;
        } catch (InterruptedException e) {
            System.out.println("Remove operation interrupted");
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public Queue<T> getQueue() {
        return queue;
    }

    public int getCapacity() {
        return capacity;
    }


}
