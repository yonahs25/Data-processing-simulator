package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.Vector;

public class PublishConferenceBroadcast implements Broadcast {

    private Vector<Model> goodResults;

    public PublishConferenceBroadcast(Vector<Model> goodResults) {
        this.goodResults = goodResults;
    }

    public Vector<Model> getGoodResults() {
        return goodResults;
    }
}
