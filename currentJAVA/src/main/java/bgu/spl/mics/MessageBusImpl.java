package bgu.spl.mics;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> microServiceQueue = new ConcurrentHashMap();
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedDeque<MicroService>> BroadcastList = new ConcurrentHashMap();
	private ConcurrentHashMap<Class<? extends Event>,LinkedBlockingDeque<MicroService>> EventList = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Event,Future> eventToFuture = new ConcurrentHashMap<>();
	private AtomicInteger currentGpuToSend = new AtomicInteger(0);
	private static class singeltonHolder
	{
		private static MessageBusImpl instance = new MessageBusImpl();
	}
	public static MessageBusImpl getInstance()
	{
		return singeltonHolder.instance;
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m)
	{
		if (EventList.get(type) == null)
			EventList.put(type, new LinkedBlockingDeque<MicroService>()); // need to change linked list

		EventList.get(type).add(m);

	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m)
	{
		if (BroadcastList.get(type) == null)
			BroadcastList.put(type, new ConcurrentLinkedDeque<MicroService>()); // need to change linked list
		BroadcastList.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result)
	{
		try {
			Future future = eventToFuture.get(e);
			future.resolve(result);
		} catch (Exception g){}


	}

	@Override
	public void sendBroadcast(Broadcast b)
	{
		for (MicroService m : BroadcastList.get(b.getClass())) {
			LinkedBlockingDeque me = microServiceQueue.get(m);
			if (me!=null)
				me.add(b);
		}
	}


	private void incrementGpuSpot(){
		int oldVal;
		int newVal;
		do {
			oldVal = currentGpuToSend.get();
			newVal = (oldVal + 1) % (EventList.get(TrainModelEvent.class).size());
		} while (!currentGpuToSend.compareAndSet(oldVal,newVal));
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e)
	{


		if(e.getClass() == TrainModelEvent.class)
		{
			MicroService m = null;
			try {
				m = EventList.get(e.getClass()).take();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			EventList.get(e.getClass()).add(m);
			microServiceQueue.get(m).add(e);

			//microServiceQueue.get(EventList.get(e.getClass()).getFirst()).add(e);
		}
		else if(e.getClass() == TestModelEvent.class)
		{
			MicroService m = null;
			try {
				m = EventList.get(e.getClass()).take();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			EventList.get(e.getClass()).add(m);
			microServiceQueue.get(m).add(e);
			//microServiceQueue.get(EventList.get(e.getClass()).getFirst()).add(e);
		}
		else
		{
			MicroService m = EventList.get(e.getClass()).getFirst();
			if(m!=null)
			microServiceQueue.get(m).addFirst(e);
		}

		Future<T> future = new Future<T>(); // where to store it?
		synchronized (eventToFuture) {
			eventToFuture.put(e, future);
		}
//		// add to every event field future which will contains his personal future
//		// e.setFuture(future);
//		//MicroService m = EventList.get(e.getClass()).remove(); // remove the head TODO check
//		microServiceQueue.get(m).add(e); // add e to m queue TODO check
//		EventList.get(e.getClass()).add(m); // add the removed m to the tail for round robbing pattern
//		// for now the future is empty until the complete method will be called
		return  future; // for the student now he can loop this future untill it will be resolve

	}

	@Override
	public void register(MicroService m)
	{
		microServiceQueue.putIfAbsent(m, new LinkedBlockingDeque<>());
	}

	@Override
	public void unregister(MicroService m)
	{
		microServiceQueue.remove(m); //TODO check if need to do more
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException
	{
		return microServiceQueue.get(m).take();
	}


	public boolean isMicroServiceRegistered(MicroService m)
	{
		return microServiceQueue.get(m) != null;
	}

	public <T> boolean isMicroServiceInEvent(Class<? extends Event<T>> type , MicroService m)
	{
		return EventList.get(type).contains(m);
	}

	public <T> boolean isMicroServiceInBroadcast( Class<? extends Broadcast> type , MicroService m)
	{
		return BroadcastList.get(type).contains(m);
	}

	public <T> boolean didMicroServiceReceiveBroadcast(Broadcast type , MicroService m)
	{
		return microServiceQueue.get(m).contains(type);
	}

	public <T> boolean didMicroServiceReceiveEvent(Event<T> type , MicroService m)
	{
		return microServiceQueue.get(m).contains(type);
	}

	public <T> boolean wasEventSent(Event<T> type)
	{
		MicroService m = EventList.get(type.getClass()).getLast();
		if(microServiceQueue.get(m).contains(type))
			return true;
		return false;
	}

	public <T> boolean wasBroadcastSent(Broadcast type)
	{
		boolean ans = true;
		for(MicroService m : BroadcastList.get(type.getClass()))
		{
			if(!microServiceQueue.get(m).contains(type))
				ans = false;
		}
		return ans;
	}





}
