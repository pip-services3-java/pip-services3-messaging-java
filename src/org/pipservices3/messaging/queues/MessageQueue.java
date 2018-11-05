package org.pipservices3.messaging.queues;

import java.util.*;

import org.pipservices3.commons.config.*;
import org.pipservices3.commons.errors.*;
import org.pipservices3.commons.refer.*;
import org.pipservices3.commons.run.*;
import org.pipservices3.components.auth.CredentialParams;
import org.pipservices3.components.auth.CredentialResolver;
import org.pipservices3.components.connect.ConnectionParams;
import org.pipservices3.components.connect.ConnectionResolver;
import org.pipservices3.components.count.CompositeCounters;
import org.pipservices3.components.log.CompositeLogger;

/**
 * Abstract message queue that is used as a basis for specific message queue implementations.
 * <p>
 * ### Configuration parameters ###
 * <ul>
 * <li>name:                        name of the message queue
 * <li>connection(s):
 *   <ul>
 *   <li>discovery_key:             key to retrieve parameters from discovery service
 *   <li>protocol:                  connection protocol like http, https, tcp, udp
 *   <li>host:                      host name or IP address
 *   <li>port:                      port number
 *   <li>uri:                       resource URI or connection string with all parameters in it
 *   </ul>
 * <li>credential(s):
 *   <ul>
 *   <li>store_key:                 key to retrieve parameters from credential store
 *   <li>username:                  user name
 *   <li>password:                  user password
 *   <li>access_id:                 application access id
 *   <li>access_key:                application secret key
 *   </ul>
 * </ul>
 * <p>
 * ### References ###
 * <ul>
 * <li>*:logger:*:*:1.0         (optional) <a href="https://raw.githubusercontent.com/pip-services3-java/pip-services3-components-java/master/doc/api/org/pipservices3/components/log/ILogger.html">ILogger</a> components to pass log messages
 * <li>*:counters:*:*:1.0         (optional) <a href="https://raw.githubusercontent.com/pip-services3-java/pip-services3-components-java/master/doc/api/org/pipservices3/components/count/ICounters.html">ICounters</a> components to pass collected measurements
 * <li>*:discovery:*:*:1.0        (optional) <a href="https://raw.githubusercontent.com/pip-services3-java/pip-services3-components-java/master/doc/api/org/pipservices3/components/connect/IDiscovery.html">IDiscovery</a> services to resolve connection
 * <li>*:credential-store:*:*:1.0 (optional) <a href="https://raw.githubusercontent.com/pip-services3-java/pip-services3-components-java/master/doc/api/org/pipservices3/components/auth/ICredentialStore.html">ICredentialStore</a> componetns to lookup credential(s)
 * </ul>
 */
public abstract class MessageQueue implements IMessageQueue, IReferenceable, IConfigurable, IOpenable, IClosable {

	protected String _name;
	protected String _kind;
	protected MessagingCapabilities _capabilities = new MessagingCapabilities(true, true, true, true, true, true, true,
			false, true);;
	protected Object _lock = new Object();
	protected CompositeLogger _logger = new CompositeLogger();
	protected CompositeCounters _counters = new CompositeCounters();
	protected ConnectionResolver _connectionResolver = new ConnectionResolver();
	protected CredentialResolver _credentialResolver = new CredentialResolver();

	/**
	 * Creates a new instance of the message queue.
	 */
	public MessageQueue() {
	}

	/**
	 * Creates a new instance of the message queue.
	 * 
	 * @param name (optional) a queue name
	 */
	public MessageQueue(String name) {
		_name = name;
	}

	/**
	 * Creates a new instance of the message queue.
	 * 
	 * @param name   (optional) a queue name
	 * @param config configuration parameters
	 */
	public MessageQueue(String name, ConfigParams config) {
		_name = name;
		if (config != null)
			configure(config);
	}

	/**
	 * Configures component by passing configuration parameters.
	 * 
	 * @param config configuration parameters to be set.
	 */
	public void configure(ConfigParams config) {
		_name = NameResolver.resolve(config, _name);
		_connectionResolver.configure(config, true);
		_credentialResolver.configure(config, true);
	}

	/**
	 * Sets references to dependent components.
	 * 
	 * @param references references to locate the component dependencies.
	 * @throws ReferenceException when no found references.
	 */
	public void setReferences(IReferences references) throws ReferenceException {
		_logger.setReferences(references);
		_counters.setReferences(references);
		_connectionResolver.setReferences(references);
		_credentialResolver.setReferences(references);
	}

	/**
	 * Gets the queue name
	 * 
	 * @return the queue name.
	 */
	public String getName() {
		return _name != null ? _name : "undefined";
	}

	/**
	 * Gets the queue capabilities
	 * 
	 * @return the queue's capabilities object.
	 */
	public MessagingCapabilities getCapabilities() {
		return _capabilities;
	}

	public abstract Long getMessageCount();

	/**
	 * Checks if the component is opened.
	 * 
	 * @return true if the component has been opened and false otherwise.
	 */
	public abstract boolean isOpen();

