package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StudentTest {

    private static MessageBusImpl bus;
    private static Student student;
    private static Data data;
    private static Model model;
    private StudentService studentService;

    @Before
    public void setUp(){
        bus = new MessageBusImpl();
        student = new Student("Simba", "Computer Science", "MSc");
        data = new Data(Data.Type.Images, 1000);
        model = new Model("YOLO10", data, student);
        student.addModel(model);
        studentService = new StudentService("Simba", bus, student);
    }


    @Test
    public void sendingTrainEvent() {
        Cluster cluster=new Cluster();
        GPU gpu = new GPU(GPU.Type.RTX3090 , cluster);
        GPUService gpuService = new GPUService("me", bus,gpu);
        Thread t1 = new Thread(studentService);
        Thread t2 = new Thread(gpuService);
        t1.start();
        t2.start();
        try {
            Thread.sleep(759);
        } catch (InterruptedException e) {}

        bus.sendBroadcast(new TickBroadcast());
        try {
            Thread.sleep(759);
        } catch (InterruptedException e) {}

        assertEquals(studentService.getCurrentModel(), 1);
        assertNotNull(studentService.getFuture());
        assertEquals(model.getStatus(), Model.Status.Training);
    }


}