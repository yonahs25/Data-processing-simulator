package bgu.spl.mics;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private final ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> microServiceQueue = new ConcurrentHashMap();
	private final ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedDeque<MicroService>> BroadcastList = new ConcurrentHashMap();
	private final ConcurrentHashMap<Class<? extends Event>,LinkedBlockingDeque<MicroService>> EventList = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Event,Future> eventToFuture = new ConcurrentHashMap<>();
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
			EventList.putIfAbsent(type, new LinkedBlockingDeque<MicroService>()); // need to change linked list

		try {
			EventList.get(type).put(m);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m)
	{

		if (BroadcastList.get(type) == null)
			BroadcastList.putIfAbsent(type, new ConcurrentLinkedDeque<MicroService>()); // need to change linked list
		BroadcastList.get(type).add(m);


	}

	@Override
	public <T> void complete(Event<T> e, T result)
	{
			Future future = eventToFuture.get(e);
			if (future == null)
				System.out.println("future null" + e.getClass());
			future.resolve(result);
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


	@Override
	public <T> Future<T> sendEvent(Event<T> e)
	{
		if(e.getClass() == TrainModelEvent.class || e.getClass() == TestModelEvent.class)
		{
			MicroService m = null;
			try {
				m = EventList.get(e.getClass()).take();
			} catch (InterruptedException ex) {}
			microServiceQueue.get(m).add(e);
			EventList.get(e.getClass()).add(m);
		}

//		else if(e.getClass() == PublishResultsEvent.class)
		else
		{
			boolean done = false;
			// taking the event queue of the event class
			LinkedBlockingDeque<MicroService> eventQ = EventList.get(e.getClass());
				while (!eventQ.isEmpty() && !done) {
					MicroService m = null;
					try {
						m = eventQ.take();
					} catch (InterruptedException ex) {
					}
					LinkedBlockingDeque myQ = microServiceQueue.get(m);
					if (myQ == null) {
						eventQ.remove(m);
					} else {
						myQ.add(e);
						try {
							eventQ.put(m);
						} catch (InterruptedException ex){}
						done = true;

					}
				}
			}

		Future<T> future = new Future<T>();
			eventToFuture.put(e, future);

		return  future;

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
		LinkedBlockingDeque<MicroService> thisQ = EventList.get(type.getClass());
		for (MicroService m : thisQ){
			LinkedBlockingDeque<Message> myQ = microServiceQueue.get(m);
			if (myQ.contains(type))
				return true;
		}
		return false;

//		MicroService m = EventList.get(type.getClass()).getLast();
//		if(microServiceQueue.get(m).contains(type))
//			return true;
//		return false;
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
