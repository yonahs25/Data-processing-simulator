package bgu.spl.mics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private HashMap<MicroService, Queue> microServiceQueue = new HashMap();
	private HashMap<Class<? extends Broadcast>, LinkedList<MicroService>> BroadcastList = new HashMap();
	private HashMap<Class<? extends Event>,LinkedList<MicroService>> EventList = new HashMap<>();

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// TODO Auto-generated method stub

	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub
		//resolve of future

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
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

	public <T> boolean didMicroServiceReceiveEvent(Event<T> type , MicroService m) {
		return true; //TODO finish
	}
}
