package com.telekom.cot.device.agent.sensor;

import java.util.Map;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.AgentService;

public interface AlarmService extends AgentService{
	/**
	 * creates a new alarm and sends it to the platform
	 * @param type type of the alarm
	 * @param severity severity of the alarm
	 * @param text description of the alarm
	 * @param status current status of the alarm
	 * @param attributes optional attributes 
	 * @param object optional object to pass
	 * @throws AbstractAgentException if the alarm can't be created or sent
	 */
	public void createAlarm(String type, AlarmSeverity severity, String text, String status, Map<String, Object> attributes, Object object) throws AbstractAgentException;
}
