package com.telekom.cot.device.agent.operation;

import static com.telekom.cot.device.agent.operation.handler.OperationUtil.getSoftwareToUpdate;
import static com.telekom.cot.device.agent.platform.objects.OperationStatus.EXECUTING;
import static com.telekom.cot.device.agent.service.AgentServiceShutdownHelper.shutdownServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.InventoryServiceException;
import com.telekom.cot.device.agent.common.exc.OperationServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.operation.handler.OperationsHandlerService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.Software;
import com.telekom.cot.device.agent.system.properties.SoftwareProperties;

public class OperationServiceImpl extends AbstractAgentService implements OperationService {

	private static final String C8Y_RESTART = "c8y_Restart";
	private static final String C8Y_SOFTWARELIST = "c8y_SoftwareList";
	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(OperationServiceImpl.class);
	private List<OperationsHandlerService> operationHandlerServices;
	private OperationWorker operationWorker;
	@Inject
	private AgentServiceProvider serviceProvider;
    @Inject
	private PlatformService platformService;
    @Inject
	private SystemService systemService;
	@Inject
	private OperationServiceConfiguration configuration;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		LOGGER.info("start the operation service");

		AssertionUtil.assertNotNull(configuration, OperationServiceException.class, LOGGER, "no configuration given");
        AssertionUtil.assertNotNull(platformService, OperationServiceException.class, LOGGER, "no platform service given");
        AssertionUtil.assertNotNull(systemService, OperationServiceException.class, LOGGER, "no system service given");

		// set the status to SUCCESSFUL
		updateRestartOperations();
		updateSoftwareUpdateOperations();
		// get all loaded operation handlers, get and verify their supported operations
		List<OperationsHandlerService> operationHandlers = getHandlerServices();
		operationHandlers = verifySupportedOperations(operationHandlers);
		// start all handler services and update supported operations
		operationHandlerServices = startHandlerServices(operationHandlers);
		if (!updateSupportedOperations()) {
			// can't update, stop all handlers
			if (!CollectionUtils.isEmpty(operationHandlerServices)) {
				shutdownServices(operationHandlerServices, configuration.getHandlersShutdownTimeout(), true);
			}
			operationHandlerServices.clear();
		}
		// create and start operation worker
		operationWorker = new OperationWorker(platformService, operationHandlerServices, configuration.getInterval());
		operationWorker.start();
		super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() throws AbstractAgentException {
		// stop the worker
		if (Objects.nonNull(this.operationWorker))
			this.operationWorker.stop();
		// stop the all operation handlers
		if (!CollectionUtils.isEmpty(operationHandlerServices)) {
			shutdownServices(operationHandlerServices, configuration.getHandlersShutdownTimeout(), true);
		}
		super.stop();
	}

	/**
	 * gets a list of all loaded operation handlers
	 * 
	 * @return a list of all loaded operation handlers, maybe empty
	 * @throws AbstractAgentException
	 */
	private List<OperationsHandlerService> getHandlerServices() throws AbstractAgentException {
		LOGGER.info("get all loaded operation handler services");
		try {
			List<OperationsHandlerService> loadedHandlerServices = serviceProvider.getServices(OperationsHandlerService.class);
			LOGGER.debug("got {} operation handler services", loadedHandlerServices.size());
			return loadedHandlerServices;
		} catch (AgentServiceNotFoundException e) {
			LOGGER.warn("can't get any operation handler service", e);
			return new ArrayList<>();
		}
	}

	/**
	 * verifies all supported operations of all given operation handlers (only one
	 * handler per operation allowed) and returns a list of verified handlers
	 * 
	 * @throws OperationServiceException
	 */
	private List<OperationsHandlerService> verifySupportedOperations(List<OperationsHandlerService> handlerServices)
			throws OperationServiceException {
		// create a list for all successfully verified handlers
		List<OperationsHandlerService> verifiedOperationHandlers = new ArrayList<>();
		// get and verify all supported operations of all handlers
		Set<String> alreadySupportedOperations = new HashSet<>();
		for (OperationsHandlerService handlerService : handlerServices) {
			// verify handler and add to list if successful
			if (verifyHandlerService(handlerService, alreadySupportedOperations)) {
				verifiedOperationHandlers.add(handlerService);
			}
		}
		return verifiedOperationHandlers;
	}

