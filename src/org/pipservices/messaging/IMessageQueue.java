package org.pipservices.messaging;

import java.util.*;

import org.pipservices.commons.errors.ApplicationException;
import org.pipservices.commons.run.*;

public interface IMessageQueue extends IOpenable {
	String getName();
	MessagingCapabilities getCapabilities();
    Long getMessageCount();

    void send(String correlationId, MessageEnvelop envelop) throws ApplicationException;
    //void send(String correlationId,  String messageType, String message) throws ApplicationException;
    void sendAsObject(String correlationId, String messageType, Object message) throws ApplicationException;
    MessageEnvelop peek(String correlationId) throws ApplicationException;
    List<MessageEnvelop> peekBatch(String correlationId, int messageCount) throws ApplicationException;
    MessageEnvelop receive(String correlationId, long waitTimeout) throws ApplicationException;

    void renewLock(MessageEnvelop message, long lockTimeout) throws ApplicationException;
    void complete(MessageEnvelop message) throws ApplicationException;
    void abandon(MessageEnvelop message) throws ApplicationException;
    void moveToDeadLetter(MessageEnvelop message) throws ApplicationException;

    void listen(String correlationId, IMessageReceiver receiver) throws ApplicationException;
    void beginListen(String correlationId, IMessageReceiver receiver);
    void endListen(String correlationId) throws ApplicationException;
}
