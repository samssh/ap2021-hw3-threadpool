package ir.sharif.math.ap.hw3;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JobRunnerTest {
    @Rule
    public final RepeatRule repeatRule = new RepeatRule();
    private static final long TIME_SAFE_MARGIN = 50;
    private static final long RUN1_SLEEP = 100;
    private static final long RUN2_SLEEP = 200;
    private static final long RUN3_SLEEP = 300;
    private static final long RUN4_SLEEP = 400;
    private static final long RUN5_SLEEP = 500;
    private static final long RUN0_SLEEP = 10;
    private Map<Object, Object> map;
    private List<Integer> list;
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
        resources.put("sadat", 20);
        resources.put("erfan", 50);
        new Thread(this::getLocks).start();
    }

    @Test
    @Repeat(10)
    public void checkResources1() {
        Job job1 = new Job(() -> run1(map, RUN2_SLEEP, 0), "g");
        Job job2 = new Job(() -> run1(map, RUN2_SLEEP, 0), "g");
        Job job3 = new Job(() -> run1(map, RUN2_SLEEP, 0), "g");
        Job job4 = new Job(() -> run2(list, RUN2_SLEEP, 0, 4), "g");
        Job job5 = new Job(() -> run2(list, RUN2_SLEEP, 0, 5), "g");
        Job job6 = new Job(() -> run2(list, RUN2_SLEEP, 0, 6), "g");
        long startTime = System.currentTimeMillis();
        jobRunner = new JobRunner(resources, Arrays.asList(job1, job2, job3), 4);
        jobRunner = new JobRunner(resources, Arrays.asList(job4, job5, job6), 2);
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN2_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(3, map.size());
        assertEquals(2, list.size());
        Collections.sort(list);
        assertEquals(4, list.get(0).intValue());
        assertEquals(5, list.get(1).intValue());
        sleep(RUN2_SLEEP);
        assertEquals(3, list.size());
        assertEquals(6, list.get(2).intValue());

    }

    @Test
    @Repeat(10)
    public void checkResources2() {
        Job job1 = new Job(() -> run1(map, RUN5_SLEEP, 0), "a", "b", "c", "d", "e", "f", "g");
        Job job2 = new Job(() -> run1(map, RUN2_SLEEP, 0), "d", "e", "f", "g");
        Job job3 = new Job(() -> run1(map, RUN5_SLEEP, 0), "f", "g");
        Job job4 = new Job(() -> run1(map, RUN2_SLEEP, 0), "a", "d");
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
    @Repeat(10)
    public void checkResources3() { // 3 < 1 < 4 < 2 < 5
        Job job1 = new Job(() -> run2(list, RUN2_SLEEP, 0, 1), "a");
        Job job2 = new Job(() -> run2(list, RUN4_SLEEP, 0, 2), "a");
        Job job3 = new Job(() -> run2(list, RUN2_SLEEP, 0, 3), "a", "b");
        Job job4 = new Job(() -> run2(list, RUN3_SLEEP, 0, 4), "b");
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
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).intValue());
        assertTime(startTime, endTime, RUN2_SLEEP + 2 * TIME_SAFE_MARGIN);
        sleep(RUN3_SLEEP - RUN2_SLEEP);
        assertEquals(2, list.size());
        assertEquals(4, list.get(1).intValue());
        sleep(RUN3_SLEEP);
        assertEquals(3, list.size());
        assertEquals(2, list.get(2).intValue());
        sleep(RUN2_SLEEP);
        assertEquals(4, list.size());
        assertEquals(3, list.get(3).intValue());
    }

    @Test
    @Repeat(10)
    public void checkFreeze1() { // check this
        Job job1 = new Job(() -> run2(list, RUN2_SLEEP, RUN5_SLEEP, 1), "g");
        Job job2 = new Job(() -> run2(list, RUN3_SLEEP, 0, 2), "a", "g");
        Job job3 = new Job(() -> run2(list, RUN2_SLEEP, 0, 3), "a");
        Job job4 = new Job(() -> run2(list, RUN4_SLEEP, 0, 4), "a");
        /*
        job1 = 0 to 200, 200 to 700
        job2 = 0 to 700
        job3 = 700 to 900
        job4 = 900 to 1300
       */
        long startTime = System.currentTimeMillis();
        jobRunner = new JobRunner(resources, Arrays.asList(job1, job2, job3, job4), 2);
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);
        // 250
        long endTime = System.currentTimeMillis();
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).intValue());
        assertTime(startTime, endTime, RUN2_SLEEP + 2 * TIME_SAFE_MARGIN);
        sleep(RUN1_SLEEP);
        //350
        assertEquals(2, list.size());
        assertEquals(2, list.get(1).intValue());
        sleep(RUN4_SLEEP);
        //750
        assertEquals(2, list.size());
        assertEquals(2, list.get(1).intValue());
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);
        // 1000
        assertEquals(3, list.size());
        assertEquals(3, list.get(2).intValue());
        sleep(RUN4_SLEEP);
        // 1400
        assertEquals(4, list.size());
        assertEquals(4, list.get(3).intValue());
    }

    @Test
    @Repeat(10)
    public void checkFreeze2() {
        Job job1 = new Job(() -> run2(list, RUN4_SLEEP, RUN4_SLEEP, 1), "a");
        Job job2 = new Job(() -> run2(list, RUN2_SLEEP, RUN4_SLEEP, 2), "b");
        Job job3 = new Job(() -> run2(list, RUN2_SLEEP, 0, 3), "c");
        /*
        job1 = 0 to 400, 600 to 1000
        job2 = 0 to 200, 200 to 600
        job3 = 1000 to 1200
        */
        long startTime = System.currentTimeMillis();
        jobRunner = new JobRunner(resources, Arrays.asList(job1, job2, job3), 2);
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);// 250
        long endTime = System.currentTimeMillis();
        assertEquals(1, list.size());
        assertEquals(2, list.get(0).intValue());
        assertTime(startTime, endTime, RUN2_SLEEP + 2 * TIME_SAFE_MARGIN);

        sleep(RUN2_SLEEP);// 450
        assertEquals(2, list.size());
        assertEquals(2, list.get(0).intValue());
        assertEquals(1, list.get(1).intValue());

        sleep(RUN3_SLEEP);// 750
        assertEquals(2, list.size());
        assertEquals(2, list.get(0).intValue());
        assertEquals(1, list.get(1).intValue());

        sleep(RUN2_SLEEP);// 950
        assertEquals(2, list.size());
        assertEquals(2, list.get(0).intValue());
        assertEquals(1, list.get(1).intValue());

        sleep(RUN3_SLEEP);// 1250
        assertEquals(3, list.size());
        assertEquals(3, list.get(2).intValue());
    }

    @Test
    @Repeat(5)
    public void checkFreeze3() {
        int n = 30;
        Job[] jobs = new Job[n + 1];
        for (int i = 0; i < n; i++) {
            jobs[i] = new Job(() -> run1(map, RUN2_SLEEP, RUN1_SLEEP), "erfan");
        }
        jobs[n] = new Job(() -> run1(map, RUN5_SLEEP, 0), "c");
        /*
        job0-job49 = 0 to 3200
        job50 = 3200 to 3700
       */
        long startTime = System.currentTimeMillis();
        jobRunner = new JobRunner(resources, Arrays.asList(jobs), n);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, TIME_SAFE_MARGIN);
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN); // 250
        assertEquals(n, map.size());
        sleep(n * RUN1_SLEEP); // 3250
        sleep(RUN4_SLEEP);
        assertEquals(n, map.size());
