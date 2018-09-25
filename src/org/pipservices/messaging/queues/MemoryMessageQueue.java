package org.pipservices.messaging;

import java.time.*;
import java.util.*;

import org.pipservices.components.auth.*;
import org.pipservices.components.connect.*;

public class MemoryMessageQueue extends MessageQueue {
    private final long _defaultLockTimeout = 30000;
    private final long _defaultWaitTimeout = 5000;

    private List<MessageEnvelop> _messages = new ArrayList<MessageEnvelop>();
    private int _lockTokenSequence = 0;
    private Map<Integer, LockedMessage> _lockedMessages = new HashMap<Integer, LockedMessage>();
    private boolean _listening;
    private boolean _opened = false;
    
    private class LockedMessage {
        //public MessageEnvelop message;
        public long lockExpiration;
    }

    public MemoryMessageQueue() {
    	this(null);
	}

    public MemoryMessageQueue(String name) {
    	super(name);

    	_capabilities = new MessagingCapabilities(
			true, true, true, true, true, true, true, false, true);
	}

    @Override
    public boolean isOpen() {
    	return _opened;
    }
    
    @Override
    public void open(String correlationId, ConnectionParams connection, CredentialParams credential) {
        _logger.trace(correlationId, "Opened queue %s", this);
        _opened = true;
    }

    @Override
    public void close(String correlationId) {
    	synchronized (_lock) {
	        _listening = false;
	        _opened = false;
	        _lock.notifyAll();
    	}

    	_logger.trace(correlationId, "Closed queue %s", this);
    }

    @Override
    public Long getMessageCount() {
        synchronized (_lock) {
            return (long)_messages.size();
        }
    }

    @Override
    public void send(String correlationId, MessageEnvelop message) {
    	if (message == null) return;
    	
        synchronized (_lock) {
        	// Set sent time
        	message.setSentTime(ZonedDateTime.now(ZoneOffset.UTC));
        	
            // Add message to the queue
            _messages.add(message);

            // Release threads waiting for messages
            _lock.notify();
        }
        
        _counters.incrementOne("queue." + getName() + ".sent_messages");
        _logger.debug(correlationId, "Sent message %s via %s", message, this);
    }   

    @Override
    public MessageEnvelop peek(String correlationId) {
        MessageEnvelop message = null;

        synchronized (_lock) {
            // Pick a message
            if (_messages.size() > 0)
                message = _messages.get(0);
        }

        if (message != null)
            _logger.trace(correlationId, "Peeked message %s on %s", message, this);

        return message;
    }

    @Override
    public List<MessageEnvelop> peekBatch(String correlationId, int messageCount) {
        List<MessageEnvelop> messages = new ArrayList<MessageEnvelop>();

        synchronized (_lock) {
        	for (int index = 0; index < _messages.size() && index < messageCount; index++)
        		messages.add(_messages.get(index));
        }

        _logger.trace(correlationId, "Peeked %d messages on %s", messages.size(), this);

        return messages;
    }


    @Override
    public MessageEnvelop receive(String correlationId, long waitTimeout) {
        MessageEnvelop message = null;

        synchronized (_lock) {
        	// Try to get a message
            if (_messages.size() > 0) {
            	message = _messages.get(0);
            	_messages.remove(0);
            }

	        if (message == null) {
	        	try {
	        		_lock.wait(waitTimeout);
	        	} catch (InterruptedException ex) {
	        		return null;
	        	}
	        }

        	// Try to get a message again
            if (message == null && _messages.size() > 0) {
            	message = _messages.get(0);
            	_messages.remove(0);
            }
            
	        // Exit if message was not found
	        if (message == null)
	            return null;

            // Generate and set locked token
            int lockedToken = _lockTokenSequence++;
            message.setReference(lockedToken);

            // Add messages to locked messages list
            LockedMessage lockedMessage = new LockedMessage();
            lockedMessage.lockExpiration = System.currentTimeMillis() + _defaultLockTimeout;
            //lockedMessage.message = message;

            _lockedMessages.put(lockedToken, lockedMessage);
        }

        _counters.incrementOne("queue." + getName() + ".received_messages");
        _logger.debug(message.getCorrelationId(), "Received message %s via %s", message, this);

        return message;
    }

    @Override
    public void renewLock(MessageEnvelop message, long lockTimeout) {
        if (message == null || message.getReference() == null) 
        	return;

        synchronized (_lock) {
            // Get message from locked queue
            int lockedToken = (int)message.getReference();
            LockedMessage lockedMessage = _lockedMessages.get(lockedToken);

            // If lock is found, extend the lock
            if (lockedMessage != null)
            	lockedMessage.lockExpiration = System.currentTimeMillis() + lockTimeout;
        }

        _logger.trace(message.getCorrelationId(), "Renewed lock for message %s at %s", message, this);
    }

    @Override
    public void abandon(MessageEnvelop message) {
        if (message == null || message.getReference() == null) 
        	return;

        synchronized (_lock) {
            // Get message from locked queue
            int lockedToken = (int)message.getReference();
            LockedMessage lockedMessage = _lockedMessages.get(lockedToken);
            if (lockedMessage != null) {
                // Remove from locked messages
                _lockedMessages.remove(lockedToken);
                message.setReference(null);

                // Skip if it is already expired
                if (lockedMessage.lockExpiration <= System.currentTimeMillis())
                    return;
            }
            // Skip if it absent
            else return;
        }

        _logger.trace(message.getCorrelationId(), "Abandoned message %s at %s", message, this);

        // Add back to the queue
        send(message.getCorrelationId(), message);
    }

    @Override
    public void complete(MessageEnvelop message) {
        if (message == null || message.getReference() == null) 
        	return;

        synchronized (_lock) {
            int lockKey = (int)message.getReference();
            _lockedMessages.remove(lockKey);
            message.setReference(null);
        }

        _logger.trace(message.getCorrelationId(), "Completed message %s at %s", message, this);
    }

    @Override
    public void moveToDeadLetter(MessageEnvelop message) {
        if (message == null || message.getReference() == null) 
        	return;

        synchronized (_lock) {
            int lockKey = (int)message.getReference();
            _lockedMessages.remove(lockKey);
            message.setReference(null);
        }

        _counters.incrementOne("queue." + getName() + ".dead_messages");
        _logger.trace(message.getCorrelationId(), "Moved to dead message %s at %s", message, this);
    }

    @Override
    public void listen(String correlationId, IMessageReceiver receiver) {
    	if (_listening) {
    		_logger.error(correlationId, "Already listening queue %s", this);
    		return;
    	}
    	
        _logger.trace(correlationId, "Started listening messages at %s", this);

        _listening = true;

        while (_listening) {
            MessageEnvelop message = receive(correlationId, _defaultWaitTimeout);

            if (_listening && message != null) {
                try {
                    receiver.receiveMessage(message, this);
                } catch (Exception ex) {
                    _logger.error(correlationId, ex, "Failed to process the message");
                    //await AbandonAsync(message);
                }
            }
        }
        
        _logger.trace(correlationId, "Stopped listening messages at %s", this);
    }

    @Override
    public void endListen(String correlationId) {
		_listening = false;
    }

    @Override
    public void clear(String correlationId) {
        synchronized (_lock) {
            // Clear messages
            _messages.clear();
            _lockedMessages.clear();
        }

        _logger.trace(correlationId, "Cleared queue %s", this);
    }
	
    @Override
    public String toString() {
        return "[" + getName() + "]";
    }

}
