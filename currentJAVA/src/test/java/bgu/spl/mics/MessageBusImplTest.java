package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    private static MessageBusImpl messageBus ;
    private static TestMicroService x;

    private class TestMicroService extends MicroService{

        private TestMicroService(String name){
            super(name);
        }

        protected void initialize() {

        }


    }

    @Before
    public void setUp() {
        messageBus = new MessageBusImpl();
    }

    @Test
    public void subscribeEvent() {

        ExampleEvent y = new ExampleEvent("hi");
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        messageBus.subscribeEvent(y.getClass(),m);
        assertTrue(messageBus.isMicroServiceInEvent(y.getClass(),m));
    }

    @Test
    public void subscribeBroadcast() {
        ExampleBroadcast y = new ExampleBroadcast("aaa");
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        messageBus.subscribeBroadcast(y.getClass(),m);
        assertTrue(messageBus.isMicroServiceInBroadcast(y.getClass(),m));
    }


    @Test
    public void complete() {
        ExampleEvent e = new ExampleEvent("m");
        Future<String> f = new Future<>();
        f.resolve("aaa");
        messageBus.complete(e ,f.get());
        assertEquals("aaa", f.get());
    }

    @Test
    public void sendBroadcast() {
        ExampleBroadcast x = new ExampleBroadcast("aaa");
        TestMicroService m = new TestMicroService("mmm");
        TestMicroService k = new TestMicroService("kkk");
        messageBus.register(m);
        messageBus.register(k);
        messageBus.subscribeBroadcast(x.getClass(),m);
        messageBus.subscribeBroadcast(x.getClass(),k);
        messageBus.sendBroadcast(x);
        assertTrue(messageBus.didMicroServiceReceiveBroadcast(x,m));
        assertTrue(messageBus.didMicroServiceReceiveBroadcast(x,k));

    }

    @Test
    public void sendEvent() {
        ExampleEvent x = new ExampleEvent("xxx");
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        messageBus.subscribeEvent(x.getClass(),m);
        messageBus.sendEvent(x);
        assertTrue(messageBus.didMicroServiceReceiveEvent(x,m));
    }

    @Test
    public void register() {
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        assertTrue(messageBus.isMicroServiceRegistered(m));
    }

    @Test
    public void unregister() {
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        assertFalse(!messageBus.isMicroServiceRegistered(m));
    }

    @Test
    public void awaitMessage() {
        TestMicroService m = new TestMicroService("mmm");
        ExampleBroadcast x = new ExampleBroadcast("aaa");
        messageBus.register(m);
        assertThrows("need to get Exception because no messages",Exception.class, () ->messageBus.awaitMessage((m)));
        messageBus.subscribeBroadcast(x.getClass(), m);
        messageBus.sendBroadcast(x);
        try {
            Message test = messageBus.awaitMessage(m);
            assertEquals(test, x);
        }catch (Exception e){}






    }
}