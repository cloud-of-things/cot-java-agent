package com.telekom.cot.device.agent.platform.mqtt;

import java.util.Arrays;

/**
 * The MQTT template ID's.
 *
 */
public enum TemplateId {
    /** Error handling. */
    NOT_AVAILABLE(-1, TemplateId.TYPE.NA),
    // ***********************
    // Requests
    // ***********************
    /**
     * get managed object id by ICCID</br>
     * response by GET_MANAGED_OBJECT_ID_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.ManagedObjectAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.ManagedObjectAgentEventListener}</br>
     */
    GET_MANAGED_OBJECT_ID_REQ(600, TemplateId.TYPE.REQ),
    /**
     * create managed object in CoT</br>
     * response by CREATE_MANAGED_OBJECT_ID_RES</br>
     * is used by the MQTT SDK</br>
     */
    CREATE_MANAGED_OBJECT_ID_REQ(602, TemplateId.TYPE.REQ),
    /**
     * create an universal event request</br>
     * response by EVENT_STARTUP_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.StartupAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.StartupAgentEventListener}</br>
     */
    CREATE_EVENT_REQ(400, TemplateId.TYPE.REQ),
    /**
     * create measurement request</br>
     * response by CREATE_MEASUREMENT_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.TemperatureAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.TemperatureAgentEventListener}</br>
     */
    CREATE_MEASUREMENT_REQ(100, TemplateId.TYPE.REQ),
    /**
     * create alarm request</br>
     * response by CREATE_ALARM_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.AlarmAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.AlarmAgentEventListener}</br>
     */
    CREATE_ALARM_REQ(450, TemplateId.TYPE.REQ),
    /**
     * get the status of a specific operation</br>
     * response by STATUS_OF_OPERATION_RESTART_RES, STATUS_OF_OPERATION_TEST_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.GetOperationStatusAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.GetOperationStatusAgentEventListener}</br>
     */
    GET_STATUS_OF_OPERATION_REQ(200, TemplateId.TYPE.REQ),
    /**
     * update the status of a specific operation</br>
     * response by UPDATE_OPERATION_SUCCESS_STATUS_RES, UPDATE_OPERATION_FAILED_STATUS_RES,
     * UPDATE_OPERATION_EXECUTING_STATUS_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener}</br>
     */
    UPDATE_STATUS_OF_OPERATION_REQ(300, TemplateId.TYPE.REQ),
    /**
     * update supported operations</br>
     * response by UPDATE_SUPPORTED_OPERATIONS_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener}</br>
     */
    UPDATE_SUPPORTED_OPERATIONS_REQ(607, TemplateId.TYPE.REQ),
    /**
     * update harware</br>
     * response by UPDATE_HARDWARE_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener}</br>
     */
    UPDATE_HARDWARE_REQ(610, TemplateId.TYPE.REQ), UPDATE_HARDWARE_EMPTY_REQ(611, TemplateId.TYPE.REQ),
    /**
     * update software lis</br>
     * response by UPDATE_SOFTWARE_LIST_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener}</br>
     */
    UPDATE_SOFTWARE_LIST_REQ(613, TemplateId.TYPE.REQ), UPDATE_SOFTWARE_LIST_EMPTY_REQ(614, TemplateId.TYPE.REQ),
    /**
     * update mobile</br>
     * response by UPDATE_MOBILE_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener}</br>
     */
    UPDATE_MOBILE_REQ(616, TemplateId.TYPE.REQ), UPDATE_MOBILE_EMPTY_REQ(617, TemplateId.TYPE.REQ),
    /**
     * update firmware</br>
     * response by UPDATE_FIRMWARE_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener}</br>
     */
    UPDATE_FIRMWARE_REQ(619, TemplateId.TYPE.REQ), UPDATE_FIRMWARE_EMPTY_REQ(620, TemplateId.TYPE.REQ),
    /**
     * update configuration</br>
     * response by UPDATE_CONFIGURATION_RES</br>
     * event by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEvent}</br>
     * listener by {@link com.telekom.cot.device.agent.platform.mqtt.event.UpdateFragmentAgentEventListener}</br>
     */
    UPDATE_CONFIGURATION_REQ(622, TemplateId.TYPE.REQ), UPDATE_CONFIGURATION_EMPTY_REQ(623, TemplateId.TYPE.REQ),
    // ***********************
    // Responses
    // ***********************
    USED_TEMPLATE(87, TemplateId.TYPE.RES),
    ERROR_TEMPLATE_NOT_FOUND(40, TemplateId.TYPE.ERROR),
    ERROR_TEMPLATE_CREATION(41, TemplateId.TYPE.ERROR),
    ERROR_MALFORMED_REQUEST_LINE(42, TemplateId.TYPE.ERROR),
    ERROR_INVALID_MESSAGE_IDENTIFIER(43, TemplateId.TYPE.ERROR),
    ERROR_INVALID_MESSAGE_ARGUMENTS(45, TemplateId.TYPE.ERROR),
    ERROR_SERVER_ERROR(50, TemplateId.TYPE.ERROR),
    /**
     * response of GET_MANAGED_OBJECT_ID_REQ
     */
    GET_MANAGED_OBJECT_ID_RES(601, TemplateId.TYPE.RES),
    /**
     * response of GET_MANAGED_OBJECT_ID_REQ
     */
    CREATE_MANAGED_OBJECT_ID_RES(603, TemplateId.TYPE.RES),
    /**
     * response of CREATE_EVENT_REQ
     */
    EVENT_STARTUP_RES(401, TemplateId.TYPE.RES),
    /**
     * response of CREATE_MEASUREMENT_REQ
     */
    CREATE_MEASUREMENT_RES(101, TemplateId.TYPE.RES),
    /**
     * response of CREATE_ALARM_REQ
     */
    CREATE_ALARM_RES(451, TemplateId.TYPE.RES),
    /**
     * response of GET_STATUS_OF_OPERATION_REQ
     */
    STATUS_OF_OPERATION_RESTART_RES(210, TemplateId.TYPE.RES),
    /**
     * response of GET_STATUS_OF_OPERATION_REQ
     */
    STATUS_OF_OPERATION_TEST_RES(211, TemplateId.TYPE.RES),
    /**
     * response of SET_STATUS_OF_OPERATION_REQ
     */
    UPDATE_OPERATION_SUCCESSFUL_STATUS_RES(625, TemplateId.TYPE.RES),
    /**
     * response of SET_STATUS_OF_OPERATION_REQ
     */
    UPDATE_OPERATION_FAILED_STATUS_RES(626, TemplateId.TYPE.RES),
    /**
     * response of SET_STATUS_OF_OPERATION_REQ
     */
    UPDATE_OPERATION_EXECUTING_STATUS_RES(627, TemplateId.TYPE.RES),
    /**
     * response of UPDATE_SUPPORTED_OPERATIONS_REQ
     */
    UPDATE_SUPPORTED_OPERATIONS_RES(608, TemplateId.TYPE.RES),
    /**
     * response of UPDATE_HARDWARE_REQ
     */
    UPDATE_HARDWARE_RES(612, TemplateId.TYPE.RES),
    /**
     * response of UPDATE_SOFTWARE_LIST_REQ
     */
    UPDATE_SOFTWARE_LIST_RES(615, TemplateId.TYPE.RES),
    /**
     * response of UPDATE_MOBILE_REQ
     */
    UPDATE_MOBILE_RES(618, TemplateId.TYPE.RES),
    /**
     * response of UPDATE_FIRMWARE_REQ
     */
    UPDATE_FIRMWARE_RES(621, TemplateId.TYPE.RES),
    /**
     * response of UPDATE_CONFIGURATION_REQ
     */
    UPDATE_CONFIGURATION_RES(624, TemplateId.TYPE.RES),
    /**
     * response of restart operation (no request)
     */
    OPERATION_RESTART_RES(510, TemplateId.TYPE.RES),
    /**
     * response of test operation (no request)
     */
    OPERATION_TEST_RES(511, TemplateId.TYPE.RES),
    /**
     * response of configuration update operation (no request)
     */
    OPERATION_CONFIGURATION_RES(512, TemplateId.TYPE.RES);

