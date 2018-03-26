package com.telekom.cot.device.agent.sensor;

import java.util.Map;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.AgentService;

public interface EventService extends AgentService {

	/**
	 * creates a new event and sends it to the platform 
	 *
	 * @param type type of the event
	 * @param text description of the event
	 * @param attributes optional attributes 
	 * @param object optional object to pass
	 * @throws AbstractAgentException if the event can't be created or sent
	 */
	public void createEvent(String type, String text, Map<String, Object> attributes, Object object) throws AbstractAgentException;
}
