package com.telekom.cot.device.agent.service;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.service.event.AgentContext;

public abstract class AbstractAgentService implements AgentService {

	private static final String ERROR_NO_AGENT_CONTEXT = "no agent context given";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAgentService.class);

	private AgentContext agentContext;
    private boolean started = false;

    /**
     * Get the agent context.
     * 
     * @return the agent context
     */
    protected AgentContext getAgentContext() {
        return agentContext;
    }
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(AgentContext agentContext) throws AbstractAgentException {
		LOGGER.debug("init service");

		// check parameters
		assertNotNull(agentContext, ERROR_NO_AGENT_CONTEXT);
		this.agentContext = agentContext;
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
    public void start() throws AbstractAgentException {
        started = true;
    }
    
	/**
	 * {@inheritDoc}
	 */
    @Override
    public void stop() throws AbstractAgentException {
        started = false;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public boolean isStarted() {
        return started;
    }

	@SuppressWarnings("serial")
	private void assertNotNull(Object object, String errorMessage) throws AbstractAgentException {
		if(Objects.isNull(object)) {
			LOGGER.error(errorMessage);
			throw new AbstractAgentException(errorMessage) {};
		}
	}
}
