package com.telekom.cot.device.agent.raspbian.sensor;

import java.io.InputStream;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.SensorDeviceServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.sensor.configuration.SensorConfiguration;
import com.telekom.cot.device.agent.sensor.deviceservices.TemperatureSensor;

public class CpuTemperatureSensor extends TemperatureSensor {

	/** command and parameters to read the raspberry pi cpu temperature at raspbian */
	protected static final String[] READ_CPU_TEMPERATURE_COMMAND = { "/bin/sh", "-c", "sudo vcgencmd measure_temp | tr -d \"temp=\" | tr -d \"'C\"" };
	
	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CpuTemperatureSensor.class);

	@Inject
	private CpuTemperatureSensorConfiguration configuration;
	private Runtime currentRuntime;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		// get current java runtime
		currentRuntime = Runtime.getRuntime();

		super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws AbstractAgentException {
		currentRuntime = null;
		super.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SensorConfiguration getSensorConfiguration() throws AbstractAgentException {
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 * @throws AbstractAgentException 
	 */
	@Override
	protected SensorMeasurement getTemperatureMeasurement() throws AbstractAgentException {
		// execute read cpu temperature and get value as string
		Process process = executeReadCpuTemperatureCommand();
		String temperatureValue = getReadCpuTemperatureProcessOutput(process);

		// parse temperature value as float and create measurement
		try {	
			float temperature = Float.parseFloat(temperatureValue);
			return new SensorMeasurement(TEMPERATURE_MEASUREMENT_TYPE, temperature, "°C");
		} catch (Exception e) {
			LOGGER.error("Can't parse the result '{}' from the 'read cpu temperature' process", temperatureValue, e);
			throw new SensorDeviceServiceException("Can't parse the result from the 'read cpu temperature' process", e);
		}
	}
	
	/**
	 * executes the READ_CPU_TEMPERATURE_COMMAND to read the cpu temperature and returns the process
	 */
	private Process executeReadCpuTemperatureCommand() throws SensorDeviceServiceException {
		try {
			return currentRuntime.exec(READ_CPU_TEMPERATURE_COMMAND);
		} catch(Exception e) {
			throw new SensorDeviceServiceException("Can't execute the 'read cpu temperature' command", e);
		}
	}

	/**
	 * reads the output of the given process and returns as string
	 */
	private String getReadCpuTemperatureProcessOutput(Process process) throws SensorDeviceServiceException {
		try {	
			InputStream inputStream = process.getInputStream();
			StringWriter writer = new StringWriter();
			
			int value;
			while((value = inputStream.read()) != -1) {
				writer.write(value);
			}

			String processOutput = writer.toString();
			LOGGER.info("read cpu temperature output = {}", processOutput);
			return processOutput;
		} catch(Exception e) {
			throw new SensorDeviceServiceException("Can't get the 'read cpu temperature' process output", e);
		}
	}
}