    /**
     * Is this a template Id of a request (subscription) or response (publishing).
     *
     */
    enum TYPE {
        NA, REQ, RES, ERROR;
    }

    /** The template id. */
    private int id;
    /** The template type */
    private TYPE type;

    TemplateId(int id, TYPE type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public TYPE getType() {
        return type;
    }

    /**
     * Find response template by id.
     * 
     * @param id
     *            the string template id
     * @return the suitable TemplateId
     */
    public static TemplateId findByResId(String id) {
        return Arrays.asList(values()).stream()
                        .filter(templateId -> templateId.getType() == TYPE.RES
                                        && String.valueOf(templateId.getId()).equals(id))
                        .findFirst().orElse(NOT_AVAILABLE);
    }

    /**
     * Find response template by name.
     * 
     * @param id
     *            the string template id
     * @return the suitable TemplateId
     */
    public static TemplateId findByResName(String name) {
        return Arrays.asList(values()).stream()
                        .filter(templateId -> templateId.getType() == TYPE.RES
                                        && String.valueOf(templateId.toString()).equals(name))
                        .findFirst().orElse(NOT_AVAILABLE);
    }

    /**
     * Find valid responses.
     * 
     * @param line
     * @return
     */
    public static boolean isValidRes(String line) {
        return Arrays.asList(values()).stream()
                        // filter only response
                        .filter(templateId -> templateId.getType() == TYPE.RES
                                        && line.startsWith(String.valueOf(templateId.getId()+",")))
                        // find
                        .findFirst().isPresent();
    }
    
    /**
     * Find error responses.
     * 
     * @param line
     * @return
     */
    public static boolean isErrorRes(String line) {
        return Arrays.asList(values()).stream()
                        // filter only response
                        .filter(templateId -> templateId.getType() == TYPE.ERROR
                                        && line.startsWith(String.valueOf(templateId.getId()+",")))
                        // find
                        .findFirst().isPresent();
    }
}
