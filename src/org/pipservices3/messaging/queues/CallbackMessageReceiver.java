package org.pipservices3.messaging.queues;

import java.util.function.Function;

/**
 * Wraps message callback into IMessageReceiver
 */
public class CallbackMessageReceiver implements IMessageReceiver {
    @FunctionalInterface
    private interface Callback {
        void apply(MessageEnvelope envelope, IMessageQueue queue);
    }

    private final Callback _callback;

    public CallbackMessageReceiver(Callback callback) {
        if (callback == null) {
            throw new Error("Callback cannot be null");
        }

        this._callback = callback;
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
        this._callback.apply(envelope, queue);
    }
}
