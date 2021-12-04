package bgu.spl.mics;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {

    private static messageBusimpl messageBus ;
    private static TestMicroService x;


    private class TestMicroService extends MicroService{

        private TestMicroService(String name){
            super(name);
        }
    }

    private class TestEvent extends Event<String>{
        private String string;

        private TestEvent(String name){
            string=name;
        }
    }
    private class TestBroadcast extends Broadcast {
        private String string;

        private TestBroadcast(String name) {
            string = name;
        }
    }


    @Before
    public void setUp(){
        messageBus = new messageBusimpl();
    }

    @Test
    public void subscribeEvent() {
        TestEvent x = new TestEvent("xxx");
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        messageBus.subscribeEvent(x,m);
        assertTrue(isMicroServiceInEvent(x,m));
    }

    @Test
    public void subscribeBroadcast() {
        TestBroadcast x = new TestBroadcast("xxx");
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        messageBus.subscribeBroadcast(x,m);
        assertTrue(isMicroServiceInBroadcast(x,m));
    }


    @Test
    public void complete() {

    }

    @Test
    public void sendBroadcast() {
        TestBroadcast x = new TestBroadcast("xxx");
        TestMicroService m = new TestMicroService("mmm");
        TestMicroService k = new TestMicroService("kkk");
        messageBus.register(m);
        messageBus.register(k);
        messageBus.subscribeBroadcast(x,m);
        messageBus.subscribeBroadcast(x,k);
        messageBus.sendBroadcast(x);
        assertTrue(isMicroServiceReceiveBroadcast(x,m));
        assertTrue(isMicroServiceReceiveBroadcast(x,k));

    }

    @Test
    public void sendEvent() {
        TestEvent x = new TestEvent("xxx");
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        messageBus.subscribeEvent(x,m);
        messageBus.sendEvent(x);
        assertTrue(isMicroServiceReceiveEvent(x,m));
    }

    @Test
    public void register() {
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        assertTrue(isMicroServiceRegistered(m));
    }

    @Test
    public void unregister() {
        TestMicroService m = new TestMicroService("mmm");
        messageBus.register(m);
        assertFalse(!isMicroServiceRegistered(m));
    }

    @Test
    public void awaitMessage() {
    }
}