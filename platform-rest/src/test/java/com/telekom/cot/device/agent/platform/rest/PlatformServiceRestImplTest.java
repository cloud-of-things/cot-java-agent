package com.telekom.cot.device.agent.platform.rest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.AgentCredentialsNotFoundException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.inventory.Binary;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.measurement.Measurement;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;
import com.telekom.m2m.cot.restsdk.util.Filter.FilterBuilder;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*") 
@PrepareForTest(CoTPlatformBuilder.class)
public class PlatformServiceRestImplTest extends PlatformServiceRestImplTestBase {

	private List<String> supportedOperations = Arrays.asList("c8y_Test");
	private Operation operation;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();

		PowerMockito.mockStatic(CoTPlatformBuilder.class);
		PowerMockito.when(CoTPlatformBuilder.create()).thenReturn(mockBuilder);
		
		operation = new Operation() {};
		operation.setId("1");
	}

	/**
	 * test method start
	 */

	@Test
	public void testStartFinishedBootstrappingMode() throws AbstractAgentException { // given are no agent credentials
		platformServiceRestImpl.start(); // no credentials found, so bootstrapping mode is true
		assertTrue(agentCredentials.isBootstrappingMode());
	}

	/**
	 * test method start
	 */
	@Test
	public void testStartBootstrappingMode() throws AbstractAgentException {
		platformServiceRestImpl.start();

		
		// verify
		PowerMockito.verifyStatic(CoTPlatformBuilder.class);
		CoTPlatformBuilder.create();
		verify(mockBuilder).setHostname("https://host/");
		verify(mockBuilder).setPassword("agent.password");
		verify(mockBuilder).setTenant("agent.tenant");
		verify(mockBuilder).setUsername("agent.username");
		verify(mockBuilder).build();
		assertTrue(agentCredentials.isBootstrappingMode());
		assertTrue(platformServiceRestImpl.isStarted());
	}

	/**
	 * test method start
	 */
	@Test
	public void testStartAgentCredentialsMode() throws AbstractAgentException {
		// behavior
		when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);

		// test
		platformServiceRestImpl.start();

		// verify
		PowerMockito.verifyStatic(CoTPlatformBuilder.class);
		CoTPlatformBuilder.create();
		verify(mockBuilder).setHostname("https://host/");
		verify(mockBuilder).setPassword("agent.password");
		verify(mockBuilder).setTenant("agent.tenant");
		verify(mockBuilder).setUsername("agent.username");
		verify(mockBuilder).build();

		Assert.assertThat(platformServiceRestImpl.isStarted(), Matchers.equalTo(true));
	}

	/**
	 * test method start
	 */
	@Test
	public void testStartAgentCredentialsNotExist() throws AbstractAgentException {
		// behavior
		doThrow(new AgentCredentialsNotFoundException("can't read agent credentials from credentials file"))
				.when(mockAgentCredentialsManager).getCredentials();

		// test
		try {
			platformServiceRestImpl.start();
			Assert.fail();
		} catch (AbstractAgentException agentException) {

		}

		// verify
		assertFalse(platformServiceRestImpl.isStarted());
	}

	/**
	 * test method start
	 */
	@Test
	public void testStartAgentCredentialsOneAttributeNotExist() throws AbstractAgentException {
		// behavior
		doThrow(new PlatformServiceException("some attributes are missing")).when(mockBuilder).build();

		// test
		try {
			platformServiceRestImpl.start();
			Assert.fail();
		} catch (AbstractAgentException agentException) {

		}

		// verify
		assertFalse(platformServiceRestImpl.isStarted());
	}

	/**
	 * test method stop
	 */
	@Test
	public void testStop() throws AbstractAgentException {
		platformServiceRestImpl.stop();

		assertFalse(platformServiceRestImpl.isStarted());
	}

	/*
	 * ---------- testCreateMeasurement ----------
	 */

	@Test
	public void testCreateMeasurement() throws AbstractAgentException {
		platformServiceRestImpl.createMeasurement(new Date(), "c8y_Temperature", 20, "�C");
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateMeasurementNullType() throws AbstractAgentException {
		platformServiceRestImpl.createMeasurement(new Date(), null, 20, "�C");
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateMeasurementNullUnit() throws AbstractAgentException {
		platformServiceRestImpl.createMeasurement(new Date(), "c8y_Temperature", 20, null);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateMeasurementCotSdkException() throws AbstractAgentException {
		reset(mockMeasurementApi);
		doThrow(new CotSdkException("test")).when(mockMeasurementApi).createMeasurement(any(Measurement.class));

		platformServiceRestImpl.createMeasurement(new Date(), "c8y_Temperature", 20, "�C");
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateMeasurementMeasurementApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		doReturn(null).when(mockCoTPlatform).getMeasurementApi();

		platformServiceRestImpl.createMeasurement(new Date(), "c8y_Temperature", 20, "�C");
	}

	/*
	 * ---------- testCreateMeasurements ----------
	 */

	@Test
	public void testCreateMeasurements() throws AbstractAgentException {
		List<SensorMeasurement> sensorMeasurements = new ArrayList<>();
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 20, "�C"));

		platformServiceRestImpl.createMeasurements(sensorMeasurements);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateMeasurementsNullMeasurements() throws AbstractAgentException {
		platformServiceRestImpl.createMeasurements(null);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateMeasurementsCotSdkException() throws AbstractAgentException {
		reset(mockMeasurementApi);
		List<SensorMeasurement> sensorMeasurements = new ArrayList<>();
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 20, "�C"));
		doThrow(new CotSdkException("test")).when(mockMeasurementApi).createMeasurements(any());

		platformServiceRestImpl.createMeasurements(sensorMeasurements);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateMeasurementsMeasurementApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		doReturn(null).when(mockCoTPlatform).getMeasurementApi();
		List<SensorMeasurement> sensorMeasurements = new ArrayList<>();
		sensorMeasurements.add(new SensorMeasurement("c8y_Temperature", 20, "�C"));
		platformServiceRestImpl.createMeasurements(sensorMeasurements);
	}

	/*
	 * ---------- testGetCredentials ----------
	 */

	@Test
	public void testGetCredentials() throws AbstractAgentException {
		AgentCredentials credentials = platformServiceRestImpl.getDeviceCredentials(DEVICE_ID, 10);

		assertEquals(DEVICE_TENANT, credentials.getTenant());
		assertEquals(DEVICE_USER, credentials.getUsername());
		assertEquals(DEVICE_PASSWORD, credentials.getPassword());
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetCredentialsNullDeviceId() throws AbstractAgentException {
		platformServiceRestImpl.getDeviceCredentials(null, 10);
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetCredentialsCotSdkException() throws AbstractAgentException {
		reset(mockDeviceCredentialsApi);
		doThrow(new CotSdkException("")).when(mockDeviceCredentialsApi).getCredentials(DEVICE_ID);

		platformServiceRestImpl.getDeviceCredentials(DEVICE_ID, 10);
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetCredentialsDeviceCredentialsApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		doReturn(null).when(mockCoTPlatform).getDeviceCredentialsApi();

		platformServiceRestImpl.getDeviceCredentials(DEVICE_ID, 10);
	}

	/*
	 * ---------- testCreateManagedObject ----------
	 */

	@Test
	public void testCreateAgentManagedObject() throws AbstractAgentException {
		AgentManagedObject agentManagedObject = new AgentManagedObject();

		assertEquals(agentManagedObject.getId(), platformServiceRestImpl.createAgentManagedObject(agentManagedObject));
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateAgentManagedObjectNullManagedObject() throws AbstractAgentException {
		platformServiceRestImpl.createAgentManagedObject(null);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateAgentManagedObjectCotSdkException() throws AbstractAgentException {
		reset(mockInventoryApi);
		doThrow(new CotSdkException("can't create managed object")).when(mockInventoryApi)
				.create(any(ManagedObject.class));

		platformServiceRestImpl.createAgentManagedObject(new AgentManagedObject());
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateAgentManagedObjectInventoryApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		doReturn(null).when(mockCoTPlatform).getInventoryApi();

		platformServiceRestImpl.createAgentManagedObject(new AgentManagedObject());
	}

	/*
	 * ---------- testCreateExternalId ----------
	 */

	@Test
	public void testCreateExternalId() throws AbstractAgentException {
		platformServiceRestImpl.createExternalId(MANAGED_OBJECT_ID);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateExternalIdNullExternalId() throws AbstractAgentException {
		platformServiceRestImpl.createExternalId(null);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateExternalIdCotSdkException() throws AbstractAgentException {
		reset(mockIdentityApi);
		doThrow(new CotSdkException("")).when(mockIdentityApi).create(any(ExternalId.class));

		platformServiceRestImpl.createExternalId(MANAGED_OBJECT_ID);
	}

	@Test(expected = PlatformServiceException.class)
	public void testCreateExternalIdIdentityApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		doReturn(null).when(mockCoTPlatform).getIdentityApi();

		platformServiceRestImpl.createExternalId(MANAGED_OBJECT_ID);
	}

	/*
	 * ---------- testIsExternalIdAvailable ----------
	 */

	@Test
	public void testIsExternalIdAvailable() throws AbstractAgentException {
		assertTrue(platformServiceRestImpl.isExternalIdAvailable());
	}

	@Test(expected = PlatformServiceException.class)
	public void testIsExternalIdAvailableCotSdkException() throws AbstractAgentException {
		reset(mockIdentityApi);
		doThrow(new CotSdkException("")).when(mockIdentityApi).getExternalId(any(ExternalId.class));

		platformServiceRestImpl.isExternalIdAvailable();
	}

	@Test(expected = PlatformServiceException.class)
	public void testIsExternalIdAvailableNullPointerException() throws AbstractAgentException {
		reset(mockCoTPlatform);
		doReturn(null).when(mockCoTPlatform).getIdentityApi();

		platformServiceRestImpl.isExternalIdAvailable();
	}

	/*
	 * ---------- testGetAgentManagedObject ----------
	 */

	@Test
	public void testGetAgentManagedObject() throws AbstractAgentException {
		assertEquals(managedObject.getId(), platformServiceRestImpl.getAgentManagedObject().getId());
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetManagedObjectCotSdkException() throws AbstractAgentException {
		reset(mockInventoryApi);
		doThrow(new CotSdkException("can't get managed object")).when(mockInventoryApi).get(MANAGED_OBJECT_ID);

		platformServiceRestImpl.getAgentManagedObject();
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetManagedObjectInventoryApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		when(mockCoTPlatform.getIdentityApi()).thenReturn(mockIdentityApi);
		doReturn(null).when(mockCoTPlatform).getInventoryApi();

		platformServiceRestImpl.getAgentManagedObject();
	}

	/*
	 * ---------- testUpdateAgentManagedObject ----------
	 */

	@Test
	public void testUpdateManagedObject() throws AbstractAgentException {

		AgentManagedObject agentManagedObject = new AgentManagedObject(managedObject.getAttributes());
		platformServiceRestImpl.updateAgentManagedObject(agentManagedObject);

		verify(mockInventoryApi).update(any(ManagedObject.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateManagedObjectNullManagedObject() throws AbstractAgentException {
		platformServiceRestImpl.updateAgentManagedObject(null);
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateManagedObjectCotSdkException() throws AbstractAgentException {
		reset(mockInventoryApi);
		doThrow(new CotSdkException("can't update managed object")).when(mockInventoryApi)
				.update(any(ManagedObject.class));

		AgentManagedObject agentManagedObject = new AgentManagedObject(managedObject.getAttributes());
		platformServiceRestImpl.updateAgentManagedObject(agentManagedObject);
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateAgentManagedObjectInventoryApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		when(mockCoTPlatform.getInventoryApi()).thenReturn(null);

		AgentManagedObject agentManagedObject = new AgentManagedObject(managedObject.getAttributes());
		platformServiceRestImpl.updateAgentManagedObject(agentManagedObject);
	}

	/*
	 * ---------- testUpdateSupportedOperations ----------
	 */

	@Test
	public void testUpdateSupportedOperations() throws AbstractAgentException {
		platformServiceRestImpl.updateSupportedOperations(supportedOperations);

		verify(mockInventoryApi).update(any(ManagedObject.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateSupportedOperationsNullSupportedOperations() throws AbstractAgentException {
		platformServiceRestImpl.updateSupportedOperations(null);
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateSupportedOperationsGetExternalIdException() throws AbstractAgentException {
		reset(mockIdentityApi);
		doThrow(new CotSdkException("test")).when(mockIdentityApi).getExternalId(any(ExternalId.class));

		platformServiceRestImpl.updateSupportedOperations(supportedOperations);
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateSupportedOperationsGetExternalIdNull() throws AbstractAgentException {
		reset(mockIdentityApi);
		when(mockIdentityApi.getExternalId(any(ExternalId.class))).thenReturn(null);

		platformServiceRestImpl.updateSupportedOperations(supportedOperations);
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateSupportedOperationsUpdateException() throws AbstractAgentException {
		reset(mockInventoryApi);
		doThrow(new CotSdkException("can't update")).when(mockInventoryApi).update(any(ManagedObject.class));

		platformServiceRestImpl.updateSupportedOperations(supportedOperations);
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateSupportedOperationsInventoryApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		when(mockCoTPlatform.getIdentityApi()).thenReturn(mockIdentityApi);
		when(mockCoTPlatform.getInventoryApi()).thenReturn(null);

		platformServiceRestImpl.updateSupportedOperations(supportedOperations);
	}
	
	/*
	 * ---------- testGetNextPendingOperation ----------
	 * poll
	 * isEmpty
	 * addAll(any)
	 */
	
	@Test
	public void testGetNextPendingOperation() throws AbstractAgentException {
		when(mockConcurrentLinkedQueue.isEmpty()).thenReturn(false);
		when(mockConcurrentLinkedQueue.poll()).thenReturn(mockPendingOperation);
		when(mockPendingOperation.getStatus()).thenReturn(OperationStatus.PENDING);
		
		Operation operation = platformServiceRestImpl.getNextPendingOperation();

		verify(mockConcurrentLinkedQueue).isEmpty();
		verify(mockConcurrentLinkedQueue).poll();
		
		Assert.assertTrue(operation.getStatus().equals(OperationStatus.PENDING));
	}
	
	@Test
	public void testGetNextPendingOperationQueueEmpty_RequestTrue() throws AbstractAgentException {
		when(mockConcurrentLinkedQueue.isEmpty()).thenReturn(true);
		when(mockConcurrentLinkedQueue.poll()).thenReturn(mockPendingOperation);
		when(mockPendingOperation.getStatus()).thenReturn(OperationStatus.PENDING);
		when(mockOperation.getStatus()).thenReturn(com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus.PENDING);
		
		Operation operation = platformServiceRestImpl.getNextPendingOperation();
		
		verify(mockConcurrentLinkedQueue).isEmpty();
		verify(mockConcurrentLinkedQueue).addAll(any());
		verify(mockConcurrentLinkedQueue).poll();
		
		Assert.assertTrue(operation.getStatus().equals(OperationStatus.PENDING));
	}
	
	@Test
	public void testGetNextPendingOperationQueueEmpty_RequestFalse() throws AbstractAgentException {
		when(mockConcurrentLinkedQueue.isEmpty()).thenReturn(true);
		when(mockConcurrentLinkedQueue.poll()).thenReturn(null);
		when(mockPendingOperation.getStatus()).thenReturn(OperationStatus.PENDING);
		when(mockOperation.getStatus()).thenReturn(com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus.PENDING);
		
		Operation operation = platformServiceRestImpl.getNextPendingOperation();
		
		verify(mockConcurrentLinkedQueue).isEmpty();
		verify(mockConcurrentLinkedQueue).addAll(any());
		verify(mockConcurrentLinkedQueue).poll();
		
		Assert.assertNull(operation);
	}

	/*
	 * ---------- testGetOperations ----------
	 */

	@Test 
	public void testGetOperations() throws AbstractAgentException {
		when(mockOperation.getStatus()).thenReturn(com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus.ACCEPTED);
		
		platformServiceRestImpl.getOperations(OperationStatus.ACCEPTED);
		
		verify(mockDeviceControlApi).getOperationCollection(any(FilterBuilder.class), any(Integer.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetOperationsNullStatus() throws AbstractAgentException {
		platformServiceRestImpl.getOperations(null);
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetOperationsExternalIdNull() throws AbstractAgentException {
		reset(mockIdentityApi);
		when(mockIdentityApi.getExternalId(externalId)).thenReturn(null);

		platformServiceRestImpl.getOperations(OperationStatus.ACCEPTED);
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetOperationsGetExternalIdException() throws AbstractAgentException {
		reset(mockIdentityApi);
		doThrow(new CotSdkException("test")).when(mockIdentityApi).getExternalId(externalId);

		platformServiceRestImpl.getOperations(OperationStatus.ACCEPTED);
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetOperationCollectionDeviceControlApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		when(mockCoTPlatform.getDeviceControlApi()).thenReturn(null);

		platformServiceRestImpl.getOperations(OperationStatus.ACCEPTED);
	}

	@Test(expected = PlatformServiceException.class)
	public void testGetOperationCollectionGetOperationCollectionExc() throws AbstractAgentException {
		reset(mockDeviceControlApi);
		doThrow(new CotSdkException("test")).when(mockDeviceControlApi).getOperationCollection(any(FilterBuilder.class),
				any(Integer.class));

		platformServiceRestImpl.getOperations(OperationStatus.ACCEPTED);
	}

	/*
	 * ---------- testUpdateOperationStatus ----------
	 */

	@Test
	public void testUpdateOperationStatus() throws AbstractAgentException {
		platformServiceRestImpl.updateOperationStatus(operation.getId(), OperationStatus.EXECUTING);

		verify(mockDeviceControlApi).update(any(com.telekom.m2m.cot.restsdk.devicecontrol.Operation.class));
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateOperationStatusNoId() throws AbstractAgentException {
		platformServiceRestImpl.updateOperationStatus(null, OperationStatus.EXECUTING);
	}

    @Test(expected = PlatformServiceException.class)
    public void testUpdateOperationStatusEmptyId() throws AbstractAgentException {
        platformServiceRestImpl.updateOperationStatus("", OperationStatus.EXECUTING);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateOperationStatusNoStatus() throws AbstractAgentException {
        platformServiceRestImpl.updateOperationStatus(operation.getId(), null);
    }

	@Test(expected = PlatformServiceException.class)
	public void testUpdateOperationUpdateStatusExc() throws AbstractAgentException {
		reset(mockDeviceControlApi);
		doThrow(new CotSdkException("test")).when(mockDeviceControlApi).update(any(com.telekom.m2m.cot.restsdk.devicecontrol.Operation.class));

        platformServiceRestImpl.updateOperationStatus(operation.getId(), OperationStatus.EXECUTING);
	}

	@Test(expected = PlatformServiceException.class)
	public void testUpdateOperationStatusDeviceControlApiNull() throws AbstractAgentException {
		reset(mockCoTPlatform);
		when(mockCoTPlatform.getDeviceControlApi()).thenReturn(null);

        platformServiceRestImpl.updateOperationStatus(operation.getId(), OperationStatus.EXECUTING);
	}
	

	/*
     * ---------- testDownloadBinary ----------
     */

	@Test(expected = PlatformServiceException.class)
	public void testDownloadBinaryNoUrl() throws Exception {
	    platformServiceRestImpl.downloadBinary(null);
	}

    @Test(expected = PlatformServiceException.class)
    public void testDownloadBinaryNoUrlPath() throws Exception {
        platformServiceRestImpl.downloadBinary(new URL("https://asterix.ram.m2m.telekom.com"));
    }

    @Test(expected = PlatformServiceException.class)
    public void testDownloadBinaryNoBinaryId() throws Exception {
        platformServiceRestImpl.downloadBinary(new URL("https://asterix.ram.m2m.telekom.com/"));
    }

    @Test(expected = PlatformServiceException.class)
    public void testDownloadBinaryNoBinariesApi() throws Exception {
        doReturn(null).when(mockCoTPlatform).getBinariesApi();
        
        platformServiceRestImpl.downloadBinary(new URL("https://asterix.ram.m2m.telekom.com/inventory/binaries/123456"));
    }

    @Test(expected = PlatformServiceException.class)
    public void testDownloadBinaryNoData() throws Exception {
        doReturn(null).when(mockBinariesApi).getData(any(Binary.class));
        
        platformServiceRestImpl.downloadBinary(new URL("https://asterix.ram.m2m.telekom.com/inventory/binaries/123456"));
    }

    @Test
    public void testDownloadBinaryEmptyData() throws Exception {
        when(mockBinariesApi.getData(any(Binary.class))).thenReturn(new byte[0]);
        
        byte[] data = platformServiceRestImpl.downloadBinary(new URL("https://asterix.ram.m2m.telekom.com/inventory/binaries/123456"));
        assertNotNull(data);
        assertEquals(0, data.length);
    }

    @Test
    public void testDownloadBinary() throws Exception {
        Binary binary = new Binary("123456");
        byte[] data = new byte[] { 7, 3, 1, 6, 9 };
        when(mockBinariesApi.getData(binary)).thenReturn(data);
        
        byte[] resultData = platformServiceRestImpl.downloadBinary(new URL("https://asterix.ram.m2m.telekom.com/inventory/binaries/123456"));
        assertNotNull(resultData);
        assertSame(data, resultData);
    }
}
