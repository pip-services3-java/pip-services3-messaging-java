package org.pipservices.messaging.build;

import org.pipservices.components.build.*;
import org.pipservices.messaging.queues.MemoryMessageQueue;
import org.pipservices.commons.refer.*;

public class MemoryMessageQueueFactory extends Factory {
	public static final Descriptor Descriptor = new Descriptor("pip-services-net", "factory", "message-queue", "memory",
			"1.0");
	public static final Descriptor MemoryQueueDescriptor = new Descriptor("pip-services-net", "message-queue", "memory",
			"*", "*");

	public MemoryMessageQueueFactory() {
		register(MemoryQueueDescriptor, (locator) -> {
			Descriptor descritor = (Descriptor) locator;
			return new MemoryMessageQueue(descritor.getName());
		});
	}

}
