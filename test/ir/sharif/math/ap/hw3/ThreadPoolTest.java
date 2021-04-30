package ir.sharif.math.ap.hw3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadPoolTest {
    private ThreadPool threadPool;
    private List<Object> list = Collections.synchronizedList(new LinkedList<>());

    @BeforeEach
    void setUp() {

    }

    @Test
    void invokeLater() throws InvocationTargetException, InterruptedException {
        this.threadPool = new ThreadPool(1);
        threadPool.invokeLater(this::run1);
        threadPool.invokeLater(this::run1);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(1, list.size());
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(2, list.size());

        // test 2
        long start = System.currentTimeMillis();
        threadPool.invokeAndWaite(this::run1);
        assertTrue(System.currentTimeMillis() - start > 1000);

        start = System.currentTimeMillis();
        threadPool.invokeAndWaiteUninterruptible(this::run1);
        assertTrue(System.currentTimeMillis() - start > 1000);
    }

    void run1() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        list.add(new Object());
    }
}