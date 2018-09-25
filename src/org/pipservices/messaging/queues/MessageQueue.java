package org.pipservices.messaging.queues;

import java.util.*;

import org.pipservices.commons.config.*;
import org.pipservices.commons.errors.*;
import org.pipservices.commons.refer.*;
import org.pipservices.commons.run.*;
import org.pipservices.components.auth.CredentialParams;
import org.pipservices.components.auth.CredentialResolver;
import org.pipservices.components.connect.ConnectionParams;
import org.pipservices.components.connect.ConnectionResolver;
import org.pipservices.components.count.CompositeCounters;
import org.pipservices.components.log.CompositeLogger;

public abstract class MessageQueue 
	implements IMessageQueue, IReferenceable, IConfigurable, IOpenable, IClosable {
	
	protected String _name;
	protected String _kind;
    protected MessagingCapabilities _capabilities = new MessagingCapabilities(
		true, true, true, true, true, true, true, false, true);;
    protected Object _lock = new Object();
    protected CompositeLogger _logger = new CompositeLogger();
    protected CompositeCounters _counters = new CompositeCounters();
    protected ConnectionResolver _connectionResolver = new ConnectionResolver();
    protected CredentialResolver _credentialResolver = new CredentialResolver();
    
    public MessageQueue() {}

    public MessageQueue(String name) {
    	_name = name;
	}

    public MessageQueue(String name, ConfigParams config) {
    	_name = name;
    	if (config != null) configure(config);
	}
    
    public void setReferences(IReferences references) throws ReferenceException {
        _logger.setReferences(references);
        _counters.setReferences(references);
        _connectionResolver.setReferences(references);
        _credentialResolver.setReferences(references);
    }

    public void configure(ConfigParams config) {
        _name = NameResolver.resolve(config, _name);
        _connectionResolver.configure(config, true);
        _credentialResolver.configure(config, true);
    }

    public String getName() { return _name != null ? _name : "undefined"; }    
    public MessagingCapabilities getCapabilities() { return _capabilities; }

    public abstract Long getMessageCount();
    
    public void open(String correlationId) throws ApplicationException {
        ConnectionParams connection = _connectionResolver.resolve(correlationId);
        CredentialParams credential = _credentialResolver.lookup(correlationId);
        open(correlationId, connection, credential);
    }
    
    public abstract boolean isOpen();
    public abstract void open(String correlationId, ConnectionParams connection, CredentialParams credential) 
		throws ApplicationException;
    public abstract void close(String correlationId) throws ApplicationException;
    public abstract void clear(String correlationId) throws ApplicationException;
    public abstract void send(String correlationId, MessageEnvelop message) throws ApplicationException;

    public void sendAsObject(String correlationId, String messageType, Object message) throws ApplicationException {
        MessageEnvelop envelop = new MessageEnvelop(correlationId, messageType, message);
        send(correlationId, envelop);
    }

    public abstract MessageEnvelop peek(String correlationId) throws ApplicationException;
    public abstract List<MessageEnvelop> peekBatch(String correlationId, int messageCount) throws ApplicationException;
    public abstract MessageEnvelop receive(String correlationId, long waitTimeout) throws ApplicationException;
    public abstract void renewLock(MessageEnvelop message, long lockTimeout) throws ApplicationException;
    public abstract void abandon(MessageEnvelop message) throws ApplicationException;
    public abstract void complete(MessageEnvelop message) throws ApplicationException;
    public abstract void moveToDeadLetter(MessageEnvelop message) throws ApplicationException;

    public abstract void listen(String correlationId, IMessageReceiver receiver) throws ApplicationException;

    public void beginListen(String correlationId, IMessageReceiver receiver) {
    	// Start listening on a parallel tread
    	new Thread(new Runnable() {
    		@Override
    		public void run() {
    			try {
    				listen(correlationId, receiver);
    			} catch (Exception ex) {
    				_logger.error(correlationId, ex, "Failed to listen messages");
    			}
    		}
    	}).start();
    }

    public abstract void endListen(String correlationId) throws ApplicationException;

    @Override
    public String toString() {
        return "[" + getName() + "]";
    }

}
