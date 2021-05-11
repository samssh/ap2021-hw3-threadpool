package ir.sharif.math.ap.hw3;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JobRunner {
    private final Map<String, Integer> resources;
    private final List<Job> jobsLeft;
    private final ThreadPool threadPool;
    private final PriorityLocker locker;
    private final Object lock;
    private int runningJobs;


    public JobRunner(Map<String, Integer> resources, List<Job> jobs, int initialThreadNumber) {
        this.resources = new ConcurrentHashMap<>(resources);
        this.jobsLeft = new LinkedList<>(jobs);
        this.locker = new PriorityLocker(2);
        this.lock = new Object();
        this.threadPool = new ThreadPool(initialThreadNumber + 1);
        threadPool.invokeLater(this::run);
    }

    public void setThreadNumbers(int threadNumbers) {
            threadPool.setThreadNumbers(threadNumbers + 1);
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
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runJob(Job job) {
        runningJobs = runningJobs + 1;
        job.getResources().forEach(s -> resources.put(s, resources.get(s) - 1));
        threadPool.invokeLater(() -> doJob(job));
    }

    private void doJob(Job job) {
        long sleep = job.getRunnable().run();
        locker.lock(1);
        runningJobs = runningJobs - 1;
        job.getResources().forEach(s -> resources.put(s, resources.get(s) + 1));
        synchronized (lock) {
            lock.notifyAll();
        }
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignore) {
            }
        }
        locker.release();
    }
}
