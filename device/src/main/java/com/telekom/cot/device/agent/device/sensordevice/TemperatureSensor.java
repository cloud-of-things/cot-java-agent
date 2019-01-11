package com.telekom.cot.device.agent.device.sensordevice;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.alarm.AlarmService;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.SensorDeviceServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.device.DeviceService;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.service.AbstractAgentService;

public abstract class TemperatureSensor extends AbstractAgentService implements SensorDeviceService {

	public static final String TEMPERATURE_MEASUREMENT_TYPE = "c8y_Temperature";

	private static final long MILLISECONDS_PER_SECOND = 1000l;

	/** the logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(TemperatureSensor.class);

	/** the running flag */
	private AtomicBoolean running = new AtomicBoolean(false);

	/** the worker thread */
	private Thread worker;

	private SensorConfiguration configuration;
	@Inject
	private AlarmService alarmService;
	@Inject
	private DeviceService deviceService;

	/** the fixed rate at which values are measured */
	private int recordReadingsInterval;

	/**
	 * gets the next temperature measurement
	 * 
	 * @return the next read temperature measurement
	 */
	protected abstract SensorMeasurement getTemperatureMeasurement() throws AbstractAgentException;

	/**
	 * gets the configuration for the temperature sensor (must be implemented by sub
	 * class)
	 */
	protected abstract SensorConfiguration getSensorConfiguration() throws AbstractAgentException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		LOGGER.debug("start " + this.getClass().getSimpleName());

		// get temperature sensor configuration (from implementing class)
		configuration = getSensorConfiguration();

		AssertionUtil.assertNotNull(alarmService, SensorDeviceServiceException.class, LOGGER, "no alarm service given");
		AssertionUtil.assertNotNull(deviceService, SensorDeviceServiceException.class, LOGGER,
				"no sensor service given");

		// get fixed rate (in seconds) at which values (e.g. cpu temperature) are
		// measured
		recordReadingsInterval = configuration.getRecordReadingsInterval();

		// start the thread getting the temperature measurements
		running.set(true);
		worker = new Thread(() -> recordReading());
		worker.start();

		super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws AbstractAgentException {
		if (isStarted()) {
			LOGGER.debug("stop " + this.getClass().getSimpleName());

			// Set running to false so that the worker thread execution will be stopped
			running.set(false);

			// Wait until the worker thread finishes recording the current reading
			try {
				worker.join();
			} catch (Exception e) {
				throw new SensorDeviceServiceException("The SensorDeviceService thread has been interrupted");
			}

			super.stop();
		}
	}

	/**
	 * run method for the worker thread
	 */
	private void recordReading() {
		while (running.get()) {
			try {
				SensorMeasurement sensorMeasurement = getTemperatureMeasurement();
				if (!Objects.isNull(sensorMeasurement)) {
					// Send an alarm if the measurement meets a defined criteria
					// This is done in another thread so that the record reading is not blocked
					Thread thread = new Thread(() -> checkAlarms(sensorMeasurement));
					thread.start();

					// Push the measurement to the queue - the sensor service sends all
					// measurements from the queue regularly
					deviceService.getQueueChannel().add(sensorMeasurement);
				}
			} catch (AbstractAgentException exception) {
				LOGGER.error("Can't get temperature measurement", exception);
			}

			sleep();
		}
	}

	/**
	 * sleeps for a given interval
	 */
	private void sleep() {
		try {
			Thread.sleep(recordReadingsInterval * MILLISECONDS_PER_SECOND);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.info("Thread was interrupted, failed to record readings");
		}
	}

	/**
	 * checks all alarms from the configuration: if the temperature meets an alarm
	 * criteria, the alarm is sent to the platform
	 * 
	 * @param temperature
	 *            the temperature to check
	 */
	private void checkAlarms(SensorMeasurement measurement) {
		float temperature = measurement.getValue();
		LOGGER.debug("current temperature = {}", temperature);

		for (SensorConfiguration.AlarmConfiguration alarmConfig : CollectionUtils
				.emptyIfNull(configuration.getAlarmConfigurations())) {
			if (temperature < alarmConfig.getMinValue() || temperature > alarmConfig.getMaxValue()) {
				continue;
			}

			// replace placeholder '<value>' by temperature value and unit
			String alarmText = alarmConfig.getText();
			alarmText = alarmText.replaceAll("<value>", String.valueOf(temperature) + " " + measurement.getUnit());

			LOGGER.info("Send alarm '{}' ({}, {})", alarmText, alarmConfig.getType(),
					alarmConfig.getSeverity().getValue());

			try {
				alarmService.createAlarm(alarmConfig.getType(), alarmConfig.getSeverity(), alarmText, null);
			} catch (Exception e) {
				LOGGER.error("Can't send alarm '{}'({}, {})", alarmText, alarmConfig.getType(),
						alarmConfig.getSeverity().getValue(), e);
			}
		}
	}
}
