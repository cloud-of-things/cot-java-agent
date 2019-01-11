package com.telekom.cot.device.agent.platform.mqtt;

import static com.telekom.cot.device.agent.platform.mqtt.SmartRestUtil.getPayloadGetOperationStatus;
import static com.telekom.cot.device.agent.platform.mqtt.SmartRestUtil.getPayloadPutOperationStatus;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.configuration.AgentCredentials;
import com.telekom.cot.device.agent.common.configuration.AgentCredentialsManager;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.common.injection.Inject;
import com.telekom.cot.device.agent.common.util.AssertionUtil;
import com.telekom.cot.device.agent.platform.PlatformService;
import com.telekom.cot.device.agent.platform.mqtt.event.AlarmAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.GetOperationStatus;
import com.telekom.cot.device.agent.platform.mqtt.event.GetOperationStatusAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.LifecycleResponseAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.ManagedObjectAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.OperationConfigUpdateAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.OperationRestartAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.OperationTestOperationAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.StartupAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.TemperatureAgentEventListener;
import com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener;
import com.telekom.cot.device.agent.platform.objects.AgentConfiguration;
import com.telekom.cot.device.agent.platform.objects.AgentFirmware;
import com.telekom.cot.device.agent.platform.objects.AgentFragmentIdentifier;
import com.telekom.cot.device.agent.platform.objects.AgentHardware;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.platform.objects.AgentMobile;
import com.telekom.cot.device.agent.platform.objects.AgentSoftwareList.Software;
import com.telekom.cot.device.agent.platform.objects.operation.Operation;
import com.telekom.cot.device.agent.platform.objects.operation.Operation.OperationStatus;
import com.telekom.cot.device.agent.platform.objects.operation.OperationFactory;
import com.telekom.cot.device.agent.platform.objects.ManagedObject;
import com.telekom.cot.device.agent.platform.objects.SensorMeasurement;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.MobileProperties;

