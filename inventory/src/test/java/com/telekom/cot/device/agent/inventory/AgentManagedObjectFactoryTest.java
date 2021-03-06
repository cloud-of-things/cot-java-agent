package com.telekom.cot.device.agent.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telekom.cot.device.agent.common.exc.PropertyNotFoundException;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.ConfigurationProperties;
import com.telekom.cot.device.agent.system.properties.FirmwareProperties;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;
import com.telekom.cot.device.agent.system.properties.MobileProperties;
import com.telekom.cot.device.agent.system.properties.SoftwareProperties;

public class AgentManagedObjectFactoryTest {

	private static final String DEVICE_TYPE = "deviceType";
	private static final String DEVICE_NAME = "deviceName";

	private static final String SOFTWARE_NAME = "softwareName";
	private static final String SOFTWARE_VERSION = "softwareVersion";
	private static final String SOFTWARE_URL = "softwareURL";

	private HardwareProperties hardwareProperties;
	private ConfigurationProperties configurationProperties;
	private FirmwareProperties firmwareProperties;
	private MobileProperties mobileProperties;
	private SoftwareProperties softwareProperties;

	private InventoryServiceConfiguration config;
	@Mock
	private SystemService mockSystemService;

	private AgentManagedObjectFactory factory;

	@Before
	public void setUp() throws Exception {

		config = new InventoryServiceConfiguration();
		config.setAgent(true);
		config.setDevice(true);
		config.setDeviceName(DEVICE_NAME);
		config.setDeviceType(DEVICE_TYPE);

		hardwareProperties = new HardwareProperties();
		hardwareProperties.setModel("model");
		hardwareProperties.setRevision("revision");
		hardwareProperties.setSerialNumber("serialNumber");

		configurationProperties = new ConfigurationProperties();
		configurationProperties.setConfig("config");

		firmwareProperties = new FirmwareProperties();
		firmwareProperties.setName("name");
		firmwareProperties.setUrl("url");
		firmwareProperties.setVersion("version");

		mobileProperties = new MobileProperties();
		mobileProperties.setCellId("cellId");
		mobileProperties.setIccid("iccid");
		mobileProperties.setImei("imei");

		softwareProperties = new SoftwareProperties();
		softwareProperties.addSoftware(SOFTWARE_NAME, SOFTWARE_VERSION, SOFTWARE_URL);

		MockitoAnnotations.initMocks(this);

		when(mockSystemService.getProperties(HardwareProperties.class)).thenReturn(hardwareProperties);
		when(mockSystemService.getProperties(ConfigurationProperties.class)).thenReturn(configurationProperties);
		when(mockSystemService.getProperties(FirmwareProperties.class)).thenReturn(firmwareProperties);
		when(mockSystemService.getProperties(MobileProperties.class)).thenReturn(mobileProperties);
		when(mockSystemService.getProperties(SoftwareProperties.class)).thenReturn(softwareProperties);

		factory = com.telekom.cot.device.agent.inventory.AgentManagedObjectFactory.getInstance(config,
				mockSystemService);
	}

	@Test
	public void testCreate() {
		AgentManagedObject agentManagedObject = factory.create();

		assertEquals(DEVICE_NAME, agentManagedObject.getName());
		assertEquals(DEVICE_TYPE, agentManagedObject.getType());
		assertFragments(agentManagedObject);
	}

	@Test
	public void testCreateWithOtherName() {
		AgentManagedObject agentManagedObject = factory.create("deviceNameOther");

		assertEquals("deviceNameOther", agentManagedObject.getName());
		assertEquals(DEVICE_TYPE, agentManagedObject.getType());
		assertFragments(agentManagedObject);
	}

	@Test
	public void testCreateNoDevice() {
		config.setDevice(false);

		assertNull(factory.create().getAttributes().get("c8y_IsDevice"));
	}

	@Test
	public void testCreateNoAgent() {
		config.setAgent(false);

		assertNull(factory.create().getAttributes().get("com_cumulocity_model_Agent"));
	}

	@Test
	public void testCreateNoHardwareProperties() throws Exception {
		doThrow(new PropertyNotFoundException("not found")).when(mockSystemService)
				.getProperties(HardwareProperties.class);

		Object value = factory.create().getAttributes().get("c8y_Hardware");

		assertNotNull(value);
		assertEquals("{}", value.toString());
	}

	@Test
	public void testCreateNoConfigurationProperties() throws Exception {
		doThrow(new PropertyNotFoundException("not found")).when(mockSystemService)
				.getProperties(ConfigurationProperties.class);

		Object value = factory.create().getAttributes().get("c8y_Configuration");

		assertNotNull(value);
		assertEquals("{}", value.toString());
	}

	@Test
	public void testCreateNoFirmwareProperties() throws Exception {
		doThrow(new PropertyNotFoundException("not found")).when(mockSystemService)
				.getProperties(FirmwareProperties.class);

		Object value = factory.create().getAttributes().get("c8y_Firmware");

		assertNotNull(value);
		assertEquals("{}", value.toString());
	}

	@Test
	public void testCreateNoMobileProperties() throws Exception {
		doThrow(new PropertyNotFoundException("not found")).when(mockSystemService)
				.getProperties(MobileProperties.class);

		Object value = factory.create().getAttributes().get("c8y_Mobile");

		assertNotNull(value);
		assertEquals("{}", value.toString());
	}

	@Test
	public void testCreateNoSoftwareProperties() throws Exception {
		doThrow(new PropertyNotFoundException("not found")).when(mockSystemService)
				.getProperties(SoftwareProperties.class);

		Object value = factory.create().getAttributes().get("c8y_SoftwareList");

		assertNotNull(value);
		assertEquals("{}", value.toString());
	}

	@Test
	public void testCreateEmptySoftwareProperties() throws Exception {
		when(mockSystemService.getProperties(SoftwareProperties.class)).thenReturn(new SoftwareProperties());

		Object value = factory.create().getAttributes().get("c8y_SoftwareList");

		assertNotNull(value);
		assertEquals("{}", value.toString());
	}

	private void assertFragments(AgentManagedObject agentManagedObject) {
		// check "com_cumulocity_model_Agent"
		assertNotNull(agentManagedObject.getAttributes().get("com_cumulocity_model_Agent"));
		// check "c8y_IsDevice"
		assertEquals("{}", agentManagedObject.getAttributes().get("c8y_IsDevice").toString());
		// c8y_Hardware
		assertEquals("{\"model\":\"model\",\"revision\":\"revision\",\"serialNumber\":\"serialNumber\"}",
				agentManagedObject.getAttributes().get("c8y_Hardware").toString());
		// c8y_Configuration
		assertEquals("{\"config\":\"config\"}", agentManagedObject.getAttributes().get("c8y_Configuration").toString());
		// c8y_Firmware
		assertEquals("{\"name\":\"name\",\"version\":\"version\",\"url\":\"url\"}",
				agentManagedObject.getAttributes().get("c8y_Firmware").toString());
		// c8y_Mobile
		assertEquals("{\"imei\":\"imei\",\"cellId\":\"cellId\",\"iccid\":\"iccid\"}",
				agentManagedObject.getAttributes().get("c8y_Mobile").toString());
		// c8y_SoftwareList
		assertEquals("[{\"name\":\"" + SOFTWARE_NAME + "\",\"version\":\"" + SOFTWARE_VERSION + "\",\"url\":\""
				+ SOFTWARE_URL + "\"}]", agentManagedObject.getAttributes().get("c8y_SoftwareList").toString());
	}

}
