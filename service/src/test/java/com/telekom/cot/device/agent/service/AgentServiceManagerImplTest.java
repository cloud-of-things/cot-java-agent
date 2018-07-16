package com.telekom.cot.device.agent.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.Configuration;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;

@SuppressWarnings("unchecked")
public class AgentServiceManagerImplTest {

	@Mock private Logger mockLogger;
	@Mock private ServiceLoader<AgentService> mockServiceLoader;
    @Mock private Iterator<AgentService> mockServiceIterator;
    @Mock private ServiceLoader<AgentService> mockServiceLoaderInject;
    @Mock private Iterator<AgentService> mockServiceIteratorInject;
    @Mock private ConfigurationManager mockConfigurationManager;
	@Mock private AgentCredentialsManager mockAgentCredentialsManager;

    private TestService1 testService1 = new TestService1();
    private TestService2 testService2 = new TestService2();
    private TestService3 testService3 = new TestService3();
    
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
    
    @Test
    public void testInjectConfiguration() throws Exception {
        InjectionUtil.injectStatic(AgentServiceManagerImpl.class, mockServiceLoaderInject);
        InjectionUtil.injectStatic(AgentServiceManagerImpl.class, mockLogger); 
        // default behavior: service loader returns one instance of 'TestService2' and one instance of 'TestService1'
        when(mockServiceLoaderInject.iterator()).thenReturn(mockServiceIteratorInject);
        when(mockServiceIteratorInject.hasNext()).thenReturn(true,false);
        when(mockServiceIteratorInject.next()).thenReturn(testService3);
        when(mockConfigurationManager.getConfiguration(TestConfiguration.class)).thenReturn(new TestConfiguration());
        agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
        agentServiceManager.getService(TestServiceIF3.class).start();
        assertThat(testService3.getConfiguration(), Matchers.notNullValue(TestConfiguration.class));
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
     * test method loadAndInitServices
     */
	@Test
	public void testLoadServices() throws Exception {
		agentServiceManager.loadAndInitServices(mockConfigurationManager, mockAgentCredentialsManager);
		
		verify(mockLogger, never()).warn(eq("can't initialize agent service '{}'"), eq(testService2.getClass()), any(AbstractAgentException.class));
        verify(mockLogger, never()).warn(eq("can't initialize agent service '{}'"), eq(testService1.getClass()), any(AbstractAgentException.class));
    	verify(mockLogger).debug("loaded agent service '{}' successfully", testService2.getClass());
    	verify(mockLogger).debug("loaded agent service '{}' successfully", testService1.getClass());
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
    	
    	assertThat(agentServiceManager.getService(TestServiceIF1.class)!=null, Matchers.equalTo(true));
    	assertThat(agentServiceManager.getService(TestServiceIF2.class)!=null, Matchers.equalTo(true));

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
    	assertEquals(1, agentServiceManager.getServices(TestServiceIF1.class).size());
    	assertEquals(1, agentServiceManager.getServices(TestServiceIF2.class).size());
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
