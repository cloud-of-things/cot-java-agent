package com.telekom.cot.device.agent.platform.mqtt;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.platform.mqtt.TemplateId.TYPE;
import com.telekom.cot.device.agent.platform.objects.AgentConfiguration;
import com.telekom.cot.device.agent.platform.objects.AgentFirmware;
import com.telekom.cot.device.agent.platform.objects.AgentHardware;
import com.telekom.cot.device.agent.platform.objects.AgentMobile;
import com.telekom.cot.device.agent.platform.objects.AgentSoftwareList.Software;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SmartRestUtil {

	private static final String REGEX_VALID_ID = "(\\$)(\\{)([A-Z]|_){1,50}(\\})";
	private static final String REGEX_INVALID_ID = "(\\$)(\\{)(.*)(\\})";
	private static final String COT_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private static final Logger LOGGER = LoggerFactory.getLogger(SmartRestUtil.class);

	/**
	 * gets payload to retrieve a managed object id for a given ICCID
	 * 
	 * @param xID
	 *            template collection id
	 * @param iccId
	 *            ICCID
	 * @return payload
	 */
	public static String getPayloadManagedObjectId(final String xID, final String iccId) {
		String payload = "15," + xID + "\n" + TemplateId.GET_MANAGED_OBJECT_ID_REQ.getId() + "," + iccId;
		LOGGER.debug("request {} to get managed id: {}", TemplateId.GET_MANAGED_OBJECT_ID_REQ, payload);
		return payload;
	}

	/**
	 * gets payload to create a measurement
	 * 
	 * @param xID
	 *            template collection id
	 * @param type
	 *            type
	 * @param value
	 *            value
	 * @param unit
	 *            unit
	 * @param time
	 *            time
	 * @param managedObjectId
	 * @return payload
	 */
	public static String getPayloadCreateMeasurement(final String xID, final String type, final float value,
			final String unit, final Date time, final String managedObjectId) {
		String valueString = String.valueOf(value);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(COT_TIME_PATTERN);
		String dateString = simpleDateFormat.format(time);
		String payload = "15," + xID + "\n" + TemplateId.CREATE_MEASUREMENT_REQ.getId() + "," + type + ",0,"
				+ valueString + "," + unit + "," + dateString + "," + managedObjectId + "," + type;
		LOGGER.debug("request {} to create measurement: {}", TemplateId.CREATE_MEASUREMENT_REQ, payload);
		return payload;
	}

	/**
	 * gets payload to create an event
	 * 
	 * @param xId
	 *            template collection id
	 * @param time
	 *            time
	 * @param type
	 *            type
	 * @param text
	 *            text
	 * @param managedObjectId
	 *            managed object id
	 * @param condition
	 *            condition to identify the response
	 * @param conditionValue
	 *            condition value
	 * @return payload
	 */
	public static String getPayloadCreateEvent(String xId, Date time, String type, String text, String managedObjectId,
			String condition, String conditionValue) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(COT_TIME_PATTERN);
		String dateString = simpleDateFormat.format(time);
		String payload = "15," + xId + "\n" + TemplateId.CREATE_EVENT_REQ.getId() + "," + condition + ","
				+ conditionValue + "," + managedObjectId + "," + dateString + "," + type + "," + text;
		LOGGER.debug("request {} to create event: {}", TemplateId.CREATE_EVENT_REQ, payload);
		return payload;
	}

	/**
	 * gets payload to create an alarm
	 * 
	 * @param xId
	 *            template collection id
	 * @param time
	 *            time
	 * @param type
	 *            type
	 * @param severity
	 *            severity
	 * @param text
	 *            text
	 * @param status
	 *            status
	 * @param managedObjectId
	 *            managed object id
	 * @return payload
	 */
	public static String getPayloadCreateAlarm(String xId, Date time, String type, AlarmSeverity severity, String text,
			String status, String managedObjectId) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(COT_TIME_PATTERN);
		String dateString = simpleDateFormat.format(time);
		String payload = "15," + xId + "\n" + TemplateId.CREATE_ALARM_REQ.getId() + "," + type + "," + dateString + ","
				+ text + "," + status + "," + severity.getValue() + "," + managedObjectId;
		LOGGER.debug("request {} to create alarm: {}", TemplateId.CREATE_ALARM_REQ, payload);
		return payload;
	}

	/**
	 * gets payload to get the list of operations having a given status and a given
	 * fragment
	 * 
	 * @param xId
	 *            template collection id
	 * @param managedObjectId
	 *            managed object id
	 * @param status
	 *            status to query
	 * @param fragmentType
	 *            frament type to query
	 * @return payload
	 */
	public static String getPayloadGetOperationStatus(String xId, String managedObjectId, String status,
			String fragmentType) {
		String payload = "15," + xId + "\n" + TemplateId.GET_STATUS_OF_OPERATION_REQ.getId() + "," + managedObjectId
				+ "," + status + "," + fragmentType;
		LOGGER.debug("request {} to get list of operations: {}", TemplateId.GET_STATUS_OF_OPERATION_REQ, payload);
		return payload;
	}

	/**
	 * gets payload to update the status of a given operation
	 * 
	 * @param xId
	 *            template collection id
	 * @param operationId
	 *            id of the operation to update
	 * @param status
	 *            operation status
	 * @return payload
	 */
	public static String getPayloadPutOperationStatus(String xId, String operationId, String status) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(COT_TIME_PATTERN);
        String dateString = simpleDateFormat.format(new Date());
		String payload = "15," + xId + "\n" + TemplateId.UPDATE_STATUS_OF_OPERATION_REQ.getId() + "," + operationId + ","
				+ status + "," + status + "," + dateString;
		LOGGER.debug("request {} to update the operation status: {}", TemplateId.UPDATE_STATUS_OF_OPERATION_REQ, payload);
		return payload;
	}

	/**
	 * gets payload to update the c8y_Hardware fragment
	 * 
	 * @param xId
	 *            template collection id
	 * @param managedObjectId
	 *            managed object id
	 * @param agentHardware
	 *            hardware details to update
	 * @param isFragmentEmpty
	 *            true if the c8y_Hardware fragment should be empty, false otherwise
	 * @return payload
	 * @throws PlatformServiceException
	 *             if the payload cannot be computed
	 */
	public static String getPayloadUpdateHardware(String xId, String managedObjectId, AgentHardware agentHardware,
			boolean isFragmentEmpty) throws PlatformServiceException {
		if (!isFragmentEmpty && Objects.isNull(agentHardware)) {
			throw new PlatformServiceException(
					"can't get payload to update hardware because agentHardware is null");
		}

        TemplateId templateId = isFragmentEmpty 
                        ? TemplateId.UPDATE_HARDWARE_EMPTY_REQ 
                        : TemplateId.UPDATE_HARDWARE_REQ;

		String payload = isFragmentEmpty
				? "15," + xId + "\n" + templateId.getId() + "," + managedObjectId
				: "15," + xId + "\n" + templateId.getId() + "," + managedObjectId + ","
						+ agentHardware.getModel() + "," + agentHardware.getRevision() + ","
						+ agentHardware.getSerialNumber();
		LOGGER.debug("request {} to update the hardware fragment: {}", templateId, payload);
		return payload;
	}

	/**
	 * gets payload to update the c8y_Firmware fragment
	 * 
	 * @param xId
	 *            template collection id
	 * @param managedObjectId
	 *            managed object id
	 * @param agentFirmware
	 *            firmware details to update
	 * @param isFragmentEmpty
	 *            true if the c8y_Firmware fragment should be empty, false otherwise
	 * @return payload
	 * @throws PlatformServiceException
	 *             if the payload cannot be computed
	 */
	public static String getPayloadUpdateFirmware(String xId, String managedObjectId, AgentFirmware agentFirmware,
			boolean isFragmentEmpty) throws PlatformServiceException {
		if (!isFragmentEmpty && Objects.isNull(agentFirmware)) {
			throw new PlatformServiceException(
					"can't get payload to update firmware because agentFirmware is null");
		}
        
		TemplateId templateId = isFragmentEmpty 
                        ? TemplateId.UPDATE_FIRMWARE_EMPTY_REQ
                        : TemplateId.UPDATE_FIRMWARE_REQ;
        
		String payload = isFragmentEmpty
				? "15," + xId + "\n" + templateId.getId() + "," + managedObjectId
				: "15," + xId + "\n" + templateId.getId() + "," + managedObjectId + ","
						+ agentFirmware.getName() + "," + agentFirmware.getVersion() + "," + agentFirmware.getUrl();
		LOGGER.debug("request {} to update the firmware fragment: {}", templateId, payload);
		return payload;
	}

	/**
	 * gets payload to update the c8y_Mobile fragment
	 * 
	 * @param xId
	 *            template collection id
	 * @param managedObjectId
	 *            managed object id
	 * @param agentMobile
	 *            mobile details to update
	 * @param isFragmentEmpty
	 *            true if the c8y_Mobile fragment should be empty, false otherwise
	 * @return payload
	 * @throws PlatformServiceException
	 *             if the payload cannot be computed
	 */
	public static String getPayloadUpdateMobile(String xId, String managedObjectId, AgentMobile agentMobile,
			boolean isFragmentEmpty) throws PlatformServiceException {
		if (!isFragmentEmpty && Objects.isNull(agentMobile)) {
			throw new PlatformServiceException("can't get payload to update mobile because agentMobile is null");
		}

        TemplateId templateId = isFragmentEmpty 
                        ? TemplateId.UPDATE_MOBILE_EMPTY_REQ
                        : TemplateId.UPDATE_MOBILE_REQ;
        
		String payload = isFragmentEmpty
				? "15," + xId + "\n" + templateId.getId() + "," + managedObjectId
				: "15," + xId + "\n" + templateId.getId() + "," + managedObjectId + ","
						+ agentMobile.getImei() + "," + agentMobile.getCellId() + "," + agentMobile.getIccid();
		LOGGER.debug("request {} to update the mobile fragment: {}", templateId, payload);
		return payload;
	}

	/**
	 * gets payload to update the c8y_SoftwareList fragment
	 * 
	 * @param xId
	 *            template collection id
	 * @param managedObjectId
	 *            managed object id
	 * @param software
	 *            software details to update
	 * @param isFragmentEmpty
	 *            true if the c8y_SoftwareList fragment should be empty, false
	 *            otherwise
	 * @return payload
	 * @throws PlatformServiceException
	 *             if the payload cannot be computed
	 */
	public static String getPayloadUpdateSoftwareList(String xId, String managedObjectId, Software software,
			boolean isFragmentEmpty) throws PlatformServiceException {
		if (!isFragmentEmpty && Objects.isNull(software)) {
			throw new PlatformServiceException(
					"can't get payload to update software because software is null");
		}

        TemplateId templateId = isFragmentEmpty 
                        ? TemplateId.UPDATE_SOFTWARE_LIST_EMPTY_REQ
                        : TemplateId.UPDATE_SOFTWARE_LIST_REQ;
		String payload = isFragmentEmpty
				? "15," + xId + "\n" + templateId.getId() + "," + managedObjectId
				: "15," + xId + "\n" + templateId.getId() + "," + managedObjectId + ","
						+ software.name + "," + software.version + "," + software.url;
		LOGGER.debug("request {} to update the software fragment: {}", templateId, payload);
		return payload;
	}

	/**
	 * gets payload to update the c8y_Configuration fragment
	 * 
	 * @param xId
	 *            template collection id
	 * @param managedObjectId
	 *            managed object id
	 * @param agentConfiguration
	 *            configuration details to update
	 * @param isFragmentEmpty
	 *            true if the c8y_Configuration fragment should be empty, false
	 *            otherwise
	 * @return payload
	 * @throws PlatformServiceException
	 *             if the payload cannot be computed
	 */
	public static String getPayloadUpdateConfiguration(String xId, String managedObjectId,
			AgentConfiguration agentConfiguration, boolean isFragmentEmpty) throws PlatformServiceException {
		if (!isFragmentEmpty && Objects.isNull(agentConfiguration)) {
			throw new PlatformServiceException(
					"can't get payload to update configuration because agentConfiguration is null");
		}

		TemplateId templateId = isFragmentEmpty 
		                ? TemplateId.UPDATE_CONFIGURATION_EMPTY_REQ
		                : TemplateId.UPDATE_CONFIGURATION_REQ;
		String payload = isFragmentEmpty
				? "15," + xId + "\n" + templateId.getId() + "," + managedObjectId
				: "15," + xId + "\n" + templateId.getId() + "," + managedObjectId + ","
						// put config into "" and escape \n
						+ '\"' + agentConfiguration.getConfig().replaceAll("\n", "\\\\n") + '\"';
		LOGGER.debug("request {} to update the configuration: {}", templateId, payload);
		return payload;
	}

	/**
	 * gets payload to update the c8y_SupportedOperations fragment
	 * 
	 * @param xId
	 *            template collection id
	 * @param managedObjectId
	 *            managed object id
	 * @param supportedOperationNames
	 *            supported operations list
	 * @return payload
	 */
	public static String getPayloadUpdateSupportedOperations(String xId, String managedObjectId,
			List<String> supportedOperationNames) {
		String payload = "";
		TemplateId templateId = TemplateId.UPDATE_SUPPORTED_OPERATIONS_REQ;
		if (Objects.isNull(supportedOperationNames) || supportedOperationNames.size() == 0) {
			// 15,xId
			// 607,mOId,""
			payload = "15," + xId + "\n" + templateId.getId() + "," + managedObjectId
					+ ",\"\"";
		} else if (supportedOperationNames.size() == 1) {
			// 15,xId
			// 607,mOId,"c8y_Restart"
			payload = "15," + xId + "\n" + templateId.getId() + "," + managedObjectId + ",\""
					+ supportedOperationNames.get(0) + "\"";
		} else {
			// 15,xId
			// 607,mOId,"c8y_Restart"",""c8y_Command"
			payload = "15," + xId + "\n" + templateId.getId() + "," + managedObjectId + ",";
			for (String supportedOperationName : supportedOperationNames) {
				if (supportedOperationName.equals(supportedOperationNames.get(0))) {
					payload += "\"" + supportedOperationName + "\"\",";
				} else if (supportedOperationName
						.equals(supportedOperationNames.get(supportedOperationNames.size() - 1))) {
					payload += "\"\"" + supportedOperationName + "\"";
				} else {
					payload += "\"\"" + supportedOperationName + "\"\",";
				}
			}
		}

		LOGGER.debug("request {} to update the update supported operations: {}", templateId, payload);
		return payload;
	}

	public static byte[] readTemplates(String root, String xid, TemplateId[] templateIds) {
		List<String> templates = new ArrayList<>();
		List<String> collections = read(root, "mqtt.template.collections");
		for (String line : collections) {
			line = line.trim();
			if (line.startsWith("#") || line.isEmpty()) {
				continue;
			}
			if (line.startsWith("xid")) {
				line = line.substring(4);
				templates.add(line.replace("${X-ID}", xid));
			} else if (line.startsWith("template=")) {
				templates.addAll(prepare(read(root, line.substring(9))));
			}
		}
		replaceIds(templates, templateIds);
		verify(templates);
		return toBytes(templates);
	}

	/*
	 * private methods
	 */
	private static void verify(List<String> templates) {
		Pattern pattern = Pattern.compile(REGEX_INVALID_ID);
		for (String template : templates) {
			Matcher matcher = pattern.matcher(template);
			if (matcher.find()) {
				// TODO exception
			}
		}
	}

	private static byte[] toBytes(List<String> templates) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		for (String template : templates) {
			try {
				os.write(template.getBytes());
				os.write('\n');
			} catch (IOException e) {
				LOGGER.error("IOException: {}", e);
			} finally {
				try {
					os.close();
				} catch (IOException e) {
                    LOGGER.error("IOException: {}", e);

                }
			}
		}
		return os.toByteArray();
	}

	private static void replaceIds(List<String> templates, TemplateId[] templateIds) {
		Pattern pattern = Pattern.compile(REGEX_VALID_ID);
		List<String> removeTemplates = new ArrayList<>();
		List<String> changedTemplates = new ArrayList<>();
		for (String template : templates) {
			Matcher matcher = pattern.matcher(template);
			if (!matcher.find()) {
				continue;
			}
			TemplateId templateId = findTemplateId(template, templateIds);
			if (Objects.isNull(templateId)) {
				// throw new Exc...
			}
			if (template.startsWith("10") && templateId.getType() == TYPE.REQ) {
				changedTemplates.add(template.replaceFirst(REGEX_VALID_ID, String.valueOf(templateId.getId())));
			} else if (template.startsWith("11") && templateId.getType() == TYPE.RES) {
				changedTemplates.add(template.replaceFirst(REGEX_VALID_ID, String.valueOf(templateId.getId())));
			}
			removeTemplates.add(template);
		}
		if (!removeTemplates.isEmpty()) {
			templates.removeAll(removeTemplates);
		}
		if (!changedTemplates.isEmpty()) {
			templates.addAll(changedTemplates);
		}
	}

	private static TemplateId findTemplateId(String template, TemplateId[] templateIds) {
		TemplateId templateId = null;
		for (TemplateId id : templateIds) {
			if (template.indexOf("${" + id.name() + "}") > 0) {
				templateId = id;
				break;
			}
		}
		return templateId;
	}

	private static List<String> prepare(List<String> lines) {
		List<String> templates = new ArrayList<>();
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith("#") || line.isEmpty()) {
				continue;
			}
			templates.add(line);
		}
		return templates;
	}

	private static List<String> read(String root, String source) {
		InputStream inputStream = SmartRestUtil.class.getResourceAsStream(root + File.separator + source);
		List<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines()
				.collect(Collectors.toList());
		return lines;
	}

	public static void main(String[] args) {
		System.out.println(new String(readTemplates("/templates", "novaTemplates", TemplateId.values())));
	}
}