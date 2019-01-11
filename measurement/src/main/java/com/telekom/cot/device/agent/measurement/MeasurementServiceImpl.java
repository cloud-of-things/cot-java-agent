package com.telekom.cot.device.agent.measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.MeasurementServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.device.DeviceService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;

public class MeasurementServiceImpl extends AbstractAgentService implements MeasurementService {

	private static final long MILLISECONDS_PER_SECOND = 1000l;

	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementServiceImpl.class);

	@Inject
	private MeasurementServiceConfiguration configuration;

	@Inject
	private AgentServiceProvider serviceProvider;

	@Inject
	private PlatformService platformService;

	@Inject
	private DeviceService deviceService;

	/** the list of measurements pushed by all registered SensorDeviceServices */
	private final List<SensorMeasurement> measurements = new ArrayList<>();

	/** the running flag */
	private AtomicBoolean running = new AtomicBoolean(false);

	/** the worker thread */
	private Thread worker;

	/** the fixed rate at which measurements are sent to the platform */
	private int sendInterval;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		if (isStarted()) {
			throw new MeasurementServiceException("the sensor service was already started");
		}

		LOGGER.debug("start " + this.getClass().getSimpleName());

		AssertionUtil.assertNotNull(configuration, MeasurementServiceException.class, LOGGER, "no configuration given");
		AssertionUtil.assertNotNull(platformService, MeasurementServiceException.class, LOGGER,
				"no platform service given");

		// get fixed rate (in seconds) at which measurements are sent to the platform
		sendInterval = configuration.getSendInterval();

		// start the thread that sends the measurements to the platform
		running.set(true);
		worker = new Thread(() -> processMeasurements());
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

			// Wait until the worker thread finishes sending the current measurements
			try {
				worker.join();
			} catch (Exception e) {
				throw new MeasurementServiceException("The MeasurementService thread has been interrupted", e);
			}

			// Send measurements already collected by the SensorDeviceServices to the
			// platform
			sendMeasurements();

			super.stop();
		}
	}

	/**
	 * run method for the worker thread
	 */
	private void processMeasurements() {
		while (running.get()) {
			sleep();
			getMeasurements();
			sendMeasurements();
		}
	}

	/**
	 * gets all sensor measurements from the queue
	 */
	private void getMeasurements() {
		SensorMeasurement measurement;
		while ((measurement = deviceService.getQueueChannel().getItem()) != null) {
			measurements.add(measurement);
		}
	}

	/**
	 * sends the collected measurements to the platform
	 */
	private void sendMeasurements() {
		if (measurements.isEmpty()) {
			LOGGER.warn("No measurements to send");
			return;
		}

		try {
			LOGGER.debug("Send {} measurements", measurements.size());
			platformService.createMeasurements(measurements);
		} catch (AbstractAgentException exception) {
			LOGGER.error("Couldn't send measurements", exception);
			deviceService.getQueueChannel().add(measurements);
		}
		measurements.clear();
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
}
