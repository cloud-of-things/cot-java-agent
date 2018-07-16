package com.telekom.cot.device.agent.platform.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.injection.InjectionUtil;
import com.telekom.cot.device.agent.platform.PlatformServiceConfiguration.ExternalIdConfig;
import com.telekom.cot.device.agent.platform.objects.Operation;
import com.telekom.cot.device.agent.platform.rest.PlatformServiceRestConfiguration.RestConfiguration;
import com.telekom.cot.device.agent.service.AgentServiceProvider;
import com.telekom.m2m.cot.restsdk.CloudOfThingsPlatform;
import com.telekom.m2m.cot.restsdk.alarm.AlarmApi;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceControlApi;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceCredentials;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceCredentialsApi;
import com.telekom.m2m.cot.restsdk.devicecontrol.NewDeviceRequest;
import com.telekom.m2m.cot.restsdk.event.EventApi;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.identity.IdentityApi;
import com.telekom.m2m.cot.restsdk.inventory.BinariesApi;
import com.telekom.m2m.cot.restsdk.inventory.InventoryApi;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.measurement.Measurement;
import com.telekom.m2m.cot.restsdk.measurement.MeasurementApi;
import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;
import com.telekom.m2m.cot.restsdk.util.Filter.FilterBuilder;

class PlatformServiceRestImplTestBase {

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
	@Mock protected BinariesApi mockBinariesApi;
	@Mock protected AgentServiceProvider mockAgentServiceProvider;
	@Mock protected AgentCredentialsManager mockAgentCredentialsManager;
	@Mock protected CoTPlatformBuilder mockBuilder;
	@Mock protected com.telekom.m2m.cot.restsdk.devicecontrol.OperationCollection mockOperationCollection;	
	@Mock protected com.telekom.m2m.cot.restsdk.devicecontrol.Operation mockOperation;
    @Mock protected ConcurrentLinkedQueue<com.telekom.cot.device.agent.platform.objects.Operation> mockConcurrentLinkedQueue;
    @Mock protected Operation mockPendingOperation;

	protected PlatformServiceRestConfiguration platformServiceConfig = new PlatformServiceRestConfiguration();
	protected ExternalId externalId = new ExternalId();
	protected ManagedObject managedObject = new ManagedObject();
	protected NewDeviceRequest deviceRequest = new NewDeviceRequest(new ExtensibleObject());
	protected DeviceCredentials deviceCredentials = new DeviceCredentials();
	protected AgentCredentials agentCredentials = new AgentCredentials(true, "agent.tenant", "agent.username", "agent.password");

	protected PlatformServiceRestImpl platformServiceRestImpl = new PlatformServiceRestImpl();

	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		// initialize mocks
		MockitoAnnotations.initMocks(this);

		// inject mocks
		InjectionUtil.injectStatic(PlatformServiceRestImpl.class, mockLogger);
		InjectionUtil.inject(platformServiceRestImpl, mockCoTPlatform);
		InjectionUtil.inject(platformServiceRestImpl, mockAgentServiceProvider);
		InjectionUtil.inject(platformServiceRestImpl, mockAgentCredentialsManager);
		InjectionUtil.inject(platformServiceRestImpl, externalId);
		InjectionUtil.inject(platformServiceRestImpl, mockConcurrentLinkedQueue);
		

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
		
		// platform config setup and inject
		platformServiceConfig.setHostName("https://host/");
		platformServiceConfig.setExternalIdConfig(new ExternalIdConfig());
		platformServiceConfig.getExternalIdConfig().setValue(EXTERNAL_ID);
		platformServiceConfig.getExternalIdConfig().setType(EXTERNAL_ID_TYPE);
		platformServiceConfig.setRestConfiguration(new RestConfiguration());
        platformServiceConfig.getRestConfiguration().setProxyHost("0.0.0.1");
        platformServiceConfig.getRestConfiguration().setProxyPort("8080");
        platformServiceConfig.getRestConfiguration().setOperationsRequestSize(10);
		InjectionUtil.inject(platformServiceRestImpl, platformServiceConfig);

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
		when(mockCoTPlatform.getBinariesApi()).thenReturn(mockBinariesApi);

		// behavior of mocked measurement API
		when(mockMeasurementApi.createMeasurement(any(Measurement.class))).then(AdditionalAnswers.returnsFirstArg());
		when(mockMeasurementApi.createMeasurements(any(List.class))).then(AdditionalAnswers.returnsFirstArg());

		// behavior of mocked device credentials API
		when(mockDeviceCredentialsApi.getCredentials(DEVICE_ID)).thenReturn(deviceCredentials);
		when(mockDeviceCredentialsApi.getNewDeviceRequest(DEVICE_REQUEST_ID)).thenReturn(deviceRequest);

		// behavior of mocked device control API
		when(mockDeviceControlApi.getOperationCollection(any(FilterBuilder.class), any(Integer.class))).thenReturn(mockOperationCollection);

		// behavior of mocked inventory API
		when(mockInventoryApi.create(any(ManagedObject.class))).then(AdditionalAnswers.returnsFirstArg());
		when(mockInventoryApi.get(MANAGED_OBJECT_ID)).thenReturn(managedObject);

		// behavior of mocked identity API
		when(mockIdentityApi.create(any(ExternalId.class))).then(AdditionalAnswers.returnsFirstArg());
		when(mockIdentityApi.getExternalId(any(ExternalId.class))).thenReturn(externalId);
		
		// behavior of mocked operation collection (REST SDK)
		when(mockOperationCollection.getOperations()).thenReturn(new com.telekom.m2m.cot.restsdk.devicecontrol.Operation[] { mockOperation });
		when(mockOperationCollection.hasNext()).thenReturn(false);
	}
}