import de.tarent.telekom.cot.mqtt.util.JsonHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class PlatformServiceMqttImpl extends AbstractAgentService implements PlatformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformServiceMqttImpl.class);
    @Inject
    private PlatformServiceMqttConfiguration platformServiceMqttConfiguration;
    @Inject
    private SystemService systemService;
    @Inject
    private AgentCredentialsManager credentialsManager;
    private MqttPlatform mqttPlatform;
    private String iccId;
    private String managedObjectId;
    private PublishCallback publishCallback;
    
    public static final int VERTICAL_WORKER_POOL_SIZE = 2;
    public static final int VERTX_EVENT_LOOP_POOL_SIZE = 1;
    public static final int VERTX_INTERNAL_BLOCKING_POOL_SIZE = 2;
    public static final int VERTX_WORKER_POOL_SIZE = 1;
    private int timeout = 0;
    private int delaySendMeasurement = 0;
    
    private final ConcurrentLinkedQueue<Operation> pendingOperations = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    private final ManagedObjectAgentEventListener managedObjectAEL = new ManagedObjectAgentEventListener();
    private final UpdateFragmentAgentEventListener updateFragmentAEL = new UpdateFragmentAgentEventListener();
    private final StartupAgentEventListener startupAEL = new StartupAgentEventListener();
    private final TemperatureAgentEventListener temperatureAEL = new TemperatureAgentEventListener();
    private final AlarmAgentEventListener alarmAEL = new AlarmAgentEventListener();
    private final GetOperationStatusAgentEventListener operationStatusAEL = new GetOperationStatusAgentEventListener();
    private final OperationConfigUpdateAgentEventListener operationConfigUpdateAEL = new OperationConfigUpdateAgentEventListener(pendingOperations);
    private final OperationTestOperationAgentEventListener operationTestOperationAEL = new OperationTestOperationAgentEventListener(pendingOperations);
    private final OperationRestartAgentEventListener operationRestartAEL = new OperationRestartAgentEventListener(pendingOperations);
    private final LifecycleResponseAgentEventListener lifecycleResponseAEL = new LifecycleResponseAgentEventListener();
    
    private final Consumer<String> loggingEvent = new Consumer<String>() {
        
        @Override
        public void accept(String eventId) {
            LOGGER.debug("got event response by id {}", eventId);
        }
    };
    
    private final Consumer<String> loggingAlarm = new Consumer<String>() {
        
        @Override
        public void accept(String alarmId) {
            LOGGER.debug("got alarm response by id {}", alarmId);
        }
    };
    
    private final Consumer<String> loggingMeasurements = new Consumer<String>() {
        
        @Override
        public void accept(String alarmId) {
            LOGGER.debug("got measurement response by id {}", alarmId);
        }
    };


    @Override
    public void start() throws AbstractAgentException {
        this.iccId = systemService.getProperties(MobileProperties.class).getIccid();
        this.timeout = platformServiceMqttConfiguration.getMqttConfiguration().getTimeout();
        this.delaySendMeasurement = platformServiceMqttConfiguration.getMqttConfiguration().getDelaySendMeasurement();
        // publishCallback
        publishCallback = PublishCallback.getInstance(getAgentContext());
        // get managed object by ICCID
        publishCallback.monitorResponse(TemplateId.GET_MANAGED_OBJECT_ID_RES, managedObjectAEL);
        // response of startup event
        publishCallback.monitorResponse(TemplateId.EVENT_STARTUP_RES, startupAEL);
        // response of measurement
        publishCallback.monitorResponse(TemplateId.CREATE_MEASUREMENT_RES, temperatureAEL);
        // response of alarm
        publishCallback.monitorResponse(TemplateId.CREATE_ALARM_RES, alarmAEL);
        // get operation status
        publishCallback.monitorResponse(TemplateId.STATUS_OF_OPERATION_RESTART_RES, operationStatusAEL);
        publishCallback.monitorResponse(TemplateId.STATUS_OF_OPERATION_TEST_RES, operationStatusAEL);
        // update operation status and update fragments (hardware, etc.)
        publishCallback.monitorResponse(TemplateId.UPDATE_OPERATION_SUCCESSFUL_STATUS_RES, updateFragmentAEL);
        publishCallback.monitorResponse(TemplateId.UPDATE_OPERATION_FAILED_STATUS_RES, updateFragmentAEL);
        publishCallback.monitorResponse(TemplateId.UPDATE_OPERATION_EXECUTING_STATUS_RES, updateFragmentAEL);
        publishCallback.monitorResponse(TemplateId.UPDATE_SUPPORTED_OPERATIONS_RES, updateFragmentAEL);
        publishCallback.monitorResponse(TemplateId.UPDATE_HARDWARE_RES, updateFragmentAEL);
        publishCallback.monitorResponse(TemplateId.UPDATE_SOFTWARE_LIST_RES, updateFragmentAEL);
        publishCallback.monitorResponse(TemplateId.UPDATE_MOBILE_RES, updateFragmentAEL);
        publishCallback.monitorResponse(TemplateId.UPDATE_FIRMWARE_RES, updateFragmentAEL);
        publishCallback.monitorResponse(TemplateId.UPDATE_CONFIGURATION_RES, updateFragmentAEL);
        // operations  
        publishCallback.monitorResponse(TemplateId.OPERATION_CONFIGURATION_RES, operationConfigUpdateAEL);
        publishCallback.monitorResponse(TemplateId.OPERATION_TEST_RES, operationTestOperationAEL);
        publishCallback.monitorResponse(TemplateId.OPERATION_RESTART_RES, operationRestartAEL);
        // finished operation
        getAgentContext().addAgentEventListener(lifecycleResponseAEL);
        lifecycleResponseAEL.addFinishedListener(operationStatusAEL);
        lifecycleResponseAEL.addStartupListener(operationStatusAEL);
        // manage ignores
        publishCallback.reverseIgnoredResponses(TemplateId.OPERATION_CONFIGURATION_RES, //
                        TemplateId.UPDATE_OPERATION_EXECUTING_STATUS_RES,
                        TemplateId.UPDATE_OPERATION_SUCCESSFUL_STATUS_RES,
                        TemplateId.UPDATE_OPERATION_FAILED_STATUS_RES);
        publishCallback.reverseIgnoredResponses(TemplateId.CREATE_MANAGED_OBJECT_ID_RES, //
                        TemplateId.UPDATE_SUPPORTED_OPERATIONS_RES,
                        TemplateId.UPDATE_HARDWARE_RES,
                        TemplateId.UPDATE_SOFTWARE_LIST_RES,
                        TemplateId.UPDATE_MOBILE_RES,
                        TemplateId.UPDATE_FIRMWARE_RES,
                        TemplateId.UPDATE_CONFIGURATION_RES);
        // AgentCredentials
        AgentCredentials credentials = credentialsManager.getCredentials();
        if (credentials.isBootstrappingMode()) {
            this.mqttPlatform = createMqttPlatform(true, iccId, credentials.getUsername(), credentials.getPassword());
        } else {
            this.mqttPlatform = createMqttPlatform(false, iccId, credentials.getUsername(), credentials.getPassword());
            subscribeToTopicAndGetManagedObjectId();
        }
        super.start();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void stop() throws AbstractAgentException {
        AtomicBoolean finished = unsubscribeAndClose();
        PublishFuture<Void> publishFuture = new PublishFuture(executorService.submit(() -> {
            while (!finished.get()) {
                TimeUnit.MILLISECONDS.sleep(500);
            }
            return Void.TYPE;
        }));
        publishFuture.get(timeout, TimeUnit.SECONDS);
        super.stop();
    }

    @Override
    public String getExternalIdValue() throws AbstractAgentException {
        return iccId;
    }

    @Override
    public void createEvent(Date time, String type, String text, String condition) throws AbstractAgentException {
        // check time
        Date eventTime = Objects.nonNull(time) ? time : new Date();
        // check type and text
        assertNotEmpty(type, "can't create event, type is null or empty");
        assertNotEmpty(text, "can't create event, text is null or empty");
        LOGGER.info("create event (time={}, type={}, text={})", eventTime, type, text);
        // create new event object
        String xid = platformServiceMqttConfiguration.getMqttConfiguration().getxId();
        String template = SmartRestUtil.getPayloadCreateEvent(xid, time, type, text, managedObjectId, condition, "");
        executorService.submit(() -> {
            mqttPlatform.publishMessage(template, (r) -> LOGGER.info("create event {}", r));
            startupAEL.provideEvent(loggingEvent);
        });
    }

    @Override
    public void createAlarm(Date time, String type, AlarmSeverity severity, String text, String status)
                    throws AbstractAgentException {
        // check time
        Date alarmTime = Objects.nonNull(time) ? time : new Date();
        // check type, severity and text
        assertNotEmpty(type, "can't create alarm, type is null or empty");
        assertNotNull(severity, "can't create alarm, severity is null");
        assertNotEmpty(text, "can't create alarm, text is null or empty");
        LOGGER.info("create alarm (time={}, type={}, severity={}, text={})", alarmTime, type, severity
                        .getValue(), text);
        // create new alarm object
        String xid = platformServiceMqttConfiguration.getMqttConfiguration().getxId();
        String template = SmartRestUtil.getPayloadCreateAlarm(xid, time, type, severity, text, status, managedObjectId);
        executorService.submit(() -> {
            mqttPlatform.publishMessage(template, (r) -> LOGGER.info("create alarm {}", r));
            alarmAEL.provideEvent(loggingAlarm);
        });
    }

    @Override
    public void createMeasurement(Date time, String type, float value, String unit) throws AbstractAgentException {
        // check type, unit
        assertNotEmpty(type, "can't create measurement, type is null or empty");
        assertNotEmpty(unit, "can't create measurement, unit is null or empty");
        // log
        Date measurementTime = Objects.nonNull(time) ? time : new Date();
        LOGGER.info("create measurement (time={}, type={}, value={}, unit={})", measurementTime, type, value, unit);
        // smart rest template
        String xid = platformServiceMqttConfiguration.getMqttConfiguration().getxId();
        String template = SmartRestUtil.getPayloadCreateMeasurement(xid, type, value, unit, time, managedObjectId);
        executorService.submit(() -> {
            mqttPlatform.publishMessage(template, (r) -> LOGGER.info("create measurement {}", r));
            temperatureAEL.provideEvent(loggingMeasurements);
        });
        try {
            TimeUnit.MILLISECONDS.sleep(delaySendMeasurement);
        } catch (InterruptedException e) {
            LOGGER.warn("InterruptedException by delay of send measurement");
        }
    }

    @Override
    public void createMeasurements(List<SensorMeasurement> measurements) throws AbstractAgentException {
        assertNotNull(measurements, "can't create measurements, measurements is null");
        if (measurements.isEmpty()) {
            LOGGER.info("no measurements are created");
            return;
        }
        // send measurements
        LOGGER.info("create measurements size={}", measurements.size());
        int errorCount = 0;
        for (SensorMeasurement m : measurements) {
            try {
                createMeasurement(m.getTime(), m.getType(), m.getValue(), m.getUnit());
            } catch (AbstractAgentException agentException) {
                errorCount++;
                LOGGER.error("can't create measurement", agentException);
            }
        }
        // check error count
        Class<PlatformServiceException> excClass = PlatformServiceException.class;
        AssertionUtil.assertIsTrue(errorCount == 0, excClass, LOGGER, "error count should be zero");
    }

    @Override
    public AgentCredentials getDeviceCredentials(String iccId, int interval) throws AbstractAgentException {
        AgentCredentials agentCredentials = createDeviceCredentials(iccId, interval);
        // change credentials (is this really necessary)
        Properties properties = mqttPlatform.getProperties();
        properties.setProperty(JsonHelper.USER_KEY, iccId);
        properties.setProperty(JsonHelper.PASSWORD_KEY, agentCredentials.getPassword());
        // subscribe and get managed object ID
        subscribeToTopicAndGetManagedObjectId();
        return agentCredentials;
    }

    @Override
    public String createAgentManagedObject(AgentManagedObject agentManagedObject) throws AbstractAgentException {
        // never used in MQTT: AppBootstrap always calls updateAgentManagedObject()
        throw new PlatformServiceException("not supported in MQTT");
    }

    @Override
    public void createExternalId(String managedObjectId) throws AbstractAgentException {
        // never used in MQTT
        throw new PlatformServiceException("not supported in MQTT");
    }

    @Override
    public boolean isExternalIdAvailable() throws AbstractAgentException {
        LOGGER.debug("currently not supported in MQTT");
        // Returning true has the following implication:
        // InventoryService.isDeviceRegistered() will always returns true for MQTT
        // In AppBootstrap, steps.isDeviceRegistered() always returns true for MQTT
        // steps.updateDevice() will always be called (and createAndRegisterDevice will
        // never be called)
        // createAndRegisterDevice() is not needed for MQTT because the managed object
        // is created by the MQTT SDK
        return true;
    }

    @Override
    public AgentManagedObject getAgentManagedObject() throws AbstractAgentException {
        AgentManagedObject managedObject = new AgentManagedObject();
        managedObject.setName(iccId);
        managedObject.setId(managedObjectId);
        return managedObject;
    }

    @Override
    public void updateAgentManagedObject(AgentManagedObject agentManagedObject) throws AbstractAgentException {
        LOGGER.info("updating managed object {} in inventory", agentManagedObject.getId());
        String xid = platformServiceMqttConfiguration.getMqttConfiguration().getxId();
        updateAgentManagedObjectFragment(agentManagedObject, xid, AgentFragmentIdentifier.HARDWARE);
        updateAgentManagedObjectFragment(agentManagedObject, xid, AgentFragmentIdentifier.FIRMWARE);
        updateAgentManagedObjectFragment(agentManagedObject, xid, AgentFragmentIdentifier.MOBILE);
        updateAgentManagedObjectFragment(agentManagedObject, xid, AgentFragmentIdentifier.SOFTWARE_LIST);
        updateAgentManagedObjectFragment(agentManagedObject, xid, AgentFragmentIdentifier.CONFIGURATION);
    }

    /**
     * updates a specific fragment for an existing managed object in the inventory
     * 
     * @param agentManagedObject
     *            managed object to update
     * @param xid
     *            template collection's xId
     * @param agentFragmentIdentifier
     *            fragment identifier to update
     * @throws AbstractAgentException
     */
    private void updateAgentManagedObjectFragment(AgentManagedObject agentManagedObject, String xid,
                    AgentFragmentIdentifier agentFragmentIdentifier) throws AbstractAgentException {
        String fragmentId = agentFragmentIdentifier.getId();
        Object fragmentObject = agentManagedObject.getAttributes().get(fragmentId);
        if (Objects.isNull(fragmentObject) || !(fragmentObject instanceof JsonElement)) {
            LOGGER.warn("Fragment id {} not found in the managed object to update", fragmentId);
            return;
        }
        JsonElement fragmentJsonElement = (JsonElement) fragmentObject;
        Gson gson = new Gson();
        String template = "";
        boolean isFragmentEmpty = (fragmentJsonElement.isJsonObject()
                        && fragmentJsonElement.getAsJsonObject().entrySet().isEmpty());
        switch (agentFragmentIdentifier) {
            case HARDWARE:
                AgentHardware agentHardware = isFragmentEmpty ? null
                                : gson.fromJson(fragmentJsonElement, AgentHardware.class);
                template = SmartRestUtil.getPayloadUpdateHardware(xid, managedObjectId, agentHardware, isFragmentEmpty);
                break;
            case FIRMWARE:
                AgentFirmware agentFirmware = isFragmentEmpty ? null
                                : gson.fromJson(fragmentJsonElement, AgentFirmware.class);
                template = SmartRestUtil.getPayloadUpdateFirmware(xid, managedObjectId, agentFirmware, isFragmentEmpty);
                break;
            case MOBILE:
                AgentMobile agentMobile = isFragmentEmpty ? null
                                : gson.fromJson(fragmentJsonElement, AgentMobile.class);
                template = SmartRestUtil.getPayloadUpdateMobile(xid, managedObjectId, agentMobile, isFragmentEmpty);
                break;
            case SOFTWARE_LIST:
                Software software = null;
                List<Software> softwareList = gson.fromJson(fragmentJsonElement, new TypeToken<List<Software>>() {
                }.getType());
                if (softwareList != null && !softwareList.isEmpty()) {
                    software = softwareList.get(0);
                }
                template = SmartRestUtil.getPayloadUpdateSoftwareList(xid, managedObjectId, software, isFragmentEmpty);
                break;
            case CONFIGURATION:
                AgentConfiguration agentConfiguration = isFragmentEmpty ? null
                                : gson.fromJson(fragmentJsonElement, AgentConfiguration.class);
                template = SmartRestUtil
                                .getPayloadUpdateConfiguration(xid, managedObjectId, agentConfiguration, isFragmentEmpty);
                break;
            default:
                break;
        }
        if (!template.isEmpty()) {
            String message = template;
            PublishFuture<String> publishFuture = new PublishFuture<String>(executorService.submit(() -> {
                mqttPlatform.publishMessage(message, (result) -> LOGGER
                                .info("published message={} result={}", message, result));
                return updateFragmentAEL.waitOnAgentEventAndCreate();
            }));
            String id = publishFuture.get(timeout, TimeUnit.SECONDS);
            LOGGER.debug("updated fragment {} for managed object id {} ", agentFragmentIdentifier.getId(), id);
        }
    }

    @Override
    public void updateSupportedOperations(List<String> supportedOperationNames) throws AbstractAgentException {
        assertNotNull(supportedOperationNames, "no supported operations given");
        LOGGER.info("update supported operations: {}", supportedOperationNames);

        // update inventory fragment
        String xid = platformServiceMqttConfiguration.getMqttConfiguration().getxId();
        String template = SmartRestUtil
                        .getPayloadUpdateSupportedOperations(xid, managedObjectId, supportedOperationNames);
        PublishFuture<String> publishFuture = new PublishFuture<String>(executorService.submit(() -> {
            mqttPlatform.publishMessage(template, (r) -> LOGGER.info("create supported operations {}", r));
            return updateFragmentAEL.waitOnAgentEventAndCreate();
        }));
        String id = publishFuture.get(timeout, TimeUnit.SECONDS);
        LOGGER.debug("got supported operation response by id {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Operation getNextPendingOperation() throws AbstractAgentException {
        return pendingOperations.poll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOperationStatus(String operationId, OperationStatus newStatus) throws AbstractAgentException {
        LOGGER.info("updating operation {} by status {}", operationId, newStatus);
        String xId = platformServiceMqttConfiguration.getMqttConfiguration().getxId();
        String message = getPayloadPutOperationStatus(xId, operationId, String.valueOf(newStatus));
        PublishFuture<String> publishFuture = new PublishFuture<String>(executorService.submit(() -> {
            mqttPlatform.publishMessage(message, (result) -> LOGGER
                            .info("published message={} result={}", message, result));
            return updateFragmentAEL.waitOnAgentEventAndCreate();
        }));
        String id = publishFuture.get(timeout, TimeUnit.SECONDS);
        LOGGER.debug("got update operation response by id {}", id);
    }

    @Override
    public byte[] downloadBinary(URL url) throws AbstractAgentException {
        throw new PlatformServiceException("not supported in MQTT");
    }

    private void assertNotNull(Object value, String errorMessage) throws AbstractAgentException {
        AssertionUtil.assertNotNull(value, PlatformServiceException.class, LOGGER, errorMessage);
    }

    private void assertNotEmpty(String value, String errorMessage) throws AbstractAgentException {
        AssertionUtil.assertNotEmpty(value, PlatformServiceException.class, LOGGER, errorMessage);
    }

    private void subscribeToTopicAndGetManagedObjectId() throws AbstractAgentException {
        LOGGER.info("subscribing to topic");
        // create subscribe callback
        final AtomicBoolean successfulSubscribe = new AtomicBoolean(false);
        final AtomicBoolean finishedSubscribe = new AtomicBoolean(false);
        final Consumer<Object> callbackSubscribe = result -> {
            finishedSubscribe.set(true);
            if (Boolean.class.isInstance(result)) {
                successfulSubscribe.set(Boolean.class.cast(result));
            } else {
                LOGGER.error(String.valueOf(result));
            }
        };
        // create managed object callback
        mqttPlatform.subscribeToTopic(callbackSubscribe, publishCallback);
        // wait
        wait(finishedSubscribe, 500, true);
        // check callback
        if (!successfulSubscribe.get()) {
            throw new PlatformServiceException("unable subscribe to topic");
        }
        // create rest template
        LOGGER.info("publish message get managedObjectId");
        // template
        String xId = platformServiceMqttConfiguration.getMqttConfiguration().getxId();
        String template = SmartRestUtil.getPayloadManagedObjectId(xId, iccId);
        // executor
        PublishFuture<ManagedObject> publishFuture = new PublishFuture<ManagedObject>(executorService.submit(() -> {
            mqttPlatform.publishMessage(template, (result) -> LOGGER
                            .info("published message={} resulte={}", template, result));
            return managedObjectAEL.waitOnAgentEventAndCreate();
        }));
        managedObjectId = publishFuture.get(timeout, TimeUnit.SECONDS).getId();
        // is never used any more
        getAgentContext().removeAgentEventListener(managedObjectAEL);
        LOGGER.info("got managedObjectId = {}", managedObjectId);
    }

    private AtomicBoolean unsubscribeAndClose() {
        final AtomicBoolean finished = new AtomicBoolean(false);
        mqttPlatform.unsubscribeFromTopic(successful -> {
            LOGGER.info("successful unsubscribe from topic {}", successful);
            Handler<AsyncResult<Void>> completionHandler = new Handler<AsyncResult<Void>>() {

                @Override
                public void handle(AsyncResult<Void> event) {
                    finished.set(true);
                    if (event.succeeded()) {
                        LOGGER.info("succeeded unsubscribe and stop");
                    } else if (event.failed()) {
                        LOGGER.error("failed unsubscribe and stop");
                    }
                }
            };
            mqttPlatform.close(completionHandler);
        });
        return finished;
    }

    private MqttPlatform createMqttPlatform(boolean bootstrapping, String iccId, String user, String password)
                    throws AbstractAgentException {
        Properties props = new Properties();
        props.setProperty("xId", platformServiceMqttConfiguration.getMqttConfiguration().getxId());
        props.setProperty(JsonHelper.BROKER_URI_KEY, platformServiceMqttConfiguration.getHostName());
        props.setProperty(JsonHelper.BROKER_PORT_KEY, platformServiceMqttConfiguration.getMqttConfiguration()
                        .getPort());
        if (bootstrapping) {
            props.setProperty(JsonHelper.INITIAL_USER_KEY, user);
            props.setProperty(JsonHelper.INITIAL_PASSWORD_KEY, password);
        } else {
            props.setProperty(JsonHelper.USER_KEY, user);
            props.setProperty(JsonHelper.PASSWORD_KEY, password);
        }
        AtomicBoolean successfulDeploy = new AtomicBoolean(false);
        MqttPlatform platform = MqttPlatformBuilder.create(iccId, props)
                        .setVerticalWorkerPoolSize(VERTICAL_WORKER_POOL_SIZE)
                        .setVertxEventLoopPoolSize(VERTX_EVENT_LOOP_POOL_SIZE)
                        .setVertxInternalBlockingPoolSize(VERTX_INTERNAL_BLOCKING_POOL_SIZE)
                        .setVertxWorkerPoolSize(VERTX_WORKER_POOL_SIZE)
                        .build(successful -> successfulDeploy.set(successful));
        wait(successfulDeploy, 500, true);
        return platform;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Operation> List<T> getOperations(Class<T> operationType, OperationStatus status) throws AbstractAgentException {
        assertNotNull(operationType, "no operation type given");
        assertNotNull(status, "can't get operations, no status is given");
        
        // create template
        String operationName = OperationFactory.getOperationName(operationType);
        String xId = platformServiceMqttConfiguration.getMqttConfiguration().getxId();
        String template = getPayloadGetOperationStatus(xId, managedObjectId, String.valueOf(status), operationName);

        // executorService
        PublishFuture<List<GetOperationStatus>> publishFuture = new PublishFuture<>(
                        executorService.submit(() -> {
                            mqttPlatform.publishMessage(template, (result) -> LOGGER
                                            .info("published message={} resulte={}", template, result));
                            return operationStatusAEL.waitOnAllAgentEvents();
                        }));
        List<GetOperationStatus> values = publishFuture.get(timeout, TimeUnit.SECONDS);
        LOGGER.debug("got operations response {}", values);

        // check result
        if (Objects.isNull(values) || values.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Operation> operations = new ArrayList<>();
        for (GetOperationStatus getOperationStatus : values) {
            Operation operation = new Operation() {};
            operation.setDeviceId(managedObjectId);
            operation.setId(getOperationStatus.getId());
            operation.setStatus(OperationStatus.valueOf(getOperationStatus.getStatus()));
            if(StringUtils.isNotEmpty(operationName)) {
                operation.setProperty(operationName, "");
            }
            
            operations.add(operation);
        }
        
        // convert operations into given type
        List<T> convertedOperations = OperationFactory.convertTo(operationType, operations);
        LOGGER.info("got {} operations from platform with status={}, type={}", operations.size(), status, operationType.getSimpleName());
        return convertedOperations;
    }

    private AgentCredentials createDeviceCredentials(String iccId, int interval) throws AbstractAgentException {
        // create callback
        final AtomicReference<String> resultSubscribe = new AtomicReference<>();
        final AtomicBoolean finishedSubscribe = new AtomicBoolean(false);
        final Consumer<String> callbackRegister = result -> {
            resultSubscribe.set(result);
            finishedSubscribe.set(true);
        };
        // register device
        mqttPlatform.registerDevice(callbackRegister);
        // wait until the accept button is pressed
        wait(finishedSubscribe, interval, false);
        // check result
        Class<PlatformServiceException> excClass = PlatformServiceException.class;
        AssertionUtil.assertIsTrue(Objects.nonNull(resultSubscribe.get()), excClass, LOGGER, "password required");
        // create credentials
        AgentCredentials agentCredentials = new AgentCredentials();
        agentCredentials.setTenant("MQTT");
        agentCredentials.setUsername(iccId);
        agentCredentials.setPassword(resultSubscribe.get());
        // use new credentials
        credentialsManager.setCredentials(agentCredentials);
        return agentCredentials;
    }

    /**
     * Wait until the callback is called or the time is exceeded.
     * 
     * @param finishedCallback
     * @param interval
     *            in milliseconds
     * @throws AbstractAgentException
     */
    private void wait(AtomicBoolean finishedCallback, int intervalInMilliseconds, boolean withTimeout)
                    throws AbstractAgentException {
        int timeoutInMilliseconds = platformServiceMqttConfiguration.getMqttConfiguration().getTimeout() * 1000;
        int expiredTimeInMilliseconds = 0;
        while (!finishedCallback.get()) {
            if (withTimeout && expiredTimeInMilliseconds > timeoutInMilliseconds) {
                LOGGER.error("the callback wasn't called");
                throw new PlatformServiceException("timeout of " + timeoutInMilliseconds + " ms is reached");
            }
            try {
                TimeUnit.MILLISECONDS.sleep(intervalInMilliseconds);
            } catch (Exception e) {
                throw new PlatformServiceException("interrupted sleep", e);
            }
            expiredTimeInMilliseconds += intervalInMilliseconds;
        }
    }
}