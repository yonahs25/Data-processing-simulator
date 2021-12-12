package bgu.spl.mics;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap<MicroService, BlockingDeque<Message>> microServiceQueue = new ConcurrentHashMap();
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedDeque<MicroService>> BroadcastList = new ConcurrentHashMap(); //TODO change linked list to something better
	private ConcurrentHashMap<Class<? extends Event>,ConcurrentLinkedDeque<MicroService>> EventList = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Event,Future> eventToFuture = new ConcurrentHashMap<>(); // check


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (EventList.get(type) == null)
			EventList.put(type, new ConcurrentLinkedDeque<MicroService>()); // need to change linked list

		EventList.get(type).add(m);

	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (BroadcastList.get(type) == null)
			BroadcastList.put(type, new ConcurrentLinkedDeque<MicroService>()); // need to change linked list
		BroadcastList.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future future = eventToFuture.get(e);
		future.resolve(result);

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		for (MicroService m : BroadcastList.get(b.getClass())) {
			microServiceQueue.get(m).add(b);
		}
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {

		if(e.getClass() == TrainModelEvent.class)
		{
			MicroService m = EventList.get(e.getClass()).remove();
			microServiceQueue.get(m).add(e);
			EventList.get(e.getClass()).add(m);
		}
		else if(e.getClass() == TestModelEvent.class)
		{
			MicroService m = EventList.get(e.getClass()).remove();
			microServiceQueue.get(m).add(e);
			EventList.get(e.getClass()).add(m);
		}
		else
		{
			MicroService m = EventList.get(e.getClass()).getFirst();
			microServiceQueue.get(m).addFirst(e);
		}

		Future<T> future = new Future<T>(); // where to store it?
		eventToFuture.put(e,future);
//		// add to every event field future which will contains his personal future
//		// e.setFuture(future);
//		//MicroService m = EventList.get(e.getClass()).remove(); // remove the head TODO check
//		microServiceQueue.get(m).add(e); // add e to m queue TODO check
//		EventList.get(e.getClass()).add(m); // add the removed m to the tail for round robbing pattern
//		// for now the future is empty until the complete method will be called
		return  future; // for the student now he can loop this future untill it will be resolve

	}

	@Override
	public void register(MicroService m) {
		microServiceQueue.putIfAbsent(m, new LinkedBlockingDeque<>());
	}

	@Override
	public void unregister(MicroService m) {
		microServiceQueue.remove(m); //TODO check if need to do more
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {

		return microServiceQueue.get(m).remove();
	}


	public boolean isMicroServiceRegistered(MicroService m){
		return microServiceQueue.get(m) != null;
	}

	public <T> boolean isMicroServiceInEvent(Class<? extends Event<T>> type , MicroService m){
		return EventList.get(type).contains(m);
	}

	public <T> boolean isMicroServiceInBroadcast( Class<? extends Broadcast> type , MicroService m){
		return BroadcastList.get(type).contains(m);
	}

	public <T> boolean didMicroServiceReceiveBroadcast(Broadcast type , MicroService m){
		return microServiceQueue.get(m).contains(type);
	}

	public <T> boolean didMicroServiceReceiveEvent(Event<T> type , MicroService m) {
		return microServiceQueue.get(m).contains(type);
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
		for(MicroService m : BroadcastList.get(type.getClass()))
		{
			if(!microServiceQueue.get(m).contains(type))
				ans = false;
		}
		return ans;
	}





}
