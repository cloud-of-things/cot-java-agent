package com.telekom.cot.device.agent.operation;

import static com.telekom.cot.device.agent.service.AgentServiceShutdownHelper.shutdownServices;
import static com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus.EXECUTING;
import static com.telekom.cot.device.agent.operation.handler.OperationUtil.getSoftwareToUpdate;

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
import com.telekom.cot.device.agent.inventory.InventoryService;
import com.telekom.cot.device.agent.operation.handler.OperationsHandlerService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.Software;
import com.telekom.cot.device.agent.system.properties.SoftwareProperties;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationCollection;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SupportedOperations;

public class OperationServiceImpl extends AbstractAgentService implements OperationService {

    private static final int OPERATION_COLLECTION_SIZE = 10;
    private static final String C8Y_RESTART = "c8y_Restart";
    private static final String C8Y_SOFTWARELIST = "c8y_SoftwareList";
    /** the logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationServiceImpl.class);
    private List<OperationsHandlerService> operationHandlerServices;
    private OperationWorker operationWorker;
    private PlatformService platformService;
    private InventoryService inventoryService;
    private SystemService systemService;
    private OperationServiceConfiguration config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws AbstractAgentException {
        LOGGER.info("start the operation service");
        platformService = getService(PlatformService.class);
        inventoryService = getService(InventoryService.class);
        systemService = getService(SystemService.class);
        config = getConfigurationManager().getConfiguration(OperationServiceConfiguration.class);
        // set the status to SUCCESSFUL
        updatePreviousRestartedOperationsStatus();
        updatePreviousSoftwareUpdateOperationsStatus();
        // get all loaded operation handlers, get and verify their supported operations
        List<OperationsHandlerService> operationHandlers = getHandlerServices();
        operationHandlers = verifySupportedOperations(operationHandlers);
        // start all handler services and update supported operations
        operationHandlerServices = startHandlerServices(operationHandlers);
        if (!updateSupportedOperations()) {
            // can't update, stop all handlers
            if (!CollectionUtils.isEmpty(operationHandlerServices)) {
                shutdownServices(operationHandlerServices, config.getHandlersShutdownTimeout(), true);
            }
            operationHandlerServices.clear();
        }
        // create and start operation worker
        operationWorker = new OperationWorker(platformService, operationHandlerServices, config.getInterval(),
                        config.getResultSize());
        operationWorker.start();
        super.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws AbstractAgentException {
        // stop the worker
        if (Objects.nonNull(this.operationWorker)) this.operationWorker.stop();
        // stop the all operation handlers
        if (!CollectionUtils.isEmpty(operationHandlerServices)) {
            shutdownServices(operationHandlerServices, config.getHandlersShutdownTimeout(), true);
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
            List<OperationsHandlerService> loadedHandlerServices = getServices(OperationsHandlerService.class);
            LOGGER.debug("got {} operation handler services", loadedHandlerServices.size());
            return loadedHandlerServices;
        } catch (AgentServiceNotFoundException e) {
            LOGGER.warn("can't get any operation handler service", e);
            return new ArrayList<>();
        }
    }

    /**
     * verifies all supported operations of all given operation handlers (only one handler per operation allowed) and
     * returns a list of verified handlers
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
            LOGGER.warn("verification failed, operation handler '{}' supports no operations", handlerService
                            .getClass());
            return false;
        }
        // verify whether another handler already supports the operations
        for (String supportedOperation : supportedOperations) {
            if (alreadySupportedOperations.contains(supportedOperation)) {
                LOGGER.error("error supported operations {} of handler {}", supportedOperations, handlerService
                                .getClass().getName());
                throw new OperationServiceException("operation handler '" + handlerService.getClass().getName()
                                + "supports operation '" + supportedOperation
                                + "', which is already supported by another operation handler");
            }
            // current operation name is verified, add to already supported operations
            LOGGER.debug("added supported operations {} of handler {}", supportedOperations, handlerService.getClass()
                            .getName());
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
        List<String> supportedOperationsNames = operationHandlerServices.stream()
                        .map(ohs -> Arrays.asList(ohs.getSupportedOperations())).collect(Collectors.toList()).stream()
                        .flatMap(List::stream).collect(Collectors.toList());
        // update supported operations
        try {
            LOGGER.debug("update supported operations {}", supportedOperationsNames);
            SupportedOperations supportedOperations = new SupportedOperations(
                            supportedOperationsNames.toArray(new String[0]));
            inventoryService.update(supportedOperations);
        } catch (AgentServiceNotFoundException | InventoryServiceException e) {
            LOGGER.error("could not update supported operations", e);
            return false;
        }
        return true;
    }

    /**
     * try to update restart operations status by status EXECUTING
     * 
     * @throws AbstractAgentException
     */
    private void updatePreviousRestartedOperationsStatus() throws AbstractAgentException {
        LOGGER.info("try to update a restart operation status by status EXECUTING");
        OperationCollection operationCollection;
        do {
            operationCollection = platformService
                            .getOperationCollection(C8Y_RESTART, EXECUTING, OPERATION_COLLECTION_SIZE);
            // check size
            if (Objects.isNull(operationCollection.getOperations())
                            || operationCollection.getOperations().length == 0) {
                break;
            }
            // loop through the operations
            for (Operation operation : operationCollection.getOperations()) {
                LOGGER.debug("update operation {}", operation.getId());
                operation.setStatus(OperationStatus.SUCCESSFUL);
                platformService.updateOperation(operation);
            }
        } while (operationCollection.hasNext());
    }

