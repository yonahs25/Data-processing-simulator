package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

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
	private LinkedBlockingDeque<DataBatch> waitingUnprocessedBatches;
	private Vector<String> modelsTrained;
	private static class singeltonHolder
	{
		private static Cluster instance = new Cluster();
	}


	public Cluster() {
		Cpus = new ArrayList<>();
		Gpus = new ArrayList<>();
		returningProcessedBatches = new HashMap<>();
		waitingUnprocessedBatches = new LinkedBlockingDeque<>();
		modelsTrained = new Vector<String>();
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
	public static Cluster getInstance()
	{
		return Cluster.singeltonHolder.instance;
	}

	// the gpu call this function to send unprocessed data to the cluster
	public void sendUnprocessedData(List<DataBatch> list)
	{
		while (!list.isEmpty())
		{
			waitingUnprocessedBatches.add(list.remove(0));
		}

	}

	public LinkedBlockingDeque<DataBatch> getWaitingUnprocessedBatches()
	{
		return waitingUnprocessedBatches;
	}


	// cpu calling this function to send processed data to the cluster
	public void putProcessedData(DataBatch e)
	{
		//put dataBatch in his right queue
		GPU gpu = e.getOwner();
		returningProcessedBatches.get(gpu).add(e);

	}

	// get gpu queue
	public Queue<DataBatch> getGpuProcessed(GPU gpu){
		return returningProcessedBatches.get(gpu);
	}

	public void addModelTrained(String modelName)
	{
		modelsTrained.add(modelName);
	}



	public List<CPU> getCpus() {
		return Cpus;
	}

	public List<GPU> getGpus() {
		return Gpus;
	}

}

