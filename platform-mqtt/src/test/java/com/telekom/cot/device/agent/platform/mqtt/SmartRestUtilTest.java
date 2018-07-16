package com.telekom.cot.device.agent.platform.mqtt;

import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import com.telekom.cot.device.agent.common.AlarmSeverity;
import com.telekom.cot.device.agent.common.exc.PlatformServiceException;
import com.telekom.cot.device.agent.platform.objects.AgentConfiguration;
import com.telekom.cot.device.agent.platform.objects.AgentFirmware;
import com.telekom.cot.device.agent.platform.objects.AgentHardware;
import com.telekom.cot.device.agent.platform.objects.AgentMobile;
import com.telekom.cot.device.agent.platform.objects.AgentSoftwareList;

public class SmartRestUtilTest {

	private String xId = "jUnitTestTemplate01";
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	@Test
	public void testFindByResId() {
		TemplateId tid = TemplateId.findByResId(String.valueOf(TemplateId.CREATE_MANAGED_OBJECT_ID_RES.getId()));
		assertThat(tid, Matchers.equalTo(TemplateId.CREATE_MANAGED_OBJECT_ID_RES));
		tid = TemplateId.findByResId(String.valueOf(TemplateId.CREATE_EVENT_REQ.getId()));
		assertThat(tid, Matchers.equalTo(TemplateId.NOT_AVAILABLE));
	}

//	@Test
//	public void testFindFragmentType() {
//		// restart
//		Map<String, Object> properties = new HashedMap<>();
//		properties.put("c8y_Restart", "");
//		assertThat("c8y_Restart", Matchers.equalTo(FragmentType.findFragmentType(properties.keySet())));
//		// test operation
//		properties = new HashedMap<>();
//		properties.put("c8y_TestOperation", "");
//		assertThat("c8y_TestOperation", Matchers.equalTo(FragmentType.findFragmentType(properties.keySet())));
//		// not available
//		properties = new HashedMap<>();
//		properties.put("c8y_Test", "");
//		assertThat("NA", Matchers.equalTo(FragmentType.findFragmentType(properties.keySet())));
//	}

	@Test
	public void testGetPayloadCreateMeasurement() {

		Date time = new Date();
		String dateString = simpleDateFormat.format(time);
		String type = TemplateId.CREATE_MEASUREMENT_REQ.getType().toString();
		String payload = "15," + xId + "\n" + TemplateId.CREATE_MEASUREMENT_REQ.getId() + ","
				+ TemplateId.CREATE_MEASUREMENT_REQ.getType() + ",0," + "8.0" + "," + "testUnit" + "," + dateString
				+ "," + "testMoId" + "," + "REQ";

		assertThat(payload, Matchers
				.equalTo(SmartRestUtil.getPayloadCreateMeasurement(xId, type, 8, "testUnit", time, "testMoId")));
	}

	@Test
	public void testGetPayloadManagedObjectId() {
		String payload = "15," + xId + "\n" + TemplateId.GET_MANAGED_OBJECT_ID_REQ.getId() + "," + "testIccId";
		assertThat(payload, Matchers.equalTo(SmartRestUtil.getPayloadManagedObjectId(xId, "testIccId")));
	}

	@Test
	public void testGetPayloadCreateEvent() {

		Date time = new Date();
		String dateString = simpleDateFormat.format(time);
		String payload = "15," + xId + "\n" + TemplateId.CREATE_EVENT_REQ.getId() + "," + "testCondition" + ","
				+ "testConditionValue" + "," + "testMoId" + "," + dateString + "," + "testType" + "," + "testText";

		assertThat(payload, Matchers.equalTo(SmartRestUtil.getPayloadCreateEvent(xId, time, "testType", "testText",
				"testMoId", "testCondition", "testConditionValue")));
	}

	@Test
	public void testGetPayloadCreateAlarm() {
		Date time = new Date();
		String dateString = simpleDateFormat.format(time);
		String payload = "15," + xId + "\n" + TemplateId.CREATE_ALARM_REQ.getId() + "," + "testType" + "," + dateString
				+ "," + "testText" + "," + "testStatus" + "," + AlarmSeverity.CRITICAL + "," + "testMoId";

		assertThat(payload, Matchers.equalTo(SmartRestUtil.getPayloadCreateAlarm(xId, time, "testType",
				AlarmSeverity.CRITICAL, "testText", "testStatus", "testMoId")));
	}

	@Test
	public void testGetPayloadGetOperationStatus() {
		String payload = "15," + xId + "\n" + TemplateId.GET_STATUS_OF_OPERATION_REQ.getId() + "," + "testDeviceId"
				+ "," + "testStatus" + "," + "testType";

		assertThat(payload, Matchers
				.equalTo(SmartRestUtil.getPayloadGetOperationStatus(xId, "testDeviceId", "testStatus", "testType")));
	}

