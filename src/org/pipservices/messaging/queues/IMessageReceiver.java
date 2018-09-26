package org.pipservices.messaging.queues;

/**
 * Callback interface to receive incoming messages.
 * <p>
 * ### Example ###
 * <pre>
 * {@code
 * class MyMessageReceiver implements IMessageReceiver {
 *   public void receiveMessage(MessageEnvelop envelop, IMessageQueue queue) {
 *       System.out.println("Received message: " + envelop.getMessageAsString());
 *       ...
 *   }
 * }
 * 
 * MemoryMessageQueue messageQueue = new MemoryMessageQueue();
 * messageQueue.listen("123", new MyMessageReceiver());
 * 
 * messageQueue.open("123");
 * messageQueue.send("123", new MessageEnvelop(null, "mymessage", "ABC")); // Output in console: "ABC"
 * });
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
	 * @see MessageEnvelop
	 * @see IMessageQueue
	 */
	void receiveMessage(MessageEnvelop message, IMessageQueue queue);
}
