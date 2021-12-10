package bgu.spl.mics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Message>> microServiceQueue = new ConcurrentHashMap();
	private ConcurrentHashMap<Class<? extends Broadcast>, LinkedList<MicroService>> BroadcastList = new ConcurrentHashMap(); //TODO change linked list to something better
	private ConcurrentHashMap<Class<? extends Event>,LinkedList<MicroService>> EventList = new ConcurrentHashMap<>();


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (EventList.get(type) == null)
			EventList.put(type, new LinkedList<MicroService>()); // need to change linked list

		EventList.get(type).add(m);

	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (BroadcastList.get(type) == null)
			BroadcastList.put(type, new LinkedList<MicroService>()); // need to change linked list

		EventList.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub
		//resolve of future

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		for (MicroService m : BroadcastList.get(b.getClass())){
			microServiceQueue.get(m).add(b);
		}

	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {

		return null;
	}

	@Override
	public void register(MicroService m) {
		microServiceQueue.putIfAbsent(m, new ConcurrentLinkedQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		microServiceQueue.remove(m); //TODO check if
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		//TODO take blocking queue
		return null;
	}


	public boolean isMicroServiceRegistered(MicroService m){
		return true; //TODO finish
	}

	public <T> boolean isMicroServiceInEvent(Class<? extends Event<T>> type , MicroService m){
		return true; //TODO finish
	}

	public <T> boolean isMicroServiceInBroadcast( Class<? extends Broadcast> type , MicroService m){
		return true; //TODO finish
	}

	public <T> boolean didMicroServiceReceiveBroadcast(Broadcast type , MicroService m){
		return true; //TODO finish
	}

	public <T> boolean wasBroadcastSent(Broadcast type) {return true;} //TODO finish


	public <T> boolean didMicroServiceReceiveEvent(Event<T> type , MicroService m) {
		return true; //TODO finish
	}

	public <T> boolean wasEventSent(Event<T> type) {return true;} //TODO finish

}
