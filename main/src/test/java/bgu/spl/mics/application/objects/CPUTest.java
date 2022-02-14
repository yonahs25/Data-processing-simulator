package bgu.spl.mics.application.objects;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class CPUTest {

    private static CPU cpu;
    private static Cluster cluster;

    @Before
    public void setUp() throws Exception {
        cluster=new Cluster();
        cpu = new CPU(32,cluster);
    }

    @Test
    public void testGetChunk() {
        cluster.registerCpu(cpu);
        GPU gpu = new GPU(GPU.Type.RTX2080, cluster);
        Data data = new Data(Data.Type.Images, 5);
        List<DataBatch> list = new LinkedList<>();
        list.add(new DataBatch(data,0, gpu));
        cluster.sendUnprocessedData(list);
        cpu.updateTime();
        assertTrue(cpu.isProcessing());
    }

    @Test
    public void testSendBack(){
        cluster.registerCpu(cpu);
        GPU gpu = new GPU(GPU.Type.RTX2080, cluster);
        cluster.registerGpu(gpu);
        Data data = new Data(Data.Type.Images, 5);
        List<DataBatch> list = new LinkedList<>();
        list.add(new DataBatch(data,0, gpu));
        cluster.sendUnprocessedData(list);
        cpu.updateTime();
        cpu.updateTime();
        cpu.updateTime();
        cpu.updateTime();
        cpu.updateTime();
        assertFalse(cpu.isProcessing());
    }
}
