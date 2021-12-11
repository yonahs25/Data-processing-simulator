package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.TerminateCallback;
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
        // TODO Implement this
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishConferenceBroadcast.class, new publishCallback());
        subscribeBroadcast(TerminateBroadcast.class,new TerminateCallback(this));
        // TODO Implement this

    }


}
