package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;

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
    private LinkedBlockingDeque<Future<Model>> futureList;
    private int currentModel;

    private class tickCallback implements Callback<TickBroadcast> {

        @Override
        public void call(TickBroadcast c)
        {
            while (futureList.isEmpty() && currentModel == 0){
                try {
                    futureList.add(sendEvent(new TrainModelEvent(student.getModels().get(currentModel))));
                } catch (Exception e){}
            }
            if (currentModel==0) {
                currentModel++;
            }
            for (Future<Model> f : futureList){
                if (f.isDone()){
                    futureList.remove(f);
                    Model done = f.get();
                    if(done.getStatus() == Model.Status.Trained){
                        futureList.add(sendEvent(new TestModelEvent(done)));
                    } else if (done.getStatus() == Model.Status.Tested){
                        if (done.getResults() == Model.Results.Good){
                            sendEvent(new PublishResultsEvent(done)); //need to delete get?
                        }
                    }
                }
            }
            if (currentModel < student.getModels().size()) {
                currentModel++;
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

    public StudentService(String name,Student student) {
        super(name);
        this.student = student;
        future = null;
        currentModel = 0;
        futureList = new LinkedBlockingDeque<>();
        // TODO Implement this
    }


    public int getCurrentModel() {
        return currentModel;
    }

    public Future<Model> getFuture() {
        return future;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishConferenceBroadcast.class, new publishCallback());
        subscribeBroadcast(TerminateBroadcast.class,new TerminateCallback(this));
        subscribeBroadcast(TickBroadcast.class, new tickCallback());
    }


}
