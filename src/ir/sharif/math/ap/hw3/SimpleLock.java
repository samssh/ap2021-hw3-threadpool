package ir.sharif.math.ap.hw3;

public class SimpleLock {
    private final Object lock;
    private boolean busy = false;

    public SimpleLock() {
        lock = new Object();
    }

    public void lock() {
        synchronized (lock) {
            while (busy) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            busy = true;
        }
    }

    public void release() {
        synchronized (lock) {
            busy = false;
            lock.notifyAll();
        }
    }
}