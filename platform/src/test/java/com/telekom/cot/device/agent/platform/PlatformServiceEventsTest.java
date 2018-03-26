package com.telekom.cot.device.agent.platform;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.m2m.cot.restsdk.event.Event;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;

public class PlatformServiceEventsTest extends PlatformServiceImplTestBase {

	private static final Date EVENT_TIME = new Date();
	private static final String EVENT_TEXT = "eventText";
	private static final String EVENT_TYPE = "eventType";
	private static final String EVENT_OBJECT = "eventObject";
	
	@Before
	public void setUp() throws Exception {
		super.setUp();

		// behavior of mocked event API
		when(mockEventApi.createEvent(any(Event.class))).then(AdditionalAnswers.returnsFirstArg());
	}
	
	@Test
	public void testCreateEvent() throws Exception {
		platformServiceImpl.createEvent(EVENT_TIME, EVENT_TYPE, EVENT_TEXT, null, EVENT_OBJECT);
		
		verify(mockLogger).info("create event (time={}, type={}, text={})", EVENT_TIME, EVENT_TYPE, EVENT_TEXT);
		verify(mockEventApi).createEvent(any(Event.class));
	}

	@Test
	public void testCreateEventNullTime() throws Exception {
		platformServiceImpl.createEvent(null, EVENT_TYPE, EVENT_TEXT, null, EVENT_OBJECT);

		verify(mockLogger).info(eq("create event (time={}, type={}, text={})"), any(Date.class), eq(EVENT_TYPE), eq(EVENT_TEXT));
		verify(mockEventApi).createEvent(any(Event.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateEventNullType() throws Exception {
		platformServiceImpl.createEvent(EVENT_TIME, null, EVENT_TEXT, null, EVENT_OBJECT);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateEventNullText() throws Exception {
		platformServiceImpl.createEvent(EVENT_TIME, EVENT_TYPE, null, null, EVENT_OBJECT);
	}

	@Test
	public void testCreateEventNullObject() throws Exception {
		platformServiceImpl.createEvent(EVENT_TIME, EVENT_TYPE, EVENT_TEXT, null, null);

		verify(mockLogger).info("create event (time={}, type={}, text={})", EVENT_TIME, EVENT_TYPE, EVENT_TEXT);
		verify(mockEventApi).createEvent(any(Event.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateEventCotSdkException() throws Exception {
		doThrow(new CotSdkException("test")).when(mockEventApi).createEvent(any(Event.class));

		platformServiceImpl.createEvent(EVENT_TIME, EVENT_TYPE, EVENT_TEXT, null, EVENT_OBJECT);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateEventEventApiNull() throws Exception {
		doReturn(null).when(mockCoTPlatform).getEventApi();

		platformServiceImpl.createEvent(EVENT_TIME, EVENT_TYPE, EVENT_TEXT, null, EVENT_OBJECT);
	}
}
