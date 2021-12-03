package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}
    enum ModelType {Train, Test, None}

    private Type type;
    //private int startTick;
    private Model model;
    private Cluster cluster;

    private int startTick;
    private int currTick;
    private List<DataBatch> unprocessedData;
    private Queue<DataBatch> processedData; //has a limit
    //private DataBatch workingOn;
    private int limit;
    private int tickTimer;


    public GPU(Type type, Cluster cluster) {
        this.type = type;
        this.cluster = cluster;
        switch (type) {
            case RTX3090:
                limit = 32;
                tickTimer = 1;
                break;
            case RTX2080:
                limit = 16;
                tickTimer = 2;
                break;
            default:
                limit = 8;
                tickTimer = 4;
        }
        unprocessedData = new ArrayList<>();
        processedData = new LinkedList<>();
        model = null;
        currTick = 0;
        startTick = 0;
        //workingOn = null;
    }

    public void setModel(Model model) {
        this.model = model;
        Data data = model.getData();
        int amountBatches = data.getSize() / 1000;
        for (int i = 0; i <amountBatches; i++){
            unprocessedData.add(new DataBatch(data, i*1000,this));
        }
    }

    public void sendUnprocessed(){
        //need to check how to deal with empty unprocessedData.
        List<DataBatch> toSend = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            toSend.add(unprocessedData.remove(0));
        }
        cluster.getUnprocessedData(toSend);
    }

    public void processData(){
        if (currTick - startTick >= tickTimer){
            if (!processedData.isEmpty()){
                DataBatch removed = processedData.remove();
                removed.getData().increment();
                startTick = currTick;
                //if removed.getData().getProcessed == max
                // return model to bus
            }
        }
    }
    private void getDataFromCluster(){
        if(processedData.isEmpty())
            startTick = currTick;
        List<DataBatch> myList = cluster.getGpuProcessed(this);
        while (processedData.size() != limit && !myList.isEmpty()){
            processedData.add(myList.remove(0));
        }
    }
    public void updateTick(){
        currTick++;

    }
}