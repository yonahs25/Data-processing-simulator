package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
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

	public TimeService( MessageBusImpl bus, long speed, long duration) {
		super("xyz", bus);
		this.speed = speed;
		this.duration = duration;
	}



	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, new TerminateCallback(this));
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				duration = duration-speed;
				if(duration == 0) {
					timer.cancel();
					System.out.println("terminating");
					sendBroadcast(new TerminateBroadcast());
				}else
					sendBroadcast(new TickBroadcast());
				// need to terminate
			}
		} ,5000,speed*5);
	}



}