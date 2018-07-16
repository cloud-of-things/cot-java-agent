package com.telekom.cot.device.agent.alarm;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AlarmServiceException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;

public class AlarmServiceImplTest {
	private static final String ALARM_TYPE = "alarmType";
	private static final AlarmSeverity ALARM_SEVERITY = AlarmSeverity.CRITICAL;
	private static final String ALARM_TEXT = "alarmText";
	private static final String ALARM_STATUS = "alarmStatus";

	@Mock
	private Logger mockLogger;
	@Mock
	private PlatformService mockPlatformService;

	private AlarmServiceImpl alarmServiceImpl = new AlarmServiceImpl();

	@Before
	public void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// inject mocks "Logger" and "PlatformService"
		InjectionUtil.injectStatic(AlarmServiceImpl.class, mockLogger);
		InjectionUtil.inject(alarmServiceImpl, mockPlatformService);
	}

	/**
	 * test method start, service has already been started
	 */
	@Test(expected = AbstractAgentException.class)
	public void testStartTwice() throws Exception {
		alarmServiceImpl.start();
		alarmServiceImpl.start();
	}

	/**
	 * test method start, AgentServiceProvider can't get a PlatformService instance
	 */
	@Test(expected = AlarmServiceException.class)
	public void testStartNoPlatformService() throws Exception {
	    InjectionUtil.inject(alarmServiceImpl, "platformService", null);
		alarmServiceImpl.start();
	}

	/**
	 * test method start
	 */
	@Test
	public void testStart() throws Exception {
		alarmServiceImpl.start();
	}

	/**
	 * test method stop
	 */
	@Test
	public void testStop() throws Exception {
		alarmServiceImpl.start();
		alarmServiceImpl.stop();
	}

	/**
	 * test method createAlarm, PlatformService.createAlarm() throws exception
	 */
	@Test(expected = PlatformServiceException.class)
	public void testCreateAlarmCreateAlarmException() throws Exception {
		// throw exception when calling "PlatformService.createAlarm"
		doThrow(new PlatformServiceException("can't create alarm")).when(mockPlatformService)
				.createAlarm(any(Date.class), eq(ALARM_TYPE), eq(ALARM_SEVERITY), eq(ALARM_TEXT), eq(ALARM_STATUS));
		InjectionUtil.inject(alarmServiceImpl, mockPlatformService);

		alarmServiceImpl.createAlarm(ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS);
	}

	/**
	 * test method createAlarm
	 */
	@Test
	public void testCreateAlarm() throws Exception {
		InjectionUtil.inject(alarmServiceImpl, mockPlatformService);

		alarmServiceImpl.createAlarm(ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS);

		verify(mockLogger).debug("create and send alarm of type {} with text {} and severity {}", ALARM_TYPE,
				ALARM_TEXT, ALARM_SEVERITY.getValue());
		verify(mockPlatformService).createAlarm(any(Date.class), eq(ALARM_TYPE), eq(ALARM_SEVERITY), eq(ALARM_TEXT),
				eq(ALARM_STATUS));
	}
}
