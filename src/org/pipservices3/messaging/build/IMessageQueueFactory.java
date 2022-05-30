package org.pipservices3.messaging.build;

import org.pipservices3.commons.refer.ReferenceException;
import org.pipservices3.messaging.queues.IMessageQueue;

/**
 * Creates message queue componens.
 *
 * @see org.pipservices3.messaging.queues.IMessageQueue
 */
public interface IMessageQueueFactory {
    /**
     * Creates a message queue component and assigns its name.
     *
     * @param name a name of the created message queue.
     */
    IMessageQueue createQueue(String name) throws ReferenceException;
}
