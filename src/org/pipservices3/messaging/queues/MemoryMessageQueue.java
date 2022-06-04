package org.pipservices3.messaging.queues;

import org.pipservices3.commons.config.ConfigParams;
import org.pipservices3.components.auth.CredentialParams;
import org.pipservices3.components.connect.ConnectionParams;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message queue that sends and receives messages within the same process by using shared memory.
 * <p>
 * This queue is typically used for testing to mock real queues.
 * <p>
 * ### Configuration parameters ###
 * <ul>
 * <li>name:                        name of the message queue
 * </ul>
 * <p>
 * ### References ###
 * <ul>
 * <li>*:logger:*:*:1.0           (optional) <a href="https://pip-services3-java.github.io/pip-services3-components-java/org/pipservices3/components/log/ILogger.html">ILogger</a> components to pass log messages
 * <li>*:counters:*:*:1.0         (optional) <a href="https://pip-services3-java.github.io/pip-services3-components-java/org/pipservices3/components/count/ICounters.html">ICounters</a> components to pass collected measurements
 * </ul>
 * <p>
 * ### Example ###
 * <pre>
 * {@code
 * MessageQueue queue = new MessageQueue("myqueue");
 *
 * queue.send("123", new MessageEnvelope(null, "mymessage", "ABC"));
 *
 * queue.receive("123", 0);
 * }
 * </pre>
 *
 * @see MessageQueue
 * @see MessagingCapabilities
 */
public class MemoryMessageQueue extends MessageQueue {
    private final List<MessageEnvelope> _messages = new ArrayList<>();
    private int _lockTokenSequence = 0;
    private final Map<Integer, LockedMessage> _lockedMessages = new HashMap<>();
    private boolean _opened = false;

    /**
     * Used to stop the listening process.
     */
    private boolean _cancel = false;
    private long _listenInterval = 1000;


    /**
     * Creates a new instance of the message queue.
     */
    public MemoryMessageQueue() {
        this(null);
    }

    /**
     * Creates a new instance of the message queue.
     *
     * @param name (optional) a queue name.
     * @see MessagingCapabilities
     */
    public MemoryMessageQueue(String name) {
        super(name);

        _capabilities = new MessagingCapabilities(true, true, true, true, true, true, true, false, true);
    }

    /**
     * Checks if the component is opened.
     *
     * @return true if the component has been opened and false otherwise.
     */
    @Override
    public boolean isOpen() {
        return _opened;
    }

    /**
     * Opens the component with given connection and credential parameters.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     * @param connection    connection parameters
     * @param credential    credential parameters
     */
    @Override
    public void openWithParams(String correlationId, ConnectionParams connection, CredentialParams credential) {
        _logger.trace(correlationId, "Opened queue %s", this);
        _opened = true;
    }

    /**
     * Closes component and frees used resources.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     */
    @Override
    public void close(String correlationId) {
        synchronized (_lock) {
            _cancel = false;
            _opened = false;
            _lock.notifyAll();
        }

        _logger.trace(correlationId, "Closed queue %s", this);
    }

    /**
     * Clears component state.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     */
    @Override
    public void clear(String correlationId) {
        synchronized (_lock) {
            // Clear messages
            _messages.clear();
            _lockedMessages.clear();
        }

        _logger.trace(correlationId, "Cleared queue %s", this);
    }

    @Override
    public void configure(ConfigParams config) {
        super.configure(config);

        this._listenInterval = config.getAsLongWithDefault("listen_interval", this._listenInterval);
        this._listenInterval = config.getAsLongWithDefault("options.listen_interval", this._listenInterval);
    }

    /**
     * Gets the current number of messages in the queue to be delivered.
     *
     * @return number of messages.
     */
    @Override
    public int readMessageCount() {
        synchronized (_lock) {
            return _messages.size();
        }
    }

