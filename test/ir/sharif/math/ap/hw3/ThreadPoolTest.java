package ir.sharif.math.ap.hw3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTest {
    private final long TIME_SAFE_MARGIN = 50;
    private final long RUN1_SLEEP = 200;
    private final long RUN2_SLEEP = 100;
    private final long RUN3_SLEEP = 10;
    private ThreadPool threadPool;
    private Map<Object, Object> map;
    private Thread testThread;

    @BeforeEach
    void setUp() {
        this.threadPool = new ThreadPool(3);
        this.map = new ConcurrentHashMap<>();
        this.testThread = Thread.currentThread();
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
        startTime = System.currentTimeMillis();
        threadPool.invokeAndWaitUninterruptible(this::run1);
        assertEquals(2, map.size());
        endTime = System.currentTimeMillis();
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
    void increaseThreadNumberTest() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(3);
        for (int i = 0; i < 9; i++) {
            threadPool.invokeLater(this::run1);
        }
        sleep(RUN1_SLEEP);
        sleep(TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(3, map.size());
        threadPool.setThreadNumbers(6);
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(9, map.size());
    }

    @Test
    void decreaseThreadNumberTest() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(3);
        for (int i = 0; i < 6; i++) {
            threadPool.invokeLater(this::run1);
        }
        threadPool.setThreadNumbers(1);
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(3, map.size());
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(4, map.size());
        sleep(RUN1_SLEEP);
        assertEquals(5, map.size());
        sleep(RUN1_SLEEP);
        assertEquals(6, map.size());
    }

    @Test
    void threadSafeTest() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(10);
        for (int i = 0; i < 10; i++) {
            threadPool.invokeLater(this::run3);
        }
        sleep(RUN1_SLEEP + RUN3_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + RUN3_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(10, map.size());
    }

    @Test
    void interruptTest() {
        threadPool.invokeLater(this::run2);
        assertDoesNotThrow(() -> threadPool.invokeAndWaitUninterruptible(this::run1));
        assertEquals(1, map.size());
        threadPool.invokeLater(this::run2);
        assertThrows(InterruptedException.class, () -> threadPool.invokeAndWait(this::run1));
        assertEquals(2, map.size());
    }

    @Test
    void throwTest() {
        RuntimeException runtimeException = new RuntimeException();
        InvocationTargetException invocationTargetException = assertThrows(InvocationTargetException.class,
                () -> threadPool.invokeAndWaitUninterruptible(() -> throwRun(runtimeException)));
        assertSame(invocationTargetException.getTargetException(), runtimeException);
        invocationTargetException = assertThrows(InvocationTargetException.class,
                () -> threadPool.invokeAndWait(() -> throwRun(runtimeException)));
        assertSame(invocationTargetException.getTargetException(), runtimeException);
    }

    private void run1() {
        sleep(RUN1_SLEEP);
        map.put(new Object(), new Object());
    }

    private void run2() {
        sleep(RUN2_SLEEP);
        testThread.interrupt();
    }

    private void run3() {
        sleep(RUN3_SLEEP);
        assertDoesNotThrow(() -> threadPool.invokeLater(this::run1));
    }

    private void throwRun(RuntimeException throwable) {
        throw throwable;
    }

    private void assertTime(long start, long end, long expectedDuration) {
        assertTrue(end - start <= expectedDuration);
    }

    void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}