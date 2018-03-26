package com.telekom.cot.device.agent.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.Configuration;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;

@SuppressWarnings("unchecked")
public class AgentServiceManagerImplTest {

	@Mock private Logger mockLogger;
	@Mock private ServiceLoader<AgentService> mockServiceLoader;
    @Mock private Iterator<AgentService> mockServiceIterator;
    @Mock private ConfigurationManager mockConfigurationManager;
	@Mock private AgentCredentialsManager mockAgentCredentialsManager;

    private TestService1 testService1 = new TestService1();
    private TestService2 testService2 = new TestService2();
    
    private AgentServiceManager agentServiceManager = AgentServiceManagerImpl.getInstance();
    
    @Before
    public void setUp() throws Exception {
    	// init mocks and inject into AgentServiceManager
    	MockitoAnnotations.initMocks(this);
    	InjectionUtil.injectStatic(AgentServiceManagerImpl.class, mockServiceLoader);
    	InjectionUtil.injectStatic(AgentServiceManagerImpl.class, mockLogger); 
    	
    	// default behavior: service loader returns one instance of 'TestService2' and one instance of 'TestService1'
    	when(mockServiceLoader.iterator()).thenReturn(mockServiceIterator);
    	when(mockServiceIterator.hasNext()).thenReturn(true, true, false);
    	when(mockServiceIterator.next()).thenReturn(testService2, testService1);
    	
    	when(mockConfigurationManager.getConfiguration(Configuration.class)).thenReturn(new Configuration() {});
    }

    /**
     * test method count
     */
    @Test
    public void testCount() throws Exception {
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	assertEquals(2, agentServiceManager.count());
    }
    
    /**
     * test method loadAndInitServices, no services loaded
     */
	@Test
	public void testLoadAndInitServicesNoServiceFound() throws Exception {
		// reset default behavior: no service found by service loader
		reset(mockServiceIterator);
    	when(mockServiceIterator.hasNext()).thenReturn(false);

    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);

