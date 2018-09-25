package org.pipservices.messaging;

public interface IMessageReceiver {
	void receiveMessage(MessageEnvelop message, IMessageQueue queue);
}
