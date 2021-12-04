package bgu.spl.mics.application.objects;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class CPUTest {

    private static CPU cpu1;
    private static CPU cpu2;
    private static Cluster cluster;

    @Before
    public void setUp(){
        cluster=new Cluster();
        cpu1 = new CPU(32,cluster);
        cpu2 = new CPU(16,cluster);
    }



}