package com.telekom.cot.device.agent.platform;

import java.net.URL;
import java.util.Date;
import java.util.List;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.service.AgentService;

/**
 * Wraps the CoT SDK functionality.
 *
 */
public interface PlatformService extends AgentService {

	/**
	 * gets (or generates by template) the external id value
	 * 
	 * @return the external id value
	 * @throws AbstractAgentException
	 */
	public String getExternalIdValue() throws AbstractAgentException;

	/**
	 * creates a new event at platform with given attributes
	 * 
	 * @param time
	 *            date and time the event occurred
	 * @param type
	 *            type of the event
	 * @param text
	 *            description of the event
	 * @param condition
	 *            the response template condition
	 */
	public void createEvent(Date time, String type, String text,String condition) throws AbstractAgentException;

	/**
	 * creates a new alarm at platform with given attributes
	 * 
	 * @param time
	 *            date and time the alarm occurred
	 * @param type
	 *            type of the alarm
	 * @param severity
	 *            severity of the alarm
	 * @param text
	 *            description of the alarm
	 * @param status
	 *            current status of the alarm
	 * @throws AbstractAgentException
	 */
	public void createAlarm(Date time, String type, AlarmSeverity severity, String text, String status) throws AbstractAgentException;

	/**
	 * stores a measurement
	 * 
	 * @param time
	 *            date and time the measurement was taken
	 * @param type
	 *            type of the measurement
	 * @param value
	 *            value of the measurement
	 * @param unit
	 *            unit of the measurement
	 * @throws AbstractAgentException
	 */
	public void createMeasurement(Date time, String type, float value, String unit) throws AbstractAgentException;

	/**
	 * stores a list of measurements
	 * 
	 * @param measurements
	 *            list of measurements to store
	 * @throws AbstractAgentException
	 */
	public void createMeasurements(final List<SensorMeasurement> measurements) throws AbstractAgentException;

	/**
	 * Retrieve the credentials of a certain device.
	 * 
	 * @param deviceId
	 * @param interval
	 *            in seconds
	 * @return
	 * @throws AbstractAgentException
	 */
	public AgentCredentials getDeviceCredentials(String deviceId, int interval) throws AbstractAgentException;

	/**
	 * Stores an agent managed object in the platform. ID should be empty, will be
	 * ignored if present.
	 * 
	 * @param agentManagedObject
	 *            the agent managed object to store in the platform
	 * @return The ID of the new managed object
	 * @throws AbstractAgentException
	 */
	public String createAgentManagedObject(AgentManagedObject agentManagedObject) throws AbstractAgentException;

	/**
	 * Stores an ExternalId in the platform.
	 * 
	 * @param managedObjectId
	 *            ID of the managed object
	 * @throws AbstractAgentException
	 */
	public void createExternalId(String managedObjectId) throws AbstractAgentException;

	/**
	 * Is the external identity object available in the CoT.
	 * 
	 * @return true if the external ID exists, false otherwise.
	 * @throws AbstractAgentException
	 */
	public boolean isExternalIdAvailable() throws AbstractAgentException;

	/**
	 * Retrieves an agent managed object identified by ID from the platform.
	 * <p>
	 * Does not set withParents, so no parents will be loaded.
	 * 
	 * @return Agent managed object
	 * @throws PlatformServiceException
	 */
	public AgentManagedObject getAgentManagedObject() throws AbstractAgentException;

	/**
	 * Updates the given agent managed object at platform
	 * 
	 * @param agentManagedObject
	 *            the agent managed object to update
	 * @throws AbstractAgentException
	 */
	public void updateAgentManagedObject(AgentManagedObject agentManagedObject) throws AbstractAgentException;

	/**
	 * Update the supported operations of the agent.
	 * 
	 * @param supportedOperations
	 *            the supported operation names of the agent
	 * @throws AbstractAgentException
	 */
	public void updateSupportedOperations(List<String> supportedOperationNames) throws AbstractAgentException;

	/**
	 * get the next pending operation to execute
	 * @return the next operation to execute or {@code null} if there's no pending operation
	 * @throws AbstractAgentException if an error occurs
	 */
	public Operation getNextPendingOperation() throws AbstractAgentException;
	
	/**
     * get operations with given status
     * 
     * @param operationName (optional) name of the operation (fragment)
     * @param status status of the operations to get
     * @return a list of operations with given status (maybe empty)
     * @throws AbstractAgentException if an error occurs
	 */
	public List<Operation> getOperations(String operationName, OperationStatus status) throws AbstractAgentException;

	/**
	 * Update the status of an operation with given id
	 * 
	 * @param operationId id of the operation to update
	 * @param newStatus new status to set
	 * @throws AbstractAgentException if updating status has not been successful
	 */
	public void updateOperationStatus(String operationId, OperationStatus newStatus) throws AbstractAgentException;

	/**
	 * Download a binary file at given url 
	 * 
	 * @param url the URL to download the binary file from 
	 * @return the binary file as byte array
	 * @throws AbstractAgentException
	 */
	public byte[] downloadBinary(URL url) throws AbstractAgentException;
}
