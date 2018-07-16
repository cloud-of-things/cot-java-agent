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

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.m2m.cot.restsdk.alarm.Alarm;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;

public class PlatformServiceRestAlarmsTest extends PlatformServiceRestImplTestBase {

	private static final Date ALARM_TIME = new Date();
	private static final String ALARM_TYPE = "alarmType";
	private static final AlarmSeverity ALARM_SEVERITY = AlarmSeverity.CRITICAL;
	private static final String ALARM_TEXT = "alarmText";
	private static final String ALARM_STATUS = "alarmStatus";

	@Before
	public void setUp() throws Exception {
		super.setUp();

		// behavior of mocked alarm API
		when(mockAlarmApi.create(any(Alarm.class))).then(AdditionalAnswers.returnsFirstArg());
	}

	@Test
	public void testCreateAlarm() throws Exception {
		platformServiceRestImpl.createAlarm(ALARM_TIME, ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS);

		verify(mockLogger).info("create alarm (time={}, type={}, severity={}, text={})", ALARM_TIME, ALARM_TYPE,
				ALARM_SEVERITY.getValue(), ALARM_TEXT);
		verify(mockAlarmApi).create(any(Alarm.class));
	}

	@Test
	public void testCreateAlarmTimeNull() throws Exception {
		platformServiceRestImpl.createAlarm(null, ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS);

		verify(mockLogger).info(eq("create alarm (time={}, type={}, severity={}, text={})"), any(Date.class),
				eq(ALARM_TYPE), eq(ALARM_SEVERITY.getValue()), eq(ALARM_TEXT));
		verify(mockAlarmApi).create(any(Alarm.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateAlarmTypeNull() throws Exception {
		platformServiceRestImpl.createAlarm(ALARM_TIME, null, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateAlarmSeverityNull() throws Exception {
		platformServiceRestImpl.createAlarm(ALARM_TIME, ALARM_TYPE, null, ALARM_TEXT, ALARM_STATUS);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateAlarmTextNull() throws Exception {
		platformServiceRestImpl.createAlarm(ALARM_TIME, ALARM_TYPE, ALARM_SEVERITY, null, ALARM_STATUS);
	}

	@Test
	public void testCreateAlarmStatusNull() throws Exception {
		platformServiceRestImpl.createAlarm(ALARM_TIME, ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, null);

		verify(mockLogger).info("create alarm (time={}, type={}, severity={}, text={})", ALARM_TIME, ALARM_TYPE,
				ALARM_SEVERITY.getValue(), ALARM_TEXT);
		verify(mockAlarmApi).create(any(Alarm.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateAlarmCotSdkException() throws Exception {
		doThrow(new CotSdkException("test")).when(mockAlarmApi).create(any(Alarm.class));

		platformServiceRestImpl.createAlarm(ALARM_TIME, ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateAlarmAlarmApiNull() throws Exception {
		doReturn(null).when(mockCoTPlatform).getAlarmApi();

		platformServiceRestImpl.createAlarm(ALARM_TIME, ALARM_TYPE, ALARM_SEVERITY, ALARM_TEXT, ALARM_STATUS);
	}
}
