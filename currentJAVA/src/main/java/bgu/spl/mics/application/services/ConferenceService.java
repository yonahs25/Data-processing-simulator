package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.TerminateCallback;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private class publishResultsCallback implements Callback<PublishResultsEvent> {
        @Override
        public void call(PublishResultsEvent c) {
            confrence.addGoodResult(c.getModel());
        }
    }



    private ConfrenceInformation confrence ;

    public ConferenceService(String name,ConfrenceInformation confrence, MessageBusImpl bus) {
        super(name,bus);
        this.confrence = confrence;

    }

    @Override
    protected void initialize() {
        subscribeEvent(PublishResultsEvent.class , new ConferenceService.publishResultsCallback());
        subscribeBroadcast(TerminateBroadcast.class,new TerminateCallback(this));
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendBroadcast(new PublishConferenceBroadcast(confrence.getGoodResults()));
                // need to unregister via the messageBus
            }
        } ,confrence.getDate());

    }
}
