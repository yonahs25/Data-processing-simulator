package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class TrainModelEvent implements Event<Model> {

    private Model model;
    Future<Model> myFuture;

    public TrainModelEvent(Model model) {
        this.model = model;
        myFuture = new Future<>();
    }

    public Model getModel() {
        return model;
    }
}
