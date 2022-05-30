package org.pipservices3.messaging.connect;

import java.util.List;

/**
 * Defines an interface for message queue connections
 */
public interface IMessageQueueConnection {
    /**
     * Reads a list of registered queue names.
     * If connection doesn't support this function returnes an empty list.
     *
     * @return a list with registered queue names.
     */
    List<String> readQueueNames();

    /**
     * Creates a message queue.
     * If connection doesn't support this function it exists without error.
     *
     * @param name the name of the queue to be created.
     */
    void createQueue(String name);

    /**
     * Deletes a message queue.
     * If connection doesn't support this function it exists without error.
     *
     * @param name the name of the queue to be deleted.
     */
    void deleteQueue(String name);
}
