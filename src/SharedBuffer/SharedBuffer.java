package SharedBuffer;

import java.util.LinkedList;
import java.util.Queue;

public class SharedBuffer<T> {
    private final Queue<T> queue;
    private final int capacity;

    public SharedBuffer(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    public synchronized void add(T item) {
        try {
            while (queue.size() == capacity) {
                wait();
            }
            queue.add(item);
            System.out.println("Added: " + item);
            Thread.sleep(2000);
            notifyAll();
        } catch (InterruptedException e) {
            System.out.println("Add operation interrupted");
            Thread.currentThread().interrupt();
        }
    }

    public synchronized T remove() {
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
