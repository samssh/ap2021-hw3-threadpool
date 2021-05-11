package ir.sharif.math.ap.hw3;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;


public class ThreadPoolTest {
    private static final long TIME_SAFE_MARGIN = 50;
    private static final long RUN1_SLEEP = 200;
    private static final long RUN2_SLEEP = 100;
    private static final long RUN3_SLEEP = 10;
    private ThreadPool threadPool;
    private Map<Object, Object> map;
    private Thread testThread;
    private Throwable throwable;

    @Before
    public void setUp() {
        this.threadPool = new ThreadPool(3);
        this.map = new ConcurrentHashMap<>();
        this.testThread = Thread.currentThread();
    }

    @After
    public void tearDown() throws Exception {
//        threadPool.setThreadNumbers(0);
//        System.gc();
    }

    @Test
    public void invokeLater() {
        long startTime = System.currentTimeMillis();
        threadPool.invokeLater(this::run1);
        threadPool.invokeLater(this::run1);
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(2, map.size());
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + 2 * TIME_SAFE_MARGIN);
    }

    @Test
    public void invokeAndWait() throws InterruptedException, InvocationTargetException {
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
    public void parallelRun() {
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
    public void increaseThreadNumberTest() {
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
    public void decreaseThreadNumberTest() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(3);
        for (int i = 0; i < 6; i++) {
            threadPool.invokeLater(this::run1);
        }
        sleep(TIME_SAFE_MARGIN);
        threadPool.setThreadNumbers(1);
        sleep(RUN1_SLEEP);
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
    public void threadSafeTest() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(10);
        for (int i = 0; i < 10; i++) {
            threadPool.invokeLater(this::run3);
        }
        sleep(RUN1_SLEEP + RUN3_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + RUN3_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(10, map.size());
        assertNull(throwable);
    }

    @Test
    public void interruptTest() {
        threadPool.invokeLater(this::run2);
        assertDoesNotThrow(() -> threadPool.invokeAndWaitUninterruptible(this::run1));
        assertEquals(1, map.size());
        threadPool.invokeLater(this::run2);
        assertThrows(InterruptedException.class, () -> threadPool.invokeAndWait(this::run1));
        assertEquals(2, map.size());
        assertNull(throwable);
    }

    @Test
    public void throwTest() {
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

    private void assertDoesNotThrow(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            throwable = t;
        }
    }

    private static <T extends Throwable> T assertThrows(Class<T> tClass, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            assertTrue(tClass.isInstance(t));
            return (T) t;
        }
        return null;
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

    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}