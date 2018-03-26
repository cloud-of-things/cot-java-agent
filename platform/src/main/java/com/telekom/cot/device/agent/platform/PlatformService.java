package com.telekom.cot.device.agent.platform;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationCollection;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SupportedOperations;
import com.telekom.m2m.cot.restsdk.measurement.Measurement;

/**
 * Wraps the CoT SDK functionality.
 *
 */
public interface PlatformService extends AgentService {

    /**
     * gets (or generates by template) the external id value

     * @return the external id value
     * @throws AbstractAgentException 
     */
	public String getExternalIdValue() throws AbstractAgentException;
	
	/**
	 * creates a new event at platform with given attributes
	 * @param time date and time the event occurred
	 * @param type type of the event
	 * @param text description of the event
	 * @param attributes optional attributes 
	 * @param object optional object to pass
	 */
	public void createEvent(Date time, String type, String text, Map<String, Object> attributes, Object object) throws AbstractAgentException;
	
	/**
	 * creates a new alarm at platform with given attributes
	 * @param time date and time the alarm occurred
	 * @param type type of the alarm
	 * @param severity severity of the alarm
	 * @param text description of the alarm
	 * @param status current status of the alarm
	 * @param attributes optional attributes 
	 * @param object optional object to pass
	 * @throws AbstractAgentException
	 */
	public void createAlarm(Date time, String type, AlarmSeverity severity, String text, String status,
					Map<String, Object> attributes, Object object) throws AbstractAgentException;

	/**
	 * Stores a Measurement.
	 * 
	 * @param measurement
	 * @return the created measurement
	 * @throws AbstractAgentException
	 */
	public Measurement createMeasurement(Measurement measurement) throws AbstractAgentException;

	/**
	 * Stores a list of Measurements.
	 * 
	 * @param measurements
	 *            List of measurements to store.
	 * @return a list of the stored measurements
	 * @throws AbstractAgentException
	 */
	public List<Measurement> createMeasurements(final List<Measurement> measurements) throws AbstractAgentException;

	/**
	 * Retrieve the credentials of a certain device.
	 * 
	 * @param deviceId
	 * @return
	 * @throws AbstractAgentException
	 */
	public AgentCredentials getDeviceCredentials(String deviceId) throws AbstractAgentException;

	/**
	 * Creates a new Device Request to register new devices.
	 * 
	 * @param operation
	 * @return
	 */
	public Operation createNewDevice(Operation operation) throws AbstractAgentException;

	/**
	 * Stores a ManagedObject in the platform. ID should be empty, will be ignored if present.
	 * 
	 * @param managedObject
	 * @return
	 * @throws AbstractAgentException
	 */
	public ManagedObject createManagedObject(ManagedObject managedObject) throws AbstractAgentException;

	/**
	 * Store an ExternalId in the platform.
	 * 
	 * @param managedObjectId
	 * @return the ExternalId
	 * @throws AbstractAgentException
	 */
	public ExternalId createExternalId(String managedObjectId) throws AbstractAgentException;

	/**
	 * Retrieves External Identity objects from the CoT.
	 * 
	 * @return
	 * @throws AbstractAgentException
	 */
	public ExternalId getExternalId() throws AbstractAgentException;

	/**
	 * Retrieves a ManagedObject identified by ID from the platform.
	 * <p>
	 * Does not set withParents, so no parents will be loaded.
	 * 
	 * @return
	 * @throws PlatformServiceException
	 */
	public ManagedObject getManagedObject() throws AbstractAgentException;

	/**
	 * Updates the given managed object at platform
	 * 
	 * @param managedObject
	 *            the managed object to update
	 * @throws AbstractAgentException
	 */
	public void updateManagedObject(ManagedObject managedObject) throws AbstractAgentException;

	/**
	 * Update the supported operation of the agent.
	 * 
	 * @param supportedOperations
	 *            the supported operations of the agent
	 * @throws AbstractAgentException
	 */
	public void updateSupportedOperations(SupportedOperations supportedOperations) throws AbstractAgentException;

	/**
	 * Get the operations.
	 * 
	 * @param operationStatus
	 * @param resultSize
	 * @return
	 * @throws AbstractAgentException
	 */
	public OperationCollection getOperationCollection(OperationStatus operationStatus, Integer resultSize)
			throws AbstractAgentException;

    /**
     * Get the operations.
     * 
     * @param operationStatus
     * @param resultSize
     * @param fragmentType
     * @return
     * @throws AbstractAgentException
     */
    public OperationCollection getOperationCollection(String fragmentType, OperationStatus operationStatus, Integer resultSize)
            throws AbstractAgentException;

	/**
	 * Update the operation.
	 * 
	 * @param externalId
	 * @param operation
	 */
	public void updateOperation(Operation operation) throws AbstractAgentException;

    /**
     * Download and verify the new agent software.
     * 
     * @param url
     *            the CoT download URL
     * @return TODO
     * @return the location path of the new software file
     */
	public byte[] downloadBinary(URL url) throws AbstractAgentException;
}
