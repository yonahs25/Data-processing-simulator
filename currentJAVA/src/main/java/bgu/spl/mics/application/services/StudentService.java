package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Student;
import jdk.nashorn.internal.codegen.CompilerConstants;

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
    private class publishCallback implements Callback<PublishConferenceBroadcast>{

        @Override
        public void call(PublishConferenceBroadcast c)
        {
            // get the vector,
            System.out.println("hi");
        }
    }





    private Student student;

    public StudentService(String name, MessageBusImpl bus,Student student) {
        super("Change_This_Name",bus);
        this.student = student;
        // TODO Implement this
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishConferenceBroadcast.class, new publishCallback());

    }
}
