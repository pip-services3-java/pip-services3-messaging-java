package org.pipservices3.messaging.queues;

import org.pipservices3.commons.config.ConfigParams;
import org.pipservices3.commons.errors.ApplicationException;
import org.pipservices3.commons.errors.InvalidStateException;
import org.pipservices3.commons.run.ICleanable;

import java.util.ArrayList;
import java.util.List;

/**
 * Message queue that caches received messages in memory to allow peek operations
 * that may not be supported by the undelying queue.
 * <p>
 * This queue is users as a base implementation for other queues
 */
public abstract class CachedMessageQueue extends MessageQueue implements ICleanable {
    protected boolean _autoSubscribe;
    protected List<MessageEnvelope> _messages;
    protected IMessageReceiver _receiver;

    /**
     * Creates a new instance of the persistence component.
     *
     * @param name         (optional) a queue name
     * @param capabilities (optional) a capabilities of this message queue
     */
    public CachedMessageQueue(String name, MessagingCapabilities capabilities) {
        super(name, capabilities);
    }

    public CachedMessageQueue() {
    }

    /**
     * Configures component by passing configuration parameters.
     *
     * @param config configuration parameters to be set.
     */
    @Override
    public void configure(ConfigParams config) {
        super.configure(config);

        this._autoSubscribe = config.getAsBooleanWithDefault("options.autosubscribe", this._autoSubscribe);
    }

    /**
     * Opens the component.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */
    @Override
    public void open(String correlationId) throws ApplicationException {
        if (this.isOpen())
            return;

        try {
            if (this._autoSubscribe)
                this.subscribe(correlationId);

            this._logger.debug(correlationId, "Opened queue " + this.getName());
        } catch (Exception ex) {
            this.close(correlationId);
        }
    }

    /**
     * Closes component and frees used resources.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */
    @Override
    public void close(String correlationId) {
        if (!this.isOpen()) {
            return;
        }

        try {
            // Unsubscribe from the broker
            this.unsubscribe(correlationId);
        } finally {
            synchronized (_lock) {
                this._messages = new ArrayList<>();
                this._receiver = null;
            }
        }
    }

    /**
     * Subscribes to the message broker.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */

    protected abstract void subscribe(String correlationId);

    /**
     * Unsubscribes from the message broker.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */

    protected abstract void unsubscribe(String correlationId);

    /**
     * Clears component state.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */
    @Override
    public void clear(String correlationId) {
        synchronized (_lock) {
            this._messages = new ArrayList<>();
        }
    }

    /**
     * Reads the current number of messages in the queue to be delivered.
     *
     * @return a number of messages in the queue.
     */
    @Override
    public int readMessageCount() {
        synchronized (_lock) {
            return this._messages.size();
        }
    }

    /**
     * Peeks a single incoming message from the queue without removing it.
     * If there are no messages available in the queue it returns null.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     * @return a peeked message or <code>null</code>.
     */
    public MessageEnvelope peek(String correlationId) throws InvalidStateException {
        this.checkOpen(correlationId);

        // Subscribe to topic if needed
        this.subscribe(correlationId);

        // Peek a message from the top
        MessageEnvelope message = null;
        synchronized (_lock) {
            if (this._messages.size() > 0) {
                message = this._messages.get(0);
            }
        }

        if (message != null)
            this._logger.trace(message.getCorrelationId(), "Peeked message %s on %s", message, this.getName());

        return message;
    }

    /**
     * Peeks multiple incoming messages from the queue without removing them.
     * If there are no messages available in the queue it returns an empty list.
     * <p>
     * Important: This method is not supported by MQTT.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     * @param messageCount  a maximum number of messages to peek.
     * @return a list with peeked messages.
     */
    public List<MessageEnvelope> peekBatch(String correlationId, int messageCount) throws InvalidStateException {
        List<MessageEnvelope> messages;

        this.checkOpen(correlationId);

        // Subscribe to topic if needed
        this.subscribe(correlationId);

        // Peek a batch of messages
        synchronized (_lock) {
            messages = this._messages.subList(0, messageCount);
        }

        this._logger.trace(correlationId, "Peeked %d messages on %s", messages.size(), this.getName());

        return messages;
    }

    /**
     * Receives an incoming message and removes it from the queue.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     * @param waitTimeout   a timeout in milliseconds to wait for a message to come.
     * @return a received message or <code>null</code>.
     */
    public MessageEnvelope receive(String correlationId, long waitTimeout) throws InvalidStateException {
        MessageEnvelope message = null;
        this.checkOpen(correlationId);

        // Subscribe to topic if needed
        this.subscribe(correlationId);

        var checkIntervalMs = 100;
        var elapsedTime = 0;

        // Get message the queue
        synchronized (_lock) {
            message = this._messages.get(0);
            this._messages.remove(0);
        }

        while (elapsedTime < waitTimeout && message == null) {
            synchronized (_lock) {
                // Wait for a while
                try {
                    _lock.wait(checkIntervalMs);
                } catch (InterruptedException ex) {
                    return null;
                }
                elapsedTime += checkIntervalMs;

                // Get message the the queue
                message = this._messages.get(0);
                _messages.remove(0);
            }
        }

        return message;
    }

    protected void sendMessageToReceiver(IMessageReceiver receiver, MessageEnvelope message) {
        var correlationId = message != null ? message.getCorrelationId() : null;
        if (message == null || receiver == null) {
            this._logger.warn(correlationId, "Message was skipped.");
            return;
        }

        try {
            this._receiver.receiveMessage(message, this);
        } catch (Exception ex) {
            this._logger.error(correlationId, ex, "Failed to process the message");
        }
    }

    /**
     * Listens for incoming messages and blocks the current thread until queue is closed.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     * @param receiver      a receiver to receive incoming messages.
     * @see [[IMessageReceiver]]
     * @see [[receive]]
     */
    public void listen(String correlationId, IMessageReceiver receiver) {
        if (!this.isOpen())
            return;

        // Subscribe to topic if needed
        this.subscribe(correlationId);

        this._logger.trace(null, "Started listening messages at %s", this.getName());

        // Resend collected messages to receiver
        while (this.isOpen() && this._messages.size() > 0) {
            synchronized (_lock) {
                var message = this._messages.get(0);
                this._messages.remove(0);

                if (message != null)
                    this.sendMessageToReceiver(receiver, message);
            }
        }

        // Set the receiver
        if (this.isOpen()) {
            this._receiver = receiver;
        }
    }

    /**
     * Ends listening for incoming messages.
     * When this method is call [[listen]] unblocks the thread and execution continues.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */
    public void endListen(String correlationId) {
        this._receiver = null;
    }
}
