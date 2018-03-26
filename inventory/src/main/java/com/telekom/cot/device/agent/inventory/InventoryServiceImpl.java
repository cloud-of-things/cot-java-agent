package com.telekom.cot.device.agent.inventory;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.InventoryServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.library.Fragment;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SupportedOperations;

public class InventoryServiceImpl extends AbstractAgentService implements InventoryService {

    /** the logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private static final int NOT_FOUND = 404;

    private PlatformService platformService;
    private InventoryServiceConfiguration configuration;
    private ManagedObjectFactory managedObjectFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws AbstractAgentException {
        LOGGER.info("start inventory service");
        platformService = getService(PlatformService.class);

        configuration = getConfigurationManager().getConfiguration(InventoryServiceConfiguration.class);
        managedObjectFactory = ManagedObjectFactory.getInstance(configuration, getService(SystemService.class));

        super.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeviceRegistered() throws AbstractAgentException {
    	boolean isRegistered = false;
    	try {
    		isRegistered = Objects.nonNull(platformService.getExternalId());
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
        ManagedObject createdManagedObject;
        try {
            ManagedObject managedObject = managedObjectFactory.create();
            createdManagedObject = platformService.createManagedObject(managedObject);
        } catch (PlatformServiceException exception) {
            throw new InventoryServiceException("could not create a managed object", exception);
        }
        // register device
        try {
            platformService.createExternalId(createdManagedObject.getId());
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
            ManagedObject managedObject = platformService.getManagedObject();
            String cotDeviceName = managedObject.getName();
            String cotDeviceId = managedObject.getId();
            ManagedObject updateManagedObject = managedObjectFactory.create(cotDeviceName);
            updateManagedObject.setId(cotDeviceId);
            platformService.updateManagedObject(updateManagedObject);
        } catch (PlatformServiceException exception) {
            throw new InventoryServiceException("could not update a managed object", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Fragment fragment) throws AbstractAgentException {
        if (SupportedOperations.class.isInstance(fragment)) {
            SupportedOperations supportedOperations = SupportedOperations.class.cast(fragment);
            LOGGER.info("update supported operations {}", Arrays.asList(supportedOperations.getOperations()));
            platformService.updateSupportedOperations(supportedOperations);
        } else {
            throw new InventoryServiceException("unsupported operation: can't update fragment " + fragment.getClass().getName());
        }
    }
}
