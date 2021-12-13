package bgu.spl.mics.application.objects;
import java.util.*;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;

    private int startTick;
    private int currTick;
    private List<DataBatch> unprocessedData;
    private Queue<DataBatch> processedData; //has a limit
    //private DataBatch workingOn;
    //how many processed data he can have in queue
    private final int limit;
    //time it takes to process data
    private  int tickTimer;
    private int missingData;
    private int workTime;
    private int batchesProcessed;



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
        missingData=0;
        workTime = 0;
        //workingOn = null;
    }

    public List<DataBatch> getUnprocessedData() {
        return unprocessedData;
    }

    public Queue<DataBatch> getProcessedData() {
        return processedData;
    }


    public Model getModel() {
        return model;
    }

    /**
     *
     * @param model
     * processing model it got from the bus
     * checking if the model was trained or need to be trained and acting accordingly
     * @pre  other.model != null
     * @post getModel().getStatus() != @pre other.model.getStatus() && getModel() == other.model
     *
     */
    public void setModel(Model model) {
        this.model = model;
        if(model.getStatus() == Model.Status.PreTrained) {
            Data data = model.getData();
            //need to deal with Incomplete number of data (4876)
            int amountBatches = data.getSize() / 1000;
            for (int i = 0; i < amountBatches; i++) {
                unprocessedData.add(new DataBatch(data, i * 1000, this));
            }
            ////
            if(amountBatches == 0)
                model.setStatus(Model.Status.Trained);
            else
                model.setStatus(Model.Status.Training);
        }
        else if (model.getStatus() == Model.Status.Trained){
            testModel();
        }
    }


    /**
     * sending list of unprocessed chunk to cluster
     */
    private void sendUnprocessed(){

        if(type == Type.RTX3090)
        {
            if(processedData.size() < 20)
            {
                List<DataBatch> toSend = new ArrayList<>();
                while ( !unprocessedData.isEmpty() && missingData < 16)
                {
                        missingData++;
                        toSend.add(unprocessedData.remove(0));
                }
                        cluster.sendUnprocessedData(toSend);
            }

        }
        else if(type == Type.RTX2080)
        {
            if(processedData.size() < 12)
            {
                List<DataBatch> toSend = new ArrayList<>();
                while ( !unprocessedData.isEmpty() && missingData < 8)
                {
                    missingData++;
                    toSend.add(unprocessedData.remove(0));
                }
                cluster.sendUnprocessedData(toSend);
            }
        }
        else
            if(processedData.size() < 6)
            {
                List<DataBatch> toSend = new ArrayList<>();
                while ( !unprocessedData.isEmpty() && missingData < 4)
                {
                    missingData++;
                    toSend.add(unprocessedData.remove(0));
                }
                cluster.sendUnprocessedData(toSend);
         }



//        //need to check how to deal with empty unprocessedData.
//        List<DataBatch> toSend = new ArrayList<>();
//        for (int i = 0; i < 10; i++){
//            if (!unprocessedData.isEmpty())
//            toSend.add(unprocessedData.remove(0));
//        }
//        cluster.getUnprocessedData(toSend);
    }


    /**
     * processing data, checking if model is done and sending back to bus
     */
    private void processData(){
        if (currTick - startTick >= tickTimer){
            if (!processedData.isEmpty())
            {
                DataBatch removed = processedData.remove();
                removed.getData().increment();
                startTick = currTick;
                cluster.incrementGpuTimeUsed(tickTimer);
                workTime+=tickTimer;
                batchesProcessed ++;

                //finished training
                if (removed.getData().getProcessed() == removed.getData().getSize())
                {
                    model.setStatus(Model.Status.Trained);
                    cluster.addModelTrained(model.getName());
                }

                // return model to bus


            }
        }
    }

    /**
     * get processed data from cluster to start working on
     */
    private void getDataFromCluster(){
        if(processedData.isEmpty())
            startTick = currTick;
        Queue<DataBatch> myList = cluster.getGpuProcessed(this);

        while (processedData.size() != limit && !myList.isEmpty())
        {
            missingData--;
            processedData.add(myList.remove());
        }
    }

    /**
     * test model
     */
    private void testModel(){
        Random rn = new Random();
        int random =rn.nextInt(100);
        if (model.getStudent().getStatus().equals(Student.Degree.MSc)){
            if (random<60)
                model.setResults(Model.Results.Good);
            else
                model.setResults(Model.Results.Bad);
        } else{
            if (random<80)
                model.setResults(Model.Results.Good);
            else
                model.setResults(Model.Results.Bad);
        }
        model.setStatus(Model.Status.Tested);
    }

    /**
     * main function, service is updating time for gpu and doing main job here
     * @pre none
     * @post currTick = @pre currTick + 1
     */
    public void updateTick(){
        currTick++;
        if (model != null && model.getStatus() == Model.Status.Training){
            sendUnprocessed();
            processData();
            getDataFromCluster();
        }

    }

    public int getWorkTime() {
        return workTime;
    }
}