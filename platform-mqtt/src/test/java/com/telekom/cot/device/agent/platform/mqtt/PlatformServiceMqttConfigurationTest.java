package com.telekom.cot.device.agent.platform.mqtt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.telekom.cot.device.agent.common.util.ValidationUtil;
import com.telekom.cot.device.agent.platform.PlatformServiceConfiguration;

public class PlatformServiceMqttConfigurationTest {

	private static final String XID = "mqttXID";
	private static final String PORT = "12345";
	private static final Integer TIMEOUT = 1;
	private static final Integer DELAY_SEND_MEASUREMENT = 100;
	private static final String HOSTNAME = "host";
	private static final String EXTERNAL_ID_TYPE = "externalIdType";
	private static final String EXTERNAL_ID_VALUE = "externalIdValue";

	private PlatformServiceConfiguration.ExternalIdConfig externalIdConfig;
	private PlatformServiceMqttConfiguration.MqttConfiguration mqttConfiguration;
	private PlatformServiceMqttConfiguration platformServiceMqttConfig;

	@Before
	public void setUp() {
		externalIdConfig = new PlatformServiceConfiguration.ExternalIdConfig();
		externalIdConfig.setType(EXTERNAL_ID_TYPE);
		externalIdConfig.setValue(EXTERNAL_ID_VALUE);

		mqttConfiguration = new PlatformServiceMqttConfiguration.MqttConfiguration();
		mqttConfiguration.setPort(PORT);
		mqttConfiguration.setxId(XID);
		mqttConfiguration.setTimeout(TIMEOUT);
		mqttConfiguration.setDelaySendMeasurement(DELAY_SEND_MEASUREMENT);

		platformServiceMqttConfig = new PlatformServiceMqttConfiguration();
		platformServiceMqttConfig.setHostName(HOSTNAME);
		platformServiceMqttConfig.setExternalIdConfig(externalIdConfig);
		platformServiceMqttConfig.setMqttConfiguration(mqttConfiguration);
	}

	@Test
	public void testSettersAndGetters() {
		assertEquals(HOSTNAME, platformServiceMqttConfig.getHostName());
		assertEquals(externalIdConfig, platformServiceMqttConfig.getExternalIdConfig());
		assertEquals(EXTERNAL_ID_TYPE, platformServiceMqttConfig.getExternalIdConfig().getType());
		assertEquals(EXTERNAL_ID_VALUE, platformServiceMqttConfig.getExternalIdConfig().getValue());
		assertEquals(mqttConfiguration, platformServiceMqttConfig.getMqttConfiguration());
		assertEquals(PORT, platformServiceMqttConfig.getMqttConfiguration().getPort());
		assertEquals(XID, platformServiceMqttConfig.getMqttConfiguration().getxId());
	}

	@Test
	public void testToString() {
		String expected = PlatformServiceMqttConfiguration.class.getSimpleName() + " [hostName=" + HOSTNAME
				+ ", externalId=" + PlatformServiceConfiguration.ExternalIdConfig.class.getSimpleName() + " [type="
				+ EXTERNAL_ID_TYPE + ", value=" + EXTERNAL_ID_VALUE + "]" + ", mqttConfiguration="
				+ PlatformServiceMqttConfiguration.MqttConfiguration.class.getSimpleName() + " [port=" + PORT + ", xId="
				+ XID + ", timeout=" + TIMEOUT + ", delaySendMeasurement=" + DELAY_SEND_MEASUREMENT + "]" + "]";

		assertEquals(expected, platformServiceMqttConfig.toString());
	}

	@Test
	public void testBeanValidation() {
		// prepared configuration (@Before) must be valid
		assertTrue(ValidationUtil.isValid(platformServiceMqttConfig));

		// configuration with no port and set xId is invalid
		mqttConfiguration.setPort(null);
		mqttConfiguration.setxId(XID);
		assertFalse(ValidationUtil.isValid(platformServiceMqttConfig));

		// configuration with set port and no xId is invalid
		mqttConfiguration.setPort(PORT);
		mqttConfiguration.setxId(null);
		assertFalse(ValidationUtil.isValid(platformServiceMqttConfig));

		// configuration with no port and no xId is invalid
		mqttConfiguration.setPort(null);
		mqttConfiguration.setxId(null);
		assertFalse(ValidationUtil.isValid(platformServiceMqttConfig));

		// configuration with no mqtt configuration is invalid
		platformServiceMqttConfig.setMqttConfiguration(null);
		assertFalse(ValidationUtil.isValid(platformServiceMqttConfig));
	}
}
