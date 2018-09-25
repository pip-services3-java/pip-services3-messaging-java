package org.pipservices.messaging;

import org.junit.*;
import org.pipservices.commons.errors.*;

public class MemoryMessageQueueTest {
	private MemoryMessageQueue queue;
	private MessageQueueFixture fixture;
		
    public MemoryMessageQueueTest() throws ApplicationException {
        queue = new MemoryMessageQueue("test");    	
        fixture = new MessageQueueFixture(queue);
    }
    
	@Before
	public void setUp() throws Exception {
		queue.clear(null);
		queue.open(null);
	}

	@After
	public void tearDown() throws Exception {
		queue.close(null);
	}

	@Test
	public void testSendReceiveMessage() throws Exception {
		fixture.testSendReceiveMessage();
	}

	@Test
	public void testReceiveSendMessage() throws Exception {
		fixture.testReceiveSendMessage();
	}

	@Test
	public void testMoveToDeadMessage() throws Exception {
		fixture.testMoveToDeadMessage();
	}

	@Test
	public void testReceiveAndCompleteMessage() throws Exception {
		fixture.testReceiveAndCompleteMessage();
	}

	@Test
	public void testReceiveAndAbandonMessage() throws Exception {
		fixture.testReceiveAndAbandonMessage();
	}

	@Test
	public void testSendPeekMessage() throws Exception {
		fixture.testSendPeekMessage();
	}

	@Test
	public void testPeekNoMessage() throws Exception {
		fixture.testPeekNoMessage();
	}

	@Test
	public void testListen() throws Exception {
		fixture.testListen();
	}
	
}
