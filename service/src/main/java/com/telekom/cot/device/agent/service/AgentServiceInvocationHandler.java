package com.telekom.cot.device.agent.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AgentInvocationHandlerException;

/**
 * the invocation handler of {@code AgentServiceProxy}
 *
 */
public class AgentServiceInvocationHandler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentServiceInvocationHandler.class);
    private AgentService agentService;

    public AgentServiceInvocationHandler(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * Processes a method invocation on a agent service instance and returns
     * the result.  This method will be invoked on an invocation handler
     * when a method is invoked on a proxy.
     * 
     * following process is done before the proxy invoke is executed:
     * 
     * <ul>
     *   <li>AgentService.start() - inject configurations</li>
     * </ul>
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(agentService, args);
        } catch (Exception e) {
            LOGGER.error("cant't invoce method {}", method.getName(), e);
            throw new AgentInvocationHandlerException("cant't invoce method " + method.getName(), e);
        }
    }
}
