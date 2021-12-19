package bgu.spl.mics;

import bgu.spl.mics.application.services.TimeService;

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

	private final ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> microServiceQueue = new ConcurrentHashMap();
	private final ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedDeque<MicroService>> BroadcastList = new ConcurrentHashMap();
	private final ConcurrentHashMap<Class<? extends Event>,LinkedBlockingDeque<MicroService>> EventList = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Event,Future> eventToFuture = new ConcurrentHashMap<>();
	private AtomicInteger subscribed =  new AtomicInteger(0);
	TimeService timer;
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
			EventList.putIfAbsent(type, new LinkedBlockingDeque<MicroService>());
		try {
			EventList.get(type).put(m);
		} catch (InterruptedException e) {}
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m)
	{
		if (BroadcastList.get(type) == null)
			BroadcastList.putIfAbsent(type, new ConcurrentLinkedDeque<>());
		BroadcastList.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result)
	{
			Future future = eventToFuture.get(e);
			future.resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b)
	{
		for (MicroService m : BroadcastList.get(b.getClass()))
		{
			LinkedBlockingDeque list = microServiceQueue.get(m);
			if (list!=null)
				list.add(b);
		}
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e)
	{
		Future<T> future = new Future<T>();
		eventToFuture.put(e, future);
			boolean done = false;
			// taking the event queue of the event class
			LinkedBlockingDeque<MicroService> eventQ = EventList.get(e.getClass());
				while (!eventQ.isEmpty() && !done) {
					MicroService m = null;
					try {
						m = eventQ.take();
					} catch (InterruptedException ex) {
					}
					LinkedBlockingDeque<Message> myQ = microServiceQueue.get(m);
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
		return  future;
	}
	private void incrementRegistered(){
		int oldVal;
		int newVal;
		do {
			oldVal = subscribed.get();
			newVal = oldVal + 1;
		} while (!subscribed.compareAndSet(oldVal, newVal));
	}

	@Override
	public void register(MicroService m)
	{
		System.out.println("here2");
		System.out.println(subscribed.get());

		LinkedBlockingDeque e = microServiceQueue.putIfAbsent(m, new LinkedBlockingDeque<>());
		if (e == null) {
			if (m.getClass() == TimeService.class)
				timer = (TimeService) m;
			else
				incrementRegistered();
			if (timer != null && subscribed.get() == timer.getHowManyToSubscribe()){
				timer.setCanStart(true);
			}

		}
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


	public boolean isMicroServiceRegistered(MicroService m) //
	{
		return microServiceQueue.get(m) != null;
	}

	public <T> boolean isMicroServiceInEvent(Class<? extends Event<T>> type , MicroService m) //
	{
		return EventList.get(type).contains(m);
	}

	public <T> boolean isMicroServiceInBroadcast( Class<? extends Broadcast> type , MicroService m) //
	{
		return BroadcastList.get(type).contains(m);
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
