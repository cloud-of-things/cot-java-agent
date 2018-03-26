package com.telekom.cot.device.agent.platform;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.util.InjectionUtil;
import com.telekom.cot.device.agent.platform.PlatformServiceConfiguration.ExternalIdConfig;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import com.telekom.cot.device.agent.service.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.service.configuration.ConfigurationManager;
import com.telekom.m2m.cot.restsdk.CloudOfThingsPlatform;
import com.telekom.m2m.cot.restsdk.alarm.AlarmApi;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceControlApi;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceCredentials;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceCredentialsApi;
import com.telekom.m2m.cot.restsdk.devicecontrol.NewDeviceRequest;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.event.EventApi;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.identity.IdentityApi;
import com.telekom.m2m.cot.restsdk.inventory.InventoryApi;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.measurement.Measurement;
import com.telekom.m2m.cot.restsdk.measurement.MeasurementApi;
import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;

class PlatformServiceImplTestBase {

	protected static final String EXTERNAL_ID_TYPE = "externalIdType";
	protected static final String EXTERNAL_ID = "externalId";
	protected static final String MANAGED_OBJECT_ID = "testManagedObjectId";
	protected static final String DEVICE_ID = "testDeviceId";
	protected static final String DEVICE_REQUEST_ID = "testDeviceRequest";
	protected static final String DEVICE_TENANT = "testDeviceTenant";
	protected static final String DEVICE_USER = "testDeviceUser";
	protected static final String DEVICE_PASSWORD = "testDevicePassword";
	
	@Mock protected Logger mockLogger;
	@Mock protected CloudOfThingsPlatform mockCoTPlatform;
	@Mock protected MeasurementApi mockMeasurementApi;
	@Mock protected EventApi mockEventApi;
	@Mock protected AlarmApi mockAlarmApi;
	@Mock protected DeviceCredentialsApi mockDeviceCredentialsApi;
	@Mock protected DeviceControlApi mockDeviceControlApi;
	@Mock protected InventoryApi mockInventoryApi;
	@Mock protected IdentityApi mockIdentityApi;
	@Mock protected AgentServiceProvider mockAgentServiceProvider;
	@Mock protected ConfigurationManager mockConfigurationManager;
	@Mock protected AgentCredentialsManager mockAgentCredentialsManager;
	@Mock protected CoTPlatformBuilder mockBuilder;

	protected PlatformServiceConfiguration platformServiceConfig = new PlatformServiceConfiguration();
	protected ExternalId externalId = new ExternalId();
	protected ManagedObject managedObject = new ManagedObject();
	protected NewDeviceRequest deviceRequest = new NewDeviceRequest(new ExtensibleObject());
	protected DeviceCredentials deviceCredentials = new DeviceCredentials();
	protected AgentCredentials agentCredentials = new AgentCredentials("agent.tenant", "agent.username", "agent.password");

	
	protected PlatformServiceImpl platformServiceImpl = new PlatformServiceImpl();

	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// inject mocks
		InjectionUtil.injectStatic(PlatformServiceImpl.class, mockLogger);
		InjectionUtil.inject(platformServiceImpl, mockCoTPlatform);
		InjectionUtil.inject(platformServiceImpl, mockAgentServiceProvider);
		InjectionUtil.inject(platformServiceImpl, mockConfigurationManager);
		InjectionUtil.inject(platformServiceImpl, mockAgentCredentialsManager);
		InjectionUtil.inject(platformServiceImpl, externalId);

		// initialize externalId and managedObject
		externalId.setExternalId(EXTERNAL_ID);
		externalId.setType(EXTERNAL_ID_TYPE);
		managedObject.setId(MANAGED_OBJECT_ID);
		externalId.setManagedObject(managedObject);

		// initialize device credentials
		deviceCredentials.setId(DEVICE_ID);
		deviceCredentials.setTenantId(DEVICE_TENANT);
		deviceCredentials.setUsername(DEVICE_USER);
		deviceCredentials.setPassword(DEVICE_PASSWORD);
		
		// builder
		when(mockBuilder.setHostname(Mockito.anyString())).thenReturn(mockBuilder);
		when(mockBuilder.setPassword(Mockito.anyString())).thenReturn(mockBuilder);
		when(mockBuilder.setTenant(Mockito.anyString())).thenReturn(mockBuilder);
		when(mockBuilder.setUsername(Mockito.anyString())).thenReturn(mockBuilder);
		when(mockBuilder.setHostname(null)).thenReturn(mockBuilder);
		when(mockBuilder.setPassword(null)).thenReturn(mockBuilder);
		when(mockBuilder.setTenant(null)).thenReturn(mockBuilder);
		when(mockBuilder.setUsername(null)).thenReturn(mockBuilder);
		
		// platform config setup
		platformServiceConfig.setHostName("https://host/");
		platformServiceConfig.setProxyHost("0.0.0.1");
		platformServiceConfig.setProxyPort("8080");
		platformServiceConfig.setExternalIdConfig(new ExternalIdConfig());
		platformServiceConfig.getExternalIdConfig().setValue(EXTERNAL_ID);
		platformServiceConfig.getExternalIdConfig().setType(EXTERNAL_ID_TYPE);

		// behavior of mocked ConfigurationManager
		when(mockConfigurationManager.getConfiguration(PlatformServiceConfiguration.class)).thenReturn(platformServiceConfig);

		// behavior of mocked AgentCredentialsManager
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
		
		// behavior of mocked CoTPlatform
		when(mockCoTPlatform.getMeasurementApi()).thenReturn(mockMeasurementApi);
		when(mockCoTPlatform.getEventApi()).thenReturn(mockEventApi);
		when(mockCoTPlatform.getAlarmApi()).thenReturn(mockAlarmApi);
		when(mockCoTPlatform.getDeviceCredentialsApi()).thenReturn(mockDeviceCredentialsApi);
		when(mockCoTPlatform.getDeviceControlApi()).thenReturn(mockDeviceControlApi);
		when(mockCoTPlatform.getInventoryApi()).thenReturn(mockInventoryApi);
		when(mockCoTPlatform.getIdentityApi()).thenReturn(mockIdentityApi);

		// behavior of mocked measurement API
		when(mockMeasurementApi.createMeasurement(any(Measurement.class))).then(AdditionalAnswers.returnsFirstArg());
		when(mockMeasurementApi.createMeasurements(any(List.class))).then(AdditionalAnswers.returnsFirstArg());

		// behavior of mocked device credentials API
		when(mockDeviceCredentialsApi.getCredentials(DEVICE_ID)).thenReturn(deviceCredentials);
		when(mockDeviceCredentialsApi.getNewDeviceRequest(DEVICE_REQUEST_ID)).thenReturn(deviceRequest);

		// behavior of mocked device control API
		when(mockDeviceControlApi.createNewDevice(any(Operation.class))).then(AdditionalAnswers.returnsFirstArg());

		// behavior of mocked inventory API
		when(mockInventoryApi.create(any(ManagedObject.class))).then(AdditionalAnswers.returnsFirstArg());
		when(mockInventoryApi.get(MANAGED_OBJECT_ID)).thenReturn(managedObject);

		// behavior of mocked identity API
		when(mockIdentityApi.create(any(ExternalId.class))).then(AdditionalAnswers.returnsFirstArg());
		when(mockIdentityApi.getExternalId(any(ExternalId.class))).thenReturn(externalId);
	}
}
