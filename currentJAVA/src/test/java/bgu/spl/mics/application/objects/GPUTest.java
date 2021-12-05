package bgu.spl.mics.application.objects;

import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

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
        Student student = new Student("Simba", "Computer Science", "MSc");
        Data data = new Data(Data.Type.Images, 200000);
        Model model = new Model("YOLO10", data, student);
        gpu.setModel(model);
        gpu.updateTick();
        assertNotEquals(gpu.getUnprocessedData().size(), 200000/1000);
    }

    @Test
    public void getProcessed(){
        Data data = new Data(Data.Type.Images, 200000);
        DataBatch databatch = new DataBatch(data, 0, gpu);
        cluster.putProcessedData(databatch);
        gpu.updateTick();
        assertEquals(1, gpu.getProcessedData().size());
        gpu.updateTick();
        assertEquals(0, gpu.getProcessedData().size());
        assertEquals(1, data.getProcessed());
    }


    @Test
    public void finishedTraining(){
        Student student = new Student("Simba", "Computer Science", "MSc");
        Data data = new Data(Data.Type.Images, 1000);
        Model model = new Model("YOLO10", data, student);
        gpu.setModel(model);
        DataBatch databatch = gpu.getUnprocessedData().get(0);
        gpu.updateTick();
        cluster.putProcessedData(databatch);
        gpu.updateTick();
        assertEquals(gpu.getModel().getStatus(), Model.Status.Trained);

    }


}