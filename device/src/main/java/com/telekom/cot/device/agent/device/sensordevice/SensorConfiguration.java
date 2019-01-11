package com.telekom.cot.device.agent.device.sensordevice;

import java.util.List;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.configuration.Configuration;

public abstract class SensorConfiguration implements Configuration {

	private int recordReadingsInterval;
	private List<AlarmConfiguration> alarmConfigurations;

	public int getRecordReadingsInterval() {
		return recordReadingsInterval;
	}

	public void setRecordReadingsInterval(int recordReadingsInterval) {
		this.recordReadingsInterval = recordReadingsInterval;
	}

	public List<AlarmConfiguration> getAlarmConfigurations() {
		return alarmConfigurations;
	}

	public void setAlarmConfigurations(List<AlarmConfiguration> alarmConfigurations) {
		this.alarmConfigurations = alarmConfigurations;
	} 

	public static class AlarmConfiguration {

		private String text;
		private String type;
		private float minValue = Float.NEGATIVE_INFINITY;
		private float maxValue = Float.POSITIVE_INFINITY;
		private AlarmSeverity severity = AlarmSeverity.UNDEFINED;

		public AlarmConfiguration() {
		}
		
		public AlarmConfiguration(String type, String text, AlarmSeverity severity, float minValue, float maxValue) {
			this.type = type;
			this.text = text;
			this.severity = severity;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public AlarmSeverity getSeverity() {
			return severity;
		}

		public void setSeverity(AlarmSeverity severity) {
			this.severity = severity;
		}

		public void setSeverity(String severity) {
			this.severity = AlarmSeverity.valueOf(severity);
		}

		public float getMinValue() {
			return minValue;
		}

		public void setMinValue(float minValue) {
			this.minValue = minValue;
		}

		public float getMaxValue() {
			return maxValue;
		}

		public void setMaxValue(float maxValue) {
			this.maxValue = maxValue;
		}
	}
}
