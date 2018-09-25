package org.pipservices.messaging;

public class MessagingCapabilities {
	private boolean _messageCount;
	private boolean _send;
	private boolean _receive;
	private boolean _peek;
	private boolean _peekBatch;
	private boolean _renewLock;
	private boolean _abandon;
	private boolean _deadLetter;
	private boolean _clear;
	
	public MessagingCapabilities(boolean messageCount, boolean send, boolean receive, 
		boolean peek, boolean peekBatch, boolean renewLock, boolean abandon, 
		boolean deadLetter, boolean clear) {
		_messageCount = messageCount;
		_send = send;
		_receive = receive;
		_peek = peek;
		_peekBatch = peekBatch;
		_renewLock = renewLock;
		_abandon = abandon;
		_deadLetter = deadLetter;
		_clear = clear;
	}
	
	public boolean canMessageCount() { return _messageCount; }
	public boolean canSend() { return _send; }
	public boolean canReceive() { return _receive; }
	public boolean canPeek() { return _peek; }
	public boolean canPeekBatch() { return _peekBatch; }
	public boolean canRenewLock() { return _renewLock; }
	public boolean canAbandon() { return _abandon; }
	public boolean canDeadLetter() { return _deadLetter; }
	public boolean canClear() { return _clear; }
}
