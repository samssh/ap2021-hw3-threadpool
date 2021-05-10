package ir.sharif.math.ap.hw3;

import java.util.*;

public class Executor {
    private final Map<String, Integer> resources;
    private final List<Job> jobsLeft;
    private final List<Job> jobsEnded;
    private final ThreadPool threadPool;
    private final Object lock2;
    private final PriorityLocker locker;
    private int runningJobs;

    public Executor(Map<String, Integer> resources, List<Job> jobs, int initialThreadNumber) {
        this.resources = new HashMap<>(resources);
        this.jobsLeft = new LinkedList<>(jobs);
        this.jobsEnded = new LinkedList<>();
        this.runningJobs = 0;
        this.locker = new PriorityLocker(2);
        this.lock2 = new Object();
        this.threadPool = new ThreadPool(initialThreadNumber + 1);
        threadPool.invokeLater(this::run);
    }

    public void setThreadNumbers(int threadNumbers) {
        synchronized (lock2) {
            threadPool.setThreadNumbers(threadNumbers + 1);
        }
    }


    private void run() {
        while (jobsLeft.size() > 0) {
            for (Iterator<Job> iterator = jobsLeft.iterator(); iterator.hasNext(); ) {
                locker.lock(2);
                Job job = iterator.next();
                if (job.getResources().stream().allMatch(s -> resources.get(s) > 0)
                        && runningJobs < threadPool.getThreadNumbers() - 1) {
                    iterator.remove();
                    runJob(job);
                }
                locker.release();
            }
        }
    }

    private void runJob(Job job) {
        runningJobs = runningJobs + 1;
        job.getResources().forEach(s -> resources.put(s, resources.get(s) - 1));
        threadPool.invokeLater(() -> doJob(job));
    }

    private void doJob(Job job) {
        job.getRunnable().run();
        locker.lock(1);
        runningJobs = runningJobs - 1;
        job.getResources().forEach(s -> resources.put(s, resources.get(s) - 1));
        locker.release();
    }

    static class PriorityLocker {
        private volatile boolean busy;
        private final Object locker;
        private final Object[] locks;
        private final int[] members;
        private final int max_priority;

        PriorityLocker(int max_priority) {
            this.max_priority = max_priority;
            locker = new Object();
            locks = new Object[this.max_priority];
            members = new int[this.max_priority];
            for (int i = 0; i < this.max_priority; i++) {
                locks[i] = new Object();
            }
        }

        void lock(int priority) {
            synchronized (locker) {
                synchronized (locks[priority - 1]) {
                    members[priority - 1]++;
                    while (busy) {
                        try {
                            locks[priority - 1].wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    members[priority - 1]--;
                    busy = true;
                }
            }
        }

        void release() {
            synchronized (locker) {
                for (int i = 0; i < max_priority; i++) {
                    synchronized (locks[i]) {
                        if (members[i] > 0) {
                            busy = false;
                            locks[i].notifyAll();
                            return;
                        }
                    }
                }
            }
        }
    }
}
