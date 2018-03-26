package com.telekom.cot.device.agent.common;

import org.junit.Assert;
import org.junit.Test;

public class AlarmSeverityTest {
	@Test
	public void testEnum() {
        AlarmSeverity alarmSeverity = AlarmSeverity.UNDEFINED;
        Assert.assertEquals("", alarmSeverity.getValue());

        alarmSeverity = AlarmSeverity.CRITICAL;
        Assert.assertEquals("CRITICAL", alarmSeverity.value());

        alarmSeverity = AlarmSeverity.fromValue("MAJOR");
        Assert.assertEquals("MAJOR", alarmSeverity.getValue());

        alarmSeverity = AlarmSeverity.MINOR;
        Assert.assertEquals("MINOR", alarmSeverity.getValue());

        alarmSeverity = AlarmSeverity.WARNING;
        Assert.assertEquals("WARNING", alarmSeverity.getValue());

        alarmSeverity = AlarmSeverity.fromValue("");
        Assert.assertEquals("", alarmSeverity.getValue());
    }
}