	/**
	 * verifies the supported operations of the given operation handler
	 * 
	 * @throws OperationServiceException
	 */
	private boolean verifyHandlerService(OperationsHandlerService handlerService,
			Set<String> alreadySupportedOperations) throws OperationServiceException {
		// get and check supported operations
		String[] supportedOperations = handlerService.getSupportedOperations();
		if (supportedOperations == null || supportedOperations.length == 0) {
			LOGGER.warn("verification failed, operation handler '{}' supports no operations",
					handlerService.getClass());
			return false;
		}
		// verify whether another handler already supports the operations
		for (String supportedOperation : supportedOperations) {
			if (alreadySupportedOperations.contains(supportedOperation)) {
				LOGGER.error("error supported operations {} of handler {}", supportedOperations,
						handlerService.getClass().getName());
				throw new OperationServiceException(
						"operation handler '" + handlerService.getClass().getName() + "supports operation '"
								+ supportedOperation + "', which is already supported by another operation handler");
			}
			// current operation name is verified, add to already supported operations
			LOGGER.debug("added supported operations {} of handler {}", supportedOperations,
					handlerService.getClass().getName());
			alreadySupportedOperations.add(supportedOperation);
		}
		return true;
	}

	/**
	 * starts all given operation handler implementations
	 * 
	 * @return a list of all successfully started operation handlers
	 */
	private List<OperationsHandlerService> startHandlerServices(List<OperationsHandlerService> handlerServices) {
		List<OperationsHandlerService> startedHandlerServices = new ArrayList<>();
		// try to start each handler
		for (OperationsHandlerService handlerService : handlerServices) {
			try {
				handlerService.start();
				startedHandlerServices.add(handlerService);
			} catch (Exception e) {
				LOGGER.error("can't start the operation handler " + handlerService.getClass(), e);
			}
		}
		return startedHandlerServices;
	}

	/**
	 * update the supported operations by externalId at CoT platform
	 * 
	 * @return true in case of a successful update
	 * 
	 * @throws AbstractAgentException
	 */
	private boolean updateSupportedOperations() throws AbstractAgentException {
		LOGGER.info("update supported operations");
		// get all supported operations from all currently started handlers
		List<String> supportedOperationNames = operationHandlerServices.stream()
				.map(ohs -> Arrays.asList(ohs.getSupportedOperations())).collect(Collectors.toList()).stream()
				.flatMap(List::stream).collect(Collectors.toList());
		// update supported operations
		try {
			LOGGER.debug("update supported operations {}", supportedOperationNames);
			platformService.updateSupportedOperations(supportedOperationNames);
		} catch (AgentServiceNotFoundException | InventoryServiceException e) {
			LOGGER.error("could not update supported operations", e);
			return false;
		}
		return true;
	}

	/**
	 * update restart operations with status EXECUTING to status SUCCESSFUL
	 */
	private void updateRestartOperations() throws AbstractAgentException {
		LOGGER.info("update restart operations with status EXECUTING to status SUCCESSFUL");
		List<Operation> restartOperations = platformService.getOperations(C8Y_RESTART, EXECUTING);
		for (Operation operation : restartOperations) {
		    String operationId = operation.getId();
			LOGGER.debug("update status of restart operation {}", operationId);
			platformService.updateOperationStatus(operationId, OperationStatus.SUCCESSFUL);
		}
	}

	/**
     * update software update operations with status EXECUTING to status SUCCESSFUL or FAILED
	 */
	private void updateSoftwareUpdateOperations() throws AbstractAgentException {
        LOGGER.info("update software update operations with status EXECUTING to status SUCCESSFUL or FAILED");
        
        // get current software info
        Software currentSoftware = getCurrentSoftware();
        
        // get software update operations and update status
        List<Operation> softwareUpdateOperations = platformService.getOperations(C8Y_SOFTWARELIST, EXECUTING);
		for (Operation operation : softwareUpdateOperations) {
		    String id = operation.getId();
		    
	        // get operation status depending on current software and requested software
	        OperationStatus status = currentSoftware.equals(getSoftwareToUpdate(operation), false)
	                        ? OperationStatus.SUCCESSFUL : OperationStatus.FAILED;
		    
			LOGGER.debug("update software update operation {} to status {}", id, status);
			platformService.updateOperationStatus(id, status);
		}
	}

	/**
	 * get the current software info (first software list entry from system service)
	 */
	private Software getCurrentSoftware() throws AbstractAgentException {
        // get software list and check size (must be 1)
        List<Software> softwareList = systemService.getProperties(SoftwareProperties.class).getSoftwareList();
        if (softwareList.size() != 1) {
            throw logErrorAndCreateException("can't get current software, not exactly one software list entry found");
        }            

        // check first entry
        if (Objects.isNull(softwareList.get(0))) {
            throw logErrorAndCreateException("can't get current software, first list entry is null");
        }

        return softwareList.get(0);
	}

	/**
	 * logs the given error message and returns a new exception
	 */
	private AbstractAgentException logErrorAndCreateException(String message) {
        LOGGER.error(message);
        return new OperationServiceException(message);
	}
}
