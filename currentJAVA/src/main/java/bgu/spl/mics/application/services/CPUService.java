package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.TerminateCallback;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

/**
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */


public class CPUService extends MicroService {

    private CPU cpu;

    private class tickCallback implements Callback<TickBroadcast>{
        @Override
        public void call(TickBroadcast c)
        {
            cpu.updateTime();
        }
    }



    public CPUService(String name,CPU cpu)
    {

        super(name);
        this.cpu = cpu;
        // TODO Implement this
    }

    @Override
    protected void initialize() {
        // TODO Implement this
        subscribeBroadcast(TickBroadcast.class , new tickCallback());
        subscribeBroadcast(TerminateBroadcast.class,new TerminateCallback(this));

    }
}
