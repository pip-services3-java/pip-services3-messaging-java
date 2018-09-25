package org.pipservices.messaging.queues;

import static org.junit.Assert.*;

import org.pipservices.messaging.queues.IMessageQueue;
import org.pipservices.messaging.queues.IMessageReceiver;
import org.pipservices.messaging.queues.MessageEnvelop;

public class MessageQueueFixture{
    private IMessageQueue _queue;

    public MessageQueueFixture(IMessageQueue queue) {
        _queue = queue;
    }

    public void testSendReceiveMessage() throws Exception {
        MessageEnvelop envelop1 = new MessageEnvelop("123", "Test", "Test message");
        _queue.send(null, envelop1);

        Long count = _queue.getMessageCount();
        assertTrue(count > 0);

        MessageEnvelop envelop2 = _queue.receive(null, 10000);
        assertNotNull(envelop2);
        assertEquals(envelop1.getMessageType(), envelop2.getMessageType());
        assertEquals(envelop1.getMessage(), envelop2.getMessage());
        assertEquals(envelop1.getCorrelationId(), envelop2.getCorrelationId());
    }

    public void testReceiveSendMessage() throws Exception {
        MessageEnvelop envelop1 = new MessageEnvelop("123", "Test", "Test message");

        new Thread(new Runnable() {
        	@Override
        	public void run() {
        		try {
		            Thread.sleep(200);
		            _queue.send(null, envelop1);
        		} catch (Exception ex) {
        			// Ignore...
        		}
        	}
        }).start();

        MessageEnvelop envelop2 = _queue.receive(null, 10000);
        assertNotNull(envelop2);
        assertEquals(envelop1.getMessageType(), envelop2.getMessageType());
        assertEquals(envelop1.getMessage(), envelop2.getMessage());
        assertEquals(envelop1.getCorrelationId(), envelop2.getCorrelationId());
    }

    public void testMoveToDeadMessage() throws Exception {
        MessageEnvelop envelop1 = new MessageEnvelop("123", "Test", "Test message");
        _queue.send(null, envelop1);

        MessageEnvelop envelop2 = _queue.receive(null, 10000);
        assertNotNull(envelop2);
        assertEquals(envelop1.getMessageType(), envelop2.getMessageType());
        assertEquals(envelop1.getMessage(), envelop2.getMessage());
        assertEquals(envelop1.getCorrelationId(), envelop2.getCorrelationId());

        _queue.moveToDeadLetter(envelop2);
    }

    public void testReceiveAndCompleteMessage() throws Exception {
        MessageEnvelop envelop1 = new MessageEnvelop("123", "Test", "Test message");
        _queue.send(null, envelop1);
        
        MessageEnvelop envelop2 = _queue.receive(null, 10000);
        assertNotNull(envelop2);
        assertEquals(envelop1.getMessageType(), envelop2.getMessageType());
        assertEquals(envelop1.getMessage(), envelop2.getMessage());
        assertEquals(envelop1.getCorrelationId(), envelop2.getCorrelationId());

         _queue.complete(envelop2);
        //envelop2 = _queue.peek(null);
        //assertNull(envelop2);
    }

    public void testReceiveAndAbandonMessage() throws Exception {
        MessageEnvelop envelop1 = new MessageEnvelop("123", "Test", "Test message");
        _queue.send(null, envelop1);
        
        MessageEnvelop envelop2 = _queue.receive(null, 10000);
        assertNotNull(envelop2);
        assertEquals(envelop1.getMessageType(), envelop2.getMessageType());
        assertEquals(envelop1.getMessage(), envelop2.getMessage());
        assertEquals(envelop1.getCorrelationId(), envelop2.getCorrelationId());

        _queue.abandon(envelop2);
        
        envelop2 = _queue.receive(null, 10000);
        assertNotNull(envelop2);
        assertEquals(envelop1.getMessageType(), envelop2.getMessageType());
        assertEquals(envelop1.getMessage(), envelop2.getMessage());
        assertEquals(envelop1.getCorrelationId(), envelop2.getCorrelationId());
    }

    public void testSendPeekMessage() throws Exception {
        MessageEnvelop envelop1 = new MessageEnvelop("123", "Test", "Test message");
        _queue.send(null, envelop1);
        
        try {
        	Thread.sleep(200);
        } catch (InterruptedException ex) {
        	// Ignore...
        }
        
        MessageEnvelop envelop2 = _queue.peek(null);
        assertNotNull(envelop2);
        assertEquals(envelop1.getMessageType(), envelop2.getMessageType());
        assertEquals(envelop1.getMessage(), envelop2.getMessage());
        assertEquals(envelop1.getCorrelationId(), envelop2.getCorrelationId());
    }

    public void testPeekNoMessage() throws Exception {
        MessageEnvelop envelop = _queue.peek(null);
        assertNull(envelop);
    }

	public void testListen() throws Exception {
        MessageEnvelop envelop1 = new MessageEnvelop("123", "Test", "Test message");
        MessageEnvelop envelop2 = new MessageEnvelop();

        _queue.beginListen(null, new IMessageReceiver() {
        	@Override
        	public void receiveMessage(MessageEnvelop envelop, IMessageQueue queue) {
	            envelop2.setMessageId(envelop.getMessageId());
	            envelop2.setCorrelationId(envelop.getCorrelationId());
	            envelop2.setMessageType(envelop.getMessageType());
	            envelop2.setMessage(envelop.getMessage());
        	}
        });

        _queue.send(null, envelop1);
        
        try {
        	Thread.sleep(200);
        } catch (InterruptedException ex) {
        	// Ignore...
        }

        assertNotNull(envelop2);
        assertEquals(envelop1.getMessageType(), envelop2.getMessageType());
        assertEquals(envelop1.getMessage(), envelop2.getMessage());
        assertEquals(envelop1.getCorrelationId(), envelop2.getCorrelationId());

        _queue.endListen(null);
    }

}
