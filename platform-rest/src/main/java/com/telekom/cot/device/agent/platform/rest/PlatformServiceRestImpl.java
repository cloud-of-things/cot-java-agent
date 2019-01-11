package com.telekom.cot.device.agent.platform.rest;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.PlatformServiceConfiguration.ExternalIdConfig;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.OperationFactory;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;
import com.telekom.m2m.cot.restsdk.CloudOfThingsPlatform;
import com.telekom.m2m.cot.restsdk.alarm.Alarm;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceCredentials;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationCollection;
import com.telekom.m2m.cot.restsdk.event.Event;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.inventory.Binary;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SupportedOperations;
import com.telekom.m2m.cot.restsdk.measurement.Measurement;
import com.telekom.m2m.cot.restsdk.measurement.MeasurementReading;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;
import com.telekom.m2m.cot.restsdk.util.Filter.FilterBuilder;
import com.telekom.m2m.cot.restsdk.util.FilterBy;

/**
 * The platform REST service.
 *
 */
public class PlatformServiceRestImpl extends AbstractAgentService implements PlatformService {

	private static final String ERROR_CREATE_MEASUREMENT = "can't create measurement";
	private static final String ERROR_GET_OPERATION_COLLECTION = "can't get the requested operation collection";
    private static final int HTTP_NOT_FOUND = 404;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformServiceRestImpl.class);

	@Inject
	private PlatformServiceRestConfiguration configuration;
	@Inject
	private SystemService systemService;
    @Inject
    private AgentCredentialsManager credentialsManager;
	
	private CloudOfThingsPlatform cotPlatform = null;
	private ExternalId externalId;
	private ConcurrentLinkedQueue<Operation> pendingOperations = new ConcurrentLinkedQueue<>();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws AbstractAgentException {
		// create external id
		createExternalId();

		// configure proxy settings
		configureProxySettings();

		// get agent credentials
		AgentCredentials agentCredentials = credentialsManager.getCredentials();

		// create CoT platform
		cotPlatform = CoTPlatformBuilder.create().setHostname(configuration.getHostName())
				.setTenant(agentCredentials.getTenant()).setUsername(agentCredentials.getUsername())
				.setPassword(agentCredentials.getPassword()).build();

        pendingOperations.clear();
        super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stop() throws AbstractAgentException {
		cotPlatform = null;
        pendingOperations.clear();
		super.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExternalIdValue() throws AbstractAgentException {
		// get and check external id config from platform service config
		ExternalIdConfig externalIdConfig = getExternalIdConfig();

		String value = externalIdConfig.getValue();
		ExternalIdConfig.ValueTemplates valueTemplate = externalIdConfig.getValueTemplate();

		// return value if only value is set
		if (StringUtils.isNotEmpty(value)
				&& (Objects.isNull(valueTemplate) || valueTemplate == ExternalIdConfig.ValueTemplates.NO_TEMPLATE)) {
			return value;
		}

		// generate value if only template is set
		if (Objects.isNull(value) && Objects.nonNull(valueTemplate)) {
			// get hardware serial number
			String hardwareSerial = systemService.getProperties(HardwareProperties.class)
					.getSerialNumber();

			switch (valueTemplate) {
			case HARDWARE_SERIAL:
				return hardwareSerial;

			case TYPE_HARDWARE_SERIAL:
				return externalIdConfig.getType() + "-" + hardwareSerial;

			default:
				throw AssertionUtil.createExceptionAndLog(PlatformServiceException.class, LOGGER,
						"not supported value template");
			}
		}

		// only one property is allowed to be set: 'value' or 'valueTemplate'
		throw AssertionUtil.createExceptionAndLog(PlatformServiceException.class, LOGGER,
				"only one property at external id configuration is allowed: 'value' or 'valueTemplate'");
	}

	/**
	 * {@inheritDoc}
	 */
	public void createEvent(Date time, String type, String text, String condition)
			throws AbstractAgentException {
		// check time
		Date eventTime = Objects.nonNull(time) ? time : new Date();

		// check type and text
		assertNotEmpty(type, "can't create event, type is null or empty");
		assertNotEmpty(text, "can't create event, text is null or empty");

		LOGGER.info("create event (time={}, type={}, text={})", eventTime, type, text);

		// create new event object
		Event event = new Event();
		event.setSource(getManagedObject());
		event.setTime(eventTime);
		event.setType(type);
		event.setText(text);
		event.set(condition, "");

		// create event at platform
		try {
			cotPlatform.getEventApi().createEvent(event);
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), "could not create event", exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException("could not create event", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createAlarm(Date time, String type, AlarmSeverity severity, String text, String status)
			throws AbstractAgentException {

		// check time
		Date alarmTime = Objects.nonNull(time) ? time : new Date();

		// check type, severity and text
		assertNotEmpty(type, "can't create alarm, type is null or empty");
		assertNotNull(severity, "can't create alarm, severity is null");
		assertNotEmpty(text, "can't create alarm, text is null or empty");

		LOGGER.info("create alarm (time={}, type={}, severity={}, text={})", alarmTime, type, severity.getValue(),
				text);

		// create new alarm object
		Alarm alarm = new Alarm();
		alarm.setSource(getManagedObject());
		alarm.setTime(alarmTime);
		alarm.setType(type);
		alarm.setSeverity(severity.getValue());
		alarm.setText(text);
		alarm.setStatus(status);

		// create alarm at platform
		try {
			cotPlatform.getAlarmApi().create(alarm);
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), "could not create alarm", exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException("could not create alarm", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void createMeasurement(Date time, String type, float value, String unit) throws AbstractAgentException {
		Date measurementTime = Objects.nonNull(time) ? time : new Date();

		// check type, unit
		assertNotEmpty(type, "can't create measurement, type is null or empty");
		assertNotEmpty(unit, "can't create measurement, unit is null or empty");

		LOGGER.info("create measurement (time={}, type={}, value={}, unit={})", measurementTime, type, value, unit);

		ManagedObject managedObject = getManagedObject();

		// create new measurement object
		Measurement measurement = new Measurement();
		measurement.setTime(measurementTime);
		measurement.setType(type);
		measurement.set(type, new SensorMeasurementReading(value, unit));
		measurement.setSource(managedObject);

		try {
			cotPlatform.getMeasurementApi().createMeasurement(measurement);
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), ERROR_CREATE_MEASUREMENT, exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException(ERROR_CREATE_MEASUREMENT, exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void createMeasurements(final List<SensorMeasurement> sensorMeasurements) throws AbstractAgentException {
		assertNotNull(sensorMeasurements, "can't create measurements, measurements is null");
		LOGGER.info("create measurements size={}",
				CollectionUtils.isEmpty(sensorMeasurements) ? 0 : sensorMeasurements.size());
		if (CollectionUtils.isEmpty(sensorMeasurements)) {
			LOGGER.info("no measurements are created");
			return;
		}
		ManagedObject managedObject = getManagedObject();
		List<Measurement> platformMeasurements = sensorMeasurements.stream().map(m -> {
			Measurement measurement = new Measurement();
			measurement.setTime(m.getTime());
			measurement.setType(m.getType());
			measurement.set(m.getType(), new SensorMeasurementReading(m.getValue(), m.getUnit()));
			measurement.setSource(managedObject);
			return measurement;
		}).collect(Collectors.toList());
		try {
			List<Measurement> sentMeasurements = cotPlatform.getMeasurementApi()
					.createMeasurements(platformMeasurements);
			LOGGER.info("sent measurements successfully, size={}", sentMeasurements.size());
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), ERROR_CREATE_MEASUREMENT, exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException(ERROR_CREATE_MEASUREMENT, exception);
		}
	}

    /**
     * {@inheritDoc}
     */
    public AgentCredentials getDeviceCredentials(String deviceId, int interval) throws AbstractAgentException {
        do {
            // try to get device credentials
            try {
                LOGGER.debug("try to get device credentials");
                return getCredentials(deviceId);
            } catch (PlatformServiceException platformServiceException) {
                int httpStatus = platformServiceException.getHttpStatus();
                if (httpStatus != HTTP_NOT_FOUND) {
                    throw createExceptionAndLog(PlatformServiceException.class, LOGGER, "HTTP status 404 was expected", platformServiceException);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(interval);
            } catch (Exception e) {
                throw createExceptionAndLog(PlatformServiceException.class, LOGGER, "interrupted exception", e);
            }
        } while (true);
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createAgentManagedObject(AgentManagedObject agentManagedObject) throws AbstractAgentException {
		assertNotNull(agentManagedObject, "can't create ManagedObject, agentManagedObject is null");
		LOGGER.info("create managed object agentManagedObject={}", agentManagedObject);
		ManagedObject managedObject = new ManagedObject();
		managedObject.setAttributes(agentManagedObject.getAttributes());
		try {
			ManagedObject createdMagagedObject = cotPlatform.getInventoryApi().create(managedObject);
			return createdMagagedObject.getId();
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), "could not create a managed object",
					exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException("could not create a managed object", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createExternalId(String managedObjectId) throws AbstractAgentException {
		assertNotNull(managedObjectId, "can't create ExternalId, managedObjectId is null");
		assertNotNull(this.externalId, "can't create ExternalId, this.externalId is null");
		LOGGER.info("create external id externalId={}", this.externalId);
		try {
			ExternalId newExternalId = new ExternalId();
			newExternalId.setExternalId(this.externalId.getExternalId());
			newExternalId.setType(this.externalId.getType());

			ManagedObject managedObject = new ManagedObject();
			managedObject.setId(managedObjectId);
			newExternalId.setManagedObject(managedObject);
			cotPlatform.getIdentityApi().create(newExternalId);
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), "could not create external id", exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException("could not create external id", exception);
		}
	}

	/**
	 * Retrieves external ID object from the CoT.
	 * @return External ID.
	 * @throws AbstractAgentException
	 */
	private ExternalId getExternalId() throws AbstractAgentException {
		assertNotNull(this.externalId, "can't get ExternalId, this.externalId is null");
		LOGGER.info("get external id externalId={} type={}", this.externalId.getExternalId(),
				this.externalId.getType());
		try {
			return cotPlatform.getIdentityApi().getExternalId(externalId);
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), "could not get external id", exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException("could not get external id", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExternalIdAvailable() throws AbstractAgentException {
		return !Objects.isNull(getExternalId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentManagedObject getAgentManagedObject() throws AbstractAgentException {
		assertNotNull(this.externalId, "can't get managed object, this.externalId is null");
		LOGGER.info("get managed object by externalId={}", this.externalId);
		String managedObjectId = getExternalId().getManagedObject().getId();
		try {
			ManagedObject managedObject = cotPlatform.getInventoryApi().get(managedObjectId);
			return new AgentManagedObject(managedObject.getAttributes());
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), "can't get a managed object", exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException("can't get a managed object", exception);
		}
	}

	/**
	 * Retrieves the managed object identified by ID from the platform
	 * @return Managed object
	 * @throws AbstractAgentException
	 */
	private ManagedObject getManagedObject() throws AbstractAgentException {
		AgentManagedObject agentManagedObject = getAgentManagedObject();
		ManagedObject managedObject = new ManagedObject();
		managedObject.setAttributes(agentManagedObject.getAttributes());

		return managedObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateAgentManagedObject(AgentManagedObject agentManagedObject) throws AbstractAgentException {
		assertNotNull(agentManagedObject, "can't update managed object, is null");
		assertNotNull(agentManagedObject.getId(), "can't update managed object, managed object ID is null");
		LOGGER.info("update managed object id={}", agentManagedObject.getId());
		ManagedObject managedObject = new ManagedObject();
		managedObject.setAttributes(agentManagedObject.getAttributes());
		try {
			cotPlatform.getInventoryApi().update(managedObject);
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), "could not update a managed object",
					exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException("could not update a managed object", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateSupportedOperations(List<String> supportedOperationNames) throws AbstractAgentException {
		assertNotNull(supportedOperationNames, "no supported operation types given");
        LOGGER.info("update supported operations: {}", supportedOperationNames);

        SupportedOperations supportedOperations = new SupportedOperations(supportedOperationNames.toArray(new String[0]));
		try {
			ExternalId identExternalId = getExternalId();
			ManagedObject managedObject = new ManagedObject();
			managedObject.setId(identExternalId.getManagedObject().getId());
			managedObject.addFragment(supportedOperations);
			cotPlatform.getInventoryApi().update(managedObject);
		} catch (CotSdkException exception) {
			throw new PlatformServiceException(exception.getHttpStatus(), "can't update supported operations",
					exception);
		} catch (NullPointerException exception) {
			throw new PlatformServiceException("can't update supported operations", exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Operation getNextPendingOperation() throws AbstractAgentException {
        // request pending operations if there are no pending operations
        if (pendingOperations.isEmpty()) {
            LOGGER.info("requested pending operations from platform");
            pendingOperations.addAll(getOperations(OperationStatus.PENDING));
        }

        return pendingOperations.poll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Operation> List<T> getOperations(Class<T> operationType, OperationStatus status) throws AbstractAgentException {
        assertNotNull(operationType, "no operation type given");
        assertNotNull(status, "can't get operations, no status is given");
        
        int pageSize = configuration.getRestConfiguration().getOperationsRequestSize();
        LOGGER.debug("get operations from platform, status={}, pageSize={}, type={}", status, pageSize, operationType);

        // create a filter to select operations by status and optional operation(fragment) name
        OperationCollection operationCollection;
        try {
            FilterBuilder filters = new FilterBuilder();
            filters.setFilter(FilterBy.BYDEVICEID,getExternalId().getManagedObject().getId());
            filters.setFilter(FilterBy.BYSTATUS, status.toString());
            
            // get operation collection
            operationCollection = cotPlatform.getDeviceControlApi().getOperationCollection(filters, pageSize);

        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), ERROR_GET_OPERATION_COLLECTION, exception);
        } catch (Exception exception) {
            throw new PlatformServiceException(ERROR_GET_OPERATION_COLLECTION, exception);
        }
        
        // map requested operations and add to list
        List<Operation> operations = new ArrayList<>();
        do {
            Arrays.stream(operationCollection.getOperations()).forEach(sdkOperation -> 
                operations.add(new Operation(SDKOperationConverter.toPropertiesMap(sdkOperation)) {})
            );
        } while(operationCollection.hasNext());
        
        // convert operations into given type
        List<T> convertedOperations = OperationFactory.convertTo(operationType, operations);
        LOGGER.info("got {} operations from platform with status={}, type={}", operations.size(), status, operationType.getSimpleName());
        return convertedOperations;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOperationStatus(String operationId, OperationStatus newStatus) throws AbstractAgentException {
        // check parameters
        assertNotEmpty(operationId, "can't update operation status without operation id");
        assertNotNull(newStatus, "can't update operation status without new status");

        // create operation object and set id and status
        LOGGER.debug("update operation status, operation id = {}, new status = {}", operationId, newStatus);
        com.telekom.m2m.cot.restsdk.devicecontrol.Operation sdkOperation = new com.telekom.m2m.cot.restsdk.devicecontrol.Operation();
        sdkOperation.set("id", operationId);
        sdkOperation.setStatus(SDKOperationConverter.toSDKOperationStatus(newStatus.name()));
        
        // update operation
        try {
            cotPlatform.getDeviceControlApi().update(sdkOperation);
            LOGGER.info("updated operation successfully, id = {}, new status = {}", operationId, newStatus);
        } catch (CotSdkException exception) {
            LOGGER.error("can't update operation status for operation {}", operationId, exception);
            throw new PlatformServiceException(exception.getHttpStatus(), "can't update operation status for operation " + operationId, exception);
        } catch (Exception exception) {
            LOGGER.error("could not update the operation", exception);
            throw new PlatformServiceException("could not update the operation", exception);
        }
    }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] downloadBinary(URL url) throws AbstractAgentException {
        assertNotNull(url, "no url given to download");

	    // get binaryId
		String urlPath = url.getPath();
        assertNotEmpty(urlPath, "can't get path from url '" + url + "'");
		String binaryId = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        assertNotEmpty(binaryId, "can't get binary id from url '" + url + "'");

        // download binary data
        byte[] binaryData = null;
        try {
            binaryData = cotPlatform.getBinariesApi().getData(new Binary(binaryId));
        } catch (Exception e) {
            throw AssertionUtil.createExceptionAndLog(PlatformServiceException.class, LOGGER, "can't download binary @ " + url, e);
        }

		assertNotNull(binaryData, "can't download binary @ " + url);
		LOGGER.debug("downloaded byte array size: {}", binaryData.length);
		return binaryData;
	}

	private void createExternalId() throws AbstractAgentException {
		ExternalIdConfig externalIdConfig = getExternalIdConfig();

		externalId = new ExternalId();
		externalId.setExternalId(getExternalIdValue());
		externalId.setType(externalIdConfig.getType());
	}

	private void configureProxySettings() {
		// set proxy host
		String proxyHost = configuration.getRestConfiguration().getProxyHost();
		if (StringUtils.isNotEmpty(proxyHost)) {
			LOGGER.info("set proxy host to '{}'", proxyHost);
			System.setProperty("https.proxyHost", proxyHost);
		}

		// set proxy port
		String proxyPort = configuration.getRestConfiguration().getProxyPort();
		if (StringUtils.isNotEmpty(proxyPort)) {
			LOGGER.info("set proxy port to '{}'", proxyPort);
			System.setProperty("https.proxyPort", proxyPort);
		}
	}

	/**
	 * gets and checks the external id config from platform service config
	 */
	private ExternalIdConfig getExternalIdConfig() throws AbstractAgentException {
		// get and check external id config, check external id type
		ExternalIdConfig externalIdConfig = configuration.getExternalIdConfig();
		assertNotNull(externalIdConfig, "can't create external id, no external id configuration is given");
		assertNotNull(externalIdConfig.getType(), "can't create external id, no type is given");

		return externalIdConfig;
	}

	private void assertNotNull(Object value, String errorMessage) throws AbstractAgentException {
		AssertionUtil.assertNotNull(value, PlatformServiceException.class, LOGGER, errorMessage);
	}

	private void assertNotEmpty(String value, String errorMessage) throws AbstractAgentException {
		AssertionUtil.assertNotEmpty(value, PlatformServiceException.class, LOGGER, errorMessage);
	}

    private AgentCredentials getCredentials(String deviceId) throws AbstractAgentException {
        assertNotNull(deviceId, "can't get credentials, deviceId is null");
        LOGGER.info("get device credentials deviceId={}", deviceId);
        try {
            DeviceCredentials deviceCredentials = cotPlatform.getDeviceCredentialsApi().getCredentials(deviceId);
            return new AgentCredentials(true, deviceCredentials.getTenantId(), deviceCredentials.getUsername(),
                            deviceCredentials.getPassword());
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), "could not get device credentials",
                            exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException("could not get device credentials", exception);
        }
    }

    /**
     * nested class for measurement readings
     */
    private class SensorMeasurementReading {
        @SuppressWarnings("unused")
        MeasurementReading sensorValue;

        SensorMeasurementReading(float value, String unit) {
            sensorValue = new MeasurementReading(value, unit);
        }
    }
}