    /**
     * try to update software update operations status by status EXECUTING
     * 
     * @throws AbstractAgentException
     */
    private void updatePreviousSoftwareUpdateOperationsStatus() throws AbstractAgentException {
        LOGGER.info("try to update a software update operation status by status EXECUTING");
        // get SoftwareProperties
        OperationCollection operationCollection;
        SoftwareProperties softwareProperties = systemService.getProperties(SoftwareProperties.class);
        List<Software> softwareList = softwareProperties.getSoftwareList();
        // check size
        boolean validSoftwareList = softwareList.size() == 1 && Objects.nonNull(softwareList.get(0));
        LOGGER.debug("only one software list entry is expected:  {}", validSoftwareList);
        do {
            // get software list operations
            operationCollection = platformService
                            .getOperationCollection(C8Y_SOFTWARELIST, EXECUTING, OPERATION_COLLECTION_SIZE);
            if (Objects.isNull(operationCollection.getOperations())
                            || operationCollection.getOperations().length == 0) {
                break;
            }
            // loop through the operations
            for (Operation operation : operationCollection.getOperations()) {
                OperationStatus operationStatus;
                if (validSoftwareList) {
                    // determine status
                    operationStatus = getOperationStatus(softwareList.get(0), getSoftwareToUpdate(operation));
                } else {
                    operationStatus = OperationStatus.FAILED;
                }
                LOGGER.debug("update operation {} with status {}", operation.getId(), operationStatus);
                operation.setStatus(operationStatus);
                platformService.updateOperation(operation);
            }
        } while (operationCollection.hasNext());
    }

    private OperationStatus getOperationStatus(com.telekom.cot.device.agent.system.properties.Software currentSoftware,
                    com.telekom.cot.device.agent.system.properties.Software operationSoftware) {
        // operation version and name
        String operationSoftwareVerion = operationSoftware.getVersion();
        String operationSoftwareName = operationSoftware.getName();
        // agent current version and name
        String currentSoftwareVerion = currentSoftware.getVersion();
        String currentSoftwareName = currentSoftware.getName();
        boolean b = operationSoftwareVerion.equals(currentSoftwareVerion);
        b = b && operationSoftwareName.equals(currentSoftwareName);
        return b ? OperationStatus.SUCCESSFUL : OperationStatus.FAILED;
    }
}
