package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.TerminateCallback;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private final long speed;
	private long duration;
	private boolean canStart;
	int howManyToSubscribe;

	public void setHowManyToSubscribe(int howManyToSubscribe) {
		this.howManyToSubscribe = howManyToSubscribe;
	}

	public void setCanStart(boolean canStart) {
		this.canStart = canStart;
	}

	public int getHowManyToSubscribe() {
		return howManyToSubscribe;
	}

	public TimeService(long speed, long duration)
	{
		super("timer");
		boolean canStart = false;
		this.speed = speed;
		this.duration = duration;
		howManyToSubscribe = 0;
	}

	@Override
	protected void initialize()
	{
		//MessageBusImpl.getInstance().register(this);
		subscribeBroadcast(TerminateBroadcast.class, new TerminateCallback(this));

		while(!canStart) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		System.out.println("im starting");

		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				duration = duration-speed;
				if(duration <= 0)
				{
					System.out.println("im done");
					timer.cancel();
					sendBroadcast(new TerminateBroadcast());
				}
				else
					sendBroadcast(new TickBroadcast());
			}
		} ,100,speed);
	}



}