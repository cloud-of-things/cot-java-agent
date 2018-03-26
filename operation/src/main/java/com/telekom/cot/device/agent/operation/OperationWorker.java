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
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationCollection;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;

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
	/** The request size of operations. */
	private int operationSize;

	private boolean started = false;
	private boolean stopped = false;

	/** The protected constructor. */
	protected OperationWorker(PlatformService platformService, List<OperationsHandlerService> operationHandlerServices,
			int sleepInterval, int operationSize) {
		LOGGER.info("create operation worker");
		this.platformService = platformService;
		this.operationHandlerServicesMap = toMap(operationHandlerServices);
		this.interval = sleepInterval;
		this.operationSize = operationSize;
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
			} catch (InterruptedException e) {
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
		    // get and check pending operations, sleep when no pending operations
		    OperationCollection pendingOperations = getPendingOperations(operationSize);
		    if (Objects.isNull(pendingOperations)) {
		        LOGGER.debug("no pending operations found");
		        sleep();
		        continue;
		    }

		    try {
		        handleOperations(pendingOperations);
			} catch (Exception exception) {
				LOGGER.error("Can't handle operations", exception);
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
	 * Handle the operations.
	 * 
	 * @param operationCollection
	 */
	private void handleOperations(OperationCollection operationCollection) throws AbstractAgentException {
		LOGGER.info("handle operations");

		for (Operation operation : operationCollection.getOperations()) {
			OperationsHandlerService handler = findHandler(operation);
			handleOperation(handler, operation);
			
			// update new status
			updateOperation(operation);
			// check the thread state
			// !!! the interrupted status of the thread is cleared
			if (!running.get() || Thread.interrupted()) {
				break;
			}
		}
	}

	/**
	 * get the pending operations from the platform
	 * 
	 * @param size maximum number of operations to get 
	 * @return a collection of pending operations
	 */
	private OperationCollection getPendingOperations(int size) {
		LOGGER.info("get pending operations");
		
		// get pending operations
		OperationCollection pendingOperations;
		try {
		    pendingOperations = platformService.getOperationCollection(OperationStatus.PENDING, size); 
		} catch(AbstractAgentException e) {
		    LOGGER.info("can't get pending operations from platform", e);
		    return null;
		}

        if (Objects.isNull(pendingOperations) || Objects.isNull(pendingOperations.getOperations())
                        || pendingOperations.getOperations().length == 0) {
            return null;
        }
		
		return pendingOperations;
	}

	/**
	 * Find a suitable handler of the operation.
	 * 
	 * @param operation
	 * @return the service implementation or null
	 */
	private OperationsHandlerService findHandler(Operation operation) {
		LOGGER.info("find operation handler {}", operation.getAttributes());

		for (Entry<String, OperationsHandlerService> mapEntry : operationHandlerServicesMap.entrySet()) {
			if (Objects.nonNull(operation.get(mapEntry.getKey()))) {
				LOGGER.debug("found operation handler {}", mapEntry.getValue().getClass());
				return mapEntry.getValue();
			}
		}

		LOGGER.debug("did not found operation handler {}", operation.getAttributes());
		return null;
	}

	/**
	 * handles the given operation by the given operation handler
	 */
	private void handleOperation(OperationsHandlerService handler, Operation operation) throws AbstractAgentException {
        if (Objects.isNull(handler)) {
            // error case
            LOGGER.error("no handler found for operation {}", operation.getAttributes());
            operation.setStatus(OperationStatus.FAILED);
            return;
        }
         
        // set status EXECUTING
        operation.setStatus(OperationStatus.EXECUTING);
        updateOperation(operation);
                
        // execute operation and set status
        LOGGER.debug("execute operation {}", operation.getAttributes());
        try {
            OperationStatus operationStatus = handler.execute(operation);
            LOGGER.debug("the operation status is {}", operationStatus);
            operation.setStatus(operationStatus);
        } catch (Exception e) {
            // error case
            LOGGER.error("can't execute operation", e);
            operation.setStatus(OperationStatus.FAILED);
        }
	}
	
	/**
	 * updates the given operation at platform
	 */
	private void updateOperation(Operation operation) throws AbstractAgentException {
		LOGGER.info("update operation {}", operation.getAttributes());
		platformService.updateOperation(operation);
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
