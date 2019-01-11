package com.telekom.cot.device.agent.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.DeviceServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.device.sensordevice.SensorDeviceService;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.AgentService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.channel.QueueChannel;
import com.telekom.cot.device.agent.service.channel.QueueChannelImpl;

public class DeviceServiceImpl extends AbstractAgentService implements DeviceService {

	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceServiceImpl.class);

	@Inject
	private AgentServiceProvider serviceProvider;

	@Inject
	private DeviceServiceConfiguration configuration;

	/** the list of all registered and started SensorDeviceServices */
	private List<SensorDeviceService> sensorDeviceServices;

	/** the queue holding the sensor measurements */
	private QueueChannel<SensorMeasurement> queueChannel;

	@Override
	public void start() throws AbstractAgentException {
		if (isStarted()) {
			throw new DeviceServiceException("the device service was already started");
		}

		LOGGER.info("start device service");

		AssertionUtil.assertNotNull(configuration, DeviceServiceException.class, LOGGER, "no configuration given");

		queueChannel = new QueueChannelImpl<>();

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
			LOGGER.debug("stop device service");

			stopSensorDeviceServices();

			super.stop();
		}
	}

	/**
	 * starts all registered sensor device services.
	 * 
	 * @throws AbstractAgentException
	 */
	private void startSensorDeviceServices() throws AbstractAgentException {
		sensorDeviceServices = new ArrayList<>();

		List<SensorDeviceService> foundServices;
		try {
			foundServices = serviceProvider.getServices(SensorDeviceService.class);
		} catch (AgentServiceNotFoundException e) {
			LOGGER.warn("No sensor device service found", e);
			foundServices = new ArrayList<>();
		}

		for (SensorDeviceService service : foundServices) {
			try {
				service.start();
				sensorDeviceServices.add(service);
				LOGGER.info("started sensor device service '{}'", service);
			} catch (AbstractAgentException e) {
				LOGGER.error("sensor device service '{}' cannot be started", service, e);
			}
		}
	}

	/**
	 * stops all sensor device services that were started
	 * 
	 * @throws AbstractAgentException
	 */
	private void stopSensorDeviceServices() throws AbstractAgentException {
		if (Objects.isNull(sensorDeviceServices)) {
			return;
		}
		for (AgentService sensorDeviceService : sensorDeviceServices) {
			sensorDeviceService.stop();
		}
	}

	@Override
	public QueueChannel<SensorMeasurement> getQueueChannel() {
		return queueChannel;
	}
}
