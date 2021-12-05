package bgu.spl.mics;

/**
 * The message-bus is a shared object used for communication between
 * micro-services.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the micro-services in the system.
 * You must not alter any of the given methods of this interface. 
 * You cannot add methods to this interface.
 */
public interface MessageBus {

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     * @pre getEventSubscribers(type).contains(m)==false
     * @post getEventSubscribers(type).contains(m)==true
     *       if @pre getEventSubscribers(type) == null then @post getEventSubscribers(type).instanceof(List)
     */
    <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m);


    ///**
    // * creates new list for this event if there isn't
    // * @param type type of event
    // * @param <T>
    // * @pre none
    // * @post getEventSubscribers(type) != null
    // * @return list of subscribed to this event
    // */
    //<T> List<T> getEventSubscribers(Class<? extends Event<T>> type);


    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @param type 	The type to subscribe to.
     * @param m    	The subscribing micro-service.
     * @pre getBroadcastSubscribers(type).contains(m)==false
     * @post getBroadcastSubscribers(type).contains(m)==true
     *       if @pre getBroadcastSubscribers(type) == null then @post getBroadcastSubscribers(type).instanceof(List)
     *
     */
    void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m);

    ///**
    // * creates new list for broadcast if there isnt
    // * @param type type of event
    // * @param <T>
    // * @pre none
    // * @post getBroadcastSubscribers(type) != null
    // * @return list of subscribed to this broadcast
    // */
    //<T> List<T> getBroadcastSubscribers(Class<? extends Broadcast> type);


    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     * @pre
     * @post
     */
    <T> void complete(Event<T> e, T result);

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b 	The message to added to the queues.
     * @pre none
     * @post for each (MicroService m: getBroadCastSubscribers) @pre getMicroServiceQueue.size() + 1  == @post getMicroServiceQueue.size()
     */
    void sendBroadcast(Broadcast b);

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     * @param <T>    	The type of the result expected by the event and its corresponding future object.
     * @param e     	The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
     * @pre
     * @post
     */
    <T> Future<T> sendEvent(Event<T> e);

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to create a queue for.
     * @pre getMicroServiceQueue(m) == null
     * @post getMicroServiceQueue(m).size() == 0
     */
    void register(MicroService m);

    ///**
    // *
    // * @param m the micro-service with the queue we want
    // *
    // * @return queue if exists in Bus, else null
    // */
    //MicroService getMicroServiceQueue(MicroService m);//**********//

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     * @param m the micro-service to unregister.
     * @pre
     * @post if @pre getMicroServiceQueue(m) != null queue = @pre getMicroServiceQueue(m) , queue.size() == 0
     *       get MicroServiceQueue(m) == null
     */
    void unregister(MicroService m);

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     * @pre
     * @post
     */
    Message awaitMessage(MicroService m) throws InterruptedException;

    boolean isMicroServiceRegistered(MicroService m);

    <T> boolean isMicroServiceInEvent( Class<? extends Event<T>> type , MicroService m);

    <T> boolean isMicroServiceInBroadcast( Class<? extends Broadcast> type , MicroService m );

    <T> boolean didMicroServiceReceiveBroadcast(Broadcast type , MicroService m);

    <T> boolean didMicroServiceReceiveEvent(Event<T> type , MicroService m);

    
}