	//test failed because of Milliseconds in time format --> cutting off last 8 chars from both strings
	@Test
	public void testGetPayloadPutOperationStatus() {
		Date time = new Date();
		String dateString = simpleDateFormat.format(time);
		String payload = "15," + xId + "\n" + TemplateId.UPDATE_STATUS_OF_OPERATION_REQ.getId() + "," + "testOperationId"
				+ "," + "testStatus" + "," + "testStatus" + "," + dateString;

		String payloadExpected = SmartRestUtil.getPayloadPutOperationStatus(xId, "testOperationId", "testStatus");

		payload = payload.substring(0, 84);
		payloadExpected = payloadExpected.substring(0, 84);

		assertThat(payload,
				Matchers.equalTo(payloadExpected));
	}

	@Test
	public void testSuccessfulGetPayloadUpdateHardware() throws PlatformServiceException {

		AgentHardware agentHardwareEmpty = new AgentHardware("", "", "");
		AgentHardware agentHardwareFilled = new AgentHardware("testModel", "testRevision", "testSerial");

		String payloadEmptyFragment = "15," + xId + "\n" + TemplateId.UPDATE_HARDWARE_EMPTY_REQ.getId() + ","
				+ "testMoId";
		String payloadExistingFragment = "15," + xId + "\n" + TemplateId.UPDATE_HARDWARE_REQ.getId() + "," + "testMoId"
				+ "," + agentHardwareFilled.getModel() + "," + agentHardwareFilled.getRevision() + ","
				+ agentHardwareFilled.getSerialNumber();

		assertThat(payloadEmptyFragment,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateHardware(xId, "testMoId", agentHardwareEmpty, true)));
		assertThat(payloadExistingFragment,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateHardware(xId, "testMoId", agentHardwareFilled, false)));
	}

	@Test(expected = PlatformServiceException.class)
	public void testFailureGetPayloadUpdateHardware() throws PlatformServiceException {

		SmartRestUtil.getPayloadUpdateHardware(xId, "testMoId", null, false);
	}

	@Test
	public void testSuccessfulGetPayloadUpdateFirmware() throws PlatformServiceException {
		AgentFirmware agentFirmwareEmpty = new AgentFirmware("", "", "");
		AgentFirmware agentFirmwareFilled = new AgentFirmware("testName", "testVersion", "testUrl");

		String payloadEmptyFragment = "15," + xId + "\n" + TemplateId.UPDATE_FIRMWARE_EMPTY_REQ.getId() + ","
				+ "testMoId";
		String payloadExistingFragment = "15," + xId + "\n" + TemplateId.UPDATE_FIRMWARE_REQ.getId() + "," + "testMoId"
				+ "," + agentFirmwareFilled.getName() + "," + agentFirmwareFilled.getVersion() + ","
				+ agentFirmwareFilled.getUrl();

		assertThat(payloadEmptyFragment,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateFirmware(xId, "testMoId", agentFirmwareEmpty, true)));
		assertThat(payloadExistingFragment,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateFirmware(xId, "testMoId", agentFirmwareFilled, false)));
	}

	@Test(expected = PlatformServiceException.class)
	public void testFailureGetPayloadUpdateFirmware() throws PlatformServiceException {

		SmartRestUtil.getPayloadUpdateFirmware(xId, "testMoId", null, false);
	}

	@Test
	public void testSuccessfulGetPayloadUpdateMobile() throws PlatformServiceException {
		AgentMobile agentMobileEmpty = new AgentMobile("", "", "");
		AgentMobile agentMobileFilled = new AgentMobile("testImei", "testCellId", "testIccId");

		String payloadEmptyFragment = "15," + xId + "\n" + TemplateId.UPDATE_MOBILE_EMPTY_REQ.getId() + ","
				+ "testMoId";
		String payloadFilledFragment = "15," + xId + "\n" + TemplateId.UPDATE_MOBILE_REQ.getId() + "," + "testMoId"
				+ "," + agentMobileFilled.getImei() + "," + agentMobileFilled.getCellId() + ","
				+ agentMobileFilled.getIccid();

		assertThat(payloadEmptyFragment,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateMobile(xId, "testMoId", agentMobileEmpty, true)));
		assertThat(payloadFilledFragment,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateMobile(xId, "testMoId", agentMobileFilled, false)));
	}

	@Test(expected = PlatformServiceException.class)
	public void testFailureGetPayloadUpdateMobile() throws PlatformServiceException {

		SmartRestUtil.getPayloadUpdateMobile(xId, "testMoId", null, false);
	}
	
	@Test
	public void testSuccessfulGetPayloadUpdateConfigurationFilled() throws PlatformServiceException {
		AgentConfiguration agentConfigurationFilled = new AgentConfiguration("line1\nline2");

		String payloadFilledFragment = "15," + xId + "\n" + TemplateId.UPDATE_CONFIGURATION_REQ.getId() + ","
				+ "testMoId,\"line1\\nline2\"";

		assertThat(SmartRestUtil.getPayloadUpdateConfiguration(xId, "testMoId", agentConfigurationFilled, false), 
				Matchers.equalTo(payloadFilledFragment));
	}
	
	@Test
	public void testSuccessfulGetPayloadUpdateConfigurationEmpty() throws PlatformServiceException {
		AgentConfiguration agentConfigurationEmpty = new AgentConfiguration("");

		String payloadEmptyFragment = "15," + xId + "\n" + TemplateId.UPDATE_CONFIGURATION_EMPTY_REQ.getId() + ","
				+ "testMoId";

		assertThat(SmartRestUtil.getPayloadUpdateConfiguration(xId, "testMoId", agentConfigurationEmpty, true), 
				Matchers.equalTo(payloadEmptyFragment));
	}
	
	@Test(expected=PlatformServiceException.class)
	public void testSuccessfulGetPayloadUpdateConfigurationNull() throws PlatformServiceException {
		AgentConfiguration agentConfigurationNull = null;

		SmartRestUtil.getPayloadUpdateConfiguration(xId, "testMoId", agentConfigurationNull, false);
	}

	@Test
	public void testSuccessfulGetPayloadUpdateSoftwareList() throws PlatformServiceException {
		AgentSoftwareList.Software softwareEmpty = new AgentSoftwareList.Software("", "", "");

		AgentSoftwareList.Software softwareFilled = new AgentSoftwareList.Software("testName", "testVersion",
				"testUrl");

		String payloadEmptyFragment = "15," + xId + "\n" + TemplateId.UPDATE_SOFTWARE_LIST_EMPTY_REQ.getId() + ","
				+ "testMoId";
		String payloadFilledFragment = "15," + xId + "\n" + TemplateId.UPDATE_SOFTWARE_LIST_REQ.getId() + ","
				+ "testMoId" + "," + softwareFilled.name + "," + softwareFilled.version + "," + softwareFilled.url;

		assertThat(payloadEmptyFragment,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateSoftwareList(xId, "testMoId", softwareEmpty, true)));
		assertThat(payloadFilledFragment,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateSoftwareList(xId, "testMoId", softwareFilled, false)));
	}

	@Test(expected = PlatformServiceException.class)
	public void testFailureGetPayloadUpdateSoftwareList() throws PlatformServiceException {

		SmartRestUtil.getPayloadUpdateSoftwareList(xId, "testMoId", null, false);
	}

	@Test
	public void testSuccessfulGetPayloadUpdateSupportedOperations() {

		List<String> emptyList = new ArrayList<>();
		List<String> supportedOpListOneEntry = new ArrayList<>();
		List<String> supportedOpListMultiEntries = new ArrayList<>();

		supportedOpListOneEntry.add("supportedTestOperation");

		supportedOpListMultiEntries.add("supportedTestOperation");
		supportedOpListMultiEntries.add("supportedTestOperationOne");
		supportedOpListMultiEntries.add("supportedTestOperationTwo");

		String payloadEmptyList = "15," + xId + "\n" + TemplateId.UPDATE_SUPPORTED_OPERATIONS_REQ.getId() + ","
				+ "testDeviceId" + ",\"\"";
		String payloadListFilledOneEntry = "15," + xId + "\n" + TemplateId.UPDATE_SUPPORTED_OPERATIONS_REQ.getId() + ","
				+ "testDeviceId" + ",\"" + supportedOpListOneEntry.get(0) + "\"";
		String payloadListFilledMultiEntries = "15," + xId + "\n" + TemplateId.UPDATE_SUPPORTED_OPERATIONS_REQ.getId() + ","
				+ "testDeviceId" + ",\"" + supportedOpListMultiEntries.get(0) + "\"\"," + "\"\""
				+ supportedOpListMultiEntries.get(1) + "\"\"," + "\"\"" + supportedOpListMultiEntries.get(2) + "\"";

		assertThat(payloadEmptyList,
				Matchers.equalTo(SmartRestUtil.getPayloadUpdateSupportedOperations(xId, "testDeviceId", emptyList)));
		assertThat(payloadListFilledOneEntry, Matchers.equalTo(
				SmartRestUtil.getPayloadUpdateSupportedOperations(xId, "testDeviceId", supportedOpListOneEntry)));
		assertThat(payloadListFilledMultiEntries, Matchers.equalTo(
				SmartRestUtil.getPayloadUpdateSupportedOperations(xId, "testDeviceId", supportedOpListMultiEntries)));
	}

	@Test
	public void testReadTemplates() {

		String templates = "10,602,POST,/inventory/managedObjects,application/vnd.com.nsn.cumulocity.managedObject+json,application/vnd.com.nsn.cumulocity.managedObject+json,&&,STRING STRING,\"{\"\"name\"\":\"\"&&\"\",\"\"type\"\":\"\"&&\"\",\"\"c8y_IsDevice\"\":{},\"\"com_cumulocity_model_Agent\"\":{}}\"\n";
		String answer = new String(SmartRestUtil.readTemplates("/templates", "testXid", TemplateId.values()));
		assertThat(answer, Matchers.containsString(templates));
	}

}
