package com.telekom.cot.device.agent.service;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceProxyException;
import com.telekom.cot.device.agent.common.proxy.ProxyUtil;
import com.telekom.cot.device.agent.service.event.AgentContext;

/**
 * create a proxy of the original service by the {@code AgentServiceInvocationHandler}
 */
public class AgentServiceProxy {

    /** the logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentServiceProxy.class);
    
    /** the original service */
    private AgentService agentService;
    
    /** the proxy service */
    private AgentService serviceProxy;
    
    /**
     * create a new instance of {@link AgentServiceProxy} for the given 'original' {@link AgentService}
     * with a new {@link AgentServiceInvocationHandler}
     * @param agentService the original service instance
     * @throws AbstractAgentException 
     */
    public static AgentServiceProxy create(AgentService agentService) throws AbstractAgentException {
        // check agent service
        assertNotNull(agentService, AgentServiceProxyException.class, LOGGER, "no agent service given");

        // create and check service proxy
        AgentService serviceProxy = ProxyUtil.createProxy(agentService.getClass(), new AgentServiceInvocationHandler(agentService));
        assertNotNull(serviceProxy, AgentServiceProxyException.class, LOGGER, "can't create an agent service proxy for service '" + agentService + "'");

        return new AgentServiceProxy(agentService, serviceProxy);
    }
    
    /**
     * private constructor, use 'create'
     */
    private AgentServiceProxy(AgentService agentService, AgentService serviceProxy) {
        this.agentService = agentService;
        this.serviceProxy = serviceProxy;
    }
    
    /**
     * get the agent service proxy
     */
    @SuppressWarnings("unchecked")
    public <T extends AgentService> T getProxy() throws AbstractAgentException {
        try {
            return (T) serviceProxy;
        } catch (Exception e) {
            throw new AgentServiceProxyException("can't get agent service proxy", e);
        }
    }

    /**
     * initializes the service
     * @throws AbstractAgentException if an error occurs during initialization 
     */
    public void init(AgentContext agentContext) throws AbstractAgentException {
        serviceProxy.init(agentContext);
    }

    /**
     * determines if the specified service type is assignment-compatible with the proxy class
     * @param serviceType the service type
     * @return true if serviceType is an instance of proxy class
     */
    boolean isInstance(Class<? extends AgentService> serviceType) {
        return serviceType.isInstance(agentService);
    }

    @Override
    public String toString() {
        return AgentServiceProxy.class.getSimpleName() + " [class=" + agentService.getClass() + "]";
    }
}
