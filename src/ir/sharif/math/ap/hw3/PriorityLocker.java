package ir.sharif.math.ap.hw3;

public class PriorityLocker {
    private boolean busy;
    private final Object locks;
    private final int[] members;
    private final int maxPriority;

    public PriorityLocker(int maxPriority) {
        this.maxPriority = maxPriority;
        locks = new Object();
        members = new int[this.maxPriority];
    }

    public void lock(int priority) {
        synchronized (locks) {
            members[priority - 1]++;
            while (busy || getMinPriority() != priority) {
                try {
                    locks.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            members[priority - 1]--;
            busy = true;
        }
    }

    public int getMinPriority() {
        for (int i = 0; i < maxPriority; i++) {
            if (members[i] > 0) return i + 1;
        }
        return maxPriority;
    }

    public void release() {
        synchronized (locks) {
            locks.notifyAll();
            busy = false;
        }
    }
}