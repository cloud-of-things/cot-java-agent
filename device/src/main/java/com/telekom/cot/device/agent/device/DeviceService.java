package com.telekom.cot.device.agent.device;

import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.channel.QueueChannel;

public interface DeviceService extends AgentService {
	/**
	 * gets the queue holding the sensor measurements
	 * 
	 * @return queue
	 */
	public QueueChannel<SensorMeasurement> getQueueChannel();
}
