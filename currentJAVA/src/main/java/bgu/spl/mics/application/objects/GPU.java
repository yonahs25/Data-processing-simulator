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
    enum Type {RTX3090, RTX2080, GTX1080}

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

    /**
     *
     * @param model
     * processing model it got from the bus
     * checking if the model was trained or need to be trained and acting accordingly
     * @pre unprocessedData.size()==0
     * @pre this.model == null && other.model != null
     * @post this.model.getStatus() != @pre this.model.getStatus()
     *
     */
    public void setModel(Model model) {
        if(model.getStatus() == Model.Status.PreTrained) {
            this.model = model;
            Data data = model.getData();
            //need to deal with Incomplete number of data (4876)
            int amountBatches = data.getSize() / 1000;
            for (int i = 0; i < amountBatches; i++) {
                unprocessedData.add(new DataBatch(data, i * 1000, this));
            }
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
     * @inv this.model.getStatus()==Training
     * @pre unprocessedData.size()>0
     * // k= number of dataBatches to send
     * @post unprocessedData.size() == (@pre unprocessedData.size() - k)
     */
    private void sendUnprocessed(){
        //need to check how to deal with empty unprocessedData.
        List<DataBatch> toSend = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            toSend.add(unprocessedData.remove(0));
        }
        cluster.getUnprocessedData(toSend);
    }

    /**
     * processing data, checking if model is done and sending back to bus
     * @inv this.model.getStatus()==Training
     * @pre none
     * @post
     */
    private void processData(){
        if (currTick - startTick >= tickTimer){
            if (!processedData.isEmpty()){
                DataBatch removed = processedData.remove();
                removed.getData().increment();
                startTick = currTick;

                //finished training
                //if removed.getData().getProcessed == max
                //model.setStatus(Model.Status.Trained);
                // return model to bus


            }
        }
    }

    /**
     * get processed data from cluster to start working on
     * @inv this.model.getStatus()==Training
     * @pre none
     * @post processedData.size() == @pre processedData.size() + myList.size()
     */
    private void getDataFromCluster(){
        if(processedData.isEmpty())
            startTick = currTick;
        List<DataBatch> myList = cluster.getGpuProcessed(this);
        while (processedData.size() != limit && !myList.isEmpty()){
            processedData.add(myList.remove(0));
        }
    }

    /**
     * test model
     * @pre model.getStatus() == Trained
     * @post model.getStatus() == Tested
     */
    private void testModel(){
        Random rn = new Random();
        int random =rn.nextInt(100);
        if (model.getStudent().getStatus() == Student.Degree.MSc){
            if (random<10)
                model.setResults(Model.Results.Good);
            else
                model.setResults(Model.Results.Bad);
        } else{
            if (random<20)
                model.setResults(Model.Results.Good);
            else
                model.setResults(Model.Results.Bad);
        }
        model.setStatus(Model.Status.Tested);
    }

    /**
     * main function, service is updating time for gpu and doing main job here
     * @inv this.model.getStatus()==Training
     */
    public void updateTick(){
        currTick++;
        if (model.getStatus() == Model.Status.Training){
            sendUnprocessed();
            getDataFromCluster();
            processData();
        }

    }
}