package com.telekom.cot.device.agent.sensor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.SensorServiceException;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.sensor.configuration.SensorServiceConfiguration;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.measurement.Measurement;
import com.telekom.m2m.cot.restsdk.measurement.MeasurementReading;

public class SensorServiceImpl extends AbstractAgentService implements SensorService, AlarmService, EventService {

	private static final long MILLISECONDS_PER_SECOND = 1000l;

	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SensorServiceImpl.class);

	private SensorServiceConfiguration configuration;
	private PlatformService platformService;
	private ManagedObject managedObject;

	/** the list of measurements pushed by all registered SensorDeviceServices */
	private List<SensorMeasurement> measurements = new ArrayList<>();

	/** the list of measurements that could not be sent */
	private List<SensorMeasurement> measurementsNotSent = new ArrayList<>();

	/** the running flag */
	private AtomicBoolean running = new AtomicBoolean(false);

	/** the worker thread */
	private Thread worker;

	/** the list of started sensor device services */
	private List<SensorDeviceService> sensorServices;

	/** the fixed rate at which measurements are sent to the platform */
	private int sendInterval;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		if (isStarted()) {
			throw new SensorServiceException("the sensor service was already started");
		}

		LOGGER.debug("start " + this.getClass().getSimpleName());

		// get configuration and instance of PlatformService
		configuration = getConfigurationManager().getConfiguration(SensorServiceConfiguration.class);
		platformService = getService(PlatformService.class);

		// get fixed rate (in seconds) at which measurements are sent to the platform
		sendInterval = configuration.getSendInterval();
		managedObject = platformService.getManagedObject();

		// start the thread that sends the measurements to the platform
		running.set(true);
		worker = new Thread(() -> processMeasurements());
		worker.start();

		// start all sensor device services
		startSensorDeviceServices();

		super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws AbstractAgentException {
		if (isStarted()) {
			LOGGER.debug("stop " + this.getClass().getSimpleName());

			stopSensorDeviceServices();

			// Set running to false so that the worker thread execution will be stopped
			running.set(false);

			// Wait until the worker thread finishes sending the current measurements
			try {
				worker.join();
			} catch (InterruptedException e) {
				throw new SensorServiceException("The SensorService thread has been interrupted");
			}

			// Send measurements already collected by the SensorDeviceServices
			sendMeasurements();

			super.stop();
		}
	}

	@Override
	public void addMeasurement(SensorMeasurement sensorMeasurement) {
		synchronized (measurements) {
			measurements.add(sensorMeasurement);
		}
	}

	/**
	 * run method for the worker thread
	 */
	private void processMeasurements() {
		while (running.get()) {
			sleep();
			sendMeasurements();
		}
	}

	/**
	 * sends the collected measurements to the platform
	 */
	private void sendMeasurements() {

		List<SensorMeasurement> measurementsCopy;
		synchronized (measurements) {
			measurementsCopy = new ArrayList<SensorMeasurement>(measurements);
			measurementsCopy.addAll(measurementsNotSent);
			measurements = new ArrayList<>();
			measurementsNotSent = new ArrayList<>();
		}

		if (measurementsCopy.isEmpty()) {
			LOGGER.warn("No measurements to send");
			return;
		}

		List<Measurement> platformMeasurements = measurementsCopy.stream().map(m -> {
			Measurement measurement = new Measurement();
			measurement.setTime(m.getTime());
			measurement.setType(m.getType());
			measurement.set(m.getType(), new SensorMeasurementReading(m.getValue(), m.getUnit()));
			measurement.setSource(managedObject);
			return measurement;
		}).collect(Collectors.toList());

		try {
			LOGGER.debug("Send {} measurements", platformMeasurements.size());
			platformService.createMeasurements(platformMeasurements);
		} catch (AbstractAgentException exception) {
			LOGGER.error("Couldn't send measurements", exception);
			measurementsNotSent.addAll(measurementsCopy);
		}
	}

	/**
	 * sleeps for a given interval
	 */
	private void sleep() {
		try {
			Thread.sleep(sendInterval * MILLISECONDS_PER_SECOND);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.info("Thread was interrupted, failed to send measurements");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createEvent(String type, String text, Map<String, Object> attributes, Object object)
			throws AbstractAgentException {
		LOGGER.debug("create and send event of type '{}' with text '{}'", type, text);
		platformService.createEvent(new Date(), type, text, attributes, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createAlarm(String type, AlarmSeverity severity, String text, String status,
			Map<String, Object> attributes, Object object) throws AbstractAgentException {
		LOGGER.debug("create and send alarm of type {} with text {} and severity {}", type, text, severity.getValue());
		platformService.createAlarm(new Date(), type, severity, text, status, attributes, object);
	}

	/**
	 * starts all registered sensor device services.
	 * 
	 * @throws AbstractAgentException
	 */
	private void startSensorDeviceServices() throws AbstractAgentException {
		sensorServices = new ArrayList<>();

		List<SensorDeviceService> foundServices;
		try {
			foundServices = getServices(SensorDeviceService.class);
		} catch (AgentServiceNotFoundException e) {
			LOGGER.warn("No sensor service found", e);
			foundServices = new ArrayList<SensorDeviceService>();
		}

		for (SensorDeviceService service : foundServices) {
			try {
				service.start();
				sensorServices.add(service);
				LOGGER.info("started sensor device service '{}'", service);
			} catch (AbstractAgentException e) {
				LOGGER.error("sensor service '{}' can't be started", service, e);
			}
		}
	}

	/**
	 * stops all sensor device services that were started
	 * 
	 * @throws AbstractAgentException
	 */
	private void stopSensorDeviceServices() throws AbstractAgentException {
		if (Objects.isNull(sensorServices)) {
			return;
		}
		for (AgentService sensorService : sensorServices) {
			sensorService.stop();
		}
	}

	/**
	 * nested class for measurement readings
	 */
	private class SensorMeasurementReading {
		@SuppressWarnings("unused")
		MeasurementReading sensorValue;

		SensorMeasurementReading(float value, String unit) {
			sensorValue = new MeasurementReading(value, unit);
		}
	}
}
