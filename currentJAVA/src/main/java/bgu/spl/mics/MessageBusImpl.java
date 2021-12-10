package bgu.spl.mics;

import java.util.LinkedList;
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
		// Future future = e.getFuture();
		// future.resolve(result);
		//resolve of future

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// if it tickBroadcast it need to bypass all the events
		for (MicroService m : BroadcastList.get(b.getClass())){
			microServiceQueue.get(m).add(b);
		}

	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> future = new Future<T>(); // where to store it?
		// add to every event field future which will contains his personal future
		// e.setFuture(future);
		MicroService m = EventList.get(e.getClass()).remove(); // remove the head
		microServiceQueue.get(m).add(e); // add e to m queue
		EventList.get(e.getClass()).add(m); // add the removed m to the tail for round robbing pattern
		// for now the future is empty until the complete method will be called
		return  future; // for the student now he can loop this future untill it will be resolve

	}

	@Override
	public void register(MicroService m) {
		microServiceQueue.putIfAbsent(m, new ConcurrentLinkedQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		microServiceQueue.remove(m); //TODO check if need to do more
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		//TODO take blocking queue
		return null;
	}


	public boolean isMicroServiceRegistered(MicroService m){
		if(microServiceQueue.get(m) != null )
			return true;
		return false;
	}

	public <T> boolean isMicroServiceInEvent(Class<? extends Event<T>> type , MicroService m){
		if(EventList.get(type).contains(m))
			return true;
		return false;
	}

	public <T> boolean isMicroServiceInBroadcast( Class<? extends Broadcast> type , MicroService m){
		if(BroadcastList.get(type).contains(m))
			return true;
		return false;
	}

	public <T> boolean didMicroServiceReceiveBroadcast(Broadcast type , MicroService m){
		if(microServiceQueue.get(m).contains(type))
			return  true;
		return false;
	}

	public <T> boolean didMicroServiceReceiveEvent(Event<T> type , MicroService m) {
		if(microServiceQueue.get(m).contains(type))
			return  true;
		return false;
	}

	public <T> boolean wasEventSent(Event<T> type) {
		// every microservice that receive event need to be removed from the list and add to the end for the round robbing pattern
		MicroService m = EventList.get(type.getClass()).getLast();
		if(microServiceQueue.get(m).contains(type))
			return true;
		return false;
	}

	public <T> boolean wasBroadcastSent(Broadcast type) {
		boolean ans = true;
		for(int i = 0; i < BroadcastList.get(type.getClass()).size() && ans ;i++){
			MicroService m = BroadcastList.get(type.getClass()).get(i);
			if(!microServiceQueue.get(m).contains(type))
				ans = false;
		}
		return ans;

	}
}
