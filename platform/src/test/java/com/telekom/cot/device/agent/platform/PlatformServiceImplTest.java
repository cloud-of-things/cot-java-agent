package com.telekom.cot.device.agent.platform;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.ConfigurationNotFoundException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SupportedOperations;
import com.telekom.m2m.cot.restsdk.measurement.Measurement;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;
import com.telekom.m2m.cot.restsdk.util.Filter.FilterBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CoTPlatformBuilder.class)
public class PlatformServiceImplTest extends PlatformServiceImplTestBase {

    private SupportedOperations supportedOperations = new SupportedOperations("c8y_Test");
    private Operation operation = new Operation("1");

    @Before
    public void setUp() throws Exception {
        super.setUp();

        PowerMockito.mockStatic(CoTPlatformBuilder.class);
        PowerMockito.when(CoTPlatformBuilder.create()).thenReturn(mockBuilder);
    }

    /**
     * test method start
     */
/*    @Test
    public void testStartFinishedBootstrappingMode() throws AbstractAgentException {
        // given are no agent credentials
        when(mockAgentCredentialsManager.getCredentials()).thenThrow(new AgentCredentialsNotFoundException("test"));
        // when start
        platformServiceImpl.start();
        // then bootstrapping mode is true
        Assert.assertThat(platformServiceImpl.isBootstrapping(), Matchers.equalTo(true));
        // when stop
        platformServiceImpl.stop();
        // then bootstrapping mode is true
        Assert.assertThat(platformServiceImpl.isBootstrapping(), Matchers.equalTo(true));

        // given are agent credentials
        reset(mockConfigurationManager);
        when(mockConfigurationManager.getConfiguration(PlatformServiceConfiguration.class)).thenReturn(platformServiceConfig);
        when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
        // when the second start
        platformServiceImpl.start();
        // then bootstrapping mode is false
        Assert.assertThat(platformServiceImpl.isBootstrapping(), Matchers.equalTo(false));
    }
*/
    /**
     * test method start
     */
/*    @Test
    public void testStartIncorrectlyBootstrappingMode() throws AbstractAgentException {
        // given are no agent credentials
        when(mockAgentCredentialsManager.getCredentials()).thenThrow(new AgentCredentialsNotFoundException("test"));
        // when start
        platformServiceImpl.start();
        // then bootstrapping mode is true
        Assert.assertThat(platformServiceImpl.isBootstrapping(), Matchers.equalTo(true));
        // when stop
        platformServiceImpl.stop();
        // then bootstrapping mode is true
        Assert.assertThat(platformServiceImpl.isBootstrapping(), Matchers.equalTo(true));
        // when the second start with no agent credentials
        try {
            // then the second start failed
            platformServiceImpl.start();
            Assert.fail();
        } catch (PlatformServiceException exception) {
            Assert.assertThat(exception.getMessage(), Matchers.equalTo("no credentials available bootstrapping mode=true"));
        }
        // then bootstrapping mode is true
        Assert.assertThat(platformServiceImpl.isBootstrapping(), Matchers.equalTo(true));
    }
*/
    /**
     * test method start
     */
/*    @Test
    public void testStartBootstrappingMode() throws AbstractAgentException {
        // behavior
        when(mockAgentCredentialsManager.getCredentials()).thenThrow(new AgentCredentialsNotFoundException("test"));
        // test
        platformServiceImpl.start();
        // verify
        PowerMockito.verifyStatic(CoTPlatformBuilder.class);
        CoTPlatformBuilder.create();
        verify(mockBuilder).setHostname("https://host/");
        verify(mockBuilder).setPassword("boot.password");
        verify(mockBuilder).setTenant("boot.tenant");
        verify(mockBuilder).setUsername("boot.username");
        verify(mockBuilder).build();
        Assert.assertThat(platformServiceImpl.isBootstrapping(), Matchers.equalTo(true));
        Assert.assertThat(platformServiceImpl.isStarted(), Matchers.equalTo(true));
    }
*/
    /**
     * test method start
     */
    @Test
    public void testStartAgentCredentialsMode() throws AbstractAgentException {
        // behavior
        when(mockAgentCredentialsManager.getCredentials()).thenReturn(agentCredentials);
        // test
        platformServiceImpl.start();
        // verify
        PowerMockito.verifyStatic(CoTPlatformBuilder.class);
        CoTPlatformBuilder.create();
        verify(mockBuilder).setHostname("https://host/");
        verify(mockBuilder).setPassword("agent.password");
        verify(mockBuilder).setTenant("agent.tenant");
        verify(mockBuilder).setUsername("agent.username");
        verify(mockBuilder).build();
//        Assert.assertThat(platformServiceImpl.isBootstrapping(), Matchers.equalTo(false));
        Assert.assertThat(platformServiceImpl.isStarted(), Matchers.equalTo(true));
    }

