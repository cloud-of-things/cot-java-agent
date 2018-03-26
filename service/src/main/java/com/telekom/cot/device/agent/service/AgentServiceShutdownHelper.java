package com.telekom.cot.device.agent.service;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentShutdownException;

public final class AgentServiceShutdownHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentServiceShutdownHelper.class);
	
	private AgentServiceProvider serviceProvider;
	
	public AgentServiceShutdownHelper(AgentServiceProvider serviceProvider) throws AbstractAgentException {
		assertNotNull(serviceProvider, AgentShutdownException.class, LOGGER, "no agent service provider given");
		this.serviceProvider = serviceProvider;
	}
	
	/**
	 * get a single agent service instance of the given type and shut it down
	 * @param serviceType type of the agent service to stop
	 * @param timeout time in milliseconds to wait for stopping services
	 * @param terminate whether to terminate services if a timeout occurs
	 * @throws AbstractAgentException if no ore more then one service of given type found or shutdown can't be terminated
	 */
	public <T extends AgentService> void shutdownService(Class<T> serviceType, long timeout, boolean terminate) throws AbstractAgentException {
		// get a single service of given type
		List<T> services = new ArrayList<>();
		try {
			T service = serviceProvider.getService(serviceType);
			services.add(service);
		} catch (AbstractAgentException e) {
			throw createExceptionAndLog(AgentShutdownException.class, LOGGER, "can't get a service of given type", e);
		}
		
		shutdownServices(services, timeout, terminate);
	}
	
	/**
	 * shutdown all agent services in the given list
	 * @param services list of agent services to stop
	 * @param timeout time in milliseconds to wait for stopping services
	 * @param terminate whether to terminate services if a timeout occurs
	 * @throws AbstractAgentException if list is {@code null} or empty or shutdown can't be terminated
	 */
	public static <T extends AgentService> void shutdownServices(List<T> services, long timeout, boolean terminate) throws AbstractAgentException {
		// check list of services
		if(CollectionUtils.isEmpty(services)) {
			throw createExceptionAndLog(AgentShutdownException.class, LOGGER, "no services to stop");
		}
		
		// check timeout
		if (timeout <= 0) {
			throw createExceptionAndLog(AgentShutdownException.class, LOGGER, "invalid timeout, must be positive (> 0)");
		}

		// run stop() method on each service in a own thread, shutdown executor service and await termination of threads
		ExecutorService executorService = executeStopServices(services);
		shutdownAndAwaitExecutorService(executorService, timeout, terminate);
	}

	/**
	 * get all agent services of the given type and shut them down
	 * @param serviceType type of the agent service to stop
	 * @param timeout time in milliseconds to wait for stopping services
	 * @param terminate whether to terminate services if a timeout occurs
	 * @throws AbstractAgentException if no services of given type found or shutdown can't be terminated
	 */
	public <T extends AgentService> void shutdownServices(Class<T> serviceType, long timeout, boolean terminate) throws AbstractAgentException {
		// get services of given type and stop them
		List<T> services;
		try {
			services = serviceProvider.getServices(serviceType);
		} catch (AbstractAgentException e) {
			throw createExceptionAndLog(AgentShutdownException.class, LOGGER, "can't get services of given type", e);
		}

		shutdownServices(services, timeout, terminate);
	}
	
	/**
	 * creates a new thread pool and executes AgentService.stop() for each service in a own thread
	 */
	private static <T extends AgentService> ExecutorService executeStopServices(List<T> services) {
		// run stop() method on each service in a own thread
		ExecutorService executorService = Executors.newFixedThreadPool(services.size());
		for(AgentService service : services) {
			executorService.execute(() -> {
				try {
					service.stop();
				} catch (AbstractAgentException e) {
					LOGGER.error("stopping service {} failed, {}", service, e);
				}
			}); 
		}
		
		return executorService;
	}
	
	/**
	 * shutdown the executor service and await it's termination (termination of threads)
	 */
	private static void shutdownAndAwaitExecutorService(ExecutorService executorService, long timeout, boolean terminate) throws AbstractAgentException {
		// shutdown executor service and await termination of stop-threads
		executorService.shutdown();
		try {
			executorService.awaitTermination(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw createExceptionAndLog(AgentShutdownException.class, LOGGER, "awaiting termination of stopping services has been interrupted", e);
		}

		// terminate hard if termination timed out
		if(!executorService.isTerminated() && terminate) {
			LOGGER.info("terminate stopping services after timeout");
			executorService.shutdownNow();
		}
		
		// check whether terminated stop thread
		if (!executorService.isTerminated()) {
			throw createExceptionAndLog(AgentShutdownException.class, LOGGER, "can't terminate stopping services");
		}
	}
}
