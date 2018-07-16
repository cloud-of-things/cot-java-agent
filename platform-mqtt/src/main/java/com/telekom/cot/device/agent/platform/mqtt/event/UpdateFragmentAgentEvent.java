package com.telekom.cot.device.agent.platform.mqtt.event;

import com.telekom.cot.device.agent.platform.mqtt.PublishCallback;
import com.telekom.cot.device.agent.platform.mqtt.PublishedValues;

public class UpdateFragmentAgentEvent extends PublishedValuesAgentEvent {

	public UpdateFragmentAgentEvent(PublishCallback source, PublishedValues publishedValues) {
		super(source, publishedValues);
	}
}