package org.pipservices3.messaging.build;

import org.pipservices3.commons.config.ConfigParams;
import org.pipservices3.commons.config.IConfigurable;
import org.pipservices3.commons.errors.ConfigException;
import org.pipservices3.commons.refer.IReferenceable;
import org.pipservices3.commons.refer.IReferences;
import org.pipservices3.commons.refer.ReferenceException;
import org.pipservices3.components.build.Factory;
import org.pipservices3.messaging.queues.IMessageQueue;

/**
 * Creates [[IMessageQueue]] components by their descriptors.
 * Name of created message queue is taken from its descriptor.
 *
 * @see Factory
 * @see org.pipservices3.messaging.queues.MessageQueue
 */
public abstract class MessageQueueFactory extends Factory implements IMessageQueueFactory, IConfigurable, IReferenceable {
    protected ConfigParams _config;
    protected IReferences _references;

    /**
     * Configures component by passing configuration parameters.
     *
     * @param config configuration parameters to be set.
     */
    @Override
    public void configure(ConfigParams config) throws ConfigException {
        this._config = config;
    }

    /**
     * Creates a message queue component and assigns its name.
     *
     * @param name a name of the created message queue.
     */
    @Override
    public abstract IMessageQueue createQueue(String name) throws ReferenceException;

    /**
     * Sets references to dependent components.
     *
     * @param references references to locate the component dependencies.
     */
    @Override
    public void setReferences(IReferences references) throws ReferenceException, ConfigException {
        this._references = references;
    }
}
