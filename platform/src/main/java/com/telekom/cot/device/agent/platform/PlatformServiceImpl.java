package com.telekom.cot.device.agent.platform;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.common.util.CotHttpClient;
import com.telekom.cot.device.agent.platform.PlatformServiceConfiguration.ExternalIdConfig;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.configuration.AgentCredentials;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;
import com.telekom.m2m.cot.restsdk.CloudOfThingsPlatform;
import com.telekom.m2m.cot.restsdk.alarm.Alarm;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceCredentials;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationCollection;
import com.telekom.m2m.cot.restsdk.devicecontrol.OperationStatus;
import com.telekom.m2m.cot.restsdk.event.Event;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.library.devicemanagement.SupportedOperations;
import com.telekom.m2m.cot.restsdk.measurement.Measurement;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;
import com.telekom.m2m.cot.restsdk.util.Filter.FilterBuilder;

/**
 * The platform service.
 *
 */
public class PlatformServiceImpl extends AbstractAgentService implements PlatformService {

    private static final String ERROR_CREATE_MEASUREMENT = "can't create measurement";
    private static final String ERROR_CANT_GET_THE_OPERATION = "can't get the operation";

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformServiceImpl.class);

	private PlatformServiceConfiguration configuration;
	private AgentCredentials agentCredentials;
    private CloudOfThingsPlatform cotPlatform = null;
    private ExternalId externalId;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws AbstractAgentException {
        // get platform service configuration and create external id 
        configuration = getConfigurationManager().getConfiguration(PlatformServiceConfiguration.class);
        createExternalId();

        // configure proxy settings
        configureProxySettings();
        
        // get agent credentials
        agentCredentials = getAgentCredentialsManager().getCredentials();

        // create CoT platform
        cotPlatform = CoTPlatformBuilder.create()
                .setHostname(configuration.getHostName())
                .setTenant(agentCredentials.getTenant())
                .setUsername(agentCredentials.getUsername())
                .setPassword(agentCredentials.getPassword())
                .build();
        
        super.start();
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws AbstractAgentException {
        cotPlatform = null;
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
    	if(StringUtils.isNotEmpty(value) && (Objects.isNull(valueTemplate) || valueTemplate == ExternalIdConfig.ValueTemplates.NO_TEMPLATE)) {
    		return value;
    	}

    	// generate value if only template is set
    	if (Objects.isNull(value) && Objects.nonNull(valueTemplate)) {
        	// get hardware serial number
        	String hardwareSerial = getService(SystemService.class).getProperties(HardwareProperties.class).getSerialNumber();

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
    public void createEvent(Date time, String type, String text, Map<String, Object> attributes, Object object)
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
        event.setAttributes(attributes);
        if (Objects.nonNull(object)) {
            event.set(object);
        }
        
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
    public void createAlarm(Date time, String type, AlarmSeverity severity, String text, String status,
            Map<String, Object> attributes, Object object) throws AbstractAgentException {

        // check time
        Date alarmTime = Objects.nonNull(time) ? time : new Date();
        
        // check type, severity and text
        assertNotEmpty(type, "can't create alarm, type is null or empty");
        assertNotNull(severity, "can't create alarm, severity is null");
        assertNotEmpty(text, "can't create alarm, text is null or empty");

        LOGGER.info("create alarm (time={}, type={}, severity={}, text={})", alarmTime, type, severity.getValue(), text);

        // create new alarm object
        Alarm alarm = new Alarm();
        alarm.setSource(getManagedObject());
        alarm.setTime(alarmTime);
        alarm.setType(type);
        alarm.setSeverity(severity.getValue());
        alarm.setText(text);
        alarm.setStatus(status);
        alarm.setAttributes(attributes);
        if (Objects.nonNull(object)) {
            alarm.set(object);
        }
        
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
    public Measurement createMeasurement(Measurement measurement) throws AbstractAgentException {
        assertNotNull(measurement, "can't create measurement, measurement is null");
        LOGGER.info("create measurement value={}", measurement);
        try {
            return cotPlatform.getMeasurementApi().createMeasurement(measurement);
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), ERROR_CREATE_MEASUREMENT, exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException(ERROR_CREATE_MEASUREMENT, exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Measurement> createMeasurements(final List<Measurement> measurements) throws AbstractAgentException {
        assertNotNull(measurements, "can't create measurements, measurements is null");
        LOGGER.info("create measurements size={}", CollectionUtils.isEmpty(measurements) ? 0 : measurements.size());
        if (CollectionUtils.isEmpty(measurements)) {
            LOGGER.info("no measurements are created");
            return new ArrayList<>();
        }
        try {
        	List<Measurement> sentMeasurements = cotPlatform.getMeasurementApi().createMeasurements(measurements);
        	LOGGER.info("sent measurements successfully, size={}", sentMeasurements.size());
        	return sentMeasurements;
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), ERROR_CREATE_MEASUREMENT, exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException(ERROR_CREATE_MEASUREMENT, exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AgentCredentials getDeviceCredentials(String deviceId) throws AbstractAgentException {
        assertNotNull(deviceId, "can't get credentials, deviceId is null");
        LOGGER.info("get device credentials deviceId={}", deviceId);
        try {
        	DeviceCredentials deviceCredentials = cotPlatform.getDeviceCredentialsApi().getCredentials(deviceId);
            return new AgentCredentials(deviceCredentials.getTenantId(), deviceCredentials.getUsername(), deviceCredentials.getPassword());
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), "could not get device credentials",
                    exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException("could not get device credentials", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Operation createNewDevice(Operation operation) throws AbstractAgentException {
        assertNotNull(operation, "can't create operation, operation is null");
        LOGGER.info("create new device operation={}", operation);
        try {
            return cotPlatform.getDeviceControlApi().createNewDevice(operation);
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), "could not create new device", exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException("could not create new device", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManagedObject createManagedObject(ManagedObject managedObject) throws AbstractAgentException {
        assertNotNull(managedObject, "can't create ManagedObject, managedObject is null");
        LOGGER.info("create managed object managedObject={}", managedObject);
        try {
            return cotPlatform.getInventoryApi().create(managedObject);
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
    public ExternalId createExternalId(String managedObjectId) throws AbstractAgentException {
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
            return cotPlatform.getIdentityApi().create(newExternalId);
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), "could not create external id", exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException("could not create external id", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalId getExternalId() throws AbstractAgentException {
        assertNotNull(this.externalId, "can't get ExternalId, this.externalId is null");
        LOGGER.info("get external id externalId={} type={}", this.externalId.getExternalId(), this.externalId.getType());
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
    public ManagedObject getManagedObject() throws AbstractAgentException {
        assertNotNull(this.externalId, "can't get managed object, this.externalId is null");
        LOGGER.info("get managed object by externalId={}", this.externalId);
        String managedObjectId = getExternalId().getManagedObject().getId();
        try {
            return cotPlatform.getInventoryApi().get(managedObjectId);
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), "can't get a managed object", exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException("can't get a managed object", exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateManagedObject(ManagedObject managedObject) throws AbstractAgentException {
        assertNotNull(managedObject, "can't update managed object, is null");
        assertNotNull(managedObject.getId(), "can't update managed object, managed object ID is null");
        LOGGER.info("update managed object id={}", managedObject.getId());
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
    public void updateSupportedOperations(SupportedOperations supportedOperations) throws AbstractAgentException {
        assertNotNull(supportedOperations, "no supported operations is given");
        LOGGER.info("update supported operations");
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
    public OperationCollection getOperationCollection(OperationStatus status, Integer resultSize)
            throws AbstractAgentException {
        assertNotNull(status, "can't get operations, no status is given");
        assertNotNull(resultSize, "can't get operations, no resultSize is given");
        LOGGER.info("get operation status={} resultSize={}", status, resultSize);
        try {
            ExternalId identExternalId = getExternalId();
            FilterBuilder filters = new FilterBuilder();
            filters.byDeviceId(identExternalId.getManagedObject().getId());
            filters.byStatus(status);
            return cotPlatform.getDeviceControlApi().getOperationCollection(filters, resultSize);
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), ERROR_CANT_GET_THE_OPERATION, exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException(ERROR_CANT_GET_THE_OPERATION, exception);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public OperationCollection getOperationCollection(String fragmentType, OperationStatus status, Integer resultSize)
            throws AbstractAgentException {
        assertNotNull(status, "can't get operations, no status is given");
        assertNotNull(resultSize, "can't get operations, no resultSize is given");
        assertNotNull(fragmentType, "can't get operations, no fragmentType is given");
        LOGGER.info("get operation status={} resultSize={}", status, resultSize);
        try {
            ExternalId identExternalId = getExternalId();
            FilterBuilder filters = new FilterBuilder();
            filters.byDeviceId(identExternalId.getManagedObject().getId());
            filters.byStatus(status);
            filters.byFragmentType(fragmentType);
            return cotPlatform.getDeviceControlApi().getOperationCollection(filters, resultSize);
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), ERROR_CANT_GET_THE_OPERATION, exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException(ERROR_CANT_GET_THE_OPERATION, exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOperation(Operation operation) throws AbstractAgentException {
        assertNotNull(operation, "can't update operation, no operation is given");
        LOGGER.info("update operation {}", operation.getAttributes());
        try {
            cotPlatform.getDeviceControlApi().update(operation);
        } catch (CotSdkException exception) {
            throw new PlatformServiceException(exception.getHttpStatus(), "could not update the operation", exception);
        } catch (NullPointerException exception) {
            throw new PlatformServiceException("could not update the operation", exception);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] downloadBinary(URL url) throws AbstractAgentException {
        // get binaryId
        String urlPath = url.getPath();
        String binaryId = urlPath.substring(urlPath.lastIndexOf("/"));
        
        // download binary data
        CotHttpClient client = new CotHttpClient(configuration.getHostName(),
                        agentCredentials.getTenant(),
                        agentCredentials.getUsername(),
                        agentCredentials.getPassword(),
                        configuration.getProxyHost(),
                        configuration.getProxyPort());

        byte[] binaryData = client.getBinary(binaryId);
        assertNotNull(binaryData, "can't download binary with id '" + binaryId + "'");
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
		String proxyHost = configuration.getProxyHost();
		if (StringUtils.isNotEmpty(proxyHost)) {
			LOGGER.info("set proxy host to '{}'", proxyHost);
			System.setProperty("https.proxyHost", proxyHost);
		}
		
		// set proxy port
		String proxyPort = configuration.getProxyPort();
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
}
