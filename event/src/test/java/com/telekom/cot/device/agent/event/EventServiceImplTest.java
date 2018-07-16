package com.telekom.cot.device.agent.event;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;

public class EventServiceImplTest {

	private static final String EVENT_TYPE = "eventType";
	private static final String EVENT_TEXT = "eventText";
	private static final String EVENT_STARTUP = "c8y_EventStartup";

	@Mock
	private Logger mockLogger;
	@Mock
	private PlatformService mockPlatformService;

	private EventServiceImpl eventServiceImpl = new EventServiceImpl();

	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// inject mocks "Logger" and "AgentServiceProvider"
		InjectionUtil.injectStatic(EventServiceImpl.class, mockLogger);
		InjectionUtil.inject(eventServiceImpl, mockPlatformService);

		// mock platform service
		doNothing().when(mockPlatformService).createEvent(any(Date.class), any(String.class), any(String.class), any(String.class));
	}

	/**
	 * test method start, service has already been started
	 */
	@Test(expected = AbstractAgentException.class)
	public void testStartTwice() throws Exception {
		eventServiceImpl.start();
		eventServiceImpl.start();
	}

	/**
	 * test method start, AgentServiceProvider can't get a PlatformService instance
	 */
	@Test(expected = AbstractAgentException.class)
	public void testStartNoPlatformService() throws Exception {
	    InjectionUtil.inject(eventServiceImpl, "platformService", null);
		eventServiceImpl.start();
	}

	/**
	 * test method start
	 */
	@Test
	public void testStart() throws Exception {
		eventServiceImpl.start();
	}

	/**
	 * test method stop
	 */
	@Test
	public void testStop() throws Exception {
		eventServiceImpl.start();
		eventServiceImpl.stop();
	}

	/**
	 * test method createEvent, PlatformService.createEvent() throws exception
	 */
	@Test(expected = PlatformServiceException.class)
	public void testCreateEventCreateEventException() throws Exception {
		// throw exception when calling "PlatformService.createEvent"
		doThrow(new PlatformServiceException("can't create event")).when(mockPlatformService)
				.createEvent(any(Date.class), eq(EVENT_TYPE), eq(EVENT_TEXT), eq(EVENT_STARTUP));
		InjectionUtil.inject(eventServiceImpl, mockPlatformService);

		eventServiceImpl.createEvent(EVENT_TYPE, EVENT_TEXT, EVENT_STARTUP);
	}

	/**
	 * test method createEvent
	 */
	@Test
	public void testCreateEvent() throws Exception {
		InjectionUtil.inject(eventServiceImpl, mockPlatformService);

		eventServiceImpl.createEvent(EVENT_TYPE, EVENT_TEXT, EVENT_STARTUP);

		verify(mockLogger).debug("create and send event of type '{}' with text '{}'", EVENT_TYPE, EVENT_TEXT);
		verify(mockPlatformService).createEvent(any(Date.class), eq(EVENT_TYPE), eq(EVENT_TEXT), eq(EVENT_STARTUP));
	}
}
