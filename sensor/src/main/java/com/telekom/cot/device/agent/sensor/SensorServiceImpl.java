package com.telekom.cot.device.agent.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.SensorServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.sensor.configuration.SensorServiceConfiguration;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;

public class SensorServiceImpl extends AbstractAgentService implements SensorService {

	private static final long MILLISECONDS_PER_SECOND = 1000l;

	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SensorServiceImpl.class);

	@Inject
	private SensorServiceConfiguration configuration;

	@Inject
	private AgentServiceProvider serviceProvider;
	
    @Inject
	private PlatformService platformService;

	/** the list of measurements pushed by all registered SensorDeviceServices */
	private final List<SensorMeasurement> measurements = new ArrayList<>();

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

        AssertionUtil.assertNotNull(configuration, SensorServiceException.class, LOGGER, "no configuration given");
        AssertionUtil.assertNotNull(platformService, SensorServiceException.class, LOGGER, "no platform service given");

		// get fixed rate (in seconds) at which measurements are sent to the platform
		sendInterval = configuration.getSendInterval();

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
			} catch (Exception e) {
				throw new SensorServiceException("The SensorService thread has been interrupted", e);
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

	@Override
	public void addMeasurements(List<SensorMeasurement> sensorMeasurements) {
		synchronized (measurements) {
			this.measurements.addAll(sensorMeasurements);
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
			measurementsCopy = new ArrayList<>(measurements);
			measurementsCopy.addAll(measurementsNotSent);
			measurements.clear();
			measurementsNotSent = new ArrayList<>();
		}

		if (measurementsCopy.isEmpty()) {
			LOGGER.warn("No measurements to send");
			return;
		}

		try {
			LOGGER.debug("Send {} measurements", measurementsCopy.size());
			platformService.createMeasurements(measurementsCopy);
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
	 * starts all registered sensor device services.
	 * 
	 * @throws AbstractAgentException
	 */
	private void startSensorDeviceServices() throws AbstractAgentException {
		sensorServices = new ArrayList<>();

		List<SensorDeviceService> foundServices;
		try {
			foundServices = serviceProvider.getServices(SensorDeviceService.class);
		} catch (AgentServiceNotFoundException e) {
			LOGGER.warn("No sensor service found", e);
			foundServices = new ArrayList<>();
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
}
