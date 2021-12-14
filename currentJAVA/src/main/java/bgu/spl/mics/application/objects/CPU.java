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
    private Queue<DataBatch> waitingOnProcess;
    private int limit; // how much the have in queue
    private int workTime;
    private int startTick;
    private int batchesProcessed;


    public CPU(int cores, Cluster cluster) {
        this.cores = cores;
        this.cluster = cluster;
        currTick = 0;
        waitingOnProcess = new LinkedBlockingDeque<>();
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
        LinkedBlockingDeque<DataBatch> toTakeFrom = cluster.getWaitingUnprocessedBatches();
        synchronized(toTakeFrom) {
            while (!toTakeFrom.isEmpty() && waitingOnProcess.size() < limit) {
                if (waitingOnProcess.isEmpty()) {
                    startTick = currTick;
                }
                try {
                    waitingOnProcess.add((toTakeFrom.take()));
                } catch (InterruptedException e) {}
            }
        }
    }

    private void sendProcessedData()
    {
        if(!waitingOnProcess.isEmpty())
        {
            int timeToProcess = (32/cores);
            switch (waitingOnProcess.peek().getData().getType())
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
                cluster.putProcessedData(waitingOnProcess.remove());
                workTime+=timeToProcess;
                batchesProcessed++;
                cluster.incrementCpuProcessedData(1);
                cluster.incrementCpuTimeUsed(timeToProcess);
                //TODO
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
        return !waitingOnProcess.isEmpty();
    }

    public int getBatchesProcessed() {
        return batchesProcessed;
    }

    public int getWorkTime() {
        return workTime;
    }
}
