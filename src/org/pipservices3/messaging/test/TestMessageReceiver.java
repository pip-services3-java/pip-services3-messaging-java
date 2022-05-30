package org.pipservices3.messaging.test;

import org.pipservices3.commons.errors.ApplicationException;
import org.pipservices3.commons.run.ICleanable;
import org.pipservices3.messaging.queues.IMessageQueue;
import org.pipservices3.messaging.queues.IMessageReceiver;
import org.pipservices3.messaging.queues.MessageEnvelope;

import java.util.ArrayList;
import java.util.List;

public class TestMessageReceiver implements IMessageReceiver, ICleanable {
    private List<MessageEnvelope> _messages = new ArrayList<>();
    private final Object _lock = new Object();

    /**
     * Gets the list of received messages.
     */
    public List<MessageEnvelope> getMessages() {
        synchronized (_lock) {
            return this._messages;
        }
    }

    /**
     * Gets the received message count.
     */
    public int getMessageCount() {
        synchronized (_lock) {
            return _messages.size();
        }
    }

    /**
     * Receives incoming message from the queue.
     *
     * @param envelope an incoming message
     * @param queue    a queue where the message comes from
     * @see MessageEnvelope
     * @see IMessageQueue
     */
    @Override
    public void receiveMessage(MessageEnvelope envelope, IMessageQueue queue) {
        synchronized (_lock) {
            this._messages.add(envelope);
        }
    }

    /**
     * Clears all received messagers.
     *
     * @param correlationId (optional) transaction id to trace execution through call chain.
     */
    @Override
    public void clear(String correlationId) throws ApplicationException {
        synchronized (_lock) {
            this._messages = new ArrayList<>();
        }
    }
}
