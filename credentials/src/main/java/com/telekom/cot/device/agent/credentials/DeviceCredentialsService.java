package com.telekom.cot.device.agent.credentials;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;

public interface DeviceCredentialsService extends AgentService {

	/**
	 * generates the device id used for device registration
	 * @return the generated device id
	 * @throws AbstractAgentException
	 */
	public String getDeviceId() throws AbstractAgentException;
	
	/**
	 * check if local device credentials are available
	 * @return whether local device credentials are available
	 */
	public boolean credentialsAvailable();

	/**
	 * requests device credentials from platform
	 * @return the requested device credentials
	 * @throws AbstractAgentException
	 */
	public AgentCredentials requestCredentials() throws AbstractAgentException;
}
