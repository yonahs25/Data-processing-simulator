package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.GPUService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class GPUTest {

    private static GPU gpu;
    private static Cluster cluster;

    @Before
    public void setUp(){
        cluster=new Cluster();
        gpu = new GPU(GPU.Type.RTX3090 , cluster);
    }

    @Test
    public void setModelToTrain() {
        Student student = new Student("Simba", "Computer Science", "MSc");
        Data data = new Data(Data.Type.Images, 200000);
        Model model = new Model("YOLO10", data, student);
        gpu.setModel(model);
        List<DataBatch> list = gpu.getUnprocessedData();
        assertEquals(list.size(), 200000/1000);
        assertEquals(Model.Status.Training, gpu.getModel().getStatus());
    }

    @Test
    public void setModelToTest(){
        Student student = new Student("Simba", "Computer Science", "MSc");
        Data data = new Data(Data.Type.Images, 200000);
        Model model = new Model("YOLO10", data, student);
        model.setStatus(Model.Status.Trained);
        gpu.setModel(model);
        assertEquals(model.getStatus(), Model.Status.Tested);
        assertNotEquals(model.getResults(), Model.Results.none);
    }

    @Test
    public void sendUnprocessed() {
        cluster.registerGpu(gpu);
        Student student = new Student("Simba", "Computer Science", "MSc");
        Data data = new Data(Data.Type.Images, 200000);
        Model model = new Model("YOLO10", data, student);
        gpu.setModel(model);
        gpu.updateTick();
        assertNotEquals(gpu.getUnprocessedData().size(), 200000/1000);
    }

    @Test
    public void getProcessed(){
        Student student = new Student("Simba", "Computer Science", "MSc");
        cluster.registerGpu(gpu);
        Data data = new Data(Data.Type.Images, 200000);
        DataBatch databatch = new DataBatch(data, 0, gpu);
        Model model = new Model("YOLO10", data, student);
        gpu.setModel(model);
        cluster.putProcessedData(databatch);
        gpu.updateTick();
        assertEquals(1, gpu.getProcessedData().size());
        gpu.updateTick();
        assertEquals(0, gpu.getProcessedData().size());
        assertEquals(1000, data.getProcessed());
    }


    @Test
    public void finishedTraining(){
        cluster.registerGpu(gpu);
        Student student = new Student("Simba", "Computer Science", "MSc");
        Data data = new Data(Data.Type.Images, 1000);
        Model model = new Model("YOLO10", data, student);
        gpu.setModel(model);
        DataBatch databatch = gpu.getUnprocessedData().get(0);
        gpu.updateTick();
        cluster.putProcessedData(databatch);
        gpu.updateTick();
        gpu.updateTick();
        assertEquals(gpu.getModel().getStatus(), Model.Status.Trained);

    }



    @Test
    public void reactToEventTest(){
        MessageBusImpl bus = new MessageBusImpl();
        Student student = new Student("Simba", "Computer Science", "MSc");
        Data data = new Data(Data.Type.Images, 1000);
        Model model = new Model("YOLO10", data, student);
        MicroService gpuService = new GPUService("hi", bus, gpu);
        Thread t1 = new Thread(gpuService);
        bus.register(gpuService);
        t1.start();


        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {}

        bus.sendEvent(new TrainModelEvent(model));


        assertEquals(model, gpu.getModel());

    }
}