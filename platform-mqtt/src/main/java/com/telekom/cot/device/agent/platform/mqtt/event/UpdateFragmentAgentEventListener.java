package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class UpdateFragmentAgentEventListener
		extends PublishedValuesAgentEventListener<String, UpdateFragmentAgentEvent> {

	private static final String ATTR_INVENTORY_UPDATE_ID = "id";
	
	public String create(PublishedValues publishedValues) {
		return publishedValues.getValue(ATTR_INVENTORY_UPDATE_ID);
	}

	@Override
	public Class<UpdateFragmentAgentEvent> getEventClass() {
	    return UpdateFragmentAgentEvent.class;
	}
	
}