    	verify(mockLogger, never()).debug(eq("loaded agent service '{}'"), any(Class.class));
    	verify(mockLogger).info("loaded and initialized {} agent services", 0);
	}
	
    /**
     * test method loadAndInitServices, iterator returns null service
     */
	@Test
	public void testLoadAndInitServicesNullServiceFound() throws Exception {
		// reset default behavior: one service found by service loader, but iterator returns 'null'
		reset(mockServiceIterator);
    	when(mockServiceIterator.hasNext()).thenReturn(true, false);
    	when(mockServiceIterator.next()).thenReturn(null);

    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);

    	verify(mockLogger, never()).debug(eq("loaded agent service '{}'"), any(Class.class));
    	verify(mockLogger).info("loaded and initialized {} agent services", 0);
	}
	
    /**
     * test method loadAndInitServices, iterator returns a service not extends AbstractAgentService
     */
	@Test
	public void testLoadAndInitServicesNoAbstractAgentService() throws Exception {
		AgentService mockAbstractService = mock(AgentService.class);
		reset(mockServiceIterator);
		when(mockServiceIterator.hasNext()).thenReturn(true, false);
    	when(mockServiceIterator.next()).thenReturn(mockAbstractService);
		
		agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
		
		verify(mockLogger, never()).debug(eq("initialize agent service '{}'"), any(Class.class));
    	verify(mockLogger).debug("loaded agent service '{}'", mockAbstractService.getClass());
    	verify(mockLogger).info("loaded and initialized {} agent services", 1);
	}	

	/**
     * test method loadAndInitServices
     */
	@Test
	public void testLoadServices() throws Exception {
		agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
		
		verify(mockLogger).debug("initialize agent service '{}'", testService2.getClass());
		verify(mockLogger).debug("initialize agent service '{}'", testService1.getClass());
    	verify(mockLogger).debug("loaded agent service '{}'", testService2.getClass());
    	verify(mockLogger).debug("loaded agent service '{}'", testService1.getClass());
    	verify(mockLogger).info("loaded and initialized {} agent services", 2);
	}
	
	/**
	 * test method getService without a service type
	 */
	@Test(expected=AgentServiceNotFoundException.class)
	public void testGetServiceNoTypeGiven() throws Exception {
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	agentServiceManager.getService(null);
	}

	/**
	 * test method getService, no service loaded
	 */
	@Test(expected=AgentServiceNotFoundException.class)
	public void testGetServiceNoServiceLoaded() throws Exception {
		// reset default behavior: no service found by service loader
		reset(mockServiceIterator);
    	when(mockServiceIterator.hasNext()).thenReturn(false);
		
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	agentServiceManager.getService(AgentService.class);
	}

	/**
	 * test method getService, requested service type not found
	 */
	@Test(expected=AgentServiceNotFoundException.class)
	public void testGetServiceTypeNotFound() throws Exception {
		// reset default behavior: one service 'TestService1' found by service loader
		reset(mockServiceIterator);
    	when(mockServiceIterator.hasNext()).thenReturn(true, false);
    	when(mockServiceIterator.next()).thenReturn(new TestService1());
		
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	agentServiceManager.getService(TestService2.class);
	}

	/**
	 * test method getService, more than one instance of requested service type found
	 */
	@Test(expected=AgentServiceNotFoundException.class)
	public void testGetServiceNotExactlyOne() throws Exception {
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	agentServiceManager.getService(AgentService.class);
	}

	/**
	 * test method getService
	 */
	@Test
	public void testGetService() throws Exception {
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	
    	assertSame(testService1, agentServiceManager.getService(TestService1.class));
    	assertSame(testService2, agentServiceManager.getService(TestService2.class));
	}

	/**
	 * test method getServices without a service type
	 */
	@Test(expected=AgentServiceNotFoundException.class)
	public void testGetServicesNoTypeGiven() throws Exception {
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	agentServiceManager.getServices(null);
	}

	/**
	 * test method getServices, no service loaded
	 */
	@Test(expected=AgentServiceNotFoundException.class)
	public void testGetServicesNoServiceLoaded() throws Exception {
		// reset default behavior: no service found by service loader
		reset(mockServiceIterator);
    	when(mockServiceIterator.hasNext()).thenReturn(false);
		
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	agentServiceManager.getServices(AgentService.class).size();
	}

	/**
	 * test method getServices, requested service type not found
	 */
	@Test(expected=AgentServiceNotFoundException.class)
	public void testGetServicesTypeNotFound() throws Exception {
		// reset default behavior: only one service 'TestService1' found by service loader
		reset(mockServiceIterator);
    	when(mockServiceIterator.hasNext()).thenReturn(true, false);
    	when(mockServiceIterator.next()).thenReturn(new TestService1());
		
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
    	agentServiceManager.getServices(TestService2.class);
	}

	/**
	 * test method getServices
	 */
	@Test
	public void testGetServices() throws Exception {
    	agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);

    	assertEquals(2, agentServiceManager.getServices(AgentService.class).size());
    	assertEquals(1, agentServiceManager.getServices(TestService1.class).size());
    	assertSame(testService1, agentServiceManager.getServices(TestService1.class).get(0));
    	assertEquals(1, agentServiceManager.getServices(TestService2.class).size());
    	assertSame(testService2, agentServiceManager.getServices(TestService2.class).get(0));
	}
	
	/**
	 * test method getConfiguration, no configuration manager
	 */
/*	@Test(expected=ConfigurationNotFoundException.class)
	public void testGetConfigurationNoConfigurationManager() throws Exception {
		InjectionUtil.inject(agentServiceManager, "configurationManager", null);
		agentServiceManager.getConfiguration(Configuration.class);
	}
*/
	/**
	 * test method getConfiguration, configuration manager throws exception
	 */
/*	@Test(expected=ConfigurationNotFoundException.class)
	public void testGetConfigurationConfigurationManagerException() throws Exception {
		reset(mockConfigurationManager);
		doThrow(new ConfigurationNotFoundException("not found")).when(mockConfigurationManager).getConfiguration(Configuration.class);
		InjectionUtil.inject(agentServiceManager, "configurationManager", mockConfigurationManager);

		agentServiceManager.getConfiguration(Configuration.class);
	}
*/
	/**
	 * test method getConfiguration, configuration manager throws exception
	 */
/*	@Test
	public void testGetConfiguration() throws Exception {
		InjectionUtil.inject(agentServiceManager, "configurationManager", mockConfigurationManager);

		assertNotNull(agentServiceManager.getConfiguration(Configuration.class));
	}
*/	
}
