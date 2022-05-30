package org.pipservices3.messaging.queues;

import java.time.ZonedDateTime;

/**
 * Data object used to store and lock incoming messages
 * in {@link MemoryMessageQueue}.
 *
 * @see MemoryMessageQueue
 */
public class LockedMessage {
    /**
     * The incoming message.
     */
    public MessageEnvelope message;

    /**
     * The expiration time for the message lock.
     * If it is null then the message is not locked.
     */
    public ZonedDateTime expirationTime;

    /**
     * The lock timeout in milliseconds.
     */
    public Long timeout;
}
