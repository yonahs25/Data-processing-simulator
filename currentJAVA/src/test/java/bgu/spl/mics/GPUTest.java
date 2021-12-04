package bgu.spl.mics.application.objects;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class GPUTest {

    private static GPU gpu1;
    private static GPU gpu2;
    private static GPU gpu3;
    private static Cluster cluster;

    @Before
    public void setUp(){
        cluster=new Cluster();
        gpu1= new GPU(RTX3090,cluster);
        gpu2= new GPU(RTX2080,cluster)
        gpu3= new GPU(GTX1080,cluster)
    }


}