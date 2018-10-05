package org.pipservices.messaging.queues;

import java.util.*;

import org.pipservices.commons.errors.ApplicationException;
import org.pipservices.commons.run.*;

/**
 * Interface for asynchronous message queues.
 * <p>
 * Not all queues may implement all the methods.
 * Attempt to call non-supported method will result in NotImplemented exception.
 * To verify if specific method is supported consult with {@link MessagingCapabilities}.
 * 
 * @see MessageEnvelop
 * @see MessagingCapabilities
 */
public interface IMessageQueue extends IOpenable {

	/**
	 * Gets the queue name
	 * 
	 * @return the queue name.
	 */
	String getName();

	/**
	 * Gets the queue capabilities
	 * 
	 * @return the queue's capabilities object.
	 */
	MessagingCapabilities getCapabilities();

	/**
	 * Gets the current number of messages in the queue to be delivered.
	 * 
	 * @return number of messages.
	 */
	Long getMessageCount();

	/**
	 * Sends a message into the queue.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param envelop       a message envelop to be sent.
	 * @throws ApplicationException when error occured.
	 */
	void send(String correlationId, MessageEnvelop envelop) throws ApplicationException;

	// void send(String correlationId, String messageType, String message) throws
	// ApplicationException;
	/**
	 * Sends an object into the queue. Before sending the object is converted into
	 * JSON string and wrapped in a MessageEnvelop.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param messageType   a message type
	 * @param message       an object value to be sent
	 * @throws ApplicationException when error occured.
	 * 
	 * @see #send(String, MessageEnvelop)
	 */
	void sendAsObject(String correlationId, String messageType, Object message) throws ApplicationException;

	/**
	 * Peeks a single incoming message from the queue without removing it. If there
	 * are no messages available in the queue it returns null.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @return a message envelop object.
	 * @throws ApplicationException when error occured.
	 */
	MessageEnvelop peek(String correlationId) throws ApplicationException;

	/**
	 * Peeks multiple incoming messages from the queue without removing them. If
	 * there are no messages available in the queue it returns an empty list.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param messageCount  a maximum number of messages to peek.
	 * @return a list with messages.
	 * @throws ApplicationException when error occured.
	 */
	List<MessageEnvelop> peekBatch(String correlationId, int messageCount) throws ApplicationException;

	/**
	 * Receives an incoming message and removes it from the queue.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param waitTimeout   a timeout in milliseconds to wait for a message to come.
	 * @return a message envelop object.
	 * @throws ApplicationException when error occured.
	 */
	MessageEnvelop receive(String correlationId, long waitTimeout) throws ApplicationException;

	/**
	 * Renews a lock on a message that makes it invisible from other receivers in
	 * the queue. This method is usually used to extend the message processing time.
	 * 
	 * @param message     a message to extend its lock.
	 * @param lockTimeout a locking timeout in milliseconds.
	 * @throws ApplicationException when error occured.
	 */
	void renewLock(MessageEnvelop message, long lockTimeout) throws ApplicationException;

	/**
	 * Permanently removes a message from the queue. This method is usually used to
	 * remove the message after successful processing.
	 * 
	 * @param message a message to remove.
	 * @throws ApplicationException when error occured.
	 */
	void complete(MessageEnvelop message) throws ApplicationException;

	/**
	 * Returns message into the queue and makes it available for all subscribers to
	 * receive it again. This method is usually used to return a message which could
	 * not be processed at the moment to repeat the attempt. Messages that cause
	 * unrecoverable errors shall be removed permanently or/and send to dead letter
	 * queue.
	 * 
	 * @param message a message to return.
	 * @throws ApplicationException when error occured.
	 */
	void abandon(MessageEnvelop message) throws ApplicationException;

	/**
	 * Permanently removes a message from the queue and sends it to dead letter
	 * queue.
	 * 
	 * @param message a message to be removed.
	 * @throws ApplicationException when error occured.
	 */
	void moveToDeadLetter(MessageEnvelop message) throws ApplicationException;

	/**
	 * Listens for incoming messages and blocks the current thread until queue is
	 * closed.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param receiver      a receiver to receive incoming messages.
	 * @throws ApplicationException when error occured.
	 * 
	 * @see IMessageReceiver
	 * @see #receive(String, long)
	 */
	void listen(String correlationId, IMessageReceiver receiver) throws ApplicationException;

	/**
	 * Listens for incoming messages without blocking the current thread.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param receiver      a receiver to receive incoming messages.
	 * 
	 * @see #listen(String, IMessageReceiver)
	 * @see IMessageReceiver
	 */
	void beginListen(String correlationId, IMessageReceiver receiver);

	/**
	 * Ends listening for incoming messages. When this method is call listen()
	 * unblocks the thread and execution continues.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @throws ApplicationException when error occured.
	 */
	void endListen(String correlationId) throws ApplicationException;
}
