package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int cores;
    private Cluster cluster;
    private int currTick;
    Queue<DataBatch> waitingOnProcess;
    int limit; // how much the have in queue


    public CPU(int cores, Cluster cluster) {
        this.cores = cores;
        this.cluster = cluster;
        currTick = 0;
        waitingOnProcess = new LinkedList<>();
        int limit = cores/4;
    }


    /**
     * @pre none
     * @post waitingOnProcess.size() == @pre waitingOnProcess.size() + toTakeFrom.size()
     */

    private void getProcessed(){

        Queue<DataBatch> toTakeFrom = cluster.getWaitingUnprocessedBatches();
        while (!toTakeFrom.isEmpty() && waitingOnProcess.size() < limit){
            waitingOnProcess.add((toTakeFrom.remove()));
        }



        DataBatch curr = waitingOnProcess.peek(); //TODO write check if this data has been processed
        //if dataBatch has been processed call cluster.putProcessedData

    }

    /**
     *
     */

    public void updateTime(){
        currTick++;
        //calling other functions
        getProcessed();

    }

}
