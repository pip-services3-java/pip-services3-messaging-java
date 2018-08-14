package org.pipservices.messaging;

import java.time.ZonedDateTime;

import org.pipservices.commons.convert.StringConverter;
import org.pipservices.commons.data.*;

import com.fasterxml.jackson.annotation.*;

public class MessageEnvelop {
	private String _messageId;
	private String _messageType;
	private String _correlationId;
	private Object _message;
	private Object _reference;
	private ZonedDateTime _sentTime;
	
    public MessageEnvelop() { }

    public MessageEnvelop(String correlationId, String messageType, Object message) {
        _correlationId = correlationId;
        _messageType = messageType;
        _message = message;
        _messageId = IdGenerator.nextLong();
    }
    
    public MessageEnvelop(String correlationId, String messageType, String message) {
        _correlationId = correlationId;
        _messageType = messageType;
        setMessage(message);
        _messageId = IdGenerator.nextLong();
    }

    @JsonIgnore
    public Object getReference() { return _reference; }
    public void setReference(Object value) { _reference = value; }

    @JsonProperty("correlation_id")
    public String getCorrelationId() { return _correlationId; }
    public void setCorrelationId(String value) { _correlationId = value; }

    @JsonProperty("message_id")
    public String getMessageId() { return _messageId; }
    public void setMessageId(String value) { _messageId = value; }

    @JsonProperty("message_type")
    public String getMessageType() { return _messageType; }
    public void setMessageType(String value) { _messageType = value; }

    @JsonProperty("sent_time")
    public ZonedDateTime getSentTime() { return _sentTime; }
    public void setSentTime(ZonedDateTime value) { _sentTime = value; }    

    @JsonProperty("message")
    public Object getMessage() { return _message; }
    public void setMessage(Object value) { _message = value; }    
    
    @Override
    public String toString() {    	
    	StringBuilder builder = new StringBuilder()
			.append("[") 
        	.append(_correlationId != null ? _correlationId : "---") 
    		.append(",") 
    		.append(_messageType != null ? _messageType : "---") 
    		.append(",") 
    		.append(_message != null ? StringConverter.toString(_message) : "--") 
    		.append("]");
        return builder.toString();
    }
}