    /**
     * Sends a message into the queue.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     * @param message       a message envelop to be sent.
     */
    @Override
    public void send(String correlationId, MessageEnvelope message) {
        if (message == null)
            return;

        synchronized (_lock) {
            // Set sent time
            message.setSentTime(ZonedDateTime.now(ZoneOffset.UTC));

            // Add message to the queue
            _messages.add(message);

            // Release threads waiting for messages
            _lock.notify();
        }

        _counters.incrementOne("queue." + getName() + ".sent_messages");
        _logger.debug(correlationId, "Sent message %s via %s", message, this);
    }

    /**
     * Peeks a single incoming message from the queue without removing it. If there
     * are no messages available in the queue it returns null.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     * @return a message envelop object.
     */
    @Override
    public MessageEnvelope peek(String correlationId) {
        MessageEnvelope message = null;

        synchronized (_lock) {
            // Pick a message
            if (_messages.size() > 0)
                message = _messages.get(0);
        }

        if (message != null)
            _logger.trace(correlationId, "Peeked message %s on %s", message, this);

        return message;
    }

    /**
     * Peeks multiple incoming messages from the queue without removing them. If
     * there are no messages available in the queue it returns an empty list.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     * @param messageCount  a maximum number of messages to peek.
     * @return a list with messages.
     */
    @Override
    public List<MessageEnvelope> peekBatch(String correlationId, int messageCount) {
        List<MessageEnvelope> messages = new ArrayList<>();

        synchronized (_lock) {
            for (int index = 0; index < _messages.size() && index < messageCount; index++)
                messages.add(_messages.get(index));
        }

        _logger.trace(correlationId, "Peeked %d messages on %s", messages.size(), this);

        return messages;
    }

    /**
     * Receives an incoming message and removes it from the queue.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     * @param waitTimeout   a timeout in milliseconds to wait for a message to come.
     * @return a message envelop object.
     */
    @Override
    public MessageEnvelope receive(String correlationId, long waitTimeout) {
        MessageEnvelope message = null;
        long checkIntervalMs = 100;
        long elapsedTime = 0;

        synchronized (_lock) {
            // Get message the the queue
            if (_messages.size() > 0) {
                message = _messages.get(0);
                _messages.remove(0);
            }
        }

        while (elapsedTime < waitTimeout && message == null) {
            synchronized (_lock) {
                if (message == null) {
                    try {
                        // Wait for a while
                        _lock.wait(checkIntervalMs);
                    } catch (InterruptedException ex) {
                        return null;
                    }
                }
            
                // Try to get a message again
                if (message == null && _messages.size() > 0) {
                    message = _messages.get(0);
                    _messages.remove(0);
                }
            }
        }

        // Exit if message was not found
        if (message == null)
            return null;

        // Generate and set locked token
        int lockedToken = _lockTokenSequence++;
        message.setReference(lockedToken);

        // Add messages to locked messages list
        LockedMessage lockedMessage = new LockedMessage();
        lockedMessage.expirationTime = ZonedDateTime.now().plus(waitTimeout, ChronoUnit.MILLIS);
        lockedMessage.message = message;
        lockedMessage.timeout = waitTimeout;

        _lockedMessages.put(lockedToken, lockedMessage);


        _counters.incrementOne("queue." + getName() + ".received_messages");
        _logger.debug(message.getCorrelationId(), "Received message %s via %s", message, this);

        return message;
    }

    /**
     * Renews a lock on a message that makes it invisible from other receivers in
     * the queue. This method is usually used to extend the message processing time.
     *
     * @param message     a message to extend its lock.
     * @param lockTimeout a locking timeout in milliseconds.
     */
    @Override
    public void renewLock(MessageEnvelope message, long lockTimeout) {
        if (message == null || message.getReference() == null)
            return;

        synchronized (_lock) {
            // Get message from locked queue
            int lockedToken = (int) message.getReference();
            LockedMessage lockedMessage = _lockedMessages.get(lockedToken);

            // If lock is found, extend the lock
            if (lockedMessage != null)
                lockedMessage.expirationTime = ZonedDateTime.now().plus(lockTimeout, ChronoUnit.MILLIS);
        }

        _logger.trace(message.getCorrelationId(), "Renewed lock for message %s at %s", message, this);
    }

