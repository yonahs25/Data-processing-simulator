package bgu.spl.mics;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FutureTest {

    private static Future<String> future;


    @Before
    public void SetUp() {
        future = new Future<>();
        Thread t1;
        Thread t2;
    }

    @Test
    public void get() {
        String ans;
    }

    @Test
    public void resolve() {
    }

    @Test
    public void isDone() {
    }

    @Test
    public void testGet() {
    }
}

