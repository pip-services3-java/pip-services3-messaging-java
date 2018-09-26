package org.pipservices.messaging.build;

import org.pipservices.components.build.*;
import org.pipservices.messaging.queues.MemoryMessageQueue;
import org.pipservices.commons.refer.*;

/**
 * Creates MemoryMessageQueue components by their descriptors.
 * Name of created message queue is taken from its descriptor.
 * 
 * @see Factory
 * @see MemoryMessageQueue
 */
public class MemoryMessageQueueFactory extends Factory {
	public static final Descriptor Descriptor = new Descriptor("pip-services-net", "factory", "message-queue", "memory",
			"1.0");
	public static final Descriptor MemoryQueueDescriptor = new Descriptor("pip-services-net", "message-queue", "memory",
			"*", "*");

	/**
	 * Create a new instance of the factory.
	 */
	public MemoryMessageQueueFactory() {
		register(MemoryQueueDescriptor, (locator) -> {
			Descriptor descritor = (Descriptor) locator;
			return new MemoryMessageQueue(descritor.getName());
		});
	}

}
