package bgu.spl.mics.application.objects;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int cores;
    private Cluster cluster;
    private int currTick;
    private int startTick;
    private Queue<DataBatch> batchesToBeProcessed;
    private int limit;
    private int workTime;
    private int batchesProcessed;


    public CPU(int cores, Cluster cluster) {
        this.cores = cores;
        this.cluster = cluster;
        currTick = 0;
        batchesToBeProcessed = new LinkedBlockingDeque<>();
        limit = cores/4;
        workTime = 0;
        startTick = 0;
        batchesProcessed = 0;
    }


    /**
     * @pre none
     * @post waitingOnProcess.size() != @pre waitingOnProcess.size()
     */

    private void getUnprocessedData()
    {
        LinkedBlockingDeque<DataBatch> UnprocessedBatchesFromCluster = cluster.getWaitingUnprocessedBatches();
            while (!UnprocessedBatchesFromCluster.isEmpty() && batchesToBeProcessed.size() < limit) {
                if (batchesToBeProcessed.isEmpty()) {
                    startTick = currTick;
                }
                try {
                    batchesToBeProcessed.add((UnprocessedBatchesFromCluster.take()));
                } catch (InterruptedException e) {}
            }
    }

    private void sendProcessedData()
    {
        if(!batchesToBeProcessed.isEmpty())
        {
            int timeToProcess = (32/cores);
            switch (batchesToBeProcessed.peek().getData().getType())
            {
                case Images:
                    timeToProcess*=4;
                    break;
                case Text:
                    timeToProcess*=2;
                    break;
                default:
                    break;
            }
            if (currTick-startTick >= timeToProcess)
            {
                cluster.putProcessedData(batchesToBeProcessed.remove());
                workTime+=timeToProcess;
                batchesProcessed++;
            }
        }
    }



    /**
     * @pre none
     * @post currTick == @pre currTick + 1
     */

    public void updateTime(){
        currTick++;
        //calling other functions
        getUnprocessedData();
        sendProcessedData();
    }

    /**
     * @pre none
     * @post none
     * @return if cpu is processing data
     */
    public boolean isProcessing (){
        return !batchesToBeProcessed.isEmpty();
    }

    public int getBatchesProcessed() {
        return batchesProcessed;
    }

    public int getWorkTime() {
        return workTime;
    }
}
