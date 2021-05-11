package ir.sharif.math.ap.hw3;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobRunnerTest {
    private static final long TIME_SAFE_MARGIN = 50;
    private static final long RUN1_SLEEP = 100;
    private static final long RUN2_SLEEP = 200;
    private static final long RUN3_SLEEP = 300;
    private static final long RUN4_SLEEP = 400;
    private static final long RUN5_SLEEP = 500;
    private static final long RUN6_SLEEP = 600;
    private static final long RUN7_SLEEP = 700;
    private static final long RUN0_SLEEP = 10;
    private Map<Object, Object> map;
    private List<Object> list;
    private JobRunner jobRunner;
    private Map<String, Integer> resources;

    @Before
    public void setUp() throws Exception {
        map = new ConcurrentHashMap<>();
        list = new CopyOnWriteArrayList<>();
        resources = new HashMap<>();
        resources.put("a", 1);
        resources.put("b", 1);
        resources.put("c", 1);
        resources.put("d", 2);
        resources.put("e", 2);
        resources.put("f", 3);
        resources.put("g", 3);
    }

    @Test
    public void checkResources1() {
        Job job1 = new Job(() -> run1(RUN5_SLEEP, 0), "a", "b", "c", "d", "e", "f", "g");
        Job job2 = new Job(() -> run1(RUN2_SLEEP, 0), "d", "e", "f", "g");
        Job job3 = new Job(() -> run1(RUN5_SLEEP, 0), "f", "g");
        Job job4 = new Job(() -> run1(RUN2_SLEEP, 0), "a", "d");
        long startTime = System.currentTimeMillis();
        jobRunner = new JobRunner(resources, Arrays.asList(job1, job2, job3, job4), 4);
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertEquals(1, map.size());
        assertTime(startTime, endTime, RUN2_SLEEP + 2 * TIME_SAFE_MARGIN);
        sleep(RUN5_SLEEP - RUN2_SLEEP);
        assertEquals(3, map.size());
        sleep(RUN2_SLEEP);
        sleep(RUN2_SLEEP);
        assertEquals(4, map.size());
    }

    @Test
    public void checkResources2() { // 3 < 1 < 4 < 2 < 5
        Job job1 = new Job(() -> run1(RUN2_SLEEP, 0), "a");
        Job job2 = new Job(() -> run1(RUN4_SLEEP, 0), "a");
        Job job3 = new Job(() -> run1(RUN2_SLEEP, 0), "a", "b");
        Job job4 = new Job(() -> run1(RUN3_SLEEP, 0), "b");
        /*
        job1 = 0 to 200
        job2 = 200 to 600
        job3 = 600 to 800
        job4 = 0 to 300
       */
        long startTime = System.currentTimeMillis();
        jobRunner = new JobRunner(resources, Arrays.asList(job1, job2, job3, job4), 2);
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertEquals(1, map.size());
        assertTime(startTime, endTime, RUN2_SLEEP + 2 * TIME_SAFE_MARGIN);
        sleep(RUN3_SLEEP - RUN2_SLEEP);
        assertEquals(2, map.size());
        sleep(RUN3_SLEEP);
        assertEquals(3, map.size());
        sleep(RUN2_SLEEP);
        assertEquals(4, map.size());
    }

    @Test
    public void checkFreeze1() { // check this
        Job job1 = new Job(() -> run2(RUN2_SLEEP, RUN5_SLEEP, 1), "g");
        Job job2 = new Job(() -> run2(RUN3_SLEEP, 0, 2), "a", "g");
        Job job3 = new Job(() -> run2(RUN2_SLEEP, 0, 3), "a");
        Job job4 = new Job(() -> run2(RUN1_SLEEP, 0, 4), "a");
        /*
        job1 = 0 to 200, 200 to 700
        job2 = 0 to 700
        job3 = 700 to 900
        job4 = 900 to 1000
       */
        long startTime = System.currentTimeMillis();
        jobRunner = new JobRunner(resources, Arrays.asList(job1, job2, job3, job4), 2);
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertEquals(1, list.size());
        assertEquals(1, list.get(0));
        assertTime(startTime, endTime, RUN2_SLEEP + 2 * TIME_SAFE_MARGIN);
        sleep(RUN5_SLEEP);
        assertEquals(2, list.size());
        assertEquals(2, list.get(1));
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);
        assertEquals(3, list.size());
        assertEquals(3, list.get(2));
        sleep(RUN1_SLEEP);
        assertEquals(4, list.size());
        assertEquals(4, list.get(3));
    }

    private long run1(long sleep, long returnSleep) {
        sleep(sleep);
        map.put(new Object(), new Object());
        return returnSleep;
    }

    private long run2(long sleep, long returnSleep, int id) {
        sleep(sleep);
        list.add(id);
        return returnSleep;
    }

    private int run3() {
        sleep(RUN0_SLEEP);
//        assertDoesNotThrow(() -> threadPool.invokeLater(this::run1));
        return 0;
    }

//    private void assertDoesNotThrow(ThrowingRunnable runnable) {
//        try {
//            runnable.run();
//        } catch (Throwable t) {
//            throwable = t;
//        }
//    }

//    private static <T extends Throwable> T assertThrows(Class<T> tClass, ThrowingRunnable runnable) {
//        try {
//            runnable.run();
//        } catch (Throwable t) {
//            assertTrue(tClass.isInstance(t));
//            return (T) t;
//        }
//        return null;
//    }

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