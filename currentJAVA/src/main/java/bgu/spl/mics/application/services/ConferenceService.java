package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.TerminateCallback;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConfrenceInformation conference ;
    private long currTick;

    private class publishResultsCallback implements Callback<PublishResultsEvent> {
        @Override
        public void call(PublishResultsEvent c)
        {
            conference.addGoodResult(c.getModel());
            c.getModel().setPublished(Model.Published.Yes);
            MessageBusImpl.getInstance().complete(c, c.getModel());
        }
    }

    private class tickCallback implements Callback<TickBroadcast>{

        @Override
        public void call(TickBroadcast c)
        {
            currTick++;
            if (currTick == conference.getDate())
            {
                sendBroadcast(new PublishConferenceBroadcast(conference.getGoodResults()));
                terminate();
            }
        }
    }

    public ConferenceService(String name,ConfrenceInformation confrence)
    {
        super(name);
        this.conference = confrence;
        currTick=0;
    }

    @Override
    protected void initialize()
    {
        subscribeBroadcast(TickBroadcast.class, new tickCallback());
        subscribeEvent(PublishResultsEvent.class , new ConferenceService.publishResultsCallback());
        subscribeBroadcast(TerminateBroadcast.class,new TerminateCallback(this));
    }
}
