package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private class tickCallback implements Callback<TickBroadcast> {
        public tickCallback() {}

        @Override
        public void call(TickBroadcast c)
        {
            gpu.updateTick();
            if(currentEvent!= null&& gpu.getModel() != null && gpu.getModel().getStatus() == Model.Status.Trained)
            {
                //System.out.println(gpu.getModel().getName() + " done");
                complete(getCurrentEvent(),gpu.getModel());
                currentEvent = null;
                if(!waitingEvents.isEmpty())
                {
                    Message myMessage = waitingEvents.remove();
                    callbackMap.get(myMessage.getClass()).call(myMessage);
                }
            }
        }
    }

    private class trainCallback implements Callback<TrainModelEvent>{

        public trainCallback() {}

        @Override
        public void call(TrainModelEvent c)
        {
            if(currentEvent == null)
            {
                gpu.setModel(c.getModel());
                setCurrentEvent(c);
            }
            else
            {
                waitingEvents.add(c);
            }


        }
    }

    private class testCallback implements Callback<TestModelEvent>{

        @Override
        public void call(TestModelEvent c)
        {
            if(currentEvent == null)
            {
                System.out.println("testing " + c.getModel().getName());
                gpu.setModel(c.getModel());
                complete(c, c.getModel());
            }
            else
            {
                waitingEvents.addFirst(c);
            }

        }
    }

    private GPU gpu;
    private Event currentEvent;
    private ConcurrentLinkedDeque<Event> waitingEvents;

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public GPUService(String name, MessageBusImpl bus, GPU gpu)
    {
        super(name,bus);
        this.gpu = gpu;
        currentEvent = null;
        waitingEvents = new ConcurrentLinkedDeque<>();
    }

    public int getWaitingEventsSize() {
        return waitingEvents.size();
    }

    @Override
    protected void initialize()
    {
        subscribeBroadcast(TickBroadcast.class , new tickCallback());
        subscribeEvent(TrainModelEvent.class, new trainCallback());
        subscribeEvent(TestModelEvent.class, new testCallback());
        subscribeBroadcast(TerminateBroadcast.class,new TerminateCallback(this));

    }
}
