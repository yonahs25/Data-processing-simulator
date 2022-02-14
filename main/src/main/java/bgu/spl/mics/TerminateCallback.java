package bgu.spl.mics;

import bgu.spl.mics.application.messages.TerminateBroadcast;

public class TerminateCallback implements Callback<TerminateBroadcast> {

    private MicroService microService;

    public TerminateCallback(MicroService microService) {
        this.microService = microService;
    }

    @Override
    public void call(TerminateBroadcast c) {
        microService.terminate();
    }
}
