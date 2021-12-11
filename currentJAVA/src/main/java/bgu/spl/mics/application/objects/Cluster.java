package bgu.spl.mics.application.objects;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private List<CPU> Cpus;
	private List<GPU> Gpus;
	private HashMap<GPU, ConcurrentLinkedDeque<DataBatch>> returningProcessedBatches;
	 //HashMap<CPU, Queue<DataBatch>> waitingUnprocessedBatches;
	private ConcurrentLinkedDeque<DataBatch> waitingUnprocessedBatches;
	private Vector<String> modelTrained;
	private AtomicInteger dataProcessedCpu;
	private AtomicInteger timeUnitsCpu;
	private AtomicInteger timeUnitsGpu;

	public Cluster() {
		Cpus = new ArrayList<>();
		Gpus = new ArrayList<>();
		returningProcessedBatches = new HashMap<>();
		waitingUnprocessedBatches = new ConcurrentLinkedDeque<>();
		modelTrained = new Vector<String>();
		dataProcessedCpu = new AtomicInteger(0);
		timeUnitsCpu = new AtomicInteger(0);
		timeUnitsGpu = new AtomicInteger(0);
	}

	public void registerGpu(GPU gpu)
	{
		Gpus.add(gpu);
		returningProcessedBatches.put(gpu,new ConcurrentLinkedDeque<>());
	}

	public void registerCpu(CPU cpu)
	{
		Cpus.add(cpu);
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		//TODO: Implement this
		return null;
	}

	public void addUnprocessedData(List<DataBatch> list)
	{
		while (!list.isEmpty())
		{
			waitingUnprocessedBatches.add(list.remove(0));
		}

	}



	//TODO make a function subscribe to cluster

	//add data from big queue to cpu queue
	public void getUnprocessedData(List<DataBatch> list){
		//TODO added each dataBatch from list to queues

		//wrong need to delete
		for (DataBatch e : list){
			waitingUnprocessedBatches.add(e);
		}
	}
	// ????????????????????
	public Queue<DataBatch> getWaitingUnprocessedBatches() {
		return waitingUnprocessedBatches;
	}


	// cpu calling this function to send processed data to the cluster
	public void putProcessedData(DataBatch e){
		//put dataBatch in his right queue
		GPU gpu = e.getOwner();
		returningProcessedBatches.get(gpu).add(e);

	}

	// get gpu queue
	public Queue<DataBatch> getGpuProcessed(GPU gpu){
		return returningProcessedBatches.get(gpu);
	}


	//can make a clone to avoid long syncronize
	public Queue<DataBatch> getProccesedData(GPU gpu) {
		return returningProcessedBatches.get(gpu);
	}

	public void addModelTrained(String modelName)
	{
		modelTrained.add(modelName);
	}

	public  void  incrementGpuTimeUsed(int time)
	{
		int oldVal;
		int newVal;
		do
		{
			oldVal = timeUnitsGpu.get();
			newVal = oldVal + time ;
		}while (!timeUnitsGpu.compareAndSet(oldVal,newVal));
	}

	public  void  incrementCpuTimeUsed(int time)
	{
		int oldVal;
		int newVal;
		do
		{
			oldVal = timeUnitsCpu.get();
			newVal = oldVal + time ;
		}while (!timeUnitsCpu.compareAndSet(oldVal,newVal));
	}

}

