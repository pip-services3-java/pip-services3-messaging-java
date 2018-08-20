package org.pipservices.build;

import org.pipservices.components.build.Factory;
import org.pipservices.messaging.MemoryMessageQueue;
import org.pipservices.commons.refer.*;

public class DefaultMessagingFactory extends Factory {

	public static Descriptor Descriptor = new Descriptor("pip-services", "factory", "messaging", "default", "1.0");
    public static Descriptor MemoryMessageQueueFactoryDescriptor = new Descriptor("pip-services", "factory", "message-queue", "memory", "1.0");
    public static Descriptor MemoryMessageQueueDescriptor = new Descriptor("pip-services", "message-queue", "memory", "*", "1.0");

    public DefaultMessagingFactory() {
        registerAsType(MemoryMessageQueueFactoryDescriptor, MemoryMessageQueueFactory.class);
        registerAsType(MemoryMessageQueueDescriptor, MemoryMessageQueue.class);
    }
}
