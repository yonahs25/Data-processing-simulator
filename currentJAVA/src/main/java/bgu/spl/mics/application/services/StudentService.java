package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.Vector;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    private Student student;
    private Future<Model> future;
    private int currentModel;

    private class tickCallback implements Callback<TickBroadcast> {

        @Override
        public void call(TickBroadcast c) {
            if (future == null)
            {
                future = sendEvent(new TrainModelEvent(student.getModels().get(currentModel)));
                currentModel++;
            }
            else {
                if (future.isDone()){
                    Model checking = future.get();
                    future = sendEvent(new TestModelEvent(future.get()));
                    Model testResult = (Model) future.get();
                    if (testResult.getResults() == Model.Results.Good){
//                        future = sendEvent(new PublishResultsEvent(testResult));
                        sendEvent(new PublishResultsEvent(testResult)).get(); //need to delete get?
                    }
                    if (currentModel < student.getModels().size()) {
                        future = sendEvent(new TrainModelEvent(student.getModels().get(currentModel)));
                        currentModel++;
                    }
                }
            }
        }
    }


    private class publishCallback implements Callback<PublishConferenceBroadcast>{

        @Override
        public void call(PublishConferenceBroadcast c)
        {
            Vector<Model> goodResults = c.getGoodResults();
            //List<Model> models = student.getModels();
            for(int i = 0 ; i< goodResults.size(); i++){
                if(goodResults.get(i).getStudent() == student)
                    student.setPublications();
                else
                    student.setPapersRead();
            }
        }
    }

    public StudentService(String name, MessageBusImpl bus,Student student) {
        super(name,bus);
        this.student = student;
        future = null;
        currentModel = 0;
        // TODO Implement this
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishConferenceBroadcast.class, new publishCallback());
        subscribeBroadcast(TerminateBroadcast.class,new TerminateCallback(this));
        subscribeBroadcast(TickBroadcast.class, new tickCallback());
        // TODO Implement this

    }


}
