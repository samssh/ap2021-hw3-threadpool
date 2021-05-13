package ir.sharif.math.ap.hw3;

import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.*;


public class ThreadPoolTest {
    private static final long TIME_SAFE_MARGIN = 50;
    private static final long RUN1_SLEEP = 200;
    private static final long RUN2_SLEEP = 100;
    private static final long RUN3_SLEEP = 10;
    private ThreadPool threadPool;
    private Map<Object, Object> map;
    private Thread testThread, lockerThread;
    private Throwable throwable;
    private Set<Thread> threadSet, threadSet2;
    private boolean fail;

    @Before
    public void setUp() {
        this.map = new ConcurrentHashMap<>();
        this.testThread = Thread.currentThread();
        this.lockerThread = new Thread(this::getLocks);
        this.threadSet = new CopyOnWriteArraySet<>();
        this.threadSet2 = new CopyOnWriteArraySet<>();
        this.fail = false;
        Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
        Collections.addAll(threadSet2, getAllThreads());
        this.threadPool = new ThreadPool(3);
        lockerThread.start();
    }


    @After
    public void tearDown() throws Exception {
        lockerThread.interrupt();
        sleep(30);
        for (Thread t : getAllThreads()) {
            if (t != null && !threadSet2.contains(t)) {
                t.stop();
            }
        }
        assertFalse(fail);
        System.out.println(Thread.getAllStackTraces());
        sleep(30);
        System.gc();
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
    public void threadSafeTest2() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(10);
        for (int i = 0; i < 10; i++) {
            threadPool.invokeLater(this::run4);
        }
        sleep(RUN1_SLEEP + RUN3_SLEEP + 3 * TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN1_SLEEP + RUN3_SLEEP + 4 * TIME_SAFE_MARGIN);
        assertEquals(10, map.size());
        assertEquals(20, threadPool.getThreadNumbers());
        for (int i = 0; i < 20; i++) {
            threadPool.invokeLater(this::run1);
        }
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(30, map.size());
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

    @Test
    public void threadReusability() {
        long startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(3);
        for (int i = 0; i < 3; i++) {
            threadPool.invokeLater(this::run5);
        }
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertEquals(3, map.size());
        assertEquals(3, threadSet.size());
        assertTime(startTime, endTime, RUN1_SLEEP + 2 * TIME_SAFE_MARGIN);

        startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(4);
        for (int i = 0; i < 4; i++) {
            threadPool.invokeLater(this::run5);
        }
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        endTime = System.currentTimeMillis();
        assertEquals(7, map.size());
        assertEquals(4, threadSet.size());
        assertTime(startTime, endTime, RUN1_SLEEP + 2 * TIME_SAFE_MARGIN);

        startTime = System.currentTimeMillis();
        threadPool.setThreadNumbers(2);
        for (int i = 0; i < 2; i++) {
            threadPool.invokeLater(this::run5);
        }
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        endTime = System.currentTimeMillis();
        assertEquals(9, map.size());
        assertEquals(4, threadSet.size());
        assertTime(startTime, endTime, RUN1_SLEEP + 2 * TIME_SAFE_MARGIN);
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

    private void run4() {
        sleep(RUN3_SLEEP);
        assertDoesNotThrow(() -> threadPool.invokeLater(this::run1));
        synchronized (map) {
            assertDoesNotThrow(() -> threadPool.setThreadNumbers(threadPool.getThreadNumbers() + 1));
        }
    }

    private void run5() {
        sleep(RUN1_SLEEP);
        map.put(new Object(), new Object());
        threadSet.add(Thread.currentThread());
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
        throw new AssertionFailedError();
    }

    private void throwRun(RuntimeException throwable) {
        throw throwable;
    }

    private void assertTime(long start, long end, long expectedDuration) {
        assertTrue(end - start <= expectedDuration);
    }

    private void sleep(long sleepTime) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < sleepTime) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignore) {
            }
        }
    }

    private void getLocks() {
        synchronized (threadPool) {
            synchronized (threadPool.getClass()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private Thread[] getAllThreads() {
        ThreadGroup threadGroup = testThread.getThreadGroup();
        Thread[] threads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threads);
        return threads;
    }

    private void uncaughtException(Thread t, Throwable e) {
        if (Arrays.asList(getAllThreads()).contains(t) && !threadSet2.contains(t) && e instanceof Exception) {
            e.printStackTrace();
            this.fail = true;
        }
    }

    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}