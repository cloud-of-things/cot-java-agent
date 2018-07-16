package com.telekom.cot.device.agent.app;

import com.telekom.cot.device.agent.service.AgentService;

public interface AppBootstrapSteps {

    /**
     * Check connectivity to platform
     * @throws AppMainException if an error occurs or connectivity check is timed out
     */
    void checkConnectivity() throws AppMainException;
    
    /**
     * Get new device credentials by the platform service, if the device-credentials.yaml not exist.
     * 
     * @throws AppMainException
     */
    void requestAndWriteDeviceCredentials() throws AppMainException;

    /**
     * Return false, if the device credentials not exist.
     * 
     * @return
     */
    boolean credentialsAvailable();

    /**
     * Get the CoT registration status from the InventoryService.
     * 
     * @return
     * @throws AppMainException
     */
    boolean isDeviceRegistered() throws AppMainException;

    /**
     * Update the CoT inventory.
     * 
     * @throws AppMainException
     */
    void updateDevice() throws AppMainException;

    /**
     * Send the started event to the CoT.
     * 
     * @throws AppMainException
     */
    void sendEventAgentStarted() throws AppMainException;

    /**
     * Create and register the device by the external Id.
     * 
     * @throws AppMainException
     */
    void createAndRegisterDevice() throws AppMainException;

    /**
     * Get a specific service implementation and start this one.
     * 
     * @param clazz
     * @throws AppMainException
     */
    <T extends AgentService> void startService(Class<T> clazz) throws AppMainException;

    /**
     * Find the service by class and stop the service.
     * 
     * @param clazz
     * @throws AppMainException
     */
    <T extends AgentService> void stopService(Class<T> clazz) throws AppMainException;
    
}