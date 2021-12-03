package bgu.spl.mics.application.objects;
import java.util.*;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	List<CPU> Cpus;
	List<GPU> Gpus;
	HashMap<GPU, List<DataBatch>> returningProcessedBatches;
	//HashMap<CPU, Queue<DataBatch>> waitingUnprocessedBatches;
	Queue<DataBatch> waitingUnprocessedBatches;

	public Cluster() {
		Cpus = new ArrayList<>();
		Gpus = new ArrayList<>();
		returningProcessedBatches = new HashMap<>();
		waitingUnprocessedBatches = new LinkedList<>();
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		//TODO: Implement this
		return null;
	}

	public void getUnprocessedData(List<DataBatch> list){
		//TODO added each dataBatch from list to queues

		for (DataBatch e : list){
			waitingUnprocessedBatches.add(e);
		}
	}
	// ????????????????????
	public Queue<DataBatch> getWaitingUnprocessedBatches() {
		return waitingUnprocessedBatches;
	}

	public void putProcessedData(DataBatch e){
		//put dataBatch in his right queue
		List toAddTo = returningProcessedBatches.get(e.getOwner());
		toAddTo.add(e);
	}

	public List<DataBatch> getGpuProcessed(GPU gpu){
		return returningProcessedBatches.get(gpu);
	}
}
