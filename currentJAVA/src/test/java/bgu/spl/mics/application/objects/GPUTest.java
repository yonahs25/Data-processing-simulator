package bgu.spl.mics.application.objects;

import org.junit.Before;
import org.junit.Test;

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
    public void setModel() {
        Model model = new Model()
    }

    @Test
    public void updateTick() {
    }
}