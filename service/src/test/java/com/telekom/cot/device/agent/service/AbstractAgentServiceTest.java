package com.telekom.cot.device.agent.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.configuration.ConfigurationManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.service.event.AgentContext;

public class AbstractAgentServiceTest {

	@Mock private AgentServiceProvider mockAgentServiceProvider;
	@Mock private ConfigurationManager mockConfigurationManager;
	@Mock private AgentCredentialsManager mockAgentCredentialsManager;
	@Mock private PropertyChangeListener mockPropertyChangeListener;
	@Mock private AgentContext mockAgentContext;
	
	private TestService1 testService1_1 = new TestService1();
	private TestService1 testService1_2 = new TestService1();
	
	private AbstractAgentService abstractAgentService;
	
	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);
		
		// behavior of mocked AgentServiceProvider
		List<TestService1> testServices = new ArrayList<>();
		testServices.add(testService1_1);
		testServices.add(testService1_2);
		when(mockAgentServiceProvider.getService(TestService1.class)).thenReturn(testService1_1);
		when(mockAgentServiceProvider.getService(TestService2.class)).thenThrow(new AgentServiceNotFoundException("not found"));
		when(mockAgentServiceProvider.getServices(TestService1.class)).thenReturn(testServices);
		when(mockAgentServiceProvider.getServices(TestService2.class)).thenThrow(new AgentServiceNotFoundException("not found"));
		
		// create service to test
		abstractAgentService = new AbstractAgentService() {};
	}

	/**
	 * test method init, no agent context given
	 */
	@Test(expected=AbstractAgentException.class)
	public void testInitNoAgentContext() throws Exception {
		abstractAgentService.init(null);
	}

	/**
	 * test method init
	 */
	@Test
	public void testInit() throws Exception {
		abstractAgentService.init(mockAgentContext);
	}

	/**
	 * test method start
	 */
	@Test
	public void testStart() throws Exception {
        assertFalse(abstractAgentService.isStarted());
	    abstractAgentService.start();
		assertTrue(abstractAgentService.isStarted());
	}

    /**
     * test method stop
     */
    @Test
    public void testStop() throws Exception {
        assertFalse(abstractAgentService.isStarted());
        abstractAgentService.start();
        abstractAgentService.stop();
        assertFalse(abstractAgentService.isStarted());
    }

//	/**
//	 * test method getService, no service provider
//	 */
//	@Test(expected=AgentServiceNotFoundException.class)
//	public void testGetServiceWithServiceProviderNull() throws Exception {
//		abstractAgentService.getService(TestService1.class);
//	}
//
//	/**
//	 * test method getService, service not found
//	 */
//	@Test(expected=AgentServiceNotFoundException.class)
//	public void testGetServiceWithBadClass() throws Exception {
//		abstractAgentService.init(mockAgentServiceProvider, mockAgentCredentialsManager, mockAgentContext);
//		abstractAgentService.getService(TestService2.class);
//	}
//
//	/**
//	 * test method getService
//	 */
//	@Test
//	public void testGetService() throws Exception {
//		abstractAgentService.init(mockAgentServiceProvider, mockAgentCredentialsManager, mockAgentContext);
//		AgentService service = abstractAgentService.getService(TestService1.class);
//
//		assertNotNull(service);
//		assertSame(testService1_1, service);
//	}
//
//	/**
//	 * test method getServices, no service provider
//	 */
//	@Test(expected=AgentServiceNotFoundException.class)
//	public void testGetServicesWithServiceProviderNull() throws Exception {
//		abstractAgentService.getServices(TestService1.class);
//	}
//
//	/**
//	 * test method getServices, service not found
//	 */
//	@Test(expected=AgentServiceNotFoundException.class)
//	public void testGetServicesWithBadClass() throws Exception {
//		abstractAgentService.init(mockAgentServiceProvider, mockAgentCredentialsManager, mockAgentContext);
//		abstractAgentService.getServices(TestService2.class);
//	}
//
//	/**
//	 * test method getServices
//	 */
//	@Test
//	public void testGetServices() throws Exception {
//		abstractAgentService.init(mockAgentServiceProvider, mockAgentCredentialsManager, mockAgentContext);
//		List<TestService1> services = abstractAgentService.getServices(TestService1.class);
//
//		assertNotNull(services);
//		assertEquals(2, services.size());
//		assertSame(testService1_1, services.get(0));
//		assertSame(testService1_2, services.get(1));
//	}
//
//    /**
//     * test method getServiceProvider, AgentService is not initialized
//     */
//    @Test(expected=AbstractAgentException.class)
//    public void testGetServiceProviderNotInitialized() throws Exception {
//        abstractAgentService.getServiceProvider();
//    }
//    
//    /**
//     * test method getServiceProvider
//     */
//    @Test
//    public void testGetServiceProvider() throws Exception {
//        abstractAgentService.init(mockAgentServiceProvider, mockAgentCredentialsManager, mockAgentContext);
//        assertSame(mockAgentServiceProvider, abstractAgentService.getServiceProvider());
//    }
}