    /**
     * test method start
     */
    @Test
    public void testStartPlatformServiceConfigNotExist() throws AbstractAgentException {
        // behavior
        when(mockConfigurationManager.getConfiguration(PlatformServiceConfiguration.class)).thenThrow(new ConfigurationNotFoundException("test"));
        // test
        try {
            platformServiceImpl.start();
            Assert.fail();
        } catch (AbstractAgentException agentException) {

        }
        Assert.assertThat(platformServiceImpl.isStarted(), Matchers.equalTo(false));
    }

    /**
     * test method start
     */
    @Test
    public void testStartAgentCredentialsNotExist() throws AbstractAgentException {
        // behavior
        doThrow(new ConfigurationNotFoundException("not found")).when(mockConfigurationManager).getConfiguration(PlatformServiceConfiguration.class);

        // test
        try {
            platformServiceImpl.start();
            Assert.fail();
        } catch (AbstractAgentException agentException) {

        }
        
        assertFalse(platformServiceImpl.isStarted());
    }

    /**
     * test method start
     */
    @Test
    public void testStartAgentCredentialsOneAttributeNotExist() throws AbstractAgentException {
        // behavior
        doThrow(new PlatformServiceException("test")).when(mockBuilder).build();
        
        // test
        try {
            platformServiceImpl.start();
            Assert.fail();
        } catch (AbstractAgentException agentException) {

        }

        assertFalse(platformServiceImpl.isStarted());
    }

    /**
     * test method stop
     */
    @Test
    public void testStop() throws AbstractAgentException {
        platformServiceImpl.stop();

        assertFalse(platformServiceImpl.isStarted());
    }

    /*
     * ---------- testCreateMeasurement ----------
     */

