package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {

    private Data data;
    private long start_index;
    private GPU owner;
    //private  int startTick;


    public DataBatch(Data data, long start_index, GPU gpu) {
        this.data = data;
        this.start_index = start_index;
        owner = gpu;
    }

    public Data getData() {
        return data;
    }

    public GPU getOwner() {
        return owner;
    }
}
