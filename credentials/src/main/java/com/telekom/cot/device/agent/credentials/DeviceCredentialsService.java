package com.telekom.cot.device.agent.credentials;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.AgentService;

public interface DeviceCredentialsService extends AgentService {

	/**
	 * requests device credentials from platform
	 * @return the requested device credentials
	 * @throws AbstractAgentException
	 */
	public void requestAndWriteDeviceCredentials() throws AbstractAgentException;
}
