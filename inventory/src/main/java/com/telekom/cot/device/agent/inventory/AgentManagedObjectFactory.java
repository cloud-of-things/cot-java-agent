package com.telekom.cot.device.agent.inventory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.platform.objects.AgentConfiguration;
import com.telekom.cot.device.agent.platform.objects.AgentFirmware;
import com.telekom.cot.device.agent.platform.objects.AgentFragment;
import com.telekom.cot.device.agent.platform.objects.AgentFragmentIdentifier;
import com.telekom.cot.device.agent.platform.objects.AgentHardware;
import com.telekom.cot.device.agent.platform.objects.AgentManagedObject;
import com.telekom.cot.device.agent.platform.objects.AgentMobile;
import com.telekom.cot.device.agent.platform.objects.AgentSoftwareList;
import com.telekom.cot.device.agent.system.SystemService;
import com.telekom.cot.device.agent.system.properties.ConfigurationProperties;
import com.telekom.cot.device.agent.system.properties.FirmwareProperties;
import com.telekom.cot.device.agent.system.properties.HardwareProperties;
import com.telekom.cot.device.agent.system.properties.MobileProperties;
import com.telekom.cot.device.agent.system.properties.Properties;
import com.telekom.cot.device.agent.system.properties.SoftwareProperties;

public class AgentManagedObjectFactory {

	private static final String LOG_CREATE_FRAGMENT = "create fragment {}";
	private static final String IS_DEVICE_FRAGMENT = "c8y_IsDevice";
	private static final String IS_AGENT_FRAGMENT = "com_cumulocity_model_Agent";
	private static final String NOT_AVAILABLE = "-";
	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

	private final SystemService systemService;
	private final InventoryServiceConfiguration config;

	private AgentManagedObjectFactory(InventoryServiceConfiguration config, SystemService systemService) {
		this.systemService = systemService;
		this.config = config;
	}

	public static AgentManagedObjectFactory getInstance(InventoryServiceConfiguration config,
			SystemService systemService) {
		return new AgentManagedObjectFactory(config, systemService);
	}

	public AgentManagedObject create(String name) {
		return initAgentManagedObject(name, config, new AgentManagedObject());
	}

	public AgentManagedObject create() {
		return initAgentManagedObject(null, config, new AgentManagedObject());
	}

	private AgentManagedObject initAgentManagedObject(String name, InventoryServiceConfiguration config,
			AgentManagedObject agentManagedObject) {
		if (Objects.isNull(name)) {
			agentManagedObject.setName(config.getDeviceName());
		} else {
			agentManagedObject.setName(name);
		}
		agentManagedObject.setType(config.getDeviceType());
		// set as device (is able to send measurements)
		if (config.isDevice()) {
			agentManagedObject.set(IS_DEVICE_FRAGMENT, new JsonObject());
		}
		// set as agent (is able to execute operations)
		if (config.isAgent()) {
			agentManagedObject.set(IS_AGENT_FRAGMENT, new JsonObject());
		}
		// hardware properties
		addFragment(agentManagedObject, createHardware(getProperties(HardwareProperties.class)));
		// configuration properties
		addFragment(agentManagedObject, createConfiguration(getProperties(ConfigurationProperties.class)));
		// firmware properties
		addFragment(agentManagedObject, createFirmware(getProperties(FirmwareProperties.class)));
		// mobile properties
		addFragment(agentManagedObject, createMobile(getProperties(MobileProperties.class)));
		// software list properties
		addFragment(agentManagedObject, createSoftware(getProperties(SoftwareProperties.class)));
		return agentManagedObject;
	}

	private <T extends Properties> T getProperties(Class<T> clazz) {
		LOGGER.info("get system properties {}", clazz.getName());
		try {
			return systemService.getProperties(clazz);
		} catch (AbstractAgentException e) {
			LOGGER.info("did not find system properties {}", clazz.getName(), e);
			return null;
		}
	}

	private void addFragment(AgentManagedObject agentManagedObject, AgentFragment agentFragment) {
		LOGGER.info("add fragment {}", agentFragment.getId());
		agentManagedObject.addFragment(agentFragment);
	}

	private AgentFragment createHardware(HardwareProperties hardwareProperties) {
		if (Objects.isNull(hardwareProperties)) {
			return new EmptyFragment(AgentFragmentIdentifier.HARDWARE);
		}
		LOGGER.info(LOG_CREATE_FRAGMENT, hardwareProperties);
		return new AgentHardware(check(hardwareProperties.getModel(), NOT_AVAILABLE),
				check(hardwareProperties.getRevision(), NOT_AVAILABLE),
				check(hardwareProperties.getSerialNumber(), NOT_AVAILABLE));
	}

	private AgentFragment createConfiguration(ConfigurationProperties configurationProperties) {
		if (Objects.isNull(configurationProperties)) {
			return new EmptyFragment(AgentFragmentIdentifier.CONFIGURATION);
		}
		LOGGER.info(LOG_CREATE_FRAGMENT, configurationProperties);
		return new AgentConfiguration(configurationProperties.getConfig());
	}

	private AgentFragment createFirmware(FirmwareProperties firmwareProperties) {
		if (Objects.isNull(firmwareProperties)) {
			return new EmptyFragment(AgentFragmentIdentifier.FIRMWARE);
		}
		LOGGER.info(LOG_CREATE_FRAGMENT, firmwareProperties);
		return new AgentFirmware(check(firmwareProperties.getName(), NOT_AVAILABLE),
				check(firmwareProperties.getVersion(), NOT_AVAILABLE),
				check(firmwareProperties.getUrl(), NOT_AVAILABLE));
	}

	private AgentFragment createMobile(MobileProperties mobileProperties) {
		if (Objects.isNull(mobileProperties)) {
			return new EmptyFragment(AgentFragmentIdentifier.MOBILE);
		}
		LOGGER.info(LOG_CREATE_FRAGMENT, mobileProperties);
		return new AgentMobile(check(mobileProperties.getImei(), NOT_AVAILABLE),
				check(mobileProperties.getCellId(), NOT_AVAILABLE), check(mobileProperties.getIccid(), NOT_AVAILABLE));
	}

	private AgentFragment createSoftware(SoftwareProperties softwareProperties) {
		if (Objects.isNull(softwareProperties) || CollectionUtils.isEmpty(softwareProperties.getSoftwareList())) {
			return new EmptyFragment(AgentFragmentIdentifier.SOFTWARE_LIST);
		}
		LOGGER.info(LOG_CREATE_FRAGMENT, softwareProperties);
		List<AgentSoftwareList.Software> softwares = softwareProperties.getSoftwareList().stream()
				.map(s -> new AgentSoftwareList.Software(check(s.getName(), NOT_AVAILABLE),
						check(s.getVersion(), NOT_AVAILABLE), check(s.getUrl(), NOT_AVAILABLE)))
				.collect(Collectors.toList());
		return new AgentSoftwareList(softwares.toArray(new AgentSoftwareList.Software[] {}));
	}

	private String check(String value, String defaultValue) {
		return Objects.isNull(value) || value.isEmpty() ? defaultValue : value;
	}

	/**
	 * Create an empty fragment.
	 *
	 */
	static class EmptyFragment implements AgentFragment {

		private AgentFragmentIdentifier identifier;

		public EmptyFragment(AgentFragmentIdentifier identifier) {
			this.identifier = identifier;
		}

		@Override
		public String getId() {
			return identifier.getId();
		}

		@Override
		public JsonElement getJson() {
			return new JsonObject();
		}
	}
}