    @Test
    public void testCreateMeasurement() throws AbstractAgentException {
        Measurement measurement = new Measurement();
        // test
        assertEquals(measurement, platformServiceImpl.createMeasurement(measurement));
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateMeasurementNullMeasurement() throws AbstractAgentException {
        platformServiceImpl.createMeasurement(null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateMeasurementCotSdkException() throws AbstractAgentException {
        reset(mockMeasurementApi);
        doThrow(new CotSdkException("test")).when(mockMeasurementApi).createMeasurement(any(Measurement.class));

        platformServiceImpl.createMeasurement(new Measurement());
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateMeasurementMeasurementApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        doReturn(null).when(mockCoTPlatform).getMeasurementApi();

        platformServiceImpl.createMeasurement(new Measurement());
    }

    /*
     * ---------- testCreateMeasurements ----------
     */

    @Test
    public void testCreateMeasurements() throws AbstractAgentException {
        Measurement measurement = new Measurement();
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(measurement);

        List<Measurement> result = platformServiceImpl.createMeasurements(measurements);

        assertEquals(measurements, result);
        assertEquals(1, result.size());
        assertEquals(measurement, result.get(0));
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateMeasurementsNullMeasurements() throws AbstractAgentException {
        platformServiceImpl.createMeasurements(null);
    }

    @Test
    public void testCreateMeasurementsEmpty() throws AbstractAgentException {
        List<Measurement> measurements = new ArrayList<>();

        List<Measurement> result = platformServiceImpl.createMeasurements(measurements);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateMeasurementsCotSdkException() throws AbstractAgentException {
        reset(mockMeasurementApi);
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(new Measurement());
        doThrow(new CotSdkException("test")).when(mockMeasurementApi).createMeasurements(measurements);

        platformServiceImpl.createMeasurements(measurements);
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateMeasurementsMeasurementApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        doReturn(null).when(mockCoTPlatform).getMeasurementApi();
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(new Measurement());
        platformServiceImpl.createMeasurements(measurements);
    }

    /*
     * ---------- testGetCredentials ----------
     */

    @Test
    public void testGetCredentials() throws AbstractAgentException {
        AgentCredentials credentials = platformServiceImpl.getDeviceCredentials(DEVICE_ID);

        assertEquals(DEVICE_TENANT, credentials.getTenant());
        assertEquals(DEVICE_USER, credentials.getUsername());
        assertEquals(DEVICE_PASSWORD, credentials.getPassword());
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetCredentialsNullDeviceId() throws AbstractAgentException {
        platformServiceImpl.getDeviceCredentials(null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetCredentialsCotSdkException() throws AbstractAgentException {
        reset(mockDeviceCredentialsApi);
        doThrow(new CotSdkException("")).when(mockDeviceCredentialsApi).getCredentials(DEVICE_ID);

        platformServiceImpl.getDeviceCredentials(DEVICE_ID);
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetCredentialsDeviceCredentialsApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        doReturn(null).when(mockCoTPlatform).getDeviceCredentialsApi();

        platformServiceImpl.getDeviceCredentials(DEVICE_ID);
    }

    /*
     * ---------- testCreateNewDevice ----------
     */

    @Test
    public void testCreateNewDevice() throws AbstractAgentException {
        Operation operation = new Operation();

        assertEquals(operation, platformServiceImpl.createNewDevice(operation));
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateNewDeviceNullOperation() throws AbstractAgentException {
        platformServiceImpl.createNewDevice(null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateNewDeviceCotSdkException() throws AbstractAgentException {
        reset(mockDeviceControlApi);
        doThrow(new CotSdkException("")).when(mockDeviceControlApi).createNewDevice(any(Operation.class));

        platformServiceImpl.createNewDevice(new Operation());
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateNewDeviceDeviceControlApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        doReturn(null).when(mockCoTPlatform).getDeviceControlApi();

        platformServiceImpl.createNewDevice(new Operation());
    }

    /*
     * ---------- testCreateManagedObject ----------
     */

    @Test
    public void testCreateManagedObject() throws AbstractAgentException {
        ManagedObject managedObject = new ManagedObject();

        assertEquals(managedObject, platformServiceImpl.createManagedObject(managedObject));
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateManagedObjectNullManagedObject() throws AbstractAgentException {
        platformServiceImpl.createManagedObject(null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateManagedObjectCotSdkException() throws AbstractAgentException {
        reset(mockInventoryApi);
        doThrow(new CotSdkException("can't create managed object")).when(mockInventoryApi).create(any(ManagedObject.class));

        platformServiceImpl.createManagedObject(new ManagedObject());
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateManagedObjectInventoryApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        doReturn(null).when(mockCoTPlatform).getInventoryApi();

        platformServiceImpl.createManagedObject(new ManagedObject());
    }

    /*
     * ---------- testCreateExternalId ----------
     */

    @Test
    public void testCreateExternalId() throws AbstractAgentException {
        ExternalId actualExternalId = platformServiceImpl.createExternalId(MANAGED_OBJECT_ID);
        assertEquals(actualExternalId.getExternalId(), externalId.getExternalId());
        assertEquals(actualExternalId.getType(), externalId.getType());
        assertEquals(actualExternalId.getManagedObject().getId(), externalId.getManagedObject().getId());
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateExternalIdNullExternalId() throws AbstractAgentException {
        platformServiceImpl.createExternalId(null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateExternalIdCotSdkException() throws AbstractAgentException {
        reset(mockIdentityApi);
        doThrow(new CotSdkException("")).when(mockIdentityApi).create(any(ExternalId.class));

        platformServiceImpl.createExternalId(MANAGED_OBJECT_ID);
    }

    @Test(expected = PlatformServiceException.class)
    public void testCreateExternalIdIdentityApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        doReturn(null).when(mockCoTPlatform).getIdentityApi();

        platformServiceImpl.createExternalId(MANAGED_OBJECT_ID);
    }

    /*
     * ---------- testGetExternalId ----------
     */

    @Test
    public void testGetExternalId() throws AbstractAgentException {
        // ExternalId externalId = new ExternalId();

        assertEquals(externalId, platformServiceImpl.getExternalId());
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetExternalIdCotSdkException() throws AbstractAgentException {
        reset(mockIdentityApi);
        doThrow(new CotSdkException("")).when(mockIdentityApi).getExternalId(any(ExternalId.class));

        platformServiceImpl.getExternalId();
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetExternalIdNullPointerException() throws AbstractAgentException {
        reset(mockCoTPlatform);
        doReturn(null).when(mockCoTPlatform).getIdentityApi();

        platformServiceImpl.getExternalId();
    }

    /*
     * ---------- testGetManagedObject ----------
     */

    @Test
    public void testGetManagedObject() throws AbstractAgentException {
        assertEquals(managedObject, platformServiceImpl.getManagedObject());
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetManagedObjectCotSdkException() throws AbstractAgentException {
        reset(mockInventoryApi);
        doThrow(new CotSdkException("can't get managed object")).when(mockInventoryApi).get(MANAGED_OBJECT_ID);

        platformServiceImpl.getManagedObject();
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetManagedObjectInventoryApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        when(mockCoTPlatform.getIdentityApi()).thenReturn(mockIdentityApi);
        doReturn(null).when(mockCoTPlatform).getInventoryApi();

        platformServiceImpl.getManagedObject();
    }

    /*
     * ---------- testUpdateManagedObject ----------
     */

    @Test
    public void testUpdateManagedObject() throws AbstractAgentException {
        platformServiceImpl.updateManagedObject(managedObject);

        verify(mockInventoryApi).update(managedObject);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateManagedObjectNullManagedObject() throws AbstractAgentException {
        platformServiceImpl.updateManagedObject(null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateManagedObjectCotSdkException() throws AbstractAgentException {
        reset(mockInventoryApi);
        doThrow(new CotSdkException("can't update managed object")).when(mockInventoryApi).update(any(ManagedObject.class));

        platformServiceImpl.updateManagedObject(managedObject);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateManagedObjectInventoryApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        when(mockCoTPlatform.getInventoryApi()).thenReturn(null);

        platformServiceImpl.updateManagedObject(managedObject);
    }

    /*
     * ---------- testUpdateSupportedOperations ----------
     */

    @Test
    public void testUpdateSupportedOperations() throws AbstractAgentException {
        platformServiceImpl.updateSupportedOperations(supportedOperations);

        verify(mockInventoryApi).update(any(ManagedObject.class));
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateSupportedOperationsNullSupportedOperations() throws AbstractAgentException {
        platformServiceImpl.updateSupportedOperations(null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateSupportedOperationsGetExternalIdException() throws AbstractAgentException {
        reset(mockIdentityApi);
        doThrow(new CotSdkException("test")).when(mockIdentityApi).getExternalId(any(ExternalId.class));

        platformServiceImpl.updateSupportedOperations(supportedOperations);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateSupportedOperationsGetExternalIdNull() throws AbstractAgentException {
        reset(mockIdentityApi);
        when(mockIdentityApi.getExternalId(any(ExternalId.class))).thenReturn(null);

        platformServiceImpl.updateSupportedOperations(supportedOperations);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateSupportedOperationsUpdateException() throws AbstractAgentException {
        reset(mockInventoryApi);
        doThrow(new CotSdkException("can't update")).when(mockInventoryApi).update(any(ManagedObject.class));

        platformServiceImpl.updateSupportedOperations(supportedOperations);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateSupportedOperationsInventoryApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        when(mockCoTPlatform.getIdentityApi()).thenReturn(mockIdentityApi);
        when(mockCoTPlatform.getInventoryApi()).thenReturn(null);

        platformServiceImpl.updateSupportedOperations(supportedOperations);
    }

    /*
     * ---------- testGetOperationCollection ----------
     */

    @Test
    public void testGetOperationCollection() throws AbstractAgentException {
        platformServiceImpl.getOperationCollection(OperationStatus.ACCEPTED, 1);

        verify(mockDeviceControlApi).getOperationCollection(any(FilterBuilder.class), any(Integer.class));
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetOperationCollectionNullStatus() throws AbstractAgentException {
        platformServiceImpl.getOperationCollection(null, 1);
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetOperationCollectionNullResultSize() throws AbstractAgentException {
        platformServiceImpl.getOperationCollection(OperationStatus.ACCEPTED, null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetOperationCollectionExternalIdNull() throws AbstractAgentException {
        reset(mockIdentityApi);
        when(mockIdentityApi.getExternalId(externalId)).thenReturn(null);

        platformServiceImpl.getOperationCollection(OperationStatus.ACCEPTED, 1);
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetOperationCollectionGetExternalIdException() throws AbstractAgentException {
        reset(mockIdentityApi);
        doThrow(new CotSdkException("test")).when(mockIdentityApi).getExternalId(externalId);

        platformServiceImpl.getOperationCollection(OperationStatus.ACCEPTED, 1);
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetOperationCollectionDeviceControlApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        when(mockCoTPlatform.getDeviceControlApi()).thenReturn(null);

        platformServiceImpl.getOperationCollection(OperationStatus.ACCEPTED, 1);
    }

    @Test(expected = PlatformServiceException.class)
    public void testGetOperationCollectionGetOperationCollectionExc() throws AbstractAgentException {
        reset(mockDeviceControlApi);
        doThrow(new CotSdkException("test")).when(mockDeviceControlApi).getOperationCollection(any(FilterBuilder.class), any(Integer.class));

        platformServiceImpl.getOperationCollection(OperationStatus.ACCEPTED, 1);
    }

    /*
     * ---------- testUpdateOperation ----------
     */

    @Test
    public void testUpdateOperation() throws AbstractAgentException {
        platformServiceImpl.updateOperation(operation);

        verify(mockDeviceControlApi).update(operation);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateOperationNullOperation() throws AbstractAgentException {
        platformServiceImpl.updateOperation(null);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateOperationUpdateExc() throws AbstractAgentException {
        reset(mockDeviceControlApi);
        doThrow(new CotSdkException("test")).when(mockDeviceControlApi).update(operation);

        platformServiceImpl.updateOperation(operation);
    }

    @Test(expected = PlatformServiceException.class)
    public void testUpdateOperationDeviceControlApiNull() throws AbstractAgentException {
        reset(mockCoTPlatform);
        when(mockCoTPlatform.getDeviceControlApi()).thenReturn(null);

        platformServiceImpl.updateOperation(operation);
    }
}
