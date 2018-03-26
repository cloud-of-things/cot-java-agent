package com.telekom.cot.device.agent.sensor;

import com.telekom.cot.device.agent.service.AgentService;

public interface SensorService extends AgentService {
	/**
	 * adds a measurement to the list of measurements collected by all registered SensorDeviceServices
	 * @param sensorMeasurement measurement to add
	 */
	public void addMeasurement(SensorMeasurement sensorMeasurement);
}
