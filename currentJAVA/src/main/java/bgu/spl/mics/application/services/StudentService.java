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
            
            while (future==null){
                try {
                    future = sendEvent(new TrainModelEvent(student.getModels().get(currentModel)));
                } catch (Exception e){}
            }
            if (currentModel==0)
                currentModel++;
            if (future != null)
            {
                if (future.isDone())
                {
                    // checking if model even is done
                    Model done = future.get();
                    // if done training sending to test
                    if (done.getStatus() == Model.Status.Trained)
                    {
                        future = sendEvent(new TestModelEvent(done));
                    } // if tested
                    else if (done.getStatus() == Model.Status.Tested && done.getPublished() == Model.Published.No)
                    { // checking if test was good, if yes, sending publish result
                        if (done.getResults() == Model.Results.Good)
                        {
                            future = sendEvent(new PublishResultsEvent(done));
                        }  // if not, checking if we have more models to send, if yes, sending
                        else if (currentModel < student.getModels().size())
                        {
                            future = sendEvent(new TrainModelEvent(student.getModels().get(currentModel)));
                            currentModel ++;
                        }
                    }
                    
                    
                    
                    
                    
                    
//                    future = sendEvent(new TestModelEvent(future.get()));
//                    Model testResult = future.get();
//                    if (testResult.getResults() == Model.Results.Good){
//                        future = sendEvent(new PublishResultsEvent(testResult));
//                        sendEvent(new PublishResultsEvent(testResult)).get(); //need to delete get?
//                    }
//                    if (currentModel < student.getModels().size()) {
//                        future = sendEvent(new TrainModelEvent(student.getModels().get(currentModel)));
//                        currentModel++;
//                    }
                }
            }


            /*
            while (futureList.isEmpty() && currentModel == 0){
                try {
                    futureList.add(sendEvent(new TrainModelEvent(student.getModels().get(currentModel))));
                } catch (Exception e){}
            }

            if (currentModel == 0) {
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
                futureList.add(sendEvent(new TrainModelEvent(student.getModels().get(currentModel))));
                currentModel++;
            }
            */
        }
    }
    private class publishCallback implements Callback<PublishConferenceBroadcast>{

        @Override
        public void call(PublishConferenceBroadcast c)
        {
            Vector<Model> goodResults = c.getGoodResults();
            //List<Model> models = student.getModels();
            for (Model goodResult : goodResults) {// if the model is of the student, increase publications by 1, and if sending new model if needed
                if (goodResult.getStudent() == student) {
                    student.setPublications();
                    //TODO check this!
                    if (currentModel < student.getModels().size()) {
                        System.out.println("published " + goodResult.getName());
                        System.out.println("sending " + (student.getModels().get(currentModel)).getName());
                        future = sendEvent(new TrainModelEvent(student.getModels().get(currentModel)));
                        currentModel++;
                    }

                } else
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
