package com.telekom.cot.device.agent.inventory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.AgentService;

public interface InventoryService extends AgentService {

	/**
	 * gets whether the device is registered
	 * 
	 * @return whether the device is registered
	 * @throws AbstractAgentException
	 *             if an error occurs
	 */
	public boolean isDeviceRegistered() throws AbstractAgentException;

	/**
	 * creates and registers a new device at platform
	 * 
	 * @throws AbstractAgentException
	 *             if an error occurs during registration
	 */

	public void createAndRegisterDevice() throws AbstractAgentException;

	/**
	 * updates the device at platform
	 * 
	 * @throws AbstractAgentException
	 *             if an error occurs during update
	 */
	public void updateDevice() throws AbstractAgentException;
}
