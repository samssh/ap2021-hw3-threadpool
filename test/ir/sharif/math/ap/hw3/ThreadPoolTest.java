package ir.sharif.math.ap.hw3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTest {
    private final long TIME_SAFE_MARGIN = 50;
    private final long RUN1_SLEEP = 200, RUN2_SLEEP = 100, RUN3_SLEEP = 500, RUN4_SLEEP = 10;
    private ThreadPool threadPool;
    private Map<Object, Object> map;
    private Thread testThread;

    @BeforeEach
    void setUp() {
        this.threadPool = new ThreadPool(3);
        this.map = new ConcurrentHashMap<>();
        testThread = Thread.currentThread();
    }

    void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void invokeLater() {
        long startTime = System.currentTimeMillis();
        threadPool.invokeLater(this::run1);
        threadPool.invokeLater(this::run1);
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(2, map.size());
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + 2 * TIME_SAFE_MARGIN);
    }

    @Test
    void invokeAndWait() throws InterruptedException, InvocationTargetException {
        long startTime = System.currentTimeMillis();
        threadPool.invokeAndWait(this::run1);
        assertEquals(1, map.size());
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + TIME_SAFE_MARGIN);
    }

    @Test
    void parallelRun() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(5);
        for (int i = 0; i < 10; i++) {
            threadPool.invokeLater(this::run1);
        }
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(5, map.size());
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(10, map.size());
    }

    @Test
    void threadNumberTest() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(3);
        for (int i = 0; i < 6; i++) {
            threadPool.invokeLater(this::run3);
        }
        threadPool.setThreadNumbers(1);
        sleep(RUN3_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN3_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(3, map.size());
        sleep(RUN3_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(4, map.size());
        sleep(RUN3_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(5, map.size());
        sleep(RUN3_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(6, map.size());
    }

    @Test
    void threadSafeTest() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(10);
        for (int i = 0; i < 10; i++) {
            threadPool.invokeLater(this::run4);
        }
        sleep(RUN1_SLEEP + RUN4_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + RUN4_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(10, map.size());
    }

    @Test
    void invokeAndWaitUninterruptible() {
        threadPool.invokeLater(this::run2);
        assertDoesNotThrow(() -> threadPool.invokeAndWaitUninterruptible(this::run1));
        assertEquals(1, map.size());
    }

    void run2() {
        sleep(RUN2_SLEEP);
        testThread.interrupt();
    }

    void run1() {
        sleep(RUN1_SLEEP);
        map.put(new Object(), new Object());
    }

    void run3() {
        sleep(RUN3_SLEEP);
        map.put(new Object(), new Object());
    }

    void run4() {
        sleep(RUN4_SLEEP);
        assertDoesNotThrow(() -> threadPool.invokeLater(this::run1));
    }

    private static void assertTime(long start, long end, long expectedDuration) {
        assertTrue(end - start <= expectedDuration);
    }
}