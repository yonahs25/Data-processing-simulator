package bgu.spl.mics;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {

    private static Future<String> future;
    private static Thread t1;
    private static Thread t2;


    @Before
    public void SetUp() {
        future = new Future<>();
        Thread t1 = null;
        Thread t2 = null;
    }

    @Test
    public void get() {
        t1 = new Thread(() ->  future.get());
        t1.start();
        try{
            Thread.sleep(2000);
        } catch (Exception E) {}
        future.resolve("aaaa");
        assertEquals("aaaa", future.get());

    }

    @Test
    public void resolve() {
        future.resolve("aaa");
        assertEquals("aaa", future.get());

    }

    @Test
    public void isDone() {
        String ans = "aaaa";
        future.resolve(ans);
        assertEquals(true, future.isDone());
    }

    @Test
    public void testGet() {
        assertNull(future.get(1,TimeUnit.SECONDS));
        String ans = "aaa";
        future.resolve("aaaa");
        assertEquals("aaaa", future.get(1,TimeUnit.SECONDS));
    }
}

