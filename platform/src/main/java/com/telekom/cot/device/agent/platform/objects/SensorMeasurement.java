package com.telekom.cot.device.agent.platform.objects;

import java.time.Instant;
import java.util.Date;

public class SensorMeasurement {

	private Date time;
	private String type;
	private float value;
	private String unit;

	public SensorMeasurement(String type, float value, String unit) {
		this.time = Date.from(Instant.now());
		this.type = type;
		this.value = value;
		this.unit = unit;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}