	/**
	 * Opens the component.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @throws ApplicationException when error occured.
	 */
	public void open(String correlationId) throws ApplicationException {
		ConnectionParams connection = _connectionResolver.resolve(correlationId);
		CredentialParams credential = _credentialResolver.lookup(correlationId);
		open(correlationId, connection, credential);
	}

	/**
	 * Opens the component with given connection and credential parameters.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param connection    connection parameters
	 * @param credential    credential parameters
	 * @throws ApplicationException when error occured.
	 */
	public abstract void open(String correlationId, ConnectionParams connection, CredentialParams credential)
			throws ApplicationException;

	/**
	 * Closes component and frees used resources.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @throws ApplicationException when error occured.
	 */
	public abstract void close(String correlationId) throws ApplicationException;

	/**
	 * Clears component state.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @throws ApplicationException when error occured.
	 */
	public abstract void clear(String correlationId) throws ApplicationException;

	/**
	 * Sends a message into the queue.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param message       a message envelop to be sent.
	 * @throws ApplicationException when error occured.
	 */
	public abstract void send(String correlationId, MessageEnvelop message) throws ApplicationException;

	/**
	 * Sends an object into the queue. Before sending the object is converted into
	 * JSON string and wrapped in a [[MessageEnvelop]].
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param messageType   a message type
	 * @param message       an object value to be sent
	 * @throws ApplicationException when error occured.
	 * 
	 * @see #send(String, MessageEnvelop)
	 */
	public void sendAsObject(String correlationId, String messageType, Object message) throws ApplicationException {
		MessageEnvelop envelop = new MessageEnvelop(correlationId, messageType, message);
		send(correlationId, envelop);
	}

	/**
	 * Peeks a single incoming message from the queue without removing it. If there
	 * are no messages available in the queue it returns null.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @return a message envelop object.
	 * @throws ApplicationException when error occured.
	 */
	public abstract MessageEnvelop peek(String correlationId) throws ApplicationException;

	/**
	 * Peeks multiple incoming messages from the queue without removing them. If
	 * there are no messages available in the queue it returns an empty list.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param messageCount  a maximum number of messages to peek.
	 * @return a list with messages.
	 * @throws ApplicationException when error occured.
	 */
	public abstract List<MessageEnvelop> peekBatch(String correlationId, int messageCount) throws ApplicationException;

	/**
	 * Receives an incoming message and removes it from the queue.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param waitTimeout   a timeout in milliseconds to wait for a message to come.
	 * @return a message envelop object.
	 * @throws ApplicationException when error occured.
	 */
	public abstract MessageEnvelop receive(String correlationId, long waitTimeout) throws ApplicationException;

	/**
	 * Renews a lock on a message that makes it invisible from other receivers in
	 * the queue. This method is usually used to extend the message processing time.
	 * 
	 * @param message     a message to extend its lock.
	 * @param lockTimeout a locking timeout in milliseconds.
	 * @throws ApplicationException when error occured.
	 */
	public abstract void renewLock(MessageEnvelop message, long lockTimeout) throws ApplicationException;

	/**
	 * Returnes message into the queue and makes it available for all subscribers to
	 * receive it again. This method is usually used to return a message which could
	 * not be processed at the moment to repeat the attempt. Messages that cause
	 * unrecoverable errors shall be removed permanently or/and send to dead letter
	 * queue.
	 * 
	 * @param message a message to return.
	 * @throws ApplicationException when error occured.
	 */
	public abstract void abandon(MessageEnvelop message) throws ApplicationException;

	/**
	 * Permanently removes a message from the queue. This method is usually used to
	 * remove the message after successful processing.
	 * 
	 * @param message a message to remove.
	 * @throws ApplicationException when error occured.
	 */
	public abstract void complete(MessageEnvelop message) throws ApplicationException;

	/**
	 * Permanently removes a message from the queue and sends it to dead letter
	 * queue.
	 * 
	 * @param message a message to be removed.
	 * @throws ApplicationException when error occured.
	 */
	public abstract void moveToDeadLetter(MessageEnvelop message) throws ApplicationException;

	/**
	 * Listens for incoming messages and blocks the current thread until queue is
	 * closed.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param receiver      a receiver to receive incoming messages.
	 * @throws ApplicationException when error occured.
	 * 
	 * @see IMessageReceiver
	 */
	public abstract void listen(String correlationId, IMessageReceiver receiver) throws ApplicationException;

	/**
	 * Listens for incoming messages without blocking the current thread.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @param receiver      a receiver to receive incoming messages.
	 * 
	 * @see IMessageReceiver
	 */
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

	/**
	 * Ends listening for incoming messages. When this method is call [[listen]]
	 * unblocks the thread and execution continues.
	 * 
	 * @param correlationId (optional) transaction id to trace execution through
	 *                      call chain.
	 * @throws ApplicationException when error occured.
	 */
	public abstract void endListen(String correlationId) throws ApplicationException;

	/**
	 * Gets a string representation of the object.
	 * 
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return "[" + getName() + "]";
	}

}
