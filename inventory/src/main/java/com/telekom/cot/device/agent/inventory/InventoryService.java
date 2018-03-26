package com.telekom.cot.device.agent.inventory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.m2m.cot.restsdk.library.Fragment;

public interface InventoryService extends AgentService {

    /**
     * gets whether the device is registered
     * 
     * @return whether the device is registered
     * @throws AbstractAgentException if an error occurs
     */
    public boolean isDeviceRegistered() throws AbstractAgentException;
	
    /**
     * creates and registers a new device at platform
     * 
     * @throws AbstractAgentException if an error occurs during registration
     */

    public void createAndRegisterDevice() throws AbstractAgentException;
	
    /**
     * updates the device at platform 
     * 
     * @throws AbstractAgentException if an error occurs during update
     */
    public void updateDevice() throws AbstractAgentException;
    
    /**
     * updates the given fragment at platform
     * @param fragment fragment to update
     * @throws AbstractAgentException if an error occurs during update
     */
	public void update(Fragment fragment) throws AbstractAgentException;

}
