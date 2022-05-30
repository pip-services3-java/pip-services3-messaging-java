package org.pipservices3.messaging.queues;

/**
 * Callback interface to receive incoming messages.
 * <p>
 * ### Example ###
 * <pre>
 * {@code
 * class MyMessageReceiver implements IMessageReceiver {
 *   public void receiveMessage(MessageEnvelope envelop, IMessageQueue queue) {
 *       System.out.println("Received message: " + envelop.getMessageAsString());
 *       ...
 *   }
 * }
 * 
 * MemoryMessageQueue messageQueue = new MemoryMessageQueue();
 * messageQueue.listen("123", new MyMessageReceiver());
 * 
 * messageQueue.open("123");
 * messageQueue.send("123", new MessageEnvelope(null, "mymessage", "ABC")); // Output in console: "ABC"
 * }
 * </pre>
 */
public interface IMessageReceiver {
	/**
	 * Receives incoming message from the queue.
	 * 
	 * @param message an incoming message
	 * @param queue    a queue where the message comes from
	 * 
	 * @see MessageEnvelope
	 * @see IMessageQueue
	 */
	void receiveMessage(MessageEnvelope message, IMessageQueue queue);
}
