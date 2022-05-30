package org.pipservices3.messaging.queues;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pipservices3.commons.convert.JsonConverter;

import java.io.IOException;

public class MessageEnvelopTest {
    @Test
    public void testFromToJson() throws IOException {
        var message = new MessageEnvelope("123", "Test", "This is a test message");
        var json = JsonConverter.toJson(message);

        var message2 = MessageEnvelope.fromJSON(json);
        assertEquals(message.getMessageId(), message2.getMessageId());
        assertEquals(message.getCorrelationId(), message2.getCorrelationId());
        assertEquals(message.getMessageType(), message2.getMessageType());
        assertEquals(message.getMessage().toString(), message2.getMessage().toString());
    }
}