//        while (n == map.size()) {
//            sleep(RUN3_SLEEP);
//        }
    }

    @Test
    @Repeat(10)
    public void increaseThreadNumber() { // check this
        Job job1 = new Job(() -> run2(list, RUN2_SLEEP, RUN3_SLEEP, 1), "a");
        Job job2 = new Job(() -> run2(list, RUN5_SLEEP, 0, 2), "b");
        Job job3 = new Job(() -> run2(list, RUN3_SLEEP, 0, 3), "c");
        /*
        job1 = 0 to 200, 200 to 500
        then in t = 350, increase thread numbers to 2
        job2 = 500 to 1000
        job3 = 500 to 800
       */
        jobRunner = new JobRunner(resources, Arrays.asList(job1, job2, job3), 1);
        sleep(RUN2_SLEEP + TIME_SAFE_MARGIN);
        // 250
        assertEquals(1, list.size());
        assertEquals(1, list.get(0).intValue());
        sleep(RUN1_SLEEP);
        // 350
        long startTime = System.currentTimeMillis();
        jobRunner.setThreadNumbers(2);
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN2_SLEEP + TIME_SAFE_MARGIN);
        assertTime2(startTime, endTime, RUN1_SLEEP);

        // 500
        sleep(RUN1_SLEEP + TIME_SAFE_MARGIN);
        // 650
        assertEquals(1, list.size());
        sleep(RUN2_SLEEP);
        // 850
        assertEquals(2, list.size());
        assertEquals(3, list.get(1).intValue());
        sleep(RUN2_SLEEP);
        // 1050
        assertEquals(3, list.size());
        assertEquals(2, list.get(2).intValue());
    }

    @Test
    @Repeat(10)
    public void decreaseThreadNumber() {
        Job job1 = new Job(() -> run1(map, RUN4_SLEEP, 0), "g");
        Job job2 = new Job(() -> run1(map, RUN4_SLEEP, 0), new String("g"));
        Job job3 = new Job(() -> run1(map, RUN2_SLEEP, RUN4_SLEEP), "g");
        Job job4 = new Job(() -> run2(list, RUN3_SLEEP, 0, 4), "f", "g");
        Job job5 = new Job(() -> run2(list, RUN2_SLEEP, 0, 5), "f", "g");
        Job job6 = new Job(() -> run2(list, RUN2_SLEEP, 0, 6), "f", "g");
        /*
         * job1: 0 400
         * job2 : 0 400
         * job3 : 0 200 , freeze 200 (set thread number to 1) 600
         * job4: 600 900
         * job5: 900 1100
         * job6: 1100 1300
         * */
        long startTime = System.currentTimeMillis();
        jobRunner = new JobRunner(resources, Arrays.asList(job1, job2, job3, job4, job5, job6), 3);
        sleep(RUN4_SLEEP + TIME_SAFE_MARGIN);// 450
        long endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN4_SLEEP + 2 * TIME_SAFE_MARGIN);
        assertEquals(3, map.size());


        startTime = System.currentTimeMillis();
        jobRunner.setThreadNumbers(1); // 600
        endTime = System.currentTimeMillis();
        assertTime(startTime, endTime, RUN2_SLEEP + TIME_SAFE_MARGIN);
        assertTime2(startTime, endTime, RUN1_SLEEP);

        sleep(RUN3_SLEEP + 3 * TIME_SAFE_MARGIN); // 1000
        assertEquals(1, list.size());
        assertEquals(4, list.get(0).intValue());


        sleep(RUN2_SLEEP); // 1100
        assertEquals(2, list.size());
        assertEquals(5, list.get(1).intValue());


        sleep(RUN2_SLEEP); // 1300
        assertEquals(3, list.size());
        assertEquals(6, list.get(2).intValue());
    }

    private long run1(Map<Object, Object> map, long sleep, long returnSleep) {
        sleep(sleep);
        map.put(new Object(), new Object());
        return returnSleep;
    }

    private long run2(List<Integer> list, long sleep, long returnSleep, int id) {
        sleep(sleep);
        list.add(id);
        return returnSleep;
    }

    private void assertTime(long start, long end, long expectedDuration) {
        assertTrue(end - start <= expectedDuration);
    }

    private void assertTime2(long start, long end, long expectedDuration) {
        assertTrue(end - start >= expectedDuration);
    }

    private void getLocks() {
        sleep(300);
        synchronized (jobRunner) {
            synchronized (resources) {
                synchronized (jobRunner.getClass()) {
                    sleep(2000);
                }
            }
        }
    }

    private void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}