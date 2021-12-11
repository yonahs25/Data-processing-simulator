package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
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
        @Override
        public void call(TickBroadcast c)
        {
            gpu.updateTick();
            if(gpu.getModel().getStatus() == Model.Status.Trained)
            {
                complete(getCurrentEvent(),gpu.getModel());
                currentEvent = null;
                if(!waitingEvents.isEmpty())
                {
                    // need to call back the next message
                }
            }
        }
    }

    private class trainCallback implements Callback<TrainModelEvent>{

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
    }

    @Override
    protected void initialize()
    {
        subscribeBroadcast(TickBroadcast.class , new tickCallback());
        subscribeEvent(TrainModelEvent.class, new trainCallback());
        subscribeEvent(TestModelEvent.class, new testCallback());

    }
}
