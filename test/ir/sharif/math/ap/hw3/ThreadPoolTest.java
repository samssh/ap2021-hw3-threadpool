package ir.sharif.math.ap.hw3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThreadPoolTest {
    private ThreadPool threadPool;
    private List<Object> list;

    @BeforeEach
    void setUp() {
        this.threadPool = new ThreadPool(3);
        this.list = Collections.synchronizedList(new LinkedList<>());
    }

    @Test
    void invokeLater() throws InvocationTargetException, InterruptedException {

        // test 1
        threadPool.invokeLater(this::run1);
        threadPool.invokeLater(this::run1);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(2, list.size());

        // test 2
        threadPool.invokeAndWaite(this::run1);
        assertEquals(3, list.size());

        //test 3
        threadPool.invokeAndWaiteUninterruptible(this::run1);
        assertEquals(4, list.size());
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