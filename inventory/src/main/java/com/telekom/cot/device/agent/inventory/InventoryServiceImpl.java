package com.telekom.cot.device.agent.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.InventoryServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.system.SystemService;

public class InventoryServiceImpl extends AbstractAgentService implements InventoryService {

	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

	private static final int NOT_FOUND = 404;

	@Inject
	private InventoryServiceConfiguration configuration;
	@Inject
	private PlatformService platformService;
    @Inject
    private SystemService systemService;

    private AgentManagedObjectFactory managedObjectFactory;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		LOGGER.info("start inventory service");

		AssertionUtil.assertNotNull(configuration, InventoryServiceException.class, LOGGER, "no configuration given");
        AssertionUtil.assertNotNull(platformService, InventoryServiceException.class, LOGGER, "no platform service given");
        AssertionUtil.assertNotNull(systemService, InventoryServiceException.class, LOGGER, "no system service given");

		managedObjectFactory = AgentManagedObjectFactory.getInstance(configuration, systemService);

		super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDeviceRegistered() throws AbstractAgentException {
		boolean isRegistered = false;
		try {
			isRegistered = platformService.isExternalIdAvailable();
		} catch (PlatformServiceException exception) {
			isRegistered = false;
			if (NOT_FOUND != exception.getHttpStatus()) {
				LOGGER.error("could not check if the device is already registered", exception);
				throw new InventoryServiceException("could not check if the device is already registered", exception);
			}
		}

		String logMessage = isRegistered ? "device is registered" : "device is not registered";
		LOGGER.info(logMessage);
		return isRegistered;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createAndRegisterDevice() throws AbstractAgentException {
		LOGGER.info("create and register device {}", configuration.getDeviceName());
		// Create the device in the inventory as a managed object
		String createdManagedObjectId;
		try {
			AgentManagedObject agentManagedObject = managedObjectFactory.create();
			createdManagedObjectId = platformService.createAgentManagedObject(agentManagedObject);
		} catch (PlatformServiceException exception) {
			throw new InventoryServiceException("could not create a managed object", exception);
		}
		// register device
		try {
			platformService.createExternalId(createdManagedObjectId);
		} catch (PlatformServiceException exception) {
			throw new InventoryServiceException("could not register a device", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateDevice() throws AbstractAgentException {
		LOGGER.info("update device {}", configuration.getDeviceName());
		try {
			AgentManagedObject agentManagedObject = platformService.getAgentManagedObject();
			String cotDeviceName = agentManagedObject.getName();
			String cotDeviceId = agentManagedObject.getId();
			AgentManagedObject updateAgentManagedObject = managedObjectFactory.create(cotDeviceName);
			updateAgentManagedObject.setId(cotDeviceId);
			platformService.updateAgentManagedObject(updateAgentManagedObject);
		} catch (PlatformServiceException exception) {
			throw new InventoryServiceException("could not update a managed object", exception);
		}
	}
}
