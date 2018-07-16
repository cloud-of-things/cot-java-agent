package com.telekom.cot.device.agent.app;

import com.telekom.cot.device.agent.service.AgentService;

public interface AppBootstrapServiceManager {
    
    /**
     * Load the {@code AgentService} implementations.
     * @throws AppMainException
     */
    void loadAndInitializeAgentServices() throws AppMainException;
    
    /**
     * Get the specific service instance by the interface.  
     * @param clazz
     * @return the specific instance of the {@code AgentService}
     * @throws AppMainException
     */
    <T extends AgentService> T getService(Class<T> clazz) throws AppMainException;
    
    /**
     * Stop the specific service instance by the interface.
     * @param clazz
     * @throws AppMainException
     */
    <T extends AgentService> void stopService(Class<T> clazz) throws AppMainException;
    
    /**
     * Start the specific service instance by the interface.
     * @param clazz
     * @return 
     * @throws AppMainException
     */
    <T extends AgentService> void startService(Class<T> clazz) throws AppMainException;
   
}
