package org.pipservices3.messaging.build;

import org.pipservices3.components.build.Factory;
import org.pipservices3.messaging.queues.MemoryMessageQueue;
import org.pipservices3.commons.refer.*;

/**
 * Creates {@link MemoryMessageQueue} components by their descriptors.
 * Name of created message queue is taken from its descriptor.
 *
 * @see <a href="https://pip-services3-java.github.io/pip-services3-components-java/org/pipservices3/components/build/Factory.html">Factory</a>
 * @see MemoryMessageQueue
 */
public class DefaultMessagingFactory extends Factory {

    private static final Descriptor MemoryQueueDescriptor = new Descriptor("pip-services", "message-queue", "memory", "*", "1.0");
    private static final Descriptor MemoryQueueFactoryDescriptor = new Descriptor("pip-services", "queue-factory", "memory", "*", "1.0");

    /**
     * Create a new instance of the factory.
     */
    public DefaultMessagingFactory() {
        registerAsType(MemoryQueueFactoryDescriptor, MemoryMessageQueueFactory.class);
        register(MemoryQueueDescriptor, (locator) -> {
            Descriptor descriptor = (Descriptor) locator;
            return new MemoryMessageQueue(descriptor.getName());
        });
    }
}
