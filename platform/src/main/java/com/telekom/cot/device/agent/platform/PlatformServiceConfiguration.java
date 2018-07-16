package com.telekom.cot.device.agent.platform;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.telekom.cot.device.agent.common.configuration.Configuration;

public abstract class PlatformServiceConfiguration implements Configuration {

	@NotNull @NotEmpty
	private String hostName;
	
	@NotNull @Valid
	private ExternalIdConfig externalIdConfig;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public ExternalIdConfig getExternalIdConfig() {
		return externalIdConfig;
	}

	public void setExternalIdConfig(ExternalIdConfig externalId) {
		this.externalIdConfig = externalId;
	}

	public static class ExternalIdConfig {

		public enum ValueTemplates { NO_TEMPLATE, HARDWARE_SERIAL, TYPE_HARDWARE_SERIAL }
		
		@NotNull @NotEmpty
		private String type;
		private String value;
		private ValueTemplates valueTemplate;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public ValueTemplates getValueTemplate() {
			return valueTemplate;
		}

		public void setValueTemplate(ValueTemplates valueTemplate) {
			this.valueTemplate = valueTemplate;
		}

		@Override
		public String toString() {
			return ExternalIdConfig.class.getSimpleName() + " [type=" + type + ", value=" + value + "]";
		}
	}
}
