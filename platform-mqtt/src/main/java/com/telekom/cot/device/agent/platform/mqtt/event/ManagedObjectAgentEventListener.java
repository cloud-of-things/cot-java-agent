package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;
import com.telekom.cot.device.agent.platform.objects.ManagedObject;

/**
 *  Listen to the ManagedObjectAgentEvent and create a managed ManagedObject.
 */
public class ManagedObjectAgentEventListener
                extends PublishedValuesAgentEventListener<ManagedObject, ManagedObjectAgentEvent> {

    /** The JSON path of MANAGED_OBJECT_ID_RES (see mqtt.response.templates) */ 
    private static final String ATTR_MANAGED_OBJECT_ID = "managedObject.id";

    /**
     * Create a ManagedObject by the PublishedValues from the PublishCallback.
     */
    public ManagedObject create(PublishedValues publishedValues) {
        return new ManagedObject(publishedValues.getValue(ATTR_MANAGED_OBJECT_ID));
    }
    
    @Override
    public Class<ManagedObjectAgentEvent> getEventClass() {
        return ManagedObjectAgentEvent.class;
    }
}