    /**
     * Returnes message into the queue and makes it available for all subscribers to
     * receive it again. This method is usually used to return a message which could
     * not be processed at the moment to repeat the attempt. Messages that cause
     * unrecoverable errors shall be removed permanently or/and send to dead letter
     * queue.
     *
     * @param message a message to return.
     */
    @Override
    public void abandon(MessageEnvelope message) {
        if (message == null || message.getReference() == null)
            return;

        synchronized (_lock) {
            // Get message from locked queue
            int lockedToken = (int) message.getReference();
            LockedMessage lockedMessage = _lockedMessages.get(lockedToken);
            if (lockedMessage != null) {
                // Remove from locked messages
                _lockedMessages.remove(lockedToken);
                message.setReference(null);

                // Skip if it is already expired
                if (lockedMessage.expirationTime.toInstant().toEpochMilli() <= ZonedDateTime.now().toInstant().toEpochMilli())
                    return;
            }
            // Skip if it absent
            else
                return;
        }

        _logger.trace(message.getCorrelationId(), "Abandoned message %s at %s", message, this);

        // Add back to the queue
        send(message.getCorrelationId(), message);
    }

    /**
     * Permanently removes a message from the queue. This method is usually used to
     * remove the message after successful processing.
     *
     * @param message a message to remove.
     */
    @Override
    public void complete(MessageEnvelope message) {
        if (message == null || message.getReference() == null)
            return;

        synchronized (_lock) {
            int lockKey = (int) message.getReference();
            _lockedMessages.remove(lockKey);
            message.setReference(null);
        }

        _logger.trace(message.getCorrelationId(), "Completed message %s at %s", message, this);
    }

    /**
     * Permanently removes a message from the queue and sends it to dead letter
     * queue.
     *
     * @param message a message to be removed.
     */
    @Override
    public void moveToDeadLetter(MessageEnvelope message) {
        if (message == null || message.getReference() == null)
            return;

        synchronized (_lock) {
            int lockKey = (int) message.getReference();
            _lockedMessages.remove(lockKey);
            message.setReference(null);
        }

        _counters.incrementOne("queue." + getName() + ".dead_messages");
        _logger.trace(message.getCorrelationId(), "Moved to dead message %s at %s", message, this);
    }

    /**
     * Listens for incoming messages and blocks the current thread until queue is
     * closed.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     * @param receiver      a receiver to receive incoming messages.
     * @see IMessageReceiver
     * @see #receive(String, long)
     */
    @Override
    public void listen(String correlationId, IMessageReceiver receiver) {
        if (_cancel) {
            _logger.error(correlationId, "Already listening queue %s", this);
            return;
        }

        _logger.trace(correlationId, "Started listening messages at %s", this);

        _cancel = true;

        while (_cancel) {
            MessageEnvelope message = receive(correlationId, _listenInterval);

            if (_cancel && message != null) {
                try {
                    receiver.receiveMessage(message, this);
                } catch (Exception ex) {
                    _logger.error(correlationId, ex, "Failed to process the message");
                    // await AbandonAsync(message);
                }
            }
        }

        _logger.trace(correlationId, "Stopped listening messages at %s", this);
    }

    /**
     * Ends listening for incoming messages. When this method is call listen()
     * unblocks the thread and execution continues.
     *
     * @param correlationId (optional) transaction id to trace execution through
     *                      call chain.
     */
    @Override
    public void endListen(String correlationId) {
        synchronized (_lock) {
            _cancel = false;
        }
    }

    /**
     * Override toString() method, string representation of queue.
     *
     * @return queue name
     */
    @Override
    public String toString() {
        return "[" + getName() + "]";
    }

}
