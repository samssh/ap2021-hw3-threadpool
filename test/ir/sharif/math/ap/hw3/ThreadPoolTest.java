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

    void run1() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        list.add(new Object());
    }

    @Test
    void testInvokeLater() {
        threadPool.invokeLater(this::run1);
        threadPool.invokeLater(this::run1);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(2, list.size());
    }

    @Test
    void invokeAndWait() throws InterruptedException, InvocationTargetException {
        threadPool.invokeAndWait(this::run1);
        assertEquals(1, list.size());
    }

    @Test
    void invokeAndWaitUninterruptible() throws InvocationTargetException {
        threadPool.invokeAndWaitUninterruptible(this::run1);
        assertEquals(1, list.size());
    }
}