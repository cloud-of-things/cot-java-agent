package com.telekom.cot.device.agent.platform.rest;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;

import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.m2m.cot.restsdk.event.Event;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;

public class PlatformServiceRestEventsTest extends PlatformServiceRestImplTestBase {

	private static final Date EVENT_TIME = new Date();
	private static final String EVENT_TEXT = "eventText";
	private static final String EVENT_TYPE = "eventType";
	private static final String EVENT_CONDITION = "c8y_Startup";
	
	@Before
	public void setUp() throws Exception {
		super.setUp();

		// behavior of mocked event API
		when(mockEventApi.createEvent(any(Event.class))).then(AdditionalAnswers.returnsFirstArg());
	}
	
	@Test
	public void testCreateEvent() throws Exception {
		platformServiceRestImpl.createEvent(EVENT_TIME, EVENT_TYPE, EVENT_TEXT,EVENT_CONDITION);
		
		verify(mockLogger).info("create event (time={}, type={}, text={})", EVENT_TIME, EVENT_TYPE, EVENT_TEXT);
		verify(mockEventApi).createEvent(any(Event.class));
	}

	@Test
	public void testCreateEventNullTime() throws Exception {
		platformServiceRestImpl.createEvent(null, EVENT_TYPE, EVENT_TEXT,EVENT_CONDITION);

		verify(mockLogger).info(eq("create event (time={}, type={}, text={})"), any(Date.class), eq(EVENT_TYPE), eq(EVENT_TEXT));
		verify(mockEventApi).createEvent(any(Event.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateEventNullType() throws Exception {
		platformServiceRestImpl.createEvent(EVENT_TIME, null, EVENT_TEXT,EVENT_CONDITION);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateEventNullText() throws Exception {
		platformServiceRestImpl.createEvent(EVENT_TIME, EVENT_TYPE, null,EVENT_CONDITION);
	}

	@Test
	public void testCreateEventNullObject() throws Exception {
		platformServiceRestImpl.createEvent(EVENT_TIME, EVENT_TYPE, EVENT_TEXT,EVENT_CONDITION);

		verify(mockLogger).info("create event (time={}, type={}, text={})", EVENT_TIME, EVENT_TYPE, EVENT_TEXT);
		verify(mockEventApi).createEvent(any(Event.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateEventCotSdkException() throws Exception {
		doThrow(new CotSdkException("test")).when(mockEventApi).createEvent(any(Event.class));

		platformServiceRestImpl.createEvent(EVENT_TIME, EVENT_TYPE, EVENT_TEXT,EVENT_CONDITION);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateEventEventApiNull() throws Exception {
		doReturn(null).when(mockCoTPlatform).getEventApi();

		platformServiceRestImpl.createEvent(EVENT_TIME, EVENT_TYPE, EVENT_TEXT,EVENT_CONDITION);
	}
}
