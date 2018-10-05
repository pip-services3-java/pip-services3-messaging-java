package org.pipservices.messaging.build;

import org.pipservices.components.build.Factory;
import org.pipservices.messaging.queues.MemoryMessageQueue;
import org.pipservices.commons.refer.*;

/**
 * Creates MemoryMessageQueue components by their descriptors.
 * Name of created message queue is taken from its descriptor.
 * 
 * @see <a href="https://raw.githubusercontent.com/pip-services-java/pip-services-components-java/master/doc/api/org/pipservices/components/build/Factory.html">Factory</a>
 * @see MemoryMessageQueue
 */
public class DefaultMessagingFactory extends Factory {

	public static Descriptor Descriptor = new Descriptor("pip-services", "factory", "messaging", "default", "1.0");
    public static Descriptor MemoryMessageQueueFactoryDescriptor = new Descriptor("pip-services", "factory", "message-queue", "memory", "1.0");
    public static Descriptor MemoryMessageQueueDescriptor = new Descriptor("pip-services", "message-queue", "memory", "*", "1.0");

    /**
	 * Create a new instance of the factory.
	 */
    public DefaultMessagingFactory() {
        registerAsType(MemoryMessageQueueFactoryDescriptor, MemoryMessageQueueFactory.class);
        registerAsType(MemoryMessageQueueDescriptor, MemoryMessageQueue.class);
    }
}
