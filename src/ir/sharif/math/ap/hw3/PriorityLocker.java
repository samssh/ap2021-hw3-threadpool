package ir.sharif.math.ap.hw3;

public class PriorityLocker {
    private boolean busy;
    private final SimpleLock locker;
    private final Object[] locks;
    private final int[] members;
    private final int max_priority;

    public PriorityLocker(int max_priority) {
        this.max_priority = max_priority;
        locker = new SimpleLock();
        locks = new Object[this.max_priority];
        members = new int[this.max_priority];
        for (int i = 0; i < this.max_priority; i++) {
            locks[i] = new Object();
        }
    }

    public void lock(int priority) {
        locker.lock();
        synchronized (locks[priority - 1]) {
            members[priority - 1]++;
            while (busy) {
                try {
                    locker.release();
                    locks[priority - 1].wait();
                    locker.lock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            members[priority - 1]--;
            busy = true;
        }
        locker.release();
    }

    public void release() {
        locker.lock();
        for (int i = 0; i < max_priority; i++) {
            synchronized (locks[i]) {
                if (members[i] > 0) {
                    locks[i].notifyAll();
                    break;
                }
            }
        }
        busy = false;
        locker.release();
    }
}