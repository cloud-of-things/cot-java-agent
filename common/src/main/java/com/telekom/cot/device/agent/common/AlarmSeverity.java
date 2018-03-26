package com.telekom.cot.device.agent.common;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AlarmSeverity {
	UNDEFINED(""),
	CRITICAL("CRITICAL"),
	MAJOR("MAJOR"),
	MINOR("MINOR"),
	WARNING("WARNING");
	
	private final String value;
	
	AlarmSeverity(String value) {
		this.value = value.toUpperCase();
	}
	
    public String value() {
        return this.value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @JsonCreator
    public static AlarmSeverity fromValue(String value) {
        return StringUtils.isNotEmpty(value) ? AlarmSeverity.valueOf(value.toUpperCase()) : UNDEFINED;
    }
}
