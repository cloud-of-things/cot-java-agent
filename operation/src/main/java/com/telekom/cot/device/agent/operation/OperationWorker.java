package com.telekom.cot.device.agent.operation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.OperationServiceException;
import com.telekom.cot.device.agent.operation.handler.OperationsHandlerService;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.objects.OperationStatus;

/**
 * Handles the operation requests and executions.
 * 
 */
public class OperationWorker {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(OperationWorker.class);

	/** The seconds factor. */
	private static final long MILLISECONDS_PER_SECOND = 1000l;

	/** The running flag. */
	private AtomicBoolean running = new AtomicBoolean(false);
	/** The worker thread. */
	private Thread worker;

	/** The CoT platform. */
	private PlatformService platformService;
	/** All operation handler implementations. */
	private Map<String, OperationsHandlerService> operationHandlerServicesMap;
	/** The sleep interval. */
	private int interval;

	private boolean started = false;
	private boolean stopped = false;

	/** The protected constructor. */
	protected OperationWorker(PlatformService platformService, List<OperationsHandlerService> operationHandlerServices,
			int sleepInterval) {
		LOGGER.info("create operation worker");
		this.platformService = platformService;
		this.operationHandlerServicesMap = toMap(operationHandlerServices);
		this.interval = sleepInterval;
	}

	/**
	 * Start the worker thread.
	 * 
	 */
	public void start() throws AbstractAgentException {
		LOGGER.info("start operation worker");
		if (isStarted()) {
			throw new OperationServiceException("the worker was already started");
		}
		running.set(true);
		worker = new Thread(() -> getOperationsAndHandle());
		worker.start();
		started = true;
		LOGGER.info("operation worker is started");
	}

	/**
	 * Is the worker thread running.
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Stop the worker thread.
	 * @throws OperationServiceException 
	 */
	public void stop() throws OperationServiceException {
		if (isStarted()) {
			stopped = true;
			
			// Set running to false so that the worker thread execution will be stopped
			running.set(false);
			
			// Wait until the worker thread finishes handling its current pending operations
			try {
				worker.join();
			} catch (Exception e) {
				throw new OperationServiceException("The worker thread has been interrupted");
			}
			LOGGER.info("operation worker is stopped");
		}
	}

	/**
	 * Is the worker thread interrupted.
	 * 
	 * @return
	 */
	public boolean isStopped() {
		return stopped;
	}

	/**
	 * run method for the worker thread
	 */
	private void getOperationsAndHandle() {
		LOGGER.info("running operation worker");
		while (running.get()) {
		    // get and check next pending operation, sleep when no pending operation found
		    Operation operation = getNextPendingOperation();
		    if (Objects.isNull(operation)) {
		        LOGGER.debug("no pending operation found");
		        sleep();
		        continue;
		    }

		    // handle operation
            try {
                OperationStatus status = handleOperation(operation);
                LOGGER.debug("handled operation, status is {}", status);
	            
    	        // update new status
    	        updateOperationStatus(operation, status);
			} catch (Exception exception) {
				LOGGER.error("can't handle operations", exception);
			}
		}

		LOGGER.info("running is finished");
	}

	
	/**
	 * Hold the worker thread.
	 */
	private void sleep() {
		try {
			Thread.sleep(interval * MILLISECONDS_PER_SECOND);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.info("Thread was interrupted, Failed to complete operation");
		}
	}

	/**
	 * handle the given operation
	 */
	private OperationStatus handleOperation(Operation operation) throws AbstractAgentException {
		LOGGER.debug("handle operation");

        // get and check specific operation handler
        OperationsHandlerService handler = findHandler(operation);
        if (Objects.isNull(handler)) {
            LOGGER.error("no handler found for operation {}", operation.getProperties());
            return OperationStatus.FAILED;
        }

        // set status EXECUTING
        updateOperationStatus(operation, OperationStatus.EXECUTING);
                
        // execute operation and return status
        LOGGER.debug("execute operation {}", operation.getProperties());
        try {
            return handler.execute(operation);
        } catch (Exception e) {
            LOGGER.error("can't execute operation", e);
            return OperationStatus.FAILED;
        }
	}

    /**
     * get the next pending operations from the platform
     * 
     * @param size maximum number of operations to get 
     * @return a collection of pending operations
     */
	private Operation getNextPendingOperation() {
        LOGGER.debug("get next pending operation");
        
        try {
            return platformService.getNextPendingOperation(); 
        } catch(AbstractAgentException e) {
            LOGGER.info("can't get pending operation from platform", e);
            return null;
        }
    }
	
	/**
	 * Find a suitable handler of the operation.
	 * 
	 * @param operation
	 * @return the service implementation or null
	 */
	private OperationsHandlerService findHandler(Operation operation) {
		LOGGER.info("find operation handler {}", operation.getProperties());

		for (Entry<String, OperationsHandlerService> mapEntry : operationHandlerServicesMap.entrySet()) {
			if (Objects.nonNull(operation.getProperty(mapEntry.getKey()))) {
				LOGGER.debug("found operation handler {}", mapEntry.getValue().getClass());
				return mapEntry.getValue();
			}
		}

		LOGGER.debug("did not found operation handler {}", operation.getProperties());
		return null;
	}

	/**
	 * updates the status of the given operation at platform
	 */
	private void updateOperationStatus(Operation operation, OperationStatus status) throws AbstractAgentException {
	    String id = operation.getId();
		LOGGER.info("update status of operation '{}' to '{}'", id, status);
		operation.setStatus(status);
		platformService.updateOperationStatus(id, status);
	}

	/**
	 * maps to each supported operation of the given operation handler services the corresponding handler
	 */
	private Map<String, OperationsHandlerService> toMap(List<OperationsHandlerService> handlerServices) {
		Map<String, OperationsHandlerService> handlerServicesMap = new ConcurrentHashMap<>();
		for (OperationsHandlerService handlerService : handlerServices) {
			for (String operation : handlerService.getSupportedOperations()) {
				handlerServicesMap.put(operation, handlerService);
			}
		}

		return handlerServicesMap;
	}
}
