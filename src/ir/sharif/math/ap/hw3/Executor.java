package ir.sharif.math.ap.hw3;

import java.util.*;

public class Executor {
    private final Map<String, Integer> resources;
    private final List<Job> jobsLeft;
    private final List<Job> jobsEnded;
    private final ThreadPool threadPool;
    private final Object lock, lock2;
    private int runningJobs;

    public Executor(Map<String, Integer> resources, List<Job> jobs, int initialThreadNumber) {
        this.resources = new HashMap<>(resources);
        this.jobsLeft = new LinkedList<>(jobs);
        this.jobsEnded = new LinkedList<>();
        this.runningJobs = 0;
        this.lock = new Object();
        this.lock2 = new Object();
        this.threadPool = new ThreadPool(initialThreadNumber + 1);
        threadPool.invokeLater(this::run);
    }

    public void setThreadNumbers(int threadNumbers) {
        synchronized (lock2) {
            threadPool.setThreadNumbers(threadNumbers);
        }
    }


    private void run() {
        while (jobsLeft.size() > 0) {
            synchronized (lock2) {
                checkJobsForRun();
            }
            synchronized (lock) {
                while (jobsEnded.size() > 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                releaseResources();
            }
        }
    }

    public void checkJobsForRun() {
        for (Iterator<Job> iterator = jobsLeft.iterator(); iterator.hasNext(); ) {
            Job job = iterator.next();
            if (job.getResources().stream().allMatch(s -> resources.get(s) > 0)
                    && runningJobs < threadPool.getThreadNumbers() - 1) {
                iterator.remove();
                runJob(job);
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
        synchronized (lock) {
            jobsEnded.add(job);
            lock.notifyAll();
        }
    }

    private void releaseResources() {
        for (Job job : jobsEnded) {
            runningJobs = runningJobs - 1;
            job.getResources().forEach(s -> resources.put(s, resources.get(s) - 1));
        }
        jobsEnded.clear();
    }
}
