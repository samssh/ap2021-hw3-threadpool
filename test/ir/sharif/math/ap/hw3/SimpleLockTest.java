package ir.sharif.math.ap.hw3;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleLockTest {
    private SimpleLock lock;
    private int i;

    @Before
    public void setUp() throws Exception {
        lock = new SimpleLock();
        i = 0;
    }

    @Test
    public void test1() throws InterruptedException {
        Thread thread = new Thread(this::run1);
        Thread thread1 = new Thread(this::run1);
        Thread thread2 = new Thread(this::run1);
        Thread thread3 = new Thread(this::run1);
        thread.start();
        thread1.start();
        thread2.start();
        thread3.start();
        thread.join();
        thread1.join();
        thread2.join();
        thread3.join();
        assertEquals(40000, i);
    }

    private void run1() {
        for (int i = 0; i < 10000; i++) {
            lock.lock();
            this.i++;
            lock.release();
        }
    }
}