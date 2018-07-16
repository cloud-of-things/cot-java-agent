package com.telekom.cot.device.agent.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentServiceNotFoundException;
import com.telekom.cot.device.agent.common.exc.AgentShutdownException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;

public class AgentServiceShutdownHelperTest {

	@Mock private Logger mockLogger;
    @Mock private AgentServiceProvider mockServiceProvider;
	@Mock TestService1 mockTimedOutService;

	private static final long TIMEOUT = 1000;
	
	private AgentServiceShutdownHelper shutdownHelper;
	
    List<TestService1> testService1List;
    List<TestService2> testService2List;
    
    @SuppressWarnings("rawtypes")
	@Before
    public void setUp() throws Exception {
    	// init mocks and inject logger into AgentServiceShutdownHelper
    	MockitoAnnotations.initMocks(this);
    	InjectionUtil.injectStatic(AgentServiceShutdownHelper.class, mockLogger); 
    	
    	shutdownHelper = new AgentServiceShutdownHelper(mockServiceProvider);
    	
    	// default behavior of mocked service provider returns one instance of 'TestService2' and one instance of 'TestService1'
    	testService1List = new ArrayList<>();
    	testService1List.add(new TestService1());
    	testService2List = new ArrayList<>();
    	testService2List.add(new TestService2());
    	doThrow(new AgentServiceNotFoundException("service not found")).when(mockServiceProvider).getService(null);
    	doThrow(new AgentServiceNotFoundException("service not found")).when(mockServiceProvider).getServices(null);
    	when(mockServiceProvider.getServices(TestService1.class)).thenReturn(testService1List);
    	when(mockServiceProvider.getServices(TestService2.class)).thenReturn(testService2List);

    	// behavior of timed out service (service with blocking stop)
    	doAnswer(new Answer() {
    		@Override
    		public Object answer(InvocationOnMock arg0) throws Throwable {
    			Thread.sleep(TIMEOUT * 10);
    			return null;
    		}
    	}).when(mockTimedOutService).stop();
    }

	/**
	 * test constructor, service provider null
	 */
	@Test(expected=AgentShutdownException.class)
	public void testConstructorServiceProviderNull() throws Exception {
    	shutdownHelper = new AgentServiceShutdownHelper(null);
	}

	/**
	 * test method shutdownService, service type null
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServiceTypeNull() throws Exception {
    	shutdownHelper.shutdownService(null, TIMEOUT, true);
	}

	/**
	 * test method shutdownService, service type not found
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServiceServiceNotFound() throws Exception {
		// reset default behavior
		AbstractAgentException e = new AgentServiceNotFoundException("not found");
		doThrow(e).when(mockServiceProvider).getService(TestService2.class);
		
    	shutdownHelper.shutdownService(TestService2.class, TIMEOUT, true);
	}

    /**
     * test method shutdownService
     */
    @Test
    public void testShutdownService() throws Exception {
        shutdownHelper.shutdownService(TestService2.class, TIMEOUT, true);
    }

	/**
	 * test method shutdownServices, service type null
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServicesTypeNull() throws Exception {
    	shutdownHelper.shutdownServices((Class<? extends AgentService>)null, TIMEOUT, true);
	}

	/**
	 * test method shutdownServices, service list is null
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServicesServiceListNull() throws Exception {
    	AgentServiceShutdownHelper.shutdownServices((List<AgentService>)null, TIMEOUT, true);
	}

	/**
	 * test method shutdownServices, service list is empty
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServicesServiceListEmpty() throws Exception {
    	AgentServiceShutdownHelper.shutdownServices(new ArrayList<>(), TIMEOUT, true);
	}

	/**
	 * test method shutdownServices, service type not found
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServicesServiceNotFound() throws Exception {
		// reset default behavior
		AbstractAgentException e = new AgentServiceNotFoundException("not found");
		doThrow(e).when(mockServiceProvider).getServices(TestService2.class);
		
    	shutdownHelper.shutdownServices(TestService2.class, TIMEOUT, true);
	}

	/**
	 * test method shutdownServices, invalid timeout 0
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServicesInvalidTimeout0() throws Exception {
    	shutdownHelper.shutdownServices(TestService1.class, 0, true);
	}

	/**
	 * test method shutdownServices, invalid timeout negative
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServicesInvalidTimeoutNegative() throws Exception {
    	shutdownHelper.shutdownServices(TestService1.class, -10, true);
	}

	/**
	 * test method shutdownServices, service.stop() throws AbstractAgentException
	 */
	@Test
	public void testShutdownServicesStopThrowsException() throws Exception {
		@SuppressWarnings("serial")
		AbstractAgentException exception = new AbstractAgentException("test"){ };
		
		// service with exception
		TestService1 exceptionService = mock(TestService1.class);
		doThrow(exception).when(exceptionService).stop();

		testService1List.clear();
		testService1List.add(exceptionService);

		shutdownHelper.shutdownServices(TestService1.class, TIMEOUT, true);
    	
		verify(mockLogger).error("stopping service {} failed, {}", exceptionService, exception);
	}

	/**
	 * test method shutdownServices, service.stop() times out, force terminate
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServicesTimeoutTerminate() throws Exception {
		testService1List.clear();
		testService1List.add(mockTimedOutService);

    	shutdownHelper.shutdownServices(TestService1.class, TIMEOUT, true);
	}

	/**
	 * test method shutdownServices, service.stop() times out, don't terminate
	 */
	@Test(expected=AgentShutdownException.class)
	public void testShutdownServicesTimeoutDontTerminate() throws Exception {
		testService1List.clear();
		testService1List.add(mockTimedOutService);

    	shutdownHelper.shutdownServices(TestService1.class, TIMEOUT, false);
	}

	/**
	 * test method shutdownServices, stop-thread is interrupted
	 */
	@Test
	public void testShutdownServicesThreadInterrupted() throws Exception {
		testService1List.clear();
		testService1List.add(mockTimedOutService);
    	
    	// execute 'shutdownServices' in a thread to be able to interrupt it
    	Thread thread = new Thread() {
    		@Override
    		public void run() {
    			try {
					shutdownHelper.shutdownServices(TestService1.class, TIMEOUT, true);
				} catch (AbstractAgentException e) {
				}    			
    		}
    	};
    	
    	// start the 
    	thread.start();
    	thread.join(TIMEOUT/4);
    	thread.interrupt();
    	Thread.sleep(TIMEOUT);

    	verify(mockLogger).error(eq("awaiting termination of stopping services has been interrupted"), any(InterruptedException.class));
	}

	/**
	 * test method shutdownServices
	 */
	@Test
	public void testShutdownServices() throws Exception {
    	shutdownHelper.shutdownServices(TestService1.class, 100, false);
	}
}
