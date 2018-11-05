package org.pipservices3.messaging.build;

import org.pipservices3.components.build.*;
import org.pipservices3.messaging.queues.MemoryMessageQueue;
import org.pipservices3.commons.refer.*;

/**
 * Creates MemoryMessageQueue components by their descriptors.
 * Name of created message queue is taken from its descriptor.
 * 
 * @see <a href="https://raw.githubusercontent.com/pip-services3-java/pip-services3-components-java/master/doc/api/org/pipservices3/components/build/Factory.html">Factory</a>
 * @see MemoryMessageQueue
 */
public class MemoryMessageQueueFactory extends Factory {
	public static final Descriptor Descriptor = new Descriptor("pip-services3-net", "factory", "message-queue", "memory",
			"1.0");
	public static final Descriptor MemoryQueueDescriptor = new Descriptor("pip-services3-net", "message-queue", "memory",
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
