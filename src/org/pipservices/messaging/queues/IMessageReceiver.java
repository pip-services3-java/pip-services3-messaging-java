package org.pipservices.messaging.queues;

public interface IMessageReceiver {
	void receiveMessage(MessageEnvelop message, IMessageQueue queue);
}
