package com.telekom.cot.device.agent.system;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PropertyNotFoundException;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.system.properties.Properties;

public interface SystemService extends AgentService {
	
	/**
	 * gets the properties of given type
	 * @param propertyType type of properties to get
	 * @return the requested properties
	 * @throws AbstractAgentException throws a {@link PropertyNotFoundException} if properties of given type not available
	 */
	public <T extends Properties> T getProperties(Class<T> propertyType) throws AbstractAgentException;
}
