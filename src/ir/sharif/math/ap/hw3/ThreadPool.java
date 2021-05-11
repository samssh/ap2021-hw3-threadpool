package ir.sharif.math.ap.hw3;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class ThreadPool {
    private final List<Thread> threads;
    private final List<Task> tasks;
    private final Object lock;

    public ThreadPool(int threadNumbers) {
        threads = new LinkedList<>();
        tasks = new LinkedList<>();
        lock = new Object();
        for (int i = 0; i < threadNumbers; i++) {
            threads.add(new Thread());
        }
        threads.forEach(java.lang.Thread::start);
    }

    public int getThreadNumbers() {
        synchronized (lock) {
            return threads.size();
        }
    }

    public void setThreadNumbers(int threadNumbers) {
        synchronized (lock) {
            if (threadNumbers < threads.size()) {
                while (threads.size() > threadNumbers) {
                    Thread thread = threads.remove(threads.size() - 1);
                    thread.stopWorking();
                }
                lock.notifyAll();
            } else {
                while (threads.size() < threadNumbers) {
                    Thread thread = new Thread();
                    threads.add(thread);
                    thread.start();
                }
            }
        }
    }

    public void invokeLater(Runnable runnable) {
        synchronized (lock) {
            tasks.add(new Task(runnable, false));
            lock.notifyAll();
        }
    }

    public void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
        Task task = new Task(runnable, true);
        synchronized (task.getLock()) {
            synchronized (lock) {
                tasks.add(task);
                lock.notifyAll();
            }
            while (task.isNotDone())
                task.getLock().wait();
            if (task.getThrowable() != null)
                throw new InvocationTargetException(task.getThrowable());
        }
    }

    public void invokeAndWaitUninterruptible(Runnable runnable) throws InvocationTargetException {
        Task task = new Task(runnable, true);
        synchronized (task.getLock()) {
            synchronized (lock) {
                tasks.add(task);
                lock.notifyAll();
            }
            while (task.isNotDone()) {
                try {
                    task.getLock().wait();
                } catch (InterruptedException ignore) {
                }
            }
            if (task.getThrowable() != null)
                throw new InvocationTargetException(task.getThrowable());
        }
    }

    private class Thread extends java.lang.Thread {
        private volatile boolean active;
        private final Object lock;

        public Thread() {
            super();
            lock = new Object();
            active = true;
        }

        @Override
        public void run() {
            while (isActive()) {
                Task task;
                synchronized (ThreadPool.this.lock) {
                    while (tasks.isEmpty()) {
                        try {
                            ThreadPool.this.lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace(); // unreachable state
                        }
                        if (!isActive()) {
                            return;
                        }
                    }
                    task = tasks.remove(tasks.size() - 1);
                }
                synchronized (task.getLock()) {
                    try {
                        task.getRunnable().run();
                    } catch (Throwable throwable) {
                        if (task.isWaiting())
                            task.setThrowable(throwable);
                    }
                    task.setDone();
                    task.getLock().notifyAll();
                }
            }

        }

        public boolean isActive() {
            synchronized (lock) {
                return active;
            }
        }

        public void stopWorking() {
            synchronized (lock) {
                this.active = false;
            }
        }
    }

    private static class Task {
        private final Runnable runnable;
        private final boolean waiting;
        private final Object lock;
        private volatile Throwable throwable;
        private volatile boolean done;

        public Task(Runnable runnable, boolean waiting) {
            this.runnable = runnable;
            this.waiting = waiting;
            this.lock = new Object();
            this.done = false;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public boolean isWaiting() {
            return waiting;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }

        public Object getLock() {
            return lock;
        }

        public boolean isNotDone() {
            return !done;
        }

        public void setDone() {
            this.done = true;
        }
    }
}
