package org.pipservices3.messaging.queues;

/**
 * Data object that contains supported capabilities of a message queue.
 * If certain capability is not supported a queue will throw NotImplemented exception.
 */
public class MessagingCapabilities {
	private final boolean _messageCount;
	private final boolean _send;
	private final boolean _receive;
	private final boolean _peek;
	private final boolean _peekBatch;
	private final boolean _renewLock;
	private final boolean _abandon;
	private final boolean _deadLetter;
	private final boolean _clear;

	/**
	 * Creates a new instance of the capabilities object.
	 * 
	 * @param messageCount true if queue supports reading message count.
	 * @param send         true if queue is able to send messages.
	 * @param receive      true if queue is able to receive messages.
	 * @param peek         true if queue is able to peek messages.
	 * @param peekBatch    true if queue is able to peek multiple messages in one
	 *                     batch.
	 * @param renewLock    true if queue is able to renew message lock.
	 * @param abandon      true if queue is able to abandon messages.
	 * @param deadLetter   true if queue is able to send messages to dead letter
	 *                     queue.
	 * @param clear        true if queue can be cleared.
	 */
	public MessagingCapabilities(boolean messageCount, boolean send, boolean receive, boolean peek, boolean peekBatch,
			boolean renewLock, boolean abandon, boolean deadLetter, boolean clear) {
		_messageCount = messageCount;
		_send = send;
		_receive = receive;
		_peek = peek;
		_peekBatch = peekBatch;
		_renewLock = renewLock;
		_abandon = abandon;
		_deadLetter = deadLetter;
		_clear = clear;
	}

	/**
	 * Informs if the queue is able to read number of messages.
	 * 
	 * @return true if queue supports reading message count.
	 */
	public boolean canMessageCount() {
		return _messageCount;
	}

	/**
	 * Informs if the queue is able to send messages.
	 * 
	 * @return true if queue is able to send messages.
	 */
	public boolean canSend() {
		return _send;
	}

	/**
	 * Informs if the queue is able to receive messages.
	 * 
	 * @return true if queue is able to receive messages.
	 */
	public boolean canReceive() {
		return _receive;
	}

	/**
	 * Informs if the queue is able to peek messages.
	 * 
	 * @return true if queue is able to peek messages.
	 */
	public boolean canPeek() {
		return _peek;
	}

	/**
	 * Informs if the queue is able to peek multiple messages in one batch.
	 * 
	 * @return true if queue is able to peek multiple messages in one batch.
	 */
	public boolean canPeekBatch() {
		return _peekBatch;
	}

	/**
	 * Informs if the queue is able to renew message lock.
	 * 
	 * @return true if queue is able to renew message lock.
	 */
	public boolean canRenewLock() {
		return _renewLock;
	}

	/**
	 * Informs if the queue is able to abandon messages.
	 * 
	 * @return true if queue is able to abandon.
	 */
	public boolean canAbandon() {
		return _abandon;
	}

	/**
	 * Informs if the queue is able to send messages to dead letter queue.
	 * 
	 * @return true if queue is able to send messages to dead letter queue.
	 */
	public boolean canDeadLetter() {
		return _deadLetter;
	}

	/**
	 * Informs if the queue can be cleared.
	 * 
	 * @return true if queue can be cleared.
	 */
	public boolean canClear() {
		return _clear;
	}
}
